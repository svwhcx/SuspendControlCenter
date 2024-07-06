package com.svwh.statusbar.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.svwh.statusbar.viewmodel.FloatViewModel

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 20:58
 */
class FloatViewRestartBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "com.svwh.statusbar.RESTART_SERVICE") {
            if (FloatViewModel.isShowSuspendWindow.value == true){
                val serviceIntent = Intent(context, FloatViewService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}