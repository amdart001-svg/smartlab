package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAndArchiveScreen(
    viewModel: SmartLabViewModel,
    onSelectGroup: (TrainingGroup) -> Unit,
    onSelectStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Schedule & Alerts, 1: Archive, 2: Audit Log

    val groupsState by viewModel.groupsState.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val auditLogsState by viewModel.auditLogsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "الجدول، الأرشيف، وسجل التدقيق",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "مواعيد المحاضرات، الأرشيف المحفوظ، والتوثيق الإداري والمالي الشامل",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("المواعيد والتنبيهات") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("الأرشيف") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("سجل التدقيق Audit Log") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Schedule and reminders
                val allGroups = (groupsState as? UiState.Success)?.data?.filter { !it.isArchived } ?: emptyList()
                val students = (studentsState as? UiState.Success)?.data?.filter { !it.isArchived } ?: emptyList()
                val overdueStudents = students.filter { it.financialStatus == "overdue" || it.financialStatus == "installment_due" }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (overdueStudents.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("تنبيهات استحقاق الأقساط:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    overdueStudents.forEach { st ->
                                        Text(
                                            "• ${st.fullName} (${st.groupName}) - متبقي بذمته: ${formatCurrencyIQD(st.remainingAmount)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("المحاضرات والمجاميع القادمة:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    if (allGroups.isEmpty()) {
                        item { Text("لا توجد مجاميع في الجدول حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(allGroups) { grp ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(grp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("${grp.courseName} • ${grp.trainerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("أيام: ${grp.lectureDays.ifBlank { "السبت والأربعاء" }} • التوقيت: ${grp.startTime}", style = MaterialTheme.typography.labelSmall, color = LabBluePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Archive of Students and Finished Groups
                val archivedStudents = (studentsState as? UiState.Success)?.data?.filter { it.isArchived } ?: emptyList()
                val archivedGroups = (groupsState as? UiState.Success)?.data?.filter { it.isArchived || it.status == "finished" } ?: emptyList()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Text("المجاميع المؤرشفة أو المنتهية:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                    if (archivedGroups.isEmpty()) {
                        item { Text("لا توجد مجاميع مؤرشفة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(archivedGroups) { grp ->
                            OutlinedCard(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(grp.name, fontWeight = FontWeight.Bold)
                                        Text(grp.courseName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(onClick = { onSelectGroup(grp) }, colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)) {
                                        Text("استعراض")
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }
                    item { Text("الطلاب المؤرشفين:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                    if (archivedStudents.isEmpty()) {
                        item { Text("لا يوجد طلاب مؤرشفين.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(archivedStudents) { st ->
                            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(st.fullName, fontWeight = FontWeight.Bold)
                                        Text("${st.phone} • مسدد: ${formatCurrencyIQD(st.paidAmount)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.archiveStudent(st, false) }
                                    ) {
                                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("استعادة")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Audit Log (Immutable record)
                when (val aState = auditLogsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val logs = aState.data
                        if (logs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("سجل التدقيق فارغ حالياً. تسجل العمليات تلقائياً عند إنشاء الطلاب والدفعات.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(logs) { log ->
                                    AuditLogRow(log = log)
                                }
                                item { Spacer(modifier = Modifier.height(20.dp)) }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text("خطأ: ${aState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogRow(log: AuditLog) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.action,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LabBluePrimary
                )
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "بواسطة: ${log.performedBy}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
