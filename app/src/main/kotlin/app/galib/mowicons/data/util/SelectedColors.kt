/*
 * Copyright 2026 Lawnchair Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.galib.mowicons.data.util

import app.galib.mowicons.data.kotlinxJson
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Encodes the foreground colours picked for the icons of the apps into a string, so that they
 * can be stored in the preferences.
 *
 * @param colors The colours, keyed by the package name of the app.
 */
fun encodeSelectedColors(colors: Map<String, Int>): String {
    if (colors.isEmpty()) return ""
    return buildJsonObject {
        colors.forEach { (packageName, color) ->
            put(packageName, JsonPrimitive(color))
        }
    }.toString()
}

/**
 * Decodes the foreground colours picked for the icons of the apps from a string, as encoded by
 * [encodeSelectedColors]. Malformed entries are ignored.
 */
fun decodeSelectedColors(encoded: String): Map<String, Int> {
    if (encoded.isBlank()) return emptyMap()
    val json = runCatching { kotlinxJson.parseToJsonElement(encoded).jsonObject }.getOrNull()
        ?: return emptyMap()
    return json.mapNotNull { (packageName, element) ->
        val color = (element as? JsonPrimitive)?.intOrNull ?: return@mapNotNull null
        packageName to color
    }.toMap()
}
