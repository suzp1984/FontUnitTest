package io.github.jacobsu.fontunittest

import java.util.*


class TrueTypeBuffer(val buffer: List<Byte>) {
    var pos : Int = 0
    val scalarType by lazy { buffer.subList(0, 4).getInt() }
    val numTables  by lazy { buffer.subList(4, 6).getShort() }
    val searchRange by lazy { buffer.subList(6, 8).getShort() }
    val entrySelector by lazy { buffer.subList(8, 10).getShort() }
    val rangeShift by lazy { buffer.subList(10, 12).getShort() }

    val tables by lazy {
        (0 until (numTables ?: 0)).map {
            val start = 12 + it * 16
            val tag = buffer.subList(start, start + 4).getString(4)
            val table = Table(checkSum = buffer.subList(start + 4, start + 8).getInt() ?: 0,
                                offSet = buffer.subList(start + 8, start + 12).getInt() ?: 0,
                                length = buffer.subList(start + 12, start + 16).getInt() ?: 0)

            tag to table
        }.toMap()
    }

    val headTable by lazy {
        val offSet = tables["head"]?.offSet

        offSet?.let {
            val version = buffer.subList(it, it + 4).getInt()
            val fontRevision = buffer.subList(it + 4, it + 8).getInt()
            val checkSumAdjustment = buffer.subList(it + 8, it + 12).getInt()
            val magicNumber = buffer.subList(it + 12, it + 16).getInt()
            val flags = buffer.subList(it + 16, it + 18).getShort()
            val unitsPerEm = buffer.subList(it + 18, it + 20).getShort()
            val created = getCalendar(it + 20)
            val modified = getCalendar(it + 28)
            val xMin = buffer.subList(it + 36, it + 38).getShort()
            val yMin = buffer.subList(it + 38, it + 40).getShort()
            val xMax = buffer.subList(it + 40, it + 42).getShort()
            val yMax = buffer.subList(it + 42, it + 44).getShort()
            val macStyle = buffer.subList(it + 44, it + 46).getShort()
            val lowestRecPPEM = buffer.subList(it + 46, it + 48).getShort()
            val fontDirectionHint = buffer.subList(it + 48, it + 50).getShort()
            val indexToLocFormat = buffer.subList(it + 50, it + 52).getShort()
            val glyphDataFormat = buffer.subList(it + 52, it + 54).getShort()

            if (version != null && fontRevision != null && checkSumAdjustment != null
                && magicNumber != null && flags != null && unitsPerEm != null
                && xMin != null && yMin != null && xMax != null && yMax != null
                && macStyle != null && lowestRecPPEM != null && fontDirectionHint != null
                && indexToLocFormat != null && glyphDataFormat != null) {
            HeadTable(version, fontRevision, checkSumAdjustment,
                    magicNumber, flags, unitsPerEm,
                    created, modified,
                    xMin, yMin, xMax, yMax,
                    macStyle, lowestRecPPEM,
                    fontDirectionHint,
                    indexToLocFormat,
                    glyphDataFormat)
            }
        }
    }
p
    private fun getCalendar(start : Int) : Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis =
                (buffer.subList(start, start + 4).getInt()?.toUnsignedLong() ?: 0 * 0x100000000)
                + (buffer.subList(start + 4, start + 8).getInt()?.toUnsignedLong() ?: 0)

        return calendar
    }
}

data class Table(val checkSum : Int, val offSet : Int, val length: Int)

data class HeadTable(val version : Int, val fontRevision : Int, val checkSumAdjustment : Int,
                     val magicNumber : Int, val flags : Short, val unitsPerEm : Short,
                     val created : Calendar, val modified : Calendar, val xMin : Short, val yMin : Short,
                     val xMax : Short, val yMax : Short, val macStyle : Short, val lowestRecPPEM : Short,
                     val fontDirectionHint : Short, val indexToLocFormat : Short, val glyphDataFormat : Short)