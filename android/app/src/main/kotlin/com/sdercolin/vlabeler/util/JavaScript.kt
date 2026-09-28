package com.sdercolin.vlabeler.util

import com.sdercolin.vlabeler.android.js.JavaBridge
import com.sdercolin.vlabeler.android.js.JsValue
import com.sdercolin.vlabeler.android.js.QuickJsRuntime
import com.sdercolin.vlabeler.env.Log
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.serialization.Serializable
import java.io.Closeable
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * A wrapper of JavaScript engine.
 *
 * Android: powered by QuickJS (instead of GraalJS on desktop). Java interoperability (`Java.type(...)`) used by
 * labelers and plugins is provided by a reflection based bridge (see [JavaBridge]).
 *
 * QuickJS contexts are bound to one thread, so every operation is executed on a dedicated thread of this instance.
 */
class JavaScript(
    outputStream: OutputStream = CombinedLoggingOutputStream(),
    @Suppress("UNUSED_PARAMETER") currentWorkingDirectory: File? = null,
) : Closeable {

    private var jsThread: Thread? = null
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(null, runnable, "vlabeler-js", JS_THREAD_STACK_SIZE).also {
            it.isDaemon = true
            jsThread = it
        }
    }
    private val output = outputStream
    private lateinit var context: QuickJSContext
    private val bridge = JavaBridge()

    @Volatile
    private var closed = false

    init {
        onJsThread {
            QuickJsRuntime.ensureInitialized()
            context = QuickJSContext.create()
            context.setMaxStackSize(JS_MAX_STACK_SIZE)
            context.setConsole(
                object : QuickJSContext.Console {
                    override fun log(info: String) = print(info)
                    override fun info(info: String) = print(info)
                    override fun warn(info: String) = print(info)
                    override fun error(info: String) = print(info)
                },
            )
            val global = context.globalObject
            global.setProperty(
                "__vl_java_bridge",
                JSCallFunction { args ->
                    val op = args.getOrNull(0) as? String ?: return@JSCallFunction null
                    val payload = args.getOrNull(1) as? String ?: "null"
                    bridge.handle(op, payload)
                },
            )
            global.setProperty(
                "print",
                JSCallFunction { args ->
                    print(args.joinToString(" ") { it?.toString() ?: "null" })
                    null
                },
            )
            context.evaluate(JavaBridge.PRELUDE, "vlabeler-java-bridge.js").releaseIfObject()
        }
    }

    private fun print(text: String) {
        runCatching {
            output.write((text + "\n").toByteArray())
            output.flush()
        }
    }

    private fun <T> onJsThread(block: () -> T): T {
        if (Thread.currentThread() === jsThread) return block()
        check(!closed) { "JavaScript engine is already closed" }
        try {
            return executor.submit(Callable { block() }).get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    private fun Any?.releaseIfObject() {
        if (this is JSObject) runCatching { release() }
    }

    private fun evaluateRaw(source: String, fileName: String = "script.js"): Any? {
        val result = context.evaluate(source, fileName)
        if (result is JSObject) {
            val text = runCatching { result.stringify() }.getOrNull()
            result.releaseIfObject()
            return JsValue.ObjectMarker(text)
        }
        return result
    }

    fun eval(source: String): JsValue? = onJsThread {
        JsValue.of(evaluateRaw(source))
    }

    fun exec(sourceFileName: String, source: String) {
        onJsThread { evaluateRaw(source, sourceFileName) }
    }

    /**
     * Execute JavaScript code in a scope. The code will be wrapped in a function and executed immediately.
     */
    fun execInScope(source: String) {
        val wrappedSource = """
            (function() {
                $source
            })()
        """.trimIndent()
        eval(wrappedSource)
    }

    private fun readVariable(name: String): Any? = onJsThread {
        val raw = evaluateRaw(
            """
            (function() {
                let __v;
                try { __v = $name; } catch (e) { if (e instanceof ReferenceError) return undefined; throw e; }
                return __vl.exportPrimitiveOrHandle(__v);
            })()
            """.trimIndent(),
        )
        if (raw is String && raw.startsWith(JavaBridge.HANDLE_PREFIX)) {
            bridge.getHandleObject(raw.removePrefix(JavaBridge.HANDLE_PREFIX).toInt())
        } else {
            raw
        }
    }

    /**
     * Only for primitives. For other types, use [getJson]
     */
    fun <T : Any> getOrNull(name: String, ofClass: Class<T>): T? {
        val value = readVariable(name) ?: return null
        if (value is JsValue.ObjectMarker) {
            @Suppress("UNCHECKED_CAST")
            return if (ofClass == String::class.java) value.text as T? else null
        }
        return JavaBridge.convertValue(value, ofClass)
    }

    /**
     * Only for primitives. For other types, use [getJson]
     */
    inline fun <reified T : Any> get(name: String): T {
        return requireNotNull(getOrNull(name, T::class.java))
    }

    /**
     * Only for primitives. For other types, use [getJson]
     */
    inline fun <reified T : Any> getOrNull(name: String): T? {
        return getOrNull(name, T::class.java)
    }

    /**
     * Only for primitives. For other types, use [setJson]
     */
    fun set(name: String, value: Any?) {
        onJsThread {
            val global = context.globalObject
            when (value) {
                null -> evaluateRaw("globalThis[${JavaBridge.quote(name)}] = null")
                is String -> global.setProperty(name, value)
                is Boolean -> global.setProperty(name, value)
                is Int -> global.setProperty(name, value)
                is Short -> global.setProperty(name, value.toInt())
                is Byte -> global.setProperty(name, value.toInt())
                is Long -> global.setProperty(name, value)
                is Float -> global.setProperty(name, value.toDouble())
                is Double -> global.setProperty(name, value)
                is Char -> global.setProperty(name, value.toString())
                else -> {
                    val encoded = bridge.encodeResult(value).toString()
                    evaluateRaw("globalThis[${JavaBridge.quote(name)}] = __vl.decode($encoded)")
                }
            }
        }
    }

    /**
     * Pass object to JavaScript via JSON serialization
     */
    inline fun <reified T : @Serializable Any> setJson(name: String, value: T) {
        set(name, value.stringifyJson())
        eval("$name = JSON.parse($name)")
    }

    /**
     * Receive object from JavaScript via JSON deserialization
     */
    inline fun <reified T : @Serializable Any> getJson(name: String): T {
        val json = eval("JSON.stringify($name)")!!.asString()
        return json.parseJson()
    }

    /**
     * Receive object from JavaScript via JSON deserialization
     */
    inline fun <reified T : @Serializable Any> getJsonOrNull(name: String): T? {
        if (!hasValue(name)) return null
        return getJson(name)
    }

    /**
     * Only for JavaScript array types with unserializable elements. For serializable types, use [getJson]
     */
    fun <T : Any> getArrayOrNull(name: String, ofClass: Class<T>): List<T>? = onJsThread {
        val raw = evaluateRaw(
            """
            (function() {
                let __v;
                try { __v = $name; } catch (e) { if (e instanceof ReferenceError) return undefined; throw e; }
                if (__v === undefined || __v === null || !Array.isArray(__v)) return undefined;
                return JSON.stringify(__vl.encode(__v));
            })()
            """.trimIndent(),
        ) as? String ?: return@onJsThread null
        bridge.decodeArgumentList(raw).map { requireNotNull(JavaBridge.convertValue(it, ofClass)) }
    }

    /**
     * Only for JavaScript array types with unserializable elements. For serializable types, use [getJson]
     */
    inline fun <reified T : Any> getArray(name: String): List<T> {
        return requireNotNull(getArrayOrNull(name, T::class.java))
    }

    /**
     * Only for JavaScript array types with unserializable elements. For serializable types, use [getJson]
     */
    inline fun <reified T : Any> getArrayOrNull(name: String): List<T>? {
        return getArrayOrNull(name, T::class.java)
    }

    /**
     * Only for JavaScript array types with unserializable elements. For serializable types, use [setJson]
     */
    fun setArray(name: String, list: List<Any?>) {
        onJsThread {
            val encoded = bridge.encodeResult(list.toTypedArray()).toString()
            evaluateRaw("globalThis[${JavaBridge.quote(name)}] = __vl.decode($encoded)")
        }
    }

    /**
     * Check if a variable is defined in JavaScript
     */
    fun hasValue(name: String): Boolean {
        return eval(
            "(function() { try { return typeof($name) == \"undefined\" || $name == null } catch (e) { return true } })()",
        )?.asBoolean() == false
    }

    override fun close() {
        if (closed) return
        runCatching {
            onJsThread {
                runCatching { context.destroy() }.onFailure { Log.debug(it) }
            }
        }
        closed = true
        bridge.clear()
        executor.shutdown()
    }

    companion object {
        private const val JS_THREAD_STACK_SIZE = 32L * 1024 * 1024
        private const val JS_MAX_STACK_SIZE = 16 * 1024 * 1024
    }
}

class CombinedLoggingOutputStream : OutputStream() {

    private val info = runCatching { com.sdercolin.vlabeler.env.Log.getInfoOutputStream() }.getOrNull()
    private var atNewLine = true

    override fun write(b: Int) {
        if (atNewLine) {
            val timeTag = "[${Instant.now()}] "
            val buf = timeTag.toByteArray()
            System.out.write(buf)
            info?.write(buf)
        }
        System.out.write(b)
        info?.write(b)
        atNewLine = b == '\n'.code
    }

    override fun flush() {
        System.out.flush()
        info?.flush()
    }
}
