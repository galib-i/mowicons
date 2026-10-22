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

/**
 * The settings which decide how the painted icons of the apps look.
 */
data class PaintedIconSettings(
    /**
     * Whether the foreground is taken from the original icon of the app, instead of
     * [foregroundColor].
     */
    val useOriginalForeground: Boolean,
    /**
     * The foreground colour of the painted icons, used when [useOriginalForeground] is false.
     */
    val foregroundColor: Int,
    /**
     * The background colour of the painted icons.
     */
    val backgroundColor: Int,
    /**
     * The shape of the painted icons.
     */
    val shape: IconShape,
) {
    /**
     * The colour of the foreground of the painted icon of an app.
     *
     * A colour the user picked for the app with the colour picker takes precedence over
     * the global foreground colour, so that single apps can be given a colour of their
     * own, no matter which foreground is picked.
     *
     * @param selectedColor The colour the user picked for the app, if any.
     * @param originalColor The colour of the original icon of the app, if it has one.
     */
    fun foregroundColorFor(
        selectedColor: Int?,
        originalColor: Int?,
    ): Int? = when {
        selectedColor != null -> selectedColor
        useOriginalForeground -> originalColor
        else -> foregroundColor
    }

    companion object {
        /** The foreground colour of the painted icons, before the user picks another one. */
        val DefaultForegroundColor = 0xFFC6C6C6.toInt()

        /** The background colour of the painted icons, before the user picks another one. */
        val DefaultBackgroundColor = 0xFF222224.toInt()
    }
}
