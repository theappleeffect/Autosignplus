package sammyuwu.autosign.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import sammyuwu.autosign.AutoSignManager
import sammyuwu.autosign.CycleMode
import sammyuwu.autosign.SignPreset

class AutoSignConfigScreen(private val parent: Screen?) : Screen(Component.literal("AutoSign Configuration")) {
    private val cfg get() = AutoSignManager.config
    private var page: Int = 0
    private val pageSize: Int = 8
    private val rowH: Int = 22

    override fun init() {
        val cx = width / 2
        val btnW = 130
        val gap = 4
        val r1y = 24
        val r2y = 48

        addButton(cx - btnW * 2 - gap * 2 + btnW / 2 - btnW / 2, r1y, btnW,
            "§rEnabled: ${onOff(cfg.enabled)}") { cfg.enabled = !cfg.enabled; cfg.save(); rebuild() }
        addButton(cx - btnW - gap / 2, r1y, btnW,
            "§rMode: §e${cfg.cycleMode}") {
            val all = CycleMode.entries
            cfg.cycleMode = all[(cfg.cycleMode.ordinal + 1) % all.size]
            cfg.save(); rebuild()
        }
        addButton(cx + gap / 2, r1y, btnW,
            "§rHUD: ${onOff(cfg.hudEnabled)}") { cfg.hudEnabled = !cfg.hudEnabled; cfg.save(); rebuild() }
        addButton(cx + btnW + gap + gap / 2, r1y, btnW,
            "§r&-Colors: ${onOff(cfg.translateAmpersand)}") { cfg.translateAmpersand = !cfg.translateAmpersand; cfg.save(); rebuild() }

        addButton(cx - btnW * 2 - gap * 2 + btnW / 2 - btnW / 2, r2y, btnW,
            "§rOnly Empty: ${onOff(cfg.onlyEmpty)}") { cfg.onlyEmpty = !cfg.onlyEmpty; cfg.save(); rebuild() }
        addButton(cx - btnW - gap / 2, r2y, btnW,
            "§rSneak Bypass: ${onOff(cfg.sneakBypass)}") { cfg.sneakBypass = !cfg.sneakBypass; cfg.save(); rebuild() }
        addButton(cx + gap / 2, r2y, btnW,
            "§rNotify Chat: ${onOff(cfg.notifyChat)}") { cfg.notifyChat = !cfg.notifyChat; cfg.save(); rebuild() }
        addButton(cx + btnW + gap + gap / 2, r2y, btnW,
            "§eReset Counter") { AutoSignManager.resetCounter(); rebuild() }

        val pages = totalPages()
        if (page >= pages) page = (pages - 1).coerceAtLeast(0)
        val pageY = 76
        addButton(cx - 60, pageY, 20, "§l<") {
            if (page > 0) { page--; rebuild() }
        }
        addButton(cx + 40, pageY, 20, "§l>") {
            if (page < totalPages() - 1) { page++; rebuild() }
        }

        val listTop = 102
        val start = page * pageSize
        val end = (start + pageSize).coerceAtMost(cfg.presets.size)
        for (i in start until end) {
            val rowY = listTop + (i - start) * rowH
            addPresetRow(i, cfg.presets[i], rowY)
        }

        val bottomY = height - 30
        addButton(cx - 200, bottomY, 130, "§a+ New Preset") {
            cfg.presets.add(SignPreset(name = "preset${cfg.presets.size}"))
            cfg.save()
            page = (cfg.presets.size - 1) / pageSize
            rebuild()
        }
        addButton(cx - 65, bottomY, 130, "§eDuplicate Selected") {
            val cur = AutoSignManager.currentPreset()
            if (cur != null) {
                cfg.presets.add(SignPreset(
                    name = "${cur.name}_copy",
                    line1 = cur.line1, line2 = cur.line2,
                    line3 = cur.line3, line4 = cur.line4
                ))
                cfg.save()
                page = (cfg.presets.size - 1) / pageSize
                rebuild()
            }
        }
        addButton(cx + 70, bottomY, 130, "§rDone") {
            cfg.save()
            minecraft.setScreen(parent)
        }
    }

    private fun addPresetRow(index: Int, preset: SignPreset, y: Int) {
        val cx = width / 2
        val current = cfg.currentIndex == index
        val nameLabel = (if (current) "§a▶ " else "  ") + preset.name
        addButton(cx - 200, y, 230, nameLabel) {
            cfg.currentIndex = index
            cfg.save()
            rebuild()
        }
        addButton(cx + 35, y, 60, "§eEdit") {
            minecraft.setScreen(PresetEditScreen(this, preset))
        }
        addButton(cx + 100, y, 30, "§c✕") {
            if (cfg.presets.size > 1) {
                cfg.presets.removeAt(index)
                if (cfg.currentIndex >= cfg.presets.size) cfg.currentIndex = cfg.presets.size - 1
                if (cfg.currentIndex < 0) cfg.currentIndex = 0
                cfg.save()
                rebuild()
            }
        }
        addButton(cx + 135, y, 30, "§7▲") {
            if (index > 0) {
                val tmp = cfg.presets[index - 1]
                cfg.presets[index - 1] = cfg.presets[index]
                cfg.presets[index] = tmp
                if (cfg.currentIndex == index) cfg.currentIndex = index - 1
                else if (cfg.currentIndex == index - 1) cfg.currentIndex = index
                cfg.save()
                rebuild()
            }
        }
        addButton(cx + 170, y, 30, "§7▼") {
            if (index < cfg.presets.size - 1) {
                val tmp = cfg.presets[index + 1]
                cfg.presets[index + 1] = cfg.presets[index]
                cfg.presets[index] = tmp
                if (cfg.currentIndex == index) cfg.currentIndex = index + 1
                else if (cfg.currentIndex == index + 1) cfg.currentIndex = index
                cfg.save()
                rebuild()
            }
        }
    }

    private fun addButton(x: Int, y: Int, w: Int, text: String, onPress: () -> Unit) {
        addRenderableWidget(
            Button.builder(Component.literal(text)) { _ -> onPress() }
                .bounds(x, y, w, 20).build()
        )
    }

    private fun rebuild() {
        clearWidgets()
        init()
    }

    private fun totalPages(): Int = ((cfg.presets.size + pageSize - 1) / pageSize).coerceAtLeast(1)

    private fun onOff(b: Boolean): String = if (b) "§aON" else "§cOFF"

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF)
        val pageInfo = "page ${page + 1}/${totalPages()} — ${cfg.presets.size} presets"
        graphics.drawCenteredString(font, Component.literal(pageInfo), width / 2, 82, 0xAAAAAA)
    }

    override fun shouldCloseOnEsc(): Boolean = true

    override fun onClose() {
        cfg.save()
        minecraft.setScreen(parent)
    }
}
