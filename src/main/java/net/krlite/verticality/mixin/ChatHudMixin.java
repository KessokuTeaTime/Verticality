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
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private void render(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
		double offsetX = Verticality.isSpectator()
				? Verticality.hotbarShift() * (Verticality.hasSpectatorMenu() ? Math.min(Verticality.earlier(), Verticality.spectatorMenuHeightScalar()) : 0)
				: (Verticality.hotbarShift() + Theory.lerp(0, Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT, Verticality.alternativeTransition())) * Verticality.earlier();

		double offsetY = Theory.lerp(
				Verticality.alternativeLayoutPartiallyEnabled() ? -Verticality.HOTBAR_FULL_HEIGHT : 0,
				Verticality.raisedSync() ? Verticality.raisedHudShift() - 3 * (Verticality.INFO_ICON_SIZE + Verticality.GAP) : 0,
				Verticality.earlier()
		);

		context.getMatrices().translate(offsetX, offsetY, 0);
	}
}
