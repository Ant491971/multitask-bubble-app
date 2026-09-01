package com.bubbletask.multitaskbubble.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.bubbletask.multitaskbubble.R
import com.bubbletask.multitaskbubble.util.RecentAppsCounter
import kotlin.math.abs

class BubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var bubbleText: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var recentAppsCounter: RecentAppsCounter

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateBubbleCount()
            handler.postDelayed(this, 30000) // Update every 30 seconds
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        recentAppsCounter = RecentAppsCounter(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        setupFloatingView()
        handler.post(updateRunnable)
    }

    private fun setupFloatingView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.layout_bubble, null)
        bubbleText = floatingView?.findViewById(R.id.bubble_count_text)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager?.addView(floatingView, params)
        updateBubbleCount()
    }

    private fun updateBubbleCount() {
        val count = recentAppsCounter.getRecentAppsCount()
        bubbleText?.text = count.toString()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Bubble Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Multitask Bubble Active")
            .setContentText("Counting recent apps...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        floatingView?.let { windowManager?.removeView(it) }
    }

    companion object {
        private const val CHANNEL_ID = "bubble_service_channel"
        private const val NOTIFICATION_ID = 1
    }
}