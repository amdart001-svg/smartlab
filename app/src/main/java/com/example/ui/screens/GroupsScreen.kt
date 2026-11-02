package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.SearchBarArabic
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun GroupsScreen(
    viewModel: SmartLabViewModel,
    onSelectGroup: (TrainingGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupsState by viewModel.groupsState.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = LabBluePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_group_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إنشاء مجموعة جديدة")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "إدارة المجاميع التدريبية",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "إنشاء ومتابعة مجموعات التدريب الطبي، الحصص، والمقاعد المتاحة",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            SearchBarArabic(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "البحث باسم المجموعة أو الدورة..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = groupsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
                    val filtered = state.data.filter { !it.isArchived && (it.name.contains(searchQuery, true) || it.courseName.contains(searchQuery, true)) }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "لا توجد مجاميع مطابقة. اضغط + لإنشاء مجموعة جديدة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { group ->
                                val groupStudents = allStudents.filter { it.groupId == group.id && !it.isArchived }
                                GroupCardItem(
                                    group = group,
                                    studentCount = groupStudents.size,
                                    paidSum = groupStudents.sumOf { it.paidAmount },
                                    onClick = { onSelectGroup(group) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "خطأ: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        GroupFormDialog(
            group = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newGroup ->
                viewModel.saveGroup(newGroup) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun GroupCardItem(
    group: TrainingGroup,
    studentCount: Int,
    paidSum: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("group_card_${group.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = group.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الدورة: ${group.courseName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (group.trainerName.isNotBlank()) {
                Text(
                    text = "المدرب: ${group.trainerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الطلاب: $studentCount / ${if (group.maxSeats > 0) group.maxSeats else "مفتوح"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LabBluePrimary
                )
                Text(
                    text = "المبلغ الإجمالي: ${formatCurrencyIQD(group.totalPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (group.startDate.isNotBlank() || group.lectureDays.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الموعد: ${group.startDate} • ${group.lectureDays} (${group.startTime})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFormDialog(
    group: TrainingGroup?,
    onDismiss: () -> Unit,
    onSave: (TrainingGroup) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var courseName by remember { mutableStateOf(group?.courseName ?: "") }
    var trainerName by remember { mutableStateOf(group?.trainerName ?: "") }
    var totalPriceStr by remember { mutableStateOf(group?.totalPrice?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    var maxSeatsStr by remember { mutableStateOf(group?.maxSeats?.let { if (it > 0) it.toString() else "" } ?: "") }
    var startDate by remember { mutableStateOf(group?.startDate ?: "") }
    var startTime by remember { mutableStateOf(group?.startTime ?: "") }
    var lectureDays by remember { mutableStateOf(group?.lectureDays ?: "") }
    var lectureTime by remember { mutableStateOf(group?.lectureTime ?: "") }
    var duration by remember { mutableStateOf(group?.duration ?: "") }
    var notes by remember { mutableStateOf(group?.notes ?: "") }
    var status by remember { mutableStateOf(group?.status ?: "registering") }

    var statusExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        "registering" to "قيد التسجيل",
        "completed_capacity" to "مكتملة العدد",
        "pending_start" to "بانتظار الانطلاق",
        "active" to "نشطة",
        "postponed" to "مؤجلة",
        "halted" to "متوقفة",
        "finished" to "منتهية"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "إنشاء مجموعة تدريبية جديدة" else "تعديل المجموعة") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم أو رقم المجموعة *") },
                    placeholder = { Text("مثال: المجموعة 12 - صباحي") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("اسم الدورة التدريبية *") },
                    placeholder = { Text("مثال: التحليلات المرضية المتقدمة") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = trainerName,
                    onValueChange = { trainerName = it },
                    label = { Text("المدرب المسؤول") },
                    placeholder = { Text("مثال: د. أحمد علي") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalPriceStr,
                        onValueChange = { totalPriceStr = it },
                        label = { Text("السعر الإجمالي (د.ع) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxSeatsStr,
                        onValueChange = { maxSeatsStr = it },
                        label = { Text("عدد المقاعد") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("تاريخ الانطلاق") },
                        placeholder = { Text("2026-11-15") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("وقت الانطلاق") },
                        placeholder = { Text("16:00") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lectureDays,
                        onValueChange = { lectureDays = it },
                        label = { Text("أيام المحاضرات") },
                        placeholder = { Text("سبت، اثنين، أربعاء") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("مدة الدورة") },
                        placeholder = { Text("6 أسابيع") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = statusOptions.firstOrNull { it.first == status }?.second ?: status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("حالة المجموعة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.second) },
                                onClick = {
                                    status = opt.first
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إدارية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && courseName.isNotBlank()) {
                        val parsedGroup = TrainingGroup(
                            id = group?.id ?: "",
                            name = name.trim(),
                            courseName = courseName.trim(),
                            trainerName = trainerName.trim(),
                            totalPrice = totalPriceStr.toDoubleOrNull() ?: 0.0,
                            maxSeats = maxSeatsStr.toLongOrNull() ?: 0L,
                            startDate = startDate.trim(),
                            startTime = startTime.trim(),
                            lectureDays = lectureDays.trim(),
                            lectureTime = lectureTime.trim(),
                            duration = duration.trim(),
                            status = status,
                            notes = notes.trim(),
                            isArchived = group?.isArchived ?: false,
                            createdAt = group?.createdAt
                        )
                        onSave(parsedGroup)
                    }
                },
                enabled = name.isNotBlank() && courseName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
