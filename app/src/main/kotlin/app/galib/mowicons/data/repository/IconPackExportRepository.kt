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

package app.galib.mowicons.data.repository

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.withClip
import androidx.graphics.shapes.toPath
import app.galib.mowicons.R
import app.galib.mowicons.data.export.AxmlWriter
import app.galib.mowicons.data.model.IconInfo
import app.galib.mowicons.data.model.IconShape
import app.galib.mowicons.data.model.InstalledApp
import app.galib.mowicons.data.model.PaintedIconSettings
import com.android.apksig.ApkSigner
import com.android.apksig.KeyConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Builds the painted icons into an icon pack and saves it as a plain file.
 */
interface IconPackExportRepository {

    /**
     * Builds an icon pack with the painted icons of [apps] and saves it as an
     * APK file, which can then be installed and used as an icon pack.
     *
     * @param selectedColors The colours picked by the user, keyed by package name.
     * @param settings The settings which decide how the painted icons look.
     * @return The location of the saved icon pack.
     */
    suspend fun export(
        apps: List<InstalledApp>,
        selectedColors: Map<String, Int>,
        settings: PaintedIconSettings,
    ): String
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class IconPackExportRepositoryImpl(
    private val application: Application,
) : IconPackExportRepository {

    override suspend fun export(
        apps: List<InstalledApp>,
        selectedColors: Map<String, Int>,
        settings: PaintedIconSettings,
    ): String = withContext(Dispatchers.IO) {
        // The pack itself is themed with its own icon, so it is left out of the pack.
        val coveredApps = coveredApps(apps)
        check(coveredApps.isNotEmpty()) { "There are no covered apps to export" }

        val template = File.createTempFile("iconpack", ".apk", application.cacheDir)
        val unsigned = File.createTempFile("iconpack-unsigned", ".apk", application.cacheDir)
        val signed = File.createTempFile("iconpack-signed", ".apk", application.cacheDir)
        try {
            application.assets.open(TEMPLATE_ASSET).use { input ->
                template.outputStream().use { input.copyTo(it) }
            }

            val defaultColor = ContextCompat.getColor(application, R.color.primaryForeground)
            val drawableNames = coveredApps.mapIndexed { index, _ -> "icon_%04d".format(index + 1) }
            val drawables = HashMap<String, ByteArray>(coveredApps.size)
            coveredApps.forEachIndexed { index, app ->
                val iconInfo = requireNotNull(app.iconInfo)
                val color = settings.foregroundColorFor(
                    selectedColors[app.packageName],
                    app.colors.firstOrNull(),
                ) ?: defaultColor
                drawables[drawableNames[index]] = renderIcon(
                    iconInfo,
                    color,
                    settings.backgroundColor,
                    settings.shape,
                )
            }
            val appfilter = buildAppfilter(coveredApps, drawableNames)
            val plainTextAppfilter = buildPlainTextAppfilter(coveredApps, drawableNames)

            // Put the painted icons and the appfilter into the template.
            ZipFile(template).use { templateZip ->
                val countingOut = CountingOutputStream(unsigned.outputStream())
                ZipOutputStream(countingOut).use { output ->
                    val entries = templateZip.entries()
                    var hasAssetsAppfilter = false
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name.startsWith("META-INF/")) continue
                        if (entry.name == ASSETS_APPFILTER_PATH) hasAssetsAppfilter = true
                        val drawable = drawables[entry.name.substringAfterLast('/').substringBeforeLast('.')]
                        val bytes = when {
                            entry.name == APPFILTER_PATH -> appfilter
                            entry.name == ASSETS_APPFILTER_PATH -> plainTextAppfilter
                            drawable != null && entry.name.startsWith("res/drawable") -> drawable
                            else -> templateZip.getInputStream(entry).use { it.readBytes() }
                        }
                        val newEntry = ZipEntry(entry.name)
                        if (entry.method == ZipEntry.STORED || entry.name == "resources.arsc") {
                            newEntry.method = ZipEntry.STORED
                            newEntry.size = bytes.size.toLong()
                            newEntry.compressedSize = bytes.size.toLong()
                            val crc = CRC32().apply { update(bytes) }
                            newEntry.crc = crc.value
                            alignEntry(newEntry, countingOut.count, 4)
                        } else {
                            newEntry.method = ZipEntry.DEFLATED
                        }
                        output.putNextEntry(newEntry)
                        output.write(bytes)
                        output.closeEntry()
                    }
                    if (!hasAssetsAppfilter) {
                        val newEntry = ZipEntry(ASSETS_APPFILTER_PATH).apply {
                            method = ZipEntry.DEFLATED
                        }
                        output.putNextEntry(newEntry)
                        output.write(plainTextAppfilter)
                        output.closeEntry()
                    }
                }
            }

            sign(unsigned, signed)
            save(signed)
        } finally {
            template.delete()
            unsigned.delete()
            signed.delete()
        }
    }

    /**
     * The apps which are covered by the pack, i.e. the ones which get a painted icon.
     * The pack itself is left out, as it is themed with its own icon.
     */
    internal fun coveredApps(apps: List<InstalledApp>): List<InstalledApp> = apps
        .filter { it.iconInfo != null }
        .filterNot { it.packageName == PACK_PACKAGE }
        .take(MAX_ICONS)

    /** Renders the icon of the pack as the app shows it: the tinted glyph on the shape. */
    private fun renderIcon(
        iconInfo: IconInfo,
        color: Int,
        backgroundColor: Int,
        shape: IconShape,
    ): ByteArray {
        val bitmap = createBitmap(ICON_SIZE, ICON_SIZE)
        val canvas = Canvas(bitmap)
        val path = shape.polygon.toPath()
        // The polygons are normalized to the unit square, so they have to be scaled up.
        path.transform(Matrix().apply { setScale(ICON_SIZE.toFloat(), ICON_SIZE.toFloat()) })
        canvas.drawPath(path, Paint().apply { this.color = backgroundColor; isAntiAlias = true })

        val drawable = ResourcesCompat.getDrawable(application.resources, iconInfo.drawableId, null)
            ?: throw IOException("Could not load the icon ${iconInfo.drawableName}")
        val inset = (ICON_SIZE * 0.2f).toInt()
        drawable.setBounds(inset, inset, ICON_SIZE - inset, ICON_SIZE - inset)
        DrawableCompat.setTint(drawable, color)
        // The glyph is clipped to the shape, just like it is in the app.
        canvas.withClip(path) {
            drawable.draw(this)
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        bitmap.recycle()
        return output.toByteArray()
    }

    /** Builds the binary appfilter which maps the apps to the icons of the pack. */
    internal fun buildAppfilter(apps: List<InstalledApp>, drawableNames: List<String>): ByteArray {
        val items = apps.mapIndexed { index, app ->
            AxmlWriter.Element(
                name = "item",
                attributes = listOf(
                    "component" to "ComponentInfo{${app.packageName}/${app.className}}",
                    "drawable" to drawableNames[index],
                    "name" to app.label,
                ),
            )
        } + AxmlWriter.Element(name = "item", attributes = packItemAttributes)
        return AxmlWriter.build(AxmlWriter.Element(name = "resources", children = items))
    }

    /**
     * The attributes of the item which maps the icon pack itself to its own icon, so that
     * the launcher themes the icon of the pack as well.
     */
    private val packItemAttributes = listOf(
        "component" to "ComponentInfo{$PACK_PACKAGE/$PACK_ACTIVITY}",
        "drawable" to PACK_DRAWABLE,
        "name" to PACK_LABEL,
    )

    /** Builds the plain text appfilter for assets. */
    internal fun buildPlainTextAppfilter(apps: List<InstalledApp>, drawableNames: List<String>): ByteArray {
        val sb = StringBuilder("<resources>\n")
        apps.forEachIndexed { index, app ->
            val escapedLabel = app.label
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
            sb.append("    <item component=\"ComponentInfo{${app.packageName}/${app.className}}\" drawable=\"${drawableNames[index]}\" name=\"$escapedLabel\" />\n")
        }
        val packItem = packItemAttributes.joinToString(" ") { (name, value) -> "$name=\"$value\"" }
        sb.append("    <item $packItem />\n")
        sb.append("</resources>")
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    /** Signs the APK with the key of the pack, so that it can be installed. */
    private fun sign(input: File, output: File) {
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(
            PKCS8EncodedKeySpec(application.assets.open(EXPORT_KEY_ASSET).readBytes()),
        )
        val certificate = CertificateFactory.getInstance("X.509").generateCertificate(
            application.assets.open(EXPORT_CERT_ASSET),
        ) as X509Certificate
        val signerConfig = ApkSigner.SignerConfig.Builder(
            SIGNER_NAME,
            KeyConfig.Jca(privateKey),
            listOf(certificate),
        ).build()
        ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(input)
            .setOutputApk(output)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setMinSdkVersion(MIN_SDK)
            .build()
            .sign()
    }

    /** Saves the APK as a plain file and returns its location. */
    private fun save(apk: File): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, APK_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = application.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Could not create $FILE_NAME")
            try {
                resolver.openOutputStream(uri)?.use { output ->
                    apk.inputStream().use { it.copyTo(output) }
                } ?: throw IOException("Could not write $FILE_NAME")
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return FILE_NAME
        }

        // The downloads folder isn't writable without a permission on older
        // versions, so the file is saved in the app specific storage instead.
        val directory = application.getExternalFilesDir(null) ?: application.filesDir
        val file = File(directory, FILE_NAME)
        apk.copyTo(file, overwrite = true)
        return file.absolutePath
    }

    private companion object {
        /** The compiled template of the icon pack, built by the app module. */
        const val TEMPLATE_ASSET = "iconpack-template.apk"

        /** The path of the appfilter inside the template. */
        const val APPFILTER_PATH = "res/xml/appfilter.xml"
        const val ASSETS_APPFILTER_PATH = "assets/appfilter.xml"

        /** The key with which the exported packs are signed. */
        const val EXPORT_KEY_ASSET = "export_key.der"
        const val EXPORT_CERT_ASSET = "export_cert.der"
        const val SIGNER_NAME = "PaintedIcons"
        const val MIN_SDK = 26

        /** The package of the exported pack. Keep in sync with the pack template. */
        const val PACK_PACKAGE = "app.galib.paintedicons.pack"

        /**
         * The launcher activity of the exported pack. The pack contains no code, so the
         * activity is the framework activity. Keep in sync with the pack template.
         */
        const val PACK_ACTIVITY = "android.app.Activity"

        /** The drawable of the pack which is used for the icon of the pack itself. */
        const val PACK_DRAWABLE = "infinite_painter"

        /** The label of the exported pack. Keep in sync with the pack template. */
        const val PACK_LABEL = "Painted Icons"

        const val FILE_NAME = "Painted Icons.apk"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"

        /** The side length of the icons of the pack, in pixels (xxxhdpi). */
        const val ICON_SIZE = 192

        /** The number of icons the pack can hold. Keep in sync with iconPackSlots in the build script. */
        const val MAX_ICONS = 512
    }
}

private class CountingOutputStream(private val out: OutputStream) : OutputStream() {
    var count: Long = 0L
        private set

    override fun write(b: Int) {
        out.write(b)
        count++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        count += len
    }

    override fun flush() {
        out.flush()
    }

    override fun close() {
        out.close()
    }
}

private fun alignEntry(entry: ZipEntry, currentOffset: Long, alignment: Int = 4) {
    val nameLen = entry.name.toByteArray(Charsets.UTF_8).size
    val extraLen = entry.extra?.size ?: 0
    val headerSize = 30 + nameLen + extraLen
    val targetOffset = currentOffset + headerSize
    val remainder = (targetOffset % alignment).toInt()
    if (remainder != 0) {
        val padding = alignment - remainder
        val newExtra = ByteArray(4 + padding)
        newExtra[0] = 0xda.toByte()
        newExtra[1] = 0xda.toByte()
        newExtra[2] = (padding and 0xff).toByte()
        newExtra[3] = ((padding shr 8) and 0xff).toByte()
        entry.extra = newExtra
    }
}
