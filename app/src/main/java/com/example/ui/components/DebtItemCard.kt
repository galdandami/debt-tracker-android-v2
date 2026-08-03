package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.ui.theme.IOweRed
import com.example.ui.theme.OwedToMeGreen
import com.example.ui.theme.OwedToMeGreenBorder
import com.example.ui.theme.OwedToMeGreenCardBg
import com.example.ui.theme.OverdueRedBg
import com.example.ui.theme.OverdueRedBorder
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.util.FormatUtils

@Composable
fun DebtItemCard(
    debt: Debt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (debt.type == DebtType.OWED_TO_ME) OwedToMeGreen else IOweRed
    val deadlineInfo = FormatUtils.getDeadlineInfo(debt.dueDate)
    val isPartiallyPaid = debt.currentAmount < debt.initialAmount && debt.currentAmount > 0
    val progress = if (debt.initialAmount > 0) {
        ((debt.initialAmount - debt.currentAmount) / debt.initialAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_item_card_${debt.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Person Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(debt.personName),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "от ${FormatUtils.formatDateShort(debt.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount Column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = FormatUtils.formatCurrency(debt.currentAmount, debt.currency),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = accentColor
                    )

                    if (isPartiallyPaid) {
                        Text(
                            text = "из ${FormatUtils.formatCurrency(debt.initialAmount, debt.currency)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Progress bar if partially paid
            if (isPartiallyPaid) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% выплачено",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Comment and Deadline Tags
            if (debt.comment.isNotBlank() || deadlineInfo != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (debt.comment.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = debt.comment,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (deadlineInfo != null) {
                        val badgeBg = when {
                            deadlineInfo.isOverdue -> OverdueRedBg
                            deadlineInfo.isWarning -> WarningAmberBg
                            else -> OwedToMeGreenCardBg
                        }
                        val badgeBorder = when {
                            deadlineInfo.isOverdue -> OverdueRedBorder
                            deadlineInfo.isWarning -> Color(0xFF5A3E1E)
                            else -> OwedToMeGreenBorder
                        }
                        val badgeText = when {
                            deadlineInfo.isOverdue -> IOweRed
                            deadlineInfo.isWarning -> WarningAmber
                            else -> OwedToMeGreen
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder),
                            modifier = Modifier.testTag("deadline_badge_${debt.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = badgeText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deadlineInfo.text,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (deadlineInfo.isWarning || deadlineInfo.isOverdue) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = badgeText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.isNotEmpty() -> parts[0].take(1).uppercase()
        else -> "?"
    }
}
