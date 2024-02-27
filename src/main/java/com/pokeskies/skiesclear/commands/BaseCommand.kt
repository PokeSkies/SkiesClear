package com.pokeskies.skiesclear.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.commands.subcommands.DebugCommand
import com.pokeskies.skiesclear.commands.subcommands.ForceCommand
import com.pokeskies.skiesclear.commands.subcommands.InfoCommand
import com.pokeskies.skiesclear.commands.subcommands.ReloadCommand
import com.pokeskies.skiesclear.config.ConfigManager
import com.pokeskies.skiesclear.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class BaseCommand {
    private val aliases = listOf("skiesclear")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("skiesclear.command.base", 4))
                .executes { ctx ->
                    execute(ctx, null)
                }
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            DebugCommand().build(),
            ReloadCommand().build(),
            ForceCommand().build(),
            InfoCommand().build()
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }

    companion object {
        private fun execute(
            ctx: CommandContext<CommandSourceStack>,
            id: String?
        ): Int {
            var clearEntries = ConfigManager.CONFIG.clears.filter { it.value.enabled }
            if (clearEntries.isEmpty()) {
                ctx.source.sendFailure(
                    Component.literal("No enabled clear entries found.")
                    .withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            for ((clearId, clearConfig) in clearEntries) {
                val clearTask = SkiesClear.INSTANCE.clearManager.getClearTask(clearId) ?: continue
                for (line in clearConfig.messages.info) {
                    ctx.source.sendMessage(Utils.deserializeText(
                        line.replace("%time_remaining%", Utils.getFormattedTime(clearTask.getTimer().toLong()))
                    ))
                }
            }

            return 1
        }
    }
}