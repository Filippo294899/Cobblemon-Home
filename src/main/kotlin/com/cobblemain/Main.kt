package com.cobblemain

import config.Config
import database.DatabaseManager
import database.DbScope
import events.OnJoinEvent
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import guis.*
import net.minecraft.command.argument.EntityArgumentType
import gui.TranslationConfig
import gui.Util
import net.minecraft.server.MinecraftServer

object Main : ModInitializer {
    // Usa nullable invece di lateinit per evitare l'errore
    private var server: MinecraftServer? = null
    private var db: DatabaseManager? = null
    private lateinit var tscfg: TranslationConfig

    override fun onInitialize() {
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

        tscfg = TranslationConfig()
        tscfg.load()
        println("CobblemonHome[config] loaded!")

        // NON inizializzare Db qui! Usa SERVER_STARTING invece di SERVER_STARTED
        ServerLifecycleEvents.SERVER_STARTING.register { serverInstance ->
            server = serverInstance

            println("Server starting - initializing database...")

            try {
                // Ora che abbiamo il server, possiamo creare Db
                db = DatabaseManager(serverInstance)
                db?.loadconfig()
                db?.connect()

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

                // Inizializza gli eventi dopo che il database è pronto
                if (db != null) {
                    OnJoinEvent(db!!).start_listening()
                    println("Event listeners registered successfully!")
                }
            } catch (e: Exception) {
                println("ERROR initializing database: ${e.message}")
                e.printStackTrace()
            }
        }

        // I comandi possono essere registrati qui, ma devono gestire il caso in cui Db non sia ancora inizializzato
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            dispatcher.register(
                CommandManager.literal("cobblemon-home")
                    .executes { context ->
                        val player = context.source.player
                        val currentDb = db

                        if (player != null) {
                            if (currentDb == null) {
                                player.sendMessage(Text.literal("§cDatabase not initialized. Retry in a few"), false)
                                return@executes 0
                            }

                            val srv = context.source.server
                            DbScope.launch {
                                currentDb.ensurePlayerInitialized(player.uuidAsString)
                                srv.execute {
                                    if (player is ServerPlayerEntity) {
                                        try {
                                            MainMenu(player, currentDb, tscfg).open()
                                        } catch (e: Error) {
                                            println(e)
                                        }
                                    }
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
                                val currentDb = db

                                if (player == null) {
                                    context.source.sendError(Text.literal("only players can use this command"))
                                    return@executes 0
                                }

                                if (currentDb == null) {
                                    context.source.sendError(Text.literal("§cDatabase not initialized"))
                                    return@executes 0
                                }

                                val uuid = player.uuidAsString
                                val srv = context.source.server
                                DbScope.launch {
                                    currentDb.ensurePlayerInitialized(uuid)
                                    currentDb.resetBoxes(uuid)
                                    srv.execute {
                                        player.sendMessage(Util.parseColorCodes(tscfg.resetboxes.replace("<user>", context.source.name.toString())), false)
                                    }
                                }

                                1
                            }
                    )
                    .then(
                        CommandManager.literal("reload")
                            .requires { source -> source.hasPermissionLevel(2) }
                            .executes { context ->
                                val player = context.source.player
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
                                        val currentDb = db
                                        if (currentDb == null) {
                                            context.source.sendError(Text.literal("§cDatabase not initialized"))
                                            return@executes 0
                                        }

                                        val target = EntityArgumentType.getPlayer(context, "target")
                                        val uuid = target.uuidAsString
                                        val srv = context.source.server

                                        DbScope.launch {
                                            currentDb.ensurePlayerInitialized(uuid)
                                            currentDb.resetBoxes(uuid)
                                            srv.execute {
                                                context.source.sendFeedback(
                                                    { Util.parseColorCodes(tscfg.adminresetboxesuser.replace("<user>", target.name.string).replace("<admin>", context.source.name.toString())) },
                                                    true
                                                )

                                                target.sendMessage(
                                                    Util.parseColorCodes(tscfg.resetBoxesuser.replace("<user>", target.name.string).replace("<admin>", context.source.name.toString())),
                                                    false
                                                )
                                            }
                                        }

                                        1
                                    }
                            )
                    )
            )
        }
    }

    // Metodo helper per ottenere il database
    fun getDatabase(): DatabaseManager? = db

    fun getServer(): MinecraftServer? = server
}