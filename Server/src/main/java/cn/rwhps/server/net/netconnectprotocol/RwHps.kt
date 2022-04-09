/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol

import cn.rwhps.server.net.core.AbstractNetPacket
import cn.rwhps.server.net.core.IRwHps
import cn.rwhps.server.net.core.ServiceLoader
import cn.rwhps.server.net.core.ServiceLoader.ServiceType
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.netconnectprotocol.`null`.NullNetPacket
import cn.rwhps.server.net.netconnectprotocol.`null`.NullTypeConnect
import cn.rwhps.server.util.log.Log

class RwHps(private val netType: IRwHps.NetType) : IRwHps {
    override val typeConnect: TypeConnect =
        try {
            val protocolClass = ServiceLoader.getServiceClass(ServiceType.Protocol,netType.name)
            ServiceLoader.getService(ServiceType.ProtocolType,netType.name,Class::class.java).newInstance(protocolClass) as TypeConnect
        } catch (e: Exception) {
            Log.fatal(e)
            NullTypeConnect()
        }

    override val abstractNetPacket: AbstractNetPacket =
        try {
            ServiceLoader.getService(ServiceType.ProtocolPacket,"ALLProtocol").newInstance() as AbstractNetPacket
        } catch (e: Exception) {
            Log.fatal(e)
            NullNetPacket()
        }

}