package com.example.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.ContactRepository
import com.example.domain.Contact
import com.example.domain.DisplayNameOrder
import com.example.domain.EmailEntry
import com.example.domain.ListDensity
import com.example.domain.PhoneEntry
import com.example.domain.SortOrder
import com.example.domain.ThemeMode
import com.example.util.CommunicationHelper
import com.example.util.DeviceContactsHelper
import com.example.util.VCardHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppScreen {
    object ContactsList : AppScreen()
    object Favorites : AppScreen()
    object Duplicates : AppScreen()
    object Settings : AppScreen()
    data class Detail(val contactId: Long) : AppScreen()
    data class Edit(val contact: Contact?) : AppScreen() // null for new contact
}

class ContactsViewModel(
    private val repository: ContactRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    val allContacts: StateFlow<List<Contact>> = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites: StateFlow<List<Contact>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.ContactsList)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Passcode lock state
    private val _isUnlocked = MutableStateFlow(!preferences.isAppLockEnabled.value)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Preferences forwarded from AppPreferences
    val sortOrder: StateFlow<SortOrder> = preferences.sortOrder
    val displayNameOrder: StateFlow<DisplayNameOrder> = preferences.displayNameOrder
    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
    val listDensity: StateFlow<ListDensity> = preferences.listDensity
    val isAppLockEnabled: StateFlow<Boolean> = preferences.isAppLockEnabled
    val appLockPin: StateFlow<String> = preferences.appLockPin

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun setSortOrder(order: SortOrder) = preferences.setSortOrder(order)
    fun setDisplayNameOrder(order: DisplayNameOrder) = preferences.setDisplayNameOrder(order)
    fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
    fun setListDensity(density: ListDensity) = preferences.setListDensity(density)
    fun setAppLock(enabled: Boolean, pin: String) = preferences.setAppLock(enabled, pin)

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            val id = repository.saveContact(contact)
            _currentScreen.value = AppScreen.Detail(if (contact.id != 0L) contact.id else id)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            _currentScreen.value = AppScreen.ContactsList
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            repository.toggleFavorite(contact.id, contact.isFavorite)
        }
    }

    fun mergeContacts(primary: Contact, secondary: Contact) {
        viewModelScope.launch {
            repository.mergeContacts(primary, secondary)
        }
    }

    fun exportAllContacts(context: Context) {
        val contacts = allContacts.value
        if (contacts.isEmpty()) {
            Toast.makeText(context, "No contacts to export", Toast.LENGTH_SHORT).show()
            return
        }
        val vCardData = VCardHelper.generateMultiVCard(contacts)
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Salim Contacts Export (${contacts.size} contacts)")
                putExtra(android.content.Intent.EXTRA_TEXT, vCardData)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Export Contacts via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun importVCardContent(vCardContent: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val parsed = VCardHelper.parseVCards(vCardContent)
                if (parsed.isEmpty()) {
                    Toast.makeText(context, "No valid contacts found in vCard data", Toast.LENGTH_SHORT).show()
                } else {
                    val count = repository.importContacts(parsed)
                    Toast.makeText(context, "Imported $count contact(s) successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to import vCard: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importDeviceContacts(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val deviceContacts = DeviceContactsHelper.readDeviceContacts(context)
                if (deviceContacts.isEmpty()) {
                    Toast.makeText(context, "No contacts found on device or permission not granted", Toast.LENGTH_SHORT).show()
                } else {
                    val count = repository.importContacts(deviceContacts)
                    Toast.makeText(context, "Imported $count contacts from device", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            repository.deleteAllContacts()
            _currentScreen.value = AppScreen.ContactsList
        }
    }

    fun seedStarterContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            val starters = listOf(
                Contact(
                    firstName = "Salim",
                    lastName = "Mansoor",
                    nickname = "Sam",
                    company = "NextGen Tech",
                    jobTitle = "Lead Architect",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 234-5678", label = "Mobile", isPrimary = true),
                        PhoneEntry(number = "+1 (555) 876-5432", label = "Work", isPrimary = false)
                    ),
                    emails = listOf(
                        EmailEntry(email = "ask.salimm@gmail.com", label = "Personal"),
                        EmailEntry(email = "salim@nextgentech.io", label = "Work")
                    ),
                    address = "100 Innovation Way, Suite 400, San Francisco, CA",
                    birthday = "1992-06-14",
                    relationship = "Colleague",
                    notes = "Principal system architect. Prefers Slack or email during morning focus hours.",
                    tags = listOf("Work", "VIP"),
                    isFavorite = true,
                    avatarColorHex = "#007AFF"
                ),
                Contact(
                    firstName = "Alexander",
                    lastName = "Wright",
                    nickname = "Alex",
                    company = "Apex Design Labs",
                    jobTitle = "Creative Director",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 345-6789", label = "Mobile", isPrimary = true)
                    ),
                    emails = listOf(
                        EmailEntry(email = "alex.wright@apexdesign.co", label = "Work")
                    ),
                    address = "420 Market Street, San Francisco, CA",
                    birthday = "1988-11-22",
                    relationship = "Partner",
                    notes = "Collaborated on interface design guidelines.",
                    tags = listOf("Work", "Design"),
                    isFavorite = true,
                    avatarColorHex = "#5856D6"
                ),
                Contact(
                    firstName = "Elena",
                    lastName = "Rostova",
                    company = "Global Ventures",
                    jobTitle = "Partner",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 456-7890", label = "Mobile", isPrimary = true)
                    ),
                    emails = listOf(
                        EmailEntry(email = "elena@globalventures.com", label = "Work")
                    ),
                    address = "500 Madison Ave, New York, NY",
                    relationship = "Investor",
                    notes = "Quarterly strategic advisory call every first Monday.",
                    tags = listOf("VIP", "Work"),
                    isFavorite = false,
                    avatarColorHex = "#FF2D55"
                ),
                Contact(
                    firstName = "David",
                    lastName = "Chen",
                    nickname = "Dave",
                    company = "Stanford Medical",
                    jobTitle = "Neurosurgeon",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 567-8901", label = "Mobile", isPrimary = true),
                        PhoneEntry(number = "+1 (555) 789-0123", label = "Home", isPrimary = false)
                    ),
                    emails = listOf(
                        EmailEntry(email = "d.chen@stanfordmed.org", label = "Work")
                    ),
                    address = "300 Pasteur Dr, Stanford, CA",
                    birthday = "1985-04-03",
                    relationship = "Friend",
                    notes = "College roommate. Call after 6 PM.",
                    tags = listOf("Friends", "VIP"),
                    isFavorite = true,
                    avatarColorHex = "#34C759"
                ),
                Contact(
                    firstName = "Sophia",
                    lastName = "Martinez",
                    company = "Starlight Studios",
                    jobTitle = "Cinematographer",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 678-9012", label = "Mobile", isPrimary = true)
                    ),
                    emails = listOf(
                        EmailEntry(email = "sophia@starlight.la", label = "Work")
                    ),
                    address = "1024 Hollywood Blvd, Los Angeles, CA",
                    relationship = "Colleague",
                    notes = "Media production and photography lead.",
                    tags = listOf("Work"),
                    isFavorite = false,
                    avatarColorHex = "#FF9500"
                ),
                Contact(
                    firstName = "Marcus",
                    lastName = "Sterling",
                    nickname = "Marc",
                    company = "Sterling & Partners",
                    jobTitle = "General Counsel",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 987-6543", label = "Mobile", isPrimary = true)
                    ),
                    emails = listOf(
                        EmailEntry(email = "m.sterling@sterlinglaw.com", label = "Work")
                    ),
                    address = "200 Park Avenue, New York, NY",
                    relationship = "Advisor",
                    notes = "Legal counsel and IP patents.",
                    tags = listOf("Work"),
                    isFavorite = false,
                    avatarColorHex = "#00C7BE"
                ),
                Contact(
                    firstName = "Maya",
                    lastName = "Patel",
                    company = "BioHealth Dynamics",
                    jobTitle = "Chief Research Officer",
                    phones = listOf(
                        PhoneEntry(number = "+1 (555) 123-4567", label = "Mobile", isPrimary = true)
                    ),
                    emails = listOf(
                        EmailEntry(email = "maya.patel@biohealth.org", label = "Work")
                    ),
                    address = "1200 Beacon St, Boston, MA",
                    birthday = "1994-09-18",
                    relationship = "Friend",
                    notes = "Met at MIT technology symposium.",
                    tags = listOf("Friends", "Work"),
                    isFavorite = false,
                    avatarColorHex = "#AF52DE"
                )
            )
            repository.importContacts(starters)
            _isLoading.value = false
        }
    }
}

class ContactsViewModelFactory(
    private val repository: ContactRepository,
    private val preferences: AppPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactsViewModel(repository, preferences) as T
    }
}
