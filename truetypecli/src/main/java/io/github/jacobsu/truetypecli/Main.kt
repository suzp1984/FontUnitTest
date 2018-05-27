package io.github.jacobsu.truetypecli

import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.github.jacobsu.truetype.*
import kotlin.system.exitProcess

fun main(args : Array<String>) = mainBody {

    ArgParser(args).parseInto(::CmdArgs).run {

        if (all) {

            val trueTypeFont = TrueTypeProvider.trueTypeFont

            val fontUnicodes = TrueTypeProvider.fontUnicodes
            // generate font digest

            val fontDigets : List<FontDigest?> = fontUnicodes.map {
                val digest = trueTypeFont.getGlyphByUnicode(it.unicode)
                                    ?.buffer
                                    ?.getMd5Digest()
                                    ?.encodeHex()

                digest?.let { str -> FontDigest(it.name, str) }
            }

            if (fontDigets.any {it == null}) {
                println("the digest can't be null")
                exitProcess(-102)
            }

            val gson = GsonBuilder().setPrettyPrinting().create()
            println(gson.toJson(fontDigets.filterNotNull()))
        } else if (unicode.isNotEmpty()) {
            unicode.decodeHex()?.let {
                TrueTypeProvider.trueTypeFont.getGlyphByUnicode(it)?.let {
                    println(it)
                    println("unicode ${unicode.trimStart('0')}: glyph buffer = ${it.buffer.encodeHex()}")
                    println("unicode ${unicode.trimStart('0')}: glyph digest = ${it.buffer.getMd5Digest().encodeHex()}")
                }
            }
        }

        return@run
    }

}