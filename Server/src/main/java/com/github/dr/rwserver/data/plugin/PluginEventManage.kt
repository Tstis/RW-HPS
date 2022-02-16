/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.data.plugin

import com.github.dr.rwserver.game.EventGlobalType.ServerLoadEvent
import com.github.dr.rwserver.game.EventType.*
import com.github.dr.rwserver.plugin.event.AbstractEvent
import com.github.dr.rwserver.plugin.event.AbstractGlobalEvent
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.threads.GetNewThreadPool

internal class PluginEventManage {
    companion object {
        private val pluginEventData = Seq<AbstractEvent>(8)
        private val pluginGlobalEventData = Seq<AbstractGlobalEvent>(8)
        private val executorService = GetNewThreadPool.getNewFixedThreadPool(5, "PluginEventASync-")
        @JvmStatic
        fun add(abstractEvent: AbstractEvent) {
            pluginEventData.add(abstractEvent)
        }
        @JvmStatic
        fun add(abstractGlobalEvent: AbstractGlobalEvent) {
            pluginGlobalEventData.add(abstractGlobalEvent)
        }

        private fun registerEventAll() {
            /* Sync */
            Events.on(PlayerJoinEvent::class.java) { e: PlayerJoinEvent ->
                pluginEventData.each { p: AbstractEvent ->
                    p.registerPlayerJoinEvent(
                        e.player
                    )
                }
            }
            /* Sync */
            Events.on(PlayerReJoinEvent::class.java) { e: PlayerReJoinEvent ->
                pluginEventData.each { p: AbstractEvent ->
                    p.registerPlayerReJoinEvent(
                        e.player
                    )
                }
            }
            /* Sync */
            Events.on(PlayerConnectPasswdCheckEvent::class.java) { e: PlayerConnectPasswdCheckEvent ->
                pluginEventData.each { p: AbstractEvent ->
                    val strings = p.registerPlayerConnectPasswdCheckEvent(e.abstractNetConnect, e.passwd)
                    e.result = strings[0].toBoolean()
                    e.name = strings[1]
                }
            }

            /* ASync */
            Events.on(PlayerConnectEvent::class.java) { e: PlayerConnectEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerConnectEvent(
                            e.player
                        )
                    }
                }
            }
            /* ASync */
            Events.on(PlayerLeaveEvent::class.java) { e: PlayerLeaveEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerLeaveEvent(
                            e.player
                        )
                    }
                }
            }
            /* ASync */
            Events.on(PlayerChatEvent::class.java) { e: PlayerChatEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerChatEvent(
                            e.player,
                            e.message
                        )
                    }
                }
            }
            /* ASync */
            Events.on(GameStartEvent::class.java) { _: GameStartEvent? ->
                executorService.execute {
                    pluginEventData.each { obj: AbstractEvent ->
                        obj.registerGameStartEvent()
                    }
                }
            }
            /* ASync */
            Events.on(GameOverEvent::class.java) { _: GameOverEvent? ->
                executorService.execute {
                    pluginEventData.each { obj: AbstractEvent ->
                        obj.registerGameOverEvent()
                    }
                }
            }
            /* ASync */
            Events.on(PlayerBanEvent::class.java) { e: PlayerBanEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerBanEvent(
                            e.player
                        )
                    }
                }
            }
            /* ASync */
            Events.on(PlayerUnbanEvent::class.java) { e: PlayerUnbanEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerUnbanEvent(
                            e.player
                        )
                    }
                }
            }
            /* ASync */
            Events.on(PlayerIpBanEvent::class.java) { e: PlayerIpBanEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerIpBanEvent(
                            e.player
                        )
                    }
                }
            }
            /* ASync */
            Events.on(PlayerIpUnbanEvent::class.java) { e: PlayerIpUnbanEvent ->
                executorService.execute {
                    pluginEventData.each { p: AbstractEvent ->
                        p.registerPlayerIpUnbanEvent(
                            e.ip
                        )
                    }
                }
            }
        }

        private fun registerGlobalEventAll() {
            /* ASync */
            Events.on(ServerLoadEvent::class.java) { _: ServerLoadEvent ->
                executorService.execute {
                    pluginGlobalEventData.each { obj: AbstractGlobalEvent ->
                        obj.registerServerLoadEvent()
                    }
                }
            }
        }
    }

    init {
        registerEventAll()
    }
}