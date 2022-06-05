/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.zip.zip

import cn.rwhps.server.game.GameMaps
import cn.rwhps.server.io.input.ReusableDisableSyncByteArrayInputStream
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.alone.annotations.NeedToRefactor
import cn.rwhps.server.util.file.FileStream.readFileData
import cn.rwhps.server.util.io.IoRead
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.exp.FileException
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry

internal class ZipFileDecoder: ZipDecoderUtils {
    private var zipFile: ZipFile
    private lateinit var file: File

    constructor(file: File) {
        zipFile = ZipFile(file)
        this.file = file
    }

    constructor(zipFile: ZipFile) {
        this.zipFile = zipFile
    }

    override fun close() {
        this.zipFile.close()
    }

    /**
     * 获取ZIP内的指定后辍的文件名(无后辍)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    override fun getSpecifiedSuffixInThePackage(endWith: String): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        var zipEntry: ZipArchiveEntry
        try {
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                val entries = zipFile.entries
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement() as ZipArchiveEntry
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
            Log.error(e)
        }
        return data
    }

    /**
     * 获取ZIP内的指定后辍的文件名(全名)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    override fun getSpecifiedSuffixInThePackageAllFileName(endWith: String): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            //ZipInputStream(FileInputStream(file), Charset.forName("GBK")).use {
                var zipEntry: ZipArchiveEntry
                IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                    val entries = zipFile.entries
                    while (entries.hasMoreElements()) {
                        zipEntry = entries.nextElement() as ZipArchiveEntry
                        val nameCache = zipEntry.name

                        val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                        if (name.endsWith(endWith)) {
                            //Log.debug(nameCache,name)
                            data.put(name, multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry)))
                        }
                    }
                }
            //}
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    /**
     * 获取ZIP内的指定后辍的文件名(全名+路径)与bytes
     * @param endWithSeq String List
     * @return OrderedMap<String, ByteArray>
     */
    override fun getSpecifiedSuffixInThePackageAllFileNameAndPath(endWithSeq: Seq<String>): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            var zipEntry: ZipArchiveEntry
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                val entries = zipFile.entries
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement() as ZipArchiveEntry
                    val nameCache = zipEntry.name
                    endWithSeq.each({ nameCache.endsWith(it) }) { _: String ->
                        //Log.debug(nameCache,name)
                        data.put(nameCache, multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry)))
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    @NeedToRefactor
    override fun modsLoadingDedicated(): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            val cache = ReusableDisableSyncByteArrayInputStream()
            var zipEntry: ZipArchiveEntry
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                // 使用这个与 ZipFile.entries() 同步
                // 我也不知道为什么 但这可以解决问题
                val entries = zipFile.entriesInPhysicalOrder
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement() as ZipArchiveEntry
                    val nameCache = zipEntry.name
                    if (zipEntry.size < 1 * 1024 * 1024 && zipEntry.size > 0) {
                        val bytes = multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
                        cache.setBytes(bytes)
                        if (isIni(cache)) {
                            data.put(nameCache,bytes)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    /**
     * 获取ZIP内满足后辍的文件名(无后辍)
     * @param endWith String
     * @return Seq<String>
     */
    override fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String> {
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
    override fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: GameMaps.MapData): ByteArray {
        var zipEntry: ZipArchiveEntry
        val entries = zipFile.entries
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement() as ZipArchiveEntry
            val name = zipEntry.name
            if (name.endsWith(mapData.type) && name.contains(mapData.mapFileName)) {
                return IoRead.readInputStreamBytes(zipFile.getInputStream(zipEntry))
            }
        }
        throw FileException("CANNOT_FIND_FILE")
    }

    override fun getZipNameInputStream(name: String): InputStream? {
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
            Log.error(e)
        }
        return null
    }

    private fun isIni(inputStream: InputStream): Boolean {
        var result = false
        readFileData(inputStream) {
            if (it.contains("core",ignoreCase = true) || it.contains("action",ignoreCase = true)) {
                result = true
            }
        }
        return result
    }
}