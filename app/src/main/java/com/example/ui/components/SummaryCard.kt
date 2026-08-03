package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtType
import com.example.ui.theme.IOweRed
import com.example.ui.theme.IOweRedBorder
import com.example.ui.theme.IOweRedCardBg
import com.example.ui.theme.OwedToMeGreen
import com.example.ui.theme.OwedToMeGreenBorder
import com.example.ui.theme.OwedToMeGreenCardBg
import com.example.util.FormatUtils

@Composable
fun SummaryHeader(
    totalOwedToMe: Double,
    totalIOwe: Double,
    countOwedToMe: Int,
    countIOwe: Int,
    currencySymbol: String,
    selectedTab: DebtType,
    onTabSelected: (DebtType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Вам должны",
            amount = totalOwedToMe,
            count = countOwedToMe,
            currencySymbol = currencySymbol,
            accentColor = OwedToMeGreen,
            containerColor = OwedToMeGreenCardBg,
            borderColor = OwedToMeGreenBorder,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            isSelected = selectedTab == DebtType.OWED_TO_ME,
            onClick = { onTabSelected(DebtType.OWED_TO_ME) },
            testTag = "summary_card_owed_to_me",
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Вы должны",
            amount = totalIOwe,
            count = countIOwe,
            currencySymbol = currencySymbol,
            accentColor = IOweRed,
            containerColor = IOweRedCardBg,
            borderColor = IOweRedBorder,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            isSelected = selectedTab == DebtType.I_OWE,
            onClick = { onTabSelected(DebtType.I_OWE) },
            testTag = "summary_card_i_owe",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    count: Int,
    currencySymbol: String,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = accentColor.copy(alpha = 0.8f)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = FormatUtils.formatCurrency(amount, currencySymbol),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$count ${getPeopleWord(count)}",
                style = MaterialTheme.typography.bodySmall,
                color = accentColor.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getPeopleWord(count: Int): String {
    val lastTwo = count % 100
    val lastOne = count % 10
    return when {
        lastTwo in 11..19 -> "долгов"
        lastOne == 1 -> "долг"
        lastOne in 2..4 -> "долга"
        else -> "долгов"
    }
}

