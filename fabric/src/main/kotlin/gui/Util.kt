package gui

import net.minecraft.text.Text

object Util {
    fun parseColorCodes(text: String): Text {
        val formatted = text
            .replace("<red>", "§c")
            .replace("<blue>", "§9")
            .replace("<green>", "§a")
            .replace("<yellow>", "§e")
            .replace("<purple>", "§5")
            .replace("<aqua>", "§b")
            .replace("<white>", "§f")
            .replace("<black>", "§0")
            .replace("<gray>", "§7")
            .replace("<dark_red>", "§4")
            .replace("<dark_blue>", "§1")
            .replace("<bold>", "§l")
            .replace("<italic>", "§o")
            .replace("<reset>", "§r")

        return Text.literal(formatted)
    }
}