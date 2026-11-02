package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SmartLabViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    student: Student,
    viewModel: SmartLabViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paymentsState by viewModel.paymentsState.collectAsState()
    val groupsState by viewModel.groupsState.collectAsState()
    val allGroups = (groupsState as? UiState.Success)?.data?.filter { !it.isArchived } ?: emptyList()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showCancelPaymentDialog by remember { mutableStateOf<Payment?>(null) }

    val studentPayments = (paymentsState as? UiState.Success)?.data?.filter { it.studentId == student.id } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student.fullName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل بيانات الطالب")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(LabBluePrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = LabBluePrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = student.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "تاريخ التسجيل: ${student.registrationDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(status = student.financialStatus)
                            StatusBadge(status = student.adminStatus)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTransferDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نقل لمجموعة أخرى")
                            }

                            Button(
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_student_payment_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تسجيل قسط / وصل")
                            }
                        }
                    }
                }
            }

            // Financial Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "الحساب المالي والأقساط",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("السعر الإجمالي للدورة:", style = MaterialTheme.typography.bodyMedium)
                            Text(formatCurrencyIQD(student.totalPrice), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مجموع المبالغ المسددة:", style = MaterialTheme.typography.bodyMedium)
                            Text(formatCurrencyIQD(student.paidAmount), fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المبلغ المتبقي بذمته:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrencyIQD(student.remainingAmount),
                                fontWeight = FontWeight.Bold,
                                color = if (student.remainingAmount > 0) StatusWarning else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("عدد الأقساط المقررة:", style = MaterialTheme.typography.bodyMedium)
                            Text("${student.installmentCount} أقساط", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Payments History
            item {
                SectionHeader(title = "سجل الوصولات والدفعات المستلمة")
            }

            if (studentPayments.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد دفعات مسجلة لهذا الطالب حتى الآن.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(studentPayments) { pay ->
                    PaymentReceiptItem(
                        payment = pay,
                        onCancelClick = { showCancelPaymentDialog = pay }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.archiveStudent(student, !student.isArchived)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (student.isArchived) "استعادة الطالب من الأرشيف" else "أرشفة الطالب دون حذف بياناته")
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showPaymentDialog) {
        PaymentFormDialog(
            student = student,
            onDismiss = { showPaymentDialog = false },
            onSave = { payment ->
                viewModel.addPayment(payment, student) {
                    showPaymentDialog = false
                }
            }
        )
    }

    if (showEditDialog) {
        StudentFormDialog(
            student = student,
            allGroups = allGroups,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.saveStudent(updated) {
                    showEditDialog = false
                }
            }
        )
    }

    if (showTransferDialog) {
        TransferGroupDialog(
            student = student,
            allGroups = allGroups,
            onDismiss = { showTransferDialog = false },
            onTransfer = { targetGroup ->
                viewModel.saveStudent(
                    student.copy(
                        groupId = targetGroup.id,
                        groupName = targetGroup.name,
                        courseName = targetGroup.courseName
                    )
                ) {
                    showTransferDialog = false
                }
            }
        )
    }

    if (showCancelPaymentDialog != null) {
        val payToCancel = showCancelPaymentDialog!!
        CancelPaymentDialog(
            payment = payToCancel,
            onDismiss = { showCancelPaymentDialog = null },
            onConfirm = { reason ->
                viewModel.cancelPayment(payToCancel, student, reason) {
                    showCancelPaymentDialog = null
                }
            }
        )
    }
}

@Composable
fun PaymentReceiptItem(
    payment: Payment,
    onCancelClick: () -> Unit
) {
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
                Text(
                    text = "وصل استلام #${payment.receiptNumber.ifBlank { "-" }}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${payment.paymentDate} • المستلم: ${payment.receivedBy.ifBlank { "الإدارة" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "طريقة الدفع: ${payment.paymentMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (payment.isCancelled) {
                    Text(
                        text = "ملغاة: ${payment.cancellationReason}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrencyIQD(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.isCancelled) StatusDanger else StatusSuccess
                )
                if (!payment.isCancelled) {
                    TextButton(onClick = onCancelClick) {
                        Text("إلغاء الدفعة", color = StatusDanger, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CancelPaymentDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إلغاء الدفعة رقم #${payment.receiptNumber}") },
        text = {
            Column {
                Text("لا يمكن حذف المعاملات المالية، ولكن سيتم وسمها كملغاة وتوثيق ذلك في سجل التدقيق المالي Audit Log.")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الإلغاء *") },
                    placeholder = { Text("مثال: خطأ في إدخال المبلغ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (reason.isNotBlank()) onConfirm(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
            ) {
                Text("تأكيد الإلغاء والتوثيق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("تراجع") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferGroupDialog(
    student: Student,
    allGroups: List<TrainingGroup>,
    onDismiss: () -> Unit,
    onTransfer: (TrainingGroup) -> Unit
) {
    var selectedGroup by remember { mutableStateOf(allGroups.firstOrNull { it.id != student.groupId }) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نقل الطالب لمجموعة أخرى") },
        text = {
            Column {
                Text("سيتم نقل الطالب مع الاحتفاظ بكافة الدفعات والحسابات المالية وسجل الحضور دون حذف.")
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: "اختر مجموعة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المجموعة الجديدة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allGroups.filter { it.id != student.groupId }.forEach { grp ->
                            DropdownMenuItem(
                                text = { Text("${grp.name} (${grp.courseName})") },
                                onClick = {
                                    selectedGroup = grp
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedGroup?.let { onTransfer(it) } },
                enabled = selectedGroup != null,
                colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary)
            ) {
                Text("تأكيد النقل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
