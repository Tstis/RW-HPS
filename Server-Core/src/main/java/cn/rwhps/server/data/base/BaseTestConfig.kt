/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.base

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.file.FileUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder


/**
 * 测试功能
 * @author RW-HPS/Dr
 */
data class BaseTestConfig(
    val NoData: Boolean = false
) {
    fun save() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        fileUtil.writeFile(gson.toJson(this))
    }

    companion object {
        private val fileUtil = FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Test.json")

        fun stringToClass(): BaseTestConfig {
            val gson = Gson()
            val json = fileUtil.readFileStringData()

            return gson.fromJson(if (IsUtil.notIsBlank(json)) json else "{}", BaseTestConfig::class.java)
        }
    }
}