package com.svwh.statusbar

import android.graphics.PixelFormat
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.svwh.statusbar.databinding.FloatingViewBinding

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 10:15
 */
class FloatingViewActivity: AppCompatActivity() {

    private lateinit var binding: FloatingViewBinding

    private lateinit var floatWindowManager : WindowManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "打开悬浮窗", Toast.LENGTH_SHORT).show()
        binding = FloatingViewBinding.inflate(layoutInflater)
        val view = LayoutInflater.from(this).inflate(R.layout.floating_view, null)
        floatWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        // Set the position of the floating button
        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.x = 0
        layoutParams.y = 100
        windowManager.addView(view, layoutParams)
        binding.openStatusBarBtn.setOnClickListener {
            Toast.makeText(this, "打开状态栏", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::floatWindowManager.isInitialized){
            windowManager.removeView(binding.root)
        }
    }
}