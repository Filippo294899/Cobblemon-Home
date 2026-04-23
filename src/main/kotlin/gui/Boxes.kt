package guis

import com.cobblemon.mod.common.item.PokemonItem
import database.DatabaseManager
import database.DbScope
import database.dbpokemon
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import gui.TranslationConfig
import gui.Util
import kotlinx.coroutines.launch
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

class HomeBoxesGui(
    player: ServerPlayerEntity,
    var dbm: DatabaseManager,
    var tscfg: TranslationConfig
) : SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {

    private var currentPage = 0
    private val totalPages = 30
    private val itemsPerPage = 30

    private val pokemonSlots = intArrayOf(
        4,
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43,
        49
    )

    init {
        update()
    }

    private fun update() {
        clearSlots()
        val currentBox = currentPage + 1

        this.title = Util.parseColorCodes(
            tscfg.currentbox
                .replace("<currentbox>", currentBox.toString())
                .replace("<max>", totalPages.toString())
        )

        renderNav(currentBox)
        renderPlaceholders(currentBox)

        val uuid = player.uuidAsString
        val server = player.server
        DbScope.launch {
            val pokemonsInBox = dbm.getPokemonsFromBox(uuid, currentBox)
            server?.execute { renderPokemon(currentBox, pokemonsInBox) }
        }
    }

    private fun renderPlaceholders(currentBox: Int) {
        for (i in 0 until itemsPerPage) {
            val slot = pokemonSlots[i]
            setSlot(slot, GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE)
                .setName(Util.parseColorCodes(tscfg.slot.replace("<index>", (i + 1).toString())))
                .addLoreLine(Util.parseColorCodes(
                    tscfg.box
                        .replace("<currentbox>", currentBox.toString())
                        .replace("<index>", (i + 1).toString())
                )))
        }
    }

    private fun renderPokemon(currentBox: Int, pokemonsInBox: List<dbpokemon>) {
        for (i in 0 until itemsPerPage) {
            val slot = pokemonSlots[i]
            val pokemonData = pokemonsInBox.getOrNull(i)
            if (pokemonData?.pokemon != null) {
                val pokemon = pokemonData.pokemon!!
                val pokemonName = pokemon.nickname?.string ?: pokemon.species.name
                setSlot(slot, GuiElementBuilder(PokemonItem.from(pokemon))
                    .setName(Util.parseColorCodes("$pokemonName"))
                    .addLoreLine(Util.parseColorCodes(
                        tscfg.level.replace("<pokemonLevel>", pokemon.level.toString())
                    ))
                    .addLoreLine(Util.parseColorCodes(
                        tscfg.box
                            .replace("<currentbox>", currentBox.toString())
                            .replace("<index>", (i + 1).toString())
                    ))
                    .setCallback { _, _, _, _ ->
                        this.close()
                        ConfirmWithdraw(player, pokemonData, dbm, tscfg).open()
                    })
            }
        }
    }

    private fun renderNav(currentBox: Int) {
        setSlot(0, GuiElementBuilder(Items.RED_DYE)
            .setName(Util.parseColorCodes(tscfg.back))
            .setCallback { _, _, _, _ ->
                this.close()
                MainMenu(player, dbm, tscfg).open()
            }
        )

        setSlot(45, GuiElementBuilder(Items.ARROW)
            .setName(Util.parseColorCodes(tscfg.previousBox))
            .setCallback { _, _, _, _ ->
                currentPage = if (currentPage == 0) totalPages - 1 else currentPage - 1
                update()
            }
        )

        setSlot(53, GuiElementBuilder(Items.ARROW)
            .setName(Util.parseColorCodes(tscfg.nextBox))
            .setCallback { _, _, _, _ ->
                currentPage = if (currentPage == totalPages - 1) 0 else currentPage + 1
                update()
            }
        )

        setSlot(49, GuiElementBuilder(Items.AIR)
            .setName(Util.parseColorCodes(
                tscfg.currentbox
                    .replace("<currentbox>", currentBox.toString())
                    .replace("<max>", totalPages.toString())
            ))
            .addLoreLine(Util.parseColorCodes("Box ${currentPage + 1}/$totalPages")))
    }

    private fun clearSlots() {
        for (i in 0 until 54) clearSlot(i)
    }
}
