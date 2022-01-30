/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.composeforwearos

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListAnchorType
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.android.wearable.composeforwearos.theme.WearAppTheme

/**
 * This code lab is meant to help existing Compose developers get up to speed quickly on
 * Compose for Wear OS.
 *
 * The code lab walks through a majority of the simple composables for Wear OS (both similar to
 * existing mobile composables and new composables).
 *
 * It also covers more advanced composables like [ScalingLazyColumn] (Wear OS's version of
 * [LazyColumn]) and the Wear OS version of [Scaffold].
 *
 * Check out [this link](https://android-developers.googleblog.com/2021/10/compose-for-wear-os-now-in-developer.html)
 * for more information on Compose for Wear OS.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp()
        }
    }
}

// @OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun WearApp() {

    WearAppTheme {
        // Get Screen Info
        /*
        val configuration = LocalConfiguration.current
        val screenHeight: Int = configuration.screenHeightDp
        Log.v("screenHeight", "${screenHeight}")
        */

        // TODO: Swap to ScalingLazyListState
        // val listState = rememberLazyListState()
        // val listState: ScalingLazyListState = rememberScalingLazyListState()

        val initCenterItemOffset = -21
        val initCenterItemIdx = 1
        val listState: ScalingLazyListState = rememberScalingLazyListState(
            initialCenterItemIndex = initCenterItemIdx,
            initialCenterItemScrollOffset = initCenterItemOffset
        )

        /* *************************** Part 4: Wear OS Scaffold *************************** */
        // TODO (Start): Create a Scaffold (Wear Version)
        Scaffold(
            timeText = {
                /*
                if (!listState.isScrollInProgress ) {
                           TimeText()
                }
                */

                // Alpha14 wear compose solution
                // ScalingLazyColumn scroll up 15dp then deactivate TimeText
                if (
                    (
                        listState.centerItemIndex == initCenterItemIdx &&
                            listState.centerItemScrollOffset <= initCenterItemOffset + 15
                        ) ||
                    (listState.centerItemIndex == 0)
                ) { // scroll up addes offset
                    TimeText()
                } else {
                    Log.d("no timetext", "itemIndex ${listState.centerItemIndex}, centerOffset: ${listState.centerItemScrollOffset}")
                }

                /*
                // Alpha13 wear compose solution
                // the first element is scrolled up more than 15dp, deactivate the Time Text
                val itemInfoList = listState.layoutInfo.visibleItemsInfo
                if (itemInfoList.isNotEmpty()
                    && itemInfoList[0].index == 0 // the first visible index is the first element
                    && itemInfoList[0].offset >= -15 // the first element is scrolled up less than 15dp
                ) {
                    TimeText()
                }
                */
            },
            vignette = {
                // Only show a Vignette for scrollable screens. This code lab only has one screen,
                // which is scrollable, so we show it all the time.
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {

            // Modifiers used by our Wear composables.
            val contentModifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            val iconModifier = Modifier.size(24.dp).wrapContentSize(align = Alignment.Center)

            /* *************************** Part 3: ScalingLazyColumn *************************** */
            // TODO: Swap a ScalingLazyColumn (Wear's version of LazyColumn)
            // LazyColumn(
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 32.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.Center,
                state = listState,
                anchorType = ScalingLazyListAnchorType.ItemCenter
            ) {

                // TODO: Remove item; for beginning only.
                // item { StartOnlyTextComposables() }
                // item { TimeTextExample(contentModifier) }

                /* ******************* Part 1: Simple composables ******************* */
                item { ButtonExample(contentModifier, iconModifier) }
                item { TextExample(contentModifier) }
                item { CardExample(contentModifier, iconModifier) }

                /* ********************* Part 2: Wear unique composables ********************* */
                item { ChipExample(contentModifier, iconModifier) }
                item { ToggleChipExample(contentModifier) }
            }

            // TODO (End): Create a Scaffold (Wear Version)
        }
    }
}

// fun ScalingLazyListState.firstElementOffSet():Int = {
//    val itemInfoList = this.layoutInfo.visibleItemsInfo
//    if (itemInfoList.isNotEmpty() && itemInfoList[0].index == 0) {
//        return itemInfoList[0].offset
//    } else {
//        return Int.MAX_VALUE
//    }
// }

// Note: Preview in Android Studio doesn't support the round view yet (coming soon).
// @Preview(
//    apiLevel = 25,
//    uiMode = Configuration.UI_MODE_TYPE_WATCH,
//    showSystemUi = true
// )
@Preview(
    // widthDp = WEAR_PREVIEW_DEVICE_WIDTH_DP,
    // heightDp = WEAR_PREVIEW_DEVICE_HEIGHT_DP,
    apiLevel = WEAR_PREVIEW_API_LEVEL,
    // uiMode = WEAR_PREVIEW_UI_MODE,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    backgroundColor = WEAR_PREVIEW_BACKGROUND_COLOR_BLACK,
    showBackground = WEAR_PREVIEW_SHOW_BACKGROUND,
    showSystemUi = false, // the android system bar on top
    device = Devices.WEAR_OS_SQUARE
)
@Preview(
    apiLevel = WEAR_PREVIEW_API_LEVEL,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = Devices.WEAR_OS_SMALL_ROUND
)
@Composable
fun WearAppPreview() {
    WearApp()
}
