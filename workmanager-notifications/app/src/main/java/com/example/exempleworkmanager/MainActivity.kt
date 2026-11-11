package com.example.exempleworkmanager

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.exempleworkmanager.ui.theme.ExempleWorkManagerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var permisNotificacions by remember { mutableStateOf(false) }
            ExempleWorkManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU) {
                        // Obligatori demanar permís de notificacions a partir de la versió 33
                        GestioPermisNotificacions(innerPadding) {
                            permisNotificacions = it
                        }
                    }else{
                        permisNotificacions = true
                    }
                        if (permisNotificacions) {
                            MainScreen(innerPadding)
                        }

                }
            }
        }
    }
}

@Composable
fun MainScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = {
            setupPeriodicWork(context)
        }) {
            Text("Activar notificacions automàtiques")
        }
    }
}

fun setupPeriodicWork(context: Context) {
    // Definim restriccions per l'execució en segon pla
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Cada 15 minuts s'executarà el mètode doWork de la class UpdateWorker
    val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(
        15, TimeUnit.MINUTES // Mínimo permitido por Android
    )
        .setConstraints(constraints)
        .build()

    // S'afegeix el workRequest al WorkManager
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "CheckUpdatesWork",
        ExistingPeriodicWorkPolicy.KEEP, // Manté la tasca si ja existeix
        workRequest
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun GestioPermisNotificacions(
    paddingValues: PaddingValues,
    onChange: (Boolean) -> Unit
) {
    var otorgat by remember { mutableStateOf(false) }

    val notificacionsPermissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    if (notificacionsPermissionState.status.isGranted) {
        otorgat = true
    } else {
        Column(modifier = Modifier.padding(paddingValues)) {
            val textToShow =
                "El permís de notificacions actual és important per aquesta aplicació. Si us plau concediu el permís."

            Text(textToShow)
            Button(onClick = { notificacionsPermissionState.launchPermissionRequest() }) {
                Text("Sol·licitar permís de notificacions")
            }
        }
    }
    onChange(otorgat)
}
