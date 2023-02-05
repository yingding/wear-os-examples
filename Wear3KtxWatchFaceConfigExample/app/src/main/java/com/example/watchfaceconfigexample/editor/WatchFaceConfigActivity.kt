package com.example.watchfaceconfigexample.editor

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
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
import com.example.watchfaceconfigexample.editor.WatchFaceConfigStateHolder.Companion.MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER
import com.example.watchfaceconfigexample.editor.WatchFaceConfigStateHolder.Companion.MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER
import com.example.watchfaceconfigexample.theme.WearAppTheme
import com.example.watchfaceconfigexample.utils.LEFT_COMPLICATION_ID
import com.example.watchfaceconfigexample.utils.RIGHT_COMPLICATION_ID
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
                ::onClickTicksEnabledSwitch,
                ::onSliderValueChange,
                ::onClickLeftComplicationButton,
                ::onClickRightComplicationButton
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

    fun onClickLeftComplicationButton() {
        Log.d(TAG, "onClickLeftComplicationButton() ")
        this@WatchFaceConfigActivity.stateHolder.setComplication(LEFT_COMPLICATION_ID)
    }

    fun onClickRightComplicationButton() {
        Log.d(TAG, "onClickRightComplicationButton() ")
        this@WatchFaceConfigActivity.stateHolder.setComplication(RIGHT_COMPLICATION_ID)
    }

    fun onSliderValueChange(value: Float) {
        Log.d(TAG, "onSliderValueChange()")
        this@WatchFaceConfigActivity.stateHolder.setMinuteHandArmLength(value)
    }

    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}


@Composable
fun WatchfaceConfigApp(
    stateHolder: WatchFaceConfigStateHolder,
    onStyleClick: () -> Unit,
    onTickerSwitchEnabled: (Boolean) -> Unit,
    onMinuteHandArmLengthChange: (Float) -> Unit,
    onLeftComplicationClick: () -> Unit,
    onRightComplicationClick: () -> Unit,
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
                onTickerSwitchEnabled = onTickerSwitchEnabled,
                onMinuteHandArmLengthChange = onMinuteHandArmLengthChange,
                onLeftComplicationClick = onLeftComplicationClick,
                onRightComplicationClick = onRightComplicationClick
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
    onMinuteHandArmLengthChange: (Float) -> Unit,
    onLeftComplicationClick: ()-> Unit,
    onRightComplicationClick: ()-> Unit,
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
                WatchfaceImage(userStylesAndPreview.previewImage, onLeftComplicationClick, onRightComplicationClick)
            }
            item {
                MoreOptionImage()
            }
            item {
               StyleClip(onClick = onStyleClick)
            }
            item {
                TicksToggleChip(
                    isCheckedState = userStylesAndPreview.ticksEnabled,
                    onCheckedChange = onTickerSwitchEnabled)
            }
            item {
                MinuteHandSlider(
                    fraction = userStylesAndPreview.minuteHandLength,
                    valueRange = MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER..MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER,
                    onMinuteHandArmLengthChange = onMinuteHandArmLengthChange)
            }
        }
    }
}

/**
 * Use custom-layout to measure the child https://effectiveandroid.substack.com/p/custom-layouts-measuring-policies
 * or using Modifier.fillMaxWidth(0.25f) to fill the 1/4 size
 */
@Composable
fun WatchfaceImage(bitmap: Bitmap, onLeftClick: ()-> Unit, onRightClick: () -> Unit) {
    Log.v("WatchfaceImage", "updated...")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // first element fill the background
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.activity_config_screenshot_content_description),
            modifier = Modifier.fillMaxSize()
        )
        Row(modifier = Modifier
            .align(Alignment.Center) // position in the parent box
            .fillMaxWidth(),
            // .background(Color.Yellow),
            horizontalArrangement = Arrangement.SpaceEvenly // for the child element
        ){
            Button(
                modifier = Modifier.width(IntrinsicSize.Max),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent.copy()),
                onClick = {
                    Log.v("watchfaceImage", "complication clicked")
                    onLeftClick()
                }
            ) {
                // Icon(Icons.Filled.Add,"")
            }
            Button(
                modifier = Modifier.width(IntrinsicSize.Max),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent.copy()),
                onClick = {
                    Log.v("watchfaceImage", "complication clicked")
                    onRightClick()
                }
            ) {
                // Icon(Icons.Filled.Add,"")
            }

        }
/*        Button(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(IntrinsicSize.Max).padding(start = 26.dp),
            // https://effectiveandroid.substack.com/p/custom-layouts-measuring-policies
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent.copy()),
            onClick = {
                Log.v("watchfaceImage", "complication clicked")
                onLeftClick()
            }
        ) {
            // Icon(Icons.Filled.Add,"")
        }
        Button(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(IntrinsicSize.Max).padding(end = 26.dp),
            // https://effectiveandroid.substack.com/p/custom-layouts-measuring-policies
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent.copy()),
            onClick = {
                Log.v("watchfaceImage", "complication clicked")
                onRightClick()
            }
        ) {
            // Icon(Icons.Filled.Add,"")
        }*/
    }
}

@Composable
fun MoreOptionImage() {
    Image(
        painter = painterResource(R.drawable.more_options_icon),
        contentDescription = stringResource(R.string.activity_config_more_options_icon_content_description),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.CenterVertically)
    )
}

@Composable
fun MinuteHandSlider(
    fraction: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onMinuteHandArmLengthChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.activity_config_slider_text_label),
            style = MaterialTheme.typography.title3,
            modifier = Modifier.padding(8.dp)
        )
        InlineSlider(
            value = fraction,
            onValueChange = onMinuteHandArmLengthChange,
            increaseIcon = {
                Icon(InlineSliderDefaults.Increase, "Increase")
            },
            decreaseIcon = {
                Icon(InlineSliderDefaults.Decrease, "Decrease")
            },
            valueRange = valueRange,
            steps = 5,
            segmented = true
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
        appIcon = {
                  Icon(
                      painter = painterResource(R.drawable.watch_ticks),
                      contentDescription = stringResource(R.string.activity_config_ticks_enabled_switch_content_description)
                  )
        },
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


//@Composable
//fun HelloWorld() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text(text = "Hello World!")
//    }
//}
//
//@Preview(
//    uiMode = Configuration.UI_MODE_TYPE_WATCH,
//    device = Devices.WEAR_OS_SMALL_ROUND,
//    // showBackground = false
//)
//@Composable
//private fun HelloWorldPreview() {
//    WearAppTheme {
//        HelloWorld()
//    }
//}