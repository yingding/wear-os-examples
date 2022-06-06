package com.example.wearpagerexample

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

import com.example.wearpagerexample.main.ContentExample
import com.example.wearpagerexample.theme.WearAppTheme

/*
 * MainActivity inherits AppCompatActivity which is a ComponentActivity
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    WearAppTheme {
        CenteredHelloWorld()
    }
}

@Composable
private fun CenteredHelloWorld() {
    // create a background surface with max screen size filled with back
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        // .fillMaxWidth()
        // .fillMaxHeight(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // center the row horizontally inside a column
            // and use row to fillMaxHeight
            Row (modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxHeight()

            ) {
                // center vertically the content composable inside row
                ContentExample(
                    modifier = Modifier
                        // .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_WATCH, device = Devices.WEAR_OS_SMALL_ROUND)
@Composable
private fun CenteredHelloWorldPreview() {
    WearAppTheme {
        CenteredHelloWorld()
    }
}