package com.nuonuo.desktopcornermark

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.reflect.Field
import java.util.*

/**
 * 桌面角标
 */
class DesktopCornerMark(private var context: Context, notice: Function<Int>? = null) {
    private val TAG = "DesktopCornerMark"

    /**
     * 限定最大显示99条
     */
    private val markMaxCount = 99

    /**
     * 小米发通知用的id
     */
    private val xiaoMi = "xiaomi"

    /**
     * 小米通知渠道
     */
    private val xiaoMiChannelName = "xiaomi_channel"

    /**
     * 是否在前台 默认在前台
     */
    private var isInFrontDesk = true

    /**
     * 默认缓存的角标数量
     */
    private var imMarkCount = 0

    /**
     * 通知数量
     */
    private val noticeMarkCount = 0


    private var xiaomiNotificationId = 999222

    private var xiaomiNotification: Notification? = null


    /**
     * @param numberIn 显示角标数量 0 则隐藏
     * 显示华为角标
     */
    private fun showHuaWeiDeskMark(numberIn: Int) {
        var number = numberIn
        try {
            number = detectMarkNumber(number)
            val extra = Bundle()
            extra.putString("package", context.packageName)
            extra.putString("class", launcherClassName)
            extra.putInt("badgenumber", number)
            context.contentResolver.call(
                Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                "change_badge",
                null,
                extra
            )
        } catch (ignored: Exception) {
            ignored.printStackTrace()
            Log.e(TAG, "showHuaWeiDeskMark is Error$ignored")
        }
    }

    /**
     * @param markNumberIn 显示数量
     * 显示小米角标
     * 小米官网 角标代码
     * https://dev.mi.com/console/doc/detail?pId=2321
     * https://dev.mi.com/console/doc/detail?pId=939
     * 显示小米角标数量
     */

    private fun showXiaoMiDeskMark(markNumberIn: Int) {
        var markNumber = markNumberIn
        try {
            markNumber = detectMarkNumber(markNumber)
            //因为小米更新数量需要发送通知 当是0的 时候就没必要进行发送了
            if (markNumber == 0) {
                return
            }
            // 通知8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    xiaoMi,
                    xiaoMiChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    //小米角标代码
                    if (xiaomiNotification != null) {
                        //小米角标代码
                        val field: Field =
                            xiaomiNotification!!.javaClass.getDeclaredField("extraNotification")
                        val extraNotification = field[xiaomiNotification]
                        val method = extraNotification.javaClass.getDeclaredMethod(
                            "setMessageCount",
                            Int::class.javaPrimitiveType
                        )
                        method.invoke(extraNotification, markNumber)
                        notificationManager.notify(xiaomiNotificationId, xiaomiNotification)
                    }
                } else {
                    if (xiaomiNotification != null) {
                        notificationManager.notify(xiaomiNotificationId, xiaomiNotification)
                    }

                }

            }


        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showXiaoMiDeskMark is Error")
        }


    }


    /**
     * @return 通知是否打开
     */
    private fun notificationIsOpen(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * @return 是否支持角标能力
     * 目前调研只有 华为 小米能够支持 Xiaomi  HUAWEI
     * 三星只进行了s6测试 需要测试进行跟进
     */
    private val isSupportDeskMark: Boolean
        get() = isHuaWei || isXiaoMi || isSamsung

    /**
     * @return 获取启动页name
     */
    private val launcherClassName: String
        get() {
            val launchComponent = launcherComponentName
            return launchComponent?.className ?: getOpenClassName()
        }
    private val launcherComponentName: ComponentName?
        get() {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(
                context.packageName
            )
            return launchIntent?.component
        }

    /**
     * @param markNumber 要显示的角标数量
     * @return 最大99 最小0
     */
    private fun detectMarkNumber(markNumber: Int): Int {

        if (markNumber < 0) {
            return 0
        } else if (markNumber > markMaxCount) {
            return markMaxCount
        }
        return markNumber
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isInFrontDesk = false
                showDeskMark()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                isInFrontDesk = true
                if (isXiaoMi) {
                    cancelNotice()
                }
            }
        })
        //添加通知
        registerNotice(context)
    }

    /**
     * @return 是否是小米
     */
    private val isXiaoMi: Boolean
        get() = "xiaomi" == phoneMode.lowercase(Locale.getDefault())

    /**
     * @return 是否是华为
     */
    private val isHuaWei: Boolean
        get() = "huawei" == phoneMode.lowercase(Locale.getDefault())

    /**
     * @return 是否是三星
     */
    private val isSamsung: Boolean
        get() = "samsung" == phoneMode.lowercase(Locale.getDefault())

    fun showDeskMark(markNumber: Int) {
        //不是华为 不是小米 直接返回
        if (!isSupportDeskMark || !notificationIsOpen()) {
            return
        }
        imMarkCount = markNumber
        //在前台就不更改角标 直到在后台为止
        if (isInFrontDesk) {
            return
        } else {
            showDeskMark()
        }
    }

    /**
     * 显示角标数量
     */
    private fun showDeskMark() {
        var markNumber = imMarkCount + noticeMarkCount
        if (markNumber < 0) {
            markNumber = 0
        }
        val finalMarkNumber = markNumber
        //延迟600毫秒执行
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                //华为
                if (isHuaWei) {
                    showHuaWeiDeskMark(finalMarkNumber)
                } else if (isXiaoMi) {
                    //小米  小米在通知的时候 会自己加1减1 只处理im消息通知的数量
                    showXiaoMiDeskMark(imMarkCount)
                } else if (isSamsung) {
                    //三星
                    showSamsungDeskMark(finalMarkNumber)
                }
            }
        }
        val timer = Timer()
        timer.schedule(timerTask, 600)
    }

    /**
     * @return 获取通知管理者
     */
    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * @param markCount 角标值
     * 显示三星角标
     */
    private fun showSamsungDeskMark(markCount: Int) {
        try {
            val launcherClassName = launcherClassName
            val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
            intent.putExtra("badge_count", markCount)
            intent.putExtra("badge_count_package_name", context.packageName)
            intent.putExtra("badge_count_class_name", launcherClassName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消通知
     */
    private fun cancelNotice() {
        notificationManager.cancel(xiaomiNotificationId)
    }

    /**
     * @param appCompatActivity activity页面
     * 注册通知监听
     */
    private fun registerNotice(appCompatActivity: Context?) {}


    companion object {

        fun getOpenClassName(): String {
            return ""
        }

        /**
         * @return 获取手机厂商
         * Xiaomi  HUAWEI samsung
         */
        private val phoneMode: String
            get() = Build.MANUFACTURER
    }

    /**
     * 设置小米通知
     * @param notification Notification
     */
    fun xiaomiNotification(notification: Notification?) {
        xiaomiNotification = notification
    }

    fun xiaomiNotification12(notification: Notification?) {
        xiaomiNotification = notification
    }


    inline fun params(func: DesktopCornerMark.() -> Unit): DesktopCornerMark =
        apply {
            this.func()
        }
}