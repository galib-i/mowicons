package app.galib.mowicons.ui.theme.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LawnIcons.Settings: ImageVector
    get() {
        if (_SettingsIcon != null) {
            return _SettingsIcon!!
        }
        _SettingsIcon = ImageVector.Builder(
            name = "SettingsIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.64f, 19.2f, 12.33f, 19.2f, 12f)
                curveTo(19.2f, 11.67f, 19.18f, 11.36f, 19.14f, 11.06f)
                lineTo(21.17f, 9.48f)
                curveTo(21.35f, 9.34f, 21.4f, 9.07f, 21.29f, 8.87f)
                lineTo(19.37f, 5.55f)
                curveTo(19.25f, 5.33f, 19f, 5.26f, 18.78f, 5.33f)
                lineTo(16.39f, 6.29f)
                curveTo(15.89f, 5.91f, 15.36f, 5.59f, 14.77f, 5.35f)
                lineTo(14.41f, 2.81f)
                curveTo(14.37f, 2.57f, 14.17f, 2.4f, 13.93f, 2.4f)
                lineTo(10.09f, 2.4f)
                curveTo(9.85f, 2.4f, 9.66f, 2.57f, 9.62f, 2.81f)
                lineTo(9.26f, 5.35f)
                curveTo(8.67f, 5.59f, 8.14f, 5.92f, 7.65f, 6.29f)
                lineTo(5.26f, 5.33f)
                curveTo(5.04f, 5.25f, 4.79f, 5.33f, 4.67f, 5.55f)
                lineTo(2.75f, 8.87f)
                curveTo(2.63f, 9.08f, 2.68f, 9.34f, 2.88f, 9.48f)
                lineTo(4.91f, 11.06f)
                curveTo(4.87f, 11.36f, 4.84f, 11.69f, 4.84f, 12f)
                curveTo(4.84f, 12.31f, 4.86f, 12.64f, 4.91f, 12.94f)
                lineTo(2.88f, 14.52f)
                curveTo(2.7f, 14.66f, 2.65f, 14.93f, 2.76f, 15.13f)
                lineTo(4.68f, 18.45f)
                curveTo(4.8f, 18.67f, 5.05f, 18.74f, 5.27f, 18.67f)
                lineTo(7.66f, 17.71f)
                curveTo(8.16f, 18.09f, 8.69f, 18.41f, 9.28f, 18.65f)
                lineTo(9.64f, 21.19f)
                curveTo(9.69f, 21.43f, 9.88f, 21.6f, 10.12f, 21.6f)
                lineTo(13.96f, 21.6f)
                curveTo(14.2f, 21.6f, 14.4f, 21.43f, 14.44f, 21.19f)
                lineTo(14.8f, 18.65f)
                curveTo(15.39f, 18.41f, 15.92f, 18.09f, 16.41f, 17.71f)
                lineTo(18.8f, 18.67f)
                curveTo(19.02f, 18.75f, 19.27f, 18.67f, 19.39f, 18.45f)
                lineTo(21.31f, 15.13f)
                curveTo(21.43f, 14.91f, 21.38f, 14.66f, 21.18f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.6f)
                curveTo(10.02f, 15.6f, 8.4f, 13.98f, 8.4f, 12f)
                curveTo(8.4f, 10.02f, 10.02f, 8.4f, 12f, 8.4f)
                curveTo(13.98f, 8.4f, 15.6f, 10.02f, 15.6f, 12f)
                curveTo(15.6f, 13.98f, 13.98f, 15.6f, 12f, 15.6f)
                close()
            }
        }.build()

        return _SettingsIcon!!
    }

@Suppress("ObjectPropertyName")
private var _SettingsIcon: ImageVector? = null
