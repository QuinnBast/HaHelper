package com.bast.quinn.hahelper

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import org.slf4j.LoggerFactory
import java.io.File

data class HaHelperServerConfig(
    val local: Host,
    val cluster: ClusterConfig,
) {

    companion object {

        private const val envVarName: String = "HA_HELPER_CONFIG_PATH"
        private const val defaultConfigFileName: String = "/ha_helper_config.yaml"
        private val logger = LoggerFactory.getLogger(HaHelperServerConfig::class.java)

        fun load(absolutePath: String = ".yaml") : HaHelperServerConfig {
            val path = System.getenv(envVarName) ?: ".yaml"

             val config = ConfigLoaderBuilder.default()
                 .addFileSource(File(path), optional = true)
                 .addFileSource(File(absolutePath), optional = true)
                 .addResourceSource(defaultConfigFileName)
                 .build()
                 .loadConfigOrThrow<HaHelperServerConfig>()

            logger.info(config.toString())

            return config
        }
    }
}

data class ClusterConfig(
    val clusterSize: Int,
    val multiMaster: Boolean,
    val storage: DataStorageConfig,
    val discoveryMethod: DiscoveryMethod,
)

data class DiscoveryMethod(
    val knownHosts: List<Host>?,
    val domainDiscovery: List<String>?,
) {
    init {
        require(!knownHosts.isNullOrEmpty() || !domainDiscovery.isNullOrEmpty())
    }
}

data class Host(
    val hostname: String,
    val port: Int,
)

data class DataStorageConfig(
    val fileBasedConfig: FileStorageConfig,
    val memoryBasedConfig: InMemoryConfig,
) {
    init {
        require(fileBasedConfig.enabled || memoryBasedConfig.enabled)
    }
}

data class FileStorageConfig(
    val enabled: Boolean,
    val storageLocation: String?,
    val compression: Boolean?,
    val compactionIntervalSeconds: Long?, // 0 Means no log compaction
)

data class InMemoryConfig(
    val enabled: Boolean
)