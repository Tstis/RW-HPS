/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.io

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 流转换
 * @author Dr
 */
object IoReadConversion {
    @JvmStatic
    @JvmOverloads fun streamBufferRead(inputStream: InputStream, charset: Charset = StandardCharsets.UTF_8): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream, charset))
    }

    @JvmStatic
    @Throws(IOException::class)
    @JvmOverloads fun fileToBufferReadStream(file: File, charset: Charset = StandardCharsets.UTF_8): BufferedReader {
        return BufferedReader(fileToReadStream(file,charset))
    }

    @JvmStatic
    @Throws(IOException::class)
    @JvmOverloads fun fileToReadStream(file: File, charset: Charset = StandardCharsets.UTF_8): InputStreamReader {
        return InputStreamReader(fileToStream(file), charset)
    }

    @JvmStatic
    @Throws(IOException::class)
    @JvmOverloads fun fileToReadStream(inputStream: InputStream, charset: Charset = StandardCharsets.UTF_8): InputStreamReader {
        return InputStreamReader(inputStream, charset)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToStream(file: File): FileInputStream {
        return FileInputStream(file)
    }
}