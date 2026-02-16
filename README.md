# Stories for Jetpack Compose

A library to make your stories easy to create with Compose. All the logic about stories transitions and shown indicating is implemented. All you need is only to create UI components.

![m2 converted](https://github.com/user-attachments/assets/f3588e80-bd03-4856-935f-983929f3c7df)


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
  implementation("com.github.m2-oss:StoriesCompose:1.3.2")
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
       id = "id2",
       imageData = R.drawable.ic_launcher_background,
       title = "2",
   ),
   UiStoriesPreviewData(
       id = "id3",
       imageData = R.drawable.ic_launcher_background,
       title = "3",
   ),
   UiStoriesPreviewData(
       id = "id4",
       imageData = R.drawable.ic_launcher_background,
       title = "4",
   )
)
```

  - create the list:
``` kotlin
StoriesPreviewList(
   previews = STORIES_PREVIEW_LIST,
   onClick = {
       // your callback handle
   }
)
```

The result:

<img src="https://github.com/user-attachments/assets/9ee54a7b-7462-4198-b04b-fb7a6c214ac9" width="360" height="780" />

To create container for stories:
  - prepare the data:
``` kotlin
private const val SLIDES_COUNT = 3
private const val STORIES_DURATION_SEC = 10
private val SLIDES_COLORS = listOf(
   Color.LightGray,
   Color.Gray,
   Color.DarkGray
)
```    

- create the container:
``` kotlin
StoriesContainer(
   data = UiStoriesData(
       storiesId = storiesId, // an id of story clicked before
       stories = buildMap {
           val ids = STORIES_PREVIEW_LIST.map { it.id }
           ids.forEach {
               put(it, SLIDES_COUNT)
           }
       },
       durationInSec = STORIES_DURATION_SEC
   ),
   onFinished = {
     // your callback handle
   }
) { stories, slide, progressBar ->
   Box(
       modifier = Modifier
           .fillMaxSize()
           .background(SLIDES_COLORS[slide])
   ) {
       Text(text = "$stories, $slide", modifier = Modifier.align(Alignment.Center))
   }
}
```
The result:

<img src="https://github.com/user-attachments/assets/cb6115dc-f3b3-4939-9999-20fcdae59d69" width="360" height="720">

## Variability

You can use either both or one of the components. In second case you need to synchronize stories shown indicator manually because they both use the same entry point to maintain the logic (the preview component observes which stories were shown in the container one). To do so you need to use [the repository contract](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/domain/repository/StoriesShownRepository.kt). Create the repo you can with [the factory](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/StoriesShownRepositoryFactory.kt). The samples of synchronization are [here](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/preview/presentation/viewmodel/PreviewViewModel.kt) and [here](https://github.com/m2-oss/StoriesCompose/blob/master/stories/src/main/java/ru/m2/squaremeter/stories/container/presentation/viewmodel/StoriesViewModel.kt). Keep in mind that the interaction is asynchronous so that it must be on worker thread otherwise an exception will be thrown.
