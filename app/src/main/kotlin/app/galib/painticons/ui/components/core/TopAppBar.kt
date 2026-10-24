package app.galib.painticons.ui.components.core

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.galib.painticons.ui.components.NavigationIconButton
import app.galib.painticons.ui.theme.adaptiveSurfaceColor
import app.galib.painticons.ui.theme.icon.Back
import app.galib.painticons.ui.theme.icon.LawnIcons
import app.galib.painticons.ui.util.PreviewLawnicons
import app.galib.painticons.ui.util.PreviewProviders

@Composable
fun TopAppBar(
    navigationIcon: @Composable () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    modifier: Modifier = Modifier,
    navigationActions: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isExpandedScreen: Boolean = false,
    containerColor: Color = adaptiveSurfaceColor,
) {
    val topAppBarColors = TopAppBarDefaults.topAppBarColors().copy(
        containerColor = containerColor,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )

    if (!isExpandedScreen) {
        LargeTopAppBar(
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    navigationIcon()
                    navigationActions()
                }
            },
            title = {
                Text(title)
            },
            actions = actions,
            colors = topAppBarColors,
        )
    } else {
        TopAppBar(
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    navigationIcon()
                    navigationActions()
                }
            },
            title = {
                Text(title)
            },
            actions = actions,
            colors = topAppBarColors,
        )
    }
}

@PreviewLawnicons
@Composable
private fun SmallTopAppBarPreview() {
    PreviewProviders {
        TopAppBar(
            navigationIcon = {
                NavigationIconButton(
                    label = "Back",
                    onClick = {},
                    imageVector = LawnIcons.Back,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    size = 40.dp,
                )
            },
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            title = "Example",
            isExpandedScreen = true,
        )
    }
}

@PreviewLawnicons
@Composable
private fun LargeTopAppBarPreview() {
    PreviewProviders {
        TopAppBar(
            navigationIcon = {
                NavigationIconButton(
                    label = "Back",
                    onClick = {},
                    imageVector = LawnIcons.Back,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    size = 40.dp,
                )
            },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            title = "Example",
            isExpandedScreen = false,
        )
    }
}
