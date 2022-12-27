package com.example.wearnavexample.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun WearAppTheme(
    content: @Composable () -> Unit
) {
    // AppCompatTheme from AppCompat Compose Theme Adapter
    // https://google.github.io/accompanist/appcompat-theme/
    MaterialTheme(
        colors = WearAppColorPalette,
        typography = WearAppTypography,
        content = content
    )
}