package com.example.exempleforegroundservice

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.exempleforegroundservice.ui.theme.ExempleForegroundServiceTheme

// Files expected under BuildConfig.MEDIA_BASE_URL (see README).
private const val AUDIO_PATH = "audio/sample.mp3"
private const val ARTWORK_PATH = "audio/sample.png"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExempleForegroundServiceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioPlayerScreen(innerPadding)
                }
            }
        }
    }
}

@Composable
fun AudioPlayerScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }

    // Conectem al servei
    DisposableEffect(Unit) {
        val sessionToken = SessionToken(context,
            ComponentName(context, AudioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            controller = controllerFuture.get()
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            val metadata = MediaMetadata.Builder()
                .setTitle("Sample track")
                .setArtist("Demo")
                .setArtworkUri(Uri.parse(BuildConfig.MEDIA_BASE_URL + ARTWORK_PATH))
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId("audio_1")
                .setUri(BuildConfig.MEDIA_BASE_URL + AUDIO_PATH)
                .setMediaMetadata(metadata)
                .build()

            controller?.setMediaItem(mediaItem)
            controller?.prepare()
            controller?.play()
        }) {
            Text("Play audio")
        }

        Button(onClick = { controller?.pause() }) {
            Text("Pause")
        }
    }
}