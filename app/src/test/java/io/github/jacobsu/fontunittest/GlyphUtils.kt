package io.github.jacobsu.fontunittest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.jacobsu.truetype.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.io.InputStreamReader

class GlyphUtils {

    private lateinit var trueTypeFont : TrueTypeFont
    private lateinit var fontUnicodes : List<FontUnicode>

    @Before
    fun initGlypFont() {
        val inputStream : InputStream = javaClass.classLoader.getResourceAsStream("assets/Font.ttf")

        trueTypeFont = TrueTypeFont(inputStream)
    }

    @Before
    fun readFontXml() {
        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("res/values/font.xml")

        fontUnicodes = getFontUnicodes(fontInputStream)
    }

    @Test
    fun generateFontDigest() {

        val fontDigets : List<FontDigest?> = fontUnicodes.map {
            val digest = trueTypeFont.getGlyphByUnicode(it.unicode)?.buffer?.getMd5Digest()?.encodeHex()
            digest?.let { str -> FontDigest(it.name, str) }
        }

        Assert.assertEquals(false, fontDigets.any { it == null })

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(fontDigets.filterNotNull()))
    }

    @Test
    fun readFontDigestFromJson() {
        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("assets/font_digest.json")


        val gson = Gson()
        val fontDigests : List<FontDigest> = gson.fromJson(InputStreamReader(fontInputStream), object : TypeToken<List<FontDigest>>() {}.type)

        fontDigests.forEachIndexed { index, fontDigest ->
            println("$index, ${fontDigest.name} -> ${fontDigest.digest}")
            val fontUnicode = fontUnicodes.filter { it.name == fontDigest.name }
            Assert.assertEquals(1, fontUnicode.size)

            val glyph = trueTypeFont.getGlyphByUnicode(fontUnicode.first().unicode)
            Assert.assertEquals(true, glyph != null)
            Assert.assertEquals(fontDigest.digest, glyph?.buffer?.getMd5Digest()?.encodeHex())
        }
    }

    @Test
    fun readFontByUnicodeFromTTF() {
        val unicodeStr = "e9aa"

        unicodeStr.decodeHex()?.let {
            trueTypeFont.getGlyphByUnicode(it)?.also {
                println(it)
                println("unicode ${unicodeStr.trimStart('0')}: glyph buffer = ${it.buffer.encodeHex()}")
                println("unicode ${unicodeStr.trimStart('0')}: glyph digest = ${it.buffer.getMd5Digest().encodeHex()}")
            }
        }
    }
}