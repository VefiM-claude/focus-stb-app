package de.fokusstb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.KlausurRow
import de.fokusstb.app.presentation.PlanView
import de.fokusstb.app.presentation.UpcomingRow
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.SectionCard
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun PlanScreen(v: PlanView, vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
    ) {
        Text("Plan", fontFamily = Caprasimo, fontSize = sp(29f))
        Text(v.planIntro, fontSize = sp(14.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))

        SectionCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(top = 18.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(v.capacityHeading, fontWeight = FontWeight.Bold, fontSize = sp(14f))
                Text(v.weekTotalLabel + " / Woche", fontSize = sp(12.5f), fontWeight = FontWeight.SemiBold, color = Tokens.accent2_700)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                v.availability.forEach { d ->
                    Column(
                        modifier = Modifier.weight(1f).clickable { vm.toggleDay(d.index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                .background(d.bg, CircleShape).border(2.dp, d.ring, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text(d.d, color = d.fg, fontWeight = FontWeight.Bold, fontSize = sp(12.5f)) }
                        Text(d.minLabel, color = d.minFg, fontSize = sp(9.5f), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            Text("STANDARD PRO ABEND", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                v.budgets.forEach { b ->
                    PillChip(
                        b.label, b.bg, b.ring, Tokens.text, b.bold, fontSize = 12.5f,
                        onClick = { vm.pickBudget(b.minutes) },
                        modifier = Modifier.weight(1f), paddingHorizontal = 0.dp, paddingVertical = 9.dp,
                    )
                }
            }
            Text(v.availabilityLine, fontSize = sp(12.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 14.dp))
        }

        SectionCard(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).padding(16.dp), bg = Tokens.surface, borderColor = null) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Text("Diese Woche", fontWeight = FontWeight.Bold, fontSize = sp(14f))
                Text(v.weekDoneLabel, fontSize = sp(13f), color = Tokens.neutral700)
            }
            Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                v.week.forEach { bar ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height((bar.heightFraction * 62).dp)
                                .background(bar.fill, RoundedCornerShape(8.dp)),
                        )
                        Text(bar.d, fontSize = sp(11f), fontWeight = if (bar.bold) FontWeight.Bold else FontWeight.Medium, color = bar.fg, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }

        Text("Als nächstes", fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            v.upcoming.forEach { u -> UpcomingCard(u, vm) }
        }

        Text("Klausurtermine", fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
        Column {
            v.klausuren.forEach { k -> KlausurItem(k) }
        }
    }
}

@Composable
private fun UpcomingCard(u: UpcomingRow, vm: AppViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { vm.startTaskSession(u.taskId) }
            .background(Tokens.neutral100, RoundedCornerShape(16.dp))
            .border(1.5.dp, Tokens.divider, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(modifier = Modifier.width(44.dp)) {
            Text(u.day.uppercase(), fontSize = sp(11f), fontWeight = FontWeight.Bold, color = Tokens.neutral600)
            Text(u.time, fontFamily = Caprasimo, fontSize = sp(15f), modifier = Modifier.padding(top = 2.dp))
        }
        Box(modifier = Modifier.height(48.dp).width(1.5.dp).background(Tokens.divider))
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            PillChip(u.kind.uppercase(), u.tagBg, u.tagBg, u.tagFg, bold = true, fontSize = 10f, onClick = {}, paddingHorizontal = 9.dp, paddingVertical = 3.dp)
            Text(u.title, fontWeight = FontWeight.SemiBold, fontSize = sp(14.5f), modifier = Modifier.padding(top = 6.dp))
            Text(u.meta, fontSize = sp(12.5f), color = Tokens.neutral600, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun KlausurItem(k: KlausurRow) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(k.title, fontWeight = FontWeight.SemiBold, fontSize = sp(14f))
                Text(k.meta, fontSize = sp(12f), color = Tokens.neutral600, modifier = Modifier.padding(top = 2.dp))
            }
            Text(k.pts, fontFamily = Caprasimo, fontSize = sp(17f), color = k.fg)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider))
    }
}
