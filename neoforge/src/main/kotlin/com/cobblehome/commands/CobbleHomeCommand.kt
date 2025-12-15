package com.cobblehome.commands

import com.mojang.brigadier.CommandDispatcher
import database.MysqlDatabaseManager
import gui.MainMenu
import gui.TranslationConfig
import gui.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object CobbleHomeCommand {

    fun register(
            dispatcher: CommandDispatcher<CommandSourceStack>,
            database: MysqlDatabaseManager,
            translationConfig: TranslationConfig
    ) {
        dispatcher.register(
                Commands.literal("cobblemon-home")
                        .executes { context ->
                            val player = context.source.playerOrException
                            openMenu(player, database, translationConfig)
                            1
                        }
                        .then(
                                Commands.literal("resetboxes").executes { context ->
                                    val player = context.source.playerOrException
                                    database.resetBoxes(player.uuid.toString())
                                    player.sendSystemMessage(
                                            Util.parseColorCodes(translationConfig.resetboxes)
                                    )
                                    1
                                }
                        )
        )
    }

    private fun openMenu(
            player: ServerPlayer,
            database: MysqlDatabaseManager,
            config: TranslationConfig
    ) {
        val uuid = player.uuid.toString()

        if (!database.playerExists(uuid)) {
            database.addPlayer(uuid)
        }

        MainMenu(player, database, config).open()
    }
}
