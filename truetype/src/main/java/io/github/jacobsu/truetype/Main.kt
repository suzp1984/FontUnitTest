package io.github.jacobsu.truetype

import com.google.gson.GsonBuilder
import org.w3c.dom.NodeList
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

fun main(args : Array<String>) {
    println("Hello, world!")

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

    val trueTypeFont = TrueTypeFont(fontBuffer)

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

    // generate font digest

    val fontDigets : List<FontDigest?> = fontUnicodes.map {
        val digest = trueTypeFont.getGlyphByUnicode(it.unicode)?.buffer?.getMd5Digest()?.encodeHex()
        digest?.let { str -> FontDigest(it.name, str) }
    }

    if (fontDigets.any() {it == null}) {
        println("the digest can't be null")
        exitProcess(-102)
    }

    val gson = GsonBuilder().setPrettyPrinting().create()
    println(gson.toJson(fontDigets.filterNotNull()))
}