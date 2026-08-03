package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DebtDatabase
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.PaymentHistory
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.DebtRepository
import com.example.data.repository.SupabaseSyncManager
import com.example.data.repository.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val title: String) {
    DATE_DESC("По дате"),
    AMOUNT_DESC("По сумме"),
    DEADLINE_ASC("По дедлайну")
}

data class DashboardSummary(
    val totalOwedToMe: Double = 0.0,
    val totalIOwe: Double = 0.0,
    val countOwedToMe: Int = 0,
    val countIOwe: Int = 0,
    val primaryCurrency: String = "₽"
)

class DebtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DebtRepository
    private val authRepository: AuthRepository
    private val syncManager: SupabaseSyncManager

    val authState: StateFlow<AuthState>
    val currentSession: StateFlow<UserSession?>

    val selectedTab = MutableStateFlow(DebtType.OWED_TO_ME)
    val searchQuery = MutableStateFlow("")
    val selectedSort = MutableStateFlow(SortOption.DATE_DESC)
    val selectedDebtId = MutableStateFlow<Long?>(null)

    val allActiveDebts: StateFlow<List<Debt>>
    val filteredActiveDebts: StateFlow<List<Debt>>
    val closedDebts: StateFlow<List<Debt>>
    val summary: StateFlow<DashboardSummary>
    val personNames: StateFlow<List<String>>

    val selectedDebtDetail: StateFlow<Debt?>
    val selectedDebtPayments: StateFlow<List<PaymentHistory>>

    init {
        val database = DebtDatabase.getDatabase(application)
        val debtDao = database.debtDao()
        repository = DebtRepository(debtDao)
        authRepository = AuthRepository(application)
        syncManager = SupabaseSyncManager(debtDao)

        authState = authRepository.authState
        currentSession = authRepository.currentSession

        allActiveDebts = repository.allActiveDebts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


        closedDebts = repository.closedDebts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        personNames = repository.distinctPersonNames
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Compute overall summary totals
        summary = allActiveDebts.map { debts ->
            var owedToMeSum = 0.0
            var iOweSum = 0.0
            var owedToMeCount = 0
            var iOweCount = 0

            debts.forEach { debt ->
                if (debt.type == DebtType.OWED_TO_ME) {
                    owedToMeSum += debt.currentAmount
                    owedToMeCount++
                } else {
                    iOweSum += debt.currentAmount
                    iOweCount++
                }
            }

            val commonCurrency = debts.firstOrNull()?.currency ?: "₽"
            DashboardSummary(
                totalOwedToMe = owedToMeSum,
                totalIOwe = iOweSum,
                countOwedToMe = owedToMeCount,
                countIOwe = iOweCount,
                primaryCurrency = commonCurrency
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

        // Combine active debts with selectedTab, searchQuery, selectedSort
        filteredActiveDebts = combine(
            allActiveDebts,
            selectedTab,
            searchQuery,
            selectedSort
        ) { debts, tab, query, sort ->
            debts.filter { debt ->
                debt.type == tab &&
                        (query.isBlank() ||
                                debt.personName.contains(query, ignoreCase = true) ||
                                debt.comment.contains(query, ignoreCase = true))
            }.sortedWith { d1, d2 ->
                when (sort) {
                    SortOption.DATE_DESC -> d2.createdAt.compareTo(d1.createdAt)
                    SortOption.AMOUNT_DESC -> d2.currentAmount.compareTo(d1.currentAmount)
                    SortOption.DEADLINE_ASC -> {
                        val due1 = d1.dueDate ?: Long.MAX_VALUE
                        val due2 = d2.dueDate ?: Long.MAX_VALUE
                        due1.compareTo(due2)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Debt detail observation
        selectedDebtDetail = selectedDebtId.flatMapLatest { id ->
            if (id != null) repository.getDebtByIdFlow(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        selectedDebtPayments = selectedDebtId.flatMapLatest { id ->
            if (id != null) repository.getPaymentsForDebtFlow(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Clear initial debts as requested by user
        clearAllDebts()
    }

    fun clearAllDebts() {
        viewModelScope.launch {
            repository.clearAllData()
            selectedDebtId.value = null
        }
    }

    fun selectTab(tab: DebtType) {
        selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        selectedSort.value = sort
    }

    fun selectDebt(id: Long?) {
        selectedDebtId.value = id
    }

    fun addDebt(
        personName: String,
        type: DebtType,
        amount: Double,
        currency: String,
        dueDate: Long?,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.createDebt(
                personName = personName,
                type = type,
                amount = amount,
                currency = currency,
                dueDate = dueDate,
                comment = comment
            )
            currentSession.value?.token?.let { token ->
                val createdDebt = Debt(
                    id = newId,
                    personName = personName,
                    type = type,
                    initialAmount = amount,
                    currentAmount = amount,
                    currency = currency,
                    dueDate = dueDate,
                    comment = comment
                )
                syncManager.pushDebtToSupabase(createdDebt, token)
            }
            onSuccess()
        }
    }

    fun editDebt(
        debt: Debt,
        personName: String,
        amount: Double,
        currency: String,
        dueDate: Long?,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val updated = debt.copy(
                personName = personName.trim(),
                currentAmount = amount,
                currency = currency,
                dueDate = dueDate,
                comment = comment.trim()
            )
            repository.updateDebt(updated)
            currentSession.value?.token?.let { token ->
                syncManager.pushDebtToSupabase(updated, token)
            }
            onSuccess()
        }
    }

    fun recordPartialPayment(debtId: Long, amount: Double, note: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addPartialPayment(debtId, amount, note)
            onSuccess()
        }
    }

    fun settleDebtInFull(debtId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.settleInFull(debtId)
            onSuccess()
        }
    }

    fun forgiveDebt(debtId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.forgiveDebt(debtId)
            onSuccess()
        }
    }

    fun reopenDebt(debtId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.reopenDebt(debtId)
            onSuccess()
        }
    }

    fun deleteDebt(debt: Debt, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            if (selectedDebtId.value == debt.id) {
                selectedDebtId.value = null
            }
            onSuccess()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val res = authRepository.login(email, password)
            if (res.isSuccess) {
                val token = res.getOrNull()?.token ?: ""
                syncManager.syncWithSupabase(token)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val res = authRepository.register(email, password)
            if (res.isSuccess) {
                val token = res.getOrNull()?.token ?: ""
                syncManager.syncWithSupabase(token)
            }
        }
    }

    fun continueAsGuest() {
        authRepository.continueAsGuest()
    }

    fun logout() {
        authRepository.logout()
    }
}

