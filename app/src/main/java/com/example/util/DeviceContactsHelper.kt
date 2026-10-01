package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.domain.Contact
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry

object DeviceContactsHelper {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun readDeviceContacts(context: Context): List<Contact> {
        if (!hasContactsPermission(context)) return emptyList()

        val contactMap = mutableMapOf<String, MutableContactBuilder>()
        val resolver = context.contentResolver

        // 1. Query phone numbers
        try {
            val phoneCursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE
                ),
                null,
                null,
                null
            )

            phoneCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: ""
                    val num = cursor.getString(numIdx) ?: ""
                    val typeVal = cursor.getInt(typeIdx)

                    val label = when (typeVal) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                        ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Main"
                        else -> "Mobile"
                    }

                    val builder = contactMap.getOrPut(id) { MutableContactBuilder(rawName = name) }
                    if (num.isNotBlank()) {
                        builder.phones.add(PhoneEntry(number = num.trim(), label = label, isPrimary = builder.phones.isEmpty()))
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query emails
        try {
            val emailCursor = resolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Email.TYPE
                ),
                null,
                null,
                null
            )

            emailCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
                val addrIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: ""
                    val addr = cursor.getString(addrIdx) ?: ""
                    val typeVal = cursor.getInt(typeIdx)

                    val label = when (typeVal) {
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "Work"
                        ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "Personal"
                        else -> "Other"
                    }

                    val builder = contactMap.getOrPut(id) { MutableContactBuilder(rawName = name) }
                    if (addr.isNotBlank()) {
                        builder.emails.add(EmailEntry(email = addr.trim(), label = label))
                    }
                }
            }
        } catch (_: Exception) {}

        return contactMap.values.map { builder ->
            val parts = builder.rawName.trim().split(" ", limit = 2)
            val first = parts.firstOrNull() ?: ""
            val last = if (parts.size > 1) parts[1] else ""

            Contact(
                firstName = first,
                lastName = last,
                phones = builder.phones.distinctBy { it.number },
                emails = builder.emails.distinctBy { it.email }
            )
        }
    }

    private class MutableContactBuilder(
        var rawName: String,
        val phones: MutableList<PhoneEntry> = mutableListOf(),
        val emails: MutableList<EmailEntry> = mutableListOf()
    )
}
