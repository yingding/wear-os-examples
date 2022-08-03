package com.example.watchfaceconfigexample.editor

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Text
import com.example.watchfaceconfigexample.theme.WearAppTheme

/**
 * Allows user to edit certain parts of the watch face (color style, ticks displayed, minute arm
 * length) by using the [WatchFaceConfigStateHolder]. (All widgets are disabled until data is
 * loaded.)
 *
 * [ComponentActivity] Base class for activities that enables composition of higher level components.
 * Reference: https://developer.android.com/reference/androidx/activity/package-summary
 */
class WatchFaceConfigActivity : ComponentActivity() {
    private val stateHolder: WatchFaceConfigStateHolder by lazy {
        WatchFaceConfigStateHolder(
            lifecycleScope,
            this@WatchFaceConfigActivity
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            WatchfaceConfigApp()
        }
    }
}

@Composable
fun WatchfaceConfigApp() {
    WearAppTheme {
        HelloWorld()
    }
}

@Composable
fun HelloWorld() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Hello World!")
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = Devices.WEAR_OS_SMALL_ROUND,
    // showBackground = false
)
@Composable
private fun HelloWorldPreview() {
    WearAppTheme {
        HelloWorld()
    }
}

