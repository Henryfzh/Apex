package pers.shennoter

import com.google.gson.Gson
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO


fun craftStat():String{
    var requestStr = ""
    try {
        val url = "https://api.mozambiquehe.re/crafting?&auth=FsKmkWPMRJlOEmW8H3ZN"
        requestStr = URL(url).readText()
    }catch (e:Exception){
        RankLookUp.logger.error("URL访问失败")
        return "查询失败"
    }


    val res = Gson().fromJson(requestStr, ApexResponseCraft::class.java)
    val daily1: BufferedImage = ImageIO.read(URL(res[0].bundleContent[0].itemType.asset))
    val daily2: BufferedImage = ImageIO.read(URL(res[0].bundleContent[1].itemType.asset))
    val weekly1: BufferedImage = ImageIO.read(URL(res[1].bundleContent[0].itemType.asset))
    val weekly2: BufferedImage = ImageIO.read(URL(res[1].bundleContent[1].itemType.asset))
    val img1: BufferedImage = mergeImage(true, daily1, daily2)
    val img2: BufferedImage = mergeImage(true, weekly1, weekly2)
    val img = mergeImage(false, img1, img2)
    val folder = File("./data/pers.shennoter.ranklookup")
    if(!folder.exists()) {
        folder.mkdirs()
    }
    ImageIO.write(img,"png", File("./data/pers.shennoter.ranklookup/craft.png"))
    return "查询完毕"
}

fun mergeImage(isHorizontal: Boolean, vararg imgs: BufferedImage): BufferedImage {
    // 生成新图片
    var destImage: BufferedImage? = null
    // 计算新图片的长和高
    var allw = 0
    var allh = 0
    var allwMax = 0
    var allhMax = 0
    // 获取总长、总宽、最长、最宽
    for (i in imgs.indices) {
        val img = imgs[i]
        allw += img.width
        allh += img.height
        if (img.width > allwMax) {
            allwMax = img.width
        }
        if (img.height > allhMax) {
            allhMax = img.height
        }
    }
    // 创建新图片
    destImage = if (isHorizontal) {
        BufferedImage(allw, allhMax, BufferedImage.TYPE_INT_RGB)
    } else {
        BufferedImage(allwMax, allh, BufferedImage.TYPE_INT_RGB)
    }
    // 合并所有子图片到新图片
    var wx = 0
    var wy = 0
    for (i in imgs.indices) {
        val img = imgs[i]
        val w1 = img.width
        val h1 = img.height
        // 从图片中读取RGB
        var ImageArrayOne: IntArray? = IntArray(w1 * h1)
        ImageArrayOne = img.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1) // 逐行扫描图像中各个像素的RGB到数组中
        if (isHorizontal) { // 水平方向合并
            destImage.setRGB(wx, 0, w1, h1, ImageArrayOne, 0, w1) // 设置上半部分或左半部分的RGB
        } else { // 垂直方向合并
            destImage.setRGB(0, wy, w1, h1, ImageArrayOne, 0, w1) // 设置上半部分或左半部分的RGB
        }
        wx += w1
        wy += h1
    }
    return destImage
}