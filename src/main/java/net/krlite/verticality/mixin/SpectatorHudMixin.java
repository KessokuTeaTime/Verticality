package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorHud.class)
public abstract class SpectatorHudMixin {
	@Shadow protected abstract void renderSpectatorCommand(DrawContext drawContext, int slot, int x, float y, float height, SpectatorMenuCommand command);

	@Shadow protected abstract float getSpectatorMenuHeight();

	@Unique
	private float antiAliasingOffset() {
		return -(1 - (Verticality.SPECTATOR_BAR_HEIGHT * getSpectatorMenuHeight()) % 1) % 1;
	}

	@Inject(
			method = "renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			)
	)
	private void renderSpectatorMenuPre(DrawContext context, float height, int x, int y, SpectatorMenuState state, CallbackInfo ci) {
		Verticality.spectatorMenuHeightScalar(height);
		context.getMatrices().push();

		// Alternative layout
		context.getMatrices().translate(
				Verticality.alternativeLayoutOffset().x(),
				Verticality.alternativeLayoutOffset().y(),
				0
		);

		if (Verticality.enabled()) {
			context.getMatrices().translate(
					Verticality.height() - Verticality.hotbarShift() * Verticality.transition() + antiAliasingOffset(),
					(Verticality.height() - Verticality.width()) / 2.0,
					0
			);

			Verticality.compatibleWithRaised(context);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
		}
		else {
			context.getMatrices().translate(0, Verticality.hotbarShift() * Verticality.transition() - antiAliasingOffset(), 0);
		}
	}

	@Inject(
			method = "renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderSpectatorMenuPost(DrawContext context, float height, int x, int y, SpectatorMenuState state, CallbackInfo ci) {
		context.getMatrices().pop();
	}

	@Redirect(
			method = "renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void drawSelectedSlot(DrawContext context, Identifier identifier, int x, int y, int width, int height) {
		Verticality.drawSelectedSlot(context, identifier, x, y, width, height);
	}

	@Redirect(
			method = "renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;FIILnet/minecraft/client/gui/hud/spectator/SpectatorMenuState;)V",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;renderSpectatorCommand(Lnet/minecraft/client/gui/DrawContext;IIFFLnet/minecraft/client/gui/hud/spectator/SpectatorMenuCommand;)V"
			)
	)
	private void renderSpectatorCommandIcon(SpectatorHud spectatorHud, DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command) {
		if (Verticality.enabled()) {
			double xRelative = (x + 8) - Verticality.width() / 2.0, yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);
			renderSpectatorCommand(
					context, slot,
					(int) Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8,
					(int) Math.round(Verticality.height() / 2.0 + xRelative) - 8,
					height, command
			);
		}
		else renderSpectatorCommand(context, slot, x, y, height, command);
	}

	@Inject(method = "renderSpectatorCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", shift = At.Shift.AFTER))
	private void renderCommandIcon(DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		Verticality.translateIcon(context, y, true, false);

		if (Verticality.enabled()) context.getMatrices().translate(antiAliasingOffset(), 0, 0);
		else context.getMatrices().translate(0, -antiAliasingOffset(), 0);
	}

	@Inject(
			method = "renderSpectatorCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private void renderCommandTextPre(DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.translateIcon(context, y, true, false);

		if (Verticality.enabled()) context.getMatrices().translate(antiAliasingOffset(), 0, 0);
		else context.getMatrices().translate(0, -antiAliasingOffset(), 0);
	}

	@Inject(
			method = "renderSpectatorCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderCommandTextPost(DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command, CallbackInfo ci) {
		context.getMatrices().pop();
	}
}

@Mixin(SpectatorHud.class)
class TextAdjustor {
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private void renderPromptPre(DrawContext context, CallbackInfo ci) {
		context.getMatrices().push();
		context.getMatrices().translate(0, Verticality.hotbarShift() * Verticality.later(), 0);
	}

	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderPromptPost(DrawContext context, CallbackInfo ci) {
		context.getMatrices().pop();
	}
}