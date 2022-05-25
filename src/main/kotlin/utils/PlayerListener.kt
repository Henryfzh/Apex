package pers.shennoter

import ApexResponsePlayer
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import pers.shennoter.RankLookUp.dataFolder
import pers.shennoter.RankLookUp.logger
import playerPicturMode
import playerTextMode
import java.io.File
import java.net.URL
import java.util.*

suspend fun playerStatListener(): TimerTask? {
    val listendPlayer: ListendPlayer = Gson().fromJson(File("$dataFolder/Data.json").readText(), ListendPlayer::class.java)
    listendPlayer.data.forEach { //遍历玩家id创建分数缓存
        if (it.key == "0") return@forEach //跳过占位符
        delay(2000) //延迟2秒防止api过热
        val url = "https://api.mozambiquehe.re/bridge?version=5&platform=PC&player=${it.key}&auth=${Config.ApiKey}"
        val requestStr = URL(url).readText()
        val firstRes = Gson().fromJson(requestStr, ApexResponsePlayer::class.java)
        val cache = File("$dataFolder/score/listened_${it.key}.score") //缓存文件，保存玩家分数
        if (!cache.exists()) {
            cache.createNewFile()
        }
        cache.writeText(firstRes.global.rank.rankScore) //将首次获取到的分数写入缓存文件
    }
    val playerTask : TimerTask = object :TimerTask() { //定时任务
        override fun run() {
            listendPlayer.data.forEach { it_id -> //遍历玩家id
                if (it_id.key == "0") return@forEach //跳过占位符
                GlobalScope.launch {  //每个id开一个协程
                    delay(2000) //延迟2秒防止api过热
                    val url = "https://api.mozambiquehe.re/bridge?version=5&platform=PC&player=${it_id.key}&auth=${Config.ApiKey}"
                    val cache = File("$dataFolder/score/listened_${it_id.key}.score")
                    val res = Gson().fromJson(URL(url).readText(), ApexResponsePlayer::class.java)
                    if (res.global.rank.rankScore != cache.readText()) { //如果分数变化
                        if (Config.mode == "pic") { //图片模式
                            val image = ApexImage()
                            playerPicturMode(res, it_id.key, image)
                            it_id.value.forEach { it_group -> //遍历玩家id对应的群号
                                Bot.instances.forEach { //遍历bot实例发送消息
                                    it.getGroup(it_group!!)
                                        ?.sendMessage("${it_id.key}的分数已更新!\n${cache.readText()} --> ${res.global.rank.rankScore}")
                                    it.getGroup(it_group)?.sendImage(image.get())
                                }
                            }
                            cache.writeText(res.global.rank.rankScore)
                        } else { //文字模式
                            it_id.value.forEach { it_group -> //遍历玩家id对应的群号
                                Bot.instances.forEach { //遍历bot实例发送消息
                                    it.getGroup(it_group!!)
                                        ?.sendMessage("${it_id.key}的分数已更新!\n${cache.readText()} --> ${res.global.rank.rankScore}")
                                    it.getGroup(it_group)?.sendMessage(playerTextMode(res, it_id.key))
                                }
                            }
                            cache.writeText(res.global.rank.rankScore)  //将刚获取到的分数写入缓存文件
                        }
                    }
                    logger.info("完成了一次对${it_id}的监听")
                }
            }
        }
    }
    Timer().schedule(playerTask, 0, Config.listenInterval.toLong() * 60 * 1000) //开始执行定时任务
    return playerTask
}