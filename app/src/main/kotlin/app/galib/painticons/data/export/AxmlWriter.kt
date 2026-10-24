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

package app.galib.painticons.data.export

import java.io.ByteArrayOutputStream

/**
 * Writes a binary XML document, the format in which Android stores XML
 * resources inside an APK.
 *
 * Only what the appfilter of the icon pack needs is supported: a single root
 * element whose descendants have plain (non-namespaced) string attributes.
 */
internal object AxmlWriter {

    private const val TYPE_STRING_POOL = 0x0001
    private const val TYPE_XML = 0x0003
    private const val TYPE_START_ELEMENT = 0x0102
    private const val TYPE_END_ELEMENT = 0x0103
    private const val TYPE_STRING = 0x03
    private const val UTF8_FLAG = 0x100

    /** A XML element with plain string attributes. */
    data class Element(
        val name: String,
        val attributes: List<Pair<String, String>> = emptyList(),
        val children: List<Element> = emptyList(),
    )

    /** Builds the binary XML document with [root] as the root element. */
    fun build(root: Element): ByteArray {
        val pool = StringPool()
        val elements = ByteArrayOutputStream().apply {
            writeElement(root, pool, depth = 1)
        }.toByteArray()

        val poolBytes = pool.toByteArray()
        return ByteArrayOutputStream(8 + poolBytes.size + elements.size).apply {
            writeChunkHeader(TYPE_XML, headerSize = 8, size = 8 + poolBytes.size + elements.size)
            write(poolBytes)
            write(elements)
        }.toByteArray()
    }

    private fun ByteArrayOutputStream.writeElement(element: Element, pool: StringPool, depth: Int) {
        val name = pool.index(element.name)

        writeChunkHeader(
            TYPE_START_ELEMENT,
            headerSize = 0x10,
            size = 0x10 + 0x14 * (element.attributes.size + 1),
        )
        writeInt(depth) // line number
        writeInt(-1) // comment
        writeInt(-1) // namespace: none
        writeInt(name)
        writeShort(0x14) // attribute start, right after the attribute extension
        writeShort(0x14) // attribute size
        writeShort(element.attributes.size)
        writeShort(0) // id index
        writeShort(0) // class index
        writeShort(0) // style index
        element.attributes.forEach { (attributeName, value) ->
            val valueIndex = pool.index(value)
            writeInt(-1) // namespace: none
            writeInt(pool.index(attributeName))
            writeInt(valueIndex) // raw value
            writeShort(8) // size of the typed value
            write(0) // reserved
            write(TYPE_STRING) // data type: string
            writeInt(valueIndex) // data of the typed value
        }

        element.children.forEach { writeElement(it, pool, depth + 1) }

        writeChunkHeader(TYPE_END_ELEMENT, headerSize = 0x10, size = 0x18)
        writeInt(depth)
        writeInt(-1) // comment
        writeInt(-1) // namespace: none
        writeInt(name)
    }

    /** The strings of a document, which are shared between all chunks. */
    private class StringPool {
        private val strings = mutableListOf<String>()
        private val indices = mutableMapOf<String, Int>()

        /** The index of [string] in the pool, adding it if it isn't known yet. */
        fun index(string: String): Int = indices.getOrPut(string) {
            strings.add(string)
            strings.size - 1
        }

        fun toByteArray(): ByteArray {
            val data = ByteArrayOutputStream()
            val offsets = IntArray(strings.size)
            strings.forEachIndexed { index, string ->
                offsets[index] = data.size()
                val bytes = string.toByteArray(Charsets.UTF_8)
                data.writeLength(string.length)
                data.writeLength(bytes.size)
                data.write(bytes)
                data.write(0) // null terminator
            }

            val stringsStart = 0x1C + strings.size * 4
            val padding = (4 - data.size() % 4) % 4
            return ByteArrayOutputStream(stringsStart + data.size() + padding).apply {
                writeChunkHeader(TYPE_STRING_POOL, headerSize = 0x1C, size = stringsStart + data.size() + padding)
                writeInt(strings.size)
                writeInt(0) // style count
                writeInt(UTF8_FLAG)
                writeInt(stringsStart)
                writeInt(0) // styles start
                offsets.forEach { writeInt(it) }
                write(data.toByteArray())
                repeat(padding) { write(0) }
            }.toByteArray()
        }
    }

    private fun ByteArrayOutputStream.writeChunkHeader(type: Int, headerSize: Int, size: Int) {
        writeShort(type)
        writeShort(headerSize)
        writeInt(size)
    }

    private fun ByteArrayOutputStream.writeInt(value: Int) {
        write(value and 0xFF)
        write((value ushr 8) and 0xFF)
        write((value ushr 16) and 0xFF)
        write((value ushr 24) and 0xFF)
    }

    private fun ByteArrayOutputStream.writeShort(value: Int) {
        write(value and 0xFF)
        write((value ushr 8) and 0xFF)
    }

    /** Writes a length in the one or two byte encoding of UTF-8 string pools. */
    private fun ByteArrayOutputStream.writeLength(value: Int) {
        if (value and 0x7F == value) {
            write(value)
        } else {
            write(0x80 or (value ushr 8))
            write(value and 0xFF)
        }
    }
}
