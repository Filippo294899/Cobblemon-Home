package com.cobblemain

import com.cobblemon.mod.common.api.storage.adapter.conversions.ReforgedConversion
import config.Config

import database.DatabaseManager
import database.MysqlDatabaseManager
import database.loadDBConfig
import events.OnJoinEvent

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import guis.*
import net.minecraft.command.argument.EntityArgumentType
import java.util.concurrent.CompletableFuture
import com.cobblemon.mod.common.api.storage.adapter.conversions.ReforgedConversion.Translator
import gui.TranslationConfig
import gui.Util

object Main : ModInitializer {

    override fun onInitialize()
    {

        println("INITIALIZING COBBLEMON HOME")
        println("""
             .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .-----------------.  .----------------.  .----------------.  .----------------.  .----------------.   
            | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. | | .--------------. || .--------------. || .--------------. || .--------------. |  
            | |     ______   | || |     ____     | || |   ______     | || |   ______     | || |   ______     | || |   _____      | || |  _________   | || | ____    ____ | || |     ____     | || | ____  _____  | | | |  ____  ____  | || |     ____     | || | ____    ____ | || |  _________   | |  
            | |   .' ___  |  | || |   .'    `.   | || |  |_   _ \    | || |  |_   _ \    | || |  |_   _ \    | || |  |_   _|     | || | |_   ___  |  | || ||_   \  /   _|| || |   .'    `.   | || ||_   \|_   _| | | | | |_   ||   _| | || |   .'    `.   | || ||_   \  /   _|| || | |_   ___  |  | |  
            | |  / .'   \_|  | || |  /  .--.  \  | || |    | |_) |   | || |    | |_) |   | || |    | |_) |   | || |    | |       | || |   | |_  \_|  | || |  |   \/   |  | || |  /  .--.  \  | || |  |   \ | |   | | | |   | |__| |   | || |  /  .--.  \  | || |  |   \/   |  | || |   | |_  \_|  | |  
            | |  | |         | || |  | |    | |  | || |    |  __'.   | || |    |  __'.   | || |    |  __'.   | || |    | |   _   | || |   |  _|  _   | || |  | |\  /| |  | || |  | |    | |  | || |  | |\ \| |   | | | |   |  __  |   | || |  | |    | |  | || |  | |\  /| |  | || |   |  _|  _   | |  
            | |  \ `.___.'\  | || |  \  `--'  /  | || |   _| |__) |  | || |   _| |__) |  | || |   _| |__) |  | || |   _| |__/ |  | || |  _| |___/ |  | || | _| |_\/_| |_ | || |  \  `--'  /  | || | _| |_\   |_  | | | |  _| |  | |_  | || |  \  `--'  /  | || | _| |_\/_| |_ | || |  _| |___/ |  | |  
            | |   `._____.'  | || |   `.____.'   | || |  |_______/   | || |  |_______/   | || |  |_______/   | || |  |________|  | || | |_________|  | || ||_____||_____|| || |   `.____.'   | || ||_____|\____| | | | | |____||____| | || |   `.____.'   | || ||_____||_____|| || | |_________|  | |  
            | |              | || |              | || |              | || |              | || |              | || |              | || |              | || |              | || |              | || |              | | | |              | || |              | || |              | || |              | |  
            | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' | | '--------------' || '--------------' || '--------------' || '--------------' |  
             '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'   '----------------'  '----------------'  '----------------'  '----------------'   
            
            
        """.trimIndent())





        // First load the config to ensure files exist
        val cfg = Config()
        cfg.load()

        var tscfg = TranslationConfig()
        tscfg.load()
        println("CobblemonHome[config] loaded!")

        var Db : MysqlDatabaseManager = MysqlDatabaseManager()

        Db.connect()
        println("""
    ${'$'}${'$'}\                                               ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\         ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\   ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  ${'$'}${'$'}\   ${'$'}${'$'}\ ${'$'}${'$'}\   ${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  
${'$'}${'$'} |                                              ${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}  __${'$'}${'$'}\       ${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}${'$'}\  ${'$'}${'$'} |${'$'}${'$'}${'$'}\  ${'$'}${'$'} |${'$'}${'$'}  _____|${'$'}${'$'}  __${'$'}${'$'}\\__${'$'}${'$'}  __|${'$'}${'$'}  _____|${'$'}${'$'}  __${'$'}${'$'}\ 
${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\   ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\  ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\${'$'}${'$'}${'$'}${'$'}\   ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\        ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |      ${'$'}${'$'} /  \__|${'$'}${'$'} /  ${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'} |${'$'}${'$'} |      ${'$'}${'$'} /  \__|  ${'$'}${'$'} |   ${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'} |
${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}  __${'$'}${'$'}\ ${'$'}${'$'}  _${'$'}${'$'}  _${'$'}${'$'}\ ${'$'}${'$'}  __${'$'}${'$'}\       ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ |      ${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} ${'$'}${'$'}\${'$'}${'$'} |${'$'}${'$'} ${'$'}${'$'}\${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}${'$'}\    ${'$'}${'$'} |        ${'$'}${'$'} |   ${'$'}${'$'}${'$'}${'$'}${'$'}\    ${'$'}${'$'} |  ${'$'}${'$'} |
${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} /  ${'$'}${'$'} |${'$'}${'$'} / ${'$'}${'$'} / ${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'}  __${'$'}${'$'}\       ${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} \${'$'}${'$'}${'$'}${'$'} |${'$'}${'$'} \${'$'}${'$'}${'$'}${'$'} |${'$'}${'$'}  __|   ${'$'}${'$'} |        ${'$'}${'$'} |   ${'$'}${'$'}  __|   ${'$'}${'$'} |  ${'$'}${'$'} |
${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} | ${'$'}${'$'} | ${'$'}${'$'} |${'$'}${'$'}   ____|      ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |  ${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'}\ ${'$'}${'$'} |  ${'$'}${'$'} |${'$'}${'$'} |\${'$'}${'$'}${'$'} |${'$'}${'$'} |\${'$'}${'$'}${'$'} |${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'}\   ${'$'}${'$'} |   ${'$'}${'$'} |      ${'$'}${'$'} |  ${'$'}${'$'} |
${'$'}${'$'} |  ${'$'}${'$'} |\${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |${'$'}${'$'} | ${'$'}${'$'} | ${'$'}${'$'} |\${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\       ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |      \${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  | ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |${'$'}${'$'} | \${'$'}${'$'} |${'$'}${'$'} | \${'$'}${'$'} |${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ \${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |  ${'$'}${'$'} |   ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}\ ${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}  |
\__|  \__| \______/ \__| \__| \__| \_______|      \_______/ \_______/        \______/  \______/ \__|  \__|\__|  \__|\________| \______/   \__|   \________|\_______/ 
                                                                                                                                                                     
                                                                                                                                                                     
                                                                                                                                                                             
            
        """.trimIndent())




        // Then set up event listeners and commands

        OnJoinEvent(Db).start_listening()


        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->

            dispatcher.register(
                CommandManager.literal("cobblemon-home")
                    .executes { context ->

                        val player = context.source.player
                        if (player != null) {
                            if (!Db.playerExists(player.uuidAsString)) Db.addPlayer(player.uuidAsString)
                            if (player is ServerPlayerEntity) {
                                try {
                                    MainMenu(player, Db,tscfg).open()
                                } catch (e: Error) {
                                    println(e)
                                }
                            }
                        }
                        1
                    }

                    // 🔹 /cobblemon-home resetboxes
                    .then(
                        CommandManager.literal("resetboxes")
                            .executes { context ->
                                val player = context.source.player
                                if (player == null) {
                                    context.source.sendError(Text.literal("only players can use this command"))
                                    return@executes 0
                                }

                                val uuid = player.uuidAsString
                                if (!Db.playerExists(uuid)) {
                                    Db.addPlayer(uuid)
                                }

                                Db.resetBoxes(uuid)

                                player.sendMessage( Util.parseColorCodes( tscfg.resetboxes.replace("<user>" ,context.source.name.toString())), false)

                                1
                            }
                    )
                    .then(
                        CommandManager.literal("reload")
                            .requires { source -> source.hasPermissionLevel(2) }
                            .executes { context ->
                                var player = context.source.player
                                tscfg.load()

                                player?.sendMessage(Util.parseColorCodes(tscfg.reloadCommand), false)

                                1
                            }
                    )

                    // 🔹 /cobblemon-home resetboxes <player>
                    .then(
                        CommandManager.literal("resetboxes")
                            .then(
                                CommandManager.argument("target", EntityArgumentType.player())
                                    .requires { source -> source.hasPermissionLevel(2) } // solo admin
                                    .executes { context ->
                                        val target = EntityArgumentType.getPlayer(context, "target")
                                        val uuid = target.uuidAsString

                                        if (!Db.playerExists(uuid)) {
                                            Db.addPlayer(uuid)
                                        }

                                        Db.resetBoxes(uuid)

                                        context.source.sendFeedback(
                                            { Util.parseColorCodes( tscfg.adminresetboxesuser.replace("<user>" , target.name.string).replace("<admin>" , context.source.name.toString())) },
                                            true
                                        )

                                        target.sendMessage(
                                            Util.parseColorCodes( tscfg.resetBoxesuser.replace("<user>" , target.name.string).replace("<admin>" , context.source.name.toString())),
                                            false
                                        )


                                        1
                                    }
                            )
                    )
            )

        }
}}