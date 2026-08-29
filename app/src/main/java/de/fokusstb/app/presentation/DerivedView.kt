package de.fokusstb.app.presentation

import androidx.compose.ui.graphics.Color

sealed class BlockAction {
    data class StartTopic(val topicId: String, val min: Int, val kind: String, val fullTitle: String) : BlockAction()
    data class StartTask(val taskId: String) : BlockAction()
}

data class ChipOption(val id: String, val label: String, val bg: Color, val ring: Color, val bold: Boolean)

data class BlockRow(
    val n: String, val kind: String, val min: Int, val title: String, val meta: String,
    val dotBg: Color, val dotFg: Color, val tagFg: Color, val action: BlockAction,
)

data class HeuteView(
    val dateLine: String,
    val headline: String,
    val streakLabel: String,
    val initials: String,
    val coachLine: String,
    val daysLeft: Int,
    val elapsedShare: Float,
    val phaseLine: String,
    val examLine: String,
    val todayHeading: String,
    val doneLabel: String,
    val blocksSource: String,
    val blocksSourceBg: Color,
    val blocksSourceFg: Color,
    val blocksHint: String,
    val todayBlocks: List<BlockRow>,
    val weakestName: String,
    val weakestLine: String,
    val weakestCta: String,
    val weakestTopicId: String?,
    val nightBudget: Int,
)

data class DayRow(
    val index: Int, val d: String, val label: String, val bg: Color, val fg: Color,
    val ring: Color, val minLabel: String, val minFg: Color,
)

data class BudgetRow(val minutes: Int, val label: String, val bg: Color, val ring: Color, val bold: Boolean)

data class WeekBar(val d: String, val heightFraction: Float, val fill: Color, val fg: Color, val bold: Boolean)

data class UpcomingRow(
    val taskId: String, val day: String, val time: String, val kind: String, val title: String,
    val meta: String, val tagBg: Color, val tagFg: Color,
)

data class KlausurRow(val title: String, val meta: String, val pts: String, val fg: Color)

data class PlanView(
    val planIntro: String,
    val capacityHeading: String,
    val weekTotalLabel: String,
    val availability: List<DayRow>,
    val budgets: List<BudgetRow>,
    val availabilityLine: String,
    val weekDoneLabel: String,
    val week: List<WeekBar>,
    val upcoming: List<UpcomingRow>,
    val klausuren: List<KlausurRow>,
)

data class TopicRow(
    val id: String, val name: String, val ref: String, val cat: String,
    val pct: String, val fill: Color, val ring: Color, val fg: Color,
    val due: String, val duePhrase: String, val dueFg: Color,
    val queued: Boolean, val mark: String, val markBg: Color, val markFg: Color, val markRing: Color,
    val recommended: Boolean = false,
)

data class CategoryRow(
    val id: String, val name: String, val pct: String, val fill: Color, val ring: Color,
    val weight: String, val weightBg: Color, val weightFg: Color, val meta: String,
)

data class ThemenView(
    val topicCountLine: String,
    val themenIntro: String,
    val queueLabel: String,
    val allSubsCount: String,
    val filters: List<ChipOption>,
    val allSubs: List<TopicRow>,
    val topicList: List<CategoryRow>,
    val nightBudget: Int,
)

data class HintRow(val at: String, val text: String)
data class NoteRow(val id: Long, val at: String, val text: String)

data class TopicDetailView(
    val catId: String,
    val tName: String,
    val tPct: String,
    val tFill: Color,
    val tRing: Color,
    val tFg: Color,
    val tMeta: String,
    val planSourceHeading: String,
    val tHints: List<HintRow>,
    val tSubs: List<TopicRow>,
    val tWeights: List<ChipOption>,
    val tPaces: List<PaceRow>,
    val tNotes: List<NoteRow>,
    val tNotesEmpty: Boolean,
    val noteEmptyLine: String,
    val addBg: Color,
    val addFg: Color,
    val tStartTopicId: String?,
    val tStartTitle: String,
    val tStartRef: String,
    val nightBudget: Int,
)

data class PaceRow(val id: String, val label: String, val hint: String, val bg: Color, val ring: Color, val bold: Boolean)

data class FokusView(
    val sessionKind: String,
    val sessionTitle: String,
    val pauseLabel: String,
    val clock: String,
    val growthLabel: String,
    val pct: Float,
    val runLabel: String,
    val runBg: Color,
    val runFg: Color,
    val distractLine: String,
    val distractions: List<de.fokusstb.app.data.DistractionEntry>,
)

data class GardenNode(val catId: String, val short: String, val pct: String, val sizeDp: Int, val fill: Color, val ring: Color, val fg: Color)
data class WeakListRow(val name: String, val meta: String, val due: String, val dot: Color)
data class HistoryBar(val d: String, val fill: Color)

data class BeetView(
    val beetIntro: String,
    val readiness: String,
    val readinessLine: String,
    val garden: List<GardenNode>,
    val weakList: List<WeakListRow>,
    val history: List<HistoryBar>,
)

data class ProfileView(
    val profileIntro: String,
    val statsHeading: String,
    val anredeOpts: List<ChipOption>,
    val genderOpts: List<ChipOption>,
    val greetingPreview: String,
    val profileStats: List<Pair<String, String>>,
)

data class RatingOption(val label: String, val hint: String, val dot: Color, val bg: Color, val ring: Color, val delta: Float, val v: Int)

data class TabView(val id: String, val label: String, val fg: Color, val pill: Color, val bold: Boolean)

data class DerivedView(
    val formal: Boolean,
    val heute: HeuteView,
    val plan: PlanView,
    val themen: ThemenView,
    val topicDetail: TopicDetailView?,
    val fokus: FokusView,
    val beet: BeetView,
    val profile: ProfileView,
    val ratedMinutes: Int,
    val ratingSheetTitle: String,
    val ratings: List<RatingOption>,
    val distractQ: String,
    val distractOptions: List<String>,
    val tabs: List<TabView>,
)
