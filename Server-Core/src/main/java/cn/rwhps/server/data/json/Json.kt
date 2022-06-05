/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.json

import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.serialization.JSONSerializer
import cn.rwhps.server.util.serialization.JsonFormatTool
import com.google.gson.Gson

//Json
//写的越久，BUG越多，伤痕越疼，脾气越差/-活得越久 故事越多 伤痕越疼，脾气越差
/**
 * @author RW-HPS/Dr
 */
class Json {
    private val jsonObject: Any

    constructor(json: String) {
        jsonObject = JSONSerializer.deserialize(json)
    }

    constructor(json: LinkedHashMap<*, *>) {
        jsonObject = json
    }

    fun getData(str: String): String {
        return (jsonObject as LinkedHashMap<*, *>)[str] as String
    }

    fun getDataNull(str: String): String? {
        return (jsonObject as LinkedHashMap<*, *>)[str] as String?
    }

    fun getInnerMap(): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return jsonObject as LinkedHashMap<String, Any>
    }

    fun getArrayData(str: String): Json? {
        val rArray = (jsonObject as LinkedHashMap<*, *>)[str] as ArrayList<*>
        for (o in rArray) {
            val r = o as LinkedHashMap<*, *>
            if (IsUtil.notIsBlank(r)) {
                return Json(r)
            }
        }
        return null
    }

    fun getArraySeqData(str: String): Seq<Json> {
        val result = Seq<Json>()
        val rArray = (jsonObject as LinkedHashMap<*, *>)[str] as ArrayList<*>
        for (o in rArray) {
            val r = o as LinkedHashMap<*, *>
            if (IsUtil.notIsBlank(r)) {
                result.add(Json(r))
            }
        }
        return result
    }

    fun getArraySeqData(): Seq<Json> {
        val result = Seq<Json>()
        val rArray = (jsonObject as LinkedHashMap<*, *>)["result"] as ArrayList<*>
        for (o in rArray) {
            val r = o as LinkedHashMap<*, *>
            if (IsUtil.notIsBlank(r)) {
                result.add(Json(r))
            }
        }
        return result
    }

    companion object {
        /**
         * Map to JSON formatted
         * @param map Map<String, Any>
         * @return Json
         */
        @JvmStatic
        fun toJson(map: Map<String, Any>): String {
            return JsonFormatTool.formatJson(JSONSerializer.serialize(map))
        }

        /**
         * Map to JSON formatted
         * @param map Map<String, Any>
         * @return Json
         */
        @JvmStatic
        fun toJson(map: ObjectMap<String, Any>): String {
            return JsonFormatTool.formatJson(JSONSerializer.serialize(map))
        }

        @JvmStatic
        fun <T> stringToClass(fileUtil: FileUtil,clazz: Class<T>): Any {
            val gson = Gson()
            val json = fileUtil.readFileStringData()
            return gson.fromJson(if (IsUtil.notIsBlank(json)) json else "{}", clazz.javaClass)
        }

    }
}