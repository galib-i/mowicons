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

package app.galib.mowicons.ui.destination.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.galib.mowicons.data.model.Component
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.data.model.IconShape
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.data.repository.IconPackExportRepository
import app.galib.mowicons.data.repository.InstalledAppsRepository
import app.galib.mowicons.data.repository.PreferenceManager
import app.galib.mowicons.data.repository.home.IconRepository
import app.galib.mowicons.data.util.decodeSelectedColors
import app.galib.mowicons.data.util.encodeSelectedColors
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * The state of the export of the painted icons as an icon pack.
 */
sealed interface IconPackExportState {
    data object Idle : IconPackExportState
    data object Building : IconPackExportState

    /** The icon pack was saved, with the location of the file. */
    data class Saved(val location: String, val file: File) : IconPackExportState

    /** The icon pack could not be built, with the reason. */
    data class Failed(val reason: String) : IconPackExportState
}

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class AppsViewModel(
    installedAppsRepository: InstalledAppsRepository,
    private val iconRepository: IconRepository,
    private val iconPackExportRepository: IconPackExportRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModel() {

    /**
     * The apps installed on the device, each one paired with the icon of the icon pack.
     * Apps which aren't covered by the pack keep their system icon.
     */
    val installedApps: StateFlow<List<InstalledApp>> = combine(
        installedAppsRepository.installedApps,
        iconRepository.iconInfoModel,
    ) { installedApps, iconInfoModel ->
        val iconsByComponent = mutableMapOf<Component, IconInfo>()
        val iconsByPackage = mutableMapOf<String, IconInfo>()

        iconInfoModel.iconInfo.forEach { iconInfo ->
            iconInfo.componentNames.forEach { labelAndComponent ->
                val component = labelAndComponent.component
                iconsByComponent.putIfAbsent(component, iconInfo)
                iconsByPackage.putIfAbsent(component.packageName, iconInfo)
            }
        }

        installedApps.mapNotNull { app ->
            val iconInfo = iconsByComponent[Component(app.packageName, app.className)]
                ?: iconsByPackage[app.packageName]
            if (iconInfo != null) {
                app.copy(iconInfo = iconInfo)
            } else {
                null
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    /**
     * The colours picked by the user for apps, keyed by the package name of the app.
     * The picks are restored from the preferences, so that they are remembered across restarts
     * of the app.
     */
    private val _selectedColors = MutableStateFlow<Map<String, Int>>(
        decodeSelectedColors(preferenceManager.selectedIconColors.get()),
    )
    val selectedColors: StateFlow<Map<String, Int>> = _selectedColors.asStateFlow()

    /**
     * Uses [color] as the foreground colour of the icon of the app with the given
     * [packageName].
     */
    fun selectColor(packageName: String, color: Int) {
        val colors = _selectedColors.updateAndGet { it + (packageName to color) }
        preferenceManager.selectedIconColors.set(encodeSelectedColors(colors))
    }

    /**
     * The shape picked by the user for the painted icons. The pick is restored from the
     * preferences, so that it is remembered across restarts of the app.
     */
    private val _selectedShape = MutableStateFlow(
        IconShape.fromName(preferenceManager.selectedIconShape.get()),
    )
    val selectedShape: StateFlow<IconShape> = _selectedShape.asStateFlow()

    /**
     * Uses [shape] as the shape of the painted icons.
     */
    fun selectShape(shape: IconShape) {
        _selectedShape.value = shape
        preferenceManager.selectedIconShape.set(shape.name)
    }

    /**
     * The state of the export of the painted icons as an icon pack.
     */
    private val _exportState = MutableStateFlow<IconPackExportState>(IconPackExportState.Idle)
    val exportState: StateFlow<IconPackExportState> = _exportState.asStateFlow()

    /**
     * Builds an icon pack with the painted icons of the covered apps and saves it
     * as a plain file.
     */
    fun exportIconPack() {
        if (_exportState.value is IconPackExportState.Building) return
        _exportState.value = IconPackExportState.Building
        viewModelScope.launch {
            _exportState.value = try {
                val apps = installedApps.value.filter { it.iconInfo != null }
                val result = iconPackExportRepository.export(
                    apps,
                    selectedColors.value,
                    selectedShape.value,
                )
                IconPackExportState.Saved(result.location, result.file)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                Log.e("AppsViewModel", "Could not export the icon pack", e)
                IconPackExportState.Failed(e.message ?: e.toString())
            }
        }
    }

    /**
     * Returns the export to [IconPackExportState.Idle], once it is done.
     */
    fun dismissExportResult() {
        if (_exportState.value !is IconPackExportState.Building) {
            _exportState.value = IconPackExportState.Idle
        }
    }
}
