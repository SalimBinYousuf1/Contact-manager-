package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.domain.Contact

object CommunicationHelper {

    fun callPhone(context: Context, number: String) {
        val cleanNumber = number.trim()
        if (cleanNumber.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val hasCallPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(cleanNumber)}"))
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleanNumber)}"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleanNumber)}"))
                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(dialIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Unable to initiate call: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSms(context: Context, number: String, body: String = "") {
        val cleanNumber = number.trim()
        if (cleanNumber.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(cleanNumber)}")
                if (body.isNotEmpty()) {
                    putExtra("sms_body", body)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open messaging app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String, subject: String = "") {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            Toast.makeText(context, "No email address available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${Uri.encode(cleanEmail)}")
                if (subject.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open email client: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMap(context: Context, address: String) {
        val cleanAddress = address.trim()
        if (cleanAddress.isEmpty()) {
            Toast.makeText(context, "No address provided", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(cleanAddress)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareContactAsVCard(context: Context, contact: Contact) {
        try {
            val vCard = VCardHelper.generateVCard(contact)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_SUBJECT, "Contact: ${contact.getDisplayName()}")
                putExtra(Intent.EXTRA_TEXT, vCard)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share contact via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share contact: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
