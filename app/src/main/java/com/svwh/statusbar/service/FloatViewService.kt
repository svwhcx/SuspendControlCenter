package com.svwh.statusbar.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.svwh.statusbar.MainActivity
import com.svwh.statusbar.R
import com.svwh.statusbar.databinding.FloatingViewBinding
import com.svwh.statusbar.listener.FloatViewTouchListener
import com.svwh.statusbar.utils.RootChecker
import com.svwh.statusbar.viewmodel.FloatViewModel

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 11:41
 */
class FloatViewService : LifecycleService() {


    private lateinit var floatWindowManager: WindowManager

    private var floatRootView: View? = null

    private lateinit var binding: FloatingViewBinding

    private lateinit var layoutParams : WindowManager.LayoutParams

    companion object {
        private const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "FloatViewServiceChannel"

    }


    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                // 判断悬浮窗是否时显示状态
                val isShow = FloatViewModel.isShowSuspendWindow.value ?: false
                if (isShow){
                    // 这里还应该判断当前的这个是否存在，防止出现两个
                    showToast("重新加载!")
                    floatWindowManager.removeView(binding.root)
                    floatWindowManager.addView(binding.root, layoutParams)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Button Service")
            .setContentText("Floating button is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        startForeground(NOTIFICATION_ID,notification)
        initObserve()
        // 注册一个广播监听屏幕解锁
//        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
//        registerReceiver(screenUnlockReceiver, filter)
    }


    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Floating Button Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(layoutParams: WindowManager.LayoutParams) {
        // 点击下拉
        binding.openStatusBarBtn.setOnClickListener {
            RootChecker.executeRootCommand("service call statusbar 3")
        }
        binding.openStatusBarBtn.setOnTouchListener(FloatViewTouchListener(binding.root,layoutParams, floatWindowManager) {
            FloatViewModel.isShowSuspendWindow.postValue(false)
            stopSelf() })
        binding.root.setOnTouchListener(FloatViewTouchListener(binding.root,layoutParams, floatWindowManager){
            FloatViewModel.isShowSuspendWindow.postValue(false)
            stopSelf()})

        // 长按返回
        binding.openStatusBarBtn.setOnLongClickListener {
            // 处理长按事件
            FloatViewModel.isBack.postValue(true)
            true
        }
    }


    private fun initObserve() {
        FloatViewModel.apply {
            isVisible.observe(this@FloatViewService) {
                floatRootView?.visibility = if (it) View.VISIBLE else View.GONE
            }
            isShowSuspendWindow.observe(this@FloatViewService) {
                if (it) {
                    showWindow()
                }
            }
        }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun showWindow() {

        floatWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val outMetrics = DisplayMetrics()
        floatWindowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = 110
            height = 110
            gravity = Gravity.TOP or Gravity.END
            // 设置屏幕居中显示
            x = -outMetrics.widthPixels + width / 2
            y = outMetrics.heightPixels / 2 - height / 2
        }
        // 新建悬浮控件
        binding = FloatingViewBinding.inflate(LayoutInflater.from(this))
        this.layoutParams = layoutParams
        floatRootView = binding.root
        floatWindowManager.addView(binding.root, layoutParams)
        initView(layoutParams)

    }

    override fun onDestroy() {
        super.onDestroy()

        Toast.makeText(this, "Service destroyed, restarting...", Toast.LENGTH_SHORT).show()
        if (FloatViewModel.isShowSuspendWindow.value == true){
            val intent = Intent(this, FloatViewService::class.java)
            startForegroundService(intent)
            Toast.makeText(this, "Service destroyed, restarting...", Toast.LENGTH_SHORT).show()
        }


        if (this::binding.isInitialized) {
            floatWindowManager.removeView(binding.root)
        }
        showToast("关闭悬浮窗！")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // 重启服务
        Toast.makeText(this, "Service destroyed, restarting...", Toast.LENGTH_SHORT).show()
        if (FloatViewModel.isShowSuspendWindow.value == true){
            val intent = Intent(this, this::class.java)
            startForegroundService(intent)
        }

    }


    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    private fun showToast(m: String) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    }



}

