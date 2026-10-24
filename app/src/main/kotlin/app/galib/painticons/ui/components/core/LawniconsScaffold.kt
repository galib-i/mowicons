package app.galib.painticons.ui.components.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.galib.painticons.R
import app.galib.painticons.ui.components.NavigationIconButton
import app.galib.painticons.ui.components.ShowNavbarScrim
import app.galib.painticons.ui.theme.adaptiveSurfaceColor
import app.galib.painticons.ui.theme.icon.Back
import app.galib.painticons.ui.theme.icon.LawnIcons
import app.galib.painticons.ui.util.PreviewLawnicons
import app.galib.painticons.ui.util.PreviewProviders
import app.galib.painticons.ui.util.toPaddingValues

@Composable
fun LawniconsScaffold(
    title: String,
    onBack: () -> Unit,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navigationActions: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = adaptiveSurfaceColor,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior =
        if (isExpandedScreen) TopAppBarDefaults.pinnedScrollBehavior() else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = title,
                navigationIcon = {
                    NavigationIconButton(
                        label = stringResource(R.string.back),
                        onClick = onBack,
                        imageVector = LawnIcons.Back,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                },
                navigationActions = navigationActions,
                actions = actions,
                isExpandedScreen = isExpandedScreen,
                containerColor = containerColor,
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
    ) {
        Box(
            modifier = Modifier.then(
                if (isExpandedScreen) {
                    Modifier.padding(
                        WindowInsets.navigationBars.toPaddingValues(
                            additionalStart = 32.dp,
                            additionalEnd = 32.dp,
                        ),
                    )
                } else {
                    Modifier
                },
            ),
        ) {
            content(it)
        }
        ShowNavbarScrim(isExpandedScreen = isExpandedScreen)
    }
}

@PreviewLawnicons
@Composable
private fun LawniconsScaffoldPreview() {
    PreviewProviders {
        LawniconsScaffold(
            title = "Example small bar",
            onBack = { },
            isExpandedScreen = false,
            content = {
                Box(
                    modifier = Modifier.padding(it),
                ) {
                    Text("Hello World")
                }
            },
        )
    }
}

@PreviewLawnicons
@Composable
private fun LawniconsScaffoldExpandedPreview() {
    PreviewProviders {
        LawniconsScaffold(
            title = "Example small bar",
            onBack = { },
            isExpandedScreen = true,
            content = {
                Box(
                    modifier = Modifier.padding(it),
                ) {
                    Text("Hello World")
                }
            },
        )
    }
}
