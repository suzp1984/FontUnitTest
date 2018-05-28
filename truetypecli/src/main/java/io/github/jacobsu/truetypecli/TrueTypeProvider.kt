package io.github.jacobsu.truetypecli

import io.github.jacobsu.truetype.FontUnicode
import io.github.jacobsu.truetype.TrueTypeFont
import io.github.jacobsu.truetype.getFontUnicodes
import java.io.InputStream

object TrueTypeProvider {

    val trueTypeFont : TrueTypeFont by lazy {
        val inputStream : InputStream = TrueTypeFont::class.java.classLoader.getResourceAsStream("src/main/assets/Font.ttf")

         TrueTypeFont(inputStream)
    }

    val fontUnicodes : List<FontUnicode> by lazy {
        val fontInputStream : InputStream  = FontUnicode::class.java.classLoader.getResourceAsStream("src/main/res/values/font.xml")
        getFontUnicodes(fontInputStream)
    }
}