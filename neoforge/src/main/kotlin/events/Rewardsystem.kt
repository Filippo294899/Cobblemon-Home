//package events
//
//import com.cobblemon.mod.common.api.events.CobblemonEvents
//import com.cobblemon.mod.common.pokedex.PokedexManager
//import database.DatabaseManager
//
//class RewardSystem(private val databaseManager: DatabaseManager) {
//
//    fun init() {
//
//        CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe { event ->
//
//            val player = event.player
//            val knowledge = event.knowledge
//            val pokedex = event.pokedexManager
//
//            val caught = knowledge.get.size
//            val total = pokedex.totalEntries
//
//            val percent = (caught.toDouble() / total.toDouble()) * 100.0
//
//            // ---- ESEMPI DI REWARD ----
//            when {
//                percent >= 100 -> {
//                    giveReward(player, "dex_100")
//                }
//                percent >= 75 -> {
//                    giveReward(player, "dex_75")
//                }
//                percent >= 50 -> {
//                    giveReward(player, "dex_50")
//                }
//            }
//        }
//    }
//
//    private fun giveReward(player: ServerPlayerEntity, rewardId: String) {
//        // Usa il tuo database manager o sistema interno
//        databaseManager.giveRewardToPlayer(player.uuid, rewardId)
//
//        player.sendMessage(Text.literal("Hai raggiunto un traguardo nel Pokédex! [$rewardId]"))
//    }
//}
