package de.fokusstb.app.presentation

import androidx.compose.ui.graphics.Color
import de.fokusstb.app.data.AppState
import de.fokusstb.app.data.Category
import de.fokusstb.app.data.DateUtils
import de.fokusstb.app.data.LernplanData
import de.fokusstb.app.data.PlanTask
import de.fokusstb.app.data.Topic
import de.fokusstb.app.ui.theme.Tokens
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure translation of the prototype's `renderVals()` into typed view data.
 * Called once per state change; cheap enough at this data size (122 topics / 255 tasks).
 */
object ViewBuilder {

    private const val FALLBACK_SESSION_MIN = 45
    private const val COACH_INTENSITY = "streng" // the only variant the prototype ever shipped without a settings UI

    fun build(state: AppState, data: LernplanData): DerivedView {
        val today = DateUtils.today()
        val topicById = data.topics.associateBy { it.id }
        val taskById = data.tasks.associateBy { it.id }

        fun catName(id: String): String = state.catNames[id] ?: data.cats.find { it.id == id }?.name ?: id
        fun rating(id: String?): Double = if (id == null) 0.0 else state.ratings[id] ?: 0.0

        fun nextTaskOf(tp: Topic): PlanTask? = tp.taskIds.mapNotNull { taskById[it] }
            .filter { it.date.isNotEmpty() && state.status[it.id] != "Erledigt" }
            .minByOrNull { it.date }

        fun dueRank(tp: Topic): Int {
            val t = nextTaskOf(tp) ?: return 9999
            return DateUtils.diff(today, t.date)
        }

        fun dueLabel(tp: Topic): String {
            val t = nextTaskOf(tp) ?: return "kein Termin"
            val n = DateUtils.diff(today, t.date)
            return when {
                n < 0 -> "überfällig"
                n == 0 -> "heute"
                n == 1 -> "morgen"
                else -> "in $n T"
            }
        }

        fun fill(s: Double): Triple<Color, Color, Color> = when {
            s <= 0.001 -> Triple(Color.Transparent, Tokens.neutral400, Tokens.neutral700)
            s < 0.35 -> Triple(Tokens.accent200, Tokens.accent500, Tokens.accent800)
            s < 0.6 -> Triple(Tokens.accent2_200, Tokens.accent2_400, Tokens.accent2_800)
            else -> Triple(Tokens.accent2_400, Tokens.accent2_600, Tokens.accent2_900)
        }

        fun hrs(mins: Int): String = String.format(Locale.GERMANY, "%.1f", mins / 60.0)

        fun skript(t: PlanTask): String {
            val idx = Regex("\\.\\s").find(t.notes)?.range?.first
            val head = if (idx != null) t.notes.substring(0, idx) else t.notes
            return head.take(90)
        }

        fun topicRow(tp: Topic, recommended: Boolean = false): TopicRow {
            val s = rating(tp.id)
            val (f, ring, fg) = fill(s)
            val queued = state.queue.contains(tp.id)
            val rank = dueRank(tp)
            return TopicRow(
                id = tp.id, name = tp.name, ref = tp.ref.ifEmpty { catName(tp.cat) }, cat = catName(tp.cat),
                pct = if (s > 0) "${(s * 100).roundToInt()}%" else "–", fill = f, ring = ring, fg = fg,
                due = dueLabel(tp),
                duePhrase = if (rank < 0) "überfällig" else "fällig ${dueLabel(tp)}",
                dueFg = if (rank <= 1) Tokens.accent700 else Tokens.neutral600,
                queued = queued, mark = if (queued) "✓" else "+",
                markBg = if (queued) Tokens.accent2_600 else Color.Transparent,
                markFg = if (queued) Color.White else Tokens.accent700,
                markRing = if (queued) Tokens.accent2_600 else Tokens.accent300,
                recommended = recommended,
            )
        }

        data class CatAgg(val cat: Category, val topics: List<Topic>, val tasks: List<PlanTask>, val s: Double, val rated: Int, val done: Int)

        val cats: List<CatAgg> = data.cats.map { c ->
            val tps = data.topics.filter { it.cat == c.id }
            val tpIds = tps.map { it.id }.toSet()
            val tks = data.tasks.filter { t -> t.topicIds.any { it in tpIds } }
            val done = tks.count { state.status[it.id] == "Erledigt" }
            val rated = tps.filter { rating(it.id) > 0 }
            val avg = if (rated.isNotEmpty()) rated.sumOf { rating(it.id) } / rated.size else 0.0
            val cover = if (tks.isNotEmpty()) done.toDouble() / tks.size else 0.0
            val score = max(cover, avg * (rated.size.toDouble() / max(1, tps.size)))
            CatAgg(c, tps, tks, score, rated.size, done)
        }
        fun catAgg(id: String) = cats.find { it.cat.id == id }

        val weakTopics = data.topics.sortedWith(compareBy({ dueRank(it) }, { rating(it.id) }))
        val weakest = weakTopics.firstOrNull()

        val profile = state.profile
        val formal = profile.anrede == "sie"
        val trimmedName = profile.name.trim()
        val addressName = if (trimmedName.isNotEmpty()) {
            if (formal) {
                (if (profile.gender == "w") "Frau " else if (profile.gender == "m") "Herr " else "") + trimmedName
            } else trimmedName
        } else ""
        val nameSuffix = if (addressName.isNotEmpty()) ", $addressName" else ""

        val doneTasks = data.tasks.count { state.status[it.id] == "Erledigt" }
        val activeTasks = data.tasks.count { state.status[it.id] == "in Arbeit" }
        val readinessPct = ((doneTasks + activeTasks * 0.5) / max(1, data.tasks.size).toDouble() * 100).roundToInt()
        val examDay = data.exam.firstOrNull()?.date ?: "2027-10-05"
        val daysLeft = DateUtils.diff(today, examDay)
        val planStart = data.tasks.mapNotNull { it.date.ifEmpty { null } }.minOrNull() ?: today
        val elapsedShare = max(0.0, min(1.0, DateUtils.diff(planStart, today).toDouble() / max(1, DateUtils.diff(planStart, examDay)))).toFloat()

        val todayTasks = data.tasks.filter { it.date == today }
        val queueTopics = state.queue.mapNotNull { topicById[it] }

        val coach = run {
            val sanft: String; val normal: String; val streng: String
            if (formal) {
                sanft = "Heute stehen ${todayTasks.size} Aufgaben im Plan. Fangen Sie mit der kürzeren an."
                normal = "${todayTasks.size} Aufgaben im Plan, $daysLeft Tage bis Tag 1. Halten Sie die Reihenfolge ein."
                streng = "$daysLeft Tage bis Tag 1 — und ${data.tasks.size - doneTasks} Aufgaben stehen noch offen. Heute wird gearbeitet, nicht sortiert."
            } else {
                sanft = "Heute stehen ${todayTasks.size} Aufgaben im Plan. Fang mit der kürzeren an."
                normal = "${todayTasks.size} Aufgaben im Plan, $daysLeft Tage bis Tag 1. Halte die Reihenfolge ein."
                streng = "$daysLeft Tage bis Tag 1 — und ${data.tasks.size - doneTasks} Aufgaben stehen noch offen. Heute wird gearbeitet, nicht sortiert."
            }
            when (COACH_INTENSITY) { "sanft" -> sanft; "normal" -> normal; else -> streng }
        }

        val streakLabel = run {
            var n = 0
            for (i in 0 until 60) {
                val day = DateUtils.add(today, -i)
                val tks = data.tasks.filter { it.date == day }
                if (tks.isEmpty()) continue
                if (tks.any { state.status[it.id] == "Erledigt" }) n++ else break
            }
            if (n == 0) "noch keine Serie" else "▲ $n " + (if (n == 1) "Lerntag" else "Lerntage") + " in Serie"
        }

        val headline = run {
            val n = if (queueTopics.isNotEmpty()) min(3, queueTopics.size) else todayTasks.size
            val names = listOf("Heute ist frei", "Eine Aufgabe", "Zwei Aufgaben", "Drei Aufgaben")
            (names.getOrNull(n) ?: "$n Aufgaben") + nameSuffix + "."
        }

        val dateLine = DateUtils.dayFull(today) + ", " + DateUtils.fmtDate(today) + DateUtils.year(today)

        val dayRec = state.days.find { it.d == DateUtils.dayShort(today) } ?: state.days.first()
        val onDays = state.days.filter { it.on }

        val todayHeading = run {
            val planned = todayTasks.sumOf { it.min }
            val cap = if (dayRec.on) dayRec.min else 0
            if (planned == 0) {
                if (cap > 0) "Heute · nichts geplant · $cap Min frei" else "Heute · Ruhetag"
            } else {
                "Heute · $planned Min geplant · " + (if (cap > 0) "$cap Min Kapazität" else "Ruhetag eingestellt")
            }
        }

        val todayBlocks: List<BlockRow> = if (queueTopics.isNotEmpty()) {
            queueTopics.take(3).mapIndexed { i, tp ->
                val each = max(20, ((dayRec.min.toDouble() / min(3, queueTopics.size) / 5).roundToInt()) * 5)
                val fullTitle = tp.name + (if (tp.ref.isNotEmpty()) ", ${tp.ref}" else "")
                BlockRow(
                    n = (i + 1).toString(), kind = "Selbst gewählt", min = each, title = tp.name,
                    meta = (tp.ref.ifEmpty { catName(tp.cat) }) + " · " + (if (dueRank(tp) < 0) "überfällig" else "fällig ${dueLabel(tp)}"),
                    dotBg = Tokens.accent2_200, dotFg = Tokens.accent2_800, tagFg = Tokens.accent2_700,
                    action = BlockAction.StartTopic(tp.id, each, "Selbst gewählt", fullTitle),
                )
            }
        } else {
            todayTasks.mapIndexed { i, t ->
                val statusLabel = when (state.status[t.id]) { "offen" -> "nicht gestartet"; else -> state.status[t.id] ?: "offen" }
                val refs = t.topicIds.mapNotNull { topicById[it]?.ref }.filter { it.isNotEmpty() }
                val metaHead = if (refs.isNotEmpty()) refs.joinToString(" · ") else skript(t)
                BlockRow(
                    n = (i + 1).toString(), kind = t.type, min = t.min, title = t.title,
                    meta = "$metaHead · $statusLabel",
                    dotBg = if (state.status[t.id] == "Erledigt") Tokens.accent2_400 else Tokens.accent200,
                    dotFg = Tokens.accent800, tagFg = Tokens.accent700,
                    action = BlockAction.StartTask(t.id),
                )
            }
        }

        val heute = HeuteView(
            dateLine = dateLine, headline = headline, streakLabel = streakLabel,
            initials = if (trimmedName.isNotEmpty()) trimmedName.split(Regex("\\s+")).mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase() else "?",
            coachLine = coach, daysLeft = daysLeft, elapsedShare = elapsedShare,
            phaseLine = "Aufbauphase · Woche " + (DateUtils.diff(planStart, today) / 7 + 1) + " von " + ceil(DateUtils.diff(planStart, examDay) / 7.0).toInt(),
            examLine = "Schriftliche Prüfung 5.–7. Okt. 2027. $doneTasks von ${data.tasks.size} Aufgaben erledigt.",
            todayHeading = todayHeading,
            doneLabel = if (state.doneMin > 0) "${state.doneMin} Min geschafft" else "noch nichts erledigt",
            blocksSource = if (queueTopics.isNotEmpty()) (if (formal) "Ihre Auswahl" else "Deine Auswahl") else (if (formal) "Aus Ihrem Lernplan" else "Aus deinem Lernplan"),
            blocksSourceBg = if (queueTopics.isNotEmpty()) Tokens.accent2_200 else Tokens.neutral300,
            blocksSourceFg = if (queueTopics.isNotEmpty()) Tokens.accent2_800 else Tokens.neutral800,
            blocksHint = if (queueTopics.isNotEmpty()) {
                if (formal) "Sie haben diese Teilthemen selbst vorgemerkt — sie gehen dem Plan vor." else "Du hast diese Teilthemen selbst vorgemerkt — sie gehen dem Plan vor."
            } else {
                if (formal) "Das ist der Plan für heute. Unter „Themen\" können Sie jederzeit etwas anderes vorziehen." else "Das ist der Plan für heute. Unter „Themen\" kannst du jederzeit etwas anderes vorziehen."
            },
            todayBlocks = todayBlocks,
            weakestName = (weakest?.name ?: "—") + (if (!weakest?.ref.isNullOrEmpty()) ", ${weakest?.ref}" else ""),
            weakestLine = (weakest?.let { catName(it.cat) } ?: "") + " · " + (if (rating(weakest?.id) > 0) "Recall ${(rating(weakest?.id) * 100).roundToInt()}%" else "noch nicht bewertet") +
                ", nächster Termin " + (if (weakest != null && dueRank(weakest) < 0) "überfällig" else weakest?.let { dueLabel(it) } ?: "") + ". Vorschlag — kein Befehl.",
            weakestCta = "${state.nightBudget} Min darauf ansetzen",
            weakestTopicId = weakest?.id,
            nightBudget = state.nightBudget,
        )

        // ---- Plan ----
        val weekStart = DateUtils.add(today, -(((DateUtils.parse(today).dayOfWeek.value - 1) + 7) % 7))
        val week: List<WeekBar> = state.days.mapIndexed { i, d ->
            val date = DateUtils.add(weekStart, i)
            val planned = data.tasks.filter { it.date == date }.sumOf { it.min }
            val isToday = date == today
            WeekBar(
                d = d.d,
                heightFraction = max(6f, min(62f, planned / 240f * 62f)) / 62f,
                fill = if (!d.on && planned == 0) Tokens.neutral300 else if (isToday) Tokens.accent500 else if (date < today) Tokens.accent2_500 else Tokens.accent2_300,
                fg = if (isToday) Tokens.accent700 else Tokens.neutral700,
                bold = isToday,
            )
        }
        val weekPlanned = state.days.indices.sumOf { i -> data.tasks.filter { it.date == DateUtils.add(weekStart, i) }.sumOf { it.min } }

        val availability = state.days.mapIndexed { i, d ->
            DayRow(
                index = i, d = d.d, label = d.label,
                bg = if (d.on) Tokens.accent2_500 else Color.Transparent,
                fg = if (d.on) Color.White else Tokens.neutral600,
                ring = if (d.on) Tokens.accent2_500 else Tokens.neutral400,
                minLabel = if (d.on) "${d.min} Min" else "frei",
                minFg = if (d.on) Tokens.neutral700 else Tokens.neutral500,
            )
        }
        val budgets = listOf(45, 60, 90, 120).map { m ->
            BudgetRow(m, "$m Min", if (state.nightBudget == m) Tokens.accent200 else Tokens.neutral100, if (state.nightBudget == m) Tokens.accent400 else Tokens.divider, state.nightBudget == m)
        }
        val upcoming = data.tasks.filter { it.date.isNotEmpty() && it.date > today }.take(8).map { t ->
            val refs = t.topicIds.mapNotNull { topicById[it]?.ref }.filter { it.isNotEmpty() }.take(2)
            val metaHead = if (refs.isNotEmpty()) refs.joinToString(" · ") else skript(t)
            UpcomingRow(
                taskId = t.id, day = DateUtils.dayShort(t.date), time = t.time.ifEmpty { DateUtils.fmtDate(t.date) },
                kind = t.type, title = t.title, meta = "$metaHead · ${t.min} Min",
                tagBg = when (t.type) { "Klausur" -> Tokens.neutral300; "Karteikarten" -> Tokens.accent2_200; else -> Tokens.accent200 },
                tagFg = when (t.type) { "Klausur" -> Tokens.neutral800; "Karteikarten" -> Tokens.accent2_800; else -> Tokens.accent800 },
            )
        }
        val klausuren = data.tasks.filter { it.type == "Klausur" && !Regex("^Vorbereitung", RegexOption.IGNORE_CASE).containsMatchIn(it.title) && it.date > today }
            .take(5).map { t ->
                val diffDays = DateUtils.diff(today, t.date)
                KlausurRow(
                    title = t.title,
                    meta = DateUtils.dayShort(t.date) + ", " + DateUtils.fmtDate(t.date) + DateUtils.year(t.date) + " · " + (if (t.min >= 120) "${(t.min / 60.0).roundToInt()} Std." else "${t.min} Min"),
                    pts = "in $diffDays T", fg = if (diffDays < 30) Tokens.accent700 else Tokens.accent2_700,
                )
            }

        val plan = PlanView(
            planIntro = if (formal) "Die Termine stehen fest aus Ihrem Lernplan. Was Sie selbst vorziehen wollen, kommt über die Merkliste unter „Themen\"." else "Die Termine stehen fest aus deinem Lernplan. Was du selbst vorziehen willst, kommt über die Merkliste unter „Themen\".",
            capacityHeading = if (formal) "Wann können Sie?" else "Wann kannst du?",
            weekTotalLabel = hrs(onDays.sumOf { it.min }) + " Std.",
            availability = availability, budgets = budgets,
            availabilityLine = "${onDays.size} Lerntage · ${hrs(onDays.sumOf { it.min })} Std. Kapazität gegen ${hrs(weekPlanned)} Std. Plan diese Woche.",
            weekDoneLabel = "${hrs(weekPlanned)} Std. geplant · ${onDays.size} Lerntage",
            week = week, upcoming = upcoming, klausuren = klausuren,
        )

        // ---- Themen ----
        val filtered = data.topics.filter { tp ->
            val q = state.search.trim().lowercase()
            if (q.isNotEmpty() && !("${tp.name} ${tp.ref} ${catName(tp.cat)}").lowercase().contains(q)) return@filter false
            when (state.subFilter) {
                "faellig" -> dueRank(tp) <= 1
                "schwach" -> rating(tp.id) > 0 && rating(tp.id) < 0.45
                "offen" -> rating(tp.id) == 0.0
                "gewaehlt" -> state.queue.contains(tp.id)
                else -> true
            }
        }.sortedWith(compareBy({ dueRank(it) }, { rating(it.id) }))

        val filterDefs = listOf("alle" to "Alle", "faellig" to "Fällig", "offen" to "Offen", "schwach" to "Schwach", "gewaehlt" to "Merkliste")
        val themen = ThemenView(
            topicCountLine = "${data.topics.size} Teilthemen aus " + (if (formal) "Ihrem" else "deinem") + " Lernplan · ${data.cats.size} Gebiete · ${data.tasks.size} Aufgaben",
            themenIntro = if (formal) "Die App empfiehlt — vormerken tun Sie." else "Die App empfiehlt — vormerken tust du.",
            queueLabel = if (state.queue.isEmpty()) "Nichts vorgemerkt" else "${state.queue.size} vorgemerkt",
            allSubsCount = "${filtered.size} Treffer" + (if (filtered.size > 40) " · erste 40" else ""),
            filters = filterDefs.map { (id, label) ->
                ChipOption(id, label, if (state.subFilter == id) Tokens.accent200 else Color.Transparent, if (state.subFilter == id) Tokens.accent400 else Tokens.divider, state.subFilter == id)
            },
            allSubs = filtered.take(40).map { topicRow(it) },
            nightBudget = state.nightBudget,
            topicList = cats.sortedBy { it.s }.map { c ->
                val w = state.weight[c.cat.id] ?: "hoch"
                CategoryRow(
                    id = c.cat.id, name = catName(c.cat.id), pct = "${(c.s * 100).roundToInt()}%",
                    fill = fill(c.s).first, ring = fill(c.s).second,
                    weight = w, weightBg = if (w == "hoch") Tokens.accent200 else Tokens.neutral300, weightFg = if (w == "hoch") Tokens.accent800 else Tokens.neutral800,
                    meta = "${c.topics.size} Teilthemen · ${c.tasks.size} Aufgaben · ${c.done} erledigt",
                )
            },
        )

        // ---- Topic detail ----
        val topicDetail: TopicDetailView? = state.openTopic?.let { openId ->
            val c = catAgg(openId) ?: return@let null
            val (f, ring, fg) = fill(c.s)
            val tps = c.topics.sortedWith(compareBy({ dueRank(it) }, { rating(it.id) }))
            fun cut(s: String): String = if (s.length <= 190) s else s.substring(0, s.lastIndexOf(' ', 190).let { if (it < 0) 190 else it }) + " …"
            val hints = c.tasks.filter { it.notes.isNotEmpty() }.take(3).map { HintRow(if (it.date.isNotEmpty()) DateUtils.fmtDate(it.date) else "", cut(it.notes)) }
            val weightNow = state.weight[c.cat.id] ?: "hoch"
            val paceNow = state.pace[c.cat.id] ?: "normal"
            val draftFilled = state.noteDraft.trim().isNotEmpty()
            val startTopic = tps.firstOrNull() ?: c.topics.firstOrNull()
            TopicDetailView(
                catId = c.cat.id, tName = catName(c.cat.id), tPct = "${(c.s * 100).roundToInt()}%", tFill = f, tRing = ring, tFg = fg,
                tMeta = "${c.topics.size} Teilthemen · ${c.done} von ${c.tasks.size} Aufgaben erledigt",
                planSourceHeading = if (formal) "Aus Ihrem Lernplan" else "Aus deinem Lernplan",
                tHints = hints,
                tSubs = tps.mapIndexed { i, tp -> topicRow(tp, recommended = i == 0) },
                tWeights = listOf("hoch", "mittel", "niedrig").map { w ->
                    ChipOption(w, w, if (weightNow == w) Tokens.accent200 else Color.Transparent, if (weightNow == w) Tokens.accent400 else Tokens.divider, weightNow == w)
                },
                tPaces = listOf(Triple("eng", "eng", "alle 2–3 Tage"), Triple("normal", "normal", "alle 5–8 Tage"), Triple("weit", "weit", "alle 2–3 Wochen")).map { (id, label, hint) ->
                    PaceRow(id, label, hint, if (paceNow == id) Tokens.accent2_200 else Color.Transparent, if (paceNow == id) Tokens.accent2_400 else Tokens.divider, paceNow == id)
                },
                tNotes = (state.notes[c.cat.id] ?: emptyList()).map { NoteRow(it.id, it.at, it.text) },
                tNotesEmpty = (state.notes[c.cat.id] ?: emptyList()).isEmpty(),
                noteEmptyLine = if (formal) "Noch keine Notiz zu diesem Gebiet. Schreiben Sie nach dem nächsten Block auf, was Sie gestolpert hat." else "Noch keine Notiz zu diesem Gebiet. Schreib nach dem nächsten Block auf, was dich gestolpert hat.",
                addBg = if (draftFilled) Tokens.accent else Tokens.neutral300,
                addFg = if (draftFilled) Color.White else Tokens.neutral600,
                tStartTopicId = startTopic?.id,
                tStartTitle = startTopic?.name ?: "",
                tStartRef = startTopic?.ref ?: "",
                nightBudget = state.nightBudget,
            )
        }

        // ---- Fokus ----
        val totalSec = (state.sessionMin ?: FALLBACK_SESSION_MIN) * 60
        val pctF = (if (totalSec > 0) state.elapsedSec.toFloat() / totalSec else 0f).coerceIn(0f, 1f)
        val remSec = max(0, totalSec - state.elapsedSec)
        val clock = "%02d:%02d".format(remSec / 60, remSec % 60)
        val fokus = FokusView(
            sessionKind = state.sessionKind ?: "Lernblock",
            sessionTitle = state.sessionTitle ?: (todayTasks.firstOrNull()?.title ?: "Lernblock"),
            pauseLabel = if (state.pausedCount > 0) "${state.pausedCount}× pausiert" else "ohne Pause",
            clock = clock,
            growthLabel = if (pctF >= 1f) "ausgewachsen" else "${(pctF * 100).roundToInt()}% gewachsen",
            pct = pctF,
            runLabel = if (state.running) "Pausieren" else if (pctF > 0f) "Weiter" else "Block starten",
            runBg = if (state.running) Tokens.surface else Tokens.accent,
            runFg = if (state.running) Tokens.accent800 else Color.White,
            distractLine = if (state.distractions.isEmpty()) {
                if (formal) "Noch nichts notiert. Tippen Sie „!\", wenn Sie etwas rausholt — das Muster zählt, nicht der einzelne Ausrutscher." else "Noch nichts notiert. Tippe „!\", wenn dich etwas rausholt — das Muster zählt, nicht der einzelne Ausrutscher."
            } else "${state.distractions.size} Unterbrechung(en) in diesem Block.",
            distractions = state.distractions,
        )

        // ---- Beet ----
        val beet = BeetView(
            beetIntro = if (formal) "Jeder Kreis ist ein Prüfungsgebiet. Er wächst mit den erledigten Aufgaben aus Ihrem Lernplan und mit Ihrer Selbsteinschätzung nach jedem Block." else "Jeder Kreis ist ein Prüfungsgebiet. Er wächst mit den erledigten Aufgaben aus deinem Lernplan und mit deiner Selbsteinschätzung nach jedem Block.",
            readiness = "$readinessPct%",
            readinessLine = "$doneTasks erledigt, $activeTasks in Arbeit, ${data.tasks.size - doneTasks - activeTasks} offen — $daysLeft Tage bis Tag 1.",
            garden = cats.map { c ->
                val (f, ring, fg) = fill(c.s)
                GardenNode(c.cat.id, c.cat.short, "${(c.s * 100).roundToInt()}%", (44 + c.s * 42).roundToInt(), f, ring, fg)
            },
            weakList = weakTopics.take(6).map { tp ->
                val rank = dueRank(tp)
                WeakListRow(
                    name = tp.name + (if (tp.ref.isNotEmpty()) ", ${tp.ref}" else ""),
                    meta = catName(tp.cat) + " · " + (if (rating(tp.id) > 0) "Recall ${(rating(tp.id) * 100).roundToInt()}%" else "noch nicht bewertet"),
                    due = dueLabel(tp),
                    dot = if (rank <= 0) Tokens.accent500 else if (rank < 14) Tokens.accent2_500 else Tokens.accent2_700,
                )
            },
            history = state.history.map { h ->
                HistoryBar(h.d, when (h.v) { 3 -> Tokens.accent2_600; 2 -> Tokens.accent2_300; 1 -> Tokens.accent400; else -> Tokens.neutral300 })
            },
        )

        // ---- Profile ----
        val profileView = ProfileView(
            profileIntro = if (formal) "Nur damit der Coach Sie richtig anspricht. Alles bleibt auf diesem Gerät — kein Konto, keine Übertragung." else "Nur damit der Coach dich richtig anspricht. Alles bleibt auf diesem Gerät — kein Konto, keine Übertragung.",
            statsHeading = if (formal) "Ihre Zahlen" else "Deine Zahlen",
            anredeOpts = listOf("du" to "Du", "sie" to "Sie").map { (id, label) ->
                ChipOption(id, label, if (profile.anrede == id) Tokens.accent200 else Color.Transparent, if (profile.anrede == id) Tokens.accent400 else Tokens.divider, profile.anrede == id)
            },
            genderOpts = listOf("w" to "weiblich", "m" to "männlich", "keine" to "keine Angabe").map { (id, label) ->
                ChipOption(id, label, if (profile.gender == id) Tokens.accent2_200 else Color.Transparent, if (profile.gender == id) Tokens.accent2_400 else Tokens.divider, profile.gender == id)
            },
            greetingPreview = (if (formal) "So spreche ich Sie an: „Bereit" else "So rede ich dich an: „Bereit") + (if (addressName.isNotEmpty()) ", $addressName" else "") + "?\"",
            profileStats = listOf(
                "Aufgaben erledigt" to "$doneTasks / ${data.tasks.size}",
                "Fokuszeit protokolliert" to "${hrs(state.doneMin)} Std.",
                "Teilthemen bewertet" to "${state.ratings.size} / ${data.topics.size}",
                "Merkliste" to "${state.queue.size} vorgemerkt",
                "Prüfungsreife" to "$readinessPct%",
                "Tage bis Tag 1" to "$daysLeft",
            ),
        )

        val ratings = listOf(
            RatingOption("Saß", if (state.focusTaskId != null) "Aufgabe im Plan als erledigt buchen" else "Recall hochsetzen, später wiederholen", Tokens.accent2_600, Tokens.accent2_100, Tokens.accent2_300, 0.12f, 3),
            RatingOption("Wackelig", if (state.focusTaskId != null) "Erledigt, aber früher wiederholen" else "Recall leicht hoch, früher wiederholen", Tokens.accent2_300, Tokens.neutral100, Tokens.divider, 0.04f, 2),
            RatingOption("Nicht verstanden", if (state.focusTaskId != null) "Bleibt „in Arbeit\", morgen erneut" else "Recall runter, morgen erneut", Tokens.accent400, Tokens.accent100, Tokens.accent300, -0.06f, 1),
        )

        val tabDefs = listOf("heute" to "Heute", "plan" to "Plan", "themen" to "Themen", "fokus" to "Fokus", "fortschritt" to "Beet")
        val tabs = tabDefs.map { (id, label) ->
            TabView(id, label, if (state.tab == id) Tokens.accent800 else Tokens.neutral600, if (state.tab == id) Tokens.accent200 else Color.Transparent, state.tab == id)
        }

        return DerivedView(
            formal = formal, heute = heute, plan = plan, themen = themen, topicDetail = topicDetail,
            fokus = fokus, beet = beet, profile = profileView,
            ratedMinutes = state.ratedMinutes,
            ratingSheetTitle = "Ehrlich jetzt: saß das?",
            ratings = ratings,
            distractQ = if (formal) "Was hat Sie rausgeholt?" else "Was hat dich rausgeholt?",
            distractOptions = listOf("Handy", "Kollege / Familie", "Müdigkeit", "Gedanken abgedriftet", "Hunger"),
            tabs = tabs,
        )
    }
}
