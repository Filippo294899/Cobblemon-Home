package com.cobblehome

import com.cobblehome.commands.CobbleHomeCommand
import com.cobblemon.mod.common.Cobblemon
import config.Config
import database.MysqlDatabaseManager
import events.OnJoinEvent
import gui.TranslationConfig
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent

@Mod("cobblehome")
class CobbleHome {

    private val database = MysqlDatabaseManager()
    private val translationConfig = TranslationConfig()

    init {
        Cobblemon.LOGGER.info("Initializing CobbleHome")

        // ---- CONFIG ----
        Config().load()
        translationConfig.load()

        // ---- DATABASE ----
        database.connect()

        // ---- EVENTS ----
        NeoForge.EVENT_BUS.addListener(OnJoinEvent(database)::onPlayerJoin)

        // ---- COMMANDS ----
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)

        Cobblemon.LOGGER.info("CobbleHome initialized")
    }

    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        CobbleHomeCommand.register(
                dispatcher = event.dispatcher,
                database = database,
                translationConfig = translationConfig
        )
    }
}
