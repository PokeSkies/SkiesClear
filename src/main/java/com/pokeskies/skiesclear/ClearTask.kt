package com.pokeskies.skiesclear

import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.config.ConfigManager
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import java.util.concurrent.atomic.AtomicInteger

class ClearTask(
    private val clearConfig: ClearConfig
) {
    private var dimensions: List<ServerLevel>? = null
    private var timer = clearConfig.interval

    fun runClear(server: MinecraftServer, broadcast: Boolean): Int {
        val totalCleared = AtomicInteger()
        for (dimension in getDimensions(server)) {
            val removeList: MutableList<Entity> = mutableListOf()
            try {
                for (entity in dimension.allEntities) {
                    if (entity is Mob && entity.isPersistenceRequired) continue

                    if (entity !is Player && clearConfig.clearables != null) {
                        if (clearConfig.clearables.items != null &&
                            clearConfig.clearables.items.enabled &&
                            clearConfig.clearables.items.shouldClear(entity)) {
                            removeList.add(entity)
                        } else if (clearConfig.clearables.cobblemon != null &&
                            clearConfig.clearables.cobblemon.enabled &&
                            clearConfig.clearables.cobblemon.shouldClear(entity)) {
                            removeList.add(entity)
                        } else if (clearConfig.clearables.entities != null &&
                            clearConfig.clearables.entities.enabled &&
                            clearConfig.clearables.entities.shouldClear(entity)) {
                            removeList.add(entity)
                        }
                    }
                }
                for (entity in removeList) {
                    entity.remove(Entity.RemovalReason.KILLED)
                    totalCleared.getAndIncrement()
                }
            } catch (exception: Exception) {
                Utils.printError("An exception was thrown while attempting to clear entities: + $exception")
                exception.printStackTrace()
            }

        }
        if (clearConfig.commands.clear.isNotEmpty()) {
            for (command in clearConfig.commands.clear) {
                if (server.commands.performPrefixedCommand(server.createCommandSourceStack(), command) == 0) {
                    Utils.printError("The post-clearing command \"$command\" failed to execute")
                    break
                }
            }
        }
        if (broadcast && (clearConfig.messages.clear.isNotEmpty() || clearConfig.sounds.clear != null)) {
            for (player in server.playerList.players) {
                for (line in clearConfig.messages.clear) {
                    player.sendMessage(
                        Utils.deserializeText(
                            line.replace("%clear_time%".toRegex(), Utils.getFormattedTime(clearConfig.interval.toLong()))
                                .replace("%clear_amount%".toRegex(), totalCleared.toString())
                        )
                    )
                }
                if (clearConfig.sounds.clear != null && clearConfig.sounds.clear.sound.isNotEmpty()) {
                    player.playNotifySound(
                        SoundEvent.createVariableRangeEvent(ResourceLocation(clearConfig.sounds.clear.sound)),
                        SoundSource.MASTER,
                        clearConfig.sounds.clear.volume,
                        clearConfig.sounds.clear.pitch
                    )
                }
            }
        }

        return totalCleared.get()
    }

    fun tick(server: MinecraftServer) {
        val warningMessage: List<String>? = clearConfig.messages.warnings[timer.toString()]
        if (warningMessage != null) {
            for (player in server.playerList.players) {
                for (line in warningMessage) {
                    player.sendMessage(
                        Utils.deserializeText(
                            line.replace(
                                "%time_remaining%".toRegex(),
                                Utils.getFormattedTime(timer.toLong())
                            )
                        )
                    )
                }
            }
        }
        val warningSound: ClearConfig.Sounds.SoundSettings? = clearConfig.sounds.warnings[timer.toString()]
        if (warningSound != null && warningSound.sound.isNotEmpty()) {
            for (player in server.playerList.players) {
                player.playNotifySound(
                    SoundEvent.createVariableRangeEvent(ResourceLocation(warningSound.sound)),
                    SoundSource.MASTER,
                    warningSound.volume,
                    warningSound.pitch
                )
            }
        }
        val warningCommands: List<String>? = clearConfig.commands.warnings[timer.toString()]
        if (warningCommands != null) {
            for (command in warningCommands) {
                if (server.commands.performPrefixedCommand(server.createCommandSourceStack(), command) == 0) {
                    Utils.printError("The ${timer}s warning command \"$command\" failed to execute")
                    break
                }
            }
        }
        if (timer-- <= 0) {
            resetTimer()
            runClear(server, true)
        }
    }

    private fun getDimensions(server: MinecraftServer): List<ServerLevel> {
        if (dimensions != null) return dimensions!!
        var newDimensions = mutableListOf<ServerLevel>()
        for (level in server.allLevels) {
            if (clearConfig.dimensions.contains(level.dimension().location().toString())) {
                newDimensions.add(level)
            }
        }
        if (newDimensions.isEmpty()) newDimensions = server.allLevels.toMutableList()
        dimensions = newDimensions
        return newDimensions
    }

    fun getTimer(): Int {
        return timer
    }

    fun resetTimer() {
        timer = clearConfig.interval
    }
}
