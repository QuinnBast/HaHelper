package com.bast.quinn.hahelper

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import org.slf4j.LoggerFactory
import java.io.File

data class HaHelperServerConfig(
    val serverPort: Int,
    val cluster: ClusterConfig,
    val persistence: PersistenceConfig,
) {

    companion object {

        private const val ENVIRONMENT_VARIABLE_NAME: String = "HA_HELPER_CONFIG_PATH"
        private const val DEFAULT_CONFIG_FILENAME: String = "/ha_helper_config.yaml"
        private const val DEFAULT_CONFIG_PATH: String = "./config/$DEFAULT_CONFIG_FILENAME"
        private val logger = LoggerFactory.getLogger(HaHelperServerConfig::class.java)

        fun load(absolutePath: String = ".yaml") : HaHelperServerConfig {
            val path = System.getenv(ENVIRONMENT_VARIABLE_NAME) ?: ".yaml"

             val config = ConfigLoaderBuilder.default()
                 .addFileSource(File(path), optional = true)
                 .addFileSource(File(DEFAULT_CONFIG_PATH), optional = true)
                 .addFileSource(File(absolutePath), optional = true)
                 .build()
                 .loadConfigOrThrow<HaHelperServerConfig>()

            logger.info(config.toString())

            return config
        }
    }
}

data class ClusterConfig(
    val clusterId: Int,
    val multiMaster: Boolean,
    val knownHosts: List<Host>,
) {
    fun getQuorum() = kotlin.math.floor(knownHosts.size / 2.0) + 1
}

data class Host(
    val hostname: String,
    val port: Int,
)

data class PersistenceConfig(
    val enabled: Boolean,
    val replicas: Int?,
    val storageLocation: String?,
    val compactionIntervalSeconds: Long?, // 0 Means no log compaction
)