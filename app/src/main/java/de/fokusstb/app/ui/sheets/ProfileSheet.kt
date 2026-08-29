package de.fokusstb.app.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.data.AppState
import de.fokusstb.app.presentation.DerivedView
import de.fokusstb.app.ui.components.PillChip
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun ProfileSheet(v: DerivedView, state: AppState, vm: AppViewModel) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x6B201E1D)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .background(Tokens.bg, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(44.dp, 4.dp).background(Tokens.neutral400, RoundedCornerShape(2.dp)))
            Text("Wer lernt hier?", fontFamily = Caprasimo, fontSize = sp(24f), modifier = Modifier.padding(top = 18.dp))
            Text(v.profile.profileIntro, fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))

            Text("NAME", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 20.dp, bottom = 7.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Tokens.neutral100, RoundedCornerShape(999.dp))
                    .border(1.5.dp, Tokens.divider, RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 13.dp),
            ) {
                BasicTextField(
                    value = state.profile.name,
                    onValueChange = { vm.setProfileName(it) },
                    textStyle = TextStyle(fontSize = sp(15f), color = Tokens.text, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (state.profile.name.isEmpty()) Text("Vorname oder Nachname", fontSize = sp(15f), color = Tokens.neutral500)
                        inner()
                    },
                )
            }

            Text("ANREDE", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp, bottom = 9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                v.profile.anredeOpts.forEach { a ->
                    PillChip(a.label, a.bg, a.ring, Tokens.text, a.bold, fontSize = 13.5f, onClick = { vm.setAnrede(a.id) }, modifier = Modifier.weight(1f), paddingHorizontal = 0.dp, paddingVertical = 11.dp)
                }
            }

            Text("FORM FÜR „FRAU / HERR\" BEI SIE", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp, bottom = 9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                v.profile.genderOpts.forEach { g ->
                    PillChip(g.label, g.bg, g.ring, Tokens.text, g.bold, fontSize = 12.5f, onClick = { vm.setGender(g.id) }, modifier = Modifier.weight(1f), paddingHorizontal = 0.dp, paddingVertical = 11.dp)
                }
            }
            Text(v.profile.greetingPreview, fontSize = sp(13f), fontWeight = FontWeight.SemiBold, color = Tokens.accent2_700, modifier = Modifier.padding(top = 12.dp))

            Text(v.profile.statsHeading.uppercase(), fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
            Column {
                v.profile.profileStats.forEach { (k, value) ->
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(k, fontSize = sp(13.5f), color = Tokens.neutral700)
                            Text(value, fontFamily = Caprasimo, fontSize = sp(15f))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Tokens.divider))
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp)
                    .clickable { vm.closeProfile() }
                    .background(Tokens.accent, RoundedCornerShape(999.dp))
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Passt so", color = Color.White, fontWeight = FontWeight.Bold, fontSize = sp(15f)) }

            Text(
                "Alle lokalen Daten auf diesem Gerät löschen", fontSize = sp(13f), fontWeight = FontWeight.SemiBold, color = Tokens.neutral600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).clickable { vm.resetLocal() },
            )
        }
    }
}
