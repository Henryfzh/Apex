package pers.shennoter

import ApexResponsePlayer
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import pers.shennoter.RankLookUp.dataFolder
import playerPicturMode
import playerTextMode
import java.io.File
import java.net.URL
import java.util.*

suspend fun playerStatListener(): Job {
    val playerJob = GlobalScope.launch {
        val listendPlayer: ListendPlayer = Gson().fromJson(File("$dataFolder/Data.json").readText(), ListendPlayer::class.java)
        listendPlayer.data.forEach { it_id -> //遍历玩家id
            if (it_id.key == "0") return@forEach
            delay(2000) //延迟2秒防止api过热
            val url = "https://api.mozambiquehe.re/bridge?version=5&platform=PC&player=${it_id.key}&auth=${Config.ApiKey}"
            val requestStr = URL(url).readText()
            val firstRes = Gson().fromJson(requestStr, ApexResponsePlayer::class.java)
            val cache = File("$dataFolder/score/listened_${it_id.key}.score") //缓存文件，保存玩家分数
            if (!cache.exists()) {
                cache.createNewFile()
            }
            cache.writeText(firstRes.global.rank.rankScore) //将首次获取到的分数写入缓存文件
            it_id.value.forEach { it_group -> //遍历QQ群
                RankLookUp.logger.info("开始监听${it_id.key}:$it_group")
            }
            Timer().schedule(object : TimerTask() { //每个玩家都开一个定时任务，当时间到时检查分数是否有变化
                override fun run() {
                    GlobalScope.launch {
                        val res = Gson().fromJson(URL(url).readText(), ApexResponsePlayer::class.java)
                        if (res.global.rank.rankScore != cache.readText()) {
                            if (Config.mode == "pic") {
                                cache.writeText(res.global.rank.rankScore)
                                val image = ApexImage()
                                playerPicturMode(res, it_id.key, image)
                                it_id.value.forEach { it_group -> //遍历玩家id对应的群号
                                    Bot.instances.forEach { //遍历bot实例发送消息
                                        it.getGroup(it_group!!)?.sendMessage("${it_id.key}的分数已更新!\n${cache.readText()} --> ${res.global.rank.rankScore}")
                                        it.getGroup(it_group)?.sendImage(image.get())
                                    }
                                }

                            } else {
                                cache.writeText(res.global.rank.rankScore)
                                it_id.value.forEach { it_group -> //遍历玩家id对应的群号
                                    Bot.instances.forEach { //遍历bot实例发送消息
                                        it.getGroup(it_group!!)?.sendMessage("${it_id.key}的分数已更新!\n${cache.readText()} --> ${res.global.rank.rankScore}")
                                        it.getGroup(it_group)?.sendMessage(playerTextMode(res, it_id.key))
                                    }
                                }
                            }
                        }
                    }
                    RankLookUp.logger.info("完成一次对${it_id.key}的监听")
                }
            }, Config.listenInterval.toLong() * 60 * 1000, Config.listenInterval.toLong() * 60 * 1000)

        }
    }
    return playerJob
}