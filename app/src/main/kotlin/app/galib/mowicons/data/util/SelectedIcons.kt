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
import kotlinx.serialization.json.jsonObject

/**
 * Encodes the icons picked for the apps into a string, so that they can be stored in the
 * preferences.
 *
 * @param icons The names of the drawables of the icons, keyed by the package name of the app.
 */
fun encodeSelectedIcons(icons: Map<String, String>): String {
    if (icons.isEmpty()) return ""
    return buildJsonObject {
        icons.forEach { (packageName, drawableName) ->
            put(packageName, JsonPrimitive(drawableName))
        }
    }.toString()
}

/**
 * Decodes the icons picked for the apps from a string, as encoded by [encodeSelectedIcons].
 * Malformed entries are ignored.
 */
fun decodeSelectedIcons(encoded: String): Map<String, String> {
    if (encoded.isBlank()) return emptyMap()
    val json = runCatching { kotlinxJson.parseToJsonElement(encoded).jsonObject }.getOrNull()
        ?: return emptyMap()
    return json.mapNotNull { (packageName, element) ->
        val primitive = element as? JsonPrimitive ?: return@mapNotNull null
        if (!primitive.isString) return@mapNotNull null
        packageName to primitive.content
    }.toMap()
}
