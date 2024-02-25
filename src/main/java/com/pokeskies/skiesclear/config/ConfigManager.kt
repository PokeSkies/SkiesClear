package com.pokeskies.skiesclear.config

import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesclear.SkiesClear
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

class ConfigManager(val configDir: File) {
    companion object {
        lateinit var CONFIG: MainConfig
    }

    init {
        reload()
    }

    fun reload() {
        copyDefaults()
        CONFIG = SkiesClear.INSTANCE.loadFile("config.json", MainConfig())
    }

    fun copyDefaults() {
        val classLoader = SkiesClear::class.java.classLoader

        configDir.mkdirs()

        // Main Config
        val configFile = configDir.resolve("config.json")
        if (!configFile.exists()) {
            try {
                val inputStream: InputStream = classLoader.getResourceAsStream("assets/skiesclear/config.json")
                Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                SkiesClear.LOGGER.error("Failed to copy the default config file: $e")
            }
        }
    }
}