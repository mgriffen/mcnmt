package dev.gr1ff3n.mcnmt.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.FieldBorder
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.SleekTextDim

/**
 * Shared OutlinedTextField colors for the dark UI: a clearly-visible unfocused
 * border (the M3 default is too faint on near-black) and an orange focus accent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mileageFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentOrange,
    unfocusedBorderColor = FieldBorder,
    focusedLabelColor = AccentOrange,
    unfocusedLabelColor = SleekTextDim,
    cursorColor = AccentOrange,
    focusedTextColor = SleekText,
    unfocusedTextColor = SleekText,
    focusedPlaceholderColor = SleekTextDim,
    unfocusedPlaceholderColor = SleekTextDim,
)
