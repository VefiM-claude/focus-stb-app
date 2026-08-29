package de.fokusstb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.BlockAction
import de.fokusstb.app.presentation.BlockRow
import de.fokusstb.app.presentation.HeuteView
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.ProgressRing
import de.fokusstb.app.ui.components.SectionCard
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun HeuteScreen(v: HeuteView, vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(v.dateLine, fontSize = sp(12f), color = Tokens.neutral600, fontWeight = FontWeight.SemiBold)
                Text(v.headline, fontFamily = Caprasimo, fontSize = sp(31f), lineHeight = sp(34f), modifier = Modifier.padding(top = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillChip(
                    label = v.streakLabel, bg = Tokens.accent200, ring = Tokens.accent200, textColor = Tokens.accent800,
                    bold = true, fontSize = 11.5f, onClick = {}, modifier = Modifier.padding(end = 8.dp),
                    paddingHorizontal = 12.dp, paddingVertical = 7.dp,
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { vm.openProfile() }
                        .background(Tokens.surface, CircleShape)
                        .border(2.dp, Tokens.accent2_400, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(v.initials, fontFamily = Caprasimo, fontSize = sp(14f), color = Tokens.accent2_800)
                }
            }
        }

        Text(v.coachLine, fontSize = sp(15f), lineHeight = sp(22f), color = Tokens.neutral700, modifier = Modifier.padding(top = 10.dp))

        SectionCard(
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp).padding(20.dp),
            bg = Tokens.surface, borderColor = null,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(pct = v.elapsedShare, size = 86.dp, stroke = 10.dp, trackColor = Tokens.neutral300, progressColor = Tokens.accent500) {
                    Box(modifier = Modifier.size(66.dp).background(Tokens.surface, CircleShape), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${v.daysLeft}", fontFamily = Caprasimo, fontSize = sp(20f))
                            Text("Tage", fontSize = sp(9f), color = Tokens.neutral600, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column(modifier = Modifier.padding(start = 18.dp)) {
                    Text(v.phaseLine, fontWeight = FontWeight.Bold, fontSize = sp(15f))
                    Text(v.examLine, fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom,
        ) {
            Text(v.todayHeading, fontFamily = Caprasimo, fontSize = sp(18f))
            Text(v.doneLabel, fontSize = sp(13f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            PillChip(
                v.blocksSource, v.blocksSourceBg, v.blocksSourceBg, v.blocksSourceFg, bold = true, fontSize = 10f, onClick = {},
                paddingHorizontal = 10.dp, paddingVertical = 3.dp,
            )
            Text(
                "Selbst wählen", color = Tokens.accent700, fontWeight = FontWeight.Bold, fontSize = sp(12.5f),
                modifier = Modifier.padding(start = 9.dp).clickable { vm.pickOwn() },
            )
        }
        Text(v.blocksHint, fontSize = sp(12.5f), color = Tokens.neutral600, modifier = Modifier.padding(bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            v.todayBlocks.forEach { block -> TodayBlockRow(block, vm) }
        }

        SectionCard(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).padding(18.dp),
            bg = Tokens.accent100, borderColor = Tokens.accent300,
        ) {
            Text("SCHWACHSTELLE", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.accent700)
            Text(v.weakestName, fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 7.dp))
            Text(v.weakestLine, fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 6.dp))
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .clickable(enabled = v.weakestTopicId != null) {
                        v.weakestTopicId?.let { vm.startTopicSession(it, v.weakestName, "", v.nightBudget, "Schwachstelle") }
                    }
                    .background(Tokens.accent, RoundedCornerShape(999.dp))
                    .padding(horizontal = 20.dp, vertical = 11.dp),
            ) {
                Text(v.weakestCta, color = Color.White, fontWeight = FontWeight.Bold, fontSize = sp(14f))
            }
        }
    }
}

@Composable
private fun TodayBlockRow(b: BlockRow, vm: AppViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (val a = b.action) {
                    is BlockAction.StartTopic -> vm.startTopicSession(a.topicId, a.fullTitle, "", a.min, a.kind)
                    is BlockAction.StartTask -> vm.startTaskSession(a.taskId)
                }
            }
            .background(Tokens.neutral100, RoundedCornerShape(16.dp))
            .border(1.5.dp, Tokens.divider, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(38.dp).background(b.dotBg, CircleShape), contentAlignment = Alignment.Center) {
            Text(b.n, color = b.dotFg, fontWeight = FontWeight.Bold, fontSize = sp(13f))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Row {
                Text(b.kind.uppercase(), fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = b.tagFg)
                Text("  · ${b.min} Min", fontSize = sp(10.5f), color = Tokens.neutral500)
            }
            Text(b.title, fontWeight = FontWeight.SemiBold, fontSize = sp(15f), modifier = Modifier.padding(top = 3.dp))
            Text(b.meta, fontSize = sp(12.5f), color = Tokens.neutral600, modifier = Modifier.padding(top = 2.dp))
        }
        Text("›", fontSize = sp(20f), color = Tokens.neutral500)
    }
}
