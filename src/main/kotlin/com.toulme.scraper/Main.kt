package com.toulme.scraper

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import java.util.concurrent.CountDownLatch

val latch = CountDownLatch(1)

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("[USAGE] api-scraper config-file")
        System.exit(1)
    }

    val config = Config.fromFile(args[0])
    val context: CamelContext = DefaultCamelContext()
    context.addRoutes(object : RouteBuilder() {
        override fun configure() {
            for (feed in config.feeds()) {
                from(String.format("atom:%s?splitEntries=true", feed))
                        .convertBodyTo(String::class.java)
                        .to(String.format("splunk-hec:%s/%s?skipTlsVerify=%s&index=%s&source=%s&sourcetype=%s&bodyOnly=true",
                                config.host(), config.token(), if (config.skipTlsVerify()) "true" else "false", config.index(), config.source(), config.sourcetype())).end()
            }
        }
    })

    Runtime.getRuntime().addShutdownHook(Thread {
        context.stop()
        latch.countDown()
    })

    context.start()

    latch.await()
}