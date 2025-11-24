# Fault Detector Android App

An Android 14-ready fault monitoring app for utility poles, built with Jetpack Compose, MVVM, Koin, and Retrofit. The client polls a FastAPI backend for node status, surfaces real-time dashboards, and runs a hardened foreground service that alerts the user whenever new faults are detected.

## Key Features

- **Role-aware login** (Citizen/Staff) with Compose UI
- **Citizen dashboard**: recent faults, stats, and quick actions
- **Staff dashboard**: operational view with node-level insights
- **Fault reporting**: map picker, photo upload, and rich descriptions
- **Foreground monitoring service**: resilient permissions handling for Android 14+ `FOREGROUND_SERVICE_DATA_SYNC`
- **Local alerting**: Notification channels + runtime `POST_NOTIFICATIONS` requests (no Firebase dependency)

## Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Navigation Compose
- **State**: ViewModels + Kotlin Flows
- **DI**: Koin
- **Networking**: Retrofit + Kotlin Serialization/Moshi (depending on build flavor)
- **Concurrency**: Coroutines
- **Build/Tools**: Gradle Kotlin DSL, lint, Crashlytics (analytics only)

## Getting Started

1. **Prerequisites**

   - Android Studio Ladybug (AGP 8.2+) or newer
   - Android SDK 24+
   - A FastAPI backend reachable on the same network/emulator bridge

2. **Backend URL**
   Update `ApiClient.kt` (or equivalent data source) with your FastAPI base URL:

   ```kotlin
   private const val BASE_URL = "http://192.168.0.100:8000/"
   ```

3. **Google Maps**

   - Enable Maps SDK, Places API, and Geocoding API in Google Cloud Console
   - Add your key to `app/src/main/AndroidManifest.xml` under `com.google.android.geo.API_KEY`

4. **Crashlytics (Optional)**

   - Place your `google-services.json` inside `app/`
   - The project only uses Crashlytics for analytics; Firebase Cloud Messaging has been removed

5. **Build & Run**
   ```bash
   ./gradlew clean assembleDebug
   ```
   Install the generated APK on an Android 13/14 device (or emulator) and grant requested permissions on first launch.

## Runtime Permissions & Foreground Services

The app proactively requests:

- `FOREGROUND_SERVICE_DATA_SYNC` (manifest + runtime enforcement)
- `POST_NOTIFICATIONS` (Android 13+ runtime request)
- Location, Camera, and Storage for map interactions and fault evidence

`MainActivity` contains the permission launcher, while `ComKSEBFaultApplication` double-checks grants before starting `FaultNotificationService`. Lint checks ensure every `NotificationManagerCompat` call handles potential `SecurityException`s.

## Project Structure

```
app/src/main/java/com/ksebl/comkseblfaultapp/
├── data/
│   ├── api/            # Retrofit client & DTOs
│   ├── model/          # Domain models
│   └── repository/     # Repository implementations
├── navigation/         # App nav graph & destinations
├── service/            # Foreground service + notifications
├── ui/
│   ├── components/    # Reusable Compose components
│   ├── screen/        # Screen-level composables
│   └── viewmodel/     # ViewModels + state holders
└── utils/             # Helpers, constants, extensions
```

## Testing & QA

- `./gradlew testDebugUnitTest` – local JVM tests
- `./gradlew lintDebug` – static analysis (passes after notification permission fixes)
- Manual checks on Android 14 emulator/device to confirm runtime permission dialogs and stable foreground services

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-change`)
3. Make and test your updates
4. Submit a pull request describing the change and testing notes

## License

This project is distributed under the MIT License. See `LICENSE` for details.
