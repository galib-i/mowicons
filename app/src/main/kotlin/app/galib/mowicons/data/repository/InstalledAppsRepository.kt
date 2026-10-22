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

package app.galib.mowicons.data.repository

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.data.util.dominantColors
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface InstalledAppsRepository {
    val installedApps: StateFlow<List<InstalledApp>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstalledAppsRepositoryImpl(
    private val application: Application,
) : InstalledAppsRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    override val installedApps = _installedApps.asStateFlow()

    init {
        coroutineScope.launch {
            _installedApps.value = loadInstalledApps()
        }
    }

    private fun loadInstalledApps(): List<InstalledApp> {
        val packageManager = application.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val iconSize = (ICON_SIZE_DP * application.resources.displayMetrics.density).toInt()

        return packageManager.queryIntentActivities(launchIntent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val appInfo = activityInfo.applicationInfo ?: return@mapNotNull null
                val icon = runCatching { packageManager.getApplicationIcon(appInfo) }
                    .getOrNull() ?: return@mapNotNull null
                val bitmap = icon.toIconBitmap(iconSize)

                InstalledApp(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    className = activityInfo.name,
                    colors = bitmap.dominantColors(),
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    /**
     * Renders the icon into a square bitmap of [size] pixels.
     *
     * Adaptive icons are drawn with a transparent margin (the outer sixth on each side,
     * which the launcher would normally mask away), so they are rendered larger and
     * cropped to make the icon fill the whole item.
     */
    private fun Drawable.toIconBitmap(size: Int): Bitmap {
        if (this !is AdaptiveIconDrawable) return toBitmap(size, size)

        val renderSize = size * 3 / 2
        val bitmap = toBitmap(renderSize, renderSize)
        val inset = renderSize / 6
        val side = renderSize - inset * 2
        return runCatching {
            Bitmap.createBitmap(bitmap, inset, inset, side, side)
        }.getOrDefault(bitmap)
    }

    private companion object {
        /**
         * The size of the icons in the grid, in dp. Adaptive icons are rendered larger
         * and cropped, so the resulting bitmap has this size.
         */
        const val ICON_SIZE_DP = 48
    }
}
