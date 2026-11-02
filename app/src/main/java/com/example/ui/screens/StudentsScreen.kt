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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.SearchBarArabic
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun StudentsScreen(
    viewModel: SmartLabViewModel,
    onSelectStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val studentsState by viewModel.studentsState.collectAsState()
    val groupsState by viewModel.groupsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val allGroups = (groupsState as? UiState.Success)?.data?.filter { !it.isArchived } ?: emptyList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = LabBluePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة طالب")
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
                text = "دليل وإدارة الطلاب",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "سجلات الطلاب، الأقساط المستحقة، الحالات الإدارية والمالية",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            SearchBarArabic(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "البحث باسم الطالب، رقم الهاتف، أو المجموعة..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = studentsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val filtered = state.data.filter { student ->
                        !student.isArchived &&
                        (student.fullName.contains(searchQuery, true) ||
                         student.phone.contains(searchQuery, true) ||
                         student.groupName.contains(searchQuery, true))
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "لا توجد نتائج مطابقة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { student ->
                                StudentFullCard(
                                    student = student,
                                    onClick = { onSelectStudent(student) }
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
        StudentFormDialog(
            student = null,
            defaultGroupId = allGroups.firstOrNull()?.id ?: "",
            defaultGroupName = allGroups.firstOrNull()?.name ?: "",
            defaultCourseName = allGroups.firstOrNull()?.courseName ?: "",
            defaultPrice = allGroups.firstOrNull()?.totalPrice ?: 0.0,
            allGroups = allGroups,
            onDismiss = { showCreateDialog = false },
            onSave = { newStudent ->
                viewModel.saveStudent(newStudent) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun StudentFullCard(
    student: Student,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.fullName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = student.financialStatus)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "المجموعة: ${student.groupName.ifBlank { "غير مسكن" }} • ${student.courseName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "الهاتف: ${student.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المدفوع: ${formatCurrencyIQD(student.paidAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المتبقي: ${formatCurrencyIQD(student.remainingAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (student.remainingAmount > 0) StatusWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = student.adminStatus)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    student: Student?,
    defaultGroupId: String = "",
    defaultGroupName: String = "",
    defaultCourseName: String = "",
    defaultPrice: Double = 0.0,
    allGroups: List<TrainingGroup>,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(student?.fullName ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var selectedGroupId by remember { mutableStateOf(student?.groupId ?: defaultGroupId) }
    var selectedGroupName by remember { mutableStateOf(student?.groupName ?: defaultGroupName) }
    var courseName by remember { mutableStateOf(student?.courseName ?: defaultCourseName) }
    var totalPriceStr by remember { mutableStateOf(student?.totalPrice?.let { if (it > 0) it.toLong().toString() else "" } ?: (if (defaultPrice > 0) defaultPrice.toLong().toString() else "")) }
    var paidAmountStr by remember { mutableStateOf(student?.paidAmount?.let { if (it > 0) it.toLong().toString() else "" } ?: "0") }
    var installmentCountStr by remember { mutableStateOf(student?.installmentCount?.toString() ?: "2") }
    var adminStatus by remember { mutableStateOf(student?.adminStatus ?: "newly_registered") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    var groupMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val adminStatusOptions = listOf(
        "newly_registered" to "مسجل حديثاً",
        "confirmed" to "مؤكد التسجيل",
        "awaiting_start" to "بانتظار المباشرة",
        "attending" to "مباشر بالدورة",
        "absent" to "متغيب",
        "suspended" to "متوقف",
        "withdrawn" to "منسحب",
        "completed" to "مكمل للدورة"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "تسجيل طالب جديد" else "تعديل بيانات الطالب") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل للطالب *") },
                    placeholder = { Text("مثال: حيدر كريم جاسم") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف *") },
                    placeholder = { Text("07701234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // Select Group
                if (allGroups.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = groupMenuExpanded,
                        onExpandedChange = { groupMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGroupName.ifBlank { "اختر المجموعة" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المجموعة التدريبية *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = groupMenuExpanded,
                            onDismissRequest = { groupMenuExpanded = false }
                        ) {
                            allGroups.forEach { grp ->
                                DropdownMenuItem(
                                    text = { Text("${grp.name} (${grp.courseName})") },
                                    onClick = {
                                        selectedGroupId = grp.id
                                        selectedGroupName = grp.name
                                        courseName = grp.courseName
                                        if (totalPriceStr.isBlank()) {
                                            totalPriceStr = grp.totalPrice.toLong().toString()
                                        }
                                        groupMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalPriceStr,
                        onValueChange = { totalPriceStr = it },
                        label = { Text("السعر الإجمالي (د.ع) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("المبلغ المدفوع (د.ع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Admin Status
                ExposedDropdownMenuBox(
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = adminStatusOptions.firstOrNull { it.first == adminStatus }?.second ?: adminStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الحالة الإدارية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        adminStatusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.second) },
                                onClick = {
                                    adminStatus = opt.first
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = installmentCountStr,
                    onValueChange = { installmentCountStr = it },
                    label = { Text("عدد الأقساط") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

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
                    if (fullName.isNotBlank() && phone.isNotBlank()) {
                        val parsed = Student(
                            id = student?.id ?: "",
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            groupId = selectedGroupId,
                            groupName = selectedGroupName,
                            courseName = courseName,
                            totalPrice = totalPriceStr.toDoubleOrNull() ?: 0.0,
                            paidAmount = paidAmountStr.toDoubleOrNull() ?: 0.0,
                            installmentCount = installmentCountStr.toLongOrNull() ?: 1L,
                            adminStatus = adminStatus,
                            notes = notes.trim(),
                            isArchived = student?.isArchived ?: false,
                            createdAt = student?.createdAt
                        )
                        onSave(parsed)
                    }
                },
                enabled = fullName.isNotBlank() && phone.isNotBlank(),
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
