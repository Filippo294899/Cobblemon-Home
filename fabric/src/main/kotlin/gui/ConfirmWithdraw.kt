package guis

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import database.StoredPokemon
import database.dbpokemon
import eu.pb4.sgui.api.gui.SimpleGui
import eu.pb4.sgui.api.elements.GuiElementBuilder
import gui.TranslationConfig
import gui.Util
import net.minecraft.data.Main
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.math.Boxes

class ConfirmWithdraw(
    player: ServerPlayerEntity,
    private  var dbpokemon: dbpokemon,
    private val dbm: DatabaseManager,
    var tscfg : TranslationConfig
) : SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()
        var pokemon = dbpokemon.pokemon
        val pokemonName = pokemon?.nickname?.string ?: pokemon?.species?.name

        this.title = Util.parseColorCodes(tscfg.confirmwithdrawn)

        setSlot(4, GuiElementBuilder(PokemonItem.from(pokemon!!))
            .setName(Text.literal(pokemonName.toString()).bold().green())
            .addLoreLine(Util.parseColorCodes(tscfg.level.replace("<pokemonLevel>" , "${pokemon.level}")))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl1))
            .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl2))
        )

        setSlot(1, GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
            .setName(Util.parseColorCodes(tscfg.confirm))
            .addLoreLine(Util.parseColorCodes(tscfg.infowithdrawnl1))
            .setCallback { _, _, _, _ ->

                if(dbm.removePokemon(player.uuidAsString, dbpokemon.box,dbpokemon.slot)!=null){
                    player.party().add(pokemon)
                    player.sendMessage(Util.parseColorCodes(tscfg.succesfullywithdrawn))
                    this.close()
                    MainMenu(player,dbm,tscfg).open()
                }

            }
        )

        setSlot(7, GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
            .setName(Util.parseColorCodes(tscfg.back))
            .addLoreLine(Util.parseColorCodes(tscfg.back))
            .setCallback { _, _, _, _ ->
                this.close()
                HomeBoxesGui(player , dbm,tscfg).open()

            }
        )

        setSlot(0, GuiElementBuilder(Items.BOOK)
            .setName(Util.parseColorCodes(tscfg.infos))
            .addLoreLine(Util.parseColorCodes(tscfg.infowithdrawnl1))
            .addLoreLine(Util.parseColorCodes(tscfg.infol2)))
    }

    private fun clearSlots() {
        for (i in 0 until 27) {
            clearSlot(i)
        }
    }
}