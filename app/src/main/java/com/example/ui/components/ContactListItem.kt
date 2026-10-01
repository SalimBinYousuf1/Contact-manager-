package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.Contact
import com.example.domain.DisplayNameOrder
import com.example.domain.ListDensity
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimOrange

@Composable
fun ContactListItem(
    contact: Contact,
    displayNameOrder: DisplayNameOrder,
    density: ListDensity,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompact = density == ListDensity.COMPACT
    val avatarSize = if (isCompact) 40.dp else 48.dp
    val verticalPadding = if (isCompact) 6.dp else 10.dp
    val primaryTextSize = if (isCompact) 15.sp else 16.sp

    val subtitle = when {
        contact.jobTitle.isNotEmpty() && contact.company.isNotEmpty() -> "${contact.jobTitle} • ${contact.company}"
        contact.company.isNotEmpty() -> contact.company
        contact.phones.isNotEmpty() -> "${contact.phones.first().label}: ${contact.phones.first().number}"
        contact.emails.isNotEmpty() -> contact.emails.first().email
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = verticalPadding)
            .testTag("contact_item_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            contact = contact,
            size = avatarSize
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.getDisplayName(displayNameOrder),
                    fontSize = primaryTextSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (contact.isFavorite) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = SalimOrange,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (contact.phones.isNotEmpty()) {
            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("quick_call_${contact.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call ${contact.getDisplayName()}",
                    tint = SalimBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
