package com.example.financemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.PaymentMethod
import com.example.financemanager.model.getCategoryDisplayName
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 120f
    val maxSwipe = 200f

    Box(modifier = modifier) {
        // Hidden actions that appear on swipe (background)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit button
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }

        // Main transaction card (draggable)
        Card(
            modifier = Modifier
                .offset(x = offsetX.dp)
                .fillMaxWidth()
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta
                        offsetX = newOffset.coerceIn(-maxSwipe, 0f)
                    },
                    onDragStopped = {
                        if (offsetX.absoluteValue > swipeThreshold) {
                            offsetX = -maxSwipe
                        } else {
                            offsetX = 0f
                        }
                    }
                ),
            onClick = {
                if (offsetX < 0f) {
                    offsetX = 0f
                } else {
                    onClick()
                }
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Method Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (transaction.paymentMethod) {
                                PaymentMethod.CASH -> MaterialTheme.colorScheme.primaryContainer
                                PaymentMethod.ALIPAY -> MaterialTheme.colorScheme.secondaryContainer
                                PaymentMethod.WECHAT -> MaterialTheme.colorScheme.tertiaryContainer
                                PaymentMethod.OCTOPUS -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (transaction.paymentMethod) {
                            PaymentMethod.CASH -> "C"
                            PaymentMethod.ALIPAY -> "A"
                            PaymentMethod.WECHAT -> "W"
                            PaymentMethod.OCTOPUS -> "O"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = when (transaction.paymentMethod) {
                            PaymentMethod.CASH -> MaterialTheme.colorScheme.primary
                            PaymentMethod.ALIPAY -> MaterialTheme.colorScheme.secondary
                            PaymentMethod.WECHAT -> MaterialTheme.colorScheme.tertiary
                            PaymentMethod.OCTOPUS -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Transaction Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = getCategoryDisplayName(transaction.category), // FIXED: Convert Category enum to String
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (transaction.isExpense) "-$${"%.2f".format(transaction.amount)}"
                            else "+$${"%.2f".format(transaction.amount)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (transaction.isExpense) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Payment Method Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (transaction.paymentMethod) {
                                    PaymentMethod.CASH -> "Cash"
                                    PaymentMethod.ALIPAY -> "Alipay"
                                    PaymentMethod.WECHAT -> "Wechat"
                                    PaymentMethod.OCTOPUS -> "Octopus"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = formatDate(transaction.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format date
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}