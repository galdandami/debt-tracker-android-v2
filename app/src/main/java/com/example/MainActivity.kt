package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.Debt
import com.example.ui.DebtViewModel
import com.example.ui.screens.AddEditDebtScreen
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtDetailScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.DebtTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DebtTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DebtTrackerApp()
                }
            }
        }
    }
}

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ADD_EDIT_DEBT = "add_edit_debt"
    const val DEBT_DETAIL = "debt_detail"
    const val ARCHIVE = "archive"
}

@Composable
fun DebtTrackerApp(viewModel: DebtViewModel = viewModel()) {
    val navController = rememberNavController()

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()

    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val filteredActiveDebts by viewModel.filteredActiveDebts.collectAsStateWithLifecycle()
    val closedDebts by viewModel.closedDebts.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val personNames by viewModel.personNames.collectAsStateWithLifecycle()

    val selectedDebtDetail by viewModel.selectedDebtDetail.collectAsStateWithLifecycle()
    val selectedDebtPayments by viewModel.selectedDebtPayments.collectAsStateWithLifecycle()

    var editingDebt by remember { mutableStateOf<Debt?>(null) }

    val startDestination = if (currentSession != null) Routes.DASHBOARD else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authState = authState,
                onLogin = { email, password ->
                    viewModel.login(email, password)
                },
                onRegister = { email, password ->
                    viewModel.register(email, password)
                },
                onContinueAsGuest = {
                    viewModel.continueAsGuest()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )

            // React to login success
            androidx.compose.runtime.LaunchedEffect(currentSession) {
                if (currentSession != null) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                summary = summary,
                debts = filteredActiveDebts,
                selectedTab = selectedTab,
                searchQuery = searchQuery,
                selectedSort = selectedSort,
                closedDebtsCount = closedDebts.size,
                userSession = currentSession,
                onTabSelected = { viewModel.selectTab(it) },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSortSelected = { viewModel.setSortOption(it) },
                onDebtClick = { debt ->
                    viewModel.selectDebt(debt.id)
                    navController.navigate(Routes.DEBT_DETAIL)
                },
                onAddDebtClick = {
                    editingDebt = null
                    navController.navigate(Routes.ADD_EDIT_DEBT)
                },
                onOpenArchiveClick = {
                    navController.navigate(Routes.ARCHIVE)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_EDIT_DEBT) {
            AddEditDebtScreen(
                debtToEdit = editingDebt,
                existingContacts = personNames,
                onDismiss = {
                    editingDebt = null
                    navController.popBackStack()
                },
                onSave = { personName, type, amount, currency, dueDate, comment ->
                    if (editingDebt == null) {
                        viewModel.addDebt(personName, type, amount, currency, dueDate, comment) {
                            navController.popBackStack()
                        }
                    } else {
                        viewModel.editDebt(editingDebt!!, personName, amount, currency, dueDate, comment) {
                            editingDebt = null
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(Routes.DEBT_DETAIL) {
            DebtDetailScreen(
                debt = selectedDebtDetail,
                payments = selectedDebtPayments,
                onBack = { navController.popBackStack() },
                onMakePartialPayment = { amount, note ->
                    selectedDebtDetail?.let { debt ->
                        viewModel.recordPartialPayment(debt.id, amount, note) {}
                    }
                },
                onSettleInFull = {
                    selectedDebtDetail?.let { debt ->
                        viewModel.settleDebtInFull(debt.id) {
                            navController.popBackStack()
                        }
                    }
                },
                onForgiveDebt = {
                    selectedDebtDetail?.let { debt ->
                        viewModel.forgiveDebt(debt.id) {
                            navController.popBackStack()
                        }
                    }
                },
                onEditDebt = {
                    editingDebt = selectedDebtDetail
                    navController.navigate(Routes.ADD_EDIT_DEBT)
                },
                onDeleteDebt = {
                    selectedDebtDetail?.let { debt ->
                        viewModel.deleteDebt(debt) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(Routes.ARCHIVE) {
            ArchiveScreen(
                closedDebts = closedDebts,
                onBack = { navController.popBackStack() },
                onReopenDebt = { debt ->
                    viewModel.reopenDebt(debt.id) {}
                },
                onDeleteDebt = { debt ->
                    viewModel.deleteDebt(debt) {}
                }
            )
        }
    }
}
