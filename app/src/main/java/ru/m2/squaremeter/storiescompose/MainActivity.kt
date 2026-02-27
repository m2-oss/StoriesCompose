package ru.m2.squaremeter.storiescompose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.m2.squaremeter.stories.container.presentation.model.UiSlidesData
import ru.m2.squaremeter.stories.container.presentation.model.UiStoriesData
import ru.m2.squaremeter.stories.container.presentation.ui.StoriesContainer
import ru.m2.squaremeter.stories.preview.presentation.model.UiStoriesPreviewData
import ru.m2.squaremeter.stories.preview.presentation.ui.StoriesPreviewList
import ru.m2.squaremeter.storiescompose.ui.theme.StoriesComposeTheme

private const val SLIDES_COUNT = 3
private const val SLIDE_DURATION = 10_000L
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
    val data = createData(storiesId, previews)
    val volumeChanged = remember { mutableStateOf(false) }
    StoriesContainer(
        data = data,
        onFinished = onFinished
    ) { stories, slide, progressBar, playerHolder ->
        val player = playerHolder.player
        Column(modifier = Modifier.fillMaxSize()) {
            val mute = remember { mutableStateOf(player.volume == 0f) }

            SilentModeDisposableEffect(
                player = player,
                mute = mute,
                volumeChanged = volumeChanged
            )

            DeviceVolumeDisposableEffect(
                player = player,
                mute = mute,
                volumeChanged = volumeChanged
            )

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
            SafeZone(player, mute, volumeChanged, video)
        }
    }
}

@Composable
private fun SafeZone(
    player: ExoPlayer,
    mute: MutableState<Boolean>,
    volumeChanged: MutableState<Boolean>,
    video: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.Blue)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (video) {
                MuteButton(
                    player = player,
                    mute = mute,
                    volumeChanged = volumeChanged
                )
            }
        }
    }
}

@Composable
private fun createData(
    storiesId: String,
    previews: List<UiStoriesPreviewData>
): UiStoriesData = UiStoriesData(
    storiesId = storiesId,
    stories = buildMap {
        val ids = previews.map { it.id }
        ids.forEach {
            when (it) {
                "video1" -> {
                    put(
                        it,
                        buildList {
                            addAll(
                                listOf(
                                    UiSlidesData.Video(url = "https://cdn.m2.ru/assets/file-upload-server/59d1bf8dd1ba8cee2d5df824ea01871d.mp4"),
                                )
                            )
                        }
                    )
                }

                else -> {
                    put(
                        it,
                        buildList {
                            repeat(SLIDES_COUNT) {
                                add(UiSlidesData.Image(duration = SLIDE_DURATION))
                            }
                        }
                    )
                }
            }
        }
    }
)

@Composable
private fun SilentModeDisposableEffect(
    player: ExoPlayer,
    mute: MutableState<Boolean>,
    volumeChanged: MutableState<Boolean>
) {
    val context = LocalContext.current
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    DisposableEffect(player) {
        // Ловим смену звукового режима
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    setPlayerState(player, mute, volumeChanged, audioManager.ringerMode)
                }
            }
        }

        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)

        // Проверка при инициализации
        setPlayerState(player, mute, volumeChanged, audioManager.ringerMode)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

private fun setPlayerState(
    player: ExoPlayer,
    mute: MutableState<Boolean>,
    volumeChanged: MutableState<Boolean>,
    mode: Int
) {
    mute.value = (player.deviceVolume == 0 && !volumeChanged.value) ||
            (mode == AudioManager.RINGER_MODE_SILENT && !volumeChanged.value)
    player.volume = if (mute.value) 0f else 1f
}

@Composable
private fun DeviceVolumeDisposableEffect(
    player: ExoPlayer,
    mute: MutableState<Boolean>,
    volumeChanged: MutableState<Boolean>
) {
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            // Слушаем изменения аппаратной громкости устройства
            override fun onDeviceVolumeChanged(
                volume: Int,
                muted: Boolean
            ) {
                volumeChanged.value = volume != 0
                mute.value = muted || (volume == 0)
                player.volume = if (mute.value) 0f else 1f
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
}

@Composable
private fun MuteButton(
    player: ExoPlayer,
    mute: MutableState<Boolean>,
    volumeChanged: MutableState<Boolean>
) {
    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            mute.value = !mute.value
            volumeChanged.value = !volumeChanged.value
            player.volume = if (mute.value) 0f else 1f
        }
    ) {
        Image(
            painter = painterResource(
                if (mute.value) {
                    R.drawable.ic_launcher_background
                } else {
                    R.drawable.ic_launcher_foreground
                }
            ),
            contentDescription = null
        )
    }
}

@Composable
private fun VideoContent(player: ExoPlayer) {
    Column(modifier = Modifier.fillMaxSize()) {
        ContentFrame(
            player = player,
            modifier = Modifier.fillMaxSize(),
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            contentScale = ContentScale.Crop,
            shutter = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AAAAA")
                }
            }
        )
    }
}

@Composable
private fun ImageContent(stories: String, slide: Int, progressBar: Dp) {
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
        Text(
            text = "$stories, $slide",
            modifier = Modifier
                .align(Alignment.Center)
                .clickable {}
                .drawBehind {
                    drawRoundRect(
                        color = Color.Yellow,
                        alpha = 0.2f,
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoriesComposeTheme {
        Content(STORIES_PREVIEW_LIST)
    }
}