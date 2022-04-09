/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.mods

import cn.rwhps.server.Main
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.mods.ModsLoad
import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.file.FileName
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.io.IoRead
import cn.rwhps.server.util.log.Log

object ModManage {
    private val coreName = "core_RW-HPS_units_114.zip"
    private val modsData = ObjectMap<String,ObjectMap<String,Int>>()
    private var loadUnitsCount = 0
    private var fileMods: FileUtil? = null

    fun load(fileUtil: FileUtil): Int {
        this.fileMods = fileUtil
        var loadCount = -1
        fileMods!!.fileList.each {
            loadCount++
            val modsDataCache = ModsLoad(it).load()
            modsData.put(FileName.getFileName(it.name), modsDataCache)
            loadUnitsCount += modsDataCache.size
        }
        return loadCount
    }

    fun loadCore() {
        if (!FileUtil.getFolder(Data.Plugin_Mods_Path).toFile(coreName).exists()) {
            FileUtil.getFolder(Data.Plugin_Mods_Path).toFile(coreName).writeFileByte(IoRead.readInputStreamBytes(Main::class.java.getResourceAsStream("/$coreName")!!),false)
        }
    }

    fun loadUnits() {
        val stream: GameOutputStream = Data.utilData
        stream.reset()
        stream.writeInt(1)
        stream.writeInt(loadUnitsCount)

        modsData.forEach {
            val modName = it.key
            val modData = it.value

            val core = (modName == coreName)

            try {
                modData.forEach {
                    stream.writeString(it.key)
                    stream.writeInt(it.value)
                    stream.writeBoolean(true)
                    if (core) {
                        stream.writeBoolean(false)
                    } else {
                        stream.writeBoolean(true)
                        stream.writeString(modName)
                    }
                    stream.writeLong(0)
                    stream.writeLong(0)
                }
                Log.debug("Load OK",modName)
            } catch (e: Exception) {
                Log.error(e)
            }
        }
    }

    fun reLoadMods(): Int {
        modsData.clear()
        loadUnitsCount = 0

        val loadCount = load(fileMods!!)
        loadUnits()
        return loadCount
    }

    fun getModsList(): Seq<String> =
        modsData.keys().toSeq()
}