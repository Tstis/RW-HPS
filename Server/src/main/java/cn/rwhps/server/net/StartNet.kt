/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.net.code.rudp.PacketDecoderTest
import cn.rwhps.server.net.core.AbstractNet
import cn.rwhps.server.net.handler.rudp.StartGameNetUdp
import cn.rwhps.server.net.handler.tcp.StartGameNetTcp
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.Log.clog
import cn.rwhps.server.util.log.Log.error
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import java.net.BindException
import java.net.ServerSocket

/**
 * @author RW-HPS/Dr
 * 遵守NetGameServer服务对外最少开放接口,尽量内部整合
 */
class StartNet {
    private val connectChannel = Seq<Channel>(4)
    private var serverSocket: ServerSocket? = null
    private var startGameNetUdp: StartGameNetUdp? = null
    private val start: AbstractNet

    internal val ioGroup: EventExecutorGroup = DefaultEventExecutorGroup(32)

    constructor() {
        start = StartGameNetTcp(this)
        //start = StartGamePortDivider(this)
    }

    constructor(abstractNetClass: Class<out AbstractNet>) {
        start =
            try {
                ReflectionUtils.accessibleConstructor(abstractNetClass,StartNet::class.java).newInstance(this) as AbstractNet
            } catch (e: NoSuchMethodException) {
                Log.fatal("[StartNet Load Error] Use default implementation",e)
                StartGameNetTcp(this)
            }
    }

    /**
     * 在指定端口启动Game Server
     * @param port 端口
     */
    fun openPort(port: Int) {
        Data.config.RunPid = Data.core.pid
        Data.config.save()

        openPort(port,1,0)
    }

    /**
     * 在指定端口范围启动Game Server
     * @param port 主端口
     * @param startPort Start Port
     * @param endPort End Port
     */
    fun openPort(port: Int,startPort:Int,endPort:Int) {
        clog(Data.i18NBundle.getinput("server.start.open"))
        val bossGroup: EventLoopGroup = getEventLoopGroup(4)
        val workerGroup: EventLoopGroup = getEventLoopGroup()
        val runClass: Class<out ServerChannel>

        if (Epoll.isAvailable()) {
            runClass = EpollServerSocketChannel::class.java
        } else {
            runClass = NioServerSocketChannel::class.java
            clog("无法使用Epoll 效率可能略低")
        }
        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup)
                .channel(runClass)
                //.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(start)
            clog(Data.i18NBundle.getinput("server.start.openPort"))
            val channelFutureTcp = serverBootstrapTcp.bind(port)

            for (i in startPort..endPort) {
                serverBootstrapTcp.bind(i)
            }

            val start = channelFutureTcp.channel()
            connectChannel.add(start)
            clog(Data.i18NBundle.getinput("server.start.end"))
            start.closeFuture().sync()
        } catch (e: InterruptedException) {
            error("[TCP Start Error]", e)
        } catch (bindError: BindException) {
            error("[Port Bind Error]", bindError)
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }

        error("END")
    }

    fun startUdp(port: Int) {
        val group = NioEventLoopGroup()
        val bootstrap = Bootstrap()
        bootstrap.group(group)
            .channel(NioDatagramChannel::class.java)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(object : ChannelInitializer<NioDatagramChannel?>() {
                @Throws(Exception::class)
                override fun initChannel(nioDatagramChannel: NioDatagramChannel?) {
                    if (nioDatagramChannel == null) {
                        return
                    }
                    nioDatagramChannel.pipeline().addLast(PacketDecoderTest())
                }
            })
        try {
            //4.bind到指定端口，并返回一个channel，该端口就是监听UDP报文的端口
            val channel: Channel = bootstrap.bind(5123).sync().channel()
            //5.等待channel的close
            channel.closeFuture().sync()
            //6.关闭group
            group.shutdownGracefully()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun getConnectSize(): Int {
        return start.getConnectSize()
    }

    fun stop() {
        connectChannel.each { obj: Channel -> obj.close() }
    }

    private fun getEventLoopGroup(size: Int = 0): EventLoopGroup {
        return if (Epoll.isAvailable()) {
            EpollEventLoopGroup(size)
        } else {
            NioEventLoopGroup(size)
        }
    }
}