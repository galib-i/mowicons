package app.galib.mowicons.ui.components.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.galib.mowicons.R
import app.galib.mowicons.ui.theme.icon.About
import app.galib.mowicons.ui.theme.icon.Github
import app.galib.mowicons.ui.theme.icon.LawnIcons
import app.galib.mowicons.ui.theme.icon.PaintBrush
import app.galib.mowicons.ui.theme.icon.Search
import app.galib.mowicons.ui.util.Constants
import app.galib.mowicons.ui.util.PreviewLawnicons
import app.galib.mowicons.ui.util.PreviewProviders
import app.galib.mowicons.ui.util.visitUrl

private data class ToolbarItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.HomeBottomBar(
    onNavigateToApps: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onExpandSearch: () -> Unit,
    scrollBehavior: FloatingToolbarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val toolbarItems = listOf(
        ToolbarItem(
            icon = LawnIcons.PaintBrush,
            label = stringResource(R.string.apps),
            onClick = onNavigateToApps,
        ),
        ToolbarItem(
            icon = LawnIcons.Github,
            label = stringResource(R.string.github),
            onClick = { context.visitUrl(Constants.GITHUB) },
        ),
        ToolbarItem(
            icon = LawnIcons.About,
            label = stringResource(R.string.about),
            onClick = onNavigateToAbout,
        ),
    )

    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        floatingActionButton = {
            SimpleTooltipBox(
                label = stringResource(id = R.string.search),
            ) {
                FloatingToolbarDefaults.StandardFloatingActionButton(
                    onClick = onExpandSearch,
                ) {
                    Icon(
                        imageVector = LawnIcons.Search,
                        contentDescription = stringResource(id = R.string.search),
                    )
                }
            }
        },
        content = {
            toolbarItems.forEach { item ->
                SimpleTooltipBox(
                    label = item.label,
                ) {
                    IconButton(onClick = item.onClick) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.requiredSize(24.dp),
                        )
                    }
                }
            }
        },
        modifier = modifier
            .align(Alignment.BottomCenter)
            .offset(y = -ScreenOffset)
            .navigationBarsPadding(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTooltipBox(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit),
) {
    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
        tooltip = {
            PlainTooltip {
                Text(label)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        content()
    }
}

@PreviewLawnicons
@Composable
private fun SimpleTooltipBoxPreview() {
    PreviewProviders {
        SimpleTooltipBox(
            label = "Example",
        ) {
            Icon(
                LawnIcons.About,
                contentDescription = null,
            )
        }
    }
}

@PreviewLawnicons
@Composable
private fun HomeBottomBarPreview() {
    PreviewProviders {
        Box {
            HomeBottomBar(
                onNavigateToApps = {},
                onNavigateToAbout = {},
                onExpandSearch = {},
                scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
                    FloatingToolbarExitDirection.Bottom,
                ),
            )
        }
    }
}
