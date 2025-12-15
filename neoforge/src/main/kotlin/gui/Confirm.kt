package guis

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import database.DatabaseManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import gui.DepositGui
import gui.MainMenu
import gui.TranslationConfig
import gui.Util
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

class Confirm(
        player: ServerPlayer,
        private val pokemon: Pokemon,
        private val dbm: DatabaseManager,
        var tscfg: TranslationConfig
) : SimpleGui(MenuType.GENERIC_9x3, player, false) {

    init {
        update()
    }

    private fun update() {
        clearSlots()

        val pokemonName = pokemon.nickname?.string ?: pokemon.species.name

        this.title = Util.parseColorCodes(tscfg.confirmdeposit)

        setSlot(
                4,
                GuiElementBuilder(PokemonItem.from(pokemon))
                        .setName(Component.literal(pokemonName).bold().green())
                        .addLoreLine(Component.literal("Lvl: ${pokemon.level}"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl1))
                        .addLoreLine(Util.parseColorCodes(tscfg.withdrawnl2))
        )

        setSlot(
                1,
                GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
                        .setName(Util.parseColorCodes(tscfg.confirm))
                        .addLoreLine(
                                Util.parseColorCodes(
                                        tscfg.depositline.replace("<pokemonname>", "$pokemonName")
                                )
                        )
                        .setCallback { _, _, _, _ ->
                            try {
                                val success =
                                        dbm.depositPokemon(
                                                uuid = player.stringUUID,
                                                boxNumber = null,
                                                slot = null,
                                                pokemon = pokemon
                                        )

                                if (success) {
                                    // Manually remove the Pokemon from the player's party
                                    player.party().remove(pokemon)
                                    player.sendSystemMessage(
                                            Util.parseColorCodes(
                                                    tscfg.succesfullydeposited.replace(
                                                            "<pokemonname>",
                                                            "$pokemonName"
                                                    )
                                            )
                                    )
                                    this.close()
                                    MainMenu(player, dbm, tscfg).open()
                                } else {
                                    player.sendSystemMessage(
                                            Util.parseColorCodes(tscfg.errordepositing)
                                    )
                                    this.close()
                                    DepositGui(player, dbm, tscfg).open()
                                }
                            } catch (e: Exception) {
                                player.sendSystemMessage(
                                        Util.parseColorCodes(tscfg.errordepositing)
                                )
                                this.close()
                                DepositGui(player, dbm, tscfg).open()
                            }
                        }
        )

        setSlot(
                7,
                GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                        .setName(Util.parseColorCodes(tscfg.cancel))
                        .addLoreLine(Util.parseColorCodes(tscfg.back))
                        .setCallback { _, _, _, _ ->
                            this.close()
                            DepositGui(player, dbm, tscfg).open()
                        }
        )

        setSlot(
                0,
                GuiElementBuilder(Items.BOOK)
                        .setName(Util.parseColorCodes(tscfg.infos))
                        .addLoreLine(Util.parseColorCodes(tscfg.infodepositl1))
                        .addLoreLine(Util.parseColorCodes(tscfg.infol2))
        )
    }

    private fun clearSlots() {
        for (i in 0 until 27) {
            clearSlot(i)
        }
    }
}
