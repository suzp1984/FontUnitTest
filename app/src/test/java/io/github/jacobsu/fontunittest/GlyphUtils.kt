package io.github.jacobsu.fontunittest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.w3c.dom.NodeList
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory

class GlyphUtils {

    lateinit var trueTypeFont : TrueTypeBuffer
    lateinit var fontUnicodes : List<FontUnicode>

    @Before
    fun initGlypFont() {
        val inputStream : InputStream = javaClass.classLoader.getResourceAsStream("assets/Font.ttf")

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

        trueTypeFont = TrueTypeBuffer(fontBuffer)
    }

    @Before
    fun readFontXml() {
        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("res/values/font.xml")
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fontInputStream)
        val fonts : NodeList = xmlDoc.getElementsByTagName("string")

        fontUnicodes = (0 until fonts.length).map {
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
        }.filterNotNull()
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