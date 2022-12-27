package com.example.wearnavexample.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.rotaryinput.rotaryWithFling

@Composable
fun NavMenuScreen(
    modifier: Modifier = Modifier,
    navigateToRoute: (String) -> Unit,
    scrollState: ScalingLazyListState,
    // focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val focusRequester = rememberActiveFocusRequester()
    ScalingLazyColumn(
        // modifier = modifier.scrollableColumn(focusRequester, scrollState),
        modifier = modifier.rotaryWithFling(focusRequester, scrollState),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally,
        autoCentering = AutoCenteringParams(itemIndex = 0)
        // autoCentering = AutoCenteringParams(itemIndex = 1, itemOffset = 30),
    ) {
        item {
            Text(text = "Menu")
        }
        item {
            SampleChip(
                onClick = { navigateToRoute(NavScreen.Activity.route) },
                label = "Activity"
            )
        }
        item {
            SampleChip(
                onClick = { navigateToRoute(NavScreen.Graph.route) },
                label = "Graph"
            )
        }
        item {
            SampleChip(
                onClick = { navigateToRoute(NavScreen.Setting.route) },
                label = "Setting"
            )
        }

    }
}


@Composable
fun SampleChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String,
    content: (@Composable () -> Unit)? = null
) {
    Chip(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ChipDefaults.primaryChipColors(),
        label = {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(modifier = Modifier.weight(1f), text = label)
                if (content != null) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        content()
                    }
                }
            }
        }
    )
}