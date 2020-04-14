package com.shengshijie.httpserver

import android.util.Log
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioChannelOption
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.util.concurrent.Future

object HttpServer {
    @JvmStatic
    fun start(port: Int) {
        val bootstrap = ServerBootstrap()
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        val httpHandler = HttpHandler()
        httpHandler.registerRouter()
        bootstrap.group(bossGroup, workerGroup)
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childOption(NioChannelOption.TCP_NODELAY, true)
        bootstrap.childOption(NioChannelOption.SO_REUSEADDR, true)
        bootstrap.childOption(NioChannelOption.SO_KEEPALIVE, false)
        bootstrap.childOption(NioChannelOption.SO_RCVBUF, 2048)
        bootstrap.childOption(NioChannelOption.SO_SNDBUF, 2048)
        bootstrap.childHandler(object : ChannelInitializer<SocketChannel>() {
            public override fun initChannel(ch: SocketChannel) {
                ch.pipeline().addLast("codec", HttpServerCodec())
                ch.pipeline().addLast("aggregator", HttpObjectAggregator(512 * 1024))
                ch.pipeline().addLast("logging", FilterLoggingHandler())
                ch.pipeline().addLast("interceptor", InterceptorHandler())
                ch.pipeline().addLast("bizHandler", httpHandler)
            }
        })
        val channelFuture = bootstrap.bind(port).syncUninterruptibly().addListener { Log.i("SERVER", "Server Start") }
        channelFuture.channel().closeFuture().addListener {
            Log.i("SERVER", "Server Shutdown")
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}