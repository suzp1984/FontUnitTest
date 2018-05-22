package io.github.jacobsu.truetypecli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class CmdArgs(parser: ArgParser) {
    val all by parser.flagging("-a", "--all", help = "generate all font digest")
    val unicode by parser.storing("-u", "--unicode",
                        help = "generate the font's digest by its unicode").default("")

}