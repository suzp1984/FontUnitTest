package io.github.jacobsu.fontunittest

import java.util.*

class TrueTypeFont(private val buffer: List<Byte>) {
    val scalarType by lazy { buffer.getIntFrom(0) }
    val numTables  by lazy { buffer.getShortFrom(4) }
    val searchRange by lazy { buffer.getShortFrom(6) }
    val entrySelector by lazy { buffer.getShortFrom(8) }
    val rangeShift by lazy { buffer.getShortFrom(10) }

    val offsetTables by lazy {
        (0 until (numTables ?: 0)).map {
            val start = 12 + it * 16
            val tag = buffer.getStringFrom(start, 4) ?: ""
            val table = Table(checkSum = buffer.getIntFrom(start + 4) ?: 0,
                                offSet = buffer.getIntFrom(start + 8) ?: 0,
                                length = buffer.getIntFrom(start + 12) ?: 0)

            tag to table
        }.toMap()
    }

    val headTable : HeadTable? by lazy {
        val offSet = offsetTables["head"]?.offSet

        offSet?.let {
            val version = buffer.getIntFrom(it)
            val fontRevision = buffer.getIntFrom(it + 4)
            val checkSumAdjustment = buffer.getIntFrom(it + 8)
            val magicNumber = buffer.getIntFrom(it + 12)
            val flags = buffer.getShortFrom(it + 16)
            val unitsPerEm = buffer.getShortFrom(it + 18)
            val created = getCalendar(it + 20)
            val modified = getCalendar(it + 28)
            val xMin = buffer.getShortFrom(it + 36)
            val yMin = buffer.getShortFrom(it + 38)
            val xMax = buffer.getShortFrom(it + 40)
            val yMax = buffer.getShortFrom(it + 42)
            val macStyle = buffer.getShortFrom(it + 44)
            val lowestRecPPEM = buffer.getShortFrom(it + 46)
            val fontDirectionHint = buffer.getShortFrom(it + 48)
            val indexToLocFormat = buffer.getShortFrom(it + 50)
            val glyphDataFormat = buffer.getShortFrom(it + 52)

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
            } else {
                null
            }
        }
    }

    val cmapTable : CmapTable? by lazy {
        offsetTables["cmap"]?.offSet?.let { offset ->
            val version = buffer.getShortFrom(offset)?.toUnsignedInt()
            val numberSubtables = buffer.getShortFrom(offset + 2)?.toUnsignedInt()

            val subTables : List<CmapSubTable?> = (0 until (numberSubtables ?: 0)).map {
                val start = offset + 4 + it * 8
                val platformID = buffer.getShortFrom(start)?.toUnsignedInt()
                val platformSpecificID = buffer.getShortFrom(start + 2)?.toUnsignedInt()
                val subTableOffset = buffer.getIntFrom(start + 4)?.toUnsignedLong()

                if (platformID != null && platformSpecificID != null
                        && subTableOffset != null) {
                    val subTableStart = (offset + subTableOffset).toInt()
                    val format = buffer.getShortFrom(subTableStart)?.toUnsignedInt()
                    val length = buffer.getShortFrom(subTableStart + 2)?.toUnsignedInt()
                    val language = buffer.getShortFrom(subTableStart + 4)?.toUnsignedInt()

                    when (format) {
                        4 -> {
                            val segCountX2 = buffer.getShortFrom(subTableStart + 6)
                                                    ?.toUnsignedInt()
                            val searchRange = buffer.getShortFrom(subTableStart + 8)
                                                    ?.toUnsignedInt()
                            val entrySelector = buffer.getShortFrom(subTableStart + 10)
                                                    ?.toUnsignedInt()
                            val rangeShift = buffer.getShortFrom(subTableStart + 12)
                                                    ?.toUnsignedInt()

                            val endcode : List<Int> = (0 until (segCountX2 ?: 0)/2).mapNotNull {
                                val endCodeStart = subTableStart + 14 + it * 2
                                buffer.getShortFrom(endCodeStart)?.toUnsignedInt()
                            }

                            val reservedPadStart = subTableStart + 14 + (segCountX2 ?: 0)
                            val reservedPad = buffer.getShortFrom(reservedPadStart)?.toUnsignedInt()
                            val startCode = (0 until (segCountX2 ?: 0)/2).map {
                                val codeStart = reservedPadStart + 2 + it * 2
                                buffer.getShortFrom(codeStart)?.toUnsignedInt()
                            }

                            val idDeltaStart = reservedPadStart + 2 + (segCountX2 ?: 0)

                            val idDelta = (0 until (segCountX2 ?: 0) / 2).mapNotNull {
                                buffer.getShortFrom(idDeltaStart + it * 2)?.toUnsignedInt()
                            }

                            val idRangeOffsetStart = idDeltaStart + (segCountX2 ?: 0)
                            val idRangeOffSet = (0 until (segCountX2 ?: 0) / 2).mapNotNull {
                                buffer.getShortFrom(idRangeOffsetStart + it * 2)
                                        ?.toUnsignedInt()
                            }

                            if (length != null && language != null
                                    && segCountX2 != null && searchRange != null
                                    && entrySelector != null && endcode.isNotEmpty()
                                    && startCode.isNotEmpty() && idDelta.isNotEmpty()
                                    && reservedPad != null && rangeShift != null) {
                                CmapSubTable4(platformID, platformSpecificID, subTableOffset,
                                        format, length, language,
                                        segCountX2, searchRange, entrySelector,
                                        rangeShift, endcode, reservedPad,
                                        startCode.filterNotNull(), idDelta, idRangeOffSet)
                            } else {
                                null
                            }

                        }
                        else -> {
                            CmapSubTableUnknown(platformID, platformSpecificID, subTableOffset, format, length)
                        }
                    }
                } else {
                    null
                }
            }

            if (version != null && numberSubtables != null) {
                CmapTable(version, numberSubtables, subTables.filterNotNull())
            } else {
                null
            }
        }

    }

    val glyphCount by lazy {
        val offset = offsetTables["maxp"]?.offSet
        offset?.let {
            buffer.getShortFrom(it + 4)?.toUnsignedInt()
        }
    }

    val glyphs : List<Glyph> by lazy {
        (0 until (glyphCount ?: 0)).mapNotNull {
            getGlyphByIndex(it)
        }
    }

    val glyphIndexedOffsets : List<GlyphIndexedOffset> by lazy {
        (0 until (glyphCount ?: 0)).mapNotNull {
            val offset = getGlyphOffsetByIndex(it)
            val length = getGlyphLengthByIndex(it)

            if (offset != null && length != null) {
                GlyphIndexedOffset(it, offset, length)
            } else {
                null
            }
        }
    }

    fun getGlyphByUnicode(unicode : Int) : Glyph? {
        return cmapTable?.subTables?.map {
            it.getGlyphIndexByUnicode(unicode)?.run { getGlyphByIndex(this) }
        }?.firstOrNull { it != null }
    }

    fun getGlyphByIndex(index: Int) : Glyph? {
        val offset = getGlyphOffsetByIndex(index)
        val length = getGlyphLengthByIndex(index)
        val glyfTable = offsetTables["glyf"]

        if (offset != null && length != null
                        && glyfTable != null) {
            return Glyph(index, buffer.subListByLength(glyfTable.offSet + offset, length))
        }

        return null
    }

    private fun getCalendar(start : Int) : Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis =
                (buffer.getIntFrom(start)?.toUnsignedLong() ?: 0 * 0x100000000)
                + (buffer.getIntFrom(start + 4)?.toUnsignedLong() ?: 0)

        return calendar
    }

    private fun getGlyphOffsetByIndex(index : Int) : Int? {
        if (index < 0 || index >= glyphCount ?: 0) {
            return null
        }

        val locaOffset = offsetTables["loca"]?.offSet

        return locaOffset?.let {
            when (headTable?.indexToLocFormat) {
                1.toShort() -> {
                    val start = it + index * 4
                    buffer.getIntFrom(start)
                }
                else -> {
                    val start = it + index * 2
                    buffer.getShortFrom(start)?.toUnsignedInt()?.run { this * 2 }
                }
            }
        }
    }

    private fun getGlyphLengthByIndex(index: Int) : Int? {
        if (index < 0 || index >= glyphCount ?: 0) {
            return null
        }

        val start = getGlyphOffsetByIndex(index)
        val end = when (index + 1) {
            glyphCount ?: 0 -> {
                offsetTables["glyf"]?.length
            }
            else -> {
                getGlyphOffsetByIndex(index + 1)
            }
        }

        if (start != null && end != null) {
            return end - start
        }

        return null
    }
}

data class Table(val checkSum : Int, val offSet : Int, val length: Int)

data class HeadTable(val version : Int, val fontRevision : Int, val checkSumAdjustment : Int,
                     val magicNumber : Int, val flags : Short, val unitsPerEm : Short,
                     val created : Calendar, val modified : Calendar, val xMin : Short, val yMin : Short,
                     val xMax : Short, val yMax : Short, val macStyle : Short, val lowestRecPPEM : Short,
                     val fontDirectionHint : Short, val indexToLocFormat : Short, val glyphDataFormat : Short)

data class Glyph(val index : Int, val buffer : List<Byte>)

data class GlyphIndexedOffset(val index : Int, val offset : Int, val length : Int)

data class CmapTable(val version : Int, val numberSubtables : Int, val subTables : List<CmapSubTable>)

sealed class CmapSubTable(open val platformID : Int, open val platformSpecificID : Int,
                        open val offset : Long, open val format : Int?, open val length : Int?) {
    abstract fun getGlyphIndexByUnicode(unicode : Int) : Int?
}

data class CmapSubTableUnknown(override val platformID : Int, override val platformSpecificID : Int,
                               override val offset : Long, override val format : Int?,
                               override val length : Int?) :
        CmapSubTable(platformID, platformSpecificID, offset, format, length) {
    override fun getGlyphIndexByUnicode(unicode: Int): Int? {
        return null
    }
}

data class CmapSubTable4(override val platformID : Int, override val platformSpecificID : Int,
                         override val offset : Long, override val format : Int,
                         override val length : Int,
                         val language : Int, val segCountX2 : Int, val searchRange : Int,
                         val entrySelector : Int, val rangeShift : Int,
                         val endCode : List<Int>, val reservedPad : Int,
                         val startCode : List<Int>, val idDelta : List<Int>,
                         val idRangeOffSet : List<Int>) :
        CmapSubTable(platformID, platformSpecificID, offset, format, length) {

    override fun getGlyphIndexByUnicode(unicode: Int): Int? {

        return (0 until segCountX2 / 2).firstOrNull {
                (endCode.elementAtOrNull(it) ?: 0) >= unicode &&
                    (startCode.elementAtOrNull(it) ?: 0) <= unicode
            }?.let {
                val start = startCode.elementAtOrNull(it) ?: 0
                val end = endCode.elementAtOrNull(it) ?: 0
                val rangeOffset = idRangeOffSet.elementAtOrNull(it) ?: 0
                val delta = idDelta.elementAtOrNull(it) ?: 0

                return if ((start .. end).contains(unicode) && rangeOffset == 0) {
                            (delta + unicode) % 65536
                        } else {
                            null
                        }
            }
    }
}

