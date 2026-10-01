package com.example.util

import com.example.domain.Contact
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry

data class DuplicateGroup(
    val reason: String,
    val contacts: List<Contact>
)

object DuplicateDetector {

    fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    fun findDuplicates(contacts: List<Contact>): List<DuplicateGroup> {
        val groups = mutableListOf<DuplicateGroup>()
        val processedIds = mutableSetOf<Long>()

        // 1. Check by normalized phone numbers
        val phoneMap = mutableMapOf<String, MutableList<Contact>>()
        contacts.forEach { contact ->
            contact.phones.forEach { p ->
                val norm = normalizePhone(p.number)
                if (norm.length >= 7) {
                    val key = if (norm.length >= 9) norm.takeLast(9) else norm
                    phoneMap.getOrPut(key) { mutableListOf() }.add(contact)
                }
            }
        }

        phoneMap.values.forEach { matched ->
            val distinct = matched.distinctBy { it.id }
            if (distinct.size > 1) {
                val groupIds = distinct.map { it.id }.toSet()
                if (groupIds.none { processedIds.contains(it) }) {
                    groups.add(DuplicateGroup(reason = "Matching phone numbers", contacts = distinct))
                    processedIds.addAll(groupIds)
                }
            }
        }

        // 2. Check by exact name match
        val nameMap = mutableMapOf<String, MutableList<Contact>>()
        contacts.forEach { contact ->
            val first = contact.firstName.trim().lowercase()
            val last = contact.lastName.trim().lowercase()
            if (first.isNotEmpty() || last.isNotEmpty()) {
                val key = "$first|$last"
                nameMap.getOrPut(key) { mutableListOf() }.add(contact)
            }
        }

        nameMap.values.forEach { matched ->
            val distinct = matched.distinctBy { it.id }
            if (distinct.size > 1) {
                val groupIds = distinct.map { it.id }.toSet()
                if (groupIds.none { processedIds.contains(it) }) {
                    groups.add(DuplicateGroup(reason = "Matching name", contacts = distinct))
                    processedIds.addAll(groupIds)
                }
            }
        }

        return groups
    }

    fun merge(primary: Contact, secondary: Contact): Contact {
        // Merge phones
        val existingNormalizedNumbers = primary.phones.map { normalizePhone(it.number) }.toMutableSet()
        val mergedPhones = primary.phones.toMutableList()
        secondary.phones.forEach { phone ->
            val norm = normalizePhone(phone.number)
            if (norm.isNotEmpty() && !existingNormalizedNumbers.contains(norm)) {
                mergedPhones.add(phone.copy(isPrimary = false))
                existingNormalizedNumbers.add(norm)
            }
        }

        // Merge emails
        val existingEmails = primary.emails.map { it.email.trim().lowercase() }.toMutableSet()
        val mergedEmails = primary.emails.toMutableList()
        secondary.emails.forEach { email ->
            val lower = email.email.trim().lowercase()
            if (lower.isNotEmpty() && !existingEmails.contains(lower)) {
                mergedEmails.add(email)
                existingEmails.add(lower)
            }
        }

        // Merge tags
        val mergedTags = (primary.tags + secondary.tags).map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        // Merge notes
        val combinedNotes = when {
            primary.notes.isNotBlank() && secondary.notes.isNotBlank() ->
                if (primary.notes.contains(secondary.notes)) primary.notes else "${primary.notes}\n---\n${secondary.notes}"
            primary.notes.isNotBlank() -> primary.notes
            else -> secondary.notes
        }

        return primary.copy(
            firstName = primary.firstName.ifBlank { secondary.firstName },
            lastName = primary.lastName.ifBlank { secondary.lastName },
            nickname = primary.nickname.ifBlank { secondary.nickname },
            company = primary.company.ifBlank { secondary.company },
            jobTitle = primary.jobTitle.ifBlank { secondary.jobTitle },
            address = primary.address.ifBlank { secondary.address },
            birthday = primary.birthday.ifBlank { secondary.birthday },
            relationship = primary.relationship.ifBlank { secondary.relationship },
            notes = combinedNotes,
            phones = mergedPhones,
            emails = mergedEmails,
            tags = mergedTags,
            isFavorite = primary.isFavorite || secondary.isFavorite,
            photoUri = primary.photoUri ?: secondary.photoUri,
            updatedAt = System.currentTimeMillis()
        )
    }
}
