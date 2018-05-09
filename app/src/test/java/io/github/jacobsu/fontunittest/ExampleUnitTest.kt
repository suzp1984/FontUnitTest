package io.github.jacobsu.fontunittest

import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun access_assets() {

        val inputStream : InputStream  = javaClass.classLoader.getResourceAsStream("Font.ttf")

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

        val trueTypeFont = TrueTypeBuffer(fontBuffer)
        println(trueTypeFont.scalarType)
        println(trueTypeFont.numTables)
        println(trueTypeFont.searchRange)
        println(trueTypeFont.entrySelector)
        println(trueTypeFont.rangeShift)

        trueTypeFont.tables.forEach { s, table ->
            println("$s -> $table")
        }

        println(fontBuffer.size)
        println(trueTypeFont.glyphCount)
        println(trueTypeFont.glyphCount?.toUnsignedLong())

        trueTypeFont.glyphIndexs.forEachIndexed { index, glyphIndex ->
            println("index, offSet, length = ($index, ${glyphIndex?.offset?.toUnsignedLong()}, ${glyphIndex?.length?.toUnsignedLong()})")

        }

        trueTypeFont.cmapTable?.let {
            println("$it")
        }

        trueTypeFont.glyphs.forEachIndexed { index, glyph ->
            println("$index: ${glyph?.buffer?.getMd5Digest()?.encodeHex(false)}")

        }

        val glyph1 = trueTypeFont.getGlyphByUnicode(59648)
        val glyph2 = trueTypeFont.getGlyphByIndex(5)

        assertEquals(glyph1, glyph2)

        assertEquals(1, 1)
    }
}
