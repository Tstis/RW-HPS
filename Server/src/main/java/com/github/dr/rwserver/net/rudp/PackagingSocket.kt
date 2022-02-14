/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.rudp

import com.github.dr.rwserver.net.core.TypeConnect
import net.udp.ReliableServerSocket
import net.udp.ReliableSocketInputStream
import java.io.OutputStream

/**
 * Wrap Socket and add custom data part like Netty
 * @property socket ReliableClientSocket
 * @property remoteSocketAddressString String
 * @property outputStream OutputStream
 * @property inputStream ReliableSocketInputStream
 * @property localPort Int
 * @property isClosed Boolean
 * @property type TypeConnect?
 * @constructor
 * @author Dr
 */
internal class PackagingSocket(private val socket: ReliableServerSocket.ReliableClientSocket) {
    val remoteSocketAddressString = socket.remoteSocketAddress.toString()
    val outputStream: OutputStream = socket.outputStream
    val inputStream: ReliableSocketInputStream = socket.inputStream
    val localPort = socket.localPort

    val isClosed = socket.isClosed

    var type: TypeConnect? = null

    fun close() {
        socket.close()
    }
}