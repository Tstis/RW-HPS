/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.data

import com.github.dr.rwserver.net.core.AbstractNetPacket
import com.github.dr.rwserver.net.core.TypeConnect

class ProtocolData {
    @Volatile
    internal lateinit var typeConnect: TypeConnect
    @Volatile
    internal lateinit var abstractNetPacket: AbstractNetPacket

    @JvmField
    var AbstractNetPacketVersion: String = ""
    @JvmField
    var TypeConnectVersion: String = ""

    fun setNetConnectPacket(packet: AbstractNetPacket, version: String) {
        abstractNetPacket = packet
        AbstractNetPacketVersion = version
    }

    fun setTypeConnect(typeConnect: TypeConnect) {
        this.typeConnect = typeConnect
        TypeConnectVersion = typeConnect.version
    }

    protected fun update(typeConnect: TypeConnect) {
        this.typeConnect = typeConnect
    }
}