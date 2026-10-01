package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Contact
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimOrange

@Composable
fun QuickActionRow(
    contact: Contact,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEmailClick: () -> Unit,
    onQrClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPhone = contact.phones.isNotEmpty()
    val hasEmail = contact.emails.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            icon = Icons.Default.Call,
            label = "Call",
            enabled = hasPhone,
            onClick = onCallClick,
            accentColor = SalimBlue
        )

        QuickActionButton(
            icon = Icons.Default.Message,
            label = "Message",
            enabled = hasPhone,
            onClick = onMessageClick,
            accentColor = SalimBlue
        )

        QuickActionButton(
            icon = Icons.Default.Email,
            label = "Mail",
            enabled = hasEmail,
            onClick = onEmailClick,
            accentColor = SalimBlue
        )

        QuickActionButton(
            icon = Icons.Default.QrCode,
            label = "QR Card",
            enabled = true,
            onClick = onQrClick,
            accentColor = SalimBlue
        )

        QuickActionButton(
            icon = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
            label = if (contact.isFavorite) "Favorite" else "Add Fav",
            enabled = true,
            onClick = onFavoriteToggle,
            accentColor = if (contact.isFavorite) SalimOrange else SalimBlue
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.8f else 0.3f)
    val contentColor = if (enabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(surfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
