package com.toulme.scraper

import org.apache.tuweni.config.Configuration
import org.apache.tuweni.config.PropertyValidator
import org.apache.tuweni.config.Schema
import org.apache.tuweni.config.SchemaBuilder
import java.io.FileNotFoundException
import java.nio.file.Paths

class Config(private var config: Configuration = Configuration.empty(createSchema())) {

    companion object {
        fun createSchema(): Schema {
            val hecSection = SchemaBuilder.create()
            hecSection.addString("host", null, "Splunk HEC host", PropertyValidator.isPresent())
            hecSection.addString("token", null, "Splunk HEC token", PropertyValidator.isPresent())
            hecSection.addString("index", null, "Splunk Index", null)
            hecSection.addString("source", null, "Splunk Source", null)
            hecSection.addString("sourcetype", null, "Splunk Sourcetype", null)
            hecSection.addBoolean("skipTlsVerify", false, "Skip TLS verification", null)

            val builder = SchemaBuilder.create()
            builder.addSection("hec", hecSection.toSchema())
            builder.addListOfString("atom", listOf<String>(), "List of Atom feeds", null)
            return builder.toSchema()
        }
        fun fromFile(path : String): Config {
            try {
                return Config(Configuration.fromToml(Paths.get(path), createSchema()))
            } catch (e: Exception) {
                when (e) {
                    is NoSuchFileException, is FileNotFoundException -> {
                        throw IllegalArgumentException("Missing config file: '$path'")
                    }
                    else -> throw e
                }
            }
        }
    }

    fun host() = config.getString("hec.host")

    fun token() = config.getString("hec.token")

    fun index() = config.getString("hec.index")

    fun source() = config.getString("hec.source")

    fun sourcetype() = config.getString("hec.sourcetype")

    fun skipTlsVerify() = config.getBoolean("hec.skipTlsVerify")

    fun feeds(): List<String> = config.getListOfString("atom")
}