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

class SelectedIconsTest {

    @Test
    fun `an empty map is encoded as an empty string`() {
        assertEquals("", encodeSelectedIcons(emptyMap()))
    }

    @Test
    fun `icons survive a round trip`() {
        val icons = mapOf(
            "com.android.email" to "email",
            "com.android.phone" to "phone",
        )

        assertEquals(icons, decodeSelectedIcons(encodeSelectedIcons(icons)))
    }

    @Test
    fun `empty input decodes to an empty map`() {
        assertTrue(decodeSelectedIcons("").isEmpty())
        assertTrue(decodeSelectedIcons("   ").isEmpty())
    }

    @Test
    fun `malformed input decodes to an empty map`() {
        assertTrue(decodeSelectedIcons("not json").isEmpty())
        assertTrue(decodeSelectedIcons("[1, 2, 3]").isEmpty())
    }

    @Test
    fun `malformed entries are ignored`() {
        val decoded = decodeSelectedIcons("""{"com.android.email":"email","com.other":42}""")

        assertEquals(mapOf("com.android.email" to "email"), decoded)
    }
}
