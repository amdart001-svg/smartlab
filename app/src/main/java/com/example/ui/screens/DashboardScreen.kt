package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.LabCyanAccent
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    viewModel: SmartLabViewModel,
    onNavigateToGroups: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onSelectGroup: (TrainingGroup) -> Unit,
    onSelectStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val groupsState by viewModel.groupsState.collectAsState()
    val paymentsState by viewModel.paymentsState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "لوحة التحكم الرئيسية",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "إحصائيات مركز SMARTLAB المحدثة تلقائياً عبر السحابة",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Financial Overview Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إجمالي الإيرادات",
                    value = formatCurrencyIQD(stats.totalRevenue),
                    icon = Icons.Default.TrendingUp,
                    iconTint = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "صافي الإيرادات",
                    value = formatCurrencyIQD(stats.netIncome),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = LabBluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "المبالغ المستحقة",
                    value = formatCurrencyIQD(stats.totalDue),
                    icon = Icons.Default.ReceiptLong,
                    iconTint = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "إجمالي المصروفات",
                    value = formatCurrencyIQD(stats.totalExpenses),
                    icon = Icons.Default.TrendingDown,
                    iconTint = StatusDanger,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Students & Groups Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إجمالي الطلاب",
                    value = "${stats.totalStudents} طالب",
                    subtitle = "النشطين: ${stats.activeStudents}",
                    icon = Icons.Default.Person,
                    iconTint = LabCyanAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "المجاميع التدريبية",
                    value = "${stats.activeGroups + stats.registeringGroups} مجموعات",
                    subtitle = "قيد التسجيل: ${stats.registeringGroups}",
                    icon = Icons.Default.Groups,
                    iconTint = LabBluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Alerts / Admin Notice
        item {
            if (stats.withdrawnStudents > 0 || stats.upcomingGroups > 0) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = LabBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تنبيهات إدارية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "يوجد ${stats.upcomingGroups} مجاميع بانتظار الانطلاق قريباً، و ${stats.withdrawnStudents} طلاب منسحبين للمتابعة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Recent Student Registrations
        item {
            SectionHeader(
                title = "آخر الطلاب المسجلين",
                actionText = "عرض الكل",
                onActionClick = onNavigateToStudents
            )
        }

        when (val state = studentsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is UiState.Success -> {
                val recentStudents = state.data.filter { !it.isArchived }.take(4)
                if (recentStudents.isEmpty()) {
                    item {
                        Text(
                            text = "لا يوجد طلاب مسجلين حالياً. ابدأ بإضافة الطلاب من إدارة المجاميع.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(recentStudents) { student ->
                        StudentDashboardRow(student = student, onClick = { onSelectStudent(student) })
                    }
                }
            }
            is UiState.Error -> {
                item { Text(text = "خطأ في تحميل الطلاب: ${state.message}", color = MaterialTheme.colorScheme.error) }
            }
        }

        // Recent Payments
        item {
            SectionHeader(
                title = "آخر الدفعات المالية والأقساط",
                actionText = "السجل المالي",
                onActionClick = onNavigateToFinance
            )
        }

        when (val payState = paymentsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is UiState.Success -> {
                val recentPayments = payState.data.take(4)
                if (recentPayments.isEmpty()) {
                    item {
                        Text(
                            text = "لم يتم تسجيل دفعات بعد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(recentPayments) { payment ->
                        PaymentDashboardRow(payment = payment)
                    }
                }
            }
            is UiState.Error -> {
                item { Text(text = "خطأ في تحميل الدفعات: ${payState.message}", color = MaterialTheme.colorScheme.error) }
            }
        }

        // Upcoming Groups
        item {
            SectionHeader(
                title = "المجاميع التدريبية الحالية والقادمة",
                actionText = "كل المجاميع",
                onActionClick = onNavigateToGroups
            )
        }

        when (val grpState = groupsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is UiState.Success -> {
                val activeOrUpcoming = grpState.data.filter { !it.isArchived && it.status != "finished" }.take(3)
                if (activeOrUpcoming.isEmpty()) {
                    item {
                        Text(
                            text = "لا توجد مجاميع نشطة حالياً. أنشئ مجموعة جديدة لبدء التسجيل.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(activeOrUpcoming) { group ->
                        GroupDashboardRow(group = group, onClick = { onSelectGroup(group) })
                    }
                }
            }
            is UiState.Error -> {
                item { Text(text = "خطأ في تحميل المجاميع: ${grpState.message}", color = MaterialTheme.colorScheme.error) }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StudentDashboardRow(
    student: Student,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${student.phone} • ${student.groupName.ifBlank { "بدون مجموعة" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status = student.financialStatus)
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(status = student.adminStatus)
            }
        }
    }
}

@Composable
fun PaymentDashboardRow(payment: Payment) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = payment.studentName.ifBlank { "طالب" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "وصل #${payment.receiptNumber.ifBlank { "-" }} • ${payment.paymentMethod} • ${payment.paymentDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${formatCurrencyIQD(payment.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.isCancelled) StatusDanger else StatusSuccess
                )
                if (payment.isCancelled) {
                    Text(
                        text = "ملغاة",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusDanger
                    )
                }
            }
        }
    }
}

@Composable
fun GroupDashboardRow(
    group: TrainingGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${group.courseName} • ${group.trainerName.ifBlank { "المدرب المسؤول" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = group.status)
        }
    }
}
