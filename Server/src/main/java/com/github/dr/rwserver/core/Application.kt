/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core

import com.github.dr.rwserver.core.thread.Threads.addSavePool
import com.github.dr.rwserver.core.thread.Threads.runSavePool
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.plugin.PluginData
import com.github.dr.rwserver.data.plugin.PluginManage.runOnDisable
import com.github.dr.rwserver.net.Administration
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil.isBlank
import com.github.dr.rwserver.util.RandomUtil.generateStr
import com.github.dr.rwserver.util.file.FileUtil
import com.github.dr.rwserver.util.log.Log.error
import java.lang.management.ManagementFactory
import java.util.*


/**
 * @author Dr
 */
class Application {
    private val pluginData: PluginData = PluginData()

    /** 服务器唯一UUID  */
    lateinit var serverConnectUuid: String
    @JvmField
    var serverToken: String = generateStr(40)
    lateinit var unitBase64: Seq<String>
    lateinit var admin: Administration
    @JvmField
    var upServerList = false
    @JvmField
    var serverName = "RW-HPS"

    fun load() {
        pluginData.setFileUtil(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Settings.bin"))
        admin = Administration(pluginData)

        Initialization.startInit(pluginData)

        serverName = Data.config.ServerName

        serverConnectUuid = pluginData.getData("serverConnectUuid") { UUID.randomUUID().toString() }
        unitBase64 = pluginData.getData("unitBase64") {Seq()}
        addSavePool {
            pluginData.setData("serverConnectUuid", serverConnectUuid)
            pluginData.setData("unitBase64", unitBase64)
        }
    }

    fun save() {
        // 先执行自己的保存
        runSavePool()
        // 保存自己
        pluginData.save()
        // 保存Plugin
        runOnDisable()
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