package sammyuwu.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sammyuwu.autosign.AutoSignManager;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin {
	@Shadow private SignBlockEntity sign;
	@Shadow private boolean isFrontText;
	@Shadow private String[] messages;

	@Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
	private void autosign$onInit(CallbackInfo ci) {
		String[] current = new String[]{messages[0], messages[1], messages[2], messages[3]};
		String[] result = AutoSignManager.INSTANCE.tryGetLines(current, sign.getBlockPos());
		if (result == null) return;

		for (int i = 0; i < 4; i++) {
			messages[i] = result[i];
		}

		Minecraft mc = Minecraft.getInstance();
		ClientPacketListener conn = mc.getConnection();
		if (conn != null) {
			conn.send(new ServerboundSignUpdatePacket(
				sign.getBlockPos(),
				isFrontText,
				messages[0], messages[1], messages[2], messages[3]
			));
		}
		mc.setScreen(null);
		ci.cancel();
	}
}
