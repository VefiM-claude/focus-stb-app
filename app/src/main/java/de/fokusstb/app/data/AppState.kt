package de.fokusstb.app.data

data class DistractionEntry(val label: String, val at: String)

/** Everything the UI needs, combining the persisted fields with in-memory session state
 *  — this mirrors the prototype's single `this.state` object. */
data class AppState(
    // persisted
    val status: Map<String, String>,
    val ratings: Map<String, Double>,
    val notes: Map<String, List<Note>>,
    val queue: List<String>,
    val days: List<DayAvailability>,
    val nightBudget: Int,
    val weight: Map<String, String>,
    val pace: Map<String, String>,
    val catNames: Map<String, String>,
    val history: List<HistoryPoint>,
    val doneMin: Int,
    val profile: Profile,

    // ephemeral / session-only
    val tab: String = "heute",
    val search: String = "",
    val subFilter: String = "alle",
    val openTopic: String? = null,
    val focusSubId: String? = null,
    val focusTaskId: String? = null,
    val noteDraft: String = "",
    val sessionMin: Int? = null,
    val sessionKind: String? = null,
    val sessionTitle: String? = null,
    val elapsedSec: Int = 0,
    val running: Boolean = false,
    val pausedCount: Int = 0,
    val distractions: List<DistractionEntry> = emptyList(),
    val showRating: Boolean = false,
    val showDistract: Boolean = false,
    val ratedMinutes: Int = 90,
    val showProfile: Boolean = false,
) {
    fun toPersisted(): PersistedState = PersistedState(
        status = status, ratings = ratings, notes = notes, queue = queue, days = days,
        nightBudget = nightBudget, weight = weight, pace = pace, catNames = catNames,
        history = history, doneMin = doneMin, profile = profile,
    )

    companion object {
        /** First-ever launch: no saved data yet — seed a little demo content, same as the prototype did. */
        fun freshInstall(data: LernplanData): AppState {
            val status = data.tasks.associate { t ->
                t.id to when (t.status) { "Erledigt" -> "Erledigt"; "in Arbeit" -> "in Arbeit"; else -> "offen" }
            }
            return AppState(
                status = status,
                ratings = mapOf("t4" to 0.55, "t5" to 0.34, "t3" to 0.42),
                notes = mapOf(
                    "ust" to listOf(
                        Note(
                            id = 1L, at = "28. Aug.",
                            text = "Reihengeschäft: erst die bewegte Lieferung bestimmen, dann Ort und Befreiung. Reihenfolge in der Klausur aufschreiben.",
                        )
                    )
                ),
                queue = emptyList(),
                days = PersistedState.defaultDays(),
                nightBudget = 90,
                weight = emptyMap(),
                pace = emptyMap(),
                catNames = emptyMap(),
                history = PersistedState.defaultHistory(),
                doneMin = 0,
                profile = Profile(),
                showProfile = true,
            )
        }

        fun fromPersisted(p: PersistedState): AppState = AppState(
            status = p.status, ratings = p.ratings, notes = p.notes, queue = p.queue, days = p.days,
            nightBudget = p.nightBudget, weight = p.weight, pace = p.pace, catNames = p.catNames,
            history = p.history, doneMin = p.doneMin, profile = p.profile, showProfile = false,
        )

        /** Recomputes each task's status straight from the plan data — used by "delete local data". */
        fun resetStatusFrom(data: LernplanData): Map<String, String> =
            data.tasks.associate { t ->
                t.id to when (t.status) { "Erledigt" -> "Erledigt"; "in Arbeit" -> "in Arbeit"; else -> "offen" }
            }
    }
}
