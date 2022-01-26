/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.file

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.SortList
import com.github.dr.rwserver.util.io.IoOutConversion.fileToOutStream
import com.github.dr.rwserver.util.io.IoOutConversion.fileToStream
import com.github.dr.rwserver.util.io.IoRead.readFileToByteArray
import com.github.dr.rwserver.util.io.IoReadConversion.fileToReadStream
import com.github.dr.rwserver.util.io.IoReadConversion.fileToStream
import com.github.dr.rwserver.util.log.Log.error
import java.io.*
import java.net.URLDecoder.decode

/**
 * FileUtil() refers to instance fileUtil. FileUtil refers to static method
 *
 * Recommended tutorial:
 * FileUtil.getFile("文件名") 因为不会创建文件 同时 位置和Jar同目录
 * FileUtil.getFolder("文件夹名") 因为不会创建文件 只会创建目录
 * FileUtil.getFolder("文件夹名").toFile("文件名") 只会创建目录
 *
 *
 * FileUtil The three instances of will not do anything and will not create directories and files:
 * 如果需要先目录再文件 那么用FileUtil.toFolder(文件夹名).toFile(文件名)
 * 如果需要先进入多个目录 那么用FileUtil.toFolder(文件夹名).toFolder(文件夹名)
 *
 * 只有使用FileUtil().read/Write时才会进行文件创建
 *
 * 注意:
 * FileUtil.toFolder初始目录是Server.jar的目录或者Main提交的参数目录
 * toFolder只是起一个进入作用
 * FileUtil().mkdir()会创建文件夹并尝试创建文件
 * FileUtil().createNewFile()会尝试创建文件
 *
 *
 * 部分误区:
 * 在操作FileUtil()的时候不会进行创建文件,但是当你操作File的时候,那么就会创建文件
 *     例子:
 *         FileUtil.getFile("文件名").exists()的时候就不会创建文件
 *         FileUtil.getFile("文件名").getInputsStream()的时候就会自动创建一个文件
 * 欢迎提交修改
 */
/**
 * Server文件处理核心
 * @author Dr
 */
class FileUtil {
    /** 内部的File  */
    val file: File

    /** 当前操作的文件  */
    val path: String

    constructor(file: File) {
        this.file = file
        this.path = file.path
    }

    constructor(filepath: String) {
        this.path = filepath
        file = File(filepath)
    }

    private constructor(file: File, filepath: String) {
        this.file = file
        this.path = filepath
    }

    private constructor(file: File, filepath: String, air: String) {
        this.file = file
        this.path = filepath
        file.mkdirs()
    }

    fun exists(): Boolean {
        return file.exists()
    }

    fun notExists(): Boolean {
        return !file.exists()
    }

    fun length(): Long {
        return file.length()
    }

    fun toFile(filename: String): FileUtil {
        return FileUtil(File(this.path + "/" + filename))
    }

    fun toFolder(filename: String): FileUtil {
        var to = this.path
        if ("/" == filename[0].toString()) {
            to += filename.substring(1, filename.length)
        } else {
            this.path + "/" + filename
        }
        return FileUtil(File(to),to,"")
    }

    val fileList: Seq<File>
        get() {
            val array = file.listFiles()
            val fileList = Seq<File>()
            if (IsUtil.isBlank(array)) {
                return fileList
            }
            for (value in array!!) {
                if (!value.isDirectory) {
                    if (value.isFile) {
                        fileList.add(value)
                    }
                }
            }
            return fileList
        }

    val fileListNotNullSize: Seq<File>
        get() {
            val list = Seq<File>()
            fileList.eachBooleanIfs({ e: File -> e.length() > 0 }) { value: File -> list.add(value) }
            return list
        }

    val fileListNotNullSizeSort: Seq<File>
        get() {
            val list = fileListNotNullSize
            SortList.sortByFileName(list)
            return list
        }

    /**
     *
     * @param log Log
     * @param cover 是否尾部写入
     */
    fun writeFile(log: Any, cover: Boolean = false) {
        mkdir()
        try {
            fileToOutStream(file, cover).use { osw ->
                osw.write(log.toString())
                osw.flush()
            }
        } catch (e: Exception) {
            error("writeFile", e)
        }
    }

    fun writeFileByte(bytes: ByteArray, cover: Boolean) {
        mkdir()
        try {
            BufferedOutputStream(FileOutputStream(file, cover)).use { osw ->
                osw.write(bytes)
                osw.flush()
            }
        } catch (e: Exception) {
            error("writeByteFile", e)
        }
    }

    @Throws(Exception::class)
    fun writeByteOutputStream(cover: Boolean): FileOutputStream {
        mkdir()
        return fileToStream(file, cover)
    }

    @Throws(IOException::class)
    fun getInputsStream(): FileInputStream {
        mkdir()
        return fileToStream(file)
    }

    @Throws(IOException::class)
    fun readInputsStream(): InputStreamReader {
        mkdir()
        return fileToReadStream(file)
    }

    @Throws(Exception::class)
    fun readFileByte(): ByteArray {
        mkdir()
        return readFileToByteArray(file)
    }

    fun readFileStringData(): String {
        mkdir()
        try {
            FileInputStream(file).use { fileInputStream -> return readFileString(fileInputStream) }
        } catch (fileNotFoundException: FileNotFoundException) {
            error("FileNotFoundException",fileNotFoundException)
        } catch (ioException: IOException) {
            error("Read IO Error", ioException)
        }
        return ""
    }

    fun readFileListStringData(): Seq<String> {
        mkdir()
        try {
            FileInputStream(file).use { fileInputStream -> return readFileListString(fileInputStream) }
        } catch (fileNotFoundException: FileNotFoundException) {
            error("FileNotFoundException")
        } catch (ioException: IOException) {
            error("Read IO Error", ioException)
        }
        return Seq()
    }

    fun copy(newFile: FileUtil) {
        FileOperation.copyFile(file,newFile.file)
    }

    fun mkdir() {
        file.parentFile.mkdirs()

        if (file.isDirectory) {
            return
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                error("Mk file", e)
            }
        }
    }

    fun createNewFile() {
        if (file.isDirectory) {
            return
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                error("Mk file", e)
            }
        }
    }

    companion object {
        /**
         * 默认的地址前缀
         * 如果不为null将会直接使用path而不是使用jar的位置
         */
		private var defaultFilePath: String

        /**
         * 加载默认的文件路径
         * 默认使用Jar同目录下的 与启动的位置无关
         */
		init {
            val path = this::class.java.protectionDomain.codeSource.location.path
            val pathSplit = path.split("/").toTypedArray()
            val jarName = pathSplit[pathSplit.size - 1]
            val jarPath = path.replace(jarName, "")

            defaultFilePath = decode(jarPath, "UTF-8")
		}

        /**
         * 设置Jar的数据存储位置
         * @param customFilePath String
         */
        @JvmStatic
        fun setFilePath(customFilePath: String? = null) {
            if (customFilePath != null) {
                defaultFilePath = customFilePath
            }
        }

        /**
         * 进入目录
         * @param toFile String?
         * @return FileUtil
         */
        @JvmStatic
		@JvmOverloads
        fun getFolder(toFile: String? = null): FileUtil {
            val filepath: String
            var to = toFile
            if (null != toFile) {
                /*
                 * 防止传入/开头的导致filepath//xxx
                 */
                if ("/" == toFile[0].toString()) {
                    to = toFile.substring(1,toFile.length)
                }
            }
            /* 防止传入/ */
            filepath = if (null == to) {
                defaultFilePath
            } else {
                defaultFilePath + to
            }
            return FileUtil(File(filepath), filepath,"")
        }

        @JvmStatic
        fun getFile(toFile: String): FileUtil {
            return FileUtil(File(defaultFilePath + toFile))
        }

        @JvmStatic
        fun getTempFile(prefix: String): FileUtil {
            return getFolder(Data.Plugin_Cache_Path).toFile(prefix)
        }

        @JvmStatic
        fun getTempDirectory(prefix: String): FileUtil {
            return getFolder(Data.Plugin_Cache_Path).toFolder(prefix)
        }

        @JvmStatic
        fun readFileString(inputStream: InputStream): String {
            return FileStream.readFileString(inputStream)
        }

        @JvmStatic
        fun readFileListString(inputStream: InputStream): Seq<String> {
            return FileStream.readFileListString(inputStream)
        }
    }
}