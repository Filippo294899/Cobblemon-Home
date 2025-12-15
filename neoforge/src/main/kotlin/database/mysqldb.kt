package database

import com.cobblemon.mod.common.api.moves.*
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class MysqlDatabaseManager : DatabaseManager() {
    private var config: DBConfig? = null

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun connect() {
        if (config == null) loadconfig()
        val cfg = config ?: throw IllegalStateException("Database config not loaded!")

        val url = buildString {
            append("jdbc:mysql://${cfg.host}:${cfg.port}/${cfg.database}")
            append("?useSSL=${cfg.useSSL}")
            append("&useUnicode=true&characterEncoding=UTF-8")
            append("&serverTimezone=UTC")
        }

        Database.connect(
                url = url,
                driver = "com.mysql.cj.jdbc.Driver",
                user = cfg.user,
                password = cfg.password
        )

        transaction {
            SchemaUtils.create(Players)
            SchemaUtils.createMissingTablesAndColumns(Players)
        }
    }

    override fun loadconfig() {
        this.config = loadDBConfig()
    }

    override fun addPlayer(uuid: String) {
        transaction {
            if (!playerExists(uuid)) {
                val emptyBoxes = mutableMapOf<Int, MutableList<StoredPokemon?>>()
                for (i in 1..30) emptyBoxes[i] = MutableList(30) { null }

                Players.insert {
                    it[Players.uuid] = uuid
                    it[boxes] = gson.toJson(emptyBoxes)
                }
            }
        }
    }

    override fun playerExists(uuid: String): Boolean {
        return transaction { Players.selectAll().where { Players.uuid eq uuid }.count() > 0 }
    }

    private fun getBoxesMap(uuid: String): MutableMap<Int, MutableList<StoredPokemon?>>? {
        return try {
            transaction {
                val row =
                        Players.selectAll().where { Players.uuid eq uuid }.singleOrNull()
                                ?: return@transaction null
                val boxesJson = row[Players.boxes]

                if (boxesJson.isBlank() || boxesJson == "null") {
                    return@transaction createEmptyBoxes()
                }

                try {
                    val type = object : TypeToken<HashMap<Int, ArrayList<StoredPokemon?>>>() {}.type
                    val rawMap: HashMap<Int, ArrayList<StoredPokemon?>> =
                            gson.fromJson(boxesJson, type) ?: HashMap()

                    val result = mutableMapOf<Int, MutableList<StoredPokemon?>>()

                    for (i in 1..30) {
                        val box =
                                rawMap[i]?.toMutableList()
                                        ?: MutableList<StoredPokemon?>(30) { null }

                        // normalize size
                        when {
                            box.size < 30 -> repeat(30 - box.size) { box.add(null) }
                            box.size > 30 -> box.subList(30, box.size).clear()
                        }

                        result[i] = box
                    }

                    result
                } catch (e: Exception) {
                    createEmptyBoxes()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createEmptyBoxes(): MutableMap<Int, MutableList<StoredPokemon?>> {
        val emptyBoxes = mutableMapOf<Int, MutableList<StoredPokemon?>>()
        for (i in 1..30) emptyBoxes[i] = MutableList<StoredPokemon?>(30) { null }
        return emptyBoxes
    }

    private fun saveBoxes(
            uuid: String,
            boxes: MutableMap<Int, MutableList<StoredPokemon?>>
    ): Boolean {
        return try {
            transaction {
                val json = gson.toJson(boxes)
                Players.update({ Players.uuid eq uuid }) { it[Players.boxes] = json }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun depositPokemon(
            uuid: String,
            boxNumber: Int?,
            slot: Int?,
            pokemon: Pokemon
    ): Boolean {
        return try {
            if (!playerExists(uuid)) {
                addPlayer(uuid)
            }

            val boxesMap = getBoxesMap(uuid) ?: return false

            val (targetBox, targetSlot) = findFreeSlot(boxesMap, boxNumber, slot) ?: return false

            val stored =
                    StoredPokemon(
                            species = pokemon.species.name,
                            nickname = pokemon.nickname?.string ?: "",
                            gender = pokemon.gender.name,
                            shiny = pokemon.shiny,
                            level = pokemon.level,
                            exp = pokemon.experience,
                            moves = pokemon.moveSet.map { it.name },
                            iv = pokemon.ivs.toList().map { it.value },
                            ev = pokemon.evs.toList().map { it.value },
                            ability = pokemon.ability?.name,
                            nature = (pokemon.nature?.name ?: "").toString(),
                            aspects = pokemon.aspects.toList(),
                            friendship = pokemon.friendship,
                            forms = pokemon.form.name,
                            heldItem = pokemon.heldItem().item.toString()
                    )

            boxesMap[targetBox]!![targetSlot] = stored
            val saved = saveBoxes(uuid, boxesMap)

            saved
        } catch (e: Exception) {
            false
        }
    }

    private fun findFreeSlot(
            boxesMap: Map<Int, MutableList<StoredPokemon?>>,
            boxNumber: Int?,
            slot: Int?
    ): Pair<Int, Int>? {
        return when {
            boxNumber != null && slot != null -> {
                if (boxNumber in 1..30 && slot in 1..30) {
                    val box = boxesMap[boxNumber] ?: return null
                    if (box[slot - 1] == null) Pair(boxNumber, slot - 1) else null
                } else null
            }
            boxNumber != null -> {
                val box = boxesMap[boxNumber] ?: return null
                val freeSlot = box.indexOfFirst { it == null }
                if (freeSlot != -1) Pair(boxNumber, freeSlot) else null
            }
            else -> {
                for (boxNum in 1..30) {
                    val box = boxesMap[boxNum] ?: continue
                    val freeSlot = box.indexOfFirst { it == null }
                    if (freeSlot != -1) return Pair(boxNum, freeSlot)
                }
                null
            }
        }
    }

    override fun getPokemonsFromBox(uuid: String, boxNumber: Int): List<dbpokemon> {
        return try {
            val boxesMap = getBoxesMap(uuid) ?: return emptyList()
            val box = boxesMap[boxNumber] ?: return emptyList()

            box.mapIndexedNotNull { index, stored ->
                if (stored != null) {
                    try {
                        val args = buildString {
                            append("${stored.species}")
                            append(" level=${stored.level}")
                            if (stored.shiny) append(" shiny=yes")
                            append(" gender=${stored.gender}")
                            stored.nature?.let { append(" nature=$it") }
                            stored.ability?.let { append(" ability=$it") }

                            stored.forms?.let { append(" form=$it") }
                        }

                        val props = PokemonProperties.parse(args)
                        val pokemon = props.create() ?: return@mapIndexedNotNull null

                        if (stored.nickname.isNotBlank() && stored.nickname != stored.species) {
                            pokemon.nickname = Component.literal(stored.nickname)
                        }

                        stored.iv?.forEachIndexed { statIndex, value ->
                            val stat = Stats.values().getOrNull(statIndex)
                            if (stat != null) {
                                var clampedValue = value as Int
                                clampedValue = if (value < 0) 0 else if (value > 31) 31 else value
                                pokemon.ivs[stat] = clampedValue
                            }
                        }

                        stored.ev?.forEachIndexed { statIndex, value ->
                            val stat = Stats.values().getOrNull(statIndex)
                            if (stat != null) {
                                var clampedValue = value as Int
                                clampedValue = if (value < 0) 0 else if (value > 252) 252 else value
                                pokemon.evs[stat] = clampedValue
                            }
                        }

                        val moveSet = pokemon.moveSet
                        moveSet.clear()

                        stored.moves?.forEach { moveName ->
                            if (moveName.isBlank()) return@forEach

                            try {
                                val move = Moves.getByName(moveName)
                                if (move == null) {
                                    return@forEach
                                }

                                if (moveSet.hasSpace()) {
                                    moveSet.add(move.create())
                                }
                            } catch (e: Exception) {}
                        }

                        pokemon.setFriendship(stored.friendship)

                        stored.aspects.forEach { aspect -> pokemon.forcedAspects += aspect }

                        stored.heldItem?.let { itemId ->
                            if (itemId.isNotBlank() && itemId != "air") {
                                try {
                                    val identifier = ResourceLocation.tryParse(itemId)
                                    if (identifier != null) {
                                        val item = BuiltInRegistries.ITEM.get(identifier)
                                        if (item != Items.AIR) {
                                            val stack = ItemStack(item)
                                            pokemon.swapHeldItem(stack, false)
                                        }
                                    }
                                } catch (e: Exception) {}
                            }
                        }

                        pokemon.updateForm()
                        pokemon.updateAspects()

                        val finalProps = pokemon.createPokemonProperties()

                        dbpokemon(
                                pokemon = pokemon,
                                box = boxNumber,
                                slot = index + 1,
                                properties = finalProps
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun removePokemon(uuid: String, boxNumber: Int, slot: Int): StoredPokemon? {
        return try {
            val boxes = getBoxesMap(uuid) ?: return null
            val box = boxes[boxNumber] ?: return null
            if (slot !in 1..30) return null

            val removed = box[slot - 1] ?: return null
            box[slot - 1] = null
            val saved = saveBoxes(uuid, boxes)

            if (saved) removed else null
        } catch (e: Exception) {
            null
        }
    }

    override fun movePokemon(
            uuid: String,
            fromBox: Int,
            fromSlot: Int,
            toBox: Int,
            toSlot: Int
    ): Boolean {
        return try {
            val boxes = getBoxesMap(uuid) ?: return false
            val sourceBox = boxes[fromBox] ?: return false
            val targetBox = boxes[toBox] ?: return false
            if (fromSlot !in 1..30 || toSlot !in 1..30) return false

            val pokemon = sourceBox[fromSlot - 1] ?: return false
            if (targetBox[toSlot - 1] != null) return false

            sourceBox[fromSlot - 1] = null
            targetBox[toSlot - 1] = pokemon
            val saved = saveBoxes(uuid, boxes)

            saved
        } catch (e: Exception) {
            false
        }
    }

    override fun resetBoxes(uuid: String): Boolean {
        return try {
            if (!playerExists(uuid)) {
                addPlayer(uuid)
                return true
            }

            val emptyBoxes = mutableMapOf<Int, MutableList<StoredPokemon?>>()
            for (i in 1..30) emptyBoxes[i] = MutableList(30) { null }
            val saved = saveBoxes(uuid, emptyBoxes)

            saved
        } catch (e: Exception) {
            false
        }
    }

    override fun debugBoxStatus(uuid: String) {
        try {
            val boxesMap = getBoxesMap(uuid) ?: return
            for (boxNum in 1..5) {
                val box = boxesMap[boxNum] ?: continue
                val occupied = box.count { it != null }
            }
        } catch (e: Exception) {}
    }

    override fun ensurePlayerInitialized(uuid: String): Boolean {
        return try {
            if (!playerExists(uuid)) {
                addPlayer(uuid)
                return true
            }

            val boxesMap = getBoxesMap(uuid)
            if (boxesMap == null || boxesMap.isEmpty()) {
                resetBoxes(uuid)
                return true
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
