package sammyuwu.client

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import sammyuwu.autosign.AutoSignManager
import sammyuwu.autosign.HudRenderer
import sammyuwu.autosign.gui.AutoSignConfigScreen

object SammysBedrockNoRenderClient : ClientModInitializer {

	private lateinit var category: KeyMapping.Category
	private lateinit var keyToggle: KeyMapping
	private lateinit var keyConfig: KeyMapping
	private lateinit var keyNext: KeyMapping
	private lateinit var keyPrev: KeyMapping
	private lateinit var keyHud: KeyMapping
	private lateinit var keyMode: KeyMapping

	override fun onInitializeClient() {
		AutoSignManager.config

		category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("autosign", "main"))

		keyToggle = registerKey("key.autosign.toggle", GLFW.GLFW_KEY_Y)
		keyConfig = registerKey("key.autosign.config", GLFW.GLFW_KEY_O)
		keyNext = registerKey("key.autosign.next", GLFW.GLFW_KEY_UNKNOWN)
		keyPrev = registerKey("key.autosign.prev", GLFW.GLFW_KEY_UNKNOWN)
		keyHud = registerKey("key.autosign.hud", GLFW.GLFW_KEY_UNKNOWN)
		keyMode = registerKey("key.autosign.mode", GLFW.GLFW_KEY_I)

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while (keyToggle.consumeClick()) AutoSignManager.toggle()
			while (keyConfig.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(AutoSignConfigScreen(null))
				}
			}
			while (keyNext.consumeClick()) AutoSignManager.nextPreset()
			while (keyPrev.consumeClick()) AutoSignManager.prevPreset()
			while (keyHud.consumeClick()) AutoSignManager.toggleHud()
			while (keyMode.consumeClick()) AutoSignManager.cycleMode()
		}

		HudElementRegistry.addLast(
			Identifier.fromNamespaceAndPath("autosign", "hud"),
			HudRenderer
		)
	}

	private fun registerKey(translationKey: String, defaultKey: Int): KeyMapping {
		return KeyBindingHelper.registerKeyBinding(
			KeyMapping(translationKey, InputConstants.Type.KEYSYM, defaultKey, category)
		)
	}
}
