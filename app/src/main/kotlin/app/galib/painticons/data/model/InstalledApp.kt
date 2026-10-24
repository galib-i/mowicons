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

package app.galib.painticons.data.model

/**
 * An app installed on the device.
 *
 * @property label The user facing name of the app.
 * @property packageName The package name of the app.
 * @property className The name of the launch activity of the app.
 * @property colors The most common colours of the system icon, used to tint the icon of
 * the pack. The first one is the default colour of the icon.
 * @property iconInfo The icon of the icon pack for the app, if the pack covers it.
 */
data class InstalledApp(
    val label: String,
    val packageName: String,
    val className: String,
    val colors: List<Int> = emptyList(),
    val iconInfo: IconInfo? = null,
)
