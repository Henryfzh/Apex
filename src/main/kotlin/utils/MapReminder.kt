package pers.shennoter

import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import java.io.File
import java.net.URL
import java.util.*


fun mapReminder() :TimerTask{
    val groups: GroupReminding =
        Gson().fromJson(File("${RankLookUp.dataFolder}/Reminder.json").readText(), GroupReminding::class.java)
    var requestStr = ""
    val url = "https://api.mozambiquehe.re/maprotation?version=2&auth=${Config.ApiKey}"
    try {
        requestStr = URL(url).readText()
    } catch (e: Exception) {
        RankLookUp.logger.error("地图信息获取错误，轮换监听已停止")
    }//获取第一次地图信息
    var res = Gson().fromJson(requestStr, ApexResponseMap::class.java)
    val cache = File("${RankLookUp.dataFolder}/map.time")
    if(!cache.exists()) {
        cache.createNewFile()
    }
    cache.writeText(res.battle_royale.current.end.toString()) //将下一次轮换时间写入缓存文件
    groups.data.forEach {
        if (it == 0.toLong()) return@forEach
        RankLookUp.logger.info("已启动对${it}的地图轮换提醒")
    }
    var firstStart = true //第一次启动标志
    val mapTask: TimerTask = object : TimerTask() {
        override fun run() {
            GlobalScope.launch {
                res = Gson().fromJson(URL(url).readText(), ApexResponseMap::class.java)
                val storedEndTime = cache.readText().toLong()//通过对比map.time文件判断是否已在提醒计划中
                val endTime = res.battle_royale.current.end.toLong()
                val currentTime = System.currentTimeMillis() / 1000
                val timeToRemind = (endTime - currentTime) * 1000 + 5000 //算出下次轮换还要多久，再加上五秒防止api没及时更新
                if (endTime != storedEndTime || firstStart) { //若未启动提醒计时则加入计时；如果是第一次启动则忽略一次判断条件，直接开始计时
                    cache.writeText(endTime.toString())
                    GlobalScope.launch {
                        delay(timeToRemind)//计时到轮换时间进行提醒
                        groups.data.forEach {
                            if (it == 0.toLong()) return@forEach //跳过文件中的占位符
                            if (Config.mode == "pic") { //图片模式
                                    val image = ApexImage()
                                    mapPictureMode(res, image)
                                    Bot.instances.forEach { bot ->
                                        bot.getGroup(it!!)?.sendMessage("大逃杀地图已轮换")
                                        bot.getGroup(it)?.sendImage(image.get())
                                    }
                                    RankLookUp.logger.info("大逃杀地图已轮换")
                            } else { //文字模式
                                Bot.instances.forEach { bot ->
                                    bot.getGroup(it!!)?.sendMessage("大逃杀地图已轮换")
                                    bot.getGroup(it)?.sendMessage(mapTextMode(res))
                                }
                                RankLookUp.logger.info("大逃杀地图已轮换")
                            }
                            RankLookUp.logger.info("完成一次对${it}的地图轮换提醒")
                        }
                    }
                }
            }
            firstStart = false
            RankLookUp.logger.info("完成一次地图监听")
        }
    }
    Timer().schedule(mapTask, 0, 30 * 60 * 1000) //每半小时获取一次地图信息
    return mapTask
}