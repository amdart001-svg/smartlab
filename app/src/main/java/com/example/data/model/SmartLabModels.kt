package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TrainingGroup(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val courseName: String = "",
    val registrationDate: String = "",
    val startDate: String = "",
    val startTime: String = "",
    val lectureDays: String = "",
    val lectureTime: String = "",
    val duration: String = "",
    val maxSeats: Long = 0L,
    val trainerName: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "registering", // registering, completed_capacity, pending_start, active, postponed, halted, finished
    val notes: String = "",
    val isArchived: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Student(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val registrationDate: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val courseName: String = "",
    val totalPrice: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val installmentCount: Long = 1L,
    val installmentDates: String = "",
    val adminStatus: String = "newly_registered", // newly_registered, confirmed, awaiting_start, attending, absent, suspended, withdrawn, completed
    val financialStatus: String = "unpaid", // paid_in_full, partially_paid, unpaid, installment_due, overdue
    val notes: String = "",
    val isArchived: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Payment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val groupId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, ZainCash, BankTransfer, QiCard
    val paymentDate: String = "",
    val receivedBy: String = "",
    val receiptNumber: String = "",
    val notes: String = "",
    val isCancelled: Boolean = false,
    val cancellationReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AttendanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val groupId: String = "",
    val lectureDate: String = "",
    val lectureTitle: String = "",
    val records: String = "{}", // JSON Map<studentId, status> where status in: present, absent, late, excused
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val category: String = "operating", // trainer_salaries, prints, lab_supplies, rent, operating
    val groupId: String = "",
    val responsiblePerson: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val action: String = "", // e.g. CREATE_STUDENT, ADD_PAYMENT, CANCEL_PAYMENT, UPDATE_GROUP
    val entityType: String = "",
    val entityId: String = "",
    val performedBy: String = "",
    val details: String = "",
    val timestamp: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
