package guis

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import eu.pb4.sgui.api.gui.SimpleGui
import eu.pb4.sgui.api.elements.GuiElementBuilder
import gui.TranslationConfig
import gui.Util
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text

class DepositGui(
    player: ServerPlayerEntity,
    val dbm : DatabaseManager,
    val tscfg : TranslationConfig
) : SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {

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
            val pokemonName = pokemon?.nickname?.string ?: pokemon?.species?.name

            val element = if (pokemon != null) {
                // Crea un elemento per il Pokémon
                GuiElementBuilder(PokemonItem.from(pokemon)) // Puoi usare Items.POKE_BALL o altri item
                    .setName(Text.literal(pokemonName))
                    .addLoreLine(Util.parseColorCodes(tscfg.level.replace("<pokemonLevel>","${pokemon.level}")))
                    .addLoreLine(Util.parseColorCodes(tscfg.hp.replace("<currenthp>","${pokemon.hp}").replace("<max>","${pokemon.maxHealth}")))
                    .setCallback { _, _, _ ->
                        this.close()
                        Confirm(player,pokemon,dbm ,tscfg).open()
                    }
            } else {
                // Slot vuoto
                GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
            }

            setSlot(slot, element)
            setSlot(
                45, GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Util.parseColorCodes(tscfg.back))
                    .setCallback { _, _, _ ->
                        this.close()
                        MainMenu(player, dbm ,tscfg).open()
                    }
            )
        }


    }

    private fun clearSlots() {
        for (i in 0 until 54) {
            setSlot(i, GuiElementBuilder(ItemStack(Items.AIR)))
        }
    }
}