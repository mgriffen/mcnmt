package dev.gr1ff3n.mcnmt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.appBackground

/**
 * Shared screen shell for the sleek-dark UI: the gradient backdrop with a
 * transparent app bar floating over it, so every screen reads as one continuous
 * surface. Title and back icon use the light text color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(Modifier.appBackground()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = SleekText,
                        navigationIconContentColor = SleekText,
                    ),
                )
            },
            floatingActionButton = floatingActionButton,
            content = content,
        )
    }
}
