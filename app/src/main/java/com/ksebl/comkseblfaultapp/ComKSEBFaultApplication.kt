package com.ksebl.comkseblfaultapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ksebl.comkseblfaultapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class ComKSEBFaultApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging first
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize Firebase with error handling (for crashlytics only, no FCM)
        try {
            FirebaseApp.initializeApp(this)
            if (BuildConfig.DEBUG) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
            } else {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            }
            Timber.d("Firebase initialization successful")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase")
            // Continue without Firebase if needed
        }
        
        // Initialize Koin for dependency injection with error handling
        try {
            startKoin {
                androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
                androidContext(this@ComKSEBFaultApplication)
                modules(appModule)
            }
            Timber.d("Koin initialization successful")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Koin")
            // Try to continue without crashing the app completely
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (firebaseException: Exception) {
                // Firebase not available, just log
                Timber.e(firebaseException, "Firebase also failed to log exception")
            }
        }
        
        // Create notification channel for O and above
        createNotificationChannel()
        
        // Start background notification service with proper permission check and delay
        startServiceWithPermissionCheck()
    }
    
    private fun startServiceWithPermissionCheck() {
        try {
            // Check if we have the required permission for Android 14+
            val hasDataSyncPermission = if (Build.VERSION.SDK_INT >= 34) {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not needed on older versions
            }
            
            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not needed on older versions
            }
            
            Timber.d("Permission check - DataSync: $hasDataSyncPermission, Notifications: $hasNotificationPermission")
            
            if (hasDataSyncPermission && hasNotificationPermission) {
                // Start service with delay to ensure full app initialization
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        com.ksebl.comkseblfaultapp.service.FaultNotificationService.start(this@ComKSEBFaultApplication)
                        Timber.d("FaultNotificationService started successfully")
                    } catch (e: SecurityException) {
                        Timber.e(e, "SecurityException starting service despite permission check")
                        // Continue without the service
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to start FaultNotificationService after delay")
                    }
                }, 3000) // Wait 3 seconds for full initialization
            } else {
                Timber.w("Missing required permissions - service not started")
                // We'll start the service later when permissions are granted
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed during service startup permission check")
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "comksebl_fault_app_channel"
    }
}
