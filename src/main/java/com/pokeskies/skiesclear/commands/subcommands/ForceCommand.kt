package com.pokeskies.skiesclear.commands.subcommands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.config.ConfigManager
import com.pokeskies.skiesclear.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

class ForceCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("force")
            .requires(Permissions.require("skiesclear.command.force", 4))
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests { ctx, builder ->
                    SharedSuggestionProvider.suggest(ConfigManager.CONFIG.clears.keys, builder)
                }
                .then(Commands.argument("announce", BoolArgumentType.bool())
                    .executes { ctx ->
                        give(
                            ctx,
                            StringArgumentType.getString(ctx, "id"),
                            BoolArgumentType.getBool(ctx, "announce")
                        )
                    }
                )
                .executes { ctx ->
                    give(ctx, StringArgumentType.getString(ctx, "id"), true)
                }
            )
            .build()
    }

    companion object {
        private fun give(
            ctx: CommandContext<CommandSourceStack>,
            id: String,
            announce: Boolean
        ): Int {
            val clearConfig = ConfigManager.CONFIG.clears[id]
            if (clearConfig == null) {
                ctx.source.sendFailure(Component.literal("Clear with id $id not found.")
                    .withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            if (!clearConfig.enabled) {
                ctx.source.sendFailure(Component.literal("The Clear entry $id is disabled.")
                    .withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            val clearTask = SkiesClear.INSTANCE.clearManager.getClearTask(id)
            if (clearTask == null) {
                ctx.source.sendFailure(Component.literal("Clear Task for $id not found. Is it disabled?")
                    .withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }


            val entitiesCleared = clearTask.runClear(ctx.source.server, announce)
            clearTask.resetTimer()

            ctx.source.sendSystemMessage(Component.literal("Successfully cleared $entitiesCleared entities!")
                .withStyle { it.withColor(ChatFormatting.GREEN) })
            return 1
        }
    }
}