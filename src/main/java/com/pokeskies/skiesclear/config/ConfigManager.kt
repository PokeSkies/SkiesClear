package com.pokeskies.skiesclear.config

import com.google.gson.stream.JsonReader
import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object ConfigManager {
    private var assetPackage = "assets/${SkiesClear.MOD_ID}"

    lateinit var CONFIG: MainConfig

    fun load() {
        copyDefaults()

        CONFIG = loadFile("config.json", MainConfig())
    }

    private fun copyDefaults() {
        val classLoader = SkiesClear::class.java.classLoader

        SkiesClear.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = SkiesClear.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            file.mkdirs()
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    fun <T : Any> loadFile(filename: String, default: T, path: String = "", create: Boolean = false): T {
        var dir = SkiesClear.INSTANCE.configDir
        if (path.isNotEmpty()) {
            dir = dir.resolve(path)
        }
        val file = File(dir, filename)
        var value: T = default
        try {
            Files.createDirectories(SkiesClear.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = SkiesClear.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(SkiesClear.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = SkiesClear.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(SkiesClear.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = SkiesClear.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.forEach { sourceFile ->
                        val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                        if (Files.isDirectory(sourceFile)) {
                            // Create subdirectories in the destination
                            destinationFile.mkdirs()
                        } else {
                            // Copy files to the destination
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }
}
