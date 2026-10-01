package com.example

import com.example.data.ContactMapper
import com.example.domain.Contact
import com.example.domain.DisplayNameOrder
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry
import com.example.domain.SortOrder
import com.example.util.DuplicateDetector
import com.example.util.VCardHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ContactDomainAndUtilityTest {

    @Test
    fun testDisplayNameOrder() {
        val contact = Contact(
            firstName = "Salim",
            lastName = "Mansoor"
        )
        assertEquals("Salim Mansoor", contact.getDisplayName(DisplayNameOrder.FIRST_LAST))
        assertEquals("Mansoor, Salim", contact.getDisplayName(DisplayNameOrder.LAST_FIRST))
    }

    @Test
    fun testInitialsGeneration() {
        val contact1 = Contact(firstName = "Salim", lastName = "Mansoor")
        assertEquals("SM", contact1.initials)

        val contact2 = Contact(firstName = "Alexander", lastName = "")
        assertEquals("A", contact2.initials)

        val contact3 = Contact(company = "Apple")
        assertEquals("A", contact3.initials)
    }

    @Test
    fun testSortLetters() {
        val c1 = Contact(firstName = "Zoe", lastName = "Adams")
        assertEquals('Z', c1.getSortLetter(SortOrder.FIRST_NAME))
        assertEquals('A', c1.getSortLetter(SortOrder.LAST_NAME))
    }

    @Test
    fun testVCardGenerationAndParsing() {
        val original = Contact(
            firstName = "John",
            lastName = "Doe",
            company = "Acme Corp",
            jobTitle = "Engineer",
            phones = listOf(
                PhoneEntry(number = "+15551234567", label = "Mobile", isPrimary = true),
                PhoneEntry(number = "+15559876543", label = "Work", isPrimary = false)
            ),
            emails = listOf(
                EmailEntry(email = "john.doe@acme.com", label = "Work")
            ),
            address = "123 Main St, New York, NY",
            birthday = "1990-01-01",
            notes = "Met at conference.",
            tags = listOf("Work", "Tech")
        )

        val vCardString = VCardHelper.generateVCard(original)
        assertTrue(vCardString.contains("BEGIN:VCARD"))
        assertTrue(vCardString.contains("FN:John Doe"))
        assertTrue(vCardString.contains("+15551234567"))
        assertTrue(vCardString.contains("END:VCARD"))

        val parsedList = VCardHelper.parseVCards(vCardString)
        assertEquals(1, parsedList.size)
        val parsed = parsedList.first()
        assertEquals("John", parsed.firstName)
        assertEquals("Doe", parsed.lastName)
        assertEquals("Acme Corp", parsed.company)
        assertEquals(2, parsed.phones.size)
        assertEquals("+15551234567", parsed.phones.first().number)
        assertEquals("john.doe@acme.com", parsed.emails.first().email)
    }

    @Test
    fun testDuplicateDetectionByPhone() {
        val c1 = Contact(
            id = 1L,
            firstName = "Alice",
            lastName = "Smith",
            phones = listOf(PhoneEntry(number = "(555) 123-4567", label = "Mobile"))
        )
        val c2 = Contact(
            id = 2L,
            firstName = "A. Smith",
            lastName = "",
            phones = listOf(PhoneEntry(number = "+1 555 123 4567", label = "Mobile"))
        )
        val c3 = Contact(
            id = 3L,
            firstName = "Bob",
            lastName = "Jones",
            phones = listOf(PhoneEntry(number = "555-999-8888", label = "Mobile"))
        )

        val duplicates = DuplicateDetector.findDuplicates(listOf(c1, c2, c3))
        assertEquals(1, duplicates.size)
        assertEquals(2, duplicates[0].contacts.size)
        assertTrue(duplicates[0].contacts.any { it.id == 1L })
        assertTrue(duplicates[0].contacts.any { it.id == 2L })
    }

    @Test
    fun testDuplicateMerge() {
        val primary = Contact(
            id = 1L,
            firstName = "Salim",
            lastName = "Mansoor",
            company = "NextGen",
            phones = listOf(PhoneEntry(number = "+15551112222", label = "Mobile", isPrimary = true)),
            emails = listOf(EmailEntry(email = "salim@work.com", label = "Work")),
            notes = "Primary notes",
            tags = listOf("Work")
        )
        val secondary = Contact(
            id = 2L,
            firstName = "Salim",
            lastName = "Mansoor",
            company = "",
            address = "77 Market St",
            phones = listOf(
                PhoneEntry(number = "+15551112222", label = "Mobile"), // Duplicate number
                PhoneEntry(number = "+15553334444", label = "Home")    // New number
            ),
            emails = listOf(EmailEntry(email = "salim.personal@gmail.com", label = "Personal")),
            notes = "Secondary notes",
            tags = listOf("VIP")
        )

        val merged = DuplicateDetector.merge(primary, secondary)
        assertEquals("Salim", merged.firstName)
        assertEquals("Mansoor", merged.lastName)
        assertEquals("NextGen", merged.company)
        assertEquals("77 Market St", merged.address)
        assertEquals(2, merged.phones.size) // No duplicate numbers
        assertEquals(2, merged.emails.size)
        assertTrue(merged.tags.contains("Work"))
        assertTrue(merged.tags.contains("VIP"))
        assertTrue(merged.notes.contains("Primary notes"))
        assertTrue(merged.notes.contains("Secondary notes"))
    }

    @Test
    fun testContactMapperRoundTrip() {
        val contact = Contact(
            id = 42L,
            firstName = "Maya",
            lastName = "Lin",
            phones = listOf(PhoneEntry(number = "1234567890", label = "Mobile", isPrimary = true)),
            emails = listOf(EmailEntry(email = "maya@example.com", label = "Work")),
            tags = listOf("Friends", "VIP"),
            isFavorite = true
        )

        val entity = ContactMapper.toEntity(contact)
        assertEquals(42L, entity.id)
        assertEquals("Maya", entity.firstName)
        assertTrue(entity.isFavorite)

        val backToDomain = ContactMapper.toDomain(entity)
        assertEquals(contact.id, backToDomain.id)
        assertEquals(contact.firstName, backToDomain.firstName)
        assertEquals(contact.lastName, backToDomain.lastName)
        assertEquals(1, backToDomain.phones.size)
        assertEquals("1234567890", backToDomain.phones.first().number)
        assertEquals(2, backToDomain.tags.size)
        assertTrue(backToDomain.isFavorite)
    }
}
