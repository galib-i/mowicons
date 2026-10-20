package app.galib.mowicons.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.ui.destination.about.About
import app.galib.mowicons.ui.destination.about.aboutDestination
import app.galib.mowicons.ui.destination.acknowledgements.Acknowledgements
import app.galib.mowicons.ui.destination.acknowledgements.acknowledgementsDestination
import app.galib.mowicons.ui.destination.apps.Apps
import app.galib.mowicons.ui.destination.apps.appsDestination
import app.galib.mowicons.ui.destination.contributors.Contributors
import app.galib.mowicons.ui.destination.contributors.contributorsDestination
import app.galib.mowicons.ui.destination.debugmenu.debugMenuDestination
import app.galib.mowicons.ui.destination.home.Home
import app.galib.mowicons.ui.destination.home.homeDestination
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

data class LawniconsActions(
    val isIconPicker: Boolean = false,
    val onSendResult: (IconInfo) -> Unit = {},
)

val LocalLawniconsActions = staticCompositionLocalOf { LawniconsActions() }

@Composable
fun Lawnicons(
    metroVmf: MetroViewModelFactory,
    isExpandedScreen: Boolean,
    onSendResult: (IconInfo) -> Unit,
    modifier: Modifier = Modifier,
    isIconPicker: Boolean = false,
) {
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides metroVmf,
        LocalLawniconsActions provides LawniconsActions(isIconPicker, onSendResult),
    ) {
        val navigationState = rememberNavigationState(
            startRoute = Home,
            topLevelRoutes = setOf(Home, Apps),
        )
        val navigator = remember { Navigator(navigationState) }
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val slideDistance = rememberSlideDistance()

        val entryProvider = entryProvider {
            homeDestination(
                onNavigateToAbout = { navigator.navigate(About) },
                onNavigateToApps = { navigator.navigate(Apps) },
                isExpandedScreen = isExpandedScreen,
            )
            debugMenuDestination(
                isExpandedScreen = isExpandedScreen,
                onBack = navigator::goBack,
            )
            aboutDestination(
                onBack = navigator::goBack,
                onNavigateToContributors = { navigator.navigate(Contributors) },
                onNavigateToAcknowledgements = { navigator.navigate(Acknowledgements) },
                isExpandedScreen = isExpandedScreen,
            )
            contributorsDestination(
                isExpandedScreen = isExpandedScreen,
                onBack = navigator::goBack,
            )
            acknowledgementsDestination(
                isExpandedScreen = isExpandedScreen,
                onBack = navigator::goBack,
            )
            appsDestination(
                isExpandedScreen = isExpandedScreen,
                onBack = navigator::goBack,
            )
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = navigator::goBack,
                transitionSpec = { materialSharedAxisX(!isRtl, slideDistance) },
                popTransitionSpec = { materialSharedAxisX(isRtl, slideDistance) },
                predictivePopTransitionSpec = { materialSharedAxisX(isRtl, slideDistance) },
            )
        }
    }
}
