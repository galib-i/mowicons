package app.galib.painticons.data.repository

import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.core.content.edit
import app.galib.painticons.data.model.IconShape
import app.galib.painticons.data.model.PaintedIconSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Backend-agnostic preference storage used by [BasePreferenceManager].
 */
interface PreferenceStore {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)

    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)

    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
}

class SharedPreferencesStore(
    private val prefs: SharedPreferences,
) : PreferenceStore {
    override fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getInt(key: String, defaultValue: Int) = prefs.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    override fun getString(key: String, defaultValue: String) =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
}

class InMemoryPreferenceStore : PreferenceStore {
    private val values = mutableMapOf<String, Any>()

    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int) = values[key] as? Int ?: defaultValue

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override fun getString(key: String, defaultValue: String) = values[key] as? String ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }
}

abstract class BasePreferenceManager(
    private val store: PreferenceStore,
) {
    /**
     * A class that represents a boolean preference
     * @param key The key of the preference
     * @param defaultValue The default value of the preference
     */
    @Stable
    inner class BoolPref(
        val key: String,
        private val defaultValue: Boolean,
    ) {
        fun get() = store.getBoolean(key, defaultValue)
        fun set(value: Boolean) = store.putBoolean(key, value)

        fun toggle() = set(!get())
    }

    /**
     * A class that represents an integer preference
     * @param key The key of the preference
     * @param defaultValue The default value of the preference
     */
    @Stable
    inner class IntPref(
        val key: String,
        private val defaultValue: Int,
    ) {
        fun get() = store.getInt(key, defaultValue)
        fun set(value: Int) = store.putInt(key, value)
    }

    /**
     * A class that represents a string preference
     * @param key The key of the preference
     * @param defaultValue The default value of the preference
     */
    @Stable
    inner class StringPref(
        val key: String,
        private val defaultValue: String,
    ) {
        fun get() = store.getString(key, defaultValue)
        fun set(value: String) = store.putString(key, value)
    }
}

@Inject
@SingleIn(AppScope::class)
class PreferenceManager(
    store: PreferenceStore,
) : BasePreferenceManager(store) {
    /**
     * The foreground colours picked by the user for the icons of the apps, encoded as a JSON
     * object which maps the package name of the app to the colour.
     */
    val selectedIconColors = StringPref("selected_icon_colors", "")

    /**
     * The icons picked by the user for the apps, encoded as a JSON object which maps the
     * package name of the app to the name of the drawable of the icon.
     */
    val selectedIconNames = StringPref("selected_icon_names", "")

    /**
     * The name of the shape picked by the user for the painted icons.
     */
    val selectedIconShape = StringPref("selected_icon_shape", IconShape.Default.name)

    /**
     * The background colour of the painted icons, stored as an ARGB colour integer.
     */
    val paintedBackgroundColor = IntPref(
        "painted_background_color",
        PaintedIconSettings.DefaultBackgroundColor,
    )

    /**
     * Whether the foreground colour of the painted icons is taken from the original icon.
     */
    val paintedUseOriginalForeground = BoolPref("painted_use_original_foreground", true)

    /**
     * The hex foreground colour of the painted icons, stored as an ARGB colour integer.
     */
    val paintedForegroundColor = IntPref(
        "painted_foreground_color",
        PaintedIconSettings.DefaultForegroundColor,
    )
}
