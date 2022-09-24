package com.example.watchfaceconfigexample.editor

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListAnchorType
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.watchfaceconfigexample.R
import com.example.watchfaceconfigexample.data.watchface.ColorStyleIdAndResourceIds
import com.example.watchfaceconfigexample.theme.WearAppTheme
import kotlinx.coroutines.Dispatchers

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

//    private val stateHolder: WatchFaceConfigStateHolder by lazy {
//        WatchFaceConfigStateHolder(
//            lifecycleScope,
//            this@WatchFaceConfigActivity
//        )
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate...")

        stateHolder =
            WatchFaceConfigStateHolder(
                lifecycleScope,
                this@WatchFaceConfigActivity
            )

        setContent{
            WatchfaceConfigApp(
                stateHolder,
                ::onClickColorStylePickerButton,
                ::onClickTicksEnabledSwitch
            )
        }
    }

    fun onClickColorStylePickerButton() {
        Log.d(TAG, "onClickColorStylePickerButton()")

        // TODO (codingjeremy): Replace with a RecyclerView to choose color style (next CL)
        // Selects a random color style from list.
        val colorStyleIdAndResourceIdsList = enumValues<ColorStyleIdAndResourceIds>()
        val newColorStyle: ColorStyleIdAndResourceIds = colorStyleIdAndResourceIdsList.random()
        Log.d(TAG, "onClickColorStylePickerButton() set ${newColorStyle.id}")

        this@WatchFaceConfigActivity.stateHolder.setColorStyle(newColorStyle.id)
    }

    fun onClickTicksEnabledSwitch(enabled: Boolean) {
        Log.d(TAG, "onClickTicksEnabledSwitch() $enabled")
        this@WatchFaceConfigActivity.stateHolder.setDrawPips(enabled)
    }


    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}


@Composable
fun WatchfaceConfigApp(
    stateHolder: WatchFaceConfigStateHolder,
    // userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview?,
    onStyleClick: () -> Unit,
    onTickerSwitchEnabled: (Boolean) -> Unit
) {
    val editWatchFaceUiState: WatchFaceConfigStateHolder.EditWatchFaceUiState by stateHolder.uiState.collectAsState(Dispatchers.Main.immediate)

    WearAppTheme {
        // show the first element with autoCenter up of 30
        val stateInit by remember { mutableStateOf(StateInit(0, 30)) }
        val state: ScalingLazyListState =
            rememberScalingLazyListState(stateInit.index, stateInit.offSet)

        Log.v("WatchfaceConfigApp", "editWatchFaceUiState changed...")

        if (editWatchFaceUiState is WatchFaceConfigStateHolder.EditWatchFaceUiState.Success) {
            Log.v("WatchfaceConfigApp", editWatchFaceUiState.toString())
            WatchFaceConfigContent(
                stateInit = stateInit,
                state = state,
                userStylesAndPreview = (editWatchFaceUiState as WatchFaceConfigStateHolder.EditWatchFaceUiState.Success).userStylesAndPreview,
                onStyleClick = onStyleClick,
                onTickerSwitchEnabled = onTickerSwitchEnabled
            )
        }
    }
}


data class StateInit(val index: Int, val offSet: Int)

@Composable
fun WatchFaceConfigContent(
    stateInit: StateInit,
    state: ScalingLazyListState,
    userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview,
    onStyleClick: () -> Unit,
    onTickerSwitchEnabled: (Boolean) -> Unit,
) {
    Scaffold (
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = state) }
    ) {
        ScalingLazyColumn (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            state = state,
            anchorType = ScalingLazyListAnchorType.ItemCenter,
            autoCentering = AutoCenteringParams(itemIndex = stateInit.index, itemOffset = stateInit.offSet)
            // the autoCentering param must be the same as the sate param, since we have full screen element, default 1, 0 will not work.
        ) {
            item {
                WatchfaceImage(userStylesAndPreview.previewImage)
            }
            item {
               StyleClip(onClick = onStyleClick)
            }
            item {
                TicksToggleChip(
                    isCheckedState = userStylesAndPreview.ticksEnabled,
                    onCheckedChange = onTickerSwitchEnabled)
            }
        }
    }
}

@Composable
fun WatchfaceImage(bitmap: Bitmap) {
    Log.v("WatchfaceImage", "updated...")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.activity_config_screenshot_content_description),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StyleClip(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Chip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                text = stringResource(R.string.activity_config_color_style_picker_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.color_style_icon),
                contentDescription = stringResource(R.string.activity_config_change_color_style_button_content_description),
                modifier = iconModifier
            )
        }
    )
}

@Composable
fun TicksToggleChip(
    modifier: Modifier = Modifier,
    isCheckedState: Boolean = true,
    onCheckedChange: (Boolean)->Unit
) {
    // var checked by remember { mutableStateOf(isCheckedState) }
    ToggleChip(
        modifier = modifier,
        checked = isCheckedState,
//        appIcon = {
//                  Icon(
//                      painter = painterResource(R.drawable.color_style_icon),
//                      contentDescription = stringResource(R.string.activity_config_change_color_style_button_content_description)
//                  )
//        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked = isCheckedState),
                contentDescription = if (isCheckedState) "checked" else "Unchecked"
            )
        },
        onCheckedChange = {
            // checked = it
            onCheckedChange(it)
        },
        label = {
            Text(
                text = "TICKS ENABLED",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    )

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

