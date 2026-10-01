package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppPreferences
import com.example.data.ContactRepository
import com.example.data.SalimDatabase
import com.example.domain.ThemeMode
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.ContactEditScreen
import com.example.ui.screens.ContactsListScreen
import com.example.ui.screens.DuplicatesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkGlassBackground
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.LightGlassBackground
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimTheme
import com.example.ui.viewmodels.AppScreen
import com.example.ui.viewmodels.ContactsViewModel
import com.example.ui.viewmodels.ContactsViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = SalimDatabase.getInstance(applicationContext)
        val repository = ContactRepository(database.contactDao())
        val preferences = AppPreferences(applicationContext)

        setContent {
            val viewModel: ContactsViewModel = viewModel(
                factory = ContactsViewModelFactory(repository, preferences)
            )

            val themeMode by viewModel.themeMode.collectAsState()

            SalimTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SalimApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SalimApp(viewModel: ContactsViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    val sortOrder by viewModel.sortOrder.collectAsState()
    val displayNameOrder by viewModel.displayNameOrder.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val listDensity by viewModel.listDensity.collectAsState()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()

    // Passcode lock screen if enabled & not yet unlocked
    if (isAppLockEnabled && !isUnlocked) {
        AppLockScreen(
            correctPin = appLockPin,
            onUnlocked = { viewModel.unlockApp() }
        )
        return
    }

    // Determine whether bottom nav should be visible (only on root tabs)
    val isRootTab = currentScreen is AppScreen.ContactsList ||
            currentScreen is AppScreen.Favorites ||
            currentScreen is AppScreen.Duplicates ||
            currentScreen is AppScreen.Settings

    Scaffold(
        bottomBar = {
            if (isRootTab) {
                FrostedBottomNavBar(
                    currentScreen = currentScreen,
                    onTabSelected = { viewModel.navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is AppScreen.ContactsList -> {
                        ContactsListScreen(
                            contacts = allContacts,
                            isLoading = isLoading,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            selectedTag = selectedTag,
                            onTagSelect = { viewModel.setSelectedTag(it) },
                            sortOrder = sortOrder,
                            displayNameOrder = displayNameOrder,
                            density = listDensity,
                            onContactClick = { contact ->
                                viewModel.navigateTo(AppScreen.Detail(contact.id))
                            },
                            onAddNewContact = {
                                viewModel.navigateTo(AppScreen.Edit(null))
                            },
                            onSeedStarterContacts = {
                                viewModel.seedStarterContacts()
                            }
                        )
                    }

                    is AppScreen.Favorites -> {
                        BackHandler { viewModel.navigateTo(AppScreen.ContactsList) }
                        FavoritesScreen(
                            favorites = favorites,
                            displayNameOrder = displayNameOrder,
                            onContactClick = { contact ->
                                viewModel.navigateTo(AppScreen.Detail(contact.id))
                            }
                        )
                    }

                    is AppScreen.Duplicates -> {
                        BackHandler { viewModel.navigateTo(AppScreen.ContactsList) }
                        DuplicatesScreen(
                            contacts = allContacts,
                            onMerge = { primary, secondary ->
                                viewModel.mergeContacts(primary, secondary)
                            }
                        )
                    }

                    is AppScreen.Settings -> {
                        BackHandler { viewModel.navigateTo(AppScreen.ContactsList) }
                        SettingsScreen(
                            sortOrder = sortOrder,
                            onSortOrderChange = { viewModel.setSortOrder(it) },
                            displayNameOrder = displayNameOrder,
                            onDisplayNameOrderChange = { viewModel.setDisplayNameOrder(it) },
                            themeMode = themeMode,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            listDensity = listDensity,
                            onDensityChange = { viewModel.setListDensity(it) },
                            isAppLockEnabled = isAppLockEnabled,
                            appLockPin = appLockPin,
                            onAppLockChange = { enabled, pin -> viewModel.setAppLock(enabled, pin) },
                            onExportVCard = { viewModel.exportAllContacts(context) },
                            onImportVCardContent = { content -> viewModel.importVCardContent(content, context) },
                            onImportDeviceContacts = { viewModel.importDeviceContacts(context) },
                            onClearAllContacts = { viewModel.clearAllContacts() },
                            totalContactsCount = allContacts.size
                        )
                    }

                    is AppScreen.Detail -> {
                        val contact = allContacts.find { it.id == targetScreen.contactId }
                        if (contact != null) {
                            ContactDetailScreen(
                                contact = contact,
                                displayNameOrder = displayNameOrder,
                                onBack = { viewModel.navigateTo(AppScreen.ContactsList) },
                                onEdit = { viewModel.navigateTo(AppScreen.Edit(contact)) },
                                onDelete = { viewModel.deleteContact(contact) },
                                onToggleFavorite = { viewModel.toggleFavorite(contact) }
                            )
                        } else {
                            viewModel.navigateTo(AppScreen.ContactsList)
                        }
                    }

                    is AppScreen.Edit -> {
                        ContactEditScreen(
                            contact = targetScreen.contact,
                            onSave = { updated ->
                                viewModel.saveContact(updated)
                            },
                            onCancel = {
                                if (targetScreen.contact != null) {
                                    viewModel.navigateTo(AppScreen.Detail(targetScreen.contact.id))
                                } else {
                                    viewModel.navigateTo(AppScreen.ContactsList)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedBottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) DarkGlassBackground else LightGlassBackground
    val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.2f))),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        containerColor = bgColor,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is AppScreen.ContactsList,
            onClick = { onTabSelected(AppScreen.ContactsList) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.ContactsList) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                    contentDescription = "Contacts"
                )
            },
            label = { Text("Contacts", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SalimBlue,
                selectedTextColor = SalimBlue,
                indicatorColor = SalimBlue.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_contacts")
        )

        NavigationBarItem(
            selected = currentScreen is AppScreen.Favorites,
            onClick = { onTabSelected(AppScreen.Favorites) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.Favorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorites"
                )
            },
            label = { Text("Favorites", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SalimBlue,
                selectedTextColor = SalimBlue,
                indicatorColor = SalimBlue.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_favorites")
        )

        NavigationBarItem(
            selected = currentScreen is AppScreen.Duplicates,
            onClick = { onTabSelected(AppScreen.Duplicates) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Duplicates"
                )
            },
            label = { Text("Duplicates", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SalimBlue,
                selectedTextColor = SalimBlue,
                indicatorColor = SalimBlue.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_duplicates")
        )

        NavigationBarItem(
            selected = currentScreen is AppScreen.Settings,
            onClick = { onTabSelected(AppScreen.Settings) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is AppScreen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SalimBlue,
                selectedTextColor = SalimBlue,
                indicatorColor = SalimBlue.copy(alpha = 0.12f)
            ),
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
