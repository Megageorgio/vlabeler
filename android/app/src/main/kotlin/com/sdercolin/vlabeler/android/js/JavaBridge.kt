package com.sdercolin.vlabeler.android.js

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.lang.reflect.Array as ReflectArray
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * A reflection based bridge between QuickJS and Java, providing the subset of GraalJS host interoperability used by
 * vLabeler labelers and plugins:
 *
 * - `Java.type("fully.qualified.ClassName")` returns a class object supporting `new`, static methods and fields.
 * - Java objects are exposed as proxies supporting method calls, public fields, and index/length access for lists.
 * - Java arrays are converted to JavaScript arrays.
 * - JavaScript values are converted to the parameter types of the Java methods (overloads are resolved by arity and
 *   convertibility).
 */
class JavaBridge {

    /** Wrapper of a class obtained by `Java.type`, whose members are the static members of the class. */
    class JavaType(val clazz: Class<*>)

    private val handles = HashMap<Int, Any>()
    private val handleIds = java.util.IdentityHashMap<Any, Int>()
    private var nextHandle = 1

    fun clear() {
        handles.clear()
        handleIds.clear()
    }

    fun getHandleObject(id: Int): Any? = handles[id]

    private fun register(obj: Any): Int {
        handleIds[obj]?.let { return it }
        val id = nextHandle++
        handles[id] = obj
        handleIds[obj] = id
        return id
    }

    /**
     * Handles a call from JavaScript. Returns a JSON string: `{"r": <result>}` or `{"e": <error message>}`.
     */
    fun handle(op: String, payload: String): String {
        return try {
            val args = Json.parseToJsonElement(payload).let { if (it is JsonNull) JsonObject(emptyMap()) else it.jsonObject }
            val result = when (op) {
                "type" -> encodeResult(JavaType(loadClass(args.getValue("n").jsonPrimitive.content)))
                "new" -> construct(args)
                "invoke" -> invoke(args)
                "members" -> return buildJsonObject { put("raw", members(args)) }.toString()
                "getField" -> getField(args)
                "setField" -> setField(args)
                "len" -> encodeResult(length(target(args)))
                "aget" -> encodeResult(elementAt(target(args), args.getValue("i").jsonPrimitive.int))
                "aset" -> {
                    setElementAt(target(args), args.getValue("i").jsonPrimitive.int, args["v"])
                    encodeResult(null)
                }
                "toString" -> encodeResult(target(args).toString())
                else -> throw IllegalArgumentException("Unknown bridge operation: $op")
            }
            buildJsonObject { put("r", result) }.toString()
        } catch (t: Throwable) {
            val cause = if (t is InvocationTargetException) t.targetException ?: t else t
            buildJsonObject { put("e", "${cause.javaClass.name}: ${cause.message}") }.toString()
        }
    }

    private fun loadClass(name: String): Class<*> {
        val loader = JavaBridge::class.java.classLoader
        return try {
            Class.forName(name, true, loader)
        } catch (e: ClassNotFoundException) {
            // nested classes written with dots, e.g. "java.util.Map.Entry"
            val index = name.lastIndexOf('.')
            if (index > 0) loadClass(name.substring(0, index) + "$" + name.substring(index + 1)) else throw e
        }
    }

    private fun target(args: JsonObject): Any {
        val id = args.getValue("h").jsonPrimitive.int
        return handles[id] ?: throw IllegalStateException("Invalid Java object handle: $id")
    }

    private fun construct(args: JsonObject): JsonElement {
        val type = target(args) as? JavaType ?: throw IllegalArgumentException("Not a Java class")
        val jsArgs = decodeArguments(args["a"])
        val constructors = type.clazz.constructors.toList()
        val (constructor, converted) = resolve(constructors, jsArgs)
            ?: throw NoSuchMethodException("No matching constructor of ${type.clazz.name} for ${describe(jsArgs)}")
        return encodeResult((constructor as Constructor<*>).newInstance(*converted))
    }

    private fun invoke(args: JsonObject): JsonElement {
        val target = target(args)
        val name = args.getValue("m").jsonPrimitive.content
        val jsArgs = decodeArguments(args["a"])
        if (target is JavaType) {
            val methods = target.clazz.methods.filter { it.name == name && Modifier.isStatic(it.modifiers) }
            val resolved = resolve(methods, jsArgs)
            if (resolved == null) {
                aliasStaticCall(target.clazz, name, jsArgs)?.let { return encodeResult(it) }
                throw NoSuchMethodException("No matching static method ${target.clazz.name}.$name for ${describe(jsArgs)}")
            }
            val (method, converted) = resolved
            return encodeResult((method as Method).invoke(null, *converted))
        }
        val methods = publicMethods(target.javaClass).filter { it.name == name && !Modifier.isStatic(it.modifiers) }
        val (method, converted) = resolve(methods, jsArgs)
            ?: throw NoSuchMethodException("No matching method ${target.javaClass.name}.$name for ${describe(jsArgs)}")
        method as Method
        runCatching { method.isAccessible = true }
        return encodeResult(method.invoke(target, *converted))
    }

    /**
     * Android doesn't provide some newer Java APIs on older versions. Emulate the ones used by the bundled scripts.
     */
    private fun aliasStaticCall(clazz: Class<*>, name: String, args: List<Any?>): Any? {
        if (clazz.name == "java.nio.file.Path" && name == "of" && args.isNotEmpty()) {
            val first = args[0] as? String ?: return null
            val more = args.drop(1).map { it.toString() }.toTypedArray()
            return java.nio.file.Paths.get(first, *more)
        }
        return null
    }

    /**
     * Public methods, looked up from public classes/interfaces so that they are accessible by reflection.
     */
    private fun publicMethods(clazz: Class<*>): List<Method> = methodCache.getOrPut(clazz) {
        clazz.methods.map { method ->
            if (Modifier.isPublic(method.declaringClass.modifiers)) {
                method
            } else {
                findPublicDeclaration(clazz, method) ?: method
            }
        }
    }

    private fun findPublicDeclaration(clazz: Class<*>, method: Method): Method? {
        val candidates = ArrayDeque<Class<*>>()
        candidates += clazz.interfaces
        clazz.superclass?.let { candidates += it }
        while (candidates.isNotEmpty()) {
            val current = candidates.removeFirst()
            if (Modifier.isPublic(current.modifiers)) {
                runCatching { current.getMethod(method.name, *method.parameterTypes) }.getOrNull()?.let { return it }
            }
            candidates += current.interfaces
            current.superclass?.let { candidates += it }
        }
        return null
    }

    private val methodCache = HashMap<Class<*>, List<Method>>()

    private fun members(args: JsonObject): JsonElement {
        val target = target(args)
        val (methods, fields) = if (target is JavaType) {
            target.clazz.methods.filter { Modifier.isStatic(it.modifiers) }.map { it.name } to
                target.clazz.fields.filter { Modifier.isStatic(it.modifiers) }.map { it.name }
        } else {
            publicMethods(target.javaClass).filter { !Modifier.isStatic(it.modifiers) }.map { it.name } to
                target.javaClass.fields.filter { !Modifier.isStatic(it.modifiers) }.map { it.name }
        }
        return buildJsonObject {
            put("m", JsonArray(methods.distinct().map { JsonPrimitive(it) }))
            put("f", JsonArray(fields.distinct().map { JsonPrimitive(it) }))
        }
    }

    private fun findField(target: Any, name: String): Pair<Field, Any?> {
        return if (target is JavaType) {
            target.clazz.getField(name) to null
        } else {
            target.javaClass.getField(name) to target
        }
    }

    private fun getField(args: JsonObject): JsonElement {
        val (field, receiver) = findField(target(args), args.getValue("f").jsonPrimitive.content)
        return encodeResult(field.get(receiver))
    }

    private fun setField(args: JsonObject): JsonElement {
        val (field, receiver) = findField(target(args), args.getValue("f").jsonPrimitive.content)
        val value = decodeArgument(args["v"])
        val converted = convertArgument(value, field.type)
        if (converted === NotConvertible) throw IllegalArgumentException("Cannot assign $value to ${field.name}")
        field.set(receiver, converted)
        return encodeResult(null)
    }

    private fun length(target: Any): Int = when {
        target.javaClass.isArray -> ReflectArray.getLength(target)
        target is List<*> -> target.size
        target is Collection<*> -> target.size
        target is Map<*, *> -> target.size
        target is CharSequence -> target.length
        else -> throw IllegalArgumentException("Object has no length: ${target.javaClass.name}")
    }

    private fun elementAt(target: Any, index: Int): Any? = when {
        target.javaClass.isArray -> ReflectArray.get(target, index)
        target is List<*> -> target[index]
        else -> throw IllegalArgumentException("Object is not indexable: ${target.javaClass.name}")
    }

    private fun setElementAt(target: Any, index: Int, value: JsonElement?) {
        val decoded = decodeArgument(value)
        when {
            target.javaClass.isArray -> {
                val converted = convertArgument(decoded, target.javaClass.componentType!!)
                ReflectArray.set(target, index, if (converted === NotConvertible) decoded else converted)
            }
            target is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                (target as MutableList<Any?>)[index] = decoded
            }
            else -> throw IllegalArgumentException("Object is not indexable: ${target.javaClass.name}")
        }
    }

    // region Encoding (Java -> JS)

    /**
     * Encodes a Java value to be decoded by `__vl.decode` in JavaScript.
     */
    fun encodeResult(value: Any?): JsonElement = when (value) {
        null, is Unit -> buildJsonObject { put("v", JsonNull) }
        is String -> buildJsonObject { put("v", value) }
        is Char -> buildJsonObject { put("v", value.toString()) }
        is Boolean -> buildJsonObject { put("v", value) }
        is Int, is Short, is Byte, is Long -> buildJsonObject { put("v", (value as Number).toLong()) }
        is Float -> buildJsonObject { put("v", value.toDouble()) }
        is Double -> buildJsonObject { put("v", value) }
        is Number -> buildJsonObject { put("v", value.toDouble()) }
        is JavaType -> buildJsonObject {
            put("h", register(value))
            put("t", true)
            put("c", value.clazz.name)
        }
        else -> if (value.javaClass.isArray) {
            val length = ReflectArray.getLength(value)
            buildJsonObject { put("arr", JsonArray(List(length) { encodeResult(ReflectArray.get(value, it)) })) }
        } else {
            buildJsonObject {
                put("h", register(value))
                put("c", value.javaClass.name)
                if (value is List<*>) put("l", true)
            }
        }
    }

    // endregion

    // region Decoding (JS -> Java)

    /** A plain JavaScript object passed to Java. */
    class JsObjectValue(val map: Map<String, Any?>)

    fun decodeArgumentList(json: String): List<Any?> {
        val element = Json.parseToJsonElement(json)
        return (decodeArgument(element) as? List<*>)?.toList() ?: emptyList()
    }

    private fun decodeArguments(element: JsonElement?): List<Any?> {
        if (element == null || element is JsonNull) return emptyList()
        return element.jsonArray.map { decodeArgument(it) }
    }

    private fun decodeArgument(element: JsonElement?): Any? = when (element) {
        null, is JsonNull -> null
        is JsonPrimitive -> when {
            element.isString -> element.content
            element.booleanOrNull != null -> element.booleanOrNull
            element.longOrNull != null && !element.content.contains('.') &&
                !element.content.contains('e', ignoreCase = true) -> element.longOrNull
            else -> element.doubleOrNull
        }
        is JsonArray -> element.map { decodeArgument(it) }
        is JsonObject -> when {
            "\$h" in element -> handles[element.getValue("\$h").jsonPrimitive.int]
            "\$a" in element -> element.getValue("\$a").jsonArray.map { decodeArgument(it) }
            "\$o" in element -> JsObjectValue(element.getValue("\$o").jsonObject.mapValues { decodeArgument(it.value) })
            else -> JsObjectValue(element.mapValues { decodeArgument(it.value) })
        }
    }

    // endregion

    // region Overload resolution

    private object NotConvertible

    private fun describe(args: List<Any?>) = args.joinToString(prefix = "(", postfix = ")") {
        it?.javaClass?.simpleName ?: "null"
    }

    private fun resolve(candidates: List<Executable>, args: List<Any?>): Pair<Executable, Array<Any?>>? {
        var best: Pair<Executable, Array<Any?>>? = null
        var bestScore = Int.MAX_VALUE
        for (candidate in candidates) {
            val params = candidate.parameterTypes
            val converted = if (candidate.isVarArgs && params.isNotEmpty()) {
                convertVarArgs(params, args)
            } else {
                if (params.size != args.size) null else convertAll(params, args)
            } ?: continue
            val score = score(params, args) + if (candidate.isVarArgs) 100 else 0
            if (score < bestScore) {
                bestScore = score
                best = candidate to converted
            }
        }
        return best
    }

    private fun convertAll(params: Array<Class<*>>, args: List<Any?>): Array<Any?>? {
        val result = arrayOfNulls<Any?>(params.size)
        for (i in params.indices) {
            val converted = convertArgument(args[i], params[i])
            if (converted === NotConvertible) return null
            result[i] = converted
        }
        return result
    }

    private fun convertVarArgs(params: Array<Class<*>>, args: List<Any?>): Array<Any?>? {
        val fixedCount = params.size - 1
        if (args.size < fixedCount) return null
        // passing an array directly
        if (args.size == params.size) {
            convertAll(params, args)?.let { return it }
        }
        val fixed = convertAll(params.copyOfRange(0, fixedCount), args.subList(0, fixedCount)) ?: return null
        val componentType = params.last().componentType!!
        val rest = args.subList(fixedCount, args.size)
        val array = ReflectArray.newInstance(componentType, rest.size)
        rest.forEachIndexed { index, arg ->
            val converted = convertArgument(arg, componentType)
            if (converted === NotConvertible) return null
            ReflectArray.set(array, index, converted)
        }
        return fixed.plus(array)
    }

    private fun score(params: Array<Class<*>>, args: List<Any?>): Int {
        var score = 0
        for (i in params.indices) {
            val arg = args.getOrNull(i) ?: continue
            val param = params[i]
            score += when {
                param == arg.javaClass -> 0
                param.isPrimitive && boxed(param) == arg.javaClass -> 0
                arg is String && (param == String::class.java || param == CharSequence::class.java) -> 0
                arg is Long && (param == Int::class.javaPrimitiveType || param == Int::class.javaObjectType) -> 1
                arg is Number && (param == Double::class.javaPrimitiveType || param == Double::class.javaObjectType) -> 2
                param == Any::class.java -> 5
                else -> 3
            }
        }
        return score
    }

    private fun boxed(clazz: Class<*>): Class<*> = when (clazz) {
        Int::class.javaPrimitiveType -> Int::class.javaObjectType
        Long::class.javaPrimitiveType -> Long::class.javaObjectType
        Double::class.javaPrimitiveType -> Double::class.javaObjectType
        Float::class.javaPrimitiveType -> Float::class.javaObjectType
        Short::class.javaPrimitiveType -> Short::class.javaObjectType
        Byte::class.javaPrimitiveType -> Byte::class.javaObjectType
        Boolean::class.javaPrimitiveType -> Boolean::class.javaObjectType
        Char::class.javaPrimitiveType -> Char::class.javaObjectType
        else -> clazz
    }

    private fun convertArgument(arg: Any?, type: Class<*>): Any? {
        if (arg == null) return if (type.isPrimitive) NotConvertible else null
        val boxedType = boxed(type)
        if (boxedType.isInstance(arg)) return arg
        return when {
            arg is Number && Number::class.java.isAssignableFrom(boxedType) -> when (boxedType) {
                Int::class.javaObjectType -> if (arg.toDouble() % 1.0 == 0.0) arg.toInt() else NotConvertible
                Long::class.javaObjectType -> if (arg.toDouble() % 1.0 == 0.0) arg.toLong() else NotConvertible
                Short::class.javaObjectType -> arg.toShort()
                Byte::class.javaObjectType -> arg.toByte()
                Double::class.javaObjectType -> arg.toDouble()
                Float::class.javaObjectType -> arg.toFloat()
                else -> arg
            }
            arg is Number && boxedType == Any::class.java -> arg
            arg is String && boxedType == Char::class.javaObjectType && arg.length == 1 -> arg[0]
            arg is String && type == CharSequence::class.java -> arg
            arg is List<*> && type.isArray -> {
                val component = type.componentType!!
                val array = ReflectArray.newInstance(component, arg.size)
                arg.forEachIndexed { index, item ->
                    val converted = convertArgument(item, component)
                    if (converted === NotConvertible) return NotConvertible
                    ReflectArray.set(array, index, converted)
                }
                array
            }
            arg is List<*> && type.isAssignableFrom(ArrayList::class.java) -> ArrayList(arg)
            arg is JsObjectValue && type.isAssignableFrom(LinkedHashMap::class.java) -> LinkedHashMap(arg.map)
            else -> NotConvertible
        }
    }

    // endregion

    companion object {
        const val HANDLE_PREFIX = "__vl_handle:"

        fun quote(text: String): String = JsonPrimitive(text).toString()

        /**
         * Converts a value returned by the engine (or a Java object) to the requested class.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> convertValue(value: Any?, ofClass: Class<T>): T? {
            if (value == null) return null
            val target = when (ofClass) {
                Int::class.javaPrimitiveType -> Int::class.javaObjectType
                Long::class.javaPrimitiveType -> Long::class.javaObjectType
                Double::class.javaPrimitiveType -> Double::class.javaObjectType
                Float::class.javaPrimitiveType -> Float::class.javaObjectType
                Boolean::class.javaPrimitiveType -> Boolean::class.javaObjectType
                else -> ofClass
            }
            if (target.isInstance(value)) return value as T
            val result: Any? = when {
                value is Number -> when (target) {
                    Int::class.javaObjectType -> value.toInt()
                    Long::class.javaObjectType -> value.toLong()
                    Double::class.javaObjectType -> value.toDouble()
                    Float::class.javaObjectType -> value.toFloat()
                    Short::class.javaObjectType -> value.toShort()
                    Byte::class.javaObjectType -> value.toByte()
                    String::class.java -> value.toString()
                    Number::class.java, Any::class.java -> value
                    else -> null
                }
                target == String::class.java -> value.toString()
                else -> null
            }
            return result as T? ?: throw ClassCastException("Cannot convert $value to ${ofClass.name}")
        }

        /**
         * JavaScript part of the bridge, evaluated in every context.
         */
        val PRELUDE = """
(function () {
    const bridge = globalThis.__vl_java_bridge;
    const HANDLE = Symbol('javaHandle');
    const memberCache = {};

    function call(op, payload) {
        const response = JSON.parse(bridge(op, JSON.stringify(payload === undefined ? null : payload)));
        if (response.e !== undefined) {
            throw new Error(response.e);
        }
        if (response.raw !== undefined) return response.raw;
        return decode(response.r);
    }

    function encode(value) {
        if (value === undefined || value === null) return null;
        const type = typeof value;
        if (type === 'string' || type === 'boolean') return value;
        if (type === 'number') return Number.isFinite(value) ? value : null;
        if (type === 'bigint') return Number(value);
        if (type === 'object' || type === 'function') {
            const handle = value[HANDLE];
            if (handle !== undefined) return { '${'$'}h': handle };
            if (type === 'function') return null;
            if (Array.isArray(value)) return { '${'$'}a': value.map(encode) };
            const result = {};
            for (const key of Object.keys(value)) {
                result[key] = encode(value[key]);
            }
            return { '${'$'}o': result };
        }
        return null;
    }

    function decode(result) {
        if (result === null || result === undefined) return null;
        if (Object.prototype.hasOwnProperty.call(result, 'v')) return result.v;
        if (Object.prototype.hasOwnProperty.call(result, 'arr')) return result.arr.map(decode);
        if (Object.prototype.hasOwnProperty.call(result, 'h')) return makeProxy(result);
        return null;
    }

    function getMembers(id, className, isType) {
        const key = className + (isType ? '#static' : '');
        let members = memberCache[key];
        if (!members) {
            members = call('members', { h: id });
            members = { m: new Set(members.m), f: new Set(members.f) };
            memberCache[key] = members;
        }
        return members;
    }

    function isIndex(prop) {
        return typeof prop === 'string' && /^\d+${'$'}/.test(prop);
    }

    function makeProxy(result) {
        const id = result.h;
        const isType = !!result.t;
        const isList = !!result.l;
        const className = result.c;
        const target = function () {};
        const toStringFunction = () => call('toString', { h: id });
        return new Proxy(target, {
            get(t, prop) {
                if (prop === HANDLE) return id;
                if (prop === 'then') return undefined;
                if (prop === Symbol.toPrimitive) {
                    return (hint) => {
                        const text = toStringFunction();
                        return hint === 'number' ? Number(text) : text;
                    };
                }
                if (prop === Symbol.iterator) {
                    if (!isList) return undefined;
                    return function* () {
                        const length = call('len', { h: id });
                        for (let i = 0; i < length; i++) yield call('aget', { h: id, i: i });
                    };
                }
                if (typeof prop === 'symbol') return undefined;
                if (isList) {
                    if (prop === 'length') return call('len', { h: id });
                    if (isIndex(prop)) return call('aget', { h: id, i: Number(prop) });
                }
                const members = getMembers(id, className, isType);
                if (members.m.has(prop)) {
                    return function (...args) {
                        return call('invoke', { h: id, m: prop, a: args.map(encode) });
                    };
                }
                if (members.f.has(prop)) return call('getField', { h: id, f: prop });
                if (prop === 'toString') return toStringFunction;
                if (prop === 'valueOf') return toStringFunction;
                return undefined;
            },
            set(t, prop, value) {
                if (isList && isIndex(prop)) {
                    call('aset', { h: id, i: Number(prop), v: encode(value) });
                } else {
                    call('setField', { h: id, f: prop, v: encode(value) });
                }
                return true;
            },
            has(t, prop) {
                if (prop === HANDLE) return true;
                if (typeof prop === 'symbol') return false;
                const members = getMembers(id, className, isType);
                return members.m.has(prop) || members.f.has(prop);
            },
            construct(t, args) {
                return call('new', { h: id, a: args.map(encode) });
            },
            apply() {
                throw new TypeError('Java object ' + className + ' is not a function');
            },
        });
    }

    globalThis.Java = {
        type(name) {
            return call('type', { n: name });
        },
        from(value) {
            if (Array.isArray(value)) return value;
            if (value && value[HANDLE] !== undefined) {
                const length = call('len', { h: value[HANDLE] });
                const result = [];
                for (let i = 0; i < length; i++) result.push(call('aget', { h: value[HANDLE], i: i }));
                return result;
            }
            return value;
        },
        to(value) {
            return value;
        },
        isJavaObject(value) {
            return !!value && value[HANDLE] !== undefined;
        },
    };

    globalThis.__vl = {
        encode: encode,
        decode: decode,
        exportPrimitiveOrHandle(value) {
            if (value === undefined || value === null) return undefined;
            if ((typeof value === 'object' || typeof value === 'function') && value[HANDLE] !== undefined) {
                return '$HANDLE_PREFIX' + value[HANDLE];
            }
            if (typeof value === 'bigint') return Number(value);
            return value;
        },
    };
})();
""".trimIndent()
    }
}
