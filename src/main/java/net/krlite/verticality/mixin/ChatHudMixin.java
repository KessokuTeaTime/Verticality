package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void render(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
		double offset = Verticality.hotbarShift()
				* (Verticality.isSpectator()
				? (Verticality.hasSpectatorMenu() ? Math.min(Verticality.earlier(), Verticality.spectatorMenuHeightScalar()) : 0)
				: Verticality.earlier());

		context.getMatrices().translate(offset, 0, 0);
	}
}
