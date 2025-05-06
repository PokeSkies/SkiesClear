package com.pokeskies.skiesclear

import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.config.clearables.Clearable
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class ClearTask(
    val clearConfig: ClearConfig
) {
    private var dimensions: List<ServerLevel>? = null
    private var timer = clearConfig.interval

    fun runClear(server: MinecraftServer, broadcast: Boolean): Int {
        val levels = getLevels(server)
        val totals = clearConfig.clearables.map { (id, clearable) ->
            clearable to clearable.clearEntities(clearConfig, levels)
        }.toMap()

        val total = totals.values.sum()
        if (broadcast && (clearConfig.messages.clear.isNotEmpty() || clearConfig.sounds.clear != null)) {
            for (player in server.playerList.players.filter { shouldInform(it) }) {
                for (line in clearConfig.messages.clear) {
                    player.sendMessage(Utils.deserializeText(parsePlaceholders(line, total, totals)))
                }
                if (clearConfig.sounds.clear != null && clearConfig.sounds.clear.sound.isNotEmpty()) {
                    player.playNotifySound(
                        SoundEvent.createVariableRangeEvent(ResourceLocation.parse(clearConfig.sounds.clear.sound)),
                        SoundSource.MASTER,
                        clearConfig.sounds.clear.volume,
                        clearConfig.sounds.clear.pitch
                    )
                }
            }
        }
        return total
    }

    fun tick(server: MinecraftServer) {
        val warningMessage: List<String>? = clearConfig.messages.warnings[timer.toString()]
        if (warningMessage != null) {
            for (player in server.playerList.players.filter { shouldInform(it) }) {
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
            for (player in server.playerList.players.filter { shouldInform(it) }) {
                player.playNotifySound(
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse(warningSound.sound)),
                    SoundSource.MASTER,
                    warningSound.volume,
                    warningSound.pitch
                )
            }
        }
        if (timer-- <= 0) {
            resetTimer()
            runClear(server, true)
        }
    }

    private fun getLevels(server: MinecraftServer): List<ServerLevel> {
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

    private fun shouldInform(player: ServerPlayer): Boolean {
        return !clearConfig.informDimensionsOnly || clearConfig.dimensions.isEmpty() ||
            player.level().dimension().location().toString() in clearConfig.dimensions
    }

    fun getTimer(): Int {
        return timer
    }

    fun resetTimer() {
        timer = clearConfig.interval
    }

    private fun parsePlaceholders(string: String, total: Int, cleared: Map<Clearable<*>, Int>): String {
        var parsed = string
            .replace("%clear_time%".toRegex(), Utils.getFormattedTime(clearConfig.interval.toLong()))
            .replace("%clear_amount%".toRegex(), total.toString())

        cleared.forEach { (clearable, amount) ->
            parsed = clearable.parse(parsed, amount)
        }

        return parsed
    }
}
