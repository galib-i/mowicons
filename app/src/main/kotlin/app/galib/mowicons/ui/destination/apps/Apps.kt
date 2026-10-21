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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.galib.mowicons.R
import app.galib.mowicons.data.model.IconShape
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.ui.components.core.LawniconsScaffold
import app.galib.mowicons.ui.components.home.iconpreview.IconPreview
import app.galib.mowicons.ui.theme.icon.Check
import app.galib.mowicons.ui.theme.icon.LawnIcons
import app.galib.mowicons.ui.theme.icon.Palette
import app.galib.mowicons.ui.theme.icon.Wrench
import app.galib.mowicons.ui.util.PreviewLawnicons
import app.galib.mowicons.ui.util.PreviewProviders
import app.galib.mowicons.ui.util.SampleData
import dev.zacsweers.metrox.viewmodel.metroViewModel
import java.io.File
import kotlinx.serialization.Serializable

/** The size of the icons in the grid. */
private val IconSize = 48.dp

@Serializable
data object Apps : NavKey

fun EntryProviderScope<NavKey>.appsDestination(
    isExpandedScreen: Boolean,
    onBack: () -> Unit,
) {
    entry<Apps> {
        Apps(
            onBack = onBack,
            isExpandedScreen = isExpandedScreen,
        )
    }
}

@Composable
private fun Apps(
    onBack: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    appsViewModel: AppsViewModel = metroViewModel(),
) {
    val installedApps by appsViewModel.installedApps.collectAsStateWithLifecycle()
    val selectedColors by appsViewModel.selectedColors.collectAsStateWithLifecycle()
    val selectedShape by appsViewModel.selectedShape.collectAsStateWithLifecycle()
    val exportState by appsViewModel.exportState.collectAsStateWithLifecycle()

    AppsScreen(
        installedApps = installedApps,
        selectedColors = selectedColors,
        selectedShape = selectedShape,
        exportState = exportState,
        onSelectColor = appsViewModel::selectColor,
        onSelectShape = appsViewModel::selectShape,
        onExportIconPack = appsViewModel::exportIconPack,
        onDismissExportResult = appsViewModel::dismissExportResult,
        onBack = onBack,
        isExpandedScreen = isExpandedScreen,
        modifier = modifier,
    )
}

@Composable
private fun AppsScreen(
    installedApps: List<InstalledApp>,
    selectedColors: Map<String, Int>,
    selectedShape: IconShape,
    exportState: IconPackExportState,
    onSelectColor: (String, Int) -> Unit,
    onSelectShape: (IconShape) -> Unit,
    onExportIconPack: () -> Unit,
    onDismissExportResult: () -> Unit,
    onBack: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    var isColorPickerEnabled by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }

    LawniconsScaffold(
        modifier = modifier,
        title = stringResource(R.string.painted_apps),
        onBack = onBack,
        isExpandedScreen = isExpandedScreen,
        actions = {
            IconButton(onClick = { showExportDialog = true }) {
                Icon(
                    imageVector = LawnIcons.Wrench,
                    contentDescription = stringResource(R.string.export_painted_icons),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = { isColorPickerEnabled = !isColorPickerEnabled }) {
                Icon(
                    imageVector = LawnIcons.Palette,
                    contentDescription = stringResource(R.string.change_color),
                    tint = if (isColorPickerEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(layoutDirection),
            ),
        ) {
            ShapePicker(
                selectedShape = selectedShape,
                onSelectShape = onSelectShape,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    bottom = paddingValues.calculateBottomPadding(),
                ),
                modifier = Modifier.weight(1f),
            ) {
                items(
                    items = installedApps,
                    key = { it.packageName },
                ) { app ->
                    InstalledAppItem(
                        app = app,
                        color = selectedColors[app.packageName] ?: app.colors.firstOrNull(),
                        shape = selectedShape,
                        isColorPickerEnabled = isColorPickerEnabled,
                        onSelectColor = { onSelectColor(app.packageName, it) },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            state = exportState,
            coveredAppCount = installedApps.count { it.iconInfo != null },
            onExport = onExportIconPack,
            onDismiss = {
                showExportDialog = false
                onDismissExportResult()
            },
        )
    }
}

/**
 * The dialog which lets the user export the painted icons as an icon pack and
 * shows the progress and the result of the export.
 */
@Composable
private fun ExportDialog(
    state: IconPackExportState,
    coveredAppCount: Int,
    onExport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (state !is IconPackExportState.Building) onDismiss()
        },
        title = {
            Text(
                stringResource(
                    when (state) {
                        IconPackExportState.Idle,
                        IconPackExportState.Building,
                        -> R.string.export_painted_icons_title

                        is IconPackExportState.Saved,
                        is IconPackExportState.Failed,
                        -> R.string.export_painted_icons
                    },
                ),
            )
        },
        text = {
            when (state) {
                IconPackExportState.Idle -> Text(
                    stringResource(R.string.export_painted_icons_text, coveredAppCount),
                )
                IconPackExportState.Building -> Column {
                    Text(stringResource(R.string.export_painted_icons_building))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    )
                }
                is IconPackExportState.Saved -> Text(
                    stringResource(R.string.export_painted_icons_saved, state.location),
                )
                is IconPackExportState.Failed -> Text(
                    stringResource(R.string.export_painted_icons_failed, state.reason),
                )
            }
        },
        confirmButton = {
            when (state) {
                IconPackExportState.Idle -> TextButton(
                    onClick = onExport,
                    enabled = coveredAppCount > 0,
                ) {
                    Text(stringResource(R.string.export))
                }
                IconPackExportState.Building -> Unit
                is IconPackExportState.Saved -> TextButton(
                    onClick = {
                        installIconPack(context, state.file)
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.install))
                }
                is IconPackExportState.Failed -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        },
        dismissButton = {
            when (state) {
                IconPackExportState.Idle -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                IconPackExportState.Building -> Unit
                is IconPackExportState.Saved -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
                is IconPackExportState.Failed -> Unit
            }
        },
    )
}

/**
 * Opens the exported icon pack, so that the user can install it.
 */
private fun installIconPack(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("Apps", "Could not install APK", e)
    }
}

@Composable
private fun InstalledAppItem(
    app: InstalledApp,
    color: Int?,
    shape: IconShape,
    isColorPickerEnabled: Boolean,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        val iconInfo = app.iconInfo
        if (iconInfo != null) {
            var showSheet by rememberSaveable { mutableStateOf(false) }
            var showColors by rememberSaveable { mutableStateOf(false) }
            Box {
                IconPreview(
                    iconInfo = iconInfo,
                    // IconPreview pads the icon itself, hence the additional space.
                    modifier = Modifier.size(IconSize + 16.dp),
                    tint = color?.let { Color(it) },
                    shape = shape.polygon,
                    showSheet = showSheet,
                    onToggleSheet = { visible ->
                        if (isColorPickerEnabled) {
                            showColors = !showColors
                        } else {
                            showSheet = visible
                        }
                    },
                )
                ColorMenu(
                    colors = app.colors,
                    currentColor = color,
                    expanded = showColors && app.colors.size > 1,
                    onSelectColor = {
                        onSelectColor(it)
                        showColors = false
                    },
                    onDismissRequest = { showColors = false },
                )
            }
        } else {
            Image(
                bitmap = remember(app.systemIcon) { app.systemIcon.asImageBitmap() },
                contentDescription = app.label,
                modifier = Modifier.size(IconSize),
            )
        }
        TruncatedLabel(
            text = app.label,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * The suffix which marks a label which had to be shortened.
 */
private const val TruncationSuffix = "..."

/**
 * The amount of characters a label may have before it is shortened.
 */
private const val MaxLabelLength = 10

/**
 * A label which is shortened to [MaxLabelLength] characters, ending with [TruncationSuffix],
 * if it is longer than that.
 */
@Composable
private fun TruncatedLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shortenedText = remember(text) {
        if (text.length > MaxLabelLength) text.take(MaxLabelLength) + TruncationSuffix else text
    }
    Text(
        text = shortenedText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/**
 * The indent of the title of the screen, which the label of the shape picker is aligned with.
 */
private val TitleIndent = 16.dp

/**
 * A picker which lets the user pick the shape the painted icons have.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapePicker(
    selectedShape: IconShape,
    onSelectShape: (IconShape) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedShape) {
        listState.animateScrollToItem(selectedShape.ordinal)
    }
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = TitleIndent),
        ) {
            Text(
                text = stringResource(R.string.shape),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(selectedShape.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            items(
                items = IconShape.entries,
                key = { it.name },
            ) { shape ->
                ShapeItem(
                    shape = shape,
                    isSelected = shape == selectedShape,
                    onClick = { onSelectShape(shape) },
                )
            }
        }
    }
}

/**
 * A single shape of the [ShapePicker], which is highlighted when it is the selected one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapeItem(
    shape: IconShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(shape.labelRes)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape.polygon.toShape())
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
        )
        if (isSelected) {
            Icon(
                imageVector = LawnIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * A menu with the colors of the original icon of the app, which lets the user pick the
 * foreground color of the icon of the pack.
 */
@Composable
private fun ColorMenu(
    colors: List<Int>,
    currentColor: Int?,
    expanded: Boolean,
    onSelectColor: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        colors.forEach { color ->
            DropdownMenuItem(
                text = { Text(color.toHexString()) },
                onClick = { onSelectColor(color) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(color)),
                    )
                },
                trailingIcon = {
                    if (color == currentColor) {
                        Icon(
                            imageVector = LawnIcons.Check,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}

/** The given color as a hex string, e.g. `#C6C6C6`. */
private fun Int.toHexString(): String =
    "#" + (this and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

@PreviewLawnicons
@Composable
private fun AppsScreenPreview() {
    PreviewProviders {
        AppsScreen(
            installedApps = listOf(
                InstalledApp(
                    label = "Email",
                    packageName = "com.android.email",
                    className = "com.android.email.MainActivity",
                    systemIcon = sampleIcon(),
                    colors = listOf(0xFF005E9E.toInt(), 0xFFD93025.toInt()),
                    iconInfo = SampleData.iconInfoList[0],
                ),
                InstalledApp(
                    label = "Phone",
                    packageName = "com.android.phone",
                    className = "com.android.phone.MainActivity",
                    systemIcon = sampleIcon(),
                    iconInfo = SampleData.iconInfoList[1],
                ),
            ),
            selectedColors = mapOf("com.android.email" to 0xFFD93025.toInt()),
            selectedShape = IconShape.Circle,
            exportState = IconPackExportState.Idle,
            onSelectColor = { _, _ -> },
            onSelectShape = {},
            onExportIconPack = {},
            onDismissExportResult = {},
            onBack = {},
            isExpandedScreen = false,
        )
    }
}

private fun sampleIcon(): Bitmap = createBitmap(48, 48).apply {
    eraseColor(android.graphics.Color.argb(255, 68, 68, 68))
}
