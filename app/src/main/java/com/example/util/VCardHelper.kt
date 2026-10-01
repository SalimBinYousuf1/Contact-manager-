package com.example.util

import com.example.domain.Contact
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry
import java.io.BufferedReader
import java.io.StringReader

object VCardHelper {

    fun generateVCard(contact: Contact): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCARD")
        sb.appendLine("VERSION:3.0")
        sb.appendLine("N:${contact.lastName};${contact.firstName};;;")
        sb.appendLine("FN:${contact.getDisplayName()}")
        if (contact.nickname.isNotBlank()) {
            sb.appendLine("NICKNAME:${contact.nickname}")
        }
        if (contact.company.isNotBlank()) {
            sb.appendLine("ORG:${contact.company}")
        }
        if (contact.jobTitle.isNotBlank()) {
            sb.appendLine("TITLE:${contact.jobTitle}")
        }
        contact.phones.forEach { phone ->
            val type = when (phone.label.lowercase()) {
                "work" -> "WORK"
                "home" -> "HOME"
                "main" -> "MAIN"
                else -> "CELL"
            }
            sb.appendLine("TEL;TYPE=$type:${phone.number}")
        }
        contact.emails.forEach { email ->
            val type = when (email.label.lowercase()) {
                "work" -> "WORK"
                "personal", "home" -> "HOME"
                else -> "INTERNET"
            }
            sb.appendLine("EMAIL;TYPE=$type:${email.email}")
        }
        if (contact.address.isNotBlank()) {
            sb.appendLine("ADR;TYPE=HOME:;;${contact.address.replace("\n", " ")};;;;")
        }
        if (contact.birthday.isNotBlank()) {
            sb.appendLine("BDAY:${contact.birthday}")
        }
        if (contact.notes.isNotBlank()) {
            sb.appendLine("NOTE:${contact.notes.replace("\n", "\\n")}")
        }
        if (contact.tags.isNotEmpty()) {
            sb.appendLine("CATEGORIES:${contact.tags.joinToString(",")}")
        }
        sb.appendLine("END:VCARD")
        return sb.toString()
    }

    fun generateMultiVCard(contacts: List<Contact>): String {
        val sb = StringBuilder()
        contacts.forEach { contact ->
            sb.append(generateVCard(contact))
            sb.append("\n")
        }
        return sb.toString()
    }

    fun parseVCards(vCardContent: String): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val reader = BufferedReader(StringReader(vCardContent))
        var line: String?
        var inVCard = false

        var firstName = ""
        var lastName = ""
        var fn = ""
        var nickname = ""
        var company = ""
        var jobTitle = ""
        val phones = mutableListOf<PhoneEntry>()
        val emails = mutableListOf<EmailEntry>()
        var address = ""
        var birthday = ""
        var notes = ""
        val tags = mutableListOf<String>()

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.equals("BEGIN:VCARD", ignoreCase = true)) {
                inVCard = true
                firstName = ""
                lastName = ""
                fn = ""
                nickname = ""
                company = ""
                jobTitle = ""
                phones.clear()
                emails.clear()
                address = ""
                birthday = ""
                notes = ""
                tags.clear()
                continue
            }

            if (!inVCard) continue

            if (trimmed.equals("END:VCARD", ignoreCase = true)) {
                // If first and last name are empty, try parsing FN
                var finalFirst = firstName
                var finalLast = lastName
                if (finalFirst.isEmpty() && finalLast.isEmpty() && fn.isNotEmpty()) {
                    val parts = fn.split(" ", limit = 2)
                    finalFirst = parts.firstOrNull() ?: ""
                    finalLast = if (parts.size > 1) parts[1] else ""
                }

                // Only add if there is some identifying information
                if (finalFirst.isNotEmpty() || finalLast.isNotEmpty() || phones.isNotEmpty() || emails.isNotEmpty() || company.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            firstName = finalFirst,
                            lastName = finalLast,
                            nickname = nickname,
                            company = company,
                            jobTitle = jobTitle,
                            phones = phones.toList(),
                            emails = emails.toList(),
                            address = address,
                            birthday = birthday,
                            notes = notes,
                            tags = tags.toList()
                        )
                    )
                }
                inVCard = false
                continue
            }

            val colonIndex = trimmed.indexOf(':')
            if (colonIndex <= 0) continue

            val keyPart = trimmed.substring(0, colonIndex).uppercase()
            val value = trimmed.substring(colonIndex + 1).trim()

            when {
                keyPart == "N" || keyPart.startsWith("N;") -> {
                    val nParts = value.split(";")
                    lastName = nParts.getOrNull(0)?.trim() ?: ""
                    firstName = nParts.getOrNull(1)?.trim() ?: ""
                }
                keyPart == "FN" || keyPart.startsWith("FN;") -> {
                    fn = value
                }
                keyPart == "NICKNAME" -> {
                    nickname = value
                }
                keyPart == "ORG" || keyPart.startsWith("ORG;") -> {
                    company = value.replace(";", " ").trim()
                }
                keyPart == "TITLE" || keyPart.startsWith("TITLE;") -> {
                    jobTitle = value
                }
                keyPart.startsWith("TEL") -> {
                    val label = when {
                        keyPart.contains("WORK") -> "Work"
                        keyPart.contains("HOME") -> "Home"
                        keyPart.contains("MAIN") -> "Main"
                        else -> "Mobile"
                    }
                    if (value.isNotEmpty()) {
                        phones.add(PhoneEntry(number = value, label = label, isPrimary = phones.isEmpty()))
                    }
                }
                keyPart.startsWith("EMAIL") -> {
                    val label = when {
                        keyPart.contains("WORK") -> "Work"
                        keyPart.contains("HOME") -> "Personal"
                        else -> "Other"
                    }
                    if (value.isNotEmpty()) {
                        emails.add(EmailEntry(email = value, label = label))
                    }
                }
                keyPart.startsWith("ADR") -> {
                    val adrParts = value.split(";").filter { it.isNotBlank() }
                    address = adrParts.joinToString(", ")
                }
                keyPart == "BDAY" -> {
                    birthday = value
                }
                keyPart == "NOTE" -> {
                    notes = value.replace("\\n", "\n")
                }
                keyPart == "CATEGORIES" -> {
                    tags.addAll(value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }
            }
        }
        return contacts
    }
}
