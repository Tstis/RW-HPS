/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.io

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object IoOutConversion {
    @JvmStatic
    @Throws(IOException::class)
    fun fileToOutStream(file: File, cover: Boolean): OutputStreamWriter =
        OutputStreamWriter(fileToStream(file, cover), StandardCharsets.UTF_8)

    @JvmStatic
    @Throws(IOException::class)
    fun fileToStream(file: File, cover: Boolean): FileOutputStream =
        FileOutputStream(file, cover)
}