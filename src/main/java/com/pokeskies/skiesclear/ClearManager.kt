package com.pokeskies.skiesclear

import com.pokeskies.skiesclear.config.ConfigManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer

class ClearManager {
    private val clearTasks: MutableMap<String, ClearTask> = mutableMapOf()
    private var ticks = 0

    init {
        reload()

        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server: MinecraftServer ->
            if (ticks++ > 20) {
                ticks = 0
                clearTasks.values.toList().forEach { it.tick(server) }
            }
        })
    }

    fun reload() {
        clearTasks.clear()
        for ((id, clearConfig) in ConfigManager.CONFIG.clears) {
            if (clearConfig.enabled) {
                val task = ClearTask(clearConfig)
                task.clearConfig.clearables.forEach { (id, clearable) ->
                    clearable.initialize()
                }
                clearTasks[id] = task
            }

        }
    }

    fun getClearTask(id: String): ClearTask? {
        return clearTasks[id]
    }
}
