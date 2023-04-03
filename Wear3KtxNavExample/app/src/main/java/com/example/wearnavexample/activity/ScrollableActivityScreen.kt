package com.example.wearnavexample.activity

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Text
import com.example.wearnavexample.nav.SampleChip

import com.google.android.horologist.compose.rotaryinput.rotaryWithFling

@Composable
fun ScrollableActivityScreen(
    modifier: Modifier = Modifier,
    scrollState: ScalingLazyListState,
) {
    val focusRequester = rememberActiveFocusRequester()

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .rotaryWithFling(focusRequester, scrollState),
                // .scrollableColumn(focusRequester, scrollState),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally,
        autoCentering = AutoCenteringParams(itemIndex = 1, itemOffset = 10),
    ) {
        item {
            Text(text = "Activities")
        }
        item {
            SampleChip(
                onClick = {},
                label = "Running"
            )
        }
        item {
            SampleChip(
                onClick = {},
                label = "Sleeping"
            )
        }
        item {
            SampleChip(
                onClick = {},
                label = "Eating"
            )
        }
    }
}