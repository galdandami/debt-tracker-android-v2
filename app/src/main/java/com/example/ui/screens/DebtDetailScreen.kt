package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.PaymentHistory
import com.example.ui.components.PartialPaymentDialog
import com.example.ui.theme.IOweRed
import com.example.ui.theme.OwedToMeGreen
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(
    debt: Debt?,
    payments: List<PaymentHistory>,
    onBack: () -> Unit,
    onMakePartialPayment: (amount: Double, note: String) -> Unit,
    onSettleInFull: () -> Unit,
    onForgiveDebt: () -> Unit,
    onEditDebt: () -> Unit,
    onDeleteDebt: () -> Unit
) {
    if (debt == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Запись не найдена", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    var showPartialPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSettleConfirmDialog by remember { mutableStateOf(false) }
    var showForgiveConfirmDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val accentColor = if (debt.type == DebtType.OWED_TO_ME) OwedToMeGreen else IOweRed
    val deadlineInfo = FormatUtils.getDeadlineInfo(debt.dueDate)
    val progress = if (debt.initialAmount > 0) {
        ((debt.initialAmount - debt.currentAmount) / debt.initialAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали долга") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_detail_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!debt.isClosed) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEditDebt()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Простить долг") },
                                leadingIcon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showForgiveConfirmDialog = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Main Person & Amount Hero Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(debt.personName),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (debt.type == DebtType.OWED_TO_ME) "Мне должны" else "Я должен",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Current Balance Amount
                    Text(
                        text = FormatUtils.formatCurrency(debt.currentAmount, debt.currency),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = accentColor
                    )

                    if (debt.currentAmount < debt.initialAmount) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Начальная сумма: ${FormatUtils.formatCurrency(debt.initialAmount, debt.currency)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Details info list
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(title = "Дата создания", value = FormatUtils.formatDate(debt.createdAt))

                    if (debt.dueDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            title = "Дедлайн",
                            value = FormatUtils.formatDate(debt.dueDate) + (deadlineInfo?.let { " (${it.text})" } ?: "")
                        )
                    }

                    if (debt.isClosed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            title = "Статус",
                            value = if (debt.isForgiven) "Прощен" else "Закрыт ${debt.closedAt?.let { FormatUtils.formatDate(it) } ?: ""}"
                        )
                    }

                    if (debt.comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(title = "Комментарий", value = debt.comment)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons for active debts
            if (!debt.isClosed) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showPartialPaymentDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("make_partial_payment_button")
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Частично")
                    }

                    Button(
                        onClick = { showSettleConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settle_full_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Погасить")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Payment History Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "История платежей (${payments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (payments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Платежи пока не вносились",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    payments.forEach { payment ->
                        PaymentHistoryItemCard(payment = payment, currencySymbol = debt.currency)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialogs
    if (showPartialPaymentDialog) {
        PartialPaymentDialog(
            remainingAmount = debt.currentAmount,
            currencySymbol = debt.currency,
            onDismiss = { showPartialPaymentDialog = false },
            onConfirm = { amount, note ->
                showPartialPaymentDialog = false
                onMakePartialPayment(amount, note)
            }
        )
    }

    if (showSettleConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSettleConfirmDialog = false },
            title = { Text("Погасить полностью?") },
            text = { Text("Остаток долга (${FormatUtils.formatCurrency(debt.currentAmount, debt.currency)}) будет зафиксирован как выплаченный, и долг переместится в Архив.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSettleConfirmDialog = false
                        onSettleInFull()
                    },
                    modifier = Modifier.testTag("confirm_settle_full_button")
                ) {
                    Text("Погасить полностью")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showForgiveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showForgiveConfirmDialog = false },
            title = { Text("Простить долг?") },
            text = { Text("Запись будет отмечена как прощенная и перемещена в Архив.") },
            confirmButton = {
                Button(
                    onClick = {
                        showForgiveConfirmDialog = false
                        onForgiveDebt()
                    }
                ) {
                    Text("Простить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgiveConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Удалить долг?") },
            text = { Text("Запись и вся история её платежей будут окончательно удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteDebt()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun PaymentHistoryItemCard(
    payment: PaymentHistory,
    currencySymbol: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = FormatUtils.formatDate(payment.paymentDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (payment.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = payment.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "- " + FormatUtils.formatCurrency(payment.amount, currencySymbol),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OwedToMeGreen
            )
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
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
