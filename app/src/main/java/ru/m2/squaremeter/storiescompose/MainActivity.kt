package ru.m2.squaremeter.storiescompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.container.presentation.ui.StoriesContainer
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreviewData
import ru.m2.squaremeter.stories.preview.presentation.ui.StoriesPreviewList
import ru.m2.squaremeter.storiescompose.ui.theme.StoriesComposeTheme

private const val SLIDES_COUNT = 3
private const val STORIES_DURATION_SEC = 10
private val SLIDES_COLORS = listOf(
    Color.LightGray,
    Color.Gray,
    Color.DarkGray
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoriesComposeTheme {
                Content(STORIES_PREVIEW_LIST)
            }
        }
    }
}

@Composable
fun Content(
    previews: List<UiStoriesPreviewData>,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StoriesPreview.route,
        modifier = Modifier
    ) {
        composable(Screen.StoriesPreview.route) {
            PreviewList(
                previews = previews,
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
                previews = previews,
                storiesId = storiesId,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun PreviewList(previews: List<UiStoriesPreviewData>, onClick: (String) -> Unit) {
    StoriesPreviewList(
        previews = previews,
        onClick = { onClick(it) }
    )
}

@Composable
fun Container(previews: List<UiStoriesPreviewData>, storiesId: String, onFinished: () -> Unit) {
    StoriesContainer(
        data = UiStoriesData(
            storiesId = storiesId,
            stories = buildMap {
                val ids = previews.map { it.id }
                ids.forEach {
                    put(it, SLIDES_COUNT)
                }
            },
            durationInSec = STORIES_DURATION_SEC
        ),
        onFinished = onFinished
    ) { stories, slide, progressBar ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SLIDES_COLORS[slide])
                .offset(y = progressBar)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null
            )
            Text(text = "$stories, $slide", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoriesComposeTheme {
        Content(STORIES_PREVIEW_LIST)
    }
}