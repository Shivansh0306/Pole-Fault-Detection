package com.ksebl.comkseblfaultapp.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ksebl.comkseblfaultapp.MainActivity
import com.ksebl.comkseblfaultapp.R
import com.ksebl.comkseblfaultapp.model.Fault
import com.ksebl.comkseblfaultapp.repository.MainRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class FaultNotificationService : Service() {
    private var repository: MainRepository? = null
    private var serviceJob: Job? = null
    private var lastKnownFaultCount = 0
    private var lastCheckedFaults = emptySet<String>()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "fault_monitoring"
        private const val POLL_INTERVAL_MS = 30000L // 30 seconds
        
        fun start(context: Context) {
            try {
                val intent = Intent(context, FaultNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: SecurityException) {
                android.util.Log.e("FaultNotificationService", "SecurityException starting service", e)
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FaultNotificationService", "Failed to start service", e)
                throw e
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, FaultNotificationService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize repository safely
        try {
            repository = org.koin.core.context.GlobalContext.get().get<MainRepository>()
        } catch (e: Exception) {
            // If Koin fails, we'll handle it gracefully
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startFaultMonitoring()
            return START_STICKY
        } catch (e: SecurityException) {
            android.util.Log.e("FaultNotificationService", "SecurityException in onStartCommand", e)
            // Stop the service if we can't run properly
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            android.util.Log.e("FaultNotificationService", "Exception in onStartCommand", e)
            return START_STICKY // Try to restart
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
    }

    private fun startFaultMonitoring() {
        try {
            // Start foreground service to keep monitoring in background
            val notification = createForegroundNotification()
            startForeground(NOTIFICATION_ID, notification)
            
            android.util.Log.d("FaultNotificationService", "Successfully started foreground service")

            serviceJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                while (isActive) {
                    try {
                        checkForNewFaults()
                    } catch (e: Exception) {
                        // Log error but continue monitoring
                        android.util.Log.e("FaultNotificationService", "Error checking for faults", e)
                    }
                    delay(POLL_INTERVAL_MS)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("FaultNotificationService", "SecurityException starting foreground", e)
            throw e
        } catch (e: Exception) {
            android.util.Log.e("FaultNotificationService", "Exception starting foreground", e)
            throw e
        }
    }

    private suspend fun checkForNewFaults() {
        try {
            val repo = repository ?: return // Exit gracefully if repository is null
            val currentFaults = repo.getFaults()
            val currentFaultIds = currentFaults.map { it.id }.toSet()
            
            // Find new faults that weren't in our last check
            val newFaults = currentFaults.filter { fault ->
                fault.id !in lastCheckedFaults && fault.status == "ACTIVE"
            }
            
            // Show notification for each new fault
            newFaults.forEach { fault ->
                showFaultNotification(fault)
            }
            
            // Update our tracking
            lastCheckedFaults = currentFaultIds
            
        } catch (e: Exception) {
            // Handle network errors gracefully
            e.printStackTrace()
        }
    }

    private fun showFaultNotification(fault: Fault) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_faults_tab", true)
            putExtra("fault_id", fault.id)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            fault.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚡ New Fault Detected!")
            .setContentText("${fault.faultType}: ${fault.description}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Fault ID: ${fault.id}\nType: ${fault.faultType}\nDescription: ${fault.description}\nReported: ${fault.reportedAt}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(resources.getColor(android.R.color.holo_red_dark, null))
            .build()

        // Check notification permission before showing notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(
                    fault.id.hashCode() + 2000, // Unique ID for each fault
                    notification
                )
            }
        } else {
            // For older versions, notifications don't require runtime permission
            NotificationManagerCompat.from(this).notify(
                fault.id.hashCode() + 2000, // Unique ID for each fault
                notification
            )
        }
    }

    private fun createForegroundNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Fault Monitoring Active")
        .setContentText("Monitoring for new electrical faults...")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fault Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new electrical faults"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}