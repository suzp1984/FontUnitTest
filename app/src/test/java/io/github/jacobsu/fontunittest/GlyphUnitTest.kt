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
    private lateinit var trueTypeFont : TrueTypeFont
    private lateinit var xmlFontUnicodes : List<FontUnicode>
    private lateinit var fontDigests : List<FontDigest>

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

        xmlFontUnicodes = (0 until fonts.length).mapNotNull {
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
    }

    @Before
    fun readFontDigestFromJson() {
        val fontInputStream : InputStream  = javaClass.classLoader.getResourceAsStream("assets/font_digest.json")
        val digestJson = Gson()

        fontDigests = digestJson.fromJson(InputStreamReader(fontInputStream), object : TypeToken<List<FontDigest>>() {}.type)
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

        collector.checkThat("check TrueType's cmap table",
                true,
                equalTo(trueTypeFont.cmapTable != null))

        collector.checkThat("check TrueType's glyph count",
                trueTypeFont.glyphCount,
                equalTo(trueTypeFont.glyphIndexedOffsets.size))
    }

    /*
     * The font icons in side xml file should not have any font name with same unicode value.
     */
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

                collector.checkThat("check duplicated fonts with same unicode in xml: " +
                        "${xmlFontUnicodes[it].unicode.encodeHex()}) ->" +
                        " ${sameUnicodes.map { it.name }
                                .plus(xmlFontUnicodes[it].name)
                                .joinToString(separator = ", ", prefix = "[", postfix = "]")}",
                        true,
                        equalTo(sameUnicodes.isEmpty()))
            }
        }
    }

    /*
     * The size of font described in Xml should be match with the font counts inside the TTF file.
     */
    @Test
    fun testXmlTTFSizeMatch() {
        collector.checkThat("check ttf's glyph size: ",
                trueTypeFont.glyphCount,
                equalTo(xmlFontUnicodes.map { it.unicode }.toSet().size))
    }

    /*
     * the font inside the TTF should not has same buffer.
     */
    @Test
    fun testTTFRedundency() {
        var redundencyIndex = listOf<Int>()

        val glyphs = trueTypeFont.glyphs
        (0 until glyphs.size - 1).forEach { index ->
            if (!redundencyIndex.contains(index)) {

                val sameGlyphs = glyphs.subList(index + 1, glyphs.size - 1).filter { glyph ->
                    glyph.buffer == glyphs[index].buffer
                }

                redundencyIndex += sameGlyphs.map { it.index }

                collector.checkThat("check ttf redundency digest ${glyphs[index].buffer.getMd5Digest().encodeHex()}, " +
                        sameGlyphs
                                .plus(glyphs[index])
                                .sortedBy { it.index }
                                .map { "${it.index}: ${it.unicodes.map { it.encodeHex().trimStartWithoutEmptyIt('0', minLength = 4) }}" }
                                .joinToString(separator = ", ", prefix = "[", postfix = "]"),
                        true,
                        equalTo(sameGlyphs.isEmpty()))
            }
        }
    }

    /*
     * The xml file, describe the font unicode, and the json file, describe the font degist, should match each other.
     */
    @Test
    fun testXmlAndJsonMatch() {
        collector.checkThat("check the count of xml font and the count of font inside json sample: ",
                xmlFontUnicodes.count(),
                equalTo(fontDigests.count()))

        xmlFontUnicodes.forEach { fontUnicode ->
            collector.checkThat("font described in xml ${fontUnicode.name} : ${fontUnicode.unicode} shouldn't found any match digest inside json sample",
                    true,
                    equalTo(fontDigests.any { fontUnicode.name == it.name }))
        }

        fontDigests.forEach { fontDigist ->
            collector.checkThat("font described in sample json ${fontDigist.name} shouldn't found any match font unicode inside xml",
                    true,
                    equalTo(xmlFontUnicodes.any { fontDigist.name == it.name }))
        }
    }

    /*
     * read the font from the TTF file according to the unicode described from the xml file, than compared with file digest read from the json sample.
     */
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