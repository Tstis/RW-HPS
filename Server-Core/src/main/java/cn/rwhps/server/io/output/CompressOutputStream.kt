/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io.output

import cn.rwhps.server.io.GameOutputStream
import java.util.zip.GZIPOutputStream

class CompressOutputStream(val head: String, outputStream: DisableSyncByteArrayOutputStream): GameOutputStream(outputStream) {
    companion object {
        fun getGzipOutputStream(head: String,isGzip: Boolean): CompressOutputStream {
            return CompressOutputStream(head,
                if (isGzip) {
                    object : DisableSyncByteArrayOutputStream() {
                        override fun toByteArray(): ByteArray {
                            val out = DisableSyncByteArrayOutputStream()
                            val gzip = GZIPOutputStream(out)
                            gzip.write(super.toByteArray())
                            gzip.close()
                            return out.toByteArray()
                        }
                    }
                } else {
                    DisableSyncByteArrayOutputStream()
                }
            )
        }
    }
}