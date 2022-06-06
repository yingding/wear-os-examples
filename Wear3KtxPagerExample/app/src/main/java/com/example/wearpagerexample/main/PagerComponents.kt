package com.example.wearpagerexample.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearpagerexample.R

@Composable
fun ContentExample(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.hello_world)
        // text = "hello world!"
    )
}

@Preview
@Composable
private fun ContentExamplePreview() {
    MaterialTheme {
        ContentExample()
    }
}
