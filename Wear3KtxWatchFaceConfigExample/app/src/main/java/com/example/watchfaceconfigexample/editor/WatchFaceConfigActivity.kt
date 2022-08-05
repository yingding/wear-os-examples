package com.example.watchfaceconfigexample.editor

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Text
import com.example.watchfaceconfigexample.R
import com.example.watchfaceconfigexample.theme.WearAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Allows user to edit certain parts of the watch face (color style, ticks displayed, minute arm
 * length) by using the [WatchFaceConfigStateHolder]. (All widgets are disabled until data is
 * loaded.)
 *
 * [ComponentActivity] Base class for activities that enables composition of higher level components.
 * Reference: https://developer.android.com/reference/androidx/activity/package-summary
 */
class WatchFaceConfigActivity : ComponentActivity() {

    /* can not use by late, it must be called in onCreate
     * otherwise you will get an error "LifecycleOwners must call register before they are STARTED"
     * https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta/67582633#67582633
     */
    private lateinit var stateHolder: WatchFaceConfigStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stateHolder =
            WatchFaceConfigStateHolder(
                lifecycleScope,
                this@WatchFaceConfigActivity
            )

        setContent{
            var userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview?
                by remember { mutableStateOf(null)}

            // To trigger the side-effect only once during the lifecycle of this composable,
            // use a constant as a key
            LaunchedEffect(true) {
                lifecycleScope.launch(Dispatchers.Main.immediate) {
                    stateHolder.uiState
                        .collect { uiState: WatchFaceConfigStateHolder.EditWatchFaceUiState ->
                            when (uiState) {
                                is WatchFaceConfigStateHolder.EditWatchFaceUiState.Loading -> {
                                    Log.d(TAG, "StateFlow Loading: ${uiState.message}")
                                }
                                is WatchFaceConfigStateHolder.EditWatchFaceUiState.Success -> {
                                    Log.d(TAG, "StateFlow Success.")
                                    userStylesAndPreview = uiState.userStylesAndPreview
                                    // updateWatchFacePreview(uiState.userStylesAndPreview)
                                }
                                is WatchFaceConfigStateHolder.EditWatchFaceUiState.Error -> {
                                    Log.e(TAG, "Flow error: ${uiState.exception}")
                                }
                            }
                        }
                }
            }

            WatchfaceConfigApp(userStylesAndPreview)
        }
    }

    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}


@Composable
fun WatchfaceConfigApp(userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview?) {
    WearAppTheme {
        userStylesAndPreview?.let {
            WatchfaceImage(bitmap = userStylesAndPreview.previewImage)
        }
        // HelloWorld()
    }
}

@Composable
fun WatchfaceImage(bitmap: Bitmap) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.activity_config_screenshot_content_description),
            modifier = Modifier.fillMaxSize()
        )
    }
}

/*@Composable
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
}*/

