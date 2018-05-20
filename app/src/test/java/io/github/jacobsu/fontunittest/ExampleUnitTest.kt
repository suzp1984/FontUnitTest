package io.github.jacobsu.fontunittest

import io.github.jacobsu.truetype.*

import org.junit.Test

import org.junit.Assert.*
import org.w3c.dom.NodeList
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun access_assets() {

        val inputStream : InputStream  = javaClass.classLoader.getResourceAsStream("assets/Font.ttf")

        val byteArray = ByteArray(1024)
        val os = ByteArrayOutputStream()

        do {
            val l = inputStream.read(byteArray)

            if (l == -1) {
                break
            }

            os.write(byteArray, 0, l)

        } while (true)

        val fontBuffer = os.toByteArray().toList()

        val trueTypeFont = TrueTypeFont(fontBuffer)
        println(trueTypeFont.scalarType)
        println(trueTypeFont.numTables)
        println(trueTypeFont.searchRange)
        println(trueTypeFont.entrySelector)
        println(trueTypeFont.rangeShift)

        trueTypeFont.offsetTables.forEach { s, table ->
            println("$s -> $table")
        }

        println(fontBuffer.size)
        println(trueTypeFont.glyphCount)
        println(trueTypeFont.glyphCount?.toUnsignedLong())

        trueTypeFont.glyphIndexedOffsets.forEachIndexed { index, glyphIndex ->
            println("index, offSet, length = ($index, ${glyphIndex.offset.toUnsignedLong()}, ${glyphIndex.length.toUnsignedLong()})")

        }

        trueTypeFont.cmapTable?.let {
            println("$it")
        }

        trueTypeFont.glyphs.forEachIndexed { index, glyph ->
            println("$index: ${glyph.buffer.getMd5Digest().encodeHex(false)}")

        }

        val glyph1 = trueTypeFont.getGlyphByUnicode(59648)
        val glyph2 = trueTypeFont.getGlyphByIndex(5)

        assertEquals(glyph1, glyph2)

        assertEquals(1, 1)
    }

    @Test
    fun access_xml() {

        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("res/values/font.xml")
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fontInputStream)
        val fonts : NodeList = xmlDoc.getElementsByTagName("string")

        (0 until fonts.length).forEach {
            fonts.item(it).also {
                println("${it.attributes.getNamedItem("name").nodeValue} -> ${it.textContent.toCharArray().first().toInt().toByteList().encodeHex(false)}")
            }
        }

        val fontUnicodes : List<FontUnicode> = (0 until fonts.length).mapNotNull {
            val node = fonts.item(it)
            val name = node.attributes.getNamedItem("name").nodeValue
            val unicode = node.textContent.let {
                if (it.toCharArray().size == 1) {
                    it.first().toInt()
                } else {
                    null
                }
            }

            unicode?.let { FontUnicode(name, unicode) }
        }


        val inputStream : InputStream  = javaClass.classLoader.getResourceAsStream("assets/Font.ttf")

        val byteArray = ByteArray(1024)
        val os = ByteArrayOutputStream()

        do {
            val l = inputStream.read(byteArray)

            if (l == -1) {
                break
            }

            os.write(byteArray, 0, l)

        } while (true)

        val fontBuffer = os.toByteArray().toList()

        val trueTypeFont = TrueTypeFont(fontBuffer)

        fontUnicodes.forEachIndexed { index, fontUnicode ->
            val digest = trueTypeFont.getGlyphByUnicode(fontUnicode.unicode)?.buffer?.getMd5Digest()?.encodeHex()
            println("$index, ${fontUnicode.name} -> $digest")
        }

        assertEquals(1, 1)
    }
}
