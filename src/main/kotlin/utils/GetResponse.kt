package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import pers.shennoter.Config
import java.net.InetSocketAddress

fun getRes(url: String):Pair<Int,String?>{
    val builder = OkHttpClient.Builder()
    val type = when(Config.proxyType){
        "HTTP" -> java.net.Proxy.Type.HTTP
        "SOCKS" -> java.net.Proxy.Type.SOCKS
        else -> return Pair(1, "代理设置错误")
    }
    if(Config.ifProxy) {
        val socket = InetSocketAddress(Config.hostName, Config.port) //构造套接字
        val proxy = java.net.Proxy(type, socket)
        builder.proxy(proxy) //构造代理
    }
    val client = builder //构造client
        .build()
    val request: Request = Request.Builder() //构造request
        .url(url)
        .get()
        .build()
    val response = try {
        client.newCall(request).execute()
    }
    catch (e:Exception){
        return Pair(1, "网络请求发起错误")
    }
    val body = response.body?.string()
    response.close()
    return when(response.code){
        200 -> Pair(0, body)
        400 -> Pair(1, "请重试")
        403 -> Pair(1, "API key无权限或不存在")
        404 -> Pair(1, "玩家不存在")
        405 -> Pair(1, "外部API错误")
        410 -> Pair(1, "未知平台")
        429 -> Pair(1, "API过热，请稍后再试")
        500 -> Pair(1, "API服务器内部错误")
        else -> Pair(1, "未知错误")
    }
}