package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AuditLog
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.data.repository.SmartLabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardStats(
    val totalStudents: Int = 0,
    val activeStudents: Int = 0,
    val pendingStudents: Int = 0,
    val withdrawnStudents: Int = 0,
    val activeGroups: Int = 0,
    val registeringGroups: Int = 0,
    val upcomingGroups: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalDue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netIncome: Double = 0.0
)

class SmartLabViewModel(
    private val repository: SmartLabRepository,
    val currentUserId: String
) : ViewModel() {

    val groupsState: StateFlow<UiState<List<TrainingGroup>>> = repository.observeGroups(currentUserId)
        .map<List<TrainingGroup>, UiState<List<TrainingGroup>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe groups")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val studentsState: StateFlow<UiState<List<Student>>> = repository.observeStudents(currentUserId)
        .map<List<Student>, UiState<List<Student>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe students")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val paymentsState: StateFlow<UiState<List<Payment>>> = repository.observePayments(currentUserId)
        .map<List<Payment>, UiState<List<Payment>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe payments")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val attendanceState: StateFlow<UiState<List<AttendanceRecord>>> = repository.observeAttendance(currentUserId)
        .map<List<AttendanceRecord>, UiState<List<AttendanceRecord>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe attendance")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val expensesState: StateFlow<UiState<List<Expense>>> = repository.observeExpenses(currentUserId)
        .map<List<Expense>, UiState<List<Expense>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe expenses")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val auditLogsState: StateFlow<UiState<List<AuditLog>>> = repository.observeAuditLogs(currentUserId)
        .map<List<AuditLog>, UiState<List<AuditLog>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe audit logs")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // User action feedback
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    // Dashboard Combined Stats Flow
    val dashboardStats: StateFlow<DashboardStats> = combine(
        repository.observeStudents(currentUserId),
        repository.observeGroups(currentUserId),
        repository.observePayments(currentUserId),
        repository.observeExpenses(currentUserId)
    ) { students, groups, payments, expenses ->
        val activeStudents = students.count { !it.isArchived && (it.adminStatus == "confirmed" || it.adminStatus == "attending") }
        val pendingStudents = students.count { !it.isArchived && it.adminStatus == "awaiting_start" }
        val withdrawnStudents = students.count { !it.isArchived && it.adminStatus == "withdrawn" }

        val activeGroups = groups.count { !it.isArchived && it.status == "active" }
        val registeringGroups = groups.count { !it.isArchived && it.status == "registering" }
        val upcomingGroups = groups.count { !it.isArchived && it.status == "pending_start" }

        val activePayments = payments.filter { !it.isCancelled }
        val totalRevenue = activePayments.sumOf { it.amount }
        val totalDue = students.filter { !it.isArchived }.sumOf { it.remainingAmount }
        val totalExp = expenses.sumOf { it.amount }
        val net = totalRevenue - totalExp

        DashboardStats(
            totalStudents = students.count { !it.isArchived },
            activeStudents = activeStudents,
            pendingStudents = pendingStudents,
            withdrawnStudents = withdrawnStudents,
            activeGroups = activeGroups,
            registeringGroups = registeringGroups,
            upcomingGroups = upcomingGroups,
            totalRevenue = totalRevenue,
            totalDue = totalDue,
            totalExpenses = totalExp,
            netIncome = net
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardStats())

    // Actions
    fun saveGroup(group: TrainingGroup, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.saveGroup(group)
            if (result.isSuccess) {
                _actionMessage.value = "تم حفظ المجموعة بنجاح"
                onComplete()
            } else {
                _actionMessage.value = "فشل حفظ المجموعة: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveStudent(student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.saveStudent(student)
            if (result.isSuccess) {
                _actionMessage.value = "تم حفظ بيانات الطالب بنجاح"
                onComplete()
            } else {
                _actionMessage.value = "فشل حفظ الطالب: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun archiveStudent(student: Student, archive: Boolean) {
        viewModelScope.launch {
            val result = repository.archiveStudent(student, archive)
            if (result.isSuccess) {
                _actionMessage.value = if (archive) "تم أرشفة الطالب" else "تم استعادة الطالب من الأرشيف"
            } else {
                _actionMessage.value = "فشل العملية: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addPayment(payment: Payment, student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.addPayment(payment, student)
            if (result.isSuccess) {
                _actionMessage.value = "تم استلام الدفعة وإصدار الوصل بنجاح"
                onComplete()
            } else {
                _actionMessage.value = "فشل تسجيل الدفعة: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun cancelPayment(payment: Payment, student: Student, reason: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.cancelPayment(payment, student, reason)
            if (result.isSuccess) {
                _actionMessage.value = "تم إلغاء الدفعة وتوثيق العملية في سجل التدقيق"
                onComplete()
            } else {
                _actionMessage.value = "فشل إلغاء الدفعة: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveAttendance(record: AttendanceRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.saveAttendance(record)
            if (result.isSuccess) {
                _actionMessage.value = "تم حفظ سجل الحضور والغياب"
                onComplete()
            } else {
                _actionMessage.value = "فشل حفظ سجل الحضور: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveExpense(expense: Expense, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.saveExpense(expense)
            if (result.isSuccess) {
                _actionMessage.value = "تم تسجيل المصروف بنجاح"
                onComplete()
            } else {
                _actionMessage.value = "فشل تسجيل المصروف: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}
