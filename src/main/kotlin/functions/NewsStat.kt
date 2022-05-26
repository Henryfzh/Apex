import com.google.gson.Gson
import pers.shennoter.*
import utils.getRes
import java.awt.image.BufferedImage

fun newsStat(image: ApexImage,index:Int):String?{
    if(Config.ApiKey == "") {
        return "未填写ApiKey"
    }
    val url = "https://api.mozambiquehe.re/news?&auth=${Config.ApiKey}"
    val requestStr = getRes(url)
    if (requestStr.first == 1) {
        RankLookUp.logger.error(requestStr.second)
        return requestStr.second
    }
    val res = Gson().fromJson(requestStr.second, ApexResponseNews::class.java)
    if(index < 1 || index > res.size){
        return "序号无效"
    }
    val img: BufferedImage = ImageCache("news_" + res[index-1].short_desc.hashCode(), res[index-1].img)
    val title = res[index-1].title
    val link = res[index-1].link
    val digest = res[index-1].short_desc
    var msg = "标题：$title\n"
    msg += "链接：$link\n"
    msg += "摘要:$digest\n"
    msg += "序号：$index/${res.size}"
    image.save(img)

    return msg
}