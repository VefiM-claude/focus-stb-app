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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.DerivedView
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun DistractSheet(v: DerivedView, vm: AppViewModel) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x6B201E1D)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Tokens.bg, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(44.dp, 4.dp).background(Tokens.neutral400, RoundedCornerShape(2.dp)))
            Text(v.distractQ, fontFamily = Caprasimo, fontSize = sp(23f), modifier = Modifier.padding(top = 18.dp))
            Text("Wird notiert, nicht bestraft. Die Uhr pausiert.", fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))

            de.fokusstb.app.ui.components.WrapRow(modifier = Modifier.padding(top = 18.dp)) {
                v.distractOptions.forEach { label ->
                    Box(
                        modifier = Modifier
                            .clickable { vm.pickDistraction(label) }
                            .background(Tokens.surface, RoundedCornerShape(999.dp))
                            .border(1.5.dp, Tokens.divider, RoundedCornerShape(999.dp))
                            .padding(horizontal = 17.dp, vertical = 11.dp),
                    ) { Text(label, fontSize = sp(14f), fontWeight = FontWeight.SemiBold) }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    .clickable { vm.closeDistractWithoutNote() }
                    .background(Tokens.accent, RoundedCornerShape(999.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Weiter, ohne Notiz", color = Color.White, fontWeight = FontWeight.Bold, fontSize = sp(15f)) }
        }
    }
}
