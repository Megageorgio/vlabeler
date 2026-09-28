package com.sdercolin.vlabeler.android.js

import com.whl.quickjs.android.QuickJSLoader

/**
 * A value returned by JavaScript evaluation. Mirrors the parts of GraalVM's `Value` API used by vLabeler.
 */
class JsValue private constructor(private val raw: Any?) {

    /** Marks a JavaScript object result. [text] is its JSON representation if available. */
    class ObjectMarker(val text: String?) {
        override fun toString(): String = text ?: "[object]"
    }

    val isNull: Boolean get() = raw == null
    val isBoolean: Boolean get() = raw is Boolean
    val isString: Boolean get() = raw is String
    val isNumber: Boolean get() = raw is Number

    fun asBoolean(): Boolean = raw as? Boolean ?: throw ClassCastException("Not a boolean: $raw")
    fun asString(): String = when (raw) {
        is String -> raw
        is ObjectMarker -> raw.text ?: throw ClassCastException("Not a string: $raw")
        else -> throw ClassCastException("Not a string: $raw")
    }

    fun asInt(): Int = (raw as? Number)?.toInt() ?: throw ClassCastException("Not a number: $raw")
    fun asLong(): Long = (raw as? Number)?.toLong() ?: throw ClassCastException("Not a number: $raw")
    fun asDouble(): Double = (raw as? Number)?.toDouble() ?: throw ClassCastException("Not a number: $raw")
    fun asFloat(): Float = (raw as? Number)?.toFloat() ?: throw ClassCastException("Not a number: $raw")

    fun <T : Any> `as`(ofClass: Class<T>): T? = JavaBridge.convertValue(raw, ofClass)

    override fun toString(): String = raw.toString()

    companion object {
        fun of(raw: Any?): JsValue? = if (raw == null) null else JsValue(raw)
    }
}

internal object QuickJsRuntime {
    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (!initialized) {
                QuickJSLoader.init()
                initialized = true
            }
        }
    }
}
