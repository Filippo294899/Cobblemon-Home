package config
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import  com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.text.Text


class Config {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    val configDir = File("config/cobblemonhome")



    val dbfolder = File("config/cobblemonhome/db")
    val dbfile = File("config/cobblemonhome/db/Database.json")
    val translationfile = File("config/cobblemonhome/translation.json")

    fun load()  {
        if(!configDir.exists()) {
            configDir.mkdir()
            println("${configDir.name} done!")
        }
        if(!dbfolder.exists()) {
            dbfolder.mkdir()

            println("${dbfolder.name} done!")
        }
        if(!dbfile.exists()) {
            createDefaultDBConfig(dbfile)

        }
        if (!translationfile.exists()){
            createTranslationFile(translationfile);
        }


    }

    private fun createDefaultDBConfig(file: File) {
        val defaultConfig = mapOf(
            "enabled" to true,
            "type" to "sqlite",
            "file" to "config/cobblemonhome/db/cobblemonhome.db",
            "host" to "localhost",
            "port" to 3306,
            "database" to "cobblemon",
            "user" to "root",
            "password" to "password",
            "useSSL" to false
        )

        FileWriter(file).use { writer ->
            gson.toJson(defaultConfig, writer)
        }
        println("db config done!")
    }


    private fun createTranslationFile(file: File) {
        val defaultConfig = mapOf(
            "title" to "CobblemonHome",
            "boxes" to "Boxes",
            "deposit" to "deposit",
            "trade" to "trade",
            "dataloaded" to "Cobblemon Home Data Loaded!",
            "currentbox" to "Box <currentbox>/<max>",
            "level" to "Lvl: <pokemonLevel>",
            "hp" to "HP: <currenthp>/<maxhp>",
            "box" to "Box: <currentbox> - Slot: <index>",
            "slot" to "Slot <index> - Empty",
            "previousBox" to "Previous Box",
            "nextBox" to "Next Box",
            "confirmdeposit" to "Confirm Deposit",
            "depositline1" to "You are about to deposit this Pokemon",
            "depositline2" to "into the PC. Do you confirm?",
            "confirm" to "confirm",
            "confirmwithdrawn" to "confirm withdrawn",
            "depositline" to "Deposit <pokemonname> in the PC",
            "succesfullydeposited" to "<pokemonname> succesfully deposited>",
            "succesfullywithdrawn" to "succesfully withdrawn ur pokemon!>",
            "errordepositing" to "unable to deposit <pokemonname>",
            "cancel" to "cancel",
            "back" to "Back",
            "infos" to "info",
            "infodepositl1" to "The Pokemon will be deposited",
            "infowithdrawnl1" to "The Pokemon will be withdrawn",
            "infol2" to "in the first available slot",
            "withdrawnl1" to "You are about to Withdrawn this Pokemon" ,
            "withdrawnl1" to "into the PC. Do you confirm?" ,
            "reloadcommand" to "ok!",
            "resetboxesuser"  to "ur boxes got resetted by an administrator",
            "adminresetboxesuser"  to "succesfully resetted <user> boxes!",
            "resetboxes" to "boxes resetted!"








            )

        FileWriter(file).use { writer ->
            gson.toJson(defaultConfig, writer)
        }
        println("translation config done!")
    }



}