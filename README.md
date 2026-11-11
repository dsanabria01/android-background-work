# Android Background Work

Two small, focused Android apps that show how to run work **outside the UI lifecycle**: a foreground media service and periodic background jobs. Built with Kotlin and Jetpack Compose.

| Module | What it demonstrates | Key APIs |
|---|---|---|
| [`foreground-service-audio`](foreground-service-audio) | Audio playback that keeps running with the app in the background, with a media notification and controls | Media3 `MediaSessionService`, `ExoPlayer`, `MediaController`, foreground service type `mediaPlayback` |
| [`workmanager-notifications`](workmanager-notifications) | A periodic job (every 15 min) that fetches data from a REST API and posts a notification | `WorkManager` + `CoroutineWorker`, work constraints, Retrofit, Gson, runtime `POST_NOTIFICATIONS` permission |

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Media3 / ExoPlayer · WorkManager · Retrofit · OkHttp · Kotlin Coroutines · Gradle (Kotlin DSL, version catalog)

Requirements: Android Studio (recent stable), JDK 21, `minSdk 24`, `compileSdk 36`.

## How the code is organised

**foreground-service-audio**

- `AudioService` owns the `ExoPlayer` and the `MediaSession`; Media3 builds the notification and keeps the service in the foreground while audio plays.
- `MainActivity` connects to the service through a `MediaController` (created and released with `DisposableEffect`) and sends play/pause commands. The UI never holds the player itself.

**workmanager-notifications**

- `MainActivity` asks for the notification permission (Android 13+) and enqueues a unique periodic work request (`ExistingPeriodicWorkPolicy.KEEP`) that requires a network connection.
- `UpdateWorker` (`CoroutineWorker`) calls the API through `CrudApi` and shows a notification. If the API is unreachable or returns nothing, it returns `Result.retry()` so WorkManager applies its backoff.
- `CrudApi` wraps the Retrofit client; network errors and empty responses are handled and return `null` instead of crashing the worker.

## Running the demos

Both apps need a small backend. A mock server is included so you do not need anything else.

1. Start the mock server on your machine:

   ```bash
   python3 tools/mock_server.py
   ```

   It serves `GET /dites/` (JSON used by the WorkManager app) and static files from `tools/media/audio/`.

2. For the audio app, put any short MP3 and image in `tools/media/audio/` named `sample.mp3` and `sample.png`.

3. Open the module you want (`foreground-service-audio` or `workmanager-notifications`) in Android Studio and run it on an emulator. The emulator reaches your machine at `http://10.0.2.2:8080/`, which is the default.

To use a different server (for example a physical device on your LAN), add this to the module's `local.properties` (it is git-ignored):

```properties
# foreground-service-audio
media.baseUrl=http://192.168.1.50:8080/

# workmanager-notifications
api.baseUrl=http://192.168.1.50:8080/
```

Expected API response for `GET /dites/`:

```json
[{ "id": 1, "dita": "...", "explicacio": "..." }]
```

## Notes and known limitations

- Android enforces a **15-minute minimum** for periodic work, so the notification appears at most every 15 minutes, and the exact timing is up to the system.
- Cleartext HTTP is enabled in the manifests only so the demos can talk to a local server. A real app should use HTTPS.
- The package names still use the `com.example.*` prefix from the Android Studio template. These are learning projects, not published apps.
- There are no automated tests yet. Good next steps: unit tests for `CrudApi` with a mocked server (`MockWebServer`) and a `TestListenableWorkerBuilder` test for `UpdateWorker`.

## Author

David Sanabria — Android / software developer. [LinkedIn](https://linkedin.com/in/davidsanabriahierro) · [GitHub](https://github.com/dsanabria01)
