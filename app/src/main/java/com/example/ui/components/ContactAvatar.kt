package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.Contact

@Composable
fun ContactAvatar(
    contact: Contact,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSizeMultiplier: Float = 0.42f
) {
    val baseColor = try {
        Color(android.graphics.Color.parseColor(contact.avatarColorHex))
    } catch (_: Exception) {
        Color(0xFF007AFF)
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.8f)
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        if (!contact.photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(contact.photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = contact.getDisplayName(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val textSize = (size.value * fontSizeMultiplier).sp
            Text(
                text = contact.initials,
                color = Color.White,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
