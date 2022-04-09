/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.net

import cn.rwhps.server.util.IsUtil.isBlank
import cn.rwhps.server.util.IsUtil.notIsBlank
import cn.rwhps.server.util.log.Log.error
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Dr
 */
object HttpRequestDefault {
    fun downUrl(url: String?, file: File): Boolean {
        var conn: HttpURLConnection? = null
        try {
            val filepath = file.parentFile
            if (!filepath.exists()) {
                filepath.mkdirs()
            }
            var httpUrl = URL(url)
            while (true) {
                conn = httpUrl.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")
                httpUrl = if (conn.responseCode == 301 || conn.responseCode == 302) {
                    val newUrl = conn.getHeaderField("Location")
                    if (isBlank(newUrl)) {
                        error("Download Fail: Empty Redirect", url!!)
                        return false
                    }
                    conn.disconnect()
                    URL(newUrl)
                } else {
                    break
                }
            }
            BufferedInputStream(conn!!.inputStream).use { bis ->
                BufferedOutputStream(FileOutputStream(file)).use { bos ->
                    val buf = ByteArray(4096)
                    var length = bis.read(buf)
                    while (length != -1) {
                        bos.write(buf, 0, length)
                        length = bis.read(buf)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            error("downUrl", e)
        } finally {
            if (notIsBlank(conn)) {
                conn!!.disconnect()
            }
        }
        return false
    }
}