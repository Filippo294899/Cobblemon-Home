package database

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.MinecraftServer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

object DbScope : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
}

class DatabaseManager(private val server: MinecraftServer) {
    private var config: DBConfig? = null
    private val gson = Gson()
    private val registryManager = server.registryManager

    object Players : Table("cobblemon_boxes") {
        val uuid = varchar("uuid", 36)
        val boxes = text("boxes")
        override val primaryKey = PrimaryKey(uuid)
    }

    fun loadconfig() {
        config = loadDBConfig()
    }

    fun connect() {
        if (config == null) loadconfig()
        val cfg = config ?: error("Database config not loaded!")

        when ((cfg.type ?: "sqlite").lowercase()) {
            "sqlite" -> {
                val path = cfg.file?.ifBlank { null } ?: "config/cobblemonhome/db/cobblemonhome.db"
                File(path).absoluteFile.parentFile?.mkdirs()
                Database.connect(
                    url = "jdbc:sqlite:$path",
                    driver = "org.sqlite.JDBC"
                )
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            }
            "postgres", "postgresql" -> {
                val url = buildString {
                    append("jdbc:postgresql://${cfg.host}:${cfg.port}/${cfg.database}")
                    if (cfg.useSSL) append("?ssl=true")
                }
                Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = cfg.user,
                    password = cfg.password
                )
            }
            else -> {
                val url = "jdbc:mysql://${cfg.host}:${cfg.port}/${cfg.database}?useSSL=${cfg.useSSL}&serverTimezone=UTC"
                Database.connect(
                    url = url,
                    driver = "com.mysql.cj.jdbc.Driver",
                    user = cfg.user,
                    password = cfg.password
                )
            }
        }

        transaction {
            SchemaUtils.create(Players)
            SchemaUtils.createMissingTablesAndColumns(Players)
        }
    }

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, statement = block)

    private suspend fun getBoxesMap(uuid: String): MutableMap<Int, MutableList<NbtCompound?>> = dbQuery {
        val row = Players.selectAll().where { Players.uuid eq uuid }.singleOrNull()
        val json = row?.get(Players.boxes) ?: return@dbQuery createEmptyBoxes()

        try {
            val type = object : TypeToken<Map<String, List<String?>>>() {}.type
            val rawMap: Map<String, List<String?>> = gson.fromJson(json, type)

            val result = mutableMapOf<Int, MutableList<NbtCompound?>>()
            for (i in 1..30) {
                val nbtStrings = rawMap[i.toString()]
                val list = MutableList<NbtCompound?>(30) { null }
                nbtStrings?.forEachIndexed { index, s ->
                    if (index < 30 && !s.isNullOrBlank()) {
                        list[index] = StringNbtReader.parse(s)
                    }
                }
                result[i] = list
            }
            result
        } catch (e: Exception) {
            createEmptyBoxes()
        }
    }

    private suspend fun saveBoxes(uuid: String, boxes: Map<Int, List<NbtCompound?>>): Boolean = try {
        val serializable = boxes.mapValues { entry -> entry.value.map { it?.toString() } }
        val json = gson.toJson(serializable)
        dbQuery {
            Players.upsert {
                it[Players.uuid] = uuid
                it[Players.boxes] = json
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    private fun createEmptyBoxes(): MutableMap<Int, MutableList<NbtCompound?>> =
        mutableMapOf<Int, MutableList<NbtCompound?>>().apply {
            for (i in 1..30) this[i] = MutableList(30) { null }
        }

    suspend fun playerExists(uuid: String): Boolean = dbQuery {
        Players.selectAll().where { Players.uuid eq uuid }.count() > 0
    }

    suspend fun addPlayer(uuid: String) {
        if (!playerExists(uuid)) saveBoxes(uuid, createEmptyBoxes())
    }

    suspend fun depositPokemon(uuid: String, boxNumber: Int? = null, slot: Int? = null, pokemon: Pokemon): Boolean {
        if (!playerExists(uuid)) addPlayer(uuid)
        val boxesMap = getBoxesMap(uuid)
        val target = findFreeSlot(boxesMap, boxNumber, slot) ?: return false
        val nbt = pokemon.saveToNBT(registryManager, NbtCompound())
        boxesMap[target.first]!![target.second] = nbt
        return saveBoxes(uuid, boxesMap)
    }

    suspend fun getPokemonsFromBox(uuid: String, boxNumber: Int): List<dbpokemon> {
        val boxesMap = getBoxesMap(uuid)
        val box = boxesMap[boxNumber] ?: return emptyList()
        return box.mapIndexedNotNull { index, nbt ->
            nbt?.let {
                val pokemon = Pokemon().loadFromNBT(registryManager, it)
                dbpokemon(
                    pokemon = pokemon,
                    box = boxNumber,
                    slot = index + 1,
                    properties = pokemon.createPokemonProperties()
                )
            }
        }
    }

    suspend fun removePokemon(uuid: String, boxNumber: Int, slot: Int): Pokemon? {
        val boxesMap = getBoxesMap(uuid)
        val box = boxesMap[boxNumber] ?: return null
        val nbt = box.getOrNull(slot - 1) ?: return null
        box[slot - 1] = null
        val saved = saveBoxes(uuid, boxesMap)
        return if (saved) Pokemon().loadFromNBT(registryManager, nbt) else null
    }

    suspend fun movePokemon(uuid: String, fromBox: Int, fromSlot: Int, toBox: Int, toSlot: Int): Boolean {
        val boxesMap = getBoxesMap(uuid)
        val sourceNbt = boxesMap[fromBox]?.getOrNull(fromSlot - 1) ?: return false
        if (boxesMap[toBox]?.getOrNull(toSlot - 1) != null) return false
        boxesMap[fromBox]!![fromSlot - 1] = null
        boxesMap[toBox]!![toSlot - 1] = sourceNbt
        return saveBoxes(uuid, boxesMap)
    }

    suspend fun resetBoxes(uuid: String): Boolean = saveBoxes(uuid, createEmptyBoxes())

    suspend fun ensurePlayerInitialized(uuid: String): Boolean {
        if (!playerExists(uuid)) addPlayer(uuid)
        return true
    }

    suspend fun debugBoxStatus(uuid: String) {
        val map = getBoxesMap(uuid)
        map.forEach { (num, content) ->
            val count = content.count { it != null }
            if (count > 0) println("Box $num: $count pokemon.")
        }
    }

    private fun findFreeSlot(map: Map<Int, List<NbtCompound?>>, b: Int?, s: Int?): Pair<Int, Int>? {
        if (b != null && s != null) {
            return if (b in 1..30 && s in 1..30 && map[b]?.getOrNull(s - 1) == null) Pair(b, s - 1) else null
        }
        if (b != null) {
            val idx = map[b]?.indexOfFirst { it == null } ?: -1
            return if (idx != -1) Pair(b, idx) else null
        }
        for (i in 1..30) {
            val idx = map[i]?.indexOfFirst { it == null } ?: -1
            if (idx != -1) return Pair(i, idx)
        }
        return null
    }

    fun serverThread(): MinecraftServer = server
}
