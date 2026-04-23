package guis

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import database.DbScope
import eu.pb4.sgui.api.gui.SimpleGui
import eu.pb4.sgui.api.elements.GuiElementBuilder
import gui.TranslationConfig
import gui.Util
import kotlinx.coroutines.launch
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.item.Items
import net.minecraft.text.Text

class Confirm(
    player: ServerPlayerEntity,
    private val pokemon: Pokemon,
    private val dbm: DatabaseManager,
    var tscfg : TranslationConfig
) : SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()

        val pokemonName = pokemon?.nickname?.string ?: pokemon?.species?.name

        this.title = Util.parseColorCodes(tscfg.confirmdeposit)

        setSlot(4, GuiElementBuilder(PokemonItem.from(pokemon))
            .setName(Text.literal(pokemonName.toString()).bold().green())
            .addLoreLine(Text.literal("Lvl: ${pokemon.level}"))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl1))
            .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl2))
        )

        setSlot(1, GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
            .setName(Util.parseColorCodes(tscfg.confirm))
            .addLoreLine(Util.parseColorCodes(tscfg.depositline.replace("<pokemonname>" , "${pokemonName}")))
            .setCallback { _, _, _, _ ->
                val server = player.server
                DbScope.launch {
                    val success = try {
                        dbm.depositPokemon(
                            uuid = player.uuidAsString,
                            boxNumber = null,
                            slot = null,
                            pokemon = pokemon
                        )
                    } catch (e: Exception) {
                        false
                    }

                    server?.execute {
                        if (success) {
                            player.party().remove(pokemon)
                            player.sendMessage(Util.parseColorCodes(tscfg.succesfullydeposited.replace("<pokemonname>", "${pokemonName}")), false)
                            this@Confirm.close()
                            MainMenu(player, dbm, tscfg).open()
                        } else {
                            player.sendMessage(Util.parseColorCodes(tscfg.errordepositing), false)
                            this@Confirm.close()
                            DepositGui(player, dbm, tscfg).open()
                        }
                    }
                }
            }
        )

        setSlot(7, GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
            .setName(Util.parseColorCodes(tscfg.cancel))
            .addLoreLine(Util.parseColorCodes(tscfg.back))
            .setCallback { _, _, _, _ ->
                this.close()

                DepositGui(player, dbm, tscfg).open()
            }
        )

        setSlot(0, GuiElementBuilder(Items.BOOK)
            .setName(Util.parseColorCodes(tscfg.infos))
            .addLoreLine(Util.parseColorCodes(tscfg.infodepositl1))
            .addLoreLine(Util.parseColorCodes(tscfg.infol2)))
    }

    private fun clearSlots() {
        for (i in 0 until 27) {
            clearSlot(i)
        }
    }
}