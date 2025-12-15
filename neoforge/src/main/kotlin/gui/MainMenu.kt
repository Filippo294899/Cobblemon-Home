package gui

import com.cobblemon.mod.common.CobblemonItems
import database.DatabaseManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MainMenu(
        player: ServerPlayer,
        private val dbm: DatabaseManager,
        private val tscfg: TranslationConfig
) : SimpleGui(MenuType.GENERIC_9x1, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()

        title = Util.parseColorCodes(tscfg.title)

        player.sendSystemMessage(Component.literal(player.stringUUID))

        setSlot(
                3,
                GuiElementBuilder(Items.CHEST)
                        .setName(Util.parseColorCodes(tscfg.boxes))
                        .setCallback { _, _, _, _ -> HomeBoxesGui(player, dbm, tscfg).open() }
        )

        setSlot(
                5,
                GuiElementBuilder(CobblemonItems.PC.asItem())
                        .setName(Util.parseColorCodes(tscfg.deposit))
                        .setCallback { _, _, _, _ -> DepositGui(player, dbm, tscfg).open() }
        )
    }

    private fun clearSlots() {
        for (i in 0 until 9) {
            setSlot(i, GuiElementBuilder(ItemStack(Items.AIR)))
        }
    }
}
