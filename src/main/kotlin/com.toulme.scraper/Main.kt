package com.toulme.scraper

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.infinispan.embedded.InfinispanEmbeddedConfiguration
import org.apache.camel.component.infinispan.embedded.InfinispanEmbeddedIdempotentRepository
import org.apache.camel.impl.DefaultCamelContext
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.configuration.global.GlobalConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch

val latch = CountDownLatch(1)

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("[USAGE] api-scraper config-file")
        System.exit(1)
    }

    val config = Config.fromFile(args[0])

    val path = Paths.get(config.persistence())

    val cacheConfig = ConfigurationBuilder().persistence().addStore(RocksDBStoreConfigurationBuilder::class.java)
        .location(path.resolve("idempotent").toAbsolutePath().toString())
        .expiredLocation(path.resolve("idempotent-expired").toAbsolutePath().toString()).build()
    val infinispanConfig = InfinispanEmbeddedConfiguration()
    val cacheManager = DefaultCacheManager(GlobalConfigurationBuilder().defaultCacheName("api-scraper").build(), cacheConfig, true)
    infinispanConfig.cacheContainer = cacheManager

    val repo = InfinispanEmbeddedIdempotentRepository("atom")
    repo.configuration = infinispanConfig

    val context: CamelContext = DefaultCamelContext()
    context.addRoutes(object : RouteBuilder() {
        override fun configure() {
            for (feed in config.feeds()) {
                from(String.format(config.atomURIFormat(), feed))
                    .convertBodyTo(String::class.java)
                    .idempotentConsumer(simple("\${body.id.toASCIIString}"), repo)
                    .to(
                        String.format(
                            "splunk-hec:%s/%s?skipTlsVerify=%s&index=%s&source=%s&sourcetype=%s&bodyOnly=true",
                            config.host(),
                            config.token(),
                            if (config.skipTlsVerify()) "true" else "false",
                            config.index(),
                            config.source(),
                            config.sourcetype()
                        )
                    ).end()
            }
        }
    })

    Runtime.getRuntime().addShutdownHook(Thread {
        context.stop()
        cacheManager.stop()
        latch.countDown()
    })

    context.start()

    latch.await()
}