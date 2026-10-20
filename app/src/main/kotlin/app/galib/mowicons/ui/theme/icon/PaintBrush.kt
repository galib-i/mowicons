package app.galib.mowicons.ui.theme.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LawnIcons.PaintBrush: ImageVector
    get() {
        if (_PaintBrushIcon != null) {
            return _PaintBrushIcon!!
        }
        _PaintBrushIcon = ImageVector.Builder(
            name = "PaintBrushIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 14f)
                curveTo(5.34f, 14f, 4f, 15.34f, 4f, 17f)
                curveTo(4f, 18.31f, 2.84f, 19f, 2f, 19f)
                curveTo(2.92f, 20.22f, 4.49f, 21f, 6f, 21f)
                curveTo(8.21f, 21f, 10f, 19.21f, 10f, 17f)
                curveTo(10f, 15.34f, 8.66f, 14f, 7f, 14f)
                close()
                moveTo(20.71f, 4.63f)
                lineTo(19.37f, 3.29f)
                curveTo(18.98f, 2.9f, 18.35f, 2.9f, 17.96f, 3.29f)
                lineTo(9f, 12.25f)
                lineTo(11.75f, 15f)
                lineTo(20.71f, 6.04f)
                curveTo(21.1f, 5.65f, 21.1f, 5.02f, 20.71f, 4.63f)
                close()
            }
        }.build()

        return _PaintBrushIcon!!
    }

@Suppress("ObjectPropertyName")
private var _PaintBrushIcon: ImageVector? = null
