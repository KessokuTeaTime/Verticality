package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorHud.class)
public abstract class SpectatorHudMixin {
	@Shadow protected abstract void renderSpectatorCommand(MatrixStack matrices, int slot, int x, float y, float height, SpectatorMenuCommand command);

	@Shadow protected abstract float getSpectatorMenuHeight();

	private float antiAliasingOffset() {
		return -(1 - (Verticality.SPECTATOR_BAR_HEIGHT * getSpectatorMenuHeight()) % 1) % 1;
	}

	@Inject(
			method = "renderSpectatorMenu(Lnet/minecraft/client/util/math/MatrixStack;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/SpectatorHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			)
	)
	private void renderSpectatorMenuPre(MatrixStack matrixStack, float height, int x, int y, SpectatorMenuState state, CallbackInfo ci) {
		Verticality.spectatorMenuHeight(height);
		matrixStack.push();

		if (Verticality.enabled()) {
			matrixStack.translate(
					Verticality.height() - Verticality.HOTBAR_HEIGHT * Verticality.hotbar() + antiAliasingOffset(),
					(Verticality.height() - Verticality.width()) / 2.0,
					0
			);
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
		}
		else {
			matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.hotbar() - antiAliasingOffset(), 0);
		}
	}

	@Inject(
			method = "renderSpectatorMenu(Lnet/minecraft/client/util/math/MatrixStack;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/SpectatorHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderSpectatorMenuPost(MatrixStack matrixStack, float height, int x, int y, SpectatorMenuState state, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Redirect(
			method = "renderSpectatorMenu(Lnet/minecraft/client/util/math/MatrixStack;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/SpectatorHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					ordinal = 1
			)
	)
	private void drawSelectedSlot(MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		Verticality.drawSelectedSlot(matrixStack, x, y, u, v, width, height);
	}

	@Redirect(
			method = "renderSpectatorMenu(Lnet/minecraft/client/util/math/MatrixStack;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;renderSpectatorCommand(Lnet/minecraft/client/util/math/MatrixStack;IIFFLnet/minecraft/client/gui/hud/spectator/SpectatorMenuCommand;)V"
			)
	)
	private void renderSpectatorCommandIcon(SpectatorHud spectatorHud, MatrixStack matrixStack, int slot, int x, float y, float height, SpectatorMenuCommand command) {
		if (Verticality.enabled()) {
			double xRelative = (x + 8) - Verticality.width() / 2.0, yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);
			renderSpectatorCommand(
					matrixStack, slot,
					(int) Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8,
					(int) Math.round(Verticality.height() / 2.0 + xRelative) - 8,
					height, command
			);
		}
		else renderSpectatorCommand(matrixStack, slot, x, y, height, command);
	}

	@Inject(method = "renderSpectatorCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", shift = At.Shift.AFTER))
	private void renderCommandIcon(MatrixStack matrixStack, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		Verticality.translateIcon(matrixStack, y, true);

		if (Verticality.enabled()) matrixStack.translate(antiAliasingOffset(), 0, 0);
		else matrixStack.translate(0, -antiAliasingOffset(), 0);
	}

	@Inject(
			method = "renderSpectatorCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"
			)
	)
	private void renderCommandTextPre(MatrixStack matrixStack, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		matrixStack.push();
		Verticality.translateIcon(matrixStack, y, true);

		if (Verticality.enabled()) matrixStack.translate(antiAliasingOffset(), 0, 0);
		else matrixStack.translate(0, -antiAliasingOffset(), 0);
	}

	@Inject(
			method = "renderSpectatorCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderCommandTextPost(MatrixStack matrixStack, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		matrixStack.pop();
	}
}

@Mixin(SpectatorHud.class)
class TextAdjustor {
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"
			)
	)
	private void renderPromptPre(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderPromptPost(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}
}
