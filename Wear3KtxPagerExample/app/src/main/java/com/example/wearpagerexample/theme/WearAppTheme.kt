package com.example.wearpagerexample.theme

import androidx.compose.runtime.Composable
import com.google.accompanist.appcompattheme.AppCompatTheme

@Composable
fun WearAppTheme(
    content: @Composable () -> Unit
) {
    // AppCompatTheme from AppCompat Compose Theme Adapter
    // https://google.github.io/accompanist/appcompat-theme/
    AppCompatTheme(
        content = content
    )
}