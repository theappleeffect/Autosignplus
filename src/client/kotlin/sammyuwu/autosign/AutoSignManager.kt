package sammyuwu.autosign

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object AutoSignManager {
    val config: AutoSignConfig = AutoSignConfig.load()

    var signedCount: Int = 0
        private set
    private var sequentialCursor: Int = 0
    private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun toggle() {
        config.enabled = !config.enabled
        config.save()
        notify(if (config.enabled) "AutoSign §aON" else "AutoSign §cOFF")
    }

    fun toggleHud() {
        config.hudEnabled = !config.hudEnabled
        config.save()
        notify("HUD ${if (config.hudEnabled) "§aON" else "§cOFF"}")
    }

    fun cycleMode() {
        val all = CycleMode.entries.toTypedArray()
        config.cycleMode = all[(config.cycleMode.ordinal + 1) % all.size]
        config.save()
        notify("Mode §e${config.cycleMode}")
    }

    fun nextPreset() {
        if (config.presets.isEmpty()) return
        config.currentIndex = (config.currentIndex + 1) % config.presets.size
        config.save()
        notify("Preset §e${currentPreset()?.name ?: "?"}")
    }

    fun prevPreset() {
        if (config.presets.isEmpty()) return
        val n = config.presets.size
        config.currentIndex = (config.currentIndex - 1 + n) % n
        config.save()
        notify("Preset §e${currentPreset()?.name ?: "?"}")
    }

    fun resetCounter() {
        signedCount = 0
    }

    fun currentPreset(): SignPreset? = config.presets.getOrNull(config.currentIndex)

    fun pickPreset(): SignPreset? {
        if (config.presets.isEmpty()) return null
        return when (config.cycleMode) {
            CycleMode.FIXED -> currentPreset()
            CycleMode.SEQUENTIAL -> {
                val n = config.presets.size
                val p = config.presets[sequentialCursor % n]
                sequentialCursor = (sequentialCursor + 1) % n
                p
            }
            CycleMode.RANDOM -> config.presets.random()
        }
    }

    fun tryGetLines(currentLines: Array<String>, pos: BlockPos): Array<String>? {
        if (!config.enabled) return null
        if (config.sneakBypass) {
            val player = Minecraft.getInstance().player
            if (player != null && player.isShiftKeyDown) return null
        }
        val nonEmpty = currentLines.any { it.isNotBlank() }
        if (config.onlyEmpty && nonEmpty) return null
        val preset = pickPreset() ?: return null
        signedCount++
        val out = preset.lines().map { transform(it, pos) }.toTypedArray()
        if (config.notifyChat) {
            notify("Signed §e${preset.name}§r §8(#$signedCount)")
        }
        return out
    }

    private fun transform(s: String, pos: BlockPos): String {
        var out = applyTemplates(s, pos)
        if (config.translateAmpersand) out = out.replace('&', '§')
        return out
    }

    private fun applyTemplates(s: String, pos: BlockPos): String {
        if (!s.contains('{')) return s
        val client = Minecraft.getInstance()
        val player = client.player
        val playerName = player?.name?.string ?: "?"
        val server = client.currentServer?.ip ?: "singleplayer"
        val dim = client.level?.dimension()?.identifier()?.toString() ?: "?"
        return s
            .replace("{player}", playerName)
            .replace("{n}", signedCount.toString())
            .replace("{counter}", signedCount.toString())
            .replace("{date}", LocalDate.now().toString())
            .replace("{time}", LocalTime.now().format(TIME_FMT))
            .replace("{x}", pos.x.toString())
            .replace("{y}", pos.y.toString())
            .replace("{z}", pos.z.toString())
            .replace("{server}", server)
            .replace("{dim}", dim)
    }

    private fun notify(msg: String) {
        val client = Minecraft.getInstance()
        client.gui.setOverlayMessage(Component.literal(msg), false)
    }
}
