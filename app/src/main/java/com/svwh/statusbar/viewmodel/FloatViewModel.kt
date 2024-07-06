package com.svwh.statusbar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 11:44
 */
object FloatViewModel : ViewModel() {

    // 悬浮窗口创建、移除，基于无障碍
    var isShowWindow = MutableLiveData<Boolean>()

    // 悬浮窗口显示
    var isShowSuspendWindow = MutableLiveData<Boolean>()

    // 悬浮窗口隐藏
    var isVisible = MutableLiveData<Boolean>()

    // 模拟返回键
    var isBack = MutableLiveData<Boolean>()

}