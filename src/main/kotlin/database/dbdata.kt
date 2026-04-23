package database
import com.google.gson.Gson

import java.io.File
import java.io.FileReader

data class DBConfig(
    val enabled: Boolean = true,
    val type: String? = "sqlite",
    val file: String? = "config/cobblemonhome/db/cobblemonhome.db",
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "cobblemon",
    val user: String = "root",
    val password: String = "password",
    val useSSL: Boolean = false
)

fun loadDBConfig(): DBConfig {
    try {
        val gson = Gson()
        val modname = "cobblemonhome"
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
