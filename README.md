# DesktopCornerMark

![Image text](https://github.com/nuonuoOkami/images/blob/main/DesktopCornerMark.png)

### 写在前面

    聊胜于无的功能需求实现。（反正通知和桌面角标不开就不显示）
    已经线上验证，安全无忧！

### 代码使用
    //显示数量（因为小米的桌面角标需要发一个通知才行，so你懂得）
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

### 支持

    华为
    小米
    三星


### 调研报告

调研报告 [调研报告](https://www.jianshu.com/p/b09c0a1fb540)

### 依赖使用

    implementation 'io.github.nuonuoOkami:DesktopCornerMark:1.1.0'

### 使用方式

**小米**<br />
需要打开通知权限，和桌面图标角标权限 <br/>

**华为**<br />
需要打开通知权限，和桌面图标角标<br />

**其他**
暂无

### Change Log

#### 1.0.0
    支持小米，华为，三星
#### 1.1.0
    小米12版本支持

### 联系我

    QQ:1085214089
    
    
    

