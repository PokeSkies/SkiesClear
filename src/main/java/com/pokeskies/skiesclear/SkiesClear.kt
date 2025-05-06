package com.pokeskies.skiesclear

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pokeskies.skiesclear.commands.BaseCommand
import com.pokeskies.skiesclear.config.ConfigManager
import com.pokeskies.skiesclear.utils.Utils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import org.apache.logging.log4j.LogManager
import java.io.File

class SkiesClear : ModInitializer {
    companion object {
        lateinit var INSTANCE: SkiesClear

        const val MOD_ID = "skiesclear"
        const val MOD_NAME = "SkiesClear"

        val LOGGER = LogManager.getLogger("skiesclear")
    }

    lateinit var configDir: File

    var adventure: FabricServerAudiences? = null
    var server: MinecraftServer? = null

    lateinit var clearManager: ClearManager

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(ResourceLocation::class.java, Utils.ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, "skiesclear")
        ConfigManager.load()

        this.clearManager = ClearManager()

        ServerLifecycleEvents.SERVER_STARTING.register(ServerStarting { server ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _ ->

        })
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerStopped { _ ->
            this.adventure = null
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()
        this.clearManager.reload()
    }
}
