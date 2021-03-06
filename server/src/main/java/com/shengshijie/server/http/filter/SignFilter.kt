package com.shengshijie.server.http.filter

import com.shengshijie.server.ServerManager
import com.shengshijie.server.http.exception.BusinessException
import com.shengshijie.server.http.request.IHttpRequest
import com.shengshijie.server.http.response.IHttpResponse
import io.netty.buffer.ByteBufUtil
import java.security.MessageDigest

internal class SignFilter : Filter {

    private val timeKey = "time"
    private val nonceKey = "nonce"
    private val signKey = "sign"
    private val expireTime = 5 * 60 * 1000L
    private val nonceList = mutableListOf<String>()

    override fun preFilter(request: IHttpRequest, response: IHttpResponse): Boolean {
        signAuthentication(request, response)
        return false
    }

    override fun postFilter(request: IHttpRequest, response: IHttpResponse) {
        ServerManager.mLogManager.i("postFilter ${request.uri()} ${response.uft8Content()}")
    }

    private fun signAuthentication(request: IHttpRequest, response: IHttpResponse) {
        ServerManager.mLogManager.i("preFilter ${request.uri()} ${response.uft8Content()}")
        val paramMap = request.getParamMap()
        val nonce = paramMap[nonceKey] ?: ""
        if (nonce.isBlank()) throw BusinessException("request parameter [$nonceKey]")
        if (nonceList.contains(nonce)) throw BusinessException("duplicate request")
        nonceList.add(nonce)
        val start = paramMap[timeKey]?.toLong() ?: throw BusinessException("request parameter [$timeKey]")
        val now = System.currentTimeMillis()
        if (now - start > expireTime || start > now) throw BusinessException("request expired")
        val sign = paramMap[signKey] ?: throw BusinessException("request parameter [$signKey]")
        paramMap.remove(signKey)
        if (sign != getParamSign(paramMap)) throw BusinessException("incorrect sign")
    }

    private fun getParamSign(map: MutableMap<String, String?>): String? {
        val keys = map.keys.toMutableList()
        keys.sortBy { it }
        val sign = StringBuilder()
        for (key in keys) {
            if (map[key] != null) {
                sign.append(key).append("=").append(map[key]).append("&")
            }
        }
        sign.deleteCharAt(sign.lastIndex)
        sign.append(ServerManager.mServerConfig.salt)
        return ByteBufUtil.hexDump(MessageDigest.getInstance("SHA-1").digest(sign.toString().toByteArray()))
    }

}




