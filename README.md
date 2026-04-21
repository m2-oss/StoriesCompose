# Stories for Jetpack Compose

A library to make your stories easy to create with Compose. All the logic about stories transitions and shown indicating is implemented. All you need is only to create UI components.

The UI design is entirely up to you - there's no need to pass parameters to expose content details.

UPD: Since the stable version [1.3.9](https://github.com/m2-oss/StoriesCompose/releases/tag/1.3.9), there is a also an ability to add video stories!

<img width="360" height="780" alt="3" src="https://github.com/user-attachments/assets/ae0560fd-c457-4c2f-86f9-b9d786a38b51" />

## Setup

Add this code:
  - to your settings.gradle of the project:
``` kotlin
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
       google()
       mavenCentral()
       maven { url = uri("https://jitpack.io") }
   }
}
```
  - to your build.gradle of the module of usage:
``` kotlin
dependencies {
   implementation("com.github.m2-oss.StoriesCompose:stories:1.3.9") // base functionality (mandatory)
   implementation("com.github.m2-oss.StoriesCompose:stories-video:1.3.9") // video stories (optional)
}

```

## Quick Start

To create a list of previews:
  - prepare the data:
``` kotlin
val STORIES_PREVIEW_LIST = listOf(
    UiStoriesPreviewData(
        id = "id1",
        imageData = R.drawable.ic_launcher_background,
        title = "1",
    ),
    UiStoriesPreviewData(
        id = "video1",
        imageData = R.drawable.ic_launcher_foreground,
        title = "video1",
    )
)
```

  - create the list:
``` kotlin
@Composable
fun PreviewList(previews: List<UiStoriesPreviewData>, onClick: (String) -> Unit) {
    StoriesPreviewList(
        previews = previews,
        onClick = { onClick(it) }
    )
}
```

The result:

<img width="360" height="780" alt="Screenshot_20260420_174714" src="https://github.com/user-attachments/assets/9384250b-fb8d-4ce5-b25e-280f924bd6ea" />

To create container for stories:
  - prepare the data:
``` kotlin
@Composable
private fun createData(
    storiesId: String,
    previews: List<UiStoriesPreviewData>
): UiStoriesData = UiStoriesData(
    storiesId = storiesId,
    stories = buildMap {
        val ids = previews.map { it.id }
        ids.forEach {
            put(
                it,
                buildList {
                    add(
                        if (it.contains("video")) {
                            UiSlidesData.Video(url = "https://cdn.m2.ru/assets/file-upload-server/20fb0b6ebd500608a682c9794a40ae7e.mp4")
                        } else {
                            UiSlidesData.Image(duration = 10_000L)
                        }
                    )
                }
            )
        }
    }
)
```    

- create the containers:
``` kotlin
@Composable
fun Container(previews: List<UiStoriesPreviewData>, storiesId: String, onFinished: () -> Unit) {
    val data = createData(storiesId, previews)
    StoriesContainer(
        data = data,
        onFinished = onFinished
    ) { stories, slide, progressBar, playerHolder ->
        val player = (playerHolder as? ExoPlayerHolder)?.player
        Column(modifier = Modifier.fillMaxSize()) {
            val video = data.stories[stories]?.get(slide) is UiSlidesData.Video
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Gray)
            ) {
                if (video) {
                    VideoContent(player)
                } else {
                    ImageContent(stories, slide, progressBar)
                }
            }
        }
    }
}

@Composable
private fun VideoContent(player: ExoPlayer?) {
    if (player == null) return

    Box(modifier = Modifier.fillMaxSize()) {
        ContentFrame(
            player = player,
            modifier = Modifier.fillMaxSize(),
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ImageContent(stories: String, slide: Int, progressBar: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .offset(y = progressBar)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null
        )
        Text(
            text = "$stories, $slide",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
```

The result:

<img width="360" height="780" alt="4" src="https://github.com/user-attachments/assets/86b24c26-7897-487c-8b2c-e5c0cdc7b28f" />

## Variability

You can use either both or one of the components. In second case you need to synchronize stories shown indicator manually because they both use the same entry point to maintain the logic (the preview component observes which stories were shown in the container one). To do so you need to use [the repository contract](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/domain/repository/StoriesShownRepository.kt). Create the repo you can with [the factory](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/StoriesShownRepositoryFactory.kt). The samples of synchronization are [here](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/preview/presentation/viewmodel/PreviewViewModel.kt) and [here](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/container/presentation/viewmodel/StoriesViewModel.kt). Keep in mind that the interaction is asynchronous so that it must be on worker thread otherwise an exception will be thrown.
