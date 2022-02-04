<<<<<<< HEAD:Server/src/main/java/com/github/dr/rwserver/io/input/CompressInputStream.kt
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.io.input

import com.github.dr.rwserver.util.zip.gzip.GzipDecoder

class CompressInputStream(inputStream: DisableSyncByteArrayInputStream): GameInputStream(inputStream) {
    companion object {
        @JvmStatic
        fun getGzipInputStream(isGzip: Boolean, bytes: ByteArray): CompressInputStream {
            return CompressInputStream(
                if (isGzip) {
                    DisableSyncByteArrayInputStream(GzipDecoder.getUnGzipBytes(bytes))
                } else {
                    DisableSyncByteArrayInputStream(bytes)
                }
            )
        }
    }
=======
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.io.input

import com.github.dr.rwserver.util.zip.gzip.GzipDecoder

class CompressInputStream(inputStream: DisableSyncByteArrayInputStream): GameInputStream(inputStream) {
    companion object {
        @JvmStatic
        fun getGzipInputStream(isGzip: Boolean, bytes: ByteArray): CompressInputStream {
            return CompressInputStream(
                if (isGzip) {
                    DisableSyncByteArrayInputStream(GzipDecoder.getUnGzipBytes(bytes))
                } else {
                    DisableSyncByteArrayInputStream(bytes)
                }
            )
        }
    }
>>>>>>> ab468a8efac1b6b73535dee9461c3323bd13d144:Server/src/main/java/com/github/dr/rwserver/util/zip/CompressInputStream.kt
}