package com.example.data

import com.example.domain.Contact
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry
import org.json.JSONArray
import org.json.JSONObject

object ContactMapper {

    fun toDomain(entity: ContactEntity): Contact {
        val phones = parsePhones(entity.phonesJson)
        val emails = parseEmails(entity.emailsJson)
        val tags = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return Contact(
            id = entity.id,
            firstName = entity.firstName,
            lastName = entity.lastName,
            nickname = entity.nickname,
            company = entity.company,
            jobTitle = entity.jobTitle,
            phones = phones,
            emails = emails,
            address = entity.address,
            birthday = entity.birthday,
            relationship = entity.relationship,
            notes = entity.notes,
            tags = tags,
            isFavorite = entity.isFavorite,
            photoUri = entity.photoUri,
            avatarColorHex = entity.avatarColorHex,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            firstName = contact.firstName.trim(),
            lastName = contact.lastName.trim(),
            nickname = contact.nickname.trim(),
            company = contact.company.trim(),
            jobTitle = contact.jobTitle.trim(),
            phonesJson = serializePhones(contact.phones),
            emailsJson = serializeEmails(contact.emails),
            address = contact.address.trim(),
            birthday = contact.birthday.trim(),
            relationship = contact.relationship.trim(),
            notes = contact.notes.trim(),
            tags = contact.tags.joinToString(","),
            isFavorite = contact.isFavorite,
            photoUri = contact.photoUri,
            avatarColorHex = contact.avatarColorHex,
            createdAt = if (contact.createdAt == 0L) System.currentTimeMillis() else contact.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun serializePhones(phones: List<PhoneEntry>): String {
        val jsonArray = JSONArray()
        phones.forEach { phone ->
            val obj = JSONObject()
            obj.put("number", phone.number)
            obj.put("label", phone.label)
            obj.put("isPrimary", phone.isPrimary)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun parsePhones(json: String): List<PhoneEntry> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<PhoneEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PhoneEntry(
                        number = obj.optString("number", ""),
                        label = obj.optString("label", "Mobile"),
                        isPrimary = obj.optBoolean("isPrimary", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeEmails(emails: List<EmailEntry>): String {
        val jsonArray = JSONArray()
        emails.forEach { email ->
            val obj = JSONObject()
            obj.put("email", email.email)
            obj.put("label", email.label)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun parseEmails(json: String): List<EmailEntry> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<EmailEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    EmailEntry(
                        email = obj.optString("email", ""),
                        label = obj.optString("label", "Personal")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
