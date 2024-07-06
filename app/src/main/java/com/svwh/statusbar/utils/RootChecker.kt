package com.svwh.statusbar.utils

/**
 * @description
 * @Author chenxin
 * @Date 2024/7/5 9:32
 */
import android.content.Context
import android.content.Intent
import com.svwh.statusbar.service.FloatViewService
import java.io.*

object RootChecker {

    // 检查常见的Root管理应用
    private val rootManagementApps = arrayOf(
        "com.noshufou.android.su",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su"
    )

    // 检查常见的Root二进制文件
    private val rootBinaries = arrayOf(
        "su",
        "busybox"
    )

    // 检查路径
    private val rootPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    /**
     * 检查设备是否已经Root
     */
    fun isDeviceRooted(context: Context): Boolean {
        return isAnyRootManagementAppInstalled(context) || isAnyRootBinaryPresent() || isAnyRootPathPresent() || canExecuteRootCommand()
    }

    // 检查常见的Root管理应用是否已安装
    private fun isAnyRootManagementAppInstalled(context: Context): Boolean {
        for (packageName in rootManagementApps) {
            try {
                val pm = context.packageManager
                pm.getPackageInfo(packageName, 0)
                return true
            } catch (e: Exception) {
                // 忽略异常
            }
        }
        return false
    }

    // 检查常见的Root二进制文件是否存在
    private fun isAnyRootBinaryPresent(): Boolean {
        for (binary in rootBinaries) {
            val paths = arrayOf(
                "/system/bin/$binary",
                "/system/xbin/$binary",
                "/sbin/$binary",
                "/system/sd/xbin/$binary",
                "/data/local/xbin/$binary",
                "/data/local/bin/$binary",
                "/system/bin/failsafe/$binary",
                "/data/local/$binary"
            )
            for (path in paths) {
                if (File(path).exists()) {
                    return true
                }
            }
        }
        return false
    }

    // 检查常见的Root路径
    private fun isAnyRootPathPresent(): Boolean {
        for (path in rootPaths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    // 尝试执行Root命令
    private fun canExecuteRootCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = bufferedReader.readLine()
            output != null
        } catch (e: Exception) {
            false
        }
    }

    // 尝试获取Root权限
    fun attemptRootAccess(): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun executeRootCommand(command: String) {

            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
//                val result = readProcessOutput(process)
                process.waitFor()
                os.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    fun startServiceWithRoot(context: Context){
        val serviceIntent = Intent(context, FloatViewService::class.java)
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val command = "am startservice --user 0 ${context.packageName}/${serviceIntent.component!!.className}"
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
