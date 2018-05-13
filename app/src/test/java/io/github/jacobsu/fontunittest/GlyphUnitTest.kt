package io.github.jacobsu.fontunittest

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import org.w3c.dom.NodeList
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.rules.ErrorCollector
import org.junit.Rule



class GlyphUnitTest {
    lateinit var trueTypeFont : TrueTypeFont
    lateinit var xmlFontUnicodes : List<FontUnicode>
    lateinit var fontDigests : List<FontDigest>

    @Rule @JvmField
    val collector = ErrorCollector()

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

        trueTypeFont = TrueTypeFont(fontBuffer)
    }

    @Before
    fun readFontXml() {
        val fontInputStream : InputStream = javaClass.classLoader.getResourceAsStream("res/values/font.xml")
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fontInputStream)
        val fonts : NodeList = xmlDoc.getElementsByTagName("string")

        xmlFontUnicodes = (0 until fonts.length).map {
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

    @Before
    fun readFontDigestFromJson() {
        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("assets/font_digest.json")
        val gson = Gson()

        fontDigests = gson.fromJson(InputStreamReader(fontInputStream), object : TypeToken<List<FontDigest>>() {}.type)
    }

    @Test
    fun testTrueTypeFont() {
        collector.checkThat("check TrueType's offset tables is not empty.",
                true,
                equalTo(trueTypeFont.offsetTables.isNotEmpty()))

        trueTypeFont.offsetTables.forEach {
            collector.checkThat("check TrueType's offset table's name should not by empty",
                    true,
                    equalTo(it.key.isNotEmpty()))

        }

        collector.checkThat("check TrueType's header table should not be empty.",
                true,
                equalTo(trueTypeFont.headTable != null))

    }

    @Test
    fun testFontXmlRedundency() {
        var redundencyNames = listOf<String>()
        (0 until xmlFontUnicodes.size - 1).forEach {
            if (!redundencyNames.contains(xmlFontUnicodes[it].name)) {
                val sameUnicodes = xmlFontUnicodes.subList(it + 1, xmlFontUnicodes.size - 1)
                                                    .filter { font ->
                    font.unicode == xmlFontUnicodes[it].unicode
                }

                redundencyNames += sameUnicodes.map { it.name }

                collector.checkThat("check duplicated fonts with same unicode in xml: ${xmlFontUnicodes[it].unicode.encodeHex()}) ->" +
                        " ${sameUnicodes.map { it.name }.plus(xmlFontUnicodes[it].name).joinToString(separator = ", ", prefix = "[", postfix = "]")}",
                        true, equalTo(sameUnicodes.isEmpty()))
            }
        }
    }

    @Test
    fun testFontSize() {
        collector.checkThat("check ttf's glyph size: ",
                trueTypeFont.glyphCount,
                equalTo(xmlFontUnicodes.map { it.unicode }.toSet().size))
    }

    @Test
    fun testTTFRedundency() {
        var redundencyIndex = listOf<Int>()

        val glyphs = trueTypeFont.glyphs.filterNotNull()
        (0 until glyphs.size - 1).forEach { index ->
            if (!redundencyIndex.contains(index)) {

                val sameGlyphs = glyphs.subList(index + 1, glyphs.size - 1).filter { glyph ->
                    glyph.buffer == glyphs[index].buffer
                }

                redundencyIndex += sameGlyphs.map { it.index }

                collector.checkThat("check ttf redundency digest ${glyphs[index].buffer.getMd5Digest().encodeHex()}, " +
                        sameGlyphs.map { "${it.index}" }
                                .plus(index.toString())
                                .sortedBy { it.toInt() }
                                .joinToString(separator = ", ", prefix = "[", postfix = "]"),
                        true,
                        equalTo(sameGlyphs.isEmpty()))
            }
        }
    }

    @Test
    fun testFontMatch() {
        xmlFontUnicodes.forEach {fontUnicode ->
            val expectedDigest = fontDigests.find { it.name == fontUnicode.name }?.digest
            val actualDigest = trueTypeFont.getGlyphByUnicode(fontUnicode.unicode)?.buffer?.getMd5Digest()?.encodeHex()

            collector.checkThat("can found matched digest from json ${fontUnicode.unicode.encodeHex()}",
                    true,
                    equalTo(expectedDigest != null))

            collector.checkThat("can found matched font from ttf ${fontUnicode.unicode.encodeHex()}",
                    true,
                    equalTo(actualDigest != null))

            if (expectedDigest != null && actualDigest != null) {
                collector.checkThat("check font unicode ${fontUnicode.unicode.encodeHex()}: ",
                        actualDigest,
                        equalTo(expectedDigest)
                )
            }
        }
    }
}