package de.fokusstb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.BeetView
import de.fokusstb.app.presentation.GardenNode
import de.fokusstb.app.ui.components.RoundBadge
import de.fokusstb.app.ui.components.SectionCard
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun BeetScreen(v: BeetView, vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
    ) {
        Text("Beet", fontFamily = Caprasimo, fontSize = sp(29f))
        Text(v.beetIntro, fontSize = sp(14.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))

        SectionCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 20.dp, vertical = 18.dp), bg = Tokens.surface, borderColor = null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(v.readiness, fontFamily = Caprasimo, fontSize = sp(38f), color = Tokens.accent700)
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Prüfungsreife", fontWeight = FontWeight.Bold, fontSize = sp(14f))
                    Text(v.readinessLine, fontSize = sp(13f), color = Tokens.neutral700, modifier = Modifier.padding(top = 3.dp))
                }
            }
        }

        SectionCard(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).padding(horizontal = 16.dp, vertical = 22.dp),
            bg = Tokens.neutral100,
        ) {
            v.garden.chunked(3).forEach { rowNodes ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                ) {
                    rowNodes.forEach { node -> GardenItem(node, vm) }
                }
            }
        }

        Text("Zuerst dran", fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
        Column {
            v.weakList.forEach { w ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(w.dot, CircleShape))
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(w.name, fontWeight = FontWeight.SemiBold, fontSize = sp(14f))
                            Text(w.meta, fontSize = sp(12f), color = Tokens.neutral600, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(w.due, fontWeight = FontWeight.Bold, fontSize = sp(12.5f), color = w.dot)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider))
                }
            }
        }

        Text("Letzte Selbsteinschätzungen", fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            v.history.forEach { h ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.fillMaxWidth().height(34.dp).background(h.fill, RoundedCornerShape(8.dp)))
                    Text(h.d, fontSize = sp(9.5f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(Tokens.accent2_600, "saß")
            LegendDot(Tokens.accent2_300, "wackelig")
            LegendDot(Tokens.accent400, "nicht verstanden")
        }
    }
}

@Composable
private fun GardenItem(node: GardenNode, vm: AppViewModel) {
    Column(
        modifier = Modifier.width(88.dp).clickable { vm.openTopicDetail(node.catId) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RoundBadge(node.pct, node.sizeDp.dp, node.fill, node.ring, node.fg, fontSize = 13f)
        Text(
            node.short, fontSize = sp(11.5f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral800,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = sp(11.5f), color = Tokens.neutral700, modifier = Modifier.padding(start = 6.dp))
    }
}
