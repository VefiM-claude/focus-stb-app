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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.CategoryRow
import de.fokusstb.app.presentation.ThemenView
import de.fokusstb.app.presentation.TopicRow
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.RoundBadge
import de.fokusstb.app.ui.components.SectionCard
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun ThemenScreen(v: ThemenView, vm: AppViewModel, search: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
    ) {
        Text("Themen", fontFamily = Caprasimo, fontSize = sp(29f))
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                buildString { append(v.topicCountLine); append(". "); append(v.themenIntro); append(" ") },
                fontSize = sp(14.5f), color = Tokens.neutral700,
            )
        }
        Text(v.queueLabel, fontSize = sp(14.5f), fontWeight = FontWeight.Bold, color = Tokens.accent2_700)

        SectionCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("ALLE TEILTHEMEN", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600)
                Text(v.allSubsCount, fontSize = sp(11f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600)
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp)
                    .background(Tokens.bg, RoundedCornerShape(999.dp))
                    .border(1.5.dp, Tokens.divider, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            ) {
                BasicTextField(
                    value = search,
                    onValueChange = { vm.setSearch(it) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = sp(13.5f), color = Tokens.text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (search.isEmpty()) {
                            Text("Suchen: § 6b, Rückstellungen, Reihengeschäft …", fontSize = sp(13.5f), color = Tokens.neutral500)
                        }
                        inner()
                    },
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                v.filters.forEach { f ->
                    PillChip(
                        f.label, f.bg, f.ring, Tokens.text, f.bold, fontSize = 12f,
                        onClick = { vm.setSubFilter(f.id) }, modifier = Modifier.weight(1f),
                        paddingHorizontal = 0.dp, paddingVertical = 8.dp,
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 8.dp)) {
                v.allSubs.forEach { row -> TopicListItem(row, vm, v.nightBudget) }
            }
        }

        Text("Nach Gebiet", fontFamily = Caprasimo, fontSize = sp(19f), modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            v.topicList.forEach { cat -> CategoryListItem(cat, vm) }
        }
    }
}

@Composable
private fun TopicListItem(row: TopicRow, vm: AppViewModel, nightBudget: Int) {
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
            Text("${row.ref} · ${row.cat}", fontSize = sp(11.5f), color = Tokens.neutral600, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(row.pct, fontWeight = FontWeight.Bold, fontSize = sp(12.5f))
            Text(row.due, fontSize = sp(10.5f), fontWeight = FontWeight.SemiBold, color = row.dueFg, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun CategoryListItem(cat: CategoryRow, vm: AppViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { vm.openTopicDetail(cat.id) }
            .background(Tokens.neutral100, RoundedCornerShape(16.dp))
            .border(1.5.dp, Tokens.divider, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundBadge(cat.pct, 44.dp, cat.fill, cat.ring, fontSize = 13f)
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(cat.name, fontWeight = FontWeight.SemiBold, fontSize = sp(15f))
            Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    cat.weight.uppercase(), fontSize = sp(10f), fontWeight = FontWeight.Bold, color = cat.weightFg,
                    modifier = Modifier.background(cat.weightBg, RoundedCornerShape(999.dp)).padding(horizontal = 9.dp, vertical = 2.dp),
                )
                Text(cat.meta, fontSize = sp(12f), color = Tokens.neutral600, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Text("›", fontSize = sp(20f), color = Tokens.neutral500)
    }
}
