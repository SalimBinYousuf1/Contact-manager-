package com.example.domain

data class PhoneEntry(
    val number: String,
    val label: String = "Mobile",
    val isPrimary: Boolean = false
)

data class EmailEntry(
    val email: String,
    val label: String = "Personal"
)

enum class SortOrder {
    FIRST_NAME,
    LAST_NAME
}

enum class DisplayNameOrder {
    FIRST_LAST,
    LAST_FIRST
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ListDensity {
    COMFORTABLE,
    COMPACT
}

data class Contact(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val company: String = "",
    val jobTitle: String = "",
    val phones: List<PhoneEntry> = emptyList(),
    val emails: List<EmailEntry> = emptyList(),
    val address: String = "",
    val birthday: String = "",
    val relationship: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val photoUri: String? = null,
    val avatarColorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getDisplayName(order: DisplayNameOrder = DisplayNameOrder.FIRST_LAST): String {
        val f = firstName.trim()
        val l = lastName.trim()
        return when {
            f.isNotEmpty() && l.isNotEmpty() -> {
                if (order == DisplayNameOrder.LAST_FIRST) "$l, $f" else "$f $l"
            }
            f.isNotEmpty() -> f
            l.isNotEmpty() -> l
            nickname.isNotBlank() -> nickname.trim()
            company.isNotBlank() -> company.trim()
            phones.isNotEmpty() -> phones.first().number
            emails.isNotEmpty() -> emails.first().email
            else -> "Unnamed Contact"
        }
    }

    val initials: String
        get() {
            val f = firstName.trim()
            val l = lastName.trim()
            val firstChar = f.firstOrNull()?.uppercaseChar()
            val lastChar = l.firstOrNull()?.uppercaseChar()
            return when {
                firstChar != null && lastChar != null -> "$firstChar$lastChar"
                firstChar != null -> "$firstChar"
                lastChar != null -> "$lastChar"
                nickname.isNotBlank() -> nickname.trim().take(1).uppercase()
                company.isNotBlank() -> company.trim().take(1).uppercase()
                else -> "?"
            }
        }

    fun getSortLetter(order: SortOrder = SortOrder.FIRST_NAME): Char {
        val primary = if (order == SortOrder.FIRST_NAME) firstName.trim() else lastName.trim()
        val secondary = if (order == SortOrder.FIRST_NAME) lastName.trim() else firstName.trim()
        val target = when {
            primary.isNotEmpty() -> primary
            secondary.isNotEmpty() -> secondary
            company.isNotEmpty() -> company
            nickname.isNotEmpty() -> nickname
            else -> ""
        }
        val firstChar = target.firstOrNull()?.uppercaseChar() ?: '#'
        return if (firstChar in 'A'..'Z') firstChar else '#'
    }
}
