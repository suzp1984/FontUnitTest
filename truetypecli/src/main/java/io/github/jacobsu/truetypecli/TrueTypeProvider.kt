package io.github.jacobsu.truetypecli

import io.github.jacobsu.truetype.FontUnicode
import io.github.jacobsu.truetype.TrueTypeFont
import org.w3c.dom.NodeList
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

object TrueTypeProvider {

    val trueTypeFont : TrueTypeFont by lazy {
        val inputStream : InputStream = TrueTypeFont::class.java.classLoader.getResourceAsStream("src/main/assets/Font.ttf")

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

         TrueTypeFont(fontBuffer)
    }

    val fontUnicodes : List<FontUnicode> by lazy {
        val fontInputStream : InputStream  = FontUnicode::class.java.classLoader.getResourceAsStream("src/main/res/values/font.xml")
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fontInputStream)
        val fonts : NodeList = xmlDoc.getElementsByTagName("string")

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

        fontUnicodes
    }
}