package de.fokusstb.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Small date helpers mirroring the prototype's dParse/iso/dAdd/dDiff/fmtDay/fmtDate. */
object DateUtils {
    private val dayNames = listOf("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa")
    private val weekdayNamesFull = listOf(
        "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"
    )

    fun parse(s: String): LocalDate = LocalDate.parse(s)

    fun iso(d: LocalDate): String = d.toString()

    fun add(s: String, days: Int): String = iso(parse(s).plusDays(days.toLong()))

    fun diff(a: String, b: String): Int = ChronoUnit.DAYS.between(parse(a), parse(b)).toInt()

    /** java.time DayOfWeek: MONDAY=1..SUNDAY=7 — remap to the JS getDay() convention (SUNDAY=0..SATURDAY=6). */
    private fun jsDayIndex(d: LocalDate): Int = d.dayOfWeek.value % 7

    fun dayShort(s: String): String = dayNames[jsDayIndex(parse(s))]

    fun dayFull(s: String): String = weekdayNamesFull[jsDayIndex(parse(s))]

    /** "dd.MM." */
    fun fmtDate(s: String): String {
        val d = parse(s)
        return "%02d.%02d.".format(d.dayOfMonth, d.monthValue)
    }

    fun year(s: String): String = s.substring(0, 4)

    fun today(): String = iso(LocalDate.now())
}
