package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val company: String = "",
    val jobTitle: String = "",
    val phonesJson: String = "[]",
    val emailsJson: String = "[]",
    val address: String = "",
    val birthday: String = "",
    val relationship: String = "",
    val notes: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val photoUri: String? = null,
    val avatarColorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
