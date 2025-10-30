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

To create preview list you need to add the code:
``` kotlin
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
```
The result:
<img width="1080" height="2340" alt="Screenshot_20251030_145303" src="https://github.com/user-attachments/assets/838e0b9d-628a-4dd8-a64a-ae7e6d3f8663" />


To create container for stories you need to add the code:
``` kotlin
StoriesContainer(
    storiesId = "id",
    stories = mapOf("id" to 3),
    durationInSec = 10,
    onFinished = { clicked = false }
) { stories, slide, progressBar ->
    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
}
```
The result:
<img width="1080" height="2340" alt="Screenshot_20251030_145425" src="https://github.com/user-attachments/assets/37d37fcd-d6db-4d4e-b42d-aee227929330" />
