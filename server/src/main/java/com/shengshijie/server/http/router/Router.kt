package com.shengshijie.server.http.router

import com.shengshijie.server.ServerManager
import com.shengshijie.server.common.Pair
import com.shengshijie.server.http.request.IHttpRequest
import com.shengshijie.server.http.response.IHttpResponse
import com.shengshijie.server.http.exception.BusinessException
import com.shengshijie.server.http.exception.RequestException
import com.shengshijie.server.http.exception.ServerException
import com.shengshijie.server.http.filter.Filter
import com.shengshijie.server.http.response.RawResponse
import com.shengshijie.server.http.utils.ExceptionUtil
import com.shengshijie.server.http.utils.HttpResponseUtil
import java.lang.reflect.InvocationTargetException

internal class Router(var invoker: Invoker) {

    private val filters = mutableListOf<Filter>()

    fun addFilters(filters: List<Filter>) {
        this.filters.addAll(filters)
    }

    fun call(request: IHttpRequest, response: IHttpResponse) {
        val allArgs = arrayListOf<Any>()
        val missingArgs = arrayListOf<String?>()
        try {
            for (filter in filters) {
                if (filter.preFilter(request, response)) {
                    return
                }
            }
            for (i in 1 until invoker.args.size) {
                request.getParamMap()[invoker.args[i].name]?.apply { allArgs.add(this) }
                        ?: missingArgs.add(invoker.args[i].name)
            }
            if (missingArgs.isNotEmpty()) {
                throw RequestException("missing parameter: [${missingArgs.joinToString()}]")
            }
            when (val obj = invoker.method.call(invoker.instance, *(allArgs.toArray()))) {
                is Pair<*, *> -> {
                    HttpResponseUtil.writeOKResponse(response, obj.second, obj.first.toString())
                }
                is RawResponse -> {
                    HttpResponseUtil.writeRawResponse(response, obj.content)
                }
                is BusinessException -> {
                    HttpResponseUtil.writeFailResponse(response, obj.code, "${obj.message}")
                }
                else -> {
                    HttpResponseUtil.writeOKResponse(response, obj)
                }
            }
        } catch (exception: Exception) {
            when (exception) {
                is BusinessException -> {
                    throw exception
                }
                is RequestException -> {
                    throw exception
                }
                is InvocationTargetException -> {
                    throw BusinessException(exception.targetException.message
                            ?: "", (exception.targetException as? BusinessException)?.code
                            ?: ServerManager.mServerConfig.errorCode)
                }
                else -> {
                    throw ServerException("server error: [${ExceptionUtil.toString(exception)}]")
                }
            }
        } finally {
            for (filter in filters) {
                filter.postFilter(request, response)
            }
        }
    }

}