package com.example.wearpagerexample.activity

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.wearpagerexample.nav.SampleChip
import com.google.android.horologist.compose.navscaffold.scrollableColumn

@Composable
fun ScrollableActivityScreen(
    modifier: Modifier = Modifier,
    scrollState: ScalingLazyListState,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    ScalingLazyColumn(
        modifier = modifier.scrollableColumn(focusRequester, scrollState),
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