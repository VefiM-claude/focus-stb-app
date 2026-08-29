package de.fokusstb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import de.fokusstb.app.presentation.FokusView
import de.fokusstb.app.ui.components.GrowingPlant
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.ProgressRing
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun FokusScreen(v: FokusView, vm: AppViewModel, running: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PillChip(v.sessionKind.uppercase(), Tokens.accent2_200, Tokens.accent2_200, Tokens.accent2_800, bold = true, fontSize = 10.5f, onClick = {}, paddingHorizontal = 12.dp, paddingVertical = 5.dp)
            Text(v.pauseLabel, fontSize = sp(12.5f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600)
        }
        Text(v.sessionTitle, fontFamily = Caprasimo, fontSize = sp(22f), modifier = Modifier.padding(top = 12.dp))

        Box(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), contentAlignment = Alignment.Center) {
            ProgressRing(pct = v.pct, size = 266.dp, stroke = 15.dp, trackColor = Tokens.neutral300, progressColor = Tokens.accent2_500) {
                Box(
                    modifier = Modifier.size(236.dp).background(Tokens.neutral100, CircleShape),
                ) {
                    GrowingPlant(
                        pct = v.pct,
                        modifier = Modifier.align(Alignment.BottomCenter).size(120.dp, 130.dp).padding(bottom = 6.dp),
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 34.dp),
                    ) {
                        Text(v.clock, fontFamily = Caprasimo, fontSize = sp(44f))
                        Text(v.growthLabel.uppercase(), fontSize = sp(11f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.weight(1f)
                    .clickable { vm.toggleRun(!running) }
                    .background(v.runBg, RoundedCornerShape(999.dp))
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text(v.runLabel, color = v.runFg, fontWeight = FontWeight.Bold, fontSize = sp(15f)) }
            Box(
                modifier = Modifier.size(56.dp)
                    .clickable { vm.openDistract() }
                    .border(1.5.dp, Tokens.accent300, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("!", fontSize = sp(19f), color = Tokens.accent700) }
        }
        Text(
            "Block früher beenden und bewerten", fontSize = sp(13.5f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600,
            modifier = Modifier.padding(top = 10.dp).clickable { vm.finishNow() },
        )

        Column(modifier = Modifier.padding(top = 20.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider))
            Text("ABLENKUNGSPROTOKOLL", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 16.dp))
            Text(v.distractLine, fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                v.distractions.forEach { d ->
                    PillChip("${d.label} · ${d.at}", Tokens.accent200, Tokens.accent200, Tokens.accent800, fontSize = 12f, onClick = {}, paddingHorizontal = 11.dp, paddingVertical = 5.dp)
                }
            }
        }
    }
}

