package guis

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
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

class MainMenu(
    player: ServerPlayerEntity,
    val dbm : DatabaseManager,
    val tscfg: TranslationConfig

) : SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false) {


    init {

        update()
    }

    private fun update() {

        clearSlots()



        this.title = Util.parseColorCodes(tscfg.title)


        setSlot(
            3, GuiElementBuilder(Items.CHEST)
                .setName(Util.parseColorCodes(tscfg.boxes))
                .setCallback { _, _, _ ->
                    HomeBoxesGui(player,dbm,tscfg).open()
                }
        )
        setSlot(
            5, GuiElementBuilder(CobblemonItems.PC)
                .setName(Util.parseColorCodes(tscfg.deposit))
                .setCallback { _, _, _ ->
                        DepositGui(player,dbm , tscfg).open()

                }
        )
//        setSlot(
//            4, GuiElementBuilder(CobblemonItems.LINK_CABLE)
//                .setName(Util.parseColorCodes(tscfg.trade))
//        )

    }

    private fun clearSlots() {
        for (i in 0 until 8) {
            setSlot(i, GuiElementBuilder(ItemStack(Items.AIR)))
        }
    }
}