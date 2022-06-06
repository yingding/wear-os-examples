package com.example.wearpagerexample.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.wearpagerexample.R
import com.google.android.horologist.compose.layout.fadeAwayLazyList
import com.google.android.horologist.compose.layout.fadeAwayScalingLazyList
import com.google.android.horologist.compose.navscaffold.scrollableColumn

@Composable
fun ContentExample(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.hello_world)
        // text = "hello world!"
    )
}

@Composable
fun FadeAwayScreenLazyColumn() {
    val scrollState: ScalingLazyListState = rememberScalingLazyListState()
    // val scrollState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = {
            TimeText(modifier = Modifier.fadeAwayScalingLazyList(scrollStateFn = { scrollState}))
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scrollState)
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.scrollableColumn(focusRequester, scrollState),
                // .padding(top = 24.dp), // additional top padding
            state = scrollState
        ) {
            item {
                // TODO: reduce the size of spacer
                Spacer(modifier = Modifier.height(2.dp)
                    // .padding(0.dp)
                )
            }
            items(3) { i ->
                val modifier = Modifier.fillParentMaxHeight(0.4f)
                ExampleCard(modifier = modifier.padding(top = 0.dp, bottom = 0.dp), i = i)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun ExampleCard(modifier: Modifier = Modifier, i: Int) {
    Card(
        modifier = modifier,
        onClick = { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Card $i")
        }
    }
}

//@Preview(
//    device = Devices.WEAR_OS_LARGE_ROUND,
//    showSystemUi = true,
//    backgroundColor = 0xff000000,
//    showBackground = true
//)
//@Preview(
//    device = Devices.WEAR_OS_SQUARE,
//    showSystemUi = true,
//    backgroundColor = 0xff000000,
//    showBackground = true
//)
//@Composable
//fun FadeAwayScreenPreview() {
//    FadeAwayScreenLazyColumn()
//}


@Preview
@Composable
private fun ContentExamplePreview() {
    MaterialTheme {
        ContentExample()
    }
}
