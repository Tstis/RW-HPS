/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.encryption

import com.github.dr.rwserver.util.log.Log.fatal
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.jar.JarInputStream

/**
 * @author Dr
 */
class Autograph {
    fun verify(url: URL): Boolean {
        try {
            JarInputStream(FileInputStream(File(url.toURI())), true).use { jarIn ->
                while (jarIn.nextJarEntry != null) {
                }
                return true
            }
        } catch (e: Exception) {
            fatal(e)
        }
        return false
    }
}