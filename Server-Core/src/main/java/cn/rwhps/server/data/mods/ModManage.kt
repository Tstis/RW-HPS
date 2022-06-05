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
import cn.rwhps.server.mods.ModsIniData
import cn.rwhps.server.mods.ModsLoad
import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.file.FileName
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.log.Log

/**
 * Mods 加载管理器
 */
internal object ModManage {
    private const val coreName = "core_RW-HPS_units_159.zip"
    private val modsData = OrderedMap<String,ObjectMap<String, ModsIniData>>()
    private var loadUnitsCount = 0
    private var fileMods: FileUtil? = null

    fun load(fileUtil: FileUtil): Int {
        loadCore()
        this.fileMods = fileUtil
        var loadCount = 0
        fileMods!!.fileList.each {
            // 只读取 RWMOD 和 ZIP
            if (!it.name.endsWith(".rwmod") && !it.name.endsWith(".zip")) {
                return@each
            }

            loadCount++
            val modsDataCache = ModsLoad(it).load()

            modsData.put(FileName.getFileName(it.name), modsDataCache)
        }
        return loadCount
    }

    private fun loadCore() {
        val modsDataCache = ModsLoad(Main::class.java.getResourceAsStream("/$coreName")!!).load()
        modsData.put(coreName, modsDataCache)
    }

    fun loadUnits() {
        modsData.values().forEach {
            loadUnitsCount += it.size
        }

        val stream: GameOutputStream = Data.utilData
        stream.reset()
        stream.writeInt(1)
        stream.writeInt(loadUnitsCount)

        modsData.forEach {
            val modName = it.key
            val modData = it.value

            val core = (modName == coreName)

            try {
                modData.forEach { iniData ->

                    stream.writeString(iniData.key)
                    stream.writeInt(iniData.value.getMd5())
                    stream.writeBoolean(true)
                    if (core) {
                        stream.writeBoolean(false)
                    } else {
                        stream.writeBoolean(true)
                        stream.writeString(modName!!)
                    }
                    stream.writeLong(0)
                    stream.writeLong(0)
                }
                Log.debug("Load OK", if (core) "Core Units" else modName!!)
            } catch (e: Exception) {
                Log.error(e)
            }
        }
    }

    fun test() {
        val ls = FileUtil.getFile("Units.txt").readFileListStringData()

        val stream: GameOutputStream = Data.utilData
        stream.reset()
        stream.writeInt(1)
        stream.writeInt(ls.size())

        ls.each {
            val a = it.split("%#%")
            stream.writeString(a[0])
            stream.writeInt(a[1].toInt())
            stream.writeBoolean(true)
            if (a.size < 3) {
                stream.writeBoolean(false)
            } else {
                stream.writeBoolean(true)
                stream.writeString(a[2])
            }
            stream.writeLong(0)
            stream.writeLong(0)
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