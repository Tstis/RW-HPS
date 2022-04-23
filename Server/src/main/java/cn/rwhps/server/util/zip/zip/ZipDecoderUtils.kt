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
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.struct.Seq
import java.io.InputStream

internal interface ZipDecoderUtils {
    fun close()

    /**
     * 获取ZIP内的指定后辍的文件名(无后辍)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun getSpecifiedSuffixInThePackage(endWith: String): OrderedMap<String, ByteArray>

    /**
     * 获取ZIP内的指定后辍的文件名(全名)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun getSpecifiedSuffixInThePackageAllFileName(endWith: String): OrderedMap<String, ByteArray>

    /**
     * 获取ZIP内的指定后辍的文件名(全名+路径)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun getSpecifiedSuffixInThePackageAllFileNameAndPath(endWithSeq: Seq<String>): OrderedMap<String, ByteArray>

    /**
     * 获取ZIP内的指定结尾的文件名(全名+路径)与bytes
     * @param endWith String
     * @return OrderedMap<String, ByteArray>
     */
    fun modsLoadingDedicated(): OrderedMap<String, ByteArray>

    /**
     * 获取ZIP内满足后辍的文件名(无后辍)
     * @param endWith String
     * @return Seq<String>
     */
    fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String>

    /**
     * 获取地图文件字节
     * @param mapData MapData
     * @return ByteArray
     */
    @Throws(Exception::class)
    fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: GameMaps.MapData): ByteArray

    fun getZipNameInputStream(name: String): InputStream?
}