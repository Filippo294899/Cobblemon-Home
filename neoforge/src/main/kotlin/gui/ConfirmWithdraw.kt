package gui

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import database.dbpokemon
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

class ConfirmWithdraw(
        player: ServerPlayer,
        private val dbpokemon: dbpokemon,
        private val dbm: DatabaseManager,
        private val tscfg: TranslationConfig
) : SimpleGui(MenuType.GENERIC_9x3, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()

        val pokemon = dbpokemon.pokemon ?: return
        val pokemonName = pokemon.nickname?.string ?: pokemon.species.name

        title = Util.parseColorCodes(tscfg.confirmwithdrawn)

        setSlot(
                4,
                GuiElementBuilder(PokemonItem.from(pokemon))
                        .setName(Component.literal(pokemonName).bold().green())
                        .addLoreLine(
                                Util.parseColorCodes(
                                        tscfg.level.replace(
                                                "<pokemonLevel>",
                                                pokemon.level.toString()
                                        )
                                )
                        )
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl1))
                        .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl2))
        )

        setSlot(
                1,
                GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
                        .setName(Util.parseColorCodes(tscfg.confirm))
                        .addLoreLine(Util.parseColorCodes(tscfg.infowithdrawnl1))
                        .setCallback { _, _, _, _ ->
                            val removed =
                                    dbm.removePokemon(
                                            player.stringUUID,
                                            dbpokemon.box,
                                            dbpokemon.slot
                                    )

                            if (removed != null) {
                                player.party().add(pokemon)
                                player.sendSystemMessage(
                                        Util.parseColorCodes(tscfg.succesfullywithdrawn)
                                )
                                close()
                                MainMenu(player, dbm, tscfg).open()
                            }
                        }
        )

        setSlot(
                7,
                GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                        .setName(Util.parseColorCodes(tscfg.back))
                        .addLoreLine(Util.parseColorCodes(tscfg.back))
                        .setCallback { _, _, _, _ ->
                            close()
                            HomeBoxesGui(player, dbm, tscfg).open()
                        }
        )

        setSlot(
                0,
                GuiElementBuilder(Items.BOOK)
                        .setName(Util.parseColorCodes(tscfg.infos))
                        .addLoreLine(Util.parseColorCodes(tscfg.infowithdrawnl1))
                        .addLoreLine(Util.parseColorCodes(tscfg.infol2))
        )
    }

    private fun clearSlots() {
        for (i in 0 until 27) {
            clearSlot(i)
        }
    }
}
