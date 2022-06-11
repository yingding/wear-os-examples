package com.example.wearpagerexample.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.edgeSwipeToDismiss
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.google.accompanist.pager.rememberPagerState
import com.google.android.horologist.compose.pager.PagerScreen

@Composable
fun HorizontalPagers() {
    val swipeDismissState = rememberSwipeToDismissBoxState()
    val pagerState = rememberPagerState()

    PagerScreen(
        modifier = Modifier
            .fillMaxSize()
            .edgeSwipeToDismiss(swipeDismissState),
        count = 10,
        state = pagerState
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Screen $it")
        }
    }
}