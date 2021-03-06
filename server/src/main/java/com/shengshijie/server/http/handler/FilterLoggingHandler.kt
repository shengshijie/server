package com.shengshijie.server.http.handler

import com.shengshijie.server.ServerManager
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import java.net.SocketAddress

internal class FilterLoggingHandler : LoggingHandler(ServerManager.mServerConfig.logLevel.toNettyLogLevel()) {

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelRegistered")
        ctx.fireChannelRegistered()
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelUnregistered")
        ctx.fireChannelUnregistered()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelActive")
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelInactive")
        ctx.fireChannelInactive()
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        ServerManager.mLogManager.d("userEventTriggered")
        ctx.fireUserEventTriggered(evt)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val response = msg as? FullHttpResponse
        response?.let {
            val space = "           "
            val log = StringBuilder()
            log.appendln()
            log.appendln("<RESPONSE> [ ${response.protocolVersion()} ] ${ctx.channel()}")
            log.appendln("${space}${response.headers().joinToString("\n${space}")}")
            log.appendln()
            log.appendln("${space}${response.content().toString(CharsetUtil.UTF_8)}")
            ServerManager.mLogManager.i(log.toString())
        }
        ctx.write(msg, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = msg as? FullHttpRequest
        request?.let {
            val space = "           "
            val log = StringBuilder()
            log.appendln()
            log.appendln("<REQUEST> [ ${request.method()} ${request.uri()} ${request.protocolVersion()} ] ${ctx.channel()}")
            log.appendln("${space}${request.headers().joinToString("\n${space}")}")
            log.appendln()
            log.appendln("${space}${request.content().toString(CharsetUtil.UTF_8)}")
            ServerManager.mLogManager.i(log.toString())
        }
        ctx.fireChannelRead(msg)
    }

    override fun bind(ctx: ChannelHandlerContext, localAddress: SocketAddress, promise: ChannelPromise) {
        ServerManager.mLogManager.d("bind localAddress: $localAddress")
        ctx.bind(localAddress, promise)
    }

    override fun connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise) {
        ServerManager.mLogManager.d("bind remoteAddress: $remoteAddress + localAddress: $localAddress ")
        ctx.connect(remoteAddress, localAddress, promise)
    }

    override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        ServerManager.mLogManager.d("disconnect")
        ctx.disconnect(promise)
    }

    override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        ServerManager.mLogManager.d("close")
        ctx.close(promise)
    }

    override fun deregister(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        ServerManager.mLogManager.d("deregister")
        ctx.deregister(promise)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelReadComplete")
        ctx.fireChannelReadComplete()
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("channelWritabilityChanged")
        ctx.fireChannelWritabilityChanged()
    }

    override fun flush(ctx: ChannelHandlerContext) {
        ServerManager.mLogManager.d("flush")
        ctx.flush()
    }
}