/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.core

import cn.rwhps.server.core.thread.Threads.addSavePool
import cn.rwhps.server.core.thread.Threads.runSavePool
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.plugin.PluginData
import cn.rwhps.server.data.plugin.PluginManage.runOnDisable
import cn.rwhps.server.net.Administration
import cn.rwhps.server.util.IsUtil.isBlank
import cn.rwhps.server.util.RandomUtil.getRandomString
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.file.FileUtil.Companion.getFolder
import cn.rwhps.server.util.log.Log.error
import cn.rwhps.server.util.log.Log.logCache
import java.lang.management.ManagementFactory
import java.util.*


/**
 * @author RW-HPS/Dr
 */
class Application {
    val settings: PluginData = PluginData()

    /** 服务器唯一UUID  */
    lateinit var serverConnectUuid: String
    @JvmField
    var serverToken: String = getRandomString(40)
    lateinit var admin: Administration
    @JvmField
    var upServerList = false

    fun load() {
        settings.setFileUtil(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Settings.bin"))
        admin = Administration(settings)

        Initialization.startInit(settings)

        serverConnectUuid = settings.getData("serverConnectUuid") { UUID.randomUUID().toString() }
        addSavePool {
            settings.setData("serverConnectUuid", serverConnectUuid)
        }
    }

    fun save() {
        // 先执行自己的保存
        runSavePool()
        // 保存自己
        settings.save()
        // 保存Plugin
        runOnDisable()

        val fileUtil = getFolder(Data.Plugin_Log_Path).toFile("Log.txt")
        fileUtil.writeFile(logCache, fileUtil.file.length() <= 1024 * 1024)
    }

    val javaHeap: Long
        get() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    val javaTotalMemory: Long
        get() = Runtime.getRuntime().totalMemory()
    val javaFreeMemory: Long
        get() = Runtime.getRuntime().freeMemory()
    val javaVendor: String
        get() = get("java.vendor")
    val javaVersion: String
        get() = get("java.version")
    val osName: String
        get() = get("os.name")

    fun isJavaVersionAtLeast(requiredVersion: Float): Boolean {
        val version = get("java.version")
        return (if (isBlank(version)) 0f else version.toFloat()) >= requiredVersion
    }

    val isWindows: Boolean
        get() {
            val os = get("os.name")
            return isBlank(os) || os.lowercase(Locale.getDefault()).contains("windows")
        }
    val pid: Long
        // 唯一的到Java11理由
        // get() = ProcessHandle.current().pid()
        // 现在理由它没了
        get() = jvmPid()


    private fun jvmPid(): Long {
        return try {
            var pid = ManagementFactory.getRuntimeMXBean().name
            val indexOf = pid.indexOf('@')
            if (indexOf > 0) {
                pid = pid.substring(0, indexOf)
            }
            pid.toLong()

        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 取得系统属性，如果因为Java安全的限制而失败，则将错误打在Log中，然后返回
     * @param name  属性名
     * @return 属性值或null
     * @see System String
     *
     * @see System String
     */
    private operator fun get(name: String): String {
        var value: String? = null
        try {
            value = System.getProperty(name)
        } catch (e: SecurityException) {
            error("Security level limit", e)
        }
        if (null == value) {
            try {
                value = System.getenv(name)
            } catch (e: SecurityException) {
                error("Security level limit", e)
            }
        }
        return value!!
    }

}