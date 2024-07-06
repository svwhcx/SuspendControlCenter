package com.svwh.statusbar.listener

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 19:23
 */
class FloatViewTouchListener(
    private val viewx: View,
    private val wl: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    closeSuspendView: () -> Unit
) : View.OnTouchListener {

    private var x = 0

    private var y = 0

    private var isMoving = false

    private var screenWidth: Int? = null


    private var closeSuspendView = closeSuspendView

    private var downTime : Long  = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        Log.i("FloatViewTouchListener", "onTouch: ")
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                // 按下的时候记录x、y
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                this.downTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                // 移动的时候计算x、y的位置
                val newX = motionEvent.rawX.toInt()
                val newY = motionEvent.rawY.toInt()
                val movedX = newX - x
                val movedY = newY - y
                x = newX
                y = newY
                if (abs(movedX) > 5 || abs(movedY) > 5){
                    // 判断是移动还是点击
                    isMoving = true
                }
                wl.apply {
                    x -= movedX
                    y += movedY
                }
                // 更新浮窗的位置
                windowManager.updateViewLayout(viewx, wl)
                // 在移动到删除区域时显示删除
            }
            MotionEvent.ACTION_UP -> {
                if (!isMoving){
                    val upTime = System.currentTimeMillis()
                    if (upTime - downTime < 50){
                        view.performClick()
                    }else{
                        view.performLongClick()
                    }
                    return false
                }
                // 手指抬起后，判断当前的位置，配置动画和轨迹到侧边
                // 获取屏幕宽度
                if (screenWidth == null){
                    screenWidth = windowManager.currentWindowMetrics.bounds.width()
                }
                val screenWidth = this.screenWidth!!
                // 计算当前的位置是否在x的中间，如果是的话则关闭这个悬浮窗口。
                if (abs(wl.x + viewx.width / 2 - screenWidth / 2) < 30) {
                    // 如果在屏幕中间则关闭悬浮窗
                    this.closeSuspendView()
                    return false
                }
                val targetX = if (wl.x + viewx.width / 2 < screenWidth / 2) 0 else screenWidth - viewx.width
                animateToEdge(targetX)
                clearStatus()
            }
            else -> return false
        }
        return true
    }

    // 悬浮窗贴边移动动画
    private fun animateToEdge(targetX: Int) {
        val animator = ObjectAnimator.ofInt(wl.x, targetX)
        animator.addUpdateListener { animation ->
            wl.x = animation.animatedValue as Int
            windowManager.updateViewLayout(viewx, wl)
        }
        animator.duration = 100
        animator.start()
    }

    private fun clearStatus(){
        isMoving = false
    }

}
