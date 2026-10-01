package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Contact
import com.example.domain.EmailEntry
import com.example.domain.PhoneEntry
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.AvatarColors
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimPink

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactEditScreen(
    contact: Contact?,
    onSave: (Contact) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onCancel() }
    val context = LocalContext.current
    val isEditing = contact != null

    var firstName by remember { mutableStateOf(contact?.firstName ?: "") }
    var lastName by remember { mutableStateOf(contact?.lastName ?: "") }
    var nickname by remember { mutableStateOf(contact?.nickname ?: "") }
    var company by remember { mutableStateOf(contact?.company ?: "") }
    var jobTitle by remember { mutableStateOf(contact?.jobTitle ?: "") }
    var address by remember { mutableStateOf(contact?.address ?: "") }
    var birthday by remember { mutableStateOf(contact?.birthday ?: "") }
    var relationship by remember { mutableStateOf(contact?.relationship ?: "") }
    var notes by remember { mutableStateOf(contact?.notes ?: "") }
    var selectedColor by remember { mutableStateOf(contact?.avatarColorHex ?: AvatarColors.first()) }

    // Dynamic phones list
    val phones = remember {
        mutableStateListOf<PhoneEntry>().apply {
            if (contact?.phones?.isNotEmpty() == true) {
                addAll(contact.phones)
            } else {
                add(PhoneEntry(number = "", label = "Mobile", isPrimary = true))
            }
        }
    }

    // Dynamic emails list
    val emails = remember {
        mutableStateListOf<EmailEntry>().apply {
            if (contact?.emails?.isNotEmpty() == true) {
                addAll(contact.emails)
            } else {
                add(EmailEntry(email = "", label = "Personal"))
            }
        }
    }

    // Tags
    val defaultTags = listOf("Work", "Family", "Friends", "VIP", "Colleague")
    val selectedTags = remember {
        mutableStateListOf<String>().apply {
            contact?.tags?.let { addAll(it) }
        }
    }
    var customTagText by remember { mutableStateOf("") }

    val isValid = remember(firstName, lastName, company, phones, emails) {
        firstName.isNotBlank() || lastName.isNotBlank() || company.isNotBlank() ||
                phones.any { it.number.isNotBlank() } || emails.any { it.email.isNotBlank() }
    }

    val previewContact = remember(firstName, lastName, nickname, company, selectedColor) {
        Contact(
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            company = company,
            avatarColorHex = selectedColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Contact" else "New Contact",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onCancel, modifier = Modifier.testTag("edit_cancel_button")) {
                        Text("Cancel", color = SalimBlue, fontSize = 16.sp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (!isValid) {
                                Toast.makeText(context, "Please enter a name, company, or phone number", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            val cleanPhones = phones.filter { it.number.isNotBlank() }
                            val cleanEmails = emails.filter { it.email.isNotBlank() }

                            val updated = (contact ?: Contact()).copy(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                nickname = nickname.trim(),
                                company = company.trim(),
                                jobTitle = jobTitle.trim(),
                                address = address.trim(),
                                birthday = birthday.trim(),
                                relationship = relationship.trim(),
                                notes = notes.trim(),
                                avatarColorHex = selectedColor,
                                phones = cleanPhones,
                                emails = cleanEmails,
                                tags = selectedTags.toList(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(updated)
                        },
                        enabled = isValid,
                        modifier = Modifier.testTag("edit_save_button")
                    ) {
                        Text(
                            "Done",
                            color = if (isValid) SalimBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Avatar Preview & Color Selection
            ContactAvatar(
                contact = previewContact,
                size = 88.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Choose Accent Color",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                AvatarColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            // Names Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StyledTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        testTag = "input_first_name"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        testTag = "input_last_name"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = "Nickname",
                        testTag = "input_nickname"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Work & Org Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StyledTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = "Company",
                        testTag = "input_company"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = "Job Title",
                        testTag = "input_job_title"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Numbers List
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Phone Numbers",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    phones.forEachIndexed { index, phone ->
                        var showLabelMenu by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box {
                                TextButton(onClick = { showLabelMenu = true }) {
                                    Text(phone.label, color = SalimBlue, fontSize = 13.sp)
                                }
                                DropdownMenu(
                                    expanded = showLabelMenu,
                                    onDismissRequest = { showLabelMenu = false }
                                ) {
                                    listOf("Mobile", "Home", "Work", "Main", "Other").forEach { label ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                phones[index] = phone.copy(label = label)
                                                showLabelMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = phone.number,
                                onValueChange = { phones[index] = phone.copy(number = it) },
                                placeholder = { Text("Phone number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_phone_$index"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (phones.size > 1) {
                                IconButton(onClick = { phones.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove phone",
                                        tint = SalimPink
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { phones.add(PhoneEntry(number = "", label = "Mobile")) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SalimBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Phone", color = SalimBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Addresses List
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Email Addresses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    emails.forEachIndexed { index, email ->
                        var showLabelMenu by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box {
                                TextButton(onClick = { showLabelMenu = true }) {
                                    Text(email.label, color = SalimBlue, fontSize = 13.sp)
                                }
                                DropdownMenu(
                                    expanded = showLabelMenu,
                                    onDismissRequest = { showLabelMenu = false }
                                ) {
                                    listOf("Personal", "Work", "Other").forEach { label ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                emails[index] = email.copy(label = label)
                                                showLabelMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = email.email,
                                onValueChange = { emails[index] = email.copy(email = it) },
                                placeholder = { Text("Email address") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_email_$index"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (emails.size > 1) {
                                IconButton(onClick = { emails.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove email",
                                        tint = SalimPink
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { emails.add(EmailEntry(email = "", label = "Personal")) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SalimBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Email", color = SalimBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details: Address, Birthday, Relationship, Notes
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StyledTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Physical Address",
                        testTag = "input_address"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(
                        value = birthday,
                        onValueChange = { birthday = it },
                        label = "Birthday (e.g. 1990-05-15)",
                        keyboardType = KeyboardType.Text,
                        testTag = "input_birthday"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StyledTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = "Relationship (e.g. Brother, Client, Friend)",
                        testTag = "input_relationship"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags & Groups
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Groups & Tags",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val allAvailable = (defaultTags + selectedTags).distinct()
                        allAvailable.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                                label = { Text(tag) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customTagText,
                            onValueChange = { customTagText = it },
                            placeholder = { Text("Add custom tag") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = customTagText.trim()
                                if (trimmed.isNotEmpty() && !selectedTags.contains(trimmed)) {
                                    selectedTags.add(trimmed)
                                    customTagText = ""
                                }
                            },
                            enabled = customTagText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = KeyboardCapitalization.Words
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
