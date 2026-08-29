package de.fokusstb.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.DerivedView
import de.fokusstb.app.presentation.ViewBuilder
import de.fokusstb.app.ui.screens.BeetScreen
import de.fokusstb.app.ui.screens.FokusScreen
import de.fokusstb.app.ui.screens.HeuteScreen
import de.fokusstb.app.ui.screens.PlanScreen
import de.fokusstb.app.ui.screens.ThemenScreen
import de.fokusstb.app.ui.screens.TopicDetailScreen
import de.fokusstb.app.ui.sheets.DistractSheet
import de.fokusstb.app.ui.sheets.ProfileSheet
import de.fokusstb.app.ui.sheets.RatingSheet
import de.fokusstb.app.ui.theme.Tokens

private val tabIcons: Map<String, ImageVector> = mapOf(
    "heute" to Icons.Filled.Home,
    "plan" to Icons.Filled.CalendarMonth,
    "themen" to Icons.Filled.MenuBook,
    "fokus" to Icons.Filled.Timer,
    "fortschritt" to Icons.Filled.Eco,
)

@Composable
fun FokusStBApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val view: DerivedView = remember(state) { ViewBuilder.build(state, viewModel.data) }

    Box(modifier = Modifier.fillMaxSize().background(Tokens.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.tab == "heute" -> HeuteScreen(view.heute, viewModel)
                    state.tab == "plan" -> PlanScreen(view.plan, viewModel)
                    state.tab == "themen" && state.openTopic == null -> ThemenScreen(view.themen, viewModel, state.search)
                    state.tab == "themen" && state.openTopic != null && view.topicDetail != null ->
                        TopicDetailScreen(view.topicDetail, state.noteDraft, viewModel)
                    state.tab == "fokus" -> FokusScreen(view.fokus, viewModel, state.running)
                    state.tab == "fortschritt" -> BeetScreen(view.beet, viewModel)
                }
            }
            BottomNav(view, viewModel)
        }

        if (state.showRating) RatingSheet(view, viewModel)
        if (state.showProfile) ProfileSheet(view, state, viewModel)
        if (state.showDistract) DistractSheet(view, viewModel)
    }
}

@Composable
private fun BottomNav(view: DerivedView, viewModel: AppViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.neutral100)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        view.tabs.forEach { tab ->
            NavItem(
                modifier = Modifier.weight(1f),
                label = tab.label,
                icon = tabIcons[tab.id] ?: Icons.Filled.Home,
                fg = tab.fg,
                pill = tab.pill,
                bold = tab.bold,
                onClick = { viewModel.goTab(tab.id) },
            )
        }
    }
}

@Composable
private fun NavItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    fg: androidx.compose.ui.graphics.Color,
    pill: androidx.compose.ui.graphics.Color,
    bold: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .asNavClickable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(26.dp)
                .background(pill, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.height(20.dp))
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, color = fg, fontSize = androidx.compose.ui.unit.TextUnit(10.5f, androidx.compose.ui.unit.TextUnitType.Sp),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(bottom = 2.dp),
        )
    }
}

private fun Modifier.asNavClickable(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(onClick = onClick)
)
