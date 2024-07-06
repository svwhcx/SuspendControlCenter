package com.svwh.statusbar

/**
 * 让悬浮窗在应用关闭后依然可以运行
 * @description
 * @Author chenxin
 * @Date 2024/7/5 11:21
 */

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.svwh.statusbar.databinding.ActivityMainBinding
import com.svwh.statusbar.service.FloatViewService
import com.svwh.statusbar.utils.*
import com.svwh.statusbar.viewmodel.FloatViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    companion object {
        private const val SYSTEM_ALERT_WINDOW_PERMISSION = 2084

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化绑定
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        initObserve()
    }


    private fun initObserve(){
        FloatViewModel.isBack.observe(this){
            if (it){
                // 模拟返回键
                onBackPressedDispatcher.onBackPressed()
                // 重置这个状态
                FloatViewModel.isBack.postValue(false)
            }
        }
    }


    private fun initView(){
        binding.enableStatusBarBtn.setOnClickListener {
            // 申请Root权限
            if (RootChecker.isDeviceRooted(this)){
                RootChecker.attemptRootAccess()
            }else{
                Toast.makeText(this, "设备未root!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.enableStatusBarBt.setOnClickListener {
            // 显示悬浮窗
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
            } else {
                startFloatingViewActivity()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingViewActivity()
            } else {
                Toast.makeText(this, "Permission denied to draw over other apps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startFloatingViewActivity() {
        val intent = Intent(this, FloatViewService::class.java)
        startForegroundService(intent)
//        RootChecker.startServiceWithRoot(this)
        FloatViewModel.isShowSuspendWindow.postValue(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this,"关闭",Toast.LENGTH_SHORT).show()
    }
}