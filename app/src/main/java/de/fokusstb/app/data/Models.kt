package de.fokusstb.app.data

/** Static plan data — loaded once from assets/lernplan.json, never mutated. */
data class Category(
    val id: String,
    val name: String,
    val short: String,
)

data class Topic(
    val id: String,
    val name: String,
    val ref: String,
    val cat: String,
    val taskIds: List<String>,
)

data class PlanTask(
    val id: String,
    val title: String,
    val status: String, // "Erledigt" | "in Arbeit" | "Nicht gestartet" | "offen"
    val date: String,   // yyyy-MM-dd
    val time: String,
    val type: String,   // Lernblock | Karteikarten | Fälle | Nacharbeit | Wiederholung | Vorbereitung | Klausur | Taper
    val min: Int,
    val topicIds: List<String>,
    val klausur: String,
    val notes: String,
)

data class ExamDay(val date: String, val label: String)

data class LernplanData(
    val today: String,
    val exam: List<ExamDay>,
    val cats: List<Category>,
    val topics: List<Topic>,
    val tasks: List<PlanTask>,
)

/** A note attached to a Gebiet (category). */
data class Note(
    val id: Long,
    val at: String,
    val text: String,
)

data class Profile(
    val name: String = "",
    val anrede: String = "du", // "du" | "sie"
    val gender: String = "keine", // "w" | "m" | "keine"
)

data class DayAvailability(
    val d: String,
    val label: String,
    val on: Boolean,
    val min: Int,
)

data class HistoryPoint(val d: String, val v: Int)

/** Everything that gets persisted to disk, mirroring the prototype's single localStorage key. */
data class PersistedState(
    val status: Map<String, String> = emptyMap(),
    val ratings: Map<String, Double> = emptyMap(),
    val notes: Map<String, List<Note>> = emptyMap(),
    val queue: List<String> = emptyList(),
    val days: List<DayAvailability> = defaultDays(),
    val nightBudget: Int = 90,
    val weight: Map<String, String> = emptyMap(),
    val pace: Map<String, String> = emptyMap(),
    val catNames: Map<String, String> = emptyMap(),
    val history: List<HistoryPoint> = defaultHistory(),
    val doneMin: Int = 0,
    val profile: Profile = Profile(),
) {
    companion object {
        fun defaultDays() = listOf(
            DayAvailability("Mo", "Montag", true, 90),
            DayAvailability("Di", "Dienstag", true, 90),
            DayAvailability("Mi", "Mittwoch", false, 0),
            DayAvailability("Do", "Donnerstag", true, 90),
            DayAvailability("Fr", "Freitag", true, 90),
            DayAvailability("Sa", "Samstag", true, 240),
            DayAvailability("So", "Sonntag", false, 0),
        )

        fun defaultHistory() = listOf(
            HistoryPoint("Di", 2), HistoryPoint("Mi", 3), HistoryPoint("Do", 2),
            HistoryPoint("Fr", 1), HistoryPoint("Sa", 3), HistoryPoint("So", 0),
            HistoryPoint("Mo", 2), HistoryPoint("Di", 1),
        )
    }
}
