/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.file

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.io.IoOutConversion.fileToOutStream
import cn.rwhps.server.util.io.IoOutConversion.fileToStream
import cn.rwhps.server.util.io.IoRead.readFileToByteArray
import cn.rwhps.server.util.io.IoReadConversion.fileToReadStream
import cn.rwhps.server.util.io.IoReadConversion.fileToStream
import cn.rwhps.server.util.log.Log.error
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
 * FileUtil.getFolder("文件夹名" , true) 返回的 FileUtil 在您操作 toFile toFolder 时 会直接返回新的 FileUtil 对象
 *     而原来的对象可重用 toFile toFolder
 *
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
 * FileUtil(filepath,true).toFile() 会直接返回一个新的 FileUtil
 * FileUtil(filepath,true).toFolder() 会直接返回一个新的 FileUtil
 *
 *
 * 部分误区:
 * 在操作FileUtil()的时候不会进行创建文件,但是当你操作File的时候,那么就会创建文件
 *     例子:
 *         FileUtil.getFile("文件名").exists()的时候就不会创建文件
 *         FileUtil.getFile("文件名").getInputsStream()的时候就会自动创建一个文件
 *
 * 只有FileUtil.getFolder("文件夹名" , true) 创建的 和 FileUtil(filepath,true) 创建的 FileUtil 在您操作 toFile toFolder 时 会直接返回新的 FileUtil 对象
 *
 * 欢迎提交修改
 */
/**
 * Server文件处理核心
 * @author RW-HPS/Dr
 * @version 5.5.0
 */
open class FileUtil {
    /** 内部的File  */
    var file: File
        protected set

    /** 当前操作的文件地址  */
    var path: String
        protected set

    protected val isNewFile: Boolean

    constructor(file: File) {
        this.file = file
        this.path = file.path
        this.isNewFile = false
    }

    constructor(filepath: String, isNewFile: Boolean = false) {
        this.path = filepath
        file = File(filepath)
        this.isNewFile = isNewFile

    }

    protected constructor(file: File, filepath: String, ismkdir: Boolean = false, isNewFile: Boolean = false) {
        this.file = file
        this.path = filepath
        if (ismkdir) {
            file.mkdirs()
        }
        this.isNewFile = isNewFile
    }

    open fun exists(): Boolean {
        return file.exists()
    }

    open fun notExists(): Boolean {
        return !file.exists()
    }

    fun length(): Long {
        return file.length()
    }

    fun toFile(filename: String): FileUtil {
        return if (isNewFile) {
            FileUtil(File(this.path + "/" + filename))
        } else {
            file = File(this.path + "/" + filename)
            path = this.path + "/" + filename
            this
        }
    }

    fun toFolder(filename: String): FileUtil {
        var to = this.path
        to += if ("/" == filename[0].toString()) {
            filename.substring(1, filename.length)
        } else {
            "/$filename"
        }

        return if (isNewFile) {
            FileUtil(File(to),to,true)
        } else {
            this.file = File(to)
            this.path = to
            file.mkdirs()
            this
        }
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

    val filePollingList: OrderedMap<String, File>
        get() {
            val array = file.listFiles()
            val fileList = OrderedMap<String, File>()
            if (IsUtil.isBlank(array)) {
                return fileList
            }
            for (value in array!!) {
                if (!value.isDirectory) {
                    if (value.isFile) {
                        fileList.put(value.name,value)
                    }
                } else {
                    FileUtil(value).filePollingList.each {k,v ->
                        fileList.put("${value.name}/${k}",v)
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
            list.sort { o1, o2 ->
                o1.name.compareTo(o2.name)
            }
            return list
        }

    /**
     *
     * @param log Log
     * @param cover 是否尾部写入
     */
    open fun writeFile(log: Any, cover: Boolean = true) {
        mkdir()
        try {
            fileToOutStream(file, !cover).use { osw ->
                osw.write(log.toString())
                osw.flush()
            }
        } catch (e: Exception) {
            error("writeFile", e)
        }
    }

    open fun writeFileByte(bytes: ByteArray, cover: Boolean) {
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
    open fun writeByteOutputStream(cover: Boolean): FileOutputStream {
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

    open fun readFileStringData(): String {
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

    open fun readFileListStringData(): Seq<String> {
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
                defaultFilePath =
                    if (customFilePath.endsWith("/")) {
                        customFilePath
                    } else {
                        "$customFilePath/"
                    }
            }
        }

        /**
         * 进入目录
         * @param toFile String?
         * @return FileUtil
         */
        @JvmStatic
		@JvmOverloads
        fun getFolder(toFile: String? = null, isNewFile: Boolean = false): FileUtil {
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
            return FileUtil(File(filepath), filepath,true,isNewFile)
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