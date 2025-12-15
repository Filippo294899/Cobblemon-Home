package gui

import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import guis.Confirm
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

class DepositGui(player: ServerPlayer, val dbm: DatabaseManager, val tscfg: TranslationConfig) :
        SimpleGui(MenuType.GENERIC_9x6, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()

        this.title = Util.parseColorCodes(tscfg.deposit)

        val party = player.party()

        val centerSlots = intArrayOf(13, 14, 15, 22, 23, 24)

        for (i in 0 until 6) {
            val slot = centerSlots[i]
            val pokemon: Pokemon? = if (i < party.size()) party.get(i) else null
            val pokemonName = pokemon?.nickname?.string ?: pokemon?.species?.name ?: "Unknown"

            val element =
                    if (pokemon != null) {
                        // Pokémon element creation
                        GuiElementBuilder(PokemonItem.from(pokemon))
                                .setName(Component.literal(pokemonName))
                                .addLoreLine(
                                        Util.parseColorCodes(
                                                tscfg.level.replace(
                                                        "<pokemonLevel>",
                                                        "${pokemon.level}"
                                                )
                                        )
                                )
                                .addLoreLine(
                                        Util.parseColorCodes(
                                                tscfg.hp
                                                        .replace(
                                                                "<currenthp>",
                                                                "${pokemon.currentHealth}"
                                                        )
                                                        .replace("<max>", "${pokemon.maxHealth}")
                                        )
                                )
                                .setCallback { _, _, _, _ ->
                                    this.close()
                                    Confirm(player, pokemon, dbm, tscfg).open()
                                }
                    } else {
                        // Empty slot
                        GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    }

            setSlot(slot, element)
        }

        setSlot(
                45,
                GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                        .setName(Util.parseColorCodes(tscfg.back))
                        .setCallback { _, _, _ ->
                            this.close()
                            MainMenu(player, dbm, tscfg).open()
                        }
        )
    }

    private fun clearSlots() {
        for (i in 0 until 54) {
            clearSlot(i)
        }
    }
}
