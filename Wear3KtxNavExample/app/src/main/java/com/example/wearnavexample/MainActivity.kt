package com.example.wearnavexample

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearnavexample.activity.ScrollableActivityScreen
import com.example.wearnavexample.nav.NavMenuScreen
import com.example.wearnavexample.nav.NavScreen
import com.example.wearnavexample.theme.WearAppTheme
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scrollable

/*
 * MainActivity inherits AppCompatActivity which is a ComponentActivity
 */
class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            navController = rememberSwipeDismissableNavController()
            WearNavApp(navController = navController)
        }
    }
}

@Composable
fun WearApp() {
    WearAppTheme {
        CenteredHelloWorld()
    }
}


@Composable
fun WearNavApp(navController: NavHostController) {
    // remove the swipe dismiss state since not pagerScreen is used.
//    val swipeDismissState = rememberSwipeToDismissBoxState()
//    val navState = rememberSwipeDismissableNavHostState(swipeDismissState)

    WearNavScaffold(
        startDestination = NavScreen.Menu.route,
        navController = navController,
        // state = navState
    ) {
        // scalingLazyColumnComposable(
        scrollable(
            route = NavScreen.Menu.route,
            /* current bug conflict between scrollStateBuilder and the NavMenuScreen with
             * autoCentering = AutoCenteringParams(itemIndex = 1, itemOffset = 30)
             * Time text is not shown https://github.com/google/horologist/issues/245
             */
            // scrollStateBuilder = { ScalingLazyListState(initialCenterItemIndex = 2, initialCenterItemScrollOffset = -30) }
        ) {
            NavMenuScreen(
                navigateToRoute = { route -> navController.navigate(route)},
                scrollState = it.scrollableState,
                // focusRequester = remember { FocusRequester() } // todo: put it into a viewModel
                )
        }
        // scalingLazyColumnComposable(
        scrollable(
            route = NavScreen.Activity.route,
            // scrollStateBuilder = { ScalingLazyListState(initialCenterItemIndex = 2, initialCenterItemScrollOffset = -50) }
        ) {
            ScrollableActivityScreen(
                scrollState = it.scrollableState,
            )
        }
//        wearNavComposable(NavScreen.Activity.route) { _,_ ->
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text(text = "Activity")
//            }
//        }
        composable(NavScreen.Graph.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Graph")
            }
        }
        // replace wearNavComposable with composable
        composable(NavScreen.Setting.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Setting")
            }
        }
    }

}

@Composable
private fun CenteredHelloWorld() {
    // create a background surface with max screen size filled with back
    Surface (color = MaterialTheme.colors.background){
        Column(modifier = Modifier.fillMaxSize()) {
            // center the row horizontally inside a column
            // and use row to fillMaxHeight
            Row (modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxHeight()

            ) {
                // center vertically the content composable inside row
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterVertically),
                    text = stringResource(R.string.hello_world)
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_WATCH, device = Devices.WEAR_OS_SMALL_ROUND)
@Composable
private fun CenteredHelloWorldPreview() {
    WearAppTheme {
        CenteredHelloWorld()
    }
}