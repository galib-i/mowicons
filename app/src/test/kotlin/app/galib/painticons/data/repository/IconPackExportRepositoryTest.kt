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

package app.galib.painticons.data.repository

import android.app.Application
import app.galib.painticons.data.model.IconInfo
import app.galib.painticons.data.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class IconPackExportRepositoryTest {

    private val repository = IconPackExportRepositoryImpl(mock<Application>())

    @Test
    fun `the appfilter maps the apps to the icons of the pack`() {
        val app = InstalledApp(
            label = "Infinite Painter",
            packageName = "com.brakefield.painter",
            className = "com.brakefield.painter.activities.ActivityStartup",
        )

        val appfilter = repository.buildPlainTextAppfilter(listOf(app), listOf("icon_0001"))
            .toString(Charsets.UTF_8)

        assertTrue(
            appfilter.contains(
                "<item component=\"ComponentInfo{com.brakefield.painter/" +
                    "com.brakefield.painter.activities.ActivityStartup}\" " +
                    "drawable=\"icon_0001\" name=\"Infinite Painter\" />",
            ),
        )
    }

    @Test
    fun `the appfilter maps the pack itself to its own icon`() {
        val appfilter = repository.buildPlainTextAppfilter(emptyList(), emptyList())
            .toString(Charsets.UTF_8)

        assertTrue(
            appfilter.contains(
                "<item component=\"ComponentInfo{app.galib.paintedicons.pack/android.app.Activity}\" " +
                    "drawable=\"infinite_painter\" name=\"Painted Icons\" />",
            ),
        )
    }

    @Test
    fun `the binary appfilter maps the pack itself to its own icon`() {
        val appfilter = repository.buildAppfilter(emptyList(), emptyList())
            .toString(Charsets.ISO_8859_1)

        assertTrue(
            appfilter.contains("ComponentInfo{app.galib.paintedicons.pack/android.app.Activity}"),
        )
        assertTrue(appfilter.contains("infinite_painter"))
    }

    @Test
    fun `the pack itself is not covered by the pack`() {
        val app = InstalledApp(
            label = "Infinite Painter",
            packageName = "com.brakefield.painter",
            className = "com.brakefield.painter.activities.ActivityStartup",
            iconInfo = iconInfo(),
        )
        val pack = InstalledApp(
            label = "Painted Icons",
            packageName = "app.galib.paintedicons.pack",
            className = "android.app.Activity",
            iconInfo = iconInfo(),
        )

        val coveredApps = repository.coveredApps(listOf(app, pack))

        assertEquals(listOf(app), coveredApps)
    }

    private fun iconInfo() = IconInfo(
        drawableName = "infinite_painter",
        componentNames = emptyList(),
        drawableId = 1,
    )
}
