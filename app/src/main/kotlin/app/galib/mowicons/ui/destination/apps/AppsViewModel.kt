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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.galib.mowicons.data.model.Component
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.data.model.IconInfoModel
import app.galib.mowicons.data.model.IconShape
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.data.model.PaintedIconSettings
import app.galib.mowicons.data.model.SearchMode
import app.galib.mowicons.data.repository.IconPackExportRepository
import app.galib.mowicons.data.repository.InstalledAppsRepository
import app.galib.mowicons.data.repository.PreferenceManager
import app.galib.mowicons.data.repository.home.IconRepository
import app.galib.mowicons.data.util.decodeSelectedColors
import app.galib.mowicons.data.util.encodeSelectedColors
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val _searchMode = MutableStateFlow(SearchMode.LABEL)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    val searchTermTextState = TextFieldState()

    val searchResults: StateFlow<IconInfoModel> = iconRepository.searchedIconInfoModel

    /**
     * Searches the icons for [query] in the current [searchMode].
     */
    fun searchIcons(query: String) {
        viewModelScope.launch {
            iconRepository.search(_searchMode.value, query)
        }
    }

    /**
     * Uses [mode] for the search and repeats the current search with it.
     */
    fun changeMode(mode: SearchMode) {
        if (_searchMode.value == mode) return
        _searchMode.value = mode
        searchIcons(searchTermTextState.text.toString())
    }

    /**
     * The apps installed on the device which are covered by the icon pack, each one paired
     * with the icon of the pack. Apps without an icon are left out, as their system icon
     * can't be painted.
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
            iconInfo?.let { app.copy(iconInfo = it) }
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
     * Forgets the colour picked for the app with the given [packageName], so that the
     * app uses the global foreground colour again.
     */
    fun clearColor(packageName: String) {
        val colors = _selectedColors.updateAndGet { it - packageName }
        preferenceManager.selectedIconColors.set(encodeSelectedColors(colors))
    }

    /**
     * The settings which decide how the painted icons look. They are restored from the
     * preferences, so that they are remembered across restarts of the app.
     */
    private val _paintedIconSettings = MutableStateFlow(
        PaintedIconSettings(
            useOriginalForeground = preferenceManager.paintedUseOriginalForeground.get(),
            foregroundColor = preferenceManager.paintedForegroundColor.get(),
            backgroundColor = preferenceManager.paintedBackgroundColor.get(),
            shape = IconShape.fromName(preferenceManager.selectedIconShape.get()),
        ),
    )
    val paintedIconSettings: StateFlow<PaintedIconSettings> = _paintedIconSettings.asStateFlow()

    /**
     * Applies the given [settings] and remembers them for the next start of the app.
     */
    fun updatePaintedIconSettings(settings: PaintedIconSettings) {
        val previous = _paintedIconSettings.value
        if (previous == settings) return
        _paintedIconSettings.value = settings

        if (settings.useOriginalForeground != previous.useOriginalForeground) {
            preferenceManager.paintedUseOriginalForeground.set(settings.useOriginalForeground)
        }
        if (settings.foregroundColor != previous.foregroundColor) {
            preferenceManager.paintedForegroundColor.set(settings.foregroundColor)
        }
        if (settings.backgroundColor != previous.backgroundColor) {
            preferenceManager.paintedBackgroundColor.set(settings.backgroundColor)
        }
        if (settings.shape != previous.shape) {
            preferenceManager.selectedIconShape.set(settings.shape.name)
        }
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
                val result = iconPackExportRepository.export(
                    installedApps.value,
                    selectedColors.value,
                    _paintedIconSettings.value,
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
