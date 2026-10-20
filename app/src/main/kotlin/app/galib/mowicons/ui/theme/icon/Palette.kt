package app.galib.mowicons.ui.theme.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LawnIcons.Palette: ImageVector
    get() {
        if (_PaletteIcon != null) {
            return _PaletteIcon!!
        }
        _PaletteIcon = ImageVector.Builder(
            name = "PaletteIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
                curveTo(12.83f, 21f, 13.5f, 20.33f, 13.5f, 19.5f)
                curveTo(13.5f, 19.11f, 13.35f, 18.76f, 13.11f, 18.49f)
                curveTo(12.88f, 18.23f, 12.73f, 17.88f, 12.73f, 17.5f)
                curveTo(12.73f, 16.67f, 13.4f, 16f, 14.23f, 16f)
                lineTo(16f, 16f)
                curveTo(18.76f, 16f, 21f, 13.76f, 21f, 11f)
                curveTo(21f, 6.58f, 16.97f, 3f, 12f, 3f)
                close()
                moveTo(6.5f, 12f)
                curveTo(5.67f, 12f, 5f, 11.33f, 5f, 10.5f)
                curveTo(5f, 9.67f, 5.67f, 9f, 6.5f, 9f)
                curveTo(7.33f, 9f, 8f, 9.67f, 8f, 10.5f)
                curveTo(8f, 11.33f, 7.33f, 12f, 6.5f, 12f)
                close()
                moveTo(9.5f, 8f)
                curveTo(8.67f, 8f, 8f, 7.33f, 8f, 6.5f)
                curveTo(8f, 5.67f, 8.67f, 5f, 9.5f, 5f)
                curveTo(10.33f, 5f, 11f, 5.67f, 11f, 6.5f)
                curveTo(11f, 7.33f, 10.33f, 8f, 9.5f, 8f)
                close()
                moveTo(14.5f, 8f)
                curveTo(13.67f, 8f, 13f, 7.33f, 13f, 6.5f)
                curveTo(13f, 5.67f, 13.67f, 5f, 14.5f, 5f)
                curveTo(15.33f, 5f, 16f, 5.67f, 16f, 6.5f)
                curveTo(16f, 7.33f, 15.33f, 8f, 14.5f, 8f)
                close()
                moveTo(17.5f, 12f)
                curveTo(16.67f, 12f, 16f, 11.33f, 16f, 10.5f)
                curveTo(16f, 9.67f, 16.67f, 9f, 17.5f, 9f)
                curveTo(18.33f, 9f, 19f, 9.67f, 19f, 10.5f)
                curveTo(19f, 11.33f, 18.33f, 12f, 17.5f, 12f)
                close()
            }
        }.build()

        return _PaletteIcon!!
    }

@Suppress("ObjectPropertyName")
private var _PaletteIcon: ImageVector? = null
