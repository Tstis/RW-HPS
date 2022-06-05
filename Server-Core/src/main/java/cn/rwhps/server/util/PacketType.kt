/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

/**
 * The tag corresponding to the protocol number of the server
 *
 * From Game-Ilb.jar and Rukkit
 *
 * @author [RukkitDev](https://github.com/RukkitDev)
 * @author RW-HPS/Dr
 */
enum class PacketType(val typeInt: Int) {
    /* DEBUG */
    SERVER_DEBUG_RECEIVE(2000),
    SERVER_DEBUG(2001),

    /* Preregister */
    PREREGISTER_INFO_RECEIVE(160),
    PREREGISTER_INFO(161),
    PASSWD_ERROR(113),
    REGISTER_PLAYER(110),

    /* Server Info */
    SERVER_INFO(106),
    TEAM_LIST(115),

    /* Heart */
    HEART_BEAT(108),
    HEART_BEAT_RESPONSE(109),

    /* Chat */
    CHAT_RECEIVE(140),
    CHAT(141),


    KICK(150),
    DISCONNECT(111),


    START_GAME(120),
    ACCEPT_START_GAME(112),

    /* GameStart Commands */
    TICK(10),
    GAMECOMMAND_RECEIVE(20),
    SYNCCHECKSUM_STATUS(31),
    _30(30),
    SYNC(35),

    /* Relay */
    RELAY_117(117),
    RELAY_118_117_RETURN(118),
    RELAY_151(151),
    RELAY_152_151_RETURN(152),

    RELAY_VERSION_INFO(163),
    FORWARD_HOST_SET(170),
    FORWARD_CLIENT_ADD(172),
    FORWARD_CLIENT_REMOVE(173),
    PACKET_FORWARD_CLIENT_FROM(174),
    PACKET_FORWARD_CLIENT_TO(175),
    PACKET_FORWARD_CLIENT_TO_REPEATED(176),
    PACKET_RECONNECT_TO(178),


    EMPTYP_ACKAGE(0),
    NOT_RESOLVED(-1);

    companion object {
        fun from(type: Int?): PacketType = values().find { it.typeInt == type } ?: NOT_RESOLVED
    }
}