package com.example.wearpagerexample

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

import com.example.wearpagerexample.theme.WearAppTheme

/*
 * MainActivity inherits AppCompatActivity which is a ComponentActivity
 */
class MainActivity : ComponentActivity() {
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
    Surface (color = MaterialTheme.colors.background){
        Column(modifier = Modifier.fillMaxSize()) {
            // center the row horizontally inside a column
            // and use row to fillMaxHeight
            Row (modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxHeight()

            ) {
                // center vertically the content composable inside row
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.hello_world)
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