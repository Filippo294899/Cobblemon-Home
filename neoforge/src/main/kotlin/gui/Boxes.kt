package gui

import com.cobblemon.mod.common.item.PokemonItem
import database.DatabaseManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

class HomeBoxesGui(
        player: ServerPlayer,
        private val dbm: DatabaseManager,
        private val tscfg: TranslationConfig
) : SimpleGui(MenuType.GENERIC_9x6, player, false) {

    private var currentPage = 0
    private val totalPages = 30
    private val itemsPerPage = 30

    init {
        update()
    }

    private fun update() {
        clearSlots()
        val currentBox = currentPage + 1

        title =
                Util.parseColorCodes(
                        tscfg.currentbox
                                .replace("<currentbox>", currentBox.toString())
                                .replace("<max>", totalPages.toString())
                )

        val pokemonsInBox = dbm.getPokemonsFromBox(player.stringUUID, currentBox)

        val pokemonSlots =
                intArrayOf(
                        4,
                        10,
                        11,
                        12,
                        13,
                        14,
                        15,
                        16,
                        19,
                        20,
                        21,
                        22,
                        23,
                        24,
                        25,
                        28,
                        29,
                        30,
                        31,
                        32,
                        33,
                        34,
                        37,
                        38,
                        39,
                        40,
                        41,
                        42,
                        43,
                        49
                )

        for (i in 0 until itemsPerPage) {
            val slot = pokemonSlots[i]
            val pokemonData = pokemonsInBox.getOrNull(i)

            val element =
                    if (pokemonData?.pokemon != null) {
                        val pokemon = pokemonData!!.pokemon!!
                        val pokemonName = pokemon.nickname?.string ?: pokemon.species.name

                        GuiElementBuilder(PokemonItem.from(pokemon))
                                .setName(Util.parseColorCodes(pokemonName))
                                .addLoreLine(
                                        Util.parseColorCodes(
                                                tscfg.level.replace(
                                                        "<pokemonLevel>",
                                                        pokemon.level.toString()
                                                )
                                        )
                                )
                                .addLoreLine(
                                        Util.parseColorCodes(
                                                tscfg.box
                                                        .replace(
                                                                "<currentbox>",
                                                                currentBox.toString()
                                                        )
                                                        .replace("<index>", (i + 1).toString())
                                        )
                                )
                                .setCallback { _, _, _, _ ->
                                    close()
                                    ConfirmWithdraw(player, pokemonData, dbm, tscfg).open()
                                }
                    } else {
                        GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE)
                                .setName(
                                        Util.parseColorCodes(
                                                tscfg.slot.replace("<index>", (i + 1).toString())
                                        )
                                )
                                .addLoreLine(
                                        Util.parseColorCodes(
                                                tscfg.box
                                                        .replace(
                                                                "<currentbox>",
                                                                currentBox.toString()
                                                        )
                                                        .replace("<index>", (i + 1).toString())
                                        )
                                )
                    }

            setSlot(slot, element)
        }

        setSlot(
                0,
                GuiElementBuilder(Items.RED_DYE)
                        .setName(Util.parseColorCodes(tscfg.back))
                        .setCallback { _, _, _, _ ->
                            close()
                            MainMenu(player, dbm, tscfg).open()
                        }
        )

        setSlot(
                45,
                GuiElementBuilder(Items.ARROW)
                        .setName(Util.parseColorCodes(tscfg.previousBox))
                        .setCallback { _, _, _, _ ->
                            currentPage = if (currentPage == 0) totalPages - 1 else currentPage - 1
                            update()
                        }
        )

        setSlot(
                53,
                GuiElementBuilder(Items.ARROW)
                        .setName(Util.parseColorCodes(tscfg.nextBox))
                        .setCallback { _, _, _, _ ->
                            currentPage = if (currentPage == totalPages - 1) 0 else currentPage + 1
                            update()
                        }
        )
    }

    private fun clearSlots() {
        for (i in 0 until 54) {
            clearSlot(i)
        }
    }
}
