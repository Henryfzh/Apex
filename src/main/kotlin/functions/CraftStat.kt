import com.google.gson.Gson
import pers.shennoter.*
import utils.getRes
import java.awt.image.BufferedImage


fun craftStat(image: ApexImage):String?{
    if(Config.ApiKey == "") {
        return "未填写ApiKey"
    }
    val url = "https://api.mozambiquehe.re/crafting?&auth=${Config.ApiKey}"
    val requestStr = getRes(url)
    if (requestStr.first == 1) {
        RankLookUp.logger.error(requestStr.second)
        return requestStr.second
    }
    val res = Gson().fromJson(requestStr.second, ApexResponseCraft::class.java)
    val daily1: BufferedImage = ImageCache("craft_"+res[0].bundleContent[0].itemType.name,res[0].bundleContent[0].itemType.asset)
    val daily2: BufferedImage = ImageCache("craft_"+res[0].bundleContent[1].itemType.name,res[0].bundleContent[1].itemType.asset)
    val weekly1: BufferedImage = ImageCache("craft_"+res[1].bundleContent[0].itemType.name,res[1].bundleContent[0].itemType.asset)
    val weekly2: BufferedImage = ImageCache("craft_"+res[1].bundleContent[1].itemType.name,res[1].bundleContent[1].itemType.asset)
    val img1: BufferedImage = mergeImage(true, daily1, daily2)
    val img2: BufferedImage = mergeImage(true, weekly1, weekly2)
    val img = mergeImage(false, img1, img2)
    image.save(img)
    return "查询成功"
}