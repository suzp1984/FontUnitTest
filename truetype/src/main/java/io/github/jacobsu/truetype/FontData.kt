package io.github.jacobsu.truetype

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.w3c.dom.NodeList
import java.io.InputStream
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory

data class FontUnicode(val name : String, val unicode : Int)

data class FontDigest(val name: String, val digest : String)

fun getFontUnicodes(inputStream: InputStream) : List<FontUnicode> {
    val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
    val fonts : NodeList = xmlDoc.getElementsByTagName("string")

    return (0 until fonts.length).mapNotNull {
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

fun getFontDigests(inputStream: InputStream) : List<FontDigest> {
    val digestJson = Gson()

    return digestJson.fromJson(InputStreamReader(inputStream), object : TypeToken<List<FontDigest>>() {}.type)
}