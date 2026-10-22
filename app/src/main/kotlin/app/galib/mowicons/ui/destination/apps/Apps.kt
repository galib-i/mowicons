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
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.galib.mowicons.R
import app.galib.mowicons.data.model.IconInfoModel
import app.galib.mowicons.data.model.IconShape
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.data.model.PaintedIconSettings
import app.galib.mowicons.data.model.SearchMode
import app.galib.mowicons.ui.components.TopBarButtons
import app.galib.mowicons.ui.components.home.HomeTopBar
import app.galib.mowicons.ui.components.home.iconpreview.IconPreview
import app.galib.mowicons.ui.components.home.search.rememberSearchState
import app.galib.mowicons.ui.theme.icon.Check
import app.galib.mowicons.ui.theme.icon.LawnIcons
import app.galib.mowicons.ui.theme.icon.Pencil
import app.galib.mowicons.ui.theme.icon.Wrench
import app.galib.mowicons.ui.util.PreviewLawnicons
import app.galib.mowicons.ui.util.PreviewProviders
import app.galib.mowicons.ui.util.SampleData
import dev.zacsweers.metrox.viewmodel.metroViewModel
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/** The size of the icons in the grid. */
private val IconSize = 48.dp

@Serializable
data object Apps : NavKey

fun EntryProviderScope<NavKey>.appsDestination(
    isExpandedScreen: Boolean,
    onNavigateToAbout: () -> Unit = {},
) {
    entry<Apps> {
        Apps(
            onNavigateToAbout = onNavigateToAbout,
            isExpandedScreen = isExpandedScreen,
        )
    }
}

/**
 * The state of the screen which the screen itself doesn't own.
 */
private data class AppsUiState(
    val installedApps: List<InstalledApp>,
    val selectedColors: Map<String, Int>,
    val paintedIcons: PaintedIconSettings,
    val exportState: IconPackExportState,
    val searchResults: IconInfoModel,
    val searchMode: SearchMode,
)

@Composable
private fun Apps(
    onNavigateToAbout: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    appsViewModel: AppsViewModel = metroViewModel(),
) {
    val installedApps by appsViewModel.installedApps.collectAsStateWithLifecycle()
    val selectedColors by appsViewModel.selectedColors.collectAsStateWithLifecycle()
    val paintedIcons by appsViewModel.paintedIconSettings.collectAsStateWithLifecycle()
    val exportState by appsViewModel.exportState.collectAsStateWithLifecycle()
    val searchResults by appsViewModel.searchResults.collectAsStateWithLifecycle()
    val searchMode by appsViewModel.searchMode.collectAsStateWithLifecycle()
    val searchTermTextState = appsViewModel.searchTermTextState

    AppsScreen(
        uiState = AppsUiState(
            installedApps = installedApps,
            selectedColors = selectedColors,
            paintedIcons = paintedIcons,
            exportState = exportState,
            searchResults = searchResults,
            searchMode = searchMode,
        ),
        onPaintedIconsChange = appsViewModel::updatePaintedIconSettings,
        onSelectColor = appsViewModel::selectColor,
        onClearColor = appsViewModel::clearColor,
        onExportIconPack = appsViewModel::exportIconPack,
        onDismissExportResult = appsViewModel::dismissExportResult,
        onNavigateToAbout = onNavigateToAbout,
        isExpandedScreen = isExpandedScreen,
        searchTermTextState = searchTermTextState,
        onSearchIcons = appsViewModel::searchIcons,
        onChangeSearchMode = appsViewModel::changeMode,
        modifier = modifier,
    )
}

@Composable
private fun AppsScreen(
    uiState: AppsUiState,
    onPaintedIconsChange: (PaintedIconSettings) -> Unit,
    onSelectColor: (String, Int) -> Unit,
    onClearColor: (String) -> Unit,
    onExportIconPack: () -> Unit,
    onDismissExportResult: () -> Unit,
    onNavigateToAbout: () -> Unit,
    isExpandedScreen: Boolean,
    searchTermTextState: TextFieldState,
    onSearchIcons: (String) -> Unit,
    onChangeSearchMode: (SearchMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    var isColorPickerEnabled by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }

    val searchState = rememberSearchState(
        textFieldState = searchTermTextState,
        mode = uiState.searchMode,
        onModeChange = onChangeSearchMode,
    )

    // The search is triggered once the user stopped typing for a moment.
    LaunchedEffect(searchState.textFieldState.text) {
        delay(300.milliseconds)
        onSearchIcons(searchState.searchTerm)
    }

    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                )
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = TitleIndent,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    end = TitleIndent,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TopBarButtons(
                        onNavigateToAbout = onNavigateToAbout,
                        onExpandSearch = {
                            coroutineScope.launch {
                                searchState.searchBarState.animateToExpanded()
                            }
                        },
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = LawnIcons.Wrench,
                            contentDescription = stringResource(R.string.export_painted_icons),
                        )
                    }
                    IconButton(onClick = { isColorPickerEnabled = !isColorPickerEnabled }) {
                        Icon(
                            imageVector = LawnIcons.Pencil,
                            contentDescription = stringResource(R.string.change_color),
                            tint = if (isColorPickerEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.defaults),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        start = TitleIndent,
                        top = 16.dp,
                        bottom = 4.dp,
                    ),
                )
                PaintedForegroundPicker(
                    settings = uiState.paintedIcons,
                    onSettingsChange = onPaintedIconsChange,
                    modifier = Modifier.padding(top = 4.dp),
                )
                PaintedBackgroundPicker(
                    settings = uiState.paintedIcons,
                    onSettingsChange = onPaintedIconsChange,
                    modifier = Modifier.padding(top = 12.dp),
                )
                ShapePicker(
                    selectedShape = uiState.paintedIcons.shape,
                    onSelectShape = { shape ->
                        onPaintedIconsChange(uiState.paintedIcons.copy(shape = shape))
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    start = TitleIndent,
                    end = TitleIndent,
                    top = 8.dp,
                    bottom = 120.dp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(
                    items = uiState.installedApps,
                    key = { it.packageName },
                ) { app ->
                    InstalledAppItem(
                        app = app,
                        settings = uiState.paintedIcons,
                        selectedColor = uiState.selectedColors[app.packageName],
                        isColorPickerEnabled = isColorPickerEnabled,
                        onSelectColor = { onSelectColor(app.packageName, it) },
                        onClearColor = { onClearColor(app.packageName) },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }

    // The search bar is only shown when it is expanded, as the screen has its own top bar.
    val isSearchExpanded = searchState.searchBarState.currentValue == SearchBarValue.Expanded ||
        searchState.searchBarState.targetValue == SearchBarValue.Expanded
    if (isSearchExpanded) {
        HomeTopBar(
            searchState = searchState,
            iconInfoModel = uiState.searchResults,
            isExpandedScreen = isExpandedScreen,
        )
    }

    if (showExportDialog) {
        ExportDialog(
            state = uiState.exportState,
            coveredAppCount = uiState.installedApps.count { it.iconInfo != null },
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
    settings: PaintedIconSettings,
    selectedColor: Int?,
    isColorPickerEnabled: Boolean,
    onSelectColor: (Int) -> Unit,
    onClearColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconInfo = app.iconInfo ?: return
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var showColors by rememberSaveable { mutableStateOf(false) }
    val color = settings.foregroundColorFor(selectedColor, app.colors.firstOrNull())
    // In Original mode the default colour is the reset colour, which is applied as a
    // colour of its own, as clearing the colour would bring back the one of the icon.
    val defaultColor = if (settings.useOriginalForeground) {
        PaintedIconSettings.DefaultForegroundColor
    } else {
        settings.foregroundColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box {
            IconPreview(
                iconInfo = iconInfo,
                // IconPreview pads the icon itself, hence the additional space.
                modifier = Modifier.size(IconSize + 16.dp),
                tint = color?.let { Color(it) },
                iconBackground = Color(settings.backgroundColor),
                shape = settings.shape.polygon,
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
                defaultColor = defaultColor,
                expanded = showColors,
                onSelectColor = {
                    onSelectColor(it)
                    showColors = false
                },
                onUseDefaultColor = {
                    if (settings.useOriginalForeground) {
                        onSelectColor(defaultColor)
                    } else {
                        onClearColor()
                    }
                    showColors = false
                },
                onDismissRequest = { showColors = false },
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
 * The horizontal padding of the sections of the screen, which keeps their titles and
 * pickers in line with each other.
 */
private val TitleIndent = 16.dp

/**
 * The height of the segmented button which picks the foreground of the painted icons.
 */
private val SegmentedButtonHeight = 32.dp

/**
 * The height of the field in which the colours are entered.
 */
private val ColorFieldHeight = 48.dp

/**
 * A picker which lets the user pick the foreground of the painted icons.
 *
 * The foreground is either taken from the original icon of the app or set to a single colour
 * for all of them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaintedForegroundPicker(
    settings: PaintedIconSettings,
    onSettingsChange: (PaintedIconSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useOriginalForeground = settings.useOriginalForeground

    Column(modifier.padding(horizontal = TitleIndent)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            PickerTitle(stringResource(R.string.foreground_color))
            Spacer(modifier = Modifier.weight(1f))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.height(SegmentedButtonHeight),
            ) {
                SegmentedButton(
                    selected = !useOriginalForeground,
                    onClick = {
                        onSettingsChange(settings.copy(useOriginalForeground = false))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.height(SegmentedButtonHeight),
                    // The check mark is left out, as the selected button is already
                    // highlighted by its background.
                    icon = {},
                    label = {
                        SegmentedButtonLabel(stringResource(R.string.hex))
                    },
                )
                SegmentedButton(
                    selected = useOriginalForeground,
                    onClick = {
                        onSettingsChange(settings.copy(useOriginalForeground = true))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.height(SegmentedButtonHeight),
                    icon = {},
                    label = {
                        SegmentedButtonLabel(stringResource(R.string.original_icons))
                    },
                )
            }
        }
        if (!useOriginalForeground) {
            HexColorField(
                color = settings.foregroundColor,
                resetColor = PaintedIconSettings.DefaultForegroundColor,
                onColorChange = { color ->
                    onSettingsChange(settings.copy(foregroundColor = color))
                },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * A picker which lets the user pick the background of the painted icons.
 */
@Composable
private fun PaintedBackgroundPicker(
    settings: PaintedIconSettings,
    onSettingsChange: (PaintedIconSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = TitleIndent)) {
        PickerTitle(stringResource(R.string.background_color))
        HexColorField(
            color = settings.backgroundColor,
            resetColor = PaintedIconSettings.DefaultBackgroundColor,
            onColorChange = { color ->
                onSettingsChange(settings.copy(backgroundColor = color))
            },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

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
    Column(modifier.padding(horizontal = TitleIndent)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            PickerTitle(stringResource(R.string.shape))
            Text(
                text = stringResource(selectedShape.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 4.dp),
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
 * The title of a picker, e.g. "Foreground colour:".
 *
 * All pickers share the same title, so that they look alike.
 */
@Composable
private fun PickerTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * The label of a segmented button, which is centred in the button.
 */
@Composable
private fun SegmentedButtonLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

/**
 * A field which lets the user enter a colour as a hex string, next to a swatch of it and a
 * button which resets the colour to [resetColor].
 */
@Composable
private fun HexColorField(
    color: Int,
    resetColor: Int,
    onColorChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var digits by remember(color) { mutableStateOf(color.toHexDigits()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(digits.hexToColorOrNull() ?: color)),
        )
        OutlinedTextField(
            value = digits,
            onValueChange = { value ->
                val newDigits = value.uppercase()
                    .filter { it.isDigit() || it in 'A'..'F' }
                    .take(6)
                digits = newDigits
                newDigits.hexToColorOrNull()?.let(onColorChange)
            },
            prefix = { Text("#") },
            placeholder = { Text("RRGGBB") },
            isError = digits.isNotEmpty() && digits.length < 6,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            // heightIn, instead of height, so that the text is laid out within the
            // requested height instead of being squeezed into it.
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .heightIn(min = ColorFieldHeight),
        )
        TextButton(
            onClick = {
                digits = resetColor.toHexDigits()
                onColorChange(resetColor)
            },
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Text(stringResource(R.string.reset))
        }
    }
}

/**
 * A menu with the colours of the original icon of the app, which lets the user pick the
 * foreground colour of the icon of the pack.
 *
 * The first entry resets the icon to the default foreground colour: the one picked for all of
 * the icons when their colour is not taken from the original icon, and the default colour of
 * the painted icons otherwise.
 */
@Composable
private fun ColorMenu(
    colors: List<Int>,
    currentColor: Int?,
    defaultColor: Int?,
    expanded: Boolean,
    onSelectColor: (Int) -> Unit,
    onUseDefaultColor: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        if (defaultColor != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.default_colour)) },
                onClick = onUseDefaultColor,
                leadingIcon = { ColorSwatch(defaultColor) },
                trailingIcon = {
                    if (currentColor == defaultColor) {
                        Icon(
                            imageVector = LawnIcons.Check,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
        colors.forEach { color ->
            DropdownMenuItem(
                text = { Text(color.toHexString()) },
                onClick = { onSelectColor(color) },
                leadingIcon = { ColorSwatch(color) },
                trailingIcon = {
                    // The default entry already marks the colour when it is the same.
                    if (color == currentColor && color != defaultColor) {
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

/**
 * A round swatch of the given [color], which is shown in front of the entries of the colour
 * menu.
 */
@Composable
private fun ColorSwatch(color: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(color)),
    )
}

/** The given colour as six hex digits, e.g. `C6C6C6`. */
private fun Int.toHexDigits(): String =
    (this and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

/** The given colour as a hex string, e.g. `#C6C6C6`. */
private fun Int.toHexString(): String = "#" + toHexDigits()

/**
 * The colour the given hex digits describe, or null if they don't describe a colour.
 */
private fun String.hexToColorOrNull(): Int? = if (length == 6 && all { it.isDigit() || it in 'A'..'F' }) {
    0xFF000000.toInt() or toLong(16).toInt()
} else {
    null
}

@PreviewLawnicons
@Composable
private fun AppsScreenPreview() {
    PreviewProviders {
        AppsScreen(
            uiState = AppsUiState(
                installedApps = listOf(
                    InstalledApp(
                        label = "Email",
                        packageName = "com.android.email",
                        className = "com.android.email.MainActivity",
                        colors = listOf(0xFF005E9E.toInt(), 0xFFD93025.toInt()),
                        iconInfo = SampleData.iconInfoList[0],
                    ),
                    InstalledApp(
                        label = "Phone",
                        packageName = "com.android.phone",
                        className = "com.android.phone.MainActivity",
                        iconInfo = SampleData.iconInfoList[1],
                    ),
                ),
                selectedColors = mapOf("com.android.email" to 0xFFD93025.toInt()),
                paintedIcons = PaintedIconSettings(
                    useOriginalForeground = true,
                    foregroundColor = 0xFFC6C6C6.toInt(),
                    backgroundColor = 0xFF222224.toInt(),
                    shape = IconShape.Circle,
                ),
                exportState = IconPackExportState.Idle,
                searchResults = IconInfoModel(SampleData.iconInfoList, SampleData.iconInfoList.size),
                searchMode = SearchMode.LABEL,
            ),
            onPaintedIconsChange = {},
            onSelectColor = { _, _ -> },
            onClearColor = {},
            onExportIconPack = {},
            onDismissExportResult = {},
            onNavigateToAbout = {},
            isExpandedScreen = false,
            searchTermTextState = TextFieldState(),
            onSearchIcons = {},
            onChangeSearchMode = {},
        )
    }
}
