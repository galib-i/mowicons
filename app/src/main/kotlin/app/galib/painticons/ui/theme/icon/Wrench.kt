package app.galib.painticons.ui.theme.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LawnIcons.Wrench: ImageVector
    get() {
        if (_WrenchIcon != null) {
            return _WrenchIcon!!
        }
        _WrenchIcon = ImageVector.Builder(
            name = "WrenchIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(22.7f, 19f)
                lineTo(13.6f, 9.9f)
                curveTo(14.5f, 7.6f, 14f, 4.9f, 12.1f, 3f)
                curveTo(10.1f, 1f, 7.1f, 0.6f, 4.7f, 1.7f)
                lineTo(9f, 6f)
                lineTo(6f, 9f)
                lineTo(1.6f, 4.7f)
                curveTo(0.4f, 7.1f, 0.9f, 10.1f, 2.9f, 12.1f)
                curveTo(4.8f, 14f, 7.5f, 14.5f, 9.8f, 13.6f)
                lineTo(18.9f, 22.7f)
                curveTo(19.3f, 23.1f, 19.9f, 23.1f, 20.3f, 22.7f)
                lineTo(22.6f, 20.4f)
                curveTo(23.1f, 20f, 23.1f, 19.5f, 22.7f, 19f)
                close()
            }
        }.build()

        return _WrenchIcon!!
    }

@Suppress("ObjectPropertyName")
private var _WrenchIcon: ImageVector? = null
