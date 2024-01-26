package com.bast.quinn.hahelper.web

import com.bast.quinn.hahelper.ClusterMonitor
import com.bast.quinn.hahelper.HaHelperServerConfig
import com.bast.quinn.hahelper.models.ClientStatus
import com.bast.quinn.hahelper.models.ClusterStateResponse
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class HaHelperWebMain() {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelperWebMain::class.java)
    }

    fun start() {
        val serverConfig = HaHelperServerConfig.load()
        val clusterMonitor = ClusterMonitor(serverConfig)

        embeddedServer(Netty, port = 9000) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(CallLogging)

            routing {
                singlePageApplication {
                    useResources = true
                    vue(".")
                }
                get("/state") {
                    val status = clusterMonitor.getClusterStatus()
                    call.respond(status)
                }
            }
        }.start(wait = true)
    }

}

fun main(args: Array<String>) = HaHelperWebMain().start()