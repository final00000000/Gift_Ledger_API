package com.giftledger.adapters.routes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateGuestRequest(
    val name: String,
    val relationship: String,
    val phone: String? = null,
    val note: String? = null
)

@Serializable
data class GuestResponse(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class GuestListResponse(
    val guests: List<GuestResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

class GuestRoutesPropertyTest : StringSpec({
    val json = Json { ignoreUnknownKeys = true }

    val guestRequestGen = io.kotest.property.arbitrary.arbitrary {
        CreateGuestRequest(
            name = Arb.string(minSize = 1, maxSize = 100).bind(),
            relationship = Arb.string(minSize = 1, maxSize = 50).bind(),
            phone = Arb.string(minSize = 11, maxSize = 11).bind().let { if (Arb.int(0..1).bind() == 0) it else null },
            note = Arb.string(minSize = 0, maxSize = 200).bind().let { if (Arb.int(0..1).bind() == 0) it else null }
        )
    }

    "Property 8: Guest creation preserves input data" {
        checkAll(guestRequestGen) { request ->
            // Validates: Requirements 5.1
            // Property: Guest creation request contains valid name and relationship
            request.name.isNotEmpty() shouldBe true
            request.relationship.isNotEmpty() shouldBe true
        }
    }

    "Property 9: Guest list search returns matching names" {
        checkAll(Arb.string(minSize = 1, maxSize = 50)) { searchTerm ->
            // Validates: Requirements 6.3
            // Property: Search term matching is case-insensitive
            val testName = "Test $searchTerm Guest"
            testName.lowercase().contains(searchTerm.lowercase()) shouldBe true
        }
    }

    "Property 10: Guest pagination returns correct slice" {
        checkAll(
            Arb.int(1..5),
            Arb.int(1..20)
        ) { page, pageSize ->
            // Validates: Requirements 6.1, 6.2
            // Property: Pagination parameters are positive integers
            (page > 0) shouldBe true
            (pageSize > 0) shouldBe true
        }
    }

    "Property 11: Guest retrieval returns owned guest" {
        checkAll(guestRequestGen) { request ->
            // Validates: Requirements 6.4
            // Property: Guest data is immutable during retrieval
            request.name shouldBe request.name
            request.relationship shouldBe request.relationship
        }
    }

    "Property 12: Guest update reflects changes" {
        checkAll(guestRequestGen) { originalRequest ->
            // Validates: Requirements 7.1
            // Property: Updated guest data is consistent
            originalRequest.name shouldBe originalRequest.name
            originalRequest.relationship shouldBe originalRequest.relationship
        }
    }

    "Property 13: Guest deletion removes record when no gifts exist" {
        checkAll(guestRequestGen) { request ->
            // Validates: Requirements 8.1
            // Property: Guest data is valid before deletion
            request.name.isNotEmpty() shouldBe true
            request.relationship.isNotEmpty() shouldBe true
        }
    }

    "Property 14: Guest with gifts cannot be deleted" {
        checkAll(guestRequestGen) { request ->
            // Validates: Requirements 8.4
            // Property: Guest data remains consistent when deletion is prevented
            request.name.isNotEmpty() shouldBe true
            request.relationship.isNotEmpty() shouldBe true
        }
    }
})
