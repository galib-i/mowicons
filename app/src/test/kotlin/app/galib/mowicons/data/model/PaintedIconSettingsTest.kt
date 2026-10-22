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

package app.galib.mowicons.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PaintedIconSettingsTest {

    private val settings = PaintedIconSettings(
        useOriginalForeground = false,
        foregroundColor = HexColor,
        backgroundColor = BackgroundColor,
        shape = IconShape.Circle,
    )

    @Test
    fun `the hex colour is used when the original foreground is not picked`() {
        assertEquals(HexColor, settings.foregroundColorFor(null, OriginalColor))
    }

    @Test
    fun `the colour of the original icon is used when it is picked`() {
        val originalSettings = settings.copy(useOriginalForeground = true)

        assertEquals(OriginalColor, originalSettings.foregroundColorFor(null, OriginalColor))
    }

    @Test
    fun `the colour picked for the app wins over the hex colour`() {
        assertEquals(PickedColor, settings.foregroundColorFor(PickedColor, OriginalColor))
    }

    @Test
    fun `the colour picked for the app wins over the original colour`() {
        val originalSettings = settings.copy(useOriginalForeground = true)

        assertEquals(PickedColor, originalSettings.foregroundColorFor(PickedColor, OriginalColor))
    }

    private companion object {
        const val HexColor = 0xFFC6C6C6.toInt()
        const val BackgroundColor = 0xFF222224.toInt()
        const val OriginalColor = 0xFF4084EE.toInt()
        const val PickedColor = 0xFFF9BC0E.toInt()
    }
}
