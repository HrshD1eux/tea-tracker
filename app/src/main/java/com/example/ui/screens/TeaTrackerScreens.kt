package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateUtils
import com.example.data.TeaRecord
import com.example.ui.TeaViewModel
import com.example.BuildConfig
import java.util.Calendar

// ==========================================
// CUSTOM VECTOR ART FOR HIGH ETHICAL CRAFT
// ==========================================

@Composable
fun DecorativeTeaCupIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Cup Body
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(w * 0.8f, h * 0.7f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.15f)
        )

        // Handle
        drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 180f,
            useCenter = false,
            size = Size(w * 0.3f, h * 0.4f),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.25f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Saucer Plate
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.85f),
            size = Size(w * 0.75f, h * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun DecorativeBiscuitIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = w * 0.45f

        // Cookie Circle Shape
        drawCircle(
            color = color,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)
        )

        // Scalloped visual ridges
        val dots = 8
        for (i in 0 until dots) {
            val angle = (360f / dots) * i
            val rad = Math.toRadians(angle.toDouble())
            val dx = (w / 2f + Math.cos(rad) * (radius * 0.82f)).toFloat()
            val dy = (h / 2f + Math.sin(rad) * (radius * 0.82f)).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.5f),
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(dx, dy)
            )
        }

        // Choco Chips
        val chips = listOf(
            androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.4f),
            androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.35f),
            androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.6f),
            androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.65f),
            androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.62f)
        )
        for (chip in chips) {
            drawCircle(
                color = Color(0xFF5D4037), // Dark Chocolate brown chip
                radius = 3.dp.toPx(),
                center = chip
            )
        }
    }
}

// ==========================================
// CORE BAR CHART COMPONENT
// ==========================================

@Composable
fun TeaStackedBarChart(
    data: List<TeaRecord>,
    isWeekly: Boolean, // True: last 7 days. False: current month distribution
    selectedMonth: String = DateUtils.getCurrentMonthYearString(),
    primaryColor: Color = Color(0xFF7D5233),
    secondaryColor: Color = Color(0xFFF4A261)
) {
    // Compile values
    val chartData = remember(data, isWeekly, selectedMonth) {
        if (isWeekly) {
            // Get last 7 entries
            data.take(7).reversed().map { r ->
                Pair(DateUtils.getShortDate(r.date), r)
            }
        } else {
            // Group by formatted months or list current month entries
            data.filter { it.date.startsWith(selectedMonth) }.reversed().map { r ->
                Pair(DateUtils.getShortDate(r.date), r)
            }
        }
    }

    if (chartData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFEFEBE9), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No Data Info",
                    tint = Color(0xFF8D6E63).copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No logs available to generate statistics charts.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFA1887F)
                )
            }
        }
        return;
    }

    val maxTotal = remember(chartData) {
        chartData.maxOfOrNull { it.second.totalTeaCount } ?: 5
    }
    val maxScaledVal = if (maxTotal == 0) 5 else maxTotal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xFFEFEBE9), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(primaryColor, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Plain Tea", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5D4037))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(10.dp).background(secondaryColor, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tea with Biscuit", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5D4037))
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = maxWidth
            val height = maxHeight

            // Horizontal Grid Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lines = 3
                for (i in 0..lines) {
                    val ratio = i.toFloat() / lines
                    val y = size.height * (1f - ratio)
                    drawLine(
                        color = Color(0xFFEFEBE9).copy(alpha = 0.8f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEach { (label, record) ->
                    val total = record.totalTeaCount
                    val totalProportion = total.toFloat() / maxScaledVal

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Value Text above the bars
                        Text(
                            text = if (total > 0) total.toString() else "0",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Stacked Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.45f)
                                .fillMaxHeight(totalProportion * 0.85f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        ) {
                            if (total > 0) {
                                val plainRatio = record.teaCount.toFloat() / total
                                val biscuitRatio = record.biscuitTeaCount.toFloat() / total

                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Biscuit tea on Top
                                    if (biscuitRatio > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(biscuitRatio)
                                                .background(secondaryColor)
                                        )
                                    }
                                    // Plain tea on Bottom
                                    if (plainRatio > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(plainRatio)
                                                .background(primaryColor)
                                        )
                                    }
                                }
                            } else {
                                // Empty state bar placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFEFEBE9).copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label Footer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            chartData.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA1887F),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: DASHBOARD SCREEN
// ==========================================

@Composable
fun DashboardScreen(
    viewModel: TeaViewModel,
    modifier: Modifier = Modifier
) {
    val todayRecord by viewModel.todayRecord.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val teaPrice by viewModel.teaPrice.collectAsState()
    val biscuitTeaPrice by viewModel.biscuitTeaPrice.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val totalPlainToday = todayRecord?.teaCount ?: 0
    val totalBiscuitToday = todayRecord?.biscuitTeaCount ?: 0
    val grandTotalToday = todayRecord?.totalTeaCount ?: 0

    // Quote engine based on current local hours
    val quote = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Start with a soothing warm brew to ignite your morning! ☕"
            in 12..16 -> "Time for an afternoon milk tea break! Complete with cookies 🍪"
            in 17..20 -> "Unwind from a long day with a cozy evening tea. ☕"
            else -> "A warm nightly cup of herbal tea eases your sleep. 💤"
        }
    }

    // Key statistics computed from repository state
    val (monthlyPlainTotal, monthlyBiscuitTotal) = remember(allRecords) {
        val key = DateUtils.getCurrentMonthYearString()
        val currentMonthMatches = allRecords.filter { it.date.startsWith(key) }
        val plain = currentMonthMatches.sumOf { it.teaCount }
        val biscuit = currentMonthMatches.sumOf { it.biscuitTeaCount }
        Pair(plain, biscuit)
    }

    val dailyAvg = remember(allRecords) {
        val key = DateUtils.getCurrentMonthYearString()
        val currentMonthMatches = allRecords.filter { it.date.startsWith(key) }
        if (currentMonthMatches.isEmpty()) 0.0 else (currentMonthMatches.sumOf { it.totalTeaCount }).toDouble() / currentMonthMatches.size
    }

    val peakDaySum = remember(allRecords) {
        val key = DateUtils.getCurrentMonthYearString()
        val currentMonthMatches = allRecords.filter { it.date.startsWith(key) }
        currentMonthMatches.maxOfOrNull { it.totalTeaCount } ?: 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Quote Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "TEA INFUSION DASHBOARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        quote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Circular dynamic Tracker section with beautiful clean minimalist outline widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "TODAY TEA COUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular gauge
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                        // Base trail
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0xFFEFEBE9).copy(alpha = 0.6f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Progress gauge (capped at 10 cups as 100%)
                        val arcPercentage = (grandTotalToday.toFloat() / 10f).coerceAtMost(1f)
                        val accentProgressColor = Color(0xFF7D5233)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (arcPercentage > 0) {
                                drawArc(
                                    color = accentProgressColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f * arcPercentage,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA1887F),
                                letterSpacing = 1.1.sp
                            )
                            Text(
                                "$grandTotalToday",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5D4037)
                                )
                            )
                            Text(
                                "CUPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E63),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detailed metrics split-row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFDF8F3),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, Color(0xFFEFEBE9), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DecorativeTeaCupIcon(modifier = Modifier.size(20.dp), color = Color(0xFF7D5233))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "$totalPlainToday",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF5D4037)
                                )
                            }
                            Text("Plain Tea", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1887F))
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFFEFEBE9))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DecorativeBiscuitIcon(modifier = Modifier.size(20.dp), color = Color(0xFFF4A261))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "$totalBiscuitToday",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF5D4037)
                                )
                            }
                            Text("With Biscuit", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1887F))
                        }
                    }
                }
            }
        }

        // Tappable Actions Bottom Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: Plain Tea + 1
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .testTag("btn_add_tea")
                        .clickable { viewModel.addTeaForToday() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7D5233)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DecorativeTeaCupIcon(modifier = Modifier.size(28.dp), color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "TEA +1",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.1.sp
                        )
                    }
                }

                // Button 2: Biscuit Tea + 1
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(84.dp)
                        .testTag("btn_add_biscuit")
                        .clickable { viewModel.addBiscuitTeaForToday() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4A261)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DecorativeBiscuitIcon(modifier = Modifier.size(28.dp), color = Color(0xFF5D4037))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "TEA + BISCUIT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037),
                            letterSpacing = 1.1.sp
                        )
                    }
                }
            }
        }

        // Side-by-side Monthly Summary Cards matching the clean layouts of the HTML specs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFE6BEA5).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFEBE0D8), RoundedCornerShape(28.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            "MONTHLY TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${monthlyPlainTotal + monthlyBiscuitTotal} Cups",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$currencySymbol${String.format("%.2f", (monthlyPlainTotal * teaPrice) + (monthlyBiscuitTotal * biscuitTeaPrice))}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7D5233)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "☕ $monthlyPlainTotal  |  🍪 $monthlyBiscuitTotal",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFFA1887F)
                        )
                    }
                }

                // Daily Avg Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFD7CCC8).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFDFD6D3), RoundedCornerShape(28.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            "DAILY AVG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            String.format("%.1f", dailyAvg),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Peak: $peakDaySum cups",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFFA1887F)
                        )
                    }
                }
            }
        }

        // Recent Timeline Headers
        item {
            Text(
                "RECENT HISTORY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D6E63),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Mini preview of historic logs
        val recentLogs = allRecords.take(3)
        if (recentLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, Color(0xFFEFEBE9), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tracking logs recorded yet. Quick tap Tea +1 above to start logging!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFA1887F)
                    )
                }
            }
        } else {
            items(recentLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Circular Calendar Icon with light background
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDF8F3))
                                    .border(1.dp, Color(0xFFEFEBE9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📅", fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    DateUtils.getFormattedDate(log.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    "${log.teaCount} Tea · ${log.biscuitTeaCount} Biscuit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA1887F)
                                )
                            }
                        }

                        // Total Count display
                        Text(
                            "${log.totalTeaCount} Total",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ==========================================
// SCREEN 2: HISTORY SCREEN
// ==========================================

@Composable
fun HistoryScreen(
    viewModel: TeaViewModel,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allRecords.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "CALENDAR HISTORY LOGS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8D6E63),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (allRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Empty History",
                        tint = Color(0xFFA1887F).copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your log book is currently empty.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Track tea using the dashboard or install the widget to record cups right on your home screen!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFA1887F)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allRecords) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    DateUtils.getFormattedDate(log.date),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    log.day,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFA1887F)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Plain Tea Count Value
                                BadgeWithDetails(
                                    count = log.teaCount,
                                    icon = { DecorativeTeaCupIcon(modifier = Modifier.size(16.dp), color = Color(0xFF7D5233)) },
                                    containerColor = Color(0xFFEFEBE9),
                                    textColor = Color(0xFF5D4037)
                                )

                                // Biscuits Tea Count Value
                                BadgeWithDetails(
                                    count = log.biscuitTeaCount,
                                    icon = { DecorativeBiscuitIcon(modifier = Modifier.size(16.dp), color = Color(0xFFF4A261)) },
                                    containerColor = Color(0xFFFDF0E6),
                                    textColor = Color(0xFF5D4037)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun BadgeWithDetails(
    count: Int,
    icon: @Composable () -> Unit,
    containerColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

// ==========================================
// SCREEN 3: ANALYTICS SCREEN
// ==========================================

@Composable
fun AnalyticsScreen(
    viewModel: TeaViewModel,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val teaPrice by viewModel.teaPrice.collectAsState()
    val biscuitTeaPrice by viewModel.biscuitTeaPrice.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    // Calculate Analytics summaries based on selected month
    val availableMonths = remember(allRecords) {
        val currentMonth = DateUtils.getCurrentMonthYearString()
        val recordMonths = allRecords.map { DateUtils.getMonthYearString(it.date) }
        (recordMonths + currentMonth).distinct().sortedDescending()
    }

    var selectedMonth by remember(availableMonths) {
        mutableStateOf(availableMonths.firstOrNull() ?: DateUtils.getCurrentMonthYearString())
    }

    val selectedMonthRecords = remember(allRecords, selectedMonth) {
        allRecords.filter { it.date.startsWith(selectedMonth) }
    }

    val totalSelectedMonth = remember(selectedMonthRecords) {
        selectedMonthRecords.sumOf { it.totalTeaCount }
    }

    val plainSelectedMonth = remember(selectedMonthRecords) {
        selectedMonthRecords.sumOf { it.teaCount }
    }

    val biscuitSelectedMonth = remember(selectedMonthRecords) {
        selectedMonthRecords.sumOf { it.biscuitTeaCount }
    }

    val costSelectedMonth = remember(plainSelectedMonth, biscuitSelectedMonth, teaPrice, biscuitTeaPrice) {
        (plainSelectedMonth * teaPrice) + (biscuitSelectedMonth * biscuitTeaPrice)
    }

    val averageSelectedMonth = remember(selectedMonthRecords) {
        if (selectedMonthRecords.isEmpty()) 0.0 else totalSelectedMonth.toDouble() / selectedMonthRecords.size
    }

    val highestDayRecord = remember(selectedMonthRecords) {
        selectedMonthRecords.maxByOrNull { it.totalTeaCount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "MONTHLY STATISTICS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D6E63),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Month Selector Dropdown
        item {
            var dropdownExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dropdownExpanded = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Month",
                                tint = Color(0xFF7D5233),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SELECT MONTH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8D6E63),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                                Text(
                                    text = DateUtils.formatMonthYear(selectedMonth),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand month selector",
                            tint = Color(0xFF8D6E63),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFFEFEBE9), RoundedCornerShape(16.dp))
                ) {
                    availableMonths.forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = DateUtils.formatMonthYear(month),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF5D4037)
                                )
                            },
                            onClick = {
                                selectedMonth = month
                                dropdownExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Monthly Expenditure Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0E6)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF4A261).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF4A261).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💰", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "MONTHLY EXPENDITURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8D6E63),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$currencySymbol${String.format("%.2f", costSelectedMonth)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Based on: $plainSelectedMonth plain ($currencySymbol${String.format("%.2f", plainSelectedMonth * teaPrice)}) + $biscuitSelectedMonth biscuit ($currencySymbol${String.format("%.2f", biscuitSelectedMonth * biscuitTeaPrice)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1887F)
                        )
                    }
                }
            }
        }

        // Stats summary cards - Styled with minimalist colors and container outlines
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Count Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6BEA5).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE6BEA5).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "TOTAL MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8D6E63),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "$totalSelectedMonth",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            "Cups total",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1887F)
                        )
                    }
                }

                // Average Daily Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD7CCC8).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFD7CCC8).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "DAILY AVERAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8D6E63),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            String.format("%.1f", averageSelectedMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            "Cups / Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1887F)
                        )
                    }
                }
            }
        }

        // Highest cup log card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDF8F3))
                            .border(1.dp, Color(0xFFEFEBE9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Highest record cup",
                            tint = Color(0xFF7D5233),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "HIGHEST TEA LOGGED DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA1887F),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (highestDayRecord != null) {
                            Text(
                                "${highestDayRecord.totalTeaCount} Cups on ${DateUtils.getShortDate(highestDayRecord.date)} (${highestDayRecord.day})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                        } else {
                            Text(
                                "No logs registered this month.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1887F)
                            )
                        }
                    }
                }
            }
        }

        // Section 1 Graph: Weekly Trend
        item {
            Column {
                Text(
                    "WEEKLY TEA TREND (LAST 7 PERIODS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8D6E63),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TeaStackedBarChart(
                    data = allRecords,
                    isWeekly = true,
                    primaryColor = Color(0xFF7D5233),
                    secondaryColor = Color(0xFFF4A261)
                )
            }
        }

        // Section 2 Graph: Monthly Distribution
        item {
            Column {
                Text(
                    "RECORD PATTERNS FOR ${DateUtils.formatMonthYear(selectedMonth).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8D6E63),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TeaStackedBarChart(
                    data = allRecords,
                    isWeekly = false,
                    selectedMonth = selectedMonth,
                    primaryColor = Color(0xFF7D5233),
                    secondaryColor = Color(0xFFF4A261)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// SCREEN 4: SETTINGS SCREEN
// ==========================================

@Composable
fun SettingsScreen(
    viewModel: TeaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val teaPriceSetting by viewModel.teaPrice.collectAsState()
    val biscuitPriceSetting by viewModel.biscuitTeaPrice.collectAsState()
    val currencySetting by viewModel.currencySymbol.collectAsState()

    var tempTeaPrice by remember(teaPriceSetting) { mutableStateOf(teaPriceSetting.toString()) }
    var tempBiscuitPrice by remember(biscuitPriceSetting) { mutableStateOf(biscuitPriceSetting.toString()) }
    var tempCurrency by remember(currencySetting) { mutableStateOf(currencySetting) }

    var showResetTodayDialog by remember { mutableStateOf(false) }
    var showResetAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "CONFIGURATION & UTILITIES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8D6E63),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tea Pricing Settings Card Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Tea Pricing Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Configure unit prices to calculate and monitor your monthly expenditures.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1887F)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempTeaPrice,
                        onValueChange = { tempTeaPrice = it },
                        label = { Text("Plain Tea") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Text(tempCurrency, color = Color(0xFF7D5233), fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7D5233),
                            focusedLabelColor = Color(0xFF7D5233)
                        )
                    )

                    OutlinedTextField(
                        value = tempBiscuitPrice,
                        onValueChange = { tempBiscuitPrice = it },
                        label = { Text("With Biscuit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.1f),
                        leadingIcon = { Text(tempCurrency, color = Color(0xFF7D5233), fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7D5233),
                            focusedLabelColor = Color(0xFF7D5233)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tempCurrency,
                    onValueChange = { if (it.length <= 3) tempCurrency = it },
                    label = { Text("Currency Symbol") },
                    placeholder = { Text("e.g. $, ₹, £, €") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D5233),
                        focusedLabelColor = Color(0xFF7D5233)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val tPrice = tempTeaPrice.toFloatOrNull() ?: 1.50f
                        val bPrice = tempBiscuitPrice.toFloatOrNull() ?: 2.50f
                        viewModel.updatePrices(tPrice, bPrice, tempCurrency)
                        Toast.makeText(context, "Pricing updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D5233))
                ) {
                    Text("Save Price Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Notification Settings Card Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Drink Reminders",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Send morning (8:30 AM) and evening (5:30 PM) log prompts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA1887F)
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF7D5233),
                            uncheckedThumbColor = Color(0xFFA1887F),
                            uncheckedTrackColor = Color(0xFFEFEBE9)
                        )
                    )
                }
            }
        }

        // Export Logs Section Card Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Export Data Records",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Download and compile log tables to a standard CSV file for physical backups or analyses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1887F)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val shareIntent = viewModel.exportRecordsToCSV(context)
                        if (shareIntent != null) {
                            context.startActivity(Intent.createChooser(shareIntent, "Save or Export CSV Logs"))
                        } else {
                            Toast.makeText(context, "No tea logs recorded to export!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D5233))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Export Logs to CSV",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }
                }
            }
        }

        // RESET ACTIONS CARD BLOCK
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Dangerous System Actions",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Perform resets below. These actions cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1887F)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Reset Today Option Button
                OutlinedButton(
                    onClick = { showResetTodayDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7D5233)),
                    border = BorderStroke(1.dp, Color(0xFF7D5233))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Today")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Reset Today's Logs",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }

                // Reset All Option Button
                Button(
                    onClick = { showResetAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset All")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Wipe All Logs History",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }
            }
        }

        // ABOUT SECTION CARD BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon Placeholder / Tea Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDF0E6)),
                    contentAlignment = Alignment.Center
                ) {
                    DecorativeTeaCupIcon(modifier = Modifier.size(32.dp), color = Color(0xFF7D5233))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // App Name
                Text(
                    "Tea Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5D4037)
                )
                
                // Version info
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1887F),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Developer / Credit
                Text(
                    "Developed by hrshd1eux",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8D6E63)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Short description
                Text(
                    "A lightweight tracker for daily tea and biscuit consumption with instant-tap widgets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1887F),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // ==========================================
    // CONFIRMATION DIALOGS
    // ==========================================

    if (showResetTodayDialog) {
        AlertDialog(
            onDismissRequest = { showResetTodayDialog = false },
            title = { Text("Confirm Day Reset", color = Color(0xFF5D4037)) },
            text = { Text("Are you sure you want to clear your tea consumption logs recorded specifically for today? This action resets today's stats back to 0.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToday()
                        showResetTodayDialog = false
                        Toast.makeText(context, "Today's log resetting successful!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear Today", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetTodayDialog = false }) {
                    Text("Cancel", color = Color(0xFF8D6E63))
                }
            }
        )
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("Confirm Entire Wipeout!", color = MaterialTheme.colorScheme.error) },
            text = { Text("WARNING: Are you absolutely certain you want to wipeout your entire log history book? This permanently deletes every record from your phone's database. This action cannot be reversed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showResetAllDialog = false
                        Toast.makeText(context, "All log histories successfully cleared!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Wipe Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("Cancel", color = Color(0xFF8D6E63))
                }
            }
        )
    }
}
