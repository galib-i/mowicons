/*
 * Copyright 2025 Lawnchair Launcher
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

package app.galib.mowicons.ui.destination.debugmenu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.data.model.getFirstLabelAndComponent
import app.galib.mowicons.data.model.splitByComponentName
import app.galib.mowicons.data.repository.BasePreferenceManager
import app.galib.mowicons.data.repository.InMemoryPreferenceStore
import app.galib.mowicons.data.repository.PreferenceManager
import app.galib.mowicons.ui.components.core.LawniconsScaffold
import app.galib.mowicons.ui.components.core.ListRow
import app.galib.mowicons.ui.components.core.ListRowDefaults
import app.galib.mowicons.ui.components.core.ListRowLabel
import app.galib.mowicons.ui.components.core.SimpleListRow
import app.galib.mowicons.ui.theme.adaptiveSurfaceContainerColor
import app.galib.mowicons.ui.theme.icon.Copy
import app.galib.mowicons.ui.theme.icon.LawnIcons
import app.galib.mowicons.ui.util.PreviewLawnicons
import app.galib.mowicons.ui.util.PreviewProviders
import app.galib.mowicons.ui.util.SampleData
import app.galib.mowicons.ui.util.copyTextToClipboard
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable

@Serializable
data object DebugMenu : NavKey

fun EntryProviderScope<NavKey>.debugMenuDestination(
    onBack: () -> Unit,
    isExpandedScreen: Boolean = false,
) {
    entry<DebugMenu> {
        DebugMenu(
            onBack = onBack,
            isExpandedScreen = isExpandedScreen,
        )
    }
}

// screen to view statistics and other internal info
@Composable
fun DebugMenu(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DebugMenuViewModel = metroViewModel(),
    isExpandedScreen: Boolean = false,
) {
    val iconInfoModel by viewModel.iconInfoModel.collectAsStateWithLifecycle()
    val newIconsModel by viewModel.newIconsModel.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val prefs = viewModel.preferenceManager

    val componentCount = remember(iconInfoModel) {
        iconInfoModel.iconInfo.splitByComponentName().size
    }

    val debugList = listOf(
        ListItemContent("Icon count", iconInfoModel.iconCount),
        ListItemContent("New icons count", newIconsModel.iconCount),
        ListItemContent("Component count", componentCount),
    )

    val boolPrefs = listOf(
        prefs.forceEnableIconRequest,
        prefs.isFirstLaunch,
    )

    DebugMenuScreen(
        newIconComponents = remember(newIconsModel) {
            newIconsModel.iconInfo.splitByComponentName()
        },
        debugList = debugList,
        boolPrefs = boolPrefs,
        onCopy = {
            context.copyTextToClipboard(it)
        },
        isExpandedScreen = isExpandedScreen,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun DebugMenuScreen(
    newIconComponents: List<IconInfo>,
    debugList: List<ListItemContent>,
    boolPrefs: List<BasePreferenceManager.BoolPref>,
    onCopy: (String) -> Unit,
    isExpandedScreen: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LawniconsScaffold(
        title = "Debug menu",
        onBack = onBack,
        isExpandedScreen = isExpandedScreen,
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
        ) {
            item {
                SimpleListRow(
                    label = "Crash app",
                    onClick = {
                        throw RuntimeException("User triggered crash")
                    },
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
            }
            itemsIndexed(debugList) { index, it ->
                SimpleListRow(
                    label = it.label,
                    description = it.value.toString(),
                    background = true,
                    shapes = ListItemDefaults.segmentedShapes(index, debugList.size),
                ) {
                    onCopy("${it.label}: ${it.value}")
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
            }
            itemsIndexed(boolPrefs) { index, pref ->
                val checked by pref.asState()
                ListItem(
                    checked = checked,
                    onCheckedChange = pref::set,
                    content = {
                        ListRowLabel(pref.key)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = adaptiveSurfaceContainerColor,
                    ),
                    modifier = Modifier.padding(horizontal = ListRowDefaults.basePadding),
                    trailingContent = {
                        Switch(
                            checked = checked,
                            onCheckedChange = pref::set,
                            interactionSource = null,
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index,
                        boolPrefs.size,
                        ListRowDefaults.singleItemShapes,
                    ),
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
            item {
                ListHeader(
                    "New icons list",
                    {
                        onCopy(
                            newIconComponents.toString(),
                        )
                    },
                )
            }
            items(
                items = newIconComponents,
                key = { it.drawableName },
            ) {
                val labelAndComponent = it.getFirstLabelAndComponent()

                ListRow(
                    label = {
                        Text(
                            text = "${labelAndComponent.label}\n${labelAndComponent.component.flattenToString()}",
                            fontFamily = FontFamily.Monospace,
                        )
                    },
                )
            }
        }
    }
}

data class ListItemContent(
    val label: String,
    val value: Int,
)

@Composable
private fun ListHeader(
    label: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.weight(0.05f))
        IconButton(
            onClick = onCopy,
        ) {
            Icon(
                imageVector = LawnIcons.Copy,
                contentDescription = null,
            )
        }
    }
}

@PreviewLawnicons
@Composable
private fun DebugMenuScreenPreview() {
    PreviewProviders {
        val prefs = PreferenceManager(InMemoryPreferenceStore())

        DebugMenuScreen(
            newIconComponents = SampleData.iconInfoList,
            debugList = listOf(
                ListItemContent("Icon count", 100),
            ),
            boolPrefs = listOf(
                prefs.forceEnableIconRequest,
            ),
            onCopy = {},
            isExpandedScreen = true,
            onBack = {},
        )
    }
}
