package sammyuwu.autosign.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import sammyuwu.autosign.AutoSignManager
import sammyuwu.autosign.SignPreset

class PresetEditScreen(
    private val parent: Screen,
    private val preset: SignPreset
) : Screen(Component.literal("Edit Preset")) {

    private lateinit var nameBox: EditBox
    private val lineBoxes: Array<EditBox?> = arrayOfNulls(4)

    override fun init() {
        val cx = width / 2
        val w = 280

        nameBox = EditBox(font, cx - w / 2, 38, w, 20, Component.literal("Name"))
        nameBox.value = preset.name
        nameBox.setMaxLength(64)
        addRenderableWidget(nameBox)

        for (i in 0 until 4) {
            val y = 80 + i * 26
            val box = EditBox(font, cx - w / 2, y, w, 20, Component.literal("Line ${i + 1}"))
            box.value = when (i) {
                0 -> preset.line1
                1 -> preset.line2
                2 -> preset.line3
                3 -> preset.line4
                else -> ""
            }
            box.setMaxLength(384)
            lineBoxes[i] = box
            addRenderableWidget(box)
        }

        val bottomY = height - 30

        addBtn(cx - 200, bottomY - 26, 130, "§rPreview Sign") { showPreview() }
        addBtn(cx - 65, bottomY - 26, 130, "§eClear All Lines") {
            lineBoxes.forEach { it?.value = "" }
        }
        addBtn(cx + 70, bottomY - 26, 130, "§7Insert §§") {
            val active = lineBoxes.find { it?.isFocused == true }
            if (active != null) {
                active.value = active.value + "&"
            }
        }
        addBtn(cx - 130, bottomY, 120, "§aSave") {
            preset.name = nameBox.value.ifBlank { "preset" }
            preset.line1 = lineBoxes[0]?.value ?: ""
            preset.line2 = lineBoxes[1]?.value ?: ""
            preset.line3 = lineBoxes[2]?.value ?: ""
            preset.line4 = lineBoxes[3]?.value ?: ""
            AutoSignManager.config.save()
            minecraft.setScreen(parent)
        }
        addBtn(cx + 10, bottomY, 120, "§cCancel") {
            minecraft.setScreen(parent)
        }
    }

    private fun addBtn(x: Int, y: Int, w: Int, text: String, onPress: () -> Unit) {
        addRenderableWidget(
            Button.builder(Component.literal(text)) { _ -> onPress() }
                .bounds(x, y, w, 20).build()
        )
    }

    private fun showPreview() {
        val translate = AutoSignManager.config.translateAmpersand
        val lines = lineBoxes.map { it?.value ?: "" }
            .map { if (translate) it.replace('&', '§') else it }
        val preview = lines.joinToString(" §8| §r")
        minecraft.gui.setOverlayMessage(Component.literal("§7preview: §r$preview"), false)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF)
        graphics.drawString(font, "§7Name", width / 2 - 140, 28, 0xAAAAAA, false)
        graphics.drawString(font, "§7Lines §8(use & for color codes — e.g. &c=red, &l=bold)",
            width / 2 - 140, 70, 0xAAAAAA, false)

        val translate = AutoSignManager.config.translateAmpersand
        val previewY = height - 80
        graphics.drawString(font, "§7live preview:", width / 2 - 140, previewY - 12, 0x888888, false)
        for (i in 0 until 4) {
            val raw = lineBoxes[i]?.value ?: ""
            val rendered = if (translate) raw.replace('&', '§') else raw
            graphics.drawString(font, Component.literal("  $rendered"), width / 2 - 140, previewY + i * 10, 0xFFFFFF, false)
        }
    }

    override fun shouldCloseOnEsc(): Boolean = true

    override fun onClose() {
        minecraft.setScreen(parent)
    }
}
