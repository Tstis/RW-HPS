/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.zip.zip

import com.github.dr.rwserver.game.GameMaps.MapData
import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.io.IoRead
import com.github.dr.rwserver.util.io.IoRead.MultiplexingReadStream
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.log.exp.FileException
import org.apache.compress.ZipEntry
import org.apache.compress.ZipFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

/**
 * 解码
 * @author Dr
 */
class ZipDecoder {
    private var zipFile: ZipFile
    private lateinit var file: File

    constructor(file: File) {
        zipFile = ZipFile(file)
        this.file = file
    }

    constructor(zipFile: ZipFile) {
        this.zipFile = zipFile
    }

    /**
     * 获取ZIP内的指定后辍的文件名(无后辍)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun getSpecifiedSuffixInThePackage(endWith: String): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        var zipEntry: ZipEntry
        try {
            MultiplexingReadStream().use { multiplexingReadStream ->
                val entries = zipFile.entries
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement() as ZipEntry
                    val nameCache = zipEntry.name
                    val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                    if (name.endsWith(endWith)) {
                        data.put(
                            name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length),
                            multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
                        )
                    }
                }
            }
        } catch (e: IOException) {
            error(e)
        }
        return data
    }

    /**
     * 获取ZIP内的指定后辍的文件名(全名)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun getSpecifiedSuffixInThePackageAllFileName(endWith: String): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            ZipInputStream(FileInputStream(file), Charset.forName("GBK")).use {
                var zipEntry: ZipEntry
                MultiplexingReadStream().use { multiplexingReadStream ->
                    val entries = zipFile.entries
                    while (entries.hasMoreElements()) {
                        zipEntry = entries.nextElement() as ZipEntry
                        val nameCache = zipEntry.name
                        val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                        if (name.endsWith(endWith)) {
                            data.put(
                                name,
                                multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
                            )
                        }
                    }
                }
            }
        } catch (e: IOException) {
            error(e)
        }
        return data
    }

    /**
     * 获取ZIP内满足后辍的文件名(无后辍)
     * @param endWith String
     * @return Seq<String>
     */
    fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String> {
        // Max 5M
        val maxSize = 1024 * 1024 * 5
        val data = Seq<String>(8)
        var zipEntry: ZipEntry
        val entries = zipFile.entries
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement() as ZipEntry
            if (zipEntry.size >= maxSize) {
                continue
            }
            val name = zipEntry.name
            if (name.endsWith(endWith)) {
                data.add(name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length))
            }
        }
        return data
    }

    /**
     * 获取地图文件字节
     * @param mapData MapData
     * @return ByteArray
     */
    @Throws(Exception::class)
    fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: MapData): ByteArray {
        ZipInputStream(FileInputStream(file), Charset.forName("GBK")).use {
            var zipEntry: ZipEntry
            val entries = zipFile.entries
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement() as ZipEntry
                val name = zipEntry.name
                if (name.endsWith(mapData.type) && name.contains(mapData.mapFileName)) {
                    return IoRead.readInputStreamBytes(zipFile.getInputStream(zipEntry))
                }
            }
        }
        throw FileException("CANNOT_FIND_FILE")
    }

    fun getZipNameInputStream(name: String): InputStream? {
        try {
            val entries = zipFile.entries
            var ze: ZipEntry
            while (entries.hasMoreElements()) {
                ze = entries.nextElement()
                if (!ze.isDirectory) {
                    if (ze.name == name) {
                        return zipFile.getInputStream(ze)
                    }
                }
            }
        } catch (e: Exception) {
            error(e)
        }
        return null
    }
}