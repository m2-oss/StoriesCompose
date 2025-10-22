package ru.m2.squaremeter.storiescompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ru.m2.squaremeter.stories.presentation.model.UiStoriesPreview
import ru.m2.squaremeter.stories.presentation.ui.StoriesContainer
import ru.m2.squaremeter.stories.presentation.ui.StoriesPreviewList
import ru.m2.squaremeter.storiescompose.ui.theme.StoriesComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoriesComposeTheme {
                StoriesComposeContent()
            }
        }
    }
}

@Composable
fun StoriesComposeContent() {
    var clicked by remember { mutableStateOf(false) }
    if (clicked) {
        StoriesContainer(
            storiesId = "id",
            stories = mapOf("id" to 3),
            durationInSec = 10,
            onFinished = { clicked = false }
        ) { stories, slide, progressBar ->
            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
        }
    } else {
        StoriesPreviewList(
            stories = listOf(
                UiStoriesPreview(
                    id = "id",
                    imageData = R.drawable.ic_launcher_background,
                    title = "title",
                    shown = false
                )
            ),
            onClick = { clicked = true }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoriesComposeTheme {
        StoriesComposeContent()
    }
}