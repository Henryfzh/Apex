package pers.shennoter

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("config") {
    @ValueDescription("ApiKey：如果没有请到 https://apexlegendsapi.com/ 获取")
    val ApiKey: String by value()
    @ValueDescription("额外ApiKey")
    val extendApiKey: ArrayList<String> by value()
    @ValueDescription("回复模式: pic为图片，text为文字(只适用于玩家信息和地图轮换)")
    var mode: String by value("pic")
    @ValueDescription("图片质量: PNG原图，JPG更小")
    var picType: String by value("PNG")
    @ValueDescription("缓存过期时间（单位：天）")
    var cacheExpireTime: Int by value(30)
    @ValueDescription("缓存自动清理：true为启用，false为关闭")
    var cacheAutoDel: String by value("true")
    @ValueDescription("玩家分数监听：true为启用，false为关闭")
    var listener: Boolean by value(false)
    @ValueDescription("监听时间间隔（单位：分钟）")
    var listenInterval: Int by value(5)
    @ValueDescription("地图轮换提醒：true为启用，false为关闭")
    var mapRotationReminder: Boolean by value(false)
    @ValueDescription("字体")
    var font: String by value("微软雅黑")
}