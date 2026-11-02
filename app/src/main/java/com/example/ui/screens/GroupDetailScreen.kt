package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.LabCyanAccent
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    group: TrainingGroup,
    viewModel: SmartLabViewModel,
    onBack: () -> Unit,
    onPrintList: () -> Unit,
    onSelectStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val studentsState by viewModel.studentsState.collectAsState()
    val attendanceState by viewModel.attendanceState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Students, 1: Attendance, 2: Info & Schedule
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
    val groupStudents = allStudents.filter { it.groupId == group.id && !it.isArchived }

    val totalPaid = groupStudents.sumOf { it.paidAmount }
    val totalRemaining = groupStudents.sumOf { it.remainingAmount }
    val expectedRevenue = groupStudents.sumOf { it.totalPrice }
    val registrationPercent = if (group.maxSeats > 0) ((groupStudents.size.toFloat() / group.maxSeats.toFloat()) * 100).toInt().coerceAtMost(100) else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = onPrintList) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "طباعة قائمة المجموعة", tint = LabBluePrimary)
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل المجموعة")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Group Overview Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = group.courseName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "المدرب: ${group.trainerName.ifBlank { "غير محدد" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = group.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadgeItem(title = "الطلاب", value = "${groupStudents.size} / ${if (group.maxSeats > 0) group.maxSeats else "∞"}", modifier = Modifier.weight(1f))
                        StatBadgeItem(title = "المحصل", value = formatCurrencyIQD(totalPaid), modifier = Modifier.weight(1.3f))
                        StatBadgeItem(title = "المتبقي", value = formatCurrencyIQD(totalRemaining), modifier = Modifier.weight(1.3f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onPrintList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("print_group_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("طباعة قائمة المجموعة والمعاينة")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("قائمة الطلاب (${groupStudents.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الحضور والغياب") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("الجدول والتفاصيل") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Students List inside group
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الطلاب المسجلين",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showAddStudentDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة طالب للمجموعة")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (groupStudents.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "لا يوجد طلاب في هذه المجموعة بعد. اضغط 'إضافة طالب للمجموعة'.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(groupStudents) { student ->
                                StudentGroupItem(
                                    student = student,
                                    onClick = { onSelectStudent(student) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(30.dp)) }
                        }
                    }
                }
                1 -> {
                    // Attendance section
                    GroupAttendanceTab(
                        group = group,
                        students = groupStudents,
                        attendanceRecords = (attendanceState as? UiState.Success)?.data?.filter { it.groupId == group.id } ?: emptyList(),
                        onSaveAttendance = { record ->
                            viewModel.saveAttendance(record)
                        }
                    )
                }
                2 -> {
                    // Schedule & Info
                    GroupScheduleTab(group = group)
                }
            }
        }
    }

    if (showEditDialog) {
        GroupFormDialog(
            group = group,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.saveGroup(updated) {
                    showEditDialog = false
                }
            }
        )
    }

    if (showAddStudentDialog) {
        StudentFormDialog(
            student = null,
            defaultGroupId = group.id,
            defaultGroupName = group.name,
            defaultCourseName = group.courseName,
            defaultPrice = group.totalPrice,
            allGroups = listOf(group),
            onDismiss = { showAddStudentDialog = false },
            onSave = { newStudent ->
                viewModel.saveStudent(newStudent) {
                    showAddStudentDialog = false
                }
            }
        )
    }
}

@Composable
fun StatBadgeItem(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StudentGroupItem(
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${student.phone} • مسدد: ${formatCurrencyIQD(student.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (student.remainingAmount > 0) {
                    Text(
                        text = "المتبقي: ${formatCurrencyIQD(student.remainingAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusWarning,
                        fontWeight = FontWeight.Bold
                    )
                }
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
fun GroupAttendanceTab(
    group: TrainingGroup,
    students: List<Student>,
    attendanceRecords: List<AttendanceRecord>,
    onSaveAttendance: (AttendanceRecord) -> Unit
) {
    var lectureTitle by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(today) }

    // Map of studentId -> status (present, absent, late, excused)
    var studentStatuses by remember { mutableStateOf(students.associate { it.id to "present" }) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "تسجيل حضور جلسة تدريبية جديدة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("تاريخ المحاضرة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lectureTitle,
                        onValueChange = { lectureTitle = it },
                        label = { Text("عنوان المحاضرة / المختبر") },
                        placeholder = { Text("مثال: فحص صورة الدم الكاملة CBC") },
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                val currentStatus = studentStatuses[student.id] ?: "present"
                OutlinedCard(shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = student.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttendanceChoiceChip(
                                label = "حاضر",
                                isSelected = currentStatus == "present",
                                color = StatusSuccess,
                                onClick = { studentStatuses = studentStatuses + (student.id to "present") }
                            )
                            AttendanceChoiceChip(
                                label = "غائب",
                                isSelected = currentStatus == "absent",
                                color = StatusWarning,
                                onClick = { studentStatuses = studentStatuses + (student.id to "absent") }
                            )
                            AttendanceChoiceChip(
                                label = "متأخر",
                                isSelected = currentStatus == "late",
                                color = LabCyanAccent,
                                onClick = { studentStatuses = studentStatuses + (student.id to "late") }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val json = JSONObject(studentStatuses).toString()
                        val record = AttendanceRecord(
                            groupId = group.id,
                            lectureDate = selectedDate,
                            lectureTitle = lectureTitle.ifBlank { "جلسة تدريبية" },
                            records = json
                        )
                        onSaveAttendance(record)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
                ) {
                    Text("حفظ وتثبيت سجل الحضور")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AttendanceChoiceChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun GroupScheduleTab(group: TrainingGroup) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "تفاصيل الجدول والمواعيد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            InfoRow(label = "تاريخ الانطلاق", value = group.startDate.ifBlank { "لم يحدد بعد" })
            InfoRow(label = "وقت الانطلاق", value = group.startTime.ifBlank { "لم يحدد" })
            InfoRow(label = "أيام المحاضرات", value = group.lectureDays.ifBlank { "لم تحدد" })
            InfoRow(label = "مدة الدورة", value = group.duration.ifBlank { "غير محدد" })
            InfoRow(label = "توقيت المحاضرات", value = group.lectureTime.ifBlank { "حسب الجدول الأسبوعي" })
            InfoRow(label = "المدرب المسؤول", value = group.trainerName.ifBlank { "لم يعين بعد" })
            InfoRow(label = "السعر الإجمالي للدورة", value = formatCurrencyIQD(group.totalPrice))
            if (group.notes.isNotBlank()) {
                InfoRow(label = "ملاحظات إدارية", value = group.notes)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
