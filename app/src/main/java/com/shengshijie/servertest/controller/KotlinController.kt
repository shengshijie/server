package com.shengshijie.servertest.controller

import com.shengshijie.server.http.annotation.Controller
import com.shengshijie.server.http.annotation.RequestBody
import com.shengshijie.server.http.annotation.RequestMapping
import com.shengshijie.server.http.annotation.RequestParam
import com.shengshijie.server.http.response.ByteArrayResponse
import com.shengshijie.server.http.response.SerializedResponse
import com.shengshijie.servertest.App
import com.shengshijie.servertest.Person
import com.shengshijie.servertest.loadAssets
import com.shengshijie.servertest.requset.TestRequest

@Controller
@RequestMapping(value = "/kotlin")
class KotlinController {
    @RequestMapping(value = "/post1", method = "POST")
    fun post1(@RequestParam(value = "amount", required = false, defaultValue = "12") amount: Double, age: Int): Any {
        return "$amount|$age"
    }

    @RequestMapping(value = "/post2", method = "POST")
    fun post2(@RequestBody testRequest: TestRequest): Any {
        return SerializedResponse(testRequest)
    }

    @RequestMapping(value = "/get1", method = "GET")
    fun get1(amount: Int): Any {
        return SerializedResponse(Person(null, "34"))
    }

    @RequestMapping(value = "/get2", method = "GET")
    fun get2(): Any {
        val content = loadAssets(App.instance, "index.html")
        return ByteArrayResponse("text/html", content)
    }

    @RequestMapping(value = "/favicon.ico", method = "GET")
    fun favicon(): Any {
        val content = loadAssets(App.instance, "favicon.ico")
        return ByteArrayResponse("text/html", content)
    }

}