package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Contact
import com.example.domain.DisplayNameOrder
import com.example.domain.ListDensity
import com.example.domain.SortOrder
import com.example.ui.components.AlphabetScrubber
import com.example.ui.components.ContactListItem
import com.example.ui.components.FrostedGlassBox
import com.example.ui.theme.SalimBlue
import com.example.util.CommunicationHelper
import kotlinx.coroutines.launch

@Composable
fun ContactsListScreen(
    contacts: List<Contact>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    sortOrder: SortOrder,
    displayNameOrder: DisplayNameOrder,
    density: ListDensity,
    onContactClick: (Contact) -> Unit,
    onAddNewContact: () -> Unit,
    onSeedStarterContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Filter contacts by search query & tag
    val filteredContacts = remember(contacts, searchQuery, selectedTag) {
        val q = searchQuery.trim().lowercase()
        contacts.filter { contact ->
            val matchesQuery = if (q.isEmpty()) true else {
                contact.firstName.lowercase().contains(q) ||
                        contact.lastName.lowercase().contains(q) ||
                        contact.nickname.lowercase().contains(q) ||
                        contact.company.lowercase().contains(q) ||
                        contact.phones.any { it.number.contains(q) } ||
                        contact.emails.any { it.email.lowercase().contains(q) }
            }
            val matchesTag = if (selectedTag == null) true else contact.tags.contains(selectedTag)
            matchesQuery && matchesTag
        }
    }

    // Sort contacts
    val sortedContacts = remember(filteredContacts, sortOrder) {
        filteredContacts.sortedWith(compareBy<Contact> {
            it.getSortLetter(sortOrder)
        }.thenBy {
            if (sortOrder == SortOrder.FIRST_NAME) it.firstName.lowercase() else it.lastName.lowercase()
        }.thenBy {
            if (sortOrder == SortOrder.FIRST_NAME) it.lastName.lowercase() else it.firstName.lowercase()
        })
    }

    // Group contacts by alphabetical section
    val groupedContacts = remember(sortedContacts, sortOrder) {
        sortedContacts.groupBy { it.getSortLetter(sortOrder) }
    }

    // Extract all unique tags
    val availableTags = remember(contacts) {
        contacts.flatMap { it.tags }.distinct().sorted()
    }

    // Available alphabet letters for scrubber
    val availableLetters = remember(groupedContacts) {
        groupedContacts.keys.toSet()
    }

    // Section index mapping for scrolling
    val sectionIndexMap = remember(groupedContacts) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 0
        groupedContacts.forEach { (char, list) ->
            map[char] = currentIndex
            currentIndex += 1 + list.size // header + items
        }
        map
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewContact,
                containerColor = SalimBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_contact")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Contact",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header: Title & Total Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Contacts",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${contacts.size} contacts",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onAddNewContact,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SalimBlue.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "New Contact",
                        tint = SalimBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search name, number, company...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = SalimBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contacts_search_input")
                )
            }

            // Tag Filter Chips (Scrollable Row)
            if (availableTags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { onTagSelect(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SalimBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(availableTags) { tag ->
                        val isSelected = selectedTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTagSelect(if (isSelected) null else tag) },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SalimBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Main Content Area
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SalimBlue)
                }
            } else if (contacts.isEmpty()) {
                // Empty State: Brand new clean state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SalimBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = "Empty Contacts",
                            tint = SalimBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Welcome to Salim",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your private, offline contacts manager. Add your first contact or load starter data to get started.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onAddNewContact,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).testTag("empty_add_contact_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Contact")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onSeedStarterContacts,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).testTag("seed_starter_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Starter Contacts")
                    }
                }
            } else if (sortedContacts.isEmpty()) {
                // Search returned no results
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No Results",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Contacts Found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No results matching \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Contacts List with Section Headers and Alphabet Scrubber
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedContacts.forEach { (char, contactGroup) ->
                            item(key = "header_$char") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SalimBlue
                                    )
                                }
                            }

                            items(contactGroup, key = { it.id }) { contact ->
                                ContactListItem(
                                    contact = contact,
                                    displayNameOrder = displayNameOrder,
                                    density = density,
                                    onClick = { onContactClick(contact) },
                                    onCallClick = {
                                        contact.phones.firstOrNull()?.let {
                                            CommunicationHelper.callPhone(context, it.number)
                                        }
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 74.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // Bottom count footer
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${sortedContacts.size} Contacts",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }

                    // A-Z Scrubber along the right edge
                    if (availableLetters.size > 2) {
                        AlphabetScrubber(
                            availableLetters = availableLetters,
                            onLetterSelected = { selectedChar ->
                                sectionIndexMap[selectedChar]?.let { targetIndex ->
                                    coroutineScope.launch {
                                        listState.scrollToItem(targetIndex)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
