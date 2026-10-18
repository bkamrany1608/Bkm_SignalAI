package com.freechart.signalai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

object ScanState {
    val running = MutableStateFlow(false)
    val scanning = MutableStateFlow(false)
    val lastScan = MutableStateFlow(0L)
}

class ScannerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var wl: PowerManager.WakeLock? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH, "Scanner", NotificationManager.IMPORTANCE_LOW))
        val n = notif("سرویس فعال است")
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1, n)
        if (wl == null) {
            wl = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "signalai:svc").apply { acquire() }
        }
        ScanState.running.value = true
        if (job == null) job = scope.launch {
            while (isActive) { scanOnce(); delay(SCAN_INTERVAL_MS) }
        }
        return START_STICKY
    }

    /** Phase 2 replaces this with the real market scan (crypto, forex, gold). */
    private suspend fun scanOnce() {
        ScanState.scanning.value = true
        delay(4000)
        ScanState.lastScan.value = System.currentTimeMillis()
        ScanState.scanning.value = false
    }

    private fun notif(text: String) = NotificationCompat.Builder(this, CH)
        .setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle("سیگنال‌یار AI")
        .setContentText(text).setOngoing(true).build()

    override fun onDestroy() {
        job?.cancel(); scope.cancel(); wl?.release()
        ScanState.running.value = false; ScanState.scanning.value = false
        super.onDestroy()
    }

    companion object {
        const val CH = "scanner"
        const val SCAN_INTERVAL_MS = 15 * 60_000L
        fun start(c: Context) = c.startForegroundService(Intent(c, ScannerService::class.java))
        fun stop(c: Context) = c.stopService(Intent(c, ScannerService::class.java))
    }
}
