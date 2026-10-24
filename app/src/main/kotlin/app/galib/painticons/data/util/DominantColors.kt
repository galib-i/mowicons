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

package app.galib.painticons.data.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Finds the most common colours of the given bitmap.
 *
 * Shades of grey are left out, since app icons often have a white to black gradient
 * as their background, which doesn't represent the icon itself. Colours which are close
 * to each other are merged into one, so that the result holds actually different colours.
 *
 * @param limit The maximum amount of colours to return.
 * @return The most common colours of the bitmap, the most common one first.
 */
fun Bitmap.dominantColors(limit: Int = 3): List<Int> {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)

    val buckets = mutableMapOf<Int, ColorBucket>()
    pixels.forEach { pixel ->
        val alpha = pixel ushr 24 and 0xFF
        if (alpha < MIN_ALPHA) return@forEach

        val red = pixel shr 16 and 0xFF
        val green = pixel shr 8 and 0xFF
        val blue = pixel and 0xFF
        if (isGreyscale(red, green, blue)) return@forEach

        buckets.getOrPut(quantize(red, green, blue)) { ColorBucket() }
            .add(red, green, blue)
    }

    return mergeSimilar(buckets.values)
        .sortedByDescending { it.count }
        .take(limit)
        .map { it.color }
}

/**
 * Merges the given buckets into groups of similar colours, keeping the most common
 * colour of each group as its representative.
 */
private fun mergeSimilar(buckets: Collection<ColorBucket>): List<ColorBucket> {
    val groups = mutableListOf<ColorBucket>()
    buckets.sortedByDescending { it.count }.forEach { bucket ->
        val group = groups.firstOrNull { it.isSimilarTo(bucket) }
        if (group == null) groups.add(bucket) else group.add(bucket)
    }
    return groups
}

/** Groups the given colour into one of [QUANTIZATION] levels per channel. */
private fun quantize(red: Int, green: Int, blue: Int): Int =
    ((red / QUANTIZATION) shl 8) or ((green / QUANTIZATION) shl 4) or (blue / QUANTIZATION)

/** Whether the given colour is a shade of grey, i.e. part of a white to black gradient. */
private fun isGreyscale(red: Int, green: Int, blue: Int): Boolean =
    abs(red - green) <= GREYSCALE_TOLERANCE && abs(green - blue) <= GREYSCALE_TOLERANCE

/**
 * Whether the two given colours are close enough to be considered the same colour.
 *
 * Colours of a similar hue are the same, everything else has to be close by distance.
 */
private fun isSimilarColor(first: Int, second: Int): Boolean {
    val firstSaturation = saturation(first)
    val secondSaturation = saturation(second)
    return if (firstSaturation >= MIN_SATURATION && secondSaturation >= MIN_SATURATION) {
        hueDifference(hue(first), hue(second)) <= HUE_TOLERANCE
    } else {
        distance(first, second) <= MIN_DISTANCE
    }
}

/** The hue of the given colour, in degrees. */
private fun hue(color: Int): Float {
    val red = (color shr 16 and 0xFF) / 255f
    val green = (color shr 8 and 0xFF) / 255f
    val blue = (color and 0xFF) / 255f
    val max = max(red, max(green, blue))
    val delta = max - min(red, min(green, blue))
    if (delta == 0f) return 0f

    val hue = when (max) {
        red -> 60f * (((green - blue) / delta) % 6f)
        green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    return (hue + 360f) % 360f
}

/** The saturation of the given colour, from 0 to 1. */
private fun saturation(color: Int): Float {
    val red = (color shr 16 and 0xFF) / 255f
    val green = (color shr 8 and 0xFF) / 255f
    val blue = (color and 0xFF) / 255f
    val max = max(red, max(green, blue))
    if (max == 0f) return 0f
    return (max - min(red, min(green, blue))) / max
}

/** The difference between the two given hues, in degrees. */
private fun hueDifference(first: Float, second: Float): Float {
    val difference = abs(first - second)
    return if (difference > 180f) 360f - difference else difference
}

/** The distance between the two given colours. */
private fun distance(first: Int, second: Int): Float {
    val red = (first shr 16 and 0xFF) - (second shr 16 and 0xFF)
    val green = (first shr 8 and 0xFF) - (second shr 8 and 0xFF)
    val blue = (first and 0xFF) - (second and 0xFF)
    return sqrt((red * red + green * green + blue * blue).toFloat())
}

/** A group of pixels which share the same colour. */
private class ColorBucket {
    var count = 0
        private set
    private var red = 0
    private var green = 0
    private var blue = 0

    /** The average colour of the pixels in this bucket. */
    val color: Int
        get() = Color.argb(255, red / count, green / count, blue / count)

    fun add(red: Int, green: Int, blue: Int) {
        count++
        this.red += red
        this.green += green
        this.blue += blue
    }

    fun add(bucket: ColorBucket) {
        count += bucket.count
        red += bucket.red
        green += bucket.green
        blue += bucket.blue
    }

    fun isSimilarTo(bucket: ColorBucket): Boolean = isSimilarColor(color, bucket.color)
}

/** The minimum opacity a pixel needs to be taken into account. */
private const val MIN_ALPHA = 128

/** The amount of levels each colour channel is grouped into. */
private const val QUANTIZATION = 16

/** The maximum difference between two channels of a shade of grey. */
private const val GREYSCALE_TOLERANCE = 12

/** The maximum difference between the hues of two colours of the same kind. */
private const val HUE_TOLERANCE = 15f

/** The minimum saturation a colour needs to be compared by its hue. */
private const val MIN_SATURATION = 0.25f

/** The maximum distance between two colours which aren't compared by their hue. */
private const val MIN_DISTANCE = 60f
