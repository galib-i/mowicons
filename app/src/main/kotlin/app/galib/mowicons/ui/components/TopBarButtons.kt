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

package app.galib.mowicons.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.galib.mowicons.R
import app.galib.mowicons.ui.theme.icon.About
import app.galib.mowicons.ui.theme.icon.Github
import app.galib.mowicons.ui.theme.icon.LawnIcons
import app.galib.mowicons.ui.theme.icon.Search
import app.galib.mowicons.ui.util.Constants
import app.galib.mowicons.ui.util.PreviewLawnicons
import app.galib.mowicons.ui.util.PreviewProviders
import app.galib.mowicons.ui.util.visitUrl

/**
 * The GitHub, About and search buttons, shown next to each other in the top bar.
 */
@Composable
fun TopBarButtons(
    onNavigateToAbout: () -> Unit,
    onExpandSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { context.visitUrl(Constants.GITHUB) }) {
            Icon(
                imageVector = LawnIcons.Github,
                contentDescription = stringResource(R.string.github),
                modifier = Modifier.requiredSize(24.dp),
            )
        }
        IconButton(onClick = onNavigateToAbout) {
            Icon(
                imageVector = LawnIcons.About,
                contentDescription = stringResource(R.string.about),
            )
        }
        IconButton(onClick = onExpandSearch) {
            Icon(
                imageVector = LawnIcons.Search,
                contentDescription = stringResource(R.string.search),
            )
        }
    }
}

@PreviewLawnicons
@Composable
private fun TopBarButtonsPreview() {
    PreviewProviders {
        TopBarButtons(
            onNavigateToAbout = {},
            onExpandSearch = {},
        )
    }
}
