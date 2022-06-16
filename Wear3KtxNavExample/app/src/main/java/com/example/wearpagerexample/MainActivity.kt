package com.example.wearpagerexample

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearpagerexample.nav.NavMenuScreen
import com.example.wearpagerexample.nav.NavScreen

import com.example.wearpagerexample.theme.WearAppTheme
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scalingLazyColumnComposable
import com.google.android.horologist.compose.navscaffold.wearNavComposable

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
    WearNavScaffold(
        startDestination = NavScreen.Menu.route,
        navController = navController,
    ) {
        scalingLazyColumnComposable(
            route = NavScreen.Menu.route,
            scrollStateBuilder = { ScalingLazyListState(initialCenterItemIndex = 0) }
        ) {
            NavMenuScreen(
                navigateToRoute = { route -> navController.navigate(route)},
                scrollState = it.scrollableState,
                // focusRequester = remember { FocusRequester() } // todo: put it into a viewModel
                )
        }
        wearNavComposable(NavScreen.Activity.route) { _,_ ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Activity")
            }
        }
        wearNavComposable(NavScreen.Graph.route) { _,_ ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Graph")
            }
        }
        wearNavComposable(NavScreen.Setting.route) { _,_ ->
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