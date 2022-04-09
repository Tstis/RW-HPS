/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.zip.zip

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.io.IoRead.copyInputStream
import org.apache.compress.ZipEntry
import org.apache.compress.ZipFile
import org.apache.compress.ZipOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * @author Dr
 */
object ZipEncoder {
    /**
     * 合并压缩文件
     * @param sourceZipFiles in
     * @return byte out
     * @throws IOException err
     */
    @JvmStatic
    @Throws(IOException::class)
    fun incrementalUpdate(updateFile: Seq<String>, vararg sourceZipFiles: String?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { out ->
            val names = HashSet<String>()
            for (sourceZipFile in sourceZipFiles) {
                ZipFile(sourceZipFile).use { zipFile ->
                    var ze: ZipEntry
                    val enumeration: Enumeration<out ZipEntry> = zipFile.entries
                    while (enumeration.hasMoreElements()) {
                        ze = enumeration.nextElement()
                        if (!ze.isDirectory) {
                            /* 只合并第一个源压缩包里面的文件，后面若有相同的文件则跳过执行合并 */
                            if (names.contains(ze.name) || !updateFile.contains(ze.name)) {
                                continue
                            }
                            val oze = ZipEntry(ze.name)
                            out.putNextEntry(oze)
                            if (ze.size > 0) {
                                copyInputStream(zipFile.getInputStream(ze), out)
                                out.closeEntry()
                                out.flush()
                            }
                            names.add(oze.name)
                        }
                    }
                }
            }
        }
        return outputStream.toByteArray()
    }
}