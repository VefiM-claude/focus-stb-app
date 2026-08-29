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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.PaceRow
import de.fokusstb.app.presentation.TopicDetailView
import de.fokusstb.app.presentation.TopicRow
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.RoundBadge
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun TopicDetailScreen(v: TopicDetailView, noteDraft: String, vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
    ) {
        Text(
            "‹ Alle Themen", color = Tokens.accent700, fontWeight = FontWeight.Bold, fontSize = sp(13.5f),
            modifier = Modifier.clickable { vm.closeTopicDetail() },
        )

        Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundBadge(v.tPct, 62.dp, v.tFill, v.tRing, v.tFg, fontSize = 16f)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text("RECALL", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600)
                Text(v.tMeta, fontSize = sp(13f), color = Tokens.neutral700, modifier = Modifier.padding(top = 3.dp))
            }
        }

        Text("GEBIET", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 20.dp, bottom = 7.dp))
        var nameField by remember(v.catId) { mutableStateOf(v.tName) }
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Tokens.neutral100, RoundedCornerShape(999.dp))
                .border(1.5.dp, Tokens.divider, RoundedCornerShape(999.dp))
                .padding(horizontal = 18.dp, vertical = 13.dp),
        ) {
            BasicTextField(
                value = nameField,
                onValueChange = { nameField = it; vm.renameCategory(v.catId, it) },
                textStyle = TextStyle(fontSize = sp(15f), color = Tokens.text, fontWeight = FontWeight.SemiBold),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(v.planSourceHeading, fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 22.dp, bottom = 9.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            v.tHints.forEach { h ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Tokens.accent100, RoundedCornerShape(16.dp))
                        .border(1.5.dp, Tokens.accent300, RoundedCornerShape(16.dp))
                        .padding(horizontal = 15.dp, vertical = 12.dp),
                ) {
                    Text(h.at.uppercase(), fontSize = sp(10f), fontWeight = FontWeight.Bold, color = Tokens.accent700)
                    Text(h.text, fontSize = sp(12.5f), modifier = Modifier.padding(top = 5.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TEILTHEMEN", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600)
            Text("+ = vormerken · Zeile = sofort starten", fontSize = sp(11.5f), color = Tokens.neutral600)
        }
        Column(modifier = Modifier.padding(top = 10.dp)) {
            v.tSubs.forEach { sub -> DetailSubRow(sub, vm, v.nightBudget) }
        }

        Text("Gewichtung in der Prüfung", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 22.dp, bottom = 9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            v.tWeights.forEach { w ->
                PillChip(w.label, w.bg, w.ring, Tokens.text, w.bold, fontSize = 13f, onClick = { vm.setWeight(v.catId, w.id) }, modifier = Modifier.weight(1f), paddingHorizontal = 0.dp, paddingVertical = 11.dp)
            }
        }

        Text("Wiederholungstakt", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp, bottom = 9.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            v.tPaces.forEach { p -> PaceItem(p, onClick = { vm.setPace(v.catId, p.id) }) }
        }

        Column(modifier = Modifier.padding(top = 26.dp).background(Color.Transparent)) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider))
            Text("Notizen", fontFamily = Caprasimo, fontSize = sp(20f), modifier = Modifier.padding(top = 20.dp))
            Text(
                "Kurz, in eigenen Worten, mit Paragraf. Beim nächsten Block liegen sie oben.",
                fontSize = sp(13f), color = Tokens.neutral700, modifier = Modifier.padding(top = 6.dp),
            )
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    .background(Tokens.neutral100, RoundedCornerShape(16.dp))
                    .border(1.5.dp, Tokens.divider, RoundedCornerShape(16.dp))
                    .padding(horizontal = 15.dp, vertical = 13.dp),
            ) {
                BasicTextField(
                    value = noteDraft,
                    onValueChange = { vm.setNoteDraft(it) },
                    textStyle = TextStyle(fontSize = sp(14f), color = Tokens.text, lineHeight = sp(20f)),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    decorationBox = { inner ->
                        if (noteDraft.isEmpty()) {
                            Text("z. B. § 170 Abs. 2 AO: Anlaufhemmung nur bei Erklärungspflicht …", fontSize = sp(14f), color = Tokens.neutral500)
                        }
                        inner()
                    },
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp)
                    .clickable { vm.addNote(v.catId) }
                    .background(v.addBg, RoundedCornerShape(999.dp))
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Notiz speichern", color = v.addFg, fontWeight = FontWeight.Bold, fontSize = sp(14.5f)) }

            if (v.tNotesEmpty) {
                Text(v.noteEmptyLine, fontSize = sp(13.5f), color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp))
            }

            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                v.tNotes.forEach { n ->
                    Column(modifier = Modifier.fillMaxWidth().background(Tokens.surface, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(n.at.uppercase(), fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600)
                            Text("×", fontSize = sp(15f), color = Tokens.neutral600, modifier = Modifier.clickable { vm.deleteNote(v.catId, n.id) })
                        }
                        Text(n.text, fontSize = sp(14f), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                .clickable(enabled = v.tStartTopicId != null) {
                    v.tStartTopicId?.let { vm.startTopicSession(it, v.tStartTitle, v.tStartRef, v.nightBudget, "Wiederholung") }
                }
                .background(Tokens.accent2_600, RoundedCornerShape(999.dp))
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) { Text("Jetzt Block dazu starten", color = Color.White, fontWeight = FontWeight.Bold, fontSize = sp(15f)) }
    }
}

@Composable
private fun DetailSubRow(row: TopicRow, vm: AppViewModel, nightBudget: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(30.dp).clickable { vm.toggleQueue(row.id) }
                .background(row.markBg, CircleShape).border(1.5.dp, row.markRing, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(row.mark, color = row.markFg, fontWeight = FontWeight.Bold, fontSize = sp(15f)) }

        Column(
            modifier = Modifier.weight(1f).padding(start = 11.dp)
                .clickable { vm.startTopicSession(row.id, row.name, row.ref, nightBudget, "Teilthema") },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row.name, fontWeight = FontWeight.SemiBold, fontSize = sp(13.5f))
                if (row.recommended) {
                    Text(
                        " empfohlen", fontSize = sp(9.5f), fontWeight = FontWeight.Bold, color = Tokens.accent800,
                        modifier = Modifier.padding(start = 6.dp).background(Tokens.accent200, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Text("${row.ref} · ${row.duePhrase}", fontSize = sp(11.5f), color = Tokens.neutral600, modifier = Modifier.padding(top = 2.dp))
        }
        Text(row.pct, fontWeight = FontWeight.Bold, fontSize = sp(12.5f))
    }
}

@Composable
private fun PaceItem(p: PaceRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .background(p.bg, RoundedCornerShape(16.dp))
            .border(1.5.dp, p.ring, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(p.label, fontSize = sp(14f), fontWeight = if (p.bold) FontWeight.Bold else FontWeight.Medium)
        Text(p.hint, fontSize = sp(12.5f), color = Tokens.neutral700)
    }
}
