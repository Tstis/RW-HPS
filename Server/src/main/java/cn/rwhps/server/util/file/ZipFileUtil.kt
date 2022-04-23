/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.file

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.alone.annotations.DidNotFinish
import cn.rwhps.server.util.log.exp.ImplementedException
import java.io.File
import java.io.FileOutputStream

/**
 * Zip文件处理核心
 * @author RW-HPS/Dr
 */
@DidNotFinish
class ZipFileUtil : FileUtil {

    constructor(file: File): super(file) {

    }

    constructor(filepath: String): super(filepath){

    }

    private constructor(file: File, filepath: String, ismkdir: Boolean = false): super(file,filepath,ismkdir) {
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    override fun notExists(): Boolean {
        return !file.exists()
    }

    override fun writeFile(log: Any, cover: Boolean) {
        throw ImplementedException("不应该使用这个")
    }

    override fun writeFileByte(bytes: ByteArray, cover: Boolean) {
        throw ImplementedException("不应该使用这个")
    }

    @Throws(Exception::class)
    override fun writeByteOutputStream(cover: Boolean): FileOutputStream {
        throw ImplementedException("不应该使用这个")
    }

    override fun readFileStringData(): String {
        throw ImplementedException("不应该使用这个")
    }

    override fun readFileListStringData(): Seq<String> {
        throw ImplementedException("不应该使用这个")
    }
}