package io.github.jacobsu.fontunittest

import java.nio.charset.Charset

fun <T> List<T>.subListByLength(start : Int, lenght : Int) : List<T> {
    return subList(start, start + lenght)
}

fun List<Byte>.getIntFrom(start : Int) : Int? {
    return subListByLength(start, 4).getInt()
}

fun List<Byte>.getShortFrom(start : Int) : Short? {
    return subListByLength(start, 2).getShort()
}

fun List<Byte>.getStringFrom(start : Int, count: Int) : String? {
    return subListByLength(start, count).getString(count)
}

fun List<Byte>.getInt() : Int? {
    if (size < 4) {
        return null
    }

    return (this[0].toUnsignedInt() shl 24) or
            (this[1].toUnsignedInt() shl 16) or
            (this[2].toUnsignedInt() shl 8) or
            this[3].toUnsignedInt()
}

fun List<Byte>.getShort() : Short? {
    if (size < 2) {
        return null
    }

    return ((this[0].toUnsignedInt() shl 8) or
            this[1].toUnsignedInt()).toShort()
}

fun List<Byte>.getString(count : Int) : String? {
    if (this.size < count) {
        return null
    }
    return subList(0, count).toByteArray().toString(Charset.defaultCharset())
}


fun Int.toUnsignedLong() : Long {
    val s : Long = (toLong() and (1L shl 63)) ushr 32

    return (toLong() and 0x007FFFFFFF) or s
}

fun Short.toUnsignedInt() : Int {
    val s : Int = (toInt() and (1 shl 31)) ushr 16

    return (toInt() and 0x00007FFF) or s
}

fun Byte.toUnsignedInt() : Int {
    val s : Int = (toInt() and (1 shl 31)) ushr 24

    return (toInt() and 0x007F) or s
}