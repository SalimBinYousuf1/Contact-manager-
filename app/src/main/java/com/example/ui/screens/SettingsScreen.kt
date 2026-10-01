package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DisplayNameOrder
import com.example.domain.ListDensity
import com.example.domain.SortOrder
import com.example.domain.ThemeMode
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimPink
import com.example.util.DeviceContactsHelper
import com.example.util.VCardHelper
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    displayNameOrder: DisplayNameOrder,
    onDisplayNameOrderChange: (DisplayNameOrder) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    listDensity: ListDensity,
    onDensityChange: (ListDensity) -> Unit,
    isAppLockEnabled: Boolean,
    appLockPin: String,
    onAppLockChange: (Boolean, String) -> Unit,
    onExportVCard: () -> Unit,
    onImportVCardContent: (String) -> Unit,
    onImportDeviceContacts: () -> Unit,
    onClearAllContacts: () -> Unit,
    totalContactsCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf(appLockPin) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPasteVCardDialog by remember { mutableStateOf(false) }
    var pastedVCardText by remember { mutableStateOf("") }

    // Launcher for selecting a .vcf file from device storage
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val content = InputStreamReader(stream).readText()
                    onImportVCardContent(content)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for READ_CONTACTS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onImportDeviceContacts()
        } else {
            Toast.makeText(context, "Contacts permission is required to import from your device", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Security PIN") },
            text = {
                Column {
                    Text("Enter a 4-digit PIN for Salim app lock:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) tempPin = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        placeholder = { Text("1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPin.length == 4) {
                            onAppLockChange(true, tempPin)
                            showPinDialog = false
                            Toast.makeText(context, "App PIN enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasteVCardDialog) {
        AlertDialog(
            onDismissRequest = { showPasteVCardDialog = false },
            title = { Text("Import from vCard Text") },
            text = {
                Column {
                    Text("Paste raw vCard (BEGIN:VCARD ... END:VCARD) text:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedVCardText,
                        onValueChange = { pastedVCardText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("BEGIN:VCARD\nVERSION:3.0\n...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedVCardText.isNotBlank()) {
                            onImportVCardContent(pastedVCardText)
                            pastedVCardText = ""
                            showPasteVCardDialog = false
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteVCardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Erase All Contacts") },
            text = { Text("Are you sure you want to permanently delete all $totalContactsCount contacts from Salim? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onClearAllContacts()
                        Toast.makeText(context, "All contacts erased", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SalimPink)
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Section 1: Display & Sorting
            SettingsSectionHeader("Organization & Display")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsSelectionRow(
                        title = "Sort Order",
                        subtitle = if (sortOrder == SortOrder.FIRST_NAME) "First Name" else "Last Name",
                        icon = Icons.Default.SortByAlpha,
                        onClick = {
                            val next = if (sortOrder == SortOrder.FIRST_NAME) SortOrder.LAST_NAME else SortOrder.FIRST_NAME
                            onSortOrderChange(next)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsSelectionRow(
                        title = "Display Name Order",
                        subtitle = if (displayNameOrder == DisplayNameOrder.FIRST_LAST) "First, Last (John Appleseed)" else "Last, First (Appleseed, John)",
                        icon = Icons.Default.Person,
                        onClick = {
                            val next = if (displayNameOrder == DisplayNameOrder.FIRST_LAST) DisplayNameOrder.LAST_FIRST else DisplayNameOrder.FIRST_LAST
                            onDisplayNameOrderChange(next)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsSelectionRow(
                        title = "List Density",
                        subtitle = if (listDensity == ListDensity.COMFORTABLE) "Comfortable" else "Compact",
                        icon = Icons.Default.FormatListBulleted,
                        onClick = {
                            val next = if (listDensity == ListDensity.COMFORTABLE) ListDensity.COMPACT else ListDensity.COMFORTABLE
                            onDensityChange(next)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Appearance
            SettingsSectionHeader("Appearance")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsSelectionRow(
                    title = "Theme",
                    subtitle = when (themeMode) {
                        ThemeMode.SYSTEM -> "System Default"
                        ThemeMode.LIGHT -> "Always Light"
                        ThemeMode.DARK -> "Always Dark"
                    },
                    icon = Icons.Default.Brightness4,
                    onClick = {
                        val next = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.SYSTEM
                        }
                        onThemeModeChange(next)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Privacy & Security
            SettingsSectionHeader("Security & Privacy")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SalimBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Passcode Lock", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Require 4-digit PIN on app launch",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    onAppLockChange(false, appLockPin)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SalimBlue)
                        )
                    }

                    if (isAppLockEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Change 4-Digit Passcode",
                                fontSize = 15.sp,
                                color = SalimBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Data, Import & Export
            SettingsSectionHeader("Data & Backup")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        title = "Export All Contacts (.vcf)",
                        subtitle = "Standard vCard 3.0 file for backup or transfer",
                        icon = Icons.Default.FileUpload,
                        onClick = onExportVCard
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Import Contacts from .vcf File",
                        subtitle = "Select a .vcf contact backup file",
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "*/*"))
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Paste vCard Text",
                        subtitle = "Directly paste vCard code",
                        icon = Icons.Default.FileUpload,
                        onClick = { showPasteVCardDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRow(
                        title = "Import from Device Contacts",
                        subtitle = "Sync contacts from Android system address book",
                        icon = Icons.Default.PhoneAndroid,
                        onClick = {
                            if (DeviceContactsHelper.hasContactsPermission(context)) {
                                onImportDeviceContacts()
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Reset / Clear
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsActionRow(
                    title = "Erase All Contacts",
                    subtitle = "$totalContactsCount stored contacts",
                    icon = Icons.Default.Delete,
                    iconColor = SalimPink,
                    titleColor = SalimPink,
                    onClick = { showDeleteConfirmDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Salim Contacts • Version 1.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "100% Offline • Private • SQLite Room Database",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsSelectionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SalimBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                fontSize = 13.sp,
                color = SalimBlue
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = SalimBlue,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = titleColor)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
