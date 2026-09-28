package com.sdercolin.vlabeler.android.compat

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/*
 * Replacements of the JSON utilities from Segment Analytics used by the shared code.
 */

val JsonElement?.safeJsonPrimitive: JsonPrimitive? get() = this as? JsonPrimitive
val JsonElement?.safeJsonArray: JsonArray? get() = this as? JsonArray
val JsonElement?.safeJsonObject: JsonObject? get() = this as? JsonObject
