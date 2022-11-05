package com.nuonuo.desktopcornermark

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<View>(R.id.btn_send).setOnClickListener {

            //显示数量
            val showNumber = 99;
            //小米11以下处理
            val intent = Intent(this, MainActivity::class.java);
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            val notification = NotificationCompat.Builder(this, "小米")
                .setContentTitle("小米测试")
                .setContentText("您有" + showNumber + "条新消息")
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setChannelId("xiaomi")
                .setNumber(showNumber)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL).build()
            //小米12处理
            val xiaomiNotification12 = Notification.Builder(this@MainActivity, "xiaomi")
                .setSmallIcon(androidx.loader.R.drawable.notification_bg)
                .setContentTitle("小米测试")
                .setContentText("小米测试")
                .setNumber(showNumber)
                .build()

            //显示
            DesktopCornerMark(this).params {
                //小米12之前处理
                xiaomiNotification(notification)
                //小米12以后处理
                xiaomiNotification12(xiaomiNotification12)
            }.showDeskMark(showNumber)
        }

    }
}