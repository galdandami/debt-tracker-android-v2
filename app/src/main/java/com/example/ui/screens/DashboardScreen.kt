package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.remote.SupabaseClient
import com.example.data.repository.UserSession
import com.example.ui.DashboardSummary
import com.example.ui.SortOption
import com.example.ui.components.DebtItemCard
import com.example.ui.components.SummaryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    debts: List<Debt>,
    selectedTab: DebtType,
    searchQuery: String,
    selectedSort: SortOption,
    closedDebtsCount: Int,
    userSession: UserSession?,
    onTabSelected: (DebtType) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onDebtClick: (Debt) -> Unit,
    onAddDebtClick: () -> Unit,
    onOpenArchiveClick: () -> Unit,
    onLogout: () -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Поиск по имени или комментарию...") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchQuery.isNotEmpty()) onSearchQueryChange("") else isSearching = false
                                }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field")
                        )
                    } else {
                        Text(
                            text = "Учет Долгов",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    if (!isSearching) {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                        }
                    }

                    // Sort menu button
                    Box {
                        IconButton(onClick = { showSortMenu = !showSortMenu }) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Сортировка")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.title,
                                            fontWeight = if (option == selectedSort) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortSelected(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Archive button with badge
                    IconButton(
                        onClick = onOpenArchiveClick,
                        modifier = Modifier.testTag("archive_nav_button")
                    ) {
                        if (closedDebtsCount > 0) {
                            BadgedBox(
                                badge = { Badge { Text("$closedDebtsCount") } }
                            ) {
                                Icon(imageVector = Icons.Default.Archive, contentDescription = "Архив")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Archive, contentDescription = "Архив")
                        }
                    }

                    // User Account Menu
                    Box {
                        IconButton(
                            onClick = { showUserMenu = !showUserMenu },
                            modifier = Modifier.testTag("user_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Профиль",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = userSession?.email ?: "Гость",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (SupabaseClient.isConfigured) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                                contentDescription = null,
                                                tint = if (SupabaseClient.isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (SupabaseClient.isConfigured) "Supabase БД" else "Локальная БД",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = { }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddDebtClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Добавить", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_debt_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Top Summary Cards
            SummaryHeader(
                totalOwedToMe = summary.totalOwedToMe,
                totalIOwe = summary.totalIOwe,
                countOwedToMe = summary.countOwedToMe,
                countIOwe = summary.countIOwe,
                currencySymbol = summary.primaryCurrency,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs Switcher
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("dashboard_tabs_row")
            ) {
                SegmentedButton(
                    selected = selectedTab == DebtType.OWED_TO_ME,
                    onClick = { onTabSelected(DebtType.OWED_TO_ME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Мне должны (${summary.countOwedToMe})")
                }

                SegmentedButton(
                    selected = selectedTab == DebtType.I_OWE,
                    onClick = { onTabSelected(DebtType.I_OWE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Я должен (${summary.countIOwe})")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Debts List or Empty State
            if (debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (selectedTab == DebtType.OWED_TO_ME) "Никто вам не должен" else "У вас нет долгов",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "По вашему запросу ничего не найдено" else "Нажмите «Добавить», чтобы зафиксировать новый долг.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(debts, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            onClick = { onDebtClick(debt) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                    }
                }
            }
        }
    }
}
