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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
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
import ru.m2.squaremeter.stories.video.presentation.model.ExoPlayerHolder
import ru.m2.squaremeter.storiescompose.ui.theme.StoriesComposeTheme

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
    StoriesContainer(
        data = data,
        onFinished = onFinished
    ) { stories, slide, progressBar, playerHolder ->
        val player = (playerHolder as? ExoPlayerHolder)?.player
        Column(modifier = Modifier.fillMaxSize()) {
            val loading = remember {
                mutableStateOf(
                    player?.playbackState == Player.STATE_BUFFERING || player?.playbackState == Player.STATE_IDLE
                )
            }
            IsVideoLoadingDisposableEffect(player = player, loading = loading)

            val mute = remember { mutableStateOf(player?.volume == 0f) }
            SilentModeDisposableEffect(
                player = player,
                mute = mute
            )
            DeviceVolumeDisposableEffect(
                player = player,
                mute = mute
            )

            val muteVisible = remember {
                mutableStateOf(
                    player?.currentTracks?.groups?.any { trackGroup ->
                        trackGroup.type == C.TRACK_TYPE_AUDIO
                    } ?: false
                )
            }
            CheckSoundDisposableEffect(
                player = player,
                muteVisible = muteVisible,
                stories = stories,
                slide = slide
            )


            val video = data.stories[stories]?.get(slide) is UiSlidesData.Video

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            ) {
                if (video) {
                    VideoContent(player, loading)
                } else {
                    ImageContent(stories, slide, progressBar)
                }
                if (video && muteVisible.value && player != null) {
                    Row(modifier = Modifier.align(Alignment.BottomStart)) {
                        MuteButton(
                            player = player,
                            mute = mute
                        )
                        Text(
                            text = "device volume = ${player.deviceVolume}, player volume = ${player.volume}",
                            modifier = Modifier.background(Color.Green).align(Alignment.CenterVertically)
                        )
                    }
                }
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
            put(
                it,
                buildList {
                    add(
                        if (it.contains("video")) {
                            UiSlidesData.Video(url = "https://cdn.m2.ru/assets/file-upload-server/88a0ed97d192b2131f80601d3f3aacd4.mp4")
                        } else {
                            UiSlidesData.Image(duration = 10_000L)
                        }
                    )
                }
            )
        }
    }
)

@Composable
private fun SilentModeDisposableEffect(
    player: ExoPlayer?,
    mute: MutableState<Boolean>
) {
    val context = LocalContext.current
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    DisposableEffect(player) {
        // Ловим смену звукового режима
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    setPlayerState(player, mute, audioManager.ringerMode)
                }
            }
        }

        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)

        // Проверка при инициализации
        setPlayerState(player, mute, audioManager.ringerMode)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

private fun setPlayerState(
    player: ExoPlayer?,
    mute: MutableState<Boolean>,
    mode: Int
) {
    if (player == null) return

    mute.value = mode == AudioManager.RINGER_MODE_SILENT
    player.volume = if (mute.value) 0f else 1f
}

@Composable
private fun DeviceVolumeDisposableEffect(
    player: ExoPlayer?,
    mute: MutableState<Boolean>
) {
    if (player == null) return

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            // Слушаем изменения аппаратной громкости устройства
            override fun onDeviceVolumeChanged(
                volume: Int,
                muted: Boolean
            ) {
                mute.value = muted || (volume == 0)
                player.volume = if (mute.value) 0f else 1f
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
}

@Composable
private fun CheckSoundDisposableEffect(
    player: ExoPlayer?,
    muteVisible: MutableState<Boolean>,
    stories: String,
    slide: Int
) {
    if (player == null) return

    DisposableEffect(player, stories, slide) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val hasAudio = tracks.groups.any { it.type == C.TRACK_TYPE_AUDIO }
                muteVisible.value = hasAudio
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
}

@Composable
private fun IsVideoLoadingDisposableEffect(
    player: ExoPlayer?,
    loading: MutableState<Boolean>
) {
    if (player == null) return

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                loading.value =
                    playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
}

@Composable
private fun MuteButton(
    player: ExoPlayer,
    mute: MutableState<Boolean>
) {
    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            mute.value = !mute.value
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
private fun VideoContent(player: ExoPlayer?, loading: MutableState<Boolean>) {
    if (player == null) return

    Box(modifier = Modifier.fillMaxSize()) {
        ContentFrame(
            player = player,
            modifier = Modifier.fillMaxSize(),
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            contentScale = ContentScale.Fit
        )
        if (loading.value) {
            Loader()
        }
    }
}

@Composable
private fun Loader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ImageContent(stories: String, slide: Int, progressBar: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoriesComposeTheme {
        Content(STORIES_PREVIEW_LIST)
    }
}