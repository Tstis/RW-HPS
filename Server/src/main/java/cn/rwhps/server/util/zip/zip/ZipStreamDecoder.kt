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
import cn.rwhps.server.util.file.FileStream
import cn.rwhps.server.util.io.IoRead
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.exp.FileException
import cn.rwhps.server.util.log.exp.ImplementedException
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry

internal class ZipStreamDecoder: ZipDecoderUtils {
    private val zipStream: ZipArchiveInputStream
    private var flag = false

    constructor(inStream: InputStream) {
        this.zipStream = ZipArchiveInputStream (inStream)
    }

    constructor(zipInStream: ZipArchiveInputStream) {
        this.zipStream = zipInStream
    }

    override fun close() {
        this.zipStream.close()
    }

    override fun getSpecifiedSuffixInThePackage(endWith: String): OrderedMap<String, ByteArray> {
        if (flag) {
            throw FileException("REPEAT_READ")
        }
        flag = true
        val data = OrderedMap<String, ByteArray>(8)
        var zipEntry: ZipArchiveEntry?
        try {
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (zipStream.nextZipEntry.also { zipEntry = it } != null) {
                    val nameCache = zipEntry!!.name
                    val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                    if (name.endsWith(endWith)) {
                        data.put(
                            name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length),
                            multiplexingReadStream.readInputStreamBytes(zipStream)
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun getSpecifiedSuffixInThePackageAllFileName(endWith: String): OrderedMap<String, ByteArray> {
        if (flag) {
            throw FileException("REPEAT_READ")
        }
        flag = true
        val data = OrderedMap<String, ByteArray>(8)
        try {
            var zipEntry: ZipArchiveEntry?
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (zipStream.nextZipEntry.also { zipEntry = it } != null) {
                    val nameCache = zipEntry!!.name

                    val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                    if (name.endsWith(endWith)) {
                        //Log.debug(nameCache,name)
                        data.put(name, multiplexingReadStream.readInputStreamBytes(zipStream))
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun getSpecifiedSuffixInThePackageAllFileNameAndPath(endWithSeq: Seq<String>): OrderedMap<String, ByteArray> {
        if (flag) {
            throw FileException("REPEAT_READ")
        }
        flag = true
        val data = OrderedMap<String, ByteArray>(8)
        try {
            var zipEntry: ZipArchiveEntry?
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (zipStream.nextZipEntry.also { zipEntry = it } != null) {
                    val nameCache = zipEntry!!.name

                    endWithSeq.each({ nameCache.endsWith(it) }) { _: String ->
                        //Log.debug(nameCache,name)
                        data.put(nameCache, multiplexingReadStream.readInputStreamBytes(zipStream))
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun modsLoadingDedicated(): OrderedMap<String, ByteArray> {
        if (flag) {
            throw FileException("REPEAT_READ")
        }
        flag = true
        val data = OrderedMap<String, ByteArray>(8)
        try {
            val cache = ReusableDisableSyncByteArrayInputStream()
            var zipEntry: ZipArchiveEntry?
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (zipStream.nextZipEntry.also { zipEntry = it } != null) {
                    val nameCache = zipEntry!!.name
                    val bytes = multiplexingReadStream.readInputStreamBytes(zipStream)

                    if (bytes.size < 1 * 1024 * 1024 && bytes.isNotEmpty()) {
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

    override fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String> {
        if (flag) {
            throw FileException("REPEAT_READ")
        }
        flag = true
        // Max 5M
        val maxSize = 1024 * 1024 * 5
        val data = Seq<String>(8)
        var zipEntry: ZipEntry?
        while (zipStream.nextZipEntry.also { zipEntry = it } != null) {
            if (zipEntry!!.size >= maxSize) {
                continue
            }
            val name = zipEntry!!.name
            if (name.endsWith(endWith)) {
                data.add(name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length))
            }
        }
        return data
    }

    @Throws(Exception::class)
    override fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: GameMaps.MapData): ByteArray {
        throw ImplementedException("Not support")
    }

    override fun getZipNameInputStream(name: String): InputStream? {
        throw ImplementedException("Not support")
    }


    private fun isIni(inputStream: InputStream): Boolean {
        var result = false
        FileStream.readFileData(inputStream) {
            if (it.contains("core", ignoreCase = true) || it.contains("action", ignoreCase = true)) {
                result = true
            }
        }
        return result
    }
}