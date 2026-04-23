package events

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import database.DatabaseManager
import database.DbScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class OnJoinEvent(var databaseManager: DatabaseManager) {

    fun start_listening() {
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            val player: ServerPlayerEntity = handler.player
            DbScope.launch {
                databaseManager.ensurePlayerInitialized(player.uuidAsString)
            }
        }
    }

    private fun onPlayerJoin(player: ServerPlayerEntity) {
        player.sendMessage(Text.literal("Cobblemon Home Data Loaded!").bold().green(), false)
    }
}
