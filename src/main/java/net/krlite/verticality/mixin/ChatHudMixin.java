package net.krlite.verticality.mixin;

import dev.yurisuika.raised.Raised;
import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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
			method = "toChatLineX",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private double modifyChatTooltipX(double value) {
		return value - Verticality.chatOffset().x();
	}

	@ModifyVariable(
			method = "toChatLineY",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private double modifyChatTooltipY(double value) {
		return value - Verticality.chatOffset().y();
	}

	@ModifyVariable(
			method = "mouseClicked",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private double modifyMouseClickX(double value) {
		return value + Verticality.chatOffset().x();
	}

	@ModifyVariable(
			method = "mouseClicked",
			at = @At("HEAD"),
			ordinal = 1,
			argsOnly = true
	)
	private double modifyMouseClickY(double value) {
		return value + Verticality.chatOffset().y();
	}
}
