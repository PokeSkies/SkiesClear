package com.pokeskies.skiesclear.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.config.ConfigManager
import com.pokeskies.skiesclear.utils.SubCommand
import com.pokeskies.skiesclear.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

class InfoCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("info")
            .requires(Permissions.require("skiesclear.command.info", 4))
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests { ctx, builder ->
                    SharedSuggestionProvider.suggest(ConfigManager.CONFIG.clears.keys, builder)
                }
                .executes { ctx ->
                    execute(ctx, StringArgumentType.getString(ctx, "id"))
                }
            )
            .executes { ctx ->
                execute(ctx, null)
            }
            .build()
    }

    companion object {
        private fun execute(
            ctx: CommandContext<CommandSourceStack>,
            id: String?
        ): Int {
            var clearEntries = ConfigManager.CONFIG.clears.filter { it.value.enabled }
            if (id != null) {
                val clearConfig = ConfigManager.CONFIG.clears[id]
                if (clearConfig == null) {
                    ctx.source.sendFailure(
                        Component.literal("Clear with id $id not found.")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                    return 0
                }

                if (!clearConfig.enabled) {
                    ctx.source.sendFailure(
                        Component.literal("The Clear entry $id is disabled.")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                    return 0
                }

                val clearTask = SkiesClear.INSTANCE.clearManager.getClearTask(id)
                if (clearTask == null) {
                    ctx.source.sendFailure(
                        Component.literal("Clear Task for $id not found. Is it disabled?")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                    return 0
                }

                clearEntries = mapOf(id to clearConfig)
            }

            if (clearEntries.isEmpty()) {
                ctx.source.sendFailure(
                    Component.literal("No enabled clear entries found.")
                        .withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            for ((clearId, clearConfig) in clearEntries) {
                val clearTask = SkiesClear.INSTANCE.clearManager.getClearTask(clearId) ?: continue
                for (line in clearConfig.messages.info) {
                    ctx.source.sendMessage(
                        Utils.deserializeText(
                        line.replace("%time_remaining%", Utils.getFormattedTime(clearTask.getTimer().toLong()))
                    ))
                }
            }

            return 1
        }
    }
}