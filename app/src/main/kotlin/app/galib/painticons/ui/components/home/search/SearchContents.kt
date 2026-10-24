package app.galib.painticons.ui.components.home.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.galib.painticons.R
import app.galib.painticons.data.model.IconInfo
import app.galib.painticons.data.model.SearchMode
import app.galib.painticons.data.model.getFirstLabelAndComponent
import app.galib.painticons.ui.LocalLawniconsActions
import app.galib.painticons.ui.components.home.iconpreview.IconInfoSheet
import app.galib.painticons.ui.components.home.iconpreview.IconPreview
import app.galib.painticons.ui.theme.PrimaryBackgroundColor
import app.galib.painticons.ui.theme.icon.Check
import app.galib.painticons.ui.theme.icon.LawnIcons
import app.galib.painticons.ui.util.PreviewLawnicons
import app.galib.painticons.ui.util.PreviewProviders
import app.galib.painticons.ui.util.SampleData

@Composable
fun ResponsiveSearchBarContents(
    isExpandedScreen: Boolean,
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isExpandedScreen) {
        ExpandedDockedSearchBar(
            state = state,
            inputField = inputField,
            modifier = modifier,
            content = content,
        )
    } else {
        ExpandedFullScreenSearchBar(
            state = state,
            inputField = inputField,
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
fun SearchContents(
    state: SearchState,
    iconInfo: List<IconInfo>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SearchModeSelector(
            currentMode = state.mode,
            onModeChange = { state.setMode(it) },
        )

        val actions = LocalLawniconsActions.current

        Crossfade(
            targetState = iconInfo.size,
            label = "On item count modified",
        ) { count ->
            when (count) {
                0 -> EmptyState(state.searchTerm)

                1 -> IconInfoListItem(
                    iconInfo = iconInfo.firstOrNull(),
                    showSheet = state.isSheetVisible,
                    onToggleSheet = { state.isSheetVisible = it },
                    isIconPicker = actions.isIconPicker,
                    onSendResult = actions.onSendResult,
                )

                else -> IconGrid(
                    iconInfo = iconInfo,
                    isIconPicker = actions.isIconPicker,
                    onSendResult = actions.onSendResult,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(searchTerm: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(R.string.no_items_found, searchTerm),
        )
    }
}

@Composable
private fun IconGrid(
    iconInfo: List<IconInfo>,
    isIconPicker: Boolean,
    onSendResult: (IconInfo) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
    ) {
        itemsIndexed(
            items = iconInfo,
            key = { _, item -> item.drawableName },
            contentType = { _, _ -> "icon_preview" },
        ) { _, it ->
            val isIconInfoShown = rememberSaveable { mutableStateOf(false) }
            IconPreview(
                iconInfo = it,
                iconBackground = PrimaryBackgroundColor(),
                showSheet = isIconInfoShown.value,
                onToggleSheet = { isIconInfoShown.value = it },
                isIconPicker = isIconPicker,
                onSendResult = onSendResult,
            )
        }
    }
}

@Composable
private fun IconInfoListItem(
    iconInfo: IconInfo?,
    showSheet: Boolean,
    onToggleSheet: (Boolean) -> Unit,
    isIconPicker: Boolean = false,
    onSendResult: (IconInfo) -> Unit = {},
) {
    if (iconInfo == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ListItem(
            content = { Text(iconInfo.getFirstLabelAndComponent().label) },
            supportingContent = {
                Text(
                    iconInfo.getFirstLabelAndComponent().component.flattenToString(),
                )
            },
            leadingContent = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .clip(shape = CircleShape)
                        .size(48.dp),
                ) {
                    if (LocalInspectionMode.current) {
                        Icon(
                            iconInfo.fallbackImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.6f),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = iconInfo.drawableId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.6f),
                        )
                    }
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {
                        if (isIconPicker) {
                            onSendResult(iconInfo)
                        } else {
                            onToggleSheet(true)
                        }
                    },
                ),
        )
        AnimatedVisibility(showSheet) {
            IconInfoSheet(
                iconInfo = iconInfo,
            ) {
                onToggleSheet(it)
            }
        }
    }
}

@Composable
private fun SearchModeSelector(
    currentMode: SearchMode,
    onModeChange: (SearchMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
        SearchMode.entries.forEach { mode ->
            FilterChip(
                leadingIcon = {
                    AnimatedVisibility(currentMode == mode) {
                        Icon(
                            imageVector = LawnIcons.Check,
                            contentDescription = null,
                        )
                    }
                },
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                label = {
                    Text(text = stringResource(mode.labelRes()))
                },
            )
        }
    }
}

/** The label of the search mode, shown on its filter chip. */
private fun SearchMode.labelRes(): Int = when (this) {
    SearchMode.LABEL -> R.string.name
    SearchMode.COMPONENT -> R.string.component
    SearchMode.DRAWABLE -> R.string.drawable
}

@PreviewLawnicons
@Composable
private fun SearchBarContentsPreview() {
    PreviewProviders {
        val searchState = rememberSearchState(
            searchBarState = rememberSearchBarState(
                SearchBarValue.Expanded,
            ),
        )

        ResponsiveSearchBarContents(
            isExpandedScreen = false,
            state = searchState.searchBarState,
            inputField = {
                SearchBarInputField(
                    searchState,
                    {
                        Text("Example placeholder")
                    },
                    {},
                )
            },
        ) {
            SearchContents(
                state = searchState,
                iconInfo = SampleData.iconInfoList,
            )
        }
    }
}

@PreviewLawnicons
@Composable
private fun EmptyStatePreview() {
    PreviewProviders {
        EmptyState("Query")
    }
}

@PreviewLawnicons
@Composable
private fun IconGridPreview() {
    PreviewProviders {
        IconGrid(
            SampleData.iconInfoList,
            isIconPicker = false,
            onSendResult = {},
        )
    }
}

@PreviewLawnicons
@Composable
private fun IconInfoListItemPreview() {
    PreviewProviders {
        IconInfoListItem(
            iconInfo = SampleData.iconInfoSample,
            showSheet = false,
            onToggleSheet = {},
            isIconPicker = false,
            onSendResult = {},
        )
    }
}

@PreviewLawnicons
@Composable
private fun SearchModeSelectorPreview() {
    PreviewProviders {
        SearchModeSelector(
            SearchMode.LABEL,
            {},
        )
    }
}
