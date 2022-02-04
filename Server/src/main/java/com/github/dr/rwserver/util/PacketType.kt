/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

/**
 * The tag corresponding to the protocol number of the server
 *
 * From Game-Ilb.jar and Rukkit
 *
 * @author [RukkitDev](https://github.com/RukkitDev)
 * @author Dr
 */
object PacketType {
    const val PACKET_SERVER_DEBUG = 2000

    //Server Commands
    const val PACKET_REGISTER_CONNECTION = 161
    const val PACKET_TEAM_LIST = 115
    const val PACKET_HEART_BEAT = 108
    const val PACKET_SEND_CHAT = 141
    const val PACKET_SERVER_INFO = 106
    const val PACKET_KICK = 150
    const val PACKET_SYNCCHECKSUM_STATUS = 31
    const val PACKET_A = 30

    //Client Commands
    const val PACKET_PREREGISTER_CONNECTION = 160
    const val PACKET_HEART_BEAT_RESPONSE = 109
    const val PACKET_ADD_CHAT = 140
    const val PACKET_PLAYER_INFO = 110
    const val PACKET_DISCONNECT = 111
    const val PACKET_ACCEPT_START_GAME = 112
    const val PACKET_ACCEPT_BUTTON_GAME = 20

    //Game Commands
    const val PACKET_ADD_GAMECOMMAND = 20
    const val PACKET_TICK = 10
    const val PACKET_SYNC = 35
    const val PACKET_START_GAME = 120
    const val PACKET_PASSWD_ERROR = 113

    //Relay Commands


}