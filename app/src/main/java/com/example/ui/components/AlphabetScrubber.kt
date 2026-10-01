package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SalimBlue

@Composable
fun AlphabetScrubber(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').toList() + listOf('#')

    Column(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        alphabet.forEach { char ->
            val isAvailable = availableLetters.contains(char)
            Text(
                text = char.toString(),
                fontSize = 10.sp,
                fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Normal,
                color = if (isAvailable) SalimBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                modifier = Modifier
                    .clickable(enabled = isAvailable) {
                        onLetterSelected(char)
                    }
                    .padding(vertical = 1.dp, horizontal = 2.dp)
            )
        }
    }
}
