# Stories for Jetpack Compose

A library to make your stories easy to create with Compose.

![42769f58e5732f5fbe2f700122427e9d](https://github.com/user-attachments/assets/ad9dfff9-93ce-4b91-87dc-1ed4f09e40ef)

## Setup

``` kotlin
dependencies {
  implementation("com.github.m2-oss:StoriesCompose:1.0.0")
}
```

## How To Use

To create a list ot preview data you need to add the code:
``` kotlin
val STORIES_PREVIEW_LIST = listOf(
    UiStoriesPreview(
        id = "1",
        imageData = R.drawable.ic_launcher_background,
        title = "1",
        shown = false
    ),
    UiStoriesPreview(
        id = "2",
        imageData = R.drawable.ic_launcher_background,
        title = "2",
        shown = false
    ),
    UiStoriesPreview(
        id = "3",
        imageData = R.drawable.ic_launcher_background,
        title = "3",
        shown = false
    )
)
```


To create preview list you need to add the code:
``` kotlin
@Composable
fun PreviewList(previews: List<UiStoriesPreview>, onClick: (String) -> Unit) {
    StoriesPreviewList(
        stories = previews,
        onClick = { onClick(it) }
    )
}
```
The result:
<img width="360" height="780" alt="image" src="https://github.com/user-attachments/assets/1c0c6f33-0460-4492-b659-e2b307f7fb7d" />


To create container for stories you need to add the code:
``` kotlin
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
```
The result:
<img width="360" height="780" alt="image" src="https://github.com/user-attachments/assets/91da11d6-ee7c-4bbf-ba60-e7d1bf906595" />