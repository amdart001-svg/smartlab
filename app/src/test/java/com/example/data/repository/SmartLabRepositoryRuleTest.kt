package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SmartLabRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createGroup_validGroup_succeedsAndObserves(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SmartLabRepository(firestore)

        val group = TrainingGroup(
            name = "Group 101",
            courseName = "Medical Analysis",
            totalPrice = 200000.0,
            status = "registering"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveGroup(group)
        }
        assertTrue(result.isSuccess)
        val groupId = result.getOrThrow()
        assertTrue(groupId.isNotEmpty())

        val groups = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeGroups(uid).first { list: List<TrainingGroup> -> list.any { it.id == groupId } }
        }
        assertTrue(groups.any { it.name == "Group 101" })
    }

    @Test
    fun registerStudentAndAddPayment_updatesStudentBalances(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SmartLabRepository(firestore)

        val student = Student(
            fullName = "Sarah Mohammed",
            phone = "07801234567",
            groupId = "group_sample",
            groupName = "Group A",
            totalPrice = 250000.0,
            paidAmount = 0.0,
            remainingAmount = 250000.0,
            adminStatus = "confirmed",
            financialStatus = "unpaid"
        )

        val studentResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveStudent(student)
        }
        assertTrue(studentResult.isSuccess)
        val studentId = studentResult.getOrThrow()

        val payment = Payment(
            studentId = studentId,
            studentName = "Sarah Mohammed",
            groupId = "group_sample",
            amount = 100000.0,
            paymentMethod = "Cash",
            receiptNumber = "REC-001"
        )

        val savedStudent = student.copy(id = studentId)
        val paymentResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addPayment(payment, savedStudent)
        }
        assertTrue(paymentResult.isSuccess)

        val updatedStudents = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudents(uid).first { list: List<Student> -> list.any { it.id == studentId && it.paidAmount == 100000.0 } }
        }
        val target = updatedStudents.first { it.id == studentId }
        assertEquals(100000.0, target.paidAmount, 0.01)
        assertEquals(150000.0, target.remainingAmount, 0.01)
        assertEquals("partially_paid", target.financialStatus)
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        val repository = SmartLabRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeGroups("unauth_user").first()
            }
            fail("Expected exception on unauthenticated query")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull(firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "admin_alice@smartlab.iq"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
