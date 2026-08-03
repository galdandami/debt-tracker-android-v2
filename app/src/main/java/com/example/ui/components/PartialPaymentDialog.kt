package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.util.FormatUtils

@Composable
fun PartialPaymentDialog(
    remainingAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun validateAndConfirm() {
        val amount = amountText.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount <= 0) {
            errorText = "Введите сумму больше 0"
            return
        }
        if (amount > remainingAmount) {
            errorText = "Сумма превышает остаток долга"
            return
        }
        errorText = null
        onConfirm(amount, noteText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Внести частичный платеж",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Остаток долга: ${FormatUtils.formatCurrency(remainingAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick percentage chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val p25 = (remainingAmount * 0.25).toInt().toDouble()
                    val p50 = (remainingAmount * 0.50).toInt().toDouble()
                    val p100 = remainingAmount

                    listOf(
                        "25%" to p25,
                        "50%" to p50,
                        "100%" to p100
                    ).forEach { (label, valAmount) ->
                        AssistChip(
                            onClick = {
                                amountText = if (valAmount % 1.0 == 0.0) valAmount.toInt().toString() else valAmount.toString()
                                errorText = null
                            },
                            label = { Text(label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input
                        errorText = null
                    },
                    label = { Text("Сумма платежа ($currencySymbol)") },
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partial_payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Комментарий / заметка (опционально)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partial_payment_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndConfirm() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_partial_payment_button")
            ) {
                Text("Внести")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
