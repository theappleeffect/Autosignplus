package sammyuwu.autosign

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

enum class CycleMode { FIXED, SEQUENTIAL, RANDOM }

class AutoSignConfig {
    var enabled: Boolean = true
    var hudEnabled: Boolean = true
    var cycleMode: CycleMode = CycleMode.FIXED
    var currentIndex: Int = 0
    var onlyEmpty: Boolean = false
    var sneakBypass: Boolean = true
    var translateAmpersand: Boolean = true
    var notifyChat: Boolean = false
    var presets: MutableList<SignPreset> = mutableListOf(
        SignPreset(name = "test", line1 = "test", line2 = "test", line3 = "test", line4 = "test")
    )

    fun save() {
        try {
            Files.createDirectories(PATH.parent)
            Files.writeString(PATH, GSON.toJson(this))
        } catch (e: Exception) {
            LOG.warn("autosign: save failed: {}", e.message)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger("autosign")
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        val PATH: Path = FabricLoader.getInstance().configDir.resolve("autosign.json")

        fun load(): AutoSignConfig {
            return try {
                if (Files.exists(PATH)) {
                    val json = Files.readString(PATH)
                    val cfg = GSON.fromJson(json, AutoSignConfig::class.java)
                    if (cfg == null) {
                        AutoSignConfig().also { it.save() }
                    } else {
                        if (cfg.presets.isEmpty()) {
                            cfg.presets.add(SignPreset(name = "test", line1 = "test", line2 = "test", line3 = "test", line4 = "test"))
                        }
                        cfg
                    }
                } else {
                    AutoSignConfig().also { it.save() }
                }
            } catch (e: Exception) {
                LOG.warn("autosign: load failed, using defaults: {}", e.message)
                AutoSignConfig()
            }
        }
    }
}
