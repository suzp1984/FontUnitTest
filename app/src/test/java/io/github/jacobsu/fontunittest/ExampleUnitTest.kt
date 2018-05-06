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
        println(trueTypeFont.glyphCount?.toUnsignedInt())

        trueTypeFont.glyphIndexs.forEachIndexed { index, glyphIndex ->
            println("offSet, length = ($index, ${glyphIndex?.offset?.toUnsignedLong()}, ${glyphIndex?.length?.toUnsignedLong()})")

        }

        trueTypeFont.cmapTable?.let {
            println("$it")
        }

        assertEquals(1, 1)
    }
}
