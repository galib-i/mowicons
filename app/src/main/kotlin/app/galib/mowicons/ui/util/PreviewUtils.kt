package app.galib.mowicons.ui.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.data.model.LabelAndComponent
import app.galib.mowicons.data.model.OssLibrary
import app.galib.mowicons.ui.LawniconsActions
import app.galib.mowicons.ui.LocalLawniconsActions
import app.galib.mowicons.ui.theme.LawniconsTheme

@Preview(
    name = "Normal",
    showBackground = true,
    wallpaper = Wallpapers.NONE,
)
@Preview(
    name = "Normal Night",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.NONE,
)
@Preview(
    name = "Themed",
    showBackground = true,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
)
@Preview(
    name = "Themed Night",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
)
annotation class PreviewLawnicons

@Composable
fun PreviewProviders(content: @Composable () -> Unit) {
    LawniconsTheme {
        CompositionLocalProvider(
            LocalLawniconsActions provides LawniconsActions(false, {}),
        ) {
            content()
        }
    }
}

object SampleData {
    val iconInfoSample = IconInfo(
        drawableName = "@drawable/email",
        componentNames = listOf(
            LabelAndComponent("Email", "com.android.email/.ExampleActivity"),
        ),
        drawableId = 1,
    )

    val iconInfoList = listOf(
        IconInfo(
            drawableName = "@drawable/email",
            componentNames = listOf(
                LabelAndComponent("Email", "com.android.email/.ExampleActivity"),
            ),
            drawableId = 1,
        ),
        IconInfo(
            drawableName = "@drawable/search",
            componentNames = listOf(
                LabelAndComponent("Search", "com.android.search/.ExampleActivity"),
            ),
            drawableId = 2,
        ),
        IconInfo(
            drawableName = "@drawable/phone",
            componentNames = listOf(
                LabelAndComponent("Phone", "com.android.phone/.ExampleActivity"),
            ),
            drawableId = 3,
        ),
    )

    val ossLibraries = listOf(
        OssLibrary(
            groupId = "group-1",
            artifactId = "example-library",
            name = "Example Library",
            version = "1",
            scm = OssLibrary.Scm(
                url = "https://example.com",
            ),
            spdxLicenses = listOf(
                OssLibrary.License(
                    name = "Example License",
                ),
            ),
        ),
        OssLibrary(
            groupId = "group-2",
            artifactId = "example-library-2",
            name = "Example Library 2",
            version = "2",
            scm = OssLibrary.Scm(
                url = "https://example.com",
            ),
            spdxLicenses = listOf(
                OssLibrary.License(
                    name = "Example License",
                ),
            ),
        ),
    )
}
