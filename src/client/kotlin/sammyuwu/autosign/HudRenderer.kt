package sammyuwu.autosign

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

object HudRenderer : HudElement {
    override fun render(graphics: GuiGraphics, tickDelta: DeltaTracker) {
        val cfg = AutoSignManager.config
        if (!cfg.hudEnabled) return
        val client = Minecraft.getInstance()
        val font = client.font
        val state = if (cfg.enabled) "§aON" else "§cOFF"
        val preset = AutoSignManager.currentPreset()?.name ?: "—"
        val mode = cfg.cycleMode.name.lowercase().replaceFirstChar { it.uppercase() }
        val header: Component = Component.literal("§6AutoSign §r[$state§r]")
        val body: Component = Component.literal("§7preset: §f$preset §8| §7mode: §f$mode §8| §7signed: §f${AutoSignManager.signedCount}")
        graphics.drawString(font, header, 4, 4, 0xFFFFFF, true)
        graphics.drawString(font, body, 4, 14, 0xCCCCCC, true)
    }
}
