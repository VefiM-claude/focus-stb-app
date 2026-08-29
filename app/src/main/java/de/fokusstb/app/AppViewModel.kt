package de.fokusstb.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fokusstb.app.data.AppState
import de.fokusstb.app.data.DateUtils
import de.fokusstb.app.data.DistractionEntry
import de.fokusstb.app.data.HistoryPoint
import de.fokusstb.app.data.LernplanData
import de.fokusstb.app.data.Note
import de.fokusstb.app.data.StateStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val FALLBACK_SESSION_MIN = 45

class AppViewModel(val data: LernplanData, private val store: StateStore) : ViewModel() {

    private val _state = MutableStateFlow(
        store.load()?.let { AppState.fromPersisted(it) } ?: AppState.freshInstall(data)
    )
    val state: StateFlow<AppState> = _state

    private var tickerJob: Job? = null

    private fun update(persist: Boolean = true, transform: (AppState) -> AppState) {
        _state.update(transform)
        if (persist) store.save(_state.value.toPersisted())
    }

    private fun totalSeconds(s: AppState): Int = (s.sessionMin ?: FALLBACK_SESSION_MIN) * 60

    private fun currentClock(s: AppState): String {
        val rem = max(0, totalSeconds(s) - s.elapsedSec)
        return "%02d:%02d".format(rem / 60, rem % 60)
    }

    // ---- Navigation ----
    fun goTab(id: String) = update(persist = false) { it.copy(tab = id, openTopic = null) }

    fun pickOwn() = update(persist = false) { it.copy(tab = "themen", openTopic = null, subFilter = "faellig", search = "") }

    fun openTopicDetail(catId: String) = update(persist = false) { it.copy(openTopic = catId, noteDraft = "") }

    fun closeTopicDetail() = update(persist = false) { it.copy(openTopic = null) }

    // ---- Themen list ----
    fun setSearch(q: String) = update(persist = false) { it.copy(search = q) }

    fun setSubFilter(id: String) = update(persist = false) { it.copy(subFilter = id) }

    fun toggleQueue(topicId: String) = update { s ->
        s.copy(queue = if (s.queue.contains(topicId)) s.queue - topicId else s.queue + topicId)
    }

    // ---- Category (Gebiet) editing ----
    fun renameCategory(catId: String, name: String) = update { s -> s.copy(catNames = s.catNames + (catId to name)) }

    fun setWeight(catId: String, weight: String) = update { s -> s.copy(weight = s.weight + (catId to weight)) }

    fun setPace(catId: String, pace: String) = update { s -> s.copy(pace = s.pace + (catId to pace)) }

    fun setNoteDraft(text: String) = update(persist = false) { it.copy(noteDraft = text) }

    fun addNote(catId: String) = update { s ->
        val text = s.noteDraft.trim()
        if (text.isEmpty()) return@update s
        val note = Note(id = System.currentTimeMillis(), at = "Heute", text = text)
        val updated = listOf(note) + (s.notes[catId] ?: emptyList())
        s.copy(notes = s.notes + (catId to updated), noteDraft = "")
    }

    fun deleteNote(catId: String, noteId: Long) = update { s ->
        val updated = (s.notes[catId] ?: emptyList()).filterNot { it.id == noteId }
        s.copy(notes = s.notes + (catId to updated))
    }

    // ---- Plan / availability ----
    fun toggleDay(index: Int) = update { s ->
        s.copy(days = s.days.mapIndexed { i, d ->
            if (i != index) d else d.copy(on = !d.on, min = if (!d.on) (if (d.d == "Sa" || d.d == "So") 240 else s.nightBudget) else 0)
        })
    }

    fun pickBudget(minutes: Int) = update { s ->
        s.copy(
            nightBudget = minutes,
            days = s.days.map { d -> if (d.on && d.d != "Sa" && d.d != "So") d.copy(min = minutes) else d },
        )
    }

    // ---- Session lifecycle ----
    fun startSession(min: Int, kind: String, title: String, subId: String?, taskId: String?) {
        tickerJob?.cancel()
        update(persist = false) {
            it.copy(
                tab = "fokus", focusSubId = subId, focusTaskId = taskId, sessionMin = min, sessionKind = kind,
                sessionTitle = title, elapsedSec = 0, running = true, distractions = emptyList(),
                showRating = false, showDistract = false, pausedCount = 0,
            )
        }
        startTicker()
    }

    fun startTopicSession(topicId: String, topicTitle: String, topicRef: String, min: Int, kind: String = "Teilthema") {
        startSession(min, kind, topicTitle + (if (topicRef.isNotEmpty()) ", $topicRef" else ""), topicId, null)
    }

    fun startTaskSession(taskId: String) {
        val t = data.tasks.find { it.id == taskId } ?: return
        startSession(min(t.min, 240), t.type, t.title, t.topicIds.firstOrNull(), t.id)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _state.value
                if (!s.running) break
                val total = totalSeconds(s)
                val next = s.elapsedSec + 1
                if (next >= total) {
                    update(persist = false) { it.copy(elapsedSec = total, running = false, showRating = true, ratedMinutes = total / 60) }
                    break
                } else {
                    update(persist = false) { it.copy(elapsedSec = next) }
                }
            }
        }
    }

    fun toggleRun(on: Boolean) {
        tickerJob?.cancel()
        update(persist = false) { it.copy(running = on) }
        if (on) startTicker()
    }

    fun finishNow() {
        tickerJob?.cancel()
        update(persist = false) { s -> s.copy(running = false, showRating = true, ratedMinutes = max(1, (s.elapsedSec / 60.0).roundToInt())) }
    }

    fun openDistract() {
        tickerJob?.cancel()
        update(persist = false) { it.copy(running = false, showDistract = true, pausedCount = it.pausedCount + 1) }
    }

    fun closeDistractWithoutNote() {
        update(persist = false) { it.copy(showDistract = false) }
        toggleRun(true)
    }

    fun pickDistraction(label: String) {
        val clock = currentClock(_state.value)
        update(persist = false) { it.copy(distractions = it.distractions + DistractionEntry(label, clock), showDistract = false) }
        toggleRun(true)
    }

    fun rate(delta: Float, v: Int) {
        tickerJob?.cancel()
        update { s ->
            val id = s.focusSubId
            val prev = if (id != null) (s.ratings[id] ?: 0.3) else 0.3
            val ratings = if (id != null) s.ratings + (id to (prev + delta).toDouble().coerceIn(0.05, 0.98)) else s.ratings
            val status = if (s.focusTaskId != null) s.status + (s.focusTaskId to if (delta > 0) "Erledigt" else "in Arbeit") else s.status
            s.copy(
                running = false, showRating = false, tab = "fortschritt", focusSubId = null, focusTaskId = null,
                ratings = ratings, status = status,
                queue = if (id != null) s.queue - id else s.queue,
                doneMin = s.doneMin + s.ratedMinutes,
                history = (if (s.history.isNotEmpty()) s.history.drop(1) else s.history) + HistoryPoint(DateUtils.dayShort(DateUtils.today()), v),
            )
        }
    }

    // ---- Profile ----
    fun openProfile() = update(persist = false) { it.copy(showProfile = true) }
    fun closeProfile() = update(persist = false) { it.copy(showProfile = false) }
    fun setProfileName(name: String) = update { s -> s.copy(profile = s.profile.copy(name = name)) }
    fun setAnrede(id: String) = update { s -> s.copy(profile = s.profile.copy(anrede = id)) }
    fun setGender(id: String) = update { s -> s.copy(profile = s.profile.copy(gender = id)) }

    fun resetLocal() {
        store.clear()
        update(persist = false) { s ->
            s.copy(
                status = AppState.resetStatusFrom(data), ratings = emptyMap(), notes = emptyMap(), queue = emptyList(),
                catNames = emptyMap(), weight = emptyMap(), pace = emptyMap(), doneMin = 0,
                profile = de.fokusstb.app.data.Profile(), showProfile = true,
            )
        }
    }
}
