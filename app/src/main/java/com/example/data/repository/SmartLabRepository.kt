package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.AuditLog
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SmartLabRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun currentDateTimeString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    // ==========================================
    // GROUPS
    // ==========================================

    fun observeGroups(userId: String): Flow<List<TrainingGroup>> = flow {
        val path = "users/$userId/groups"
        db.collection("users").document(userId).collection("groups")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(TrainingGroup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveGroup(group: TrainingGroup): Result<String> {
        val uid = requireUserId()
        val docId = if (group.id.isNotBlank()) group.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("groups").document(docId)
        val isNew = group.id.isBlank()

        return try {
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "name" to group.name,
                "courseName" to group.courseName,
                "registrationDate" to group.registrationDate,
                "startDate" to group.startDate,
                "startTime" to group.startTime,
                "lectureDays" to group.lectureDays,
                "lectureTime" to group.lectureTime,
                "duration" to group.duration,
                "maxSeats" to group.maxSeats,
                "trainerName" to group.trainerName,
                "totalPrice" to group.totalPrice,
                "status" to group.status,
                "notes" to group.notes,
                "isArchived" to group.isArchived,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = group.createdAt ?: FieldValue.serverTimestamp()
            }

            docRef.set(payload.filterValues { it != null }).await()

            logAuditAction(
                action = if (isNew) "CREATE_GROUP" else "UPDATE_GROUP",
                entityType = "group",
                entityId = docId,
                details = "Group '${group.name}' (${group.courseName})"
            )

            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==========================================
    // STUDENTS
    // ==========================================

    fun observeStudents(userId: String): Flow<List<Student>> = flow {
        val path = "users/$userId/students"
        db.collection("users").document(userId).collection("students")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Student::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveStudent(student: Student): Result<String> {
        val uid = requireUserId()
        val docId = if (student.id.isNotBlank()) student.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("students").document(docId)
        val isNew = student.id.isBlank()

        // Calculate remaining amount and financial status automatically
        val remaining = (student.totalPrice - student.paidAmount).coerceAtLeast(0.0)
        val finStatus = when {
            remaining <= 0.0 -> "paid_in_full"
            student.paidAmount > 0.0 -> "partially_paid"
            else -> "unpaid"
        }

        return try {
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "fullName" to student.fullName,
                "phone" to student.phone,
                "photoUrl" to student.photoUrl,
                "registrationDate" to (if (student.registrationDate.isNotBlank()) student.registrationDate else currentDateTimeString()),
                "groupId" to student.groupId,
                "groupName" to student.groupName,
                "courseName" to student.courseName,
                "totalPrice" to student.totalPrice,
                "paidAmount" to student.paidAmount,
                "remainingAmount" to remaining,
                "installmentCount" to student.installmentCount,
                "installmentDates" to student.installmentDates,
                "adminStatus" to student.adminStatus,
                "financialStatus" to finStatus,
                "notes" to student.notes,
                "isArchived" to student.isArchived,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = student.createdAt ?: FieldValue.serverTimestamp()
            }

            docRef.set(payload.filterValues { it != null }).await()

            logAuditAction(
                action = if (isNew) "CREATE_STUDENT" else "UPDATE_STUDENT",
                entityType = "student",
                entityId = docId,
                details = "Student '${student.fullName}', Group: ${student.groupName}, Price: ${student.totalPrice} IQD"
            )

            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun archiveStudent(student: Student, archive: Boolean): Result<Unit> {
        return saveStudent(student.copy(isArchived = archive)).map { }
    }

    // ==========================================
    // PAYMENTS & FINANCIAL AUDIT
    // ==========================================

    fun observePayments(userId: String): Flow<List<Payment>> = flow {
        val path = "users/$userId/payments"
        db.collection("users").document(userId).collection("payments")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Payment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun addPayment(payment: Payment, student: Student): Result<String> {
        val uid = requireUserId()
        val paymentId = UUID.randomUUID().toString()
        val payDocRef = db.collection("users").document(uid).collection("payments").document(paymentId)
        val studentDocRef = db.collection("users").document(uid).collection("students").document(student.id)

        return try {
            val payPayload = mapOf<String, Any?>(
                "userId" to uid,
                "studentId" to student.id,
                "studentName" to student.fullName,
                "groupId" to student.groupId,
                "amount" to payment.amount,
                "paymentMethod" to payment.paymentMethod,
                "paymentDate" to (if (payment.paymentDate.isNotBlank()) payment.paymentDate else currentDateTimeString()),
                "receivedBy" to payment.receivedBy,
                "receiptNumber" to payment.receiptNumber,
                "notes" to payment.notes,
                "isCancelled" to false,
                "cancellationReason" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            // Write payment document
            payDocRef.set(payPayload).await()

            // Update student's financial records
            val newPaid = student.paidAmount + payment.amount
            val newRemaining = (student.totalPrice - newPaid).coerceAtLeast(0.0)
            val newFinStatus = if (newRemaining <= 0.0) "paid_in_full" else "partially_paid"

            studentDocRef.update(
                mapOf(
                    "paidAmount" to newPaid,
                    "remainingAmount" to newRemaining,
                    "financialStatus" to newFinStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            logAuditAction(
                action = "ADD_PAYMENT",
                entityType = "payment",
                entityId = paymentId,
                details = "Receipt #${payment.receiptNumber}: ${payment.amount} IQD for student '${student.fullName}' by ${payment.receivedBy}"
            )

            Result.success(paymentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, payDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun cancelPayment(payment: Payment, student: Student, reason: String): Result<Unit> {
        val uid = requireUserId()
        val payDocRef = db.collection("users").document(uid).collection("payments").document(payment.id)
        val studentDocRef = db.collection("users").document(uid).collection("students").document(student.id)

        return try {
            payDocRef.update(
                mapOf(
                    "isCancelled" to true,
                    "cancellationReason" to reason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Deduct paid amount from student
            val newPaid = (student.paidAmount - payment.amount).coerceAtLeast(0.0)
            val newRemaining = (student.totalPrice - newPaid).coerceAtLeast(0.0)
            val newFinStatus = when {
                newPaid <= 0.0 -> "unpaid"
                newRemaining <= 0.0 -> "paid_in_full"
                else -> "partially_paid"
            }

            studentDocRef.update(
                mapOf(
                    "paidAmount" to newPaid,
                    "remainingAmount" to newRemaining,
                    "financialStatus" to newFinStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            logAuditAction(
                action = "CANCEL_PAYMENT",
                entityType = "payment",
                entityId = payment.id,
                details = "Cancelled payment of ${payment.amount} IQD for '${student.fullName}'. Reason: $reason"
            )

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, payDocRef.path)
            Result.failure(e)
        }
    }

    // ==========================================
    // ATTENDANCE
    // ==========================================

    fun observeAttendance(userId: String): Flow<List<AttendanceRecord>> = flow {
        val path = "users/$userId/attendance"
        db.collection("users").document(userId).collection("attendance")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(AttendanceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveAttendance(record: AttendanceRecord): Result<String> {
        val uid = requireUserId()
        val docId = if (record.id.isNotBlank()) record.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("attendance").document(docId)
        val isNew = record.id.isBlank()

        return try {
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "groupId" to record.groupId,
                "lectureDate" to record.lectureDate,
                "lectureTitle" to record.lectureTitle,
                "records" to record.records,
                "notes" to record.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = record.createdAt ?: FieldValue.serverTimestamp()
            }

            docRef.set(payload.filterValues { it != null }).await()

            logAuditAction(
                action = if (isNew) "RECORD_ATTENDANCE" else "UPDATE_ATTENDANCE",
                entityType = "attendance",
                entityId = docId,
                details = "Attendance for group ${record.groupId} on ${record.lectureDate}"
            )

            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==========================================
    // EXPENSES
    // ==========================================

    fun observeExpenses(userId: String): Flow<List<Expense>> = flow {
        val path = "users/$userId/expenses"
        db.collection("users").document(userId).collection("expenses")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Expense::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveExpense(expense: Expense): Result<String> {
        val uid = requireUserId()
        val docId = if (expense.id.isNotBlank()) expense.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("expenses").document(docId)
        val isNew = expense.id.isBlank()

        return try {
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "title" to expense.title,
                "amount" to expense.amount,
                "date" to expense.date,
                "category" to expense.category,
                "groupId" to expense.groupId,
                "responsiblePerson" to expense.responsiblePerson,
                "notes" to expense.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = expense.createdAt ?: FieldValue.serverTimestamp()
            }

            docRef.set(payload.filterValues { it != null }).await()

            logAuditAction(
                action = if (isNew) "ADD_EXPENSE" else "UPDATE_EXPENSE",
                entityType = "expense",
                entityId = docId,
                details = "Expense '${expense.title}': ${expense.amount} IQD (${expense.category})"
            )

            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================

    fun observeAuditLogs(userId: String): Flow<List<AuditLog>> = flow {
        val path = "users/$userId/audit_logs"
        db.collection("users").document(userId).collection("audit_logs")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    private suspend fun logAuditAction(action: String, entityType: String, entityId: String, details: String) {
        val uid = auth.currentUser?.uid ?: return
        val logId = UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("audit_logs").document(logId)
        try {
            val payload = mapOf<String, Any?>(
                "userId" to uid,
                "action" to action,
                "entityType" to entityType,
                "entityId" to entityId,
                "performedBy" to (auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Admin"),
                "details" to details,
                "timestamp" to currentDateTimeString(),
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
        } catch (e: Exception) {
            // Non-blocking log failure
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }
}
