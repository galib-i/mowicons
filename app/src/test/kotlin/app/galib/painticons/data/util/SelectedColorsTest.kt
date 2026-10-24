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

package app.galib.painticons.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectedColorsTest {

    @Test
    fun `encoding an empty map yields an empty string`() {
        assertEquals("", encodeSelectedColors(emptyMap()))
    }

    @Test
    fun `encoded colors can be decoded again`() {
        val colors = mapOf(
            "com.android.email" to 0xFF005E9E.toInt(),
            "com.android.phone" to -16777216,
        )

        assertEquals(colors, decodeSelectedColors(encodeSelectedColors(colors)))
    }

    @Test
    fun `decoding an empty string yields an empty map`() {
        assertTrue(decodeSelectedColors("").isEmpty())
        assertTrue(decodeSelectedColors("   ").isEmpty())
    }

    @Test
    fun `decoding a malformed string yields an empty map`() {
        assertTrue(decodeSelectedColors("not json").isEmpty())
        assertTrue(decodeSelectedColors("[1, 2, 3]").isEmpty())
    }

    @Test
    fun `decoding ignores entries which are no colour`() {
        val decoded = decodeSelectedColors("""{"com.android.email":-16777216,"com.other":"red"}""")

        assertEquals(mapOf("com.android.email" to -16777216), decoded)
    }
}
