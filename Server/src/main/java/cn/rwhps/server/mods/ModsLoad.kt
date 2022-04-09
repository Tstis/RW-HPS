/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.mods

import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.util.IsUtil.isBlank
import cn.rwhps.server.util.ModsIniUtil
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.zip.zip.ZipDecoder
import java.io.File
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Mods加载
 * @author Dr
 */
class ModsLoad {
    val a: Pattern = Pattern.compile("\\$\\{([^}]*)}")
    val b: Pattern = Pattern.compile("[A-Za-z_][A-Za-z_.0-9]*")

    val file: File

    constructor(fileUtil: FileUtil) {
        this.file = fileUtil.file
    }

    constructor(file: File) {
        this.file = file
    }

    fun load(): ObjectMap<String,Int> {
        val orderedMap = ZipDecoder(file).getSpecifiedSuffixInThePackageAllFileNameAndPath("ini")
        val template = ZipDecoder(file).getSpecifiedSuffixInThePackageAllFileName("template")

        val result = ObjectMap<String,Int>()

        orderedMap.forEach { k: ObjectMap.Entry<String, ByteArray> ->
            val namePath = k.key
            val bytes = k.value

            val name = namePath.split("/").toTypedArray()[namePath.split("/").toTypedArray().size - 1]
            val filePath = namePath.substring(0,namePath.length - name.length)

            val modData = ModsIniData(bytes)
            if (modData.getValue("core", "dont_load").toBoolean()) {
                return@forEach
            }

            copyFrom(modData, modData, filePath, orderedMap)

            if (template.containsKey("all-units.template")) {
                copyFromAllUnit(modData,ModsIniData(template["all-units.template"]),orderedMap)
            }

            val copyFromSection = modData.checkEachModuleValue("@copyFromSection")
            copyFromSection.each {
                copyFromSection(modData,it,it)
            }

            globalAndDefine(modData)

            result.put(modData.getName(), modData.getMd5())
        }
        return result
    }

    private fun copyFromAllUnit(beRecorded: ModsIniData, allUnit: ModsIniData, orderedMap: OrderedMap<String, ByteArray>) {
        beRecorded.addModsConfig(allUnit)
        copyFrom(beRecorded,allUnit, orderedMap = orderedMap)
    }

    private fun copyFrom(beRecorded: ModsIniData, read: ModsIniData?, pathIn: String? = null, orderedMap: OrderedMap<String, ByteArray>) {
        val str = read!!.getValue("core", "copyFrom")
        var a: ModsIniData? = null
        if (str != null) {
            val arrayOfString = str.split(",".toRegex()).toTypedArray()
            mutableListOf(*arrayOfString as Array<*>).reverse()

            for (copyFromIn in arrayOfString) {
                var copyFrom = copyFromIn.trim ()
                if (!isBlank(copyFrom)) {
                    if (copyFrom.contains("..")) {
                        continue
                    }
                    if (copyFrom.startsWith("ROOT:")) {
                        copyFrom = copyFrom.substring("ROOT:".length)
                    }

                    var path = copyFrom
                    if (pathIn != null) {
                        path = "$pathIn$copyFrom"
                    }

                    try {
                        a = ModsIniData(orderedMap[path])
                        beRecorded.addModsConfig(a)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    copyFrom(beRecorded, a, pathIn, orderedMap)
                }
            }
        }
    }

    private fun copyFromSection(beRecorded: ModsIniData, moduleName: String,key: String) {
        val copyFromSectionValue = beRecorded.getValue(key, "@copyFromSection")
        if (copyFromSectionValue.isNullOrEmpty()) {
            return
        }
        val arrayOfString: Array<String> = copyFromSectionValue.split(",").toTypedArray()
        mutableListOf(arrayOfString as Array<*>).reverse()

        for (sectionIn in arrayOfString) {
            val section = sectionIn.trim()
            if (section.isNotBlank()) {
                val sectionList = beRecorded.checkForASpecificModuleAndStartWithASpecificCharacter(section,"")

                if (sectionList.size() == 0) {
                    Log.warn("[$key] @copyFromSection: Could not find keys in target section : $section")
                }

                sectionList.each {
                    val str3 = beRecorded.getValue(section,it)

                    if (str3 != null) {
                        beRecorded.addValue(moduleName,it,str3)
                    }
                }

                copyFromSection(beRecorded,moduleName,section)
            }
        }

    }

    private fun globalAndDefine(beRecorded: ModsIniData) {
        val global = beRecorded.checkForKeysStartingWithASpecificName("@global ")
        val gblbalMap = ObjectMap<String,String>()

        global.each { moduleName ->
            beRecorded.checkForASpecificModuleAndStartWithASpecificCharacter(moduleName,"@global ").each {
                val str2: String = it.substring("@global ".length).trim()
                ModsIniUtil.nameCheck(str2)

                gblbalMap.put(str2,beRecorded.getValue(moduleName,it))
            }
        }

        beRecorded.modFileData.forEach { (moduleName: String, v: LinkedHashMap<String, String>) ->
            if (moduleName.startsWith("comment_") || moduleName.startsWith("template_")) {
                return@forEach
            }

            val defineMap = ObjectMap<String,String>()

            beRecorded.checkForASpecificModuleAndStartWithASpecificCharacter(moduleName,"@define ").each {
                val str2: String = it.substring("@define ".length).trim()
                ModsIniUtil.nameCheck(str2)

                defineMap.put(str2,beRecorded.getValue(moduleName,it))
            }

            v.forEach { (key: String, value: String) ->
                if (value.contains("\${")) {
                    var parseLoopCount: Byte = 0
                    val stringBuffer = StringBuffer()
                    val matcher: Matcher = a.matcher(value)
                    while (matcher.find()) {
                        var str4: String

                        parseLoopCount++
                        if (parseLoopCount > 100) {
                            throw RuntimeException("[$moduleName]$key: Too many loops while parsing")
                        }
                        val str3: String = matcher.group(1)

                        str4 = a(beRecorded, moduleName, str3, defineMap ,gblbalMap)

                        matcher.appendReplacement(stringBuffer, str4)

                        matcher.appendTail(stringBuffer)
                        val str2 = stringBuffer.toString()
                        beRecorded.addValue0(moduleName,key,str2)
                    }
                }
            }
        }
    }

    private fun a(beRecorded: ModsIniData, moduleName: String, paramString2: String, defineMap: ObjectMap<String,String>, gblbalMap: ObjectMap<String,String>): String {
        var result = paramString2.trim()
        val existentialComputing = ModsIniUtil.checkForInclusion(result)
        val stringBuffer = StringBuffer()
        val matcher = b.matcher(result)
        while (matcher.find()) {
            val str1 = matcher.group(0)
            if (ModsIniUtil.checkCharAt(str1)) {
                continue
            }
            if ("int" == str1 || "cos" == str1 || "sin" == str1 || "sqrt" == str1) {
                continue
            }
            val str2: String = loadVariable(beRecorded,moduleName, str1,defineMap,gblbalMap)
            if (existentialComputing) {
                if (!ModsIniUtil.checkCharAt(str2)) {
                    throw RuntimeException("Cannot do maths on '$str2' from $str1 (not a number)")
                }
            }
            matcher.appendReplacement(stringBuffer, str2)
        }
        matcher.appendTail(stringBuffer)
        result = stringBuffer.toString()

        if (existentialComputing) {
            result = ModsIniUtil.doubleToString(`b$1`(result).b())
        }
        return result
    }

    private fun loadVariable(beRecorded: ModsIniData, moduleNameIn: String, paramString2: String,defineMap: ObjectMap<String,String>, gblbalMap: ObjectMap<String,String>): String {
        if (paramString2.contains(".")) {
            val arrayOfString = ModsLoadUtil.b(paramString2, '.')
            if (arrayOfString.size != 2) {
                throw RuntimeException("Unexpected key format: $paramString2")
            }
            var moduleName = arrayOfString[0]
            val str2 = arrayOfString[1]
            if ("section" == moduleName) {
                moduleName = moduleNameIn
            }
            val str3: String = beRecorded.getValue(moduleName,str2) ?: throw RuntimeException("Could not find: [$moduleName] $str2 in this conf file")

            if (str3.contains("\${")) {
                throw RuntimeException("Reference [$moduleName]$str2 is dynamic, chaining is not yet supported")
            }
            return str3
        }
        // 先尝试区域变量
        val define: String? = defineMap.get(paramString2)
        if (define != null) {
            return define
        }
        // 再尝试找全局
        val gblbal = gblbalMap.get(paramString2)
        if (gblbal != null) {
            return gblbal
        }
        throw RuntimeException("Could not find variable with name: $paramString2")
    }
}