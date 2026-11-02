package com.example.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.components.formatCurrencyIQD
import com.example.ui.theme.LabBluePrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPrintScreen(
    group: TrainingGroup,
    students: List<Student>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) }

    var filterType by remember { mutableStateOf("all") } // all, paid, due
    var showPhone by remember { mutableStateOf(true) }
    var showFinancials by remember { mutableStateOf(true) }
    var showSignatures by remember { mutableStateOf(true) }

    val displayedStudents = remember(filterType, students) {
        when (filterType) {
            "paid" -> students.filter { it.financialStatus == "paid_in_full" }
            "due" -> students.filter { it.remainingAmount > 0 }
            else -> students
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معاينة وطباعة قائمة المجموعة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            printGroupHtml(
                                context = context,
                                group = group,
                                students = displayedStudents,
                                printDate = today,
                                showPhone = showPhone,
                                showFinancials = showFinancials,
                                showSignatures = showSignatures
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LabBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("execute_print_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة / تصدير PDF")
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
            // Options Row
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("خيارات تصفية الطباعة:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == "all",
                            onClick = { filterType = "all" },
                            label = { Text("كافة الطلاب (${students.size})") }
                        )
                        FilterChip(
                            selected = filterType == "paid",
                            onClick = { filterType = "paid" },
                            label = { Text("المسددين بالكامل") }
                        )
                        FilterChip(
                            selected = filterType == "due",
                            onClick = { filterType = "due" },
                            label = { Text("أصحاب المبالغ المتبقية") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = showPhone, onCheckedChange = { showPhone = it })
                        Text("إظهار رقم الهاتف", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = showFinancials, onCheckedChange = { showFinancials = it })
                        Text("المبالغ والتحصيل", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = showSignatures, onCheckedChange = { showSignatures = it })
                        Text("خانة التوقيع", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // A4 Preview Sheet Container
            Text("معاينة ورقة A4 الرسمية للطباعة:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Document Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("مركز SMARTLAB للتدريب المختبري", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LabBluePrimary)
                            Text("قائمة حضور وبيانات المجموعة التدريبية", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("تاريخ الطباعة: $today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("الصفحة 1 من 1", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Group Details in Sheet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المجموعة: ${group.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            Text("الدورة: ${group.courseName}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Column {
                            Text("المدرب: ${group.trainerName.ifBlank { "غير محدد" }}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("موعد الانطلاق: ${group.startDate} (${group.lectureDays})", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("عدد الطلاب: ${displayedStudents.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LabBluePrimary)
                            Text("سعر الدورة: ${formatCurrencyIQD(group.totalPrice)}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Print Table Preview
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D47A1))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                            Text("اسم الطالب الكامل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                            if (showPhone) {
                                Text("رقم الهاتف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            }
                            if (showFinancials) {
                                Text("المدفوع", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                                Text("المتبقي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                            }
                            Text("الحالة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                            if (showSignatures) {
                                Text("التوقيع", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                            }
                        }

                        // Table Rows
                        displayedStudents.forEachIndexed { index, st ->
                            val bg = if (index % 2 == 0) Color.White else Color(0xFFF1F5F9)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", fontSize = 11.sp, color = Color.Black, modifier = Modifier.width(24.dp))
                                Text(st.fullName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                                if (showPhone) {
                                    Text(st.phone, fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                                }
                                if (showFinancials) {
                                    Text("${st.paidAmount.toLong()}", fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                                    Text("${st.remainingAmount.toLong()}", fontSize = 10.sp, color = if (st.remainingAmount > 0) Color(0xFFD97706) else Color.DarkGray, modifier = Modifier.weight(0.9f))
                                }
                                Text(
                                    when (st.financialStatus) {
                                        "paid_in_full" -> "مسدد"
                                        "partially_paid" -> "جزئي"
                                        else -> "غير مسدد"
                                    },
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(0.8f)
                                )
                                if (showSignatures) {
                                    Text("...........", fontSize = 10.sp, color = Color.LightGray, modifier = Modifier.weight(0.8f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signatures Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("توقيع المدرب المسؤول", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(30.dp))
                            Text("...........................", color = Color.Gray, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ختم وتوقيع إدارة المركز", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(30.dp))
                            Text("...........................", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun printGroupHtml(
    context: Context,
    group: TrainingGroup,
    students: List<Student>,
    printDate: String,
    showPhone: Boolean,
    showFinancials: Boolean,
    showSignatures: Boolean
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

    val tableRowsHtml = StringBuilder()
    students.forEachIndexed { index, st ->
        val bg = if (index % 2 == 0) "#ffffff" else "#f8fafc"
        tableRowsHtml.append("""
            <tr style="background-color: $bg;">
                <td style="padding: 6px; border: 1px solid #cbd5e1; text-align: center;">${index + 1}</td>
                <td style="padding: 6px; border: 1px solid #cbd5e1; font-weight: bold;">${st.fullName}</td>
                ${if (showPhone) "<td style=\"padding: 6px; border: 1px solid #cbd5e1;\">${st.phone}</td>" else ""}
                ${if (showFinancials) "<td style=\"padding: 6px; border: 1px solid #cbd5e1; color: #059669;\">${st.paidAmount.toLong()} د.ع</td><td style=\"padding: 6px; border: 1px solid #cbd5e1; color: #d97706;\">${st.remainingAmount.toLong()} د.ع</td>" else ""}
                <td style="padding: 6px; border: 1px solid #cbd5e1; text-align: center;">${if (st.financialStatus == "paid_in_full") "مسدد" else "غير مسدد"}</td>
                ${if (showSignatures) "<td style=\"padding: 6px; border: 1px solid #cbd5e1; text-align: center;\"></td>" else ""}
            </tr>
        """.trimIndent())
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="utf-8">
            <title>قائمة مجموعة ${group.name} - SMARTLAB</title>
            <style>
                @page { size: A4; margin: 15mm; }
                body { font-family: 'Segoe UI', Tahoma, sans-serif; direction: rtl; color: #0f172a; margin: 0; padding: 0; }
                .header { border-bottom: 2px solid #0d47a1; padding-bottom: 10px; margin-bottom: 15px; display: flex; justify-content: space-between; }
                .info-box { background: #f1f5f9; border-radius: 6px; padding: 10px; margin-bottom: 15px; font-size: 13px; line-height: 1.6; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; }
                th { background-color: #0d47a1; color: white; padding: 8px; border: 1px solid #0d47a1; }
                .footer { margin-top: 40px; display: flex; justify-content: space-around; text-align: center; }
            </style>
        </head>
        <body>
            <div class="header">
                <div>
                    <h2 style="margin: 0; color: #0d47a1;">مركز SMARTLAB للتدريب المختبري</h2>
                    <p style="margin: 4px 0 0 0; font-size: 13px; color: #475569;">قائمة التدريب والمتابعة الإدارية الرسمية</p>
                </div>
                <div style="text-align: left; font-size: 12px; color: #64748b;">
                    <p style="margin: 0;">تاريخ الطباعة: $printDate</p>
                    <p style="margin: 4px 0 0 0;">المجموعة: ${group.name}</p>
                </div>
            </div>

            <div class="info-box">
                <strong>الدورة:</strong> ${group.courseName} &nbsp;|&nbsp;
                <strong>المدرب:</strong> ${group.trainerName.ifBlank { "غير محدد" }} &nbsp;|&nbsp;
                <strong>الموعد:</strong> ${group.startDate} (${group.lectureDays} - ${group.startTime}) &nbsp;|&nbsp;
                <strong>عدد الطلاب:</strong> ${students.size} طالب
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 30px;">ت</th>
                        <th>اسم الطالب الكامل</th>
                        ${if (showPhone) "<th>رقم الهاتف</th>" else ""}
                        ${if (showFinancials) "<th>المدفوع</th><th>المتبقي</th>" else ""}
                        <th>الحالة</th>
                        ${if (showSignatures) "<th style=\"width: 90px;\">التوقيع</th>" else ""}
                    </tr>
                </thead>
                <tbody>
                    $tableRowsHtml
                </tbody>
            </table>

            <div class="footer">
                <div>
                    <strong>توقيع المدرب المسؤول</strong><br><br><br>
                    ____________________
                </div>
                <div>
                    <strong>ختم وإدارة المركز</strong><br><br><br>
                    ____________________
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printAdapter = webView.createPrintDocumentAdapter("SMARTLAB_${group.name}")
            printManager.print("SMARTLAB_${group.name}", printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
}
