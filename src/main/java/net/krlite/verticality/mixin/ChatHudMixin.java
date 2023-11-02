package net.krlite.verticality.mixin;

import net.krlite.equator.math.algebra.Theory;
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
					shift = At.Shift.AFTER
			)
	)
	private void render(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
		double offset = Theory.lerp(
				Verticality.chat(), Verticality.spectatorMenuHeight(),
				Verticality.enabled() && Verticality.isSpectator()
						? Verticality.later() : 0
		);

		context.getMatrices().translate((Verticality.CENTER_DISTANCE_TO_BORDER + Verticality.raisedShift() / 2.0) * offset, 0, 0);
	}
}
