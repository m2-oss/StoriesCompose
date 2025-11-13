package ru.m2.squaremeter.storiescompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreview
import ru.m2.squaremeter.stories.presentation.ui.StoriesContainer
import ru.m2.squaremeter.stories.presentation.ui.StoriesPreviewList
import ru.m2.squaremeter.storiescompose.ui.theme.StoriesComposeTheme

private const val SLIDES_COUNT = 3
private const val STORIES_DURATION_SEC = 10

class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoriesComposeTheme {
                Content(viewModel.stateFlow)
            }
        }
    }
}

@Composable
fun Content(
    stateFlow: StateFlow<MainState>,
    navController: NavHostController = rememberNavController()
) {
    val state = stateFlow.collectAsStateWithLifecycle().value
    NavHost(
        navController = navController,
        startDestination = Screen.StoriesPreview.route,
        modifier = Modifier
    ) {
        composable(Screen.StoriesPreview.route) {
            PreviewList(
                previews = state.preview,
                onClick = {
                    navController.navigate(
                        route = "${Screen.StoriesContent.route}/$it"
                    )
                }
            )
        }
        composable("${Screen.StoriesContent.route}/{storiesId}") { backStackEntry ->
            val storiesId = backStackEntry.arguments?.getString("storiesId")
                ?: error("StoriesId must be passed")
            Container(
                previews = state.preview,
                storiesId = storiesId,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun PreviewList(previews: List<UiStoriesPreview>, onClick: (String) -> Unit) {
    StoriesPreviewList(
        stories = previews,
        onClick = { onClick(it) }
    )
}

@Composable
fun Container(previews: List<UiStoriesPreview>, storiesId: String, onFinished: () -> Unit) {
    StoriesContainer(
        storiesId = storiesId,
        stories = buildMap {
            val ids = previews.map { it.id }
            ids.forEach {
                put(it, SLIDES_COUNT)
            }
        },
        durationInSec = STORIES_DURATION_SEC,
        onFinished = onFinished
    ) { stories, slide, progressBar ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            Text(text = "$stories, $slide", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoriesComposeTheme {
        Content(
            MutableStateFlow(
                MainState(
                    STORIES_PREVIEW_LIST
                )
            )
        )
    }
}