/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.`null`

import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.core.server.AbstractNetConnect

internal class NullTypeConnect : TypeConnect {
    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return NullTypeConnect()
    }

    override fun typeConnect(packet: Packet) {
        TODO("Not yet implemented")
    }

    override val abstractNetConnect: AbstractNetConnect
        get() = TODO("Not yet implemented")

    override val version: String
        get() = "Null TypeConnect"
}