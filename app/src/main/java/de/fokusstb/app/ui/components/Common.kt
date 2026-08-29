package de.fokusstb.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import de.fokusstb.app.ui.theme.Tokens

fun sp(v: Float): TextUnit = TextUnit(v, TextUnitType.Sp)

/** Minimal wrap-into-rows layout (avoids depending on the experimental FlowRow API) —
 *  used for the small, fixed sets of chips (distraction reasons, etc). */
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 9.dp,
    verticalSpacing: Dp = 9.dp,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.layout.Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hGap = horizontalSpacing.roundToPx()
        val vGap = verticalSpacing.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        val maxWidth = constraints.maxWidth

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0
        placeables.forEach { p ->
            val extra = if (currentRow.isEmpty()) 0 else hGap
            if (currentWidth + extra + p.width > maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentWidth += (if (currentRow.isEmpty()) 0 else hGap) + p.width
            currentRow.add(p)
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val totalHeight = rows.sumOf { row -> row.maxOf { it.height } } + vGap * (rows.size - 1).coerceAtLeast(0)
        layout(maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { p ->
                    p.placeRelative(x, y)
                    x += p.width + hGap
                }
                y += rowHeight + vGap
            }
        }
    }
}

/** A rounded, tinted "pill" used everywhere for filter chips, day toggles, weight/pace picks. */
@Composable
fun PillChip(
    label: String,
    bg: Color,
    ring: Color,
    textColor: Color = Tokens.text,
    bold: Boolean = false,
    fontSize: Float = 12.5f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingHorizontal: Dp = 14.dp,
    paddingVertical: Dp = 9.dp,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.5.dp, ring, RoundedCornerShape(999.dp))
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label, color = textColor, fontSize = sp(fontSize),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

/** A rounded surface container, mirroring the prototype's repeated card treatment. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    bg: Color = Tokens.neutral100,
    borderColor: Color? = Tokens.divider,
    radius: Dp = Tokens.radiusLg.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(radius))
            .then(if (borderColor != null) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(radius)) else Modifier),
        content = content,
    )
}

/** A circular ring gauge (replaces the CSS conic-gradient rings). */
@Composable
fun ProgressRing(
    pct: Float,
    size: Dp,
    stroke: Dp,
    trackColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = stroke.toPx()
            drawArc(
                color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = Size(size.toPx() - strokePx, size.toPx() - strokePx),
                style = Stroke(width = strokePx),
            )
            if (pct > 0f) {
                drawArc(
                    color = progressColor, startAngle = -90f, sweepAngle = 360f * pct.coerceIn(0f, 1f), useCenter = false,
                    topLeft = Offset(strokePx / 2, strokePx / 2),
                    size = Size(size.toPx() - strokePx, size.toPx() - strokePx),
                    style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** A round badge showing e.g. "62%" — used for topic/category avatars. */
@Composable
fun RoundBadge(
    text: String,
    size: Dp,
    fill: Color,
    ring: Color,
    textColor: Color = Tokens.text,
    fontSize: Float = 13f,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(fill, CircleShape)
            .border(2.5.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = textColor, fontSize = sp(fontSize), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

/** The growing-sprout illustration on the Fokus screen — a simplified stem + two leaves + bloom,
 *  sized by session progress (0f..1f), mirroring the prototype's plantEl(). */
@Composable
fun GrowingPlant(pct: Float, modifier: Modifier = Modifier) {
    val stemColor = Tokens.accent2_600
    val leafColor = Tokens.accent2_500
    val bloomColor = Tokens.accent500
    val bloomHalo = Tokens.accent200
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stemHeight = (26 + pct * (h - 26)).coerceAtMost(h)
        val stemWidth = 7.dp.toPx()
        val cx = w / 2f

        // stem
        drawRect(
            color = stemColor,
            topLeft = Offset(cx - stemWidth / 2, h - stemHeight),
            size = Size(stemWidth, stemHeight),
        )

        fun leafAlpha(threshold: Float): Float = ((pct - threshold) / 0.14f).coerceIn(0f, 1f)

        // two leaves off the stem at increasing heights
        val leafSpecs = listOf(
            Triple(0.18f, -1, 0.32f),
            Triple(0.40f, 1, 0.56f),
            Triple(0.62f, -1, 0.80f),
        )
        leafSpecs.forEach { (threshold, side, yFrac) ->
            val a = leafAlpha(threshold)
            if (a > 0f) {
                val leafW = 34.dp.toPx() * a
                val leafH = 20.dp.toPx() * a
                val y = h - stemHeight * (1f - yFrac)
                val cxLeaf = cx + side * (leafW / 2.2f)
                drawOval(color = leafColor.copy(alpha = 0.92f), topLeft = Offset(cxLeaf - leafW / 2, y - leafH / 2), size = Size(leafW, leafH))
            }
        }

        // bloom
        if (pct > 0.97f) {
            val bloomR = 15.dp.toPx()
            val bloomCenter = Offset(cx, h - stemHeight)
            drawCircle(color = bloomHalo, radius = bloomR + 7.dp.toPx(), center = bloomCenter)
            drawCircle(color = bloomColor, radius = bloomR, center = bloomCenter)
        }
    }
}
