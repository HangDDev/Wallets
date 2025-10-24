package com.example.financemanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financemanager.model.Category
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.getCategoryDisplayName
import kotlin.math.atan2
import kotlin.math.min

@Composable
fun ExpensePieChart(
    transactions: List<Transaction>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter only expense transactions for the pie chart
    val expenseTransactions = transactions.filter { it.isExpense }

    // Get color scheme
    val colorScheme = MaterialTheme.colorScheme

    // Modern color palette
    val modernColors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF8B5CF6), // Violet
        Color(0xFF06B6D4), // Cyan
        Color(0xFF84CC16), // Lime
        Color(0xFFF97316), // Orange
        Color(0xFFEC4899), // Pink
    )

    // Animation state
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(expenseTransactions) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    if (expenseTransactions.isEmpty()) {
        EmptyChartState(modifier)
        return
    }

    // Calculate category totals
    val categoryTotals = expenseTransactions
        .groupBy { it.category }
        .mapValues { (_, trans) -> trans.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalExpense = categoryTotals.sumOf { it.second }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        // REMOVED DUPLICATE HEADER - Only keep the chart and legend

        // Chart Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    clip = true
                )
                .background(colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            var selectedSlice by remember { mutableStateOf<Int?>(null) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchAngle = calculateAngle(center, offset)

                            var currentStartAngle = -90f
                            categoryTotals.forEachIndexed { index, (_, amount) ->
                                val sweepAngle = ((amount / totalExpense) * 360f).toFloat() * animationProgress.value
                                if (isPointInSlice(touchAngle, currentStartAngle, sweepAngle)) {
                                    selectedSlice = if (selectedSlice == index) null else index
                                    return@detectTapGestures
                                }
                                currentStartAngle += sweepAngle
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = (min(canvasWidth, canvasHeight) / 2f) * 0.8f
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                var startAngle = -90f

                // Draw donut chart
                categoryTotals.forEachIndexed { index, (category, amount) ->
                    val sweepAngle = ((amount / totalExpense) * 360f).toFloat() * animationProgress.value
                    val color = modernColors.getOrElse(index) { Color.Gray }
                    val isSelected = selectedSlice == index

                    drawArc(
                        color = if (isSelected) color.copy(alpha = 0.9f) else color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Fill
                    )

                    drawArc(
                        color = colorScheme.surface,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = 3f)
                    )

                    startAngle += sweepAngle
                }

                drawCircle(
                    color = colorScheme.surface,
                    center = center,
                    radius = radius * 0.6f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "Total\n$${"%.0f".format(totalExpense)}",
                    center.x,
                    center.y + 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = colorScheme.onSurface.hashCode()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            selectedSlice?.let { index ->
                val (category, amount) = categoryTotals[index]
                val percentage = (amount / totalExpense) * 100
                val color = modernColors.getOrElse(index) { Color.Gray }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 8.dp)
                ) {
                    Surface(
                        color = color,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = getCategoryDisplayName(category),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "$${"%.1f".format(amount)} (${"%.0f".format(percentage)}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enhanced Legend - FIXED: Properly visible and clickable
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Legend Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurface
                )

                Text(
                    text = "Tap to view transactions",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Legend Items - FIXED: Proper grid layout
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // Fixed height for consistency
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                items(categoryTotals) { (category, amount) ->
                    val index = categoryTotals.indexOfFirst { it.first == category }
                    val color = modernColors.getOrElse(index) { Color.Gray }
                    val percentage = (amount / totalExpense) * 100

                    CategoryLegendItem(
                        category = category,
                        amount = amount,
                        percentage = percentage,
                        color = color,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Stats - Compact
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CompactStatItem(
                    label = "Total",
                    value = "$${"%.0f".format(totalExpense)}",
                    color = colorScheme.primary
                )
                CompactStatItem(
                    label = "Categories",
                    value = "${categoryTotals.size}",
                    color = colorScheme.secondary
                )
                CompactStatItem(
                    label = "Transactions",
                    value = "${expenseTransactions.size}",
                    color = colorScheme.tertiary
                )
            }
        }
    }
}

// NEW: Category Legend Item that's clearly visible and clickable
@Composable
private fun CategoryLegendItem(
    category: Category,
    amount: Double,
    percentage: Double,
    color: Color,
    onCategoryClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(70.dp)
            .clickable {
                onCategoryClick(getCategoryDisplayName(category))
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Category details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCategoryDisplayName(category),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "$${"%.0f".format(amount)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.primary,
                        maxLines = 1
                    )
                    Text(
                        text = " (${"%.0f".format(percentage)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Keep the existing CompactStatItem and EmptyChartState as they are
@Composable
private fun CompactStatItem(
    label: String,
    value: String,
    color: Color
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChartState(modifier: Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                colorScheme.surfaceVariant.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "No data",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "No Expense Data",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = "Add expenses to see breakdown",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions remain the same
private fun calculateAngle(center: Offset, touch: Offset): Float {
    val x = touch.x - center.x
    val y = touch.y - center.y
    return (atan2(y, x) * (180f / Math.PI.toFloat()) + 360f) % 360f
}

private fun isPointInSlice(touchAngle: Float, startAngle: Float, sweepAngle: Float): Boolean {
    val normalizedStart = (startAngle + 360f) % 360f
    val normalizedEnd = (normalizedStart + sweepAngle) % 360f

    return if (normalizedEnd >= normalizedStart) {
        touchAngle >= normalizedStart && touchAngle <= normalizedEnd
    } else {
        touchAngle >= normalizedStart || touchAngle <= normalizedEnd
    }
}