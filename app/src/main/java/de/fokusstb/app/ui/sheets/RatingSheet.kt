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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fokusstb.app.AppViewModel
import de.fokusstb.app.presentation.DerivedView
import de.fokusstb.app.presentation.RatingOption
import de.fokusstb.app.ui.components.sp
import de.fokusstb.app.ui.theme.Caprasimo
import de.fokusstb.app.ui.theme.Tokens

@Composable
fun RatingSheet(v: DerivedView, vm: AppViewModel) {
    Box(
        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0x6B201E1D)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Tokens.bg, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(44.dp, 4.dp).background(Tokens.neutral400, RoundedCornerShape(2.dp)))
            Text("${v.ratedMinutes} MIN PROTOKOLLIERT", fontSize = sp(10.5f), fontWeight = FontWeight.Bold, color = Tokens.neutral600, modifier = Modifier.padding(top = 18.dp))
            Text(v.ratingSheetTitle, fontFamily = Caprasimo, fontSize = sp(24f), modifier = Modifier.padding(top = 8.dp))
            Text(v.fokus.sessionTitle, fontSize = sp(13.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 8.dp))

            Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                v.ratings.forEach { r -> RatingRow(r, vm) }
            }
        }
    }
}

@Composable
private fun RatingRow(r: RatingOption, vm: AppViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { vm.rate(r.delta, r.v) }
            .background(r.bg, RoundedCornerShape(16.dp))
            .border(1.5.dp, r.ring, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(14.dp).background(r.dot, CircleShape))
        Column(modifier = Modifier.weight(1f).padding(start = 13.dp)) {
            Text(r.label, fontWeight = FontWeight.Bold, fontSize = sp(15f))
            Text(r.hint, fontSize = sp(12.5f), color = Tokens.neutral700, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
