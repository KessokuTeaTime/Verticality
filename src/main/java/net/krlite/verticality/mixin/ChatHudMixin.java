package net.krlite.verticality.mixin;

import dev.yurisuika.raised.Raised;
import net.krlite.equator.math.algebra.Theory;
import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
		context.getMatrices().translate(Verticality.chatOffset().x(), Verticality.chatOffset().y(), 0);
	}

	@ModifyVariable(
			method = "mouseClicked",
			at = @At(value = "HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private double mouseClickedOffsetX(double value) {
		return value + Verticality.chatOffset().x();
	}

	@ModifyVariable(
			method = "mouseClicked",
			at = @At(value = "HEAD"),
			ordinal = 1,
			argsOnly = true
	)
	private double mouseClickedOffsetY(double value) {
		return value + Verticality.chatOffset().y();
	}

	@ModifyVariable(
			method = "toChatLineY",
			at = @At(value = "HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private double chatTooltipOffsetY(double value) {
		return value + Verticality.chatOffset().y();
	}
}
