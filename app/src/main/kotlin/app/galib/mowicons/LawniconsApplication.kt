package app.galib.mowicons

import android.app.Application
import android.content.res.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dev.zacsweers.metro.createGraphFactory

class LawniconsApplication :
    Application(),
    ImageLoaderFactory {
    val lawniconsGraph by lazy {
        createGraphFactory<LawniconsGraph.Factory>().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        forceDarkMode()
    }

    /** Mowicons is a dark-only app, the ui mode is forced to night. */
    @Suppress("DEPRECATION")
    private fun forceDarkMode() {
        val configuration = Configuration(resources.configuration)
        if (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES) {
            configuration.uiMode =
                (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) or
                    Configuration.UI_MODE_NIGHT_YES
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
