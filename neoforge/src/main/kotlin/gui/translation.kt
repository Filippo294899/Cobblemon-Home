package gui
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class TranslationConfig {

    // Tutte stringhe vuote di default
    var title: String = ""
    var boxes: String = ""
    var deposit: String = ""
    var trade: String = ""
    var dataloaded: String = ""
    var currentbox: String = ""
    var level: String = ""
    var hp: String = ""
    var box: String = ""
    var slot: String = ""
    var previousBox: String = ""
    var nextBox: String = ""
    var confirmdeposit: String = ""
    var depositline1: String = ""
    var depositline2: String = ""
    var confirm: String = ""
    var confirmwithdrawn: String = ""
    var depositline: String = ""
    var succesfullydeposited: String = ""
    var succesfullywithdrawn: String = ""
    var errordepositing: String = ""
    var cancel: String = ""
    var back: String = ""
    var infos: String = ""
    var infodepositl1: String = ""
    var infowithdrawnl1: String = ""
    var infol2: String = ""
    var withdrawnl1: String = ""
    var withdrawnl2: String = ""
    var reloadCommand  : String = ""
    var resetBoxesuser : String = ""
    var resetboxes : String= ""
    var adminresetboxesuser : String = ""
    val translationfile = File("config/cobblemonhome/translation.json")
    private val gson = Gson()

    fun load() {

        if (!translationfile.exists()) {
            println("Translation file not found: ${translationfile.path}")
            return
        }

        val type = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = gson.fromJson(translationfile.readText(), type)

        map["title"]?.let { title = it }
        map["boxes"]?.let { boxes = it }
        map["deposit"]?.let { deposit = it }
        map["trade"]?.let { trade = it }
        map["dataloaded"]?.let { dataloaded = it }
        map["currentbox"]?.let { currentbox = it }
        map["level"]?.let { level = it }
        map["hp"]?.let { hp = it }
        map["box"]?.let { box = it }
        map["slot"]?.let { slot = it }
        map["previousBox"]?.let { previousBox = it }
        map["nextBox"]?.let { nextBox = it }
        map["confirmdeposit"]?.let { confirmdeposit = it }
        map["depositline1"]?.let { depositline1 = it }
        map["depositline2"]?.let { depositline2 = it }
        map["confirm"]?.let { confirm = it }
        map["confirmwithdrawn"]?.let { confirmwithdrawn = it }
        map["depositline"]?.let { depositline = it }
        map["succesfullydeposited"]?.let { succesfullydeposited = it }
        map["succesfullywithdrawn"]?.let { succesfullywithdrawn = it }
        map["errordepositing"]?.let { errordepositing = it }
        map["cancel"]?.let { cancel = it }
        map["back"]?.let { back = it }
        map["infos"]?.let { infos = it }
        map["infodepositl1"]?.let { infodepositl1 = it }
        map["infowithdrawnl1"]?.let { infowithdrawnl1 = it }
        map["infol2"]?.let { infol2 = it }
        map["withdrawnl1"]?.let { withdrawnl1 = it }
        map["withdrawnl2"]?.let { withdrawnl2 = it }
        map["reloadcommand"] ?.let {reloadCommand = it}
        map["resetboxesuser"]?.let {resetBoxesuser = it}
        map["resetboxes"]?.let {resetboxes = it }
        map["adminresetboxesuser"]?.let {adminresetboxesuser = it}


        println("Translation config loaded!")
    }
}
