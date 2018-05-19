package io.github.jacobsu.truetype

import java.nio.charset.Charset
import java.security.MessageDigest

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

fun ByteArray.getMd5Digest() : ByteArray {
    return MessageDigest.getInstance("MD5").digest(this)
}

fun List<Byte>.getMd5Digest() : ByteArray {
    return toByteArray().getMd5Digest()
}

fun ByteArray.encodeHex(toLowerCase : Boolean = true) : String {
    val chars = ('0' .. '9') + if (toLowerCase) {
        ('a' .. 'f')
    } else {
        ('A' .. 'F')
    }

    return joinToString(separator = "") {
        "${chars.elementAt((240 and it.toUnsignedInt()) ushr 4)}" +
                "${chars.elementAt(15 and it.toUnsignedInt())}"
    }
}

fun List<Byte>.encodeHex(toLowerCase: Boolean = true) : String {
    return toByteArray().encodeHex(toLowerCase)
}

fun Int.toByteList() : List<Byte> {
    val r1 = (this and 0x00FF).toByte()
    val r2 = ((this and 0x00FF00) ushr 8).toByte()
    val r3 = ((this and 0x00FF0000) ushr 16).toByte()
    val r4 = ((this and 0xFF000000.toInt()) ushr 24).toByte()

    return listOf(r4, r3, r2, r1)
}

fun Int.encodeHex(toLowerCase: Boolean = true) : String {
    return toByteList().encodeHex(toLowerCase)
}

fun String.decodeHex() : Int? {
    val charsTable = ('0' .. '9') + ('a' .. 'f')
    val bytes = toLowerCase().trimStart('0').toCharArray().map {
        charsTable.indexOf(it)
    }

    return bytes.fold(0) { r, t ->
        (r shl 4) or t
    }
}

fun String.trimStartWithoutEmptyIt(vararg chars: Char, minLength : Int = 1) : String {
    return if (length > minLength) {
        substring(0, length - minLength).trimStart { it in chars } + substring(length - minLength, length)
    } else {
        this
    }
}