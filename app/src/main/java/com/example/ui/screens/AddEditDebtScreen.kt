package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditDebtScreen(
    debtToEdit: Debt? = null,
    existingContacts: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        personName: String,
        type: DebtType,
        amount: Double,
        currency: String,
        dueDate: Long?,
        comment: String
    ) -> Unit
) {
    var personName by remember { mutableStateOf(debtToEdit?.personName ?: "") }
    var selectedType by remember { mutableStateOf(debtToEdit?.type ?: DebtType.OWED_TO_ME) }
    var amountText by remember {
        mutableStateOf(debtToEdit?.let {
            if (it.currentAmount % 1.0 == 0.0) it.currentAmount.toInt().toString() else it.currentAmount.toString()
        } ?: "")
    }
    var currency by remember { mutableStateOf(debtToEdit?.currency ?: "₽") }
    var dueDate by remember { mutableStateOf(debtToEdit?.dueDate) }
    var comment by remember { mutableStateOf(debtToEdit?.comment ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    val currencies = listOf("₽", "$", "€", "₸", "£", "₴", "BYN")

    fun validateAndSave() {
        var isValid = true

        if (personName.trim().isEmpty()) {
            nameError = "Укажите имя человека"
            isValid = false
        } else {
            nameError = null
        }

        val parsedAmount = amountText.replace(',', '.').toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            amountError = "Введите корректную сумму больше 0"
            isValid = false
        } else {
            amountError = null
        }

        if (isValid && parsedAmount != null) {
            onSave(personName.trim(), selectedType, parsedAmount, currency, dueDate, comment.trim())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (debtToEdit == null) "Новый долг" else "Редактировать долг",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_debt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Debt Type Segmented Button Switcher
            Text(
                text = "Тип записи",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_type_segmented_row")
            ) {
                SegmentedButton(
                    selected = selectedType == DebtType.OWED_TO_ME,
                    onClick = { selectedType = DebtType.OWED_TO_ME },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Мне должны")
                }

                SegmentedButton(
                    selected = selectedType == DebtType.I_OWE,
                    onClick = { selectedType = DebtType.I_OWE },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Я должен")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Person Name Input
            OutlinedTextField(
                value = personName,
                onValueChange = {
                    personName = it
                    nameError = null
                },
                label = { Text("Имя человека *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_name_input")
            )

            // Quick Contact Chips if existing contacts available
            if (existingContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Быстрый выбор контакта:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    existingContacts.take(5).forEach { contact ->
                        AssistChip(
                            onClick = {
                                personName = contact
                                nameError = null
                            },
                            label = { Text(contact) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (personName.equals(contact, ignoreCase = true)) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount and Currency Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                    },
                    label = { Text("Сумма *") },
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("debt_amount_input")
                )

                // Currency Dropdown
                ExposedDropdownMenuBox(
                    expanded = currencyDropdownExpanded,
                    onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                    modifier = Modifier.width(100.dp)
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Валюта") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .testTag("currency_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    currency = curr
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Picker Button
            OutlinedTextField(
                value = dueDate?.let { FormatUtils.formatDate(it) } ?: "Не указана",
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата возврата (дедлайн)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                },
                trailingIcon = {
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Очистить дату")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .testTag("date_picker_trigger")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Field
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Комментарий / заметка (опционально)") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comment_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = { validateAndSave() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_debt_button")
            ) {
                Text(
                    text = if (debtToEdit == null) "Сохранить долг" else "Сохранить изменения",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
