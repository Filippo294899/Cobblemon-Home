package database

import com.cobblemon.mod.common.CobblemonBrainConfigs.gson
import java.io.File
import java.io.FileReader

data class DBConfig(
    val enabled: Boolean,
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val useSSL: Boolean
)

fun loadDBConfig(): DBConfig {
    try {
        var modname = "cobblemonhome"
        val file = File("config/${modname}/db/Database.json")
        if (!file.exists()) {
            throw RuntimeException("Database config file not found: ${file.absolutePath}")
        }
        FileReader(file).use { reader ->
            return gson.fromJson(reader, DBConfig::class.java)
        }
    } catch (e: Exception) {
        println("Error loading database config: ${e.message}")
        throw e
    }
}