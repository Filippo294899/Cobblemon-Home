package events

import database.DatabaseManager
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.event.entity.player.PlayerEvent

class OnJoinEvent(private val databaseManager: DatabaseManager) {

    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as ServerPlayer
        val uuid = player.uuid.toString()

        if (!databaseManager.playerExists(uuid)) {
            databaseManager.addPlayer(uuid)
        }

        player.sendSystemMessage(
            Component.literal("Cobblemon Home Data Loaded!")
                .withStyle { style ->
                    style.withBold(true).withColor(0x55FF55)
                }
        )
    }
}
