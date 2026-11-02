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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.ui.components.SearchBarArabic
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: SmartLabViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val paymentsState by viewModel.paymentsState.collectAsState()
    val expensesState by viewModel.expensesState.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Payments, 1: Expenses, 2: Dues & Overdue
    var showExpenseDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = { showExpenseDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary),
                    modifier = Modifier.testTag("add_expense_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة مصروف")
                }
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
                text = "الإدارة المالية والمصروفات",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "متابعة الإيرادات، الأقساط، المصروفات التشغيلية، وصافي الدخل",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Finance Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinanceStatBadge(
                    label = "الإيرادات",
                    amount = formatCurrencyIQD(stats.totalRevenue),
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                FinanceStatBadge(
                    label = "المصروفات",
                    amount = formatCurrencyIQD(stats.totalExpenses),
                    color = StatusDanger,
                    modifier = Modifier.weight(1f)
                )
                FinanceStatBadge(
                    label = "الصافي",
                    amount = formatCurrencyIQD(stats.netIncome),
                    color = LabBluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الوصولات والدفعات") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("المصروفات الإدارية") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("المبالغ المستحقة") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Payments list
                    when (val pState = paymentsState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Success -> {
                            val list = pState.data
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد دفعات مالية مسجلة بعد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(list) { pay ->
                                        PaymentReceiptItem(payment = pay, onCancelClick = {})
                                    }
                                    item { Spacer(modifier = Modifier.height(30.dp)) }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text("خطأ: ${pState.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                1 -> {
                    // Expenses list
                    when (val eState = expensesState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Success -> {
                            val list = eState.data
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد مصروفات مسجلة. اضغط 'إضافة مصروف' لتوثيق المصاريف.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(list) { exp ->
                                        ExpenseItemCard(expense = exp)
                                    }
                                    item { Spacer(modifier = Modifier.height(70.dp)) }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text("خطأ: ${eState.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                2 -> {
                    // Dues list
                    when (val sState = studentsState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Success -> {
                            val studentsWithDues = sState.data.filter { !it.isArchived && it.remainingAmount > 0 }
                            if (studentsWithDues.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("كافة الطلاب مسددين بالكامل! لا توجد مستحقات متأخرة.", color = StatusSuccess)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(studentsWithDues) { student ->
                                        DueStudentItem(student = student)
                                    }
                                    item { Spacer(modifier = Modifier.height(30.dp)) }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text("خطأ: ${sState.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        ExpenseFormDialog(
            onDismiss = { showExpenseDialog = false },
            onSave = { expense ->
                viewModel.saveExpense(expense) {
                    showExpenseDialog = false
                }
            }
        )
    }
}

@Composable
fun FinanceStatBadge(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = amount, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ExpenseItemCard(expense: Expense) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = expense.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${expense.date} • التصنيف: ${getCategoryLabel(expense.category)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.responsiblePerson.isNotBlank()) {
                    Text(
                        text = "المسؤول: ${expense.responsiblePerson}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "-${formatCurrencyIQD(expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StatusDanger
            )
        }
    }
}

fun getCategoryLabel(cat: String): String {
    return when (cat) {
        "trainer_salaries" -> "رواتب المدربين"
        "prints" -> "المطبوعات"
        "lab_supplies" -> "مستلزمات مختبرية"
        "rent" -> "الإيجار"
        "operating" -> "مصاريف تشغيلية"
        else -> cat
    }
}

@Composable
fun DueStudentItem(student: Student) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${student.phone} • المجموعة: ${student.groupName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "الإجمالي: ${formatCurrencyIQD(student.totalPrice)} • المسدد: ${formatCurrencyIQD(student.paidAmount)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "مستحق: ${formatCurrencyIQD(student.remainingAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusWarning
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormDialog(
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(today) }
    var category by remember { mutableStateOf("lab_supplies") }
    var responsiblePerson by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categoryOptions = listOf(
        "trainer_salaries" to "رواتب المدربين",
        "prints" to "المطبوعات",
        "lab_supplies" to "المستلزمات المختبرية",
        "rent" to "الإيجار",
        "operating" to "المصاريف التشغيلية"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل مصروف إداري / مختبري جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان المصروف *") },
                    placeholder = { Text("مثال: شراء كواشف ومحاليل مخبرية") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة المصروف (د.ع) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoryOptions.firstOrNull { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("التصنيف") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categoryOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.second) },
                                onClick = {
                                    category = opt.first
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = responsiblePerson,
                    onValueChange = { responsiblePerson = it },
                    label = { Text("الشخص المسؤول") },
                    placeholder = { Text("اسم الموظف أو المشرف") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (title.isNotBlank() && amount != null && amount > 0) {
                        onSave(
                            Expense(
                                title = title.trim(),
                                amount = amount,
                                date = date,
                                category = category,
                                responsiblePerson = responsiblePerson.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
            ) {
                Text("حفظ المصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (Payment) -> Unit
) {
    var amountStr by remember { mutableStateOf(if (student.remainingAmount > 0) student.remainingAmount.toLong().toString() else "") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var receiptNumber by remember {
        mutableStateOf("RCP-" + (System.currentTimeMillis() % 100000).toString())
    }
    var receivedBy by remember { mutableStateOf("الإدارة المالية") }
    var notes by remember { mutableStateOf("") }

    val methods = listOf("Cash" to "نقداً (كاش)", "ZainCash" to "زين كاش", "QiCard" to "كي كارد", "BankTransfer" to "تحويل بنكي")
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إصدار وصل وقبض دفعة مالية") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الطالب: ${student.fullName}\nالمتبقي بذمته: ${formatCurrencyIQD(student.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المقبوض (د.ع) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("رقم الوصل *") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = methods.firstOrNull { it.first == paymentMethod }?.second ?: paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("طريقة الدفع") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.second) },
                                onClick = {
                                    paymentMethod = m.first
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = receivedBy,
                    onValueChange = { receivedBy = it },
                    label = { Text("اسم الموظف المستلم") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSave(
                            Payment(
                                studentId = student.id,
                                studentName = student.fullName,
                                groupId = student.groupId,
                                amount = amount,
                                paymentMethod = paymentMethod,
                                receiptNumber = receiptNumber.trim(),
                                receivedBy = receivedBy.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
            ) {
                Text("إصدار الوصل والترحيل المالي")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
