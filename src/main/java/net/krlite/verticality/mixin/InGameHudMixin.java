package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {
	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
	private void renderHotbar(float tickDelta, MatrixStack matrixStack, CallbackInfo ci) {
		if (Verticality.enabled()) {
			matrixStack.translate(
					 Verticality.CENTER_DISTANCE_TO_BORDER
							 + (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER)
							 - Verticality.HOTBAR_HEIGHT * Verticality.hotbar(),
					(
							Verticality.height() - Verticality.width()
									+ Verticality.OFFHAND_WIDTH * Verticality.offset()
					) / 2.0, 0
			);
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
		}
		else {
			matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.hotbar(), 0);
		}
	}

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 1))
	private void drawSelectedSlot(MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		Verticality.drawSelectedSlot(matrixStack, x, y, u, v, width, height);
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
			)
	)
	private void drawOffhandSlot(MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		if (Verticality.enabled()) {
			if ((MinecraftClient.getInstance().options.getMainArm().getValue().getOpposite() == Arm.LEFT) == !Verticality.upsideDown()) {
				drawTexture(matrixStack, (int) (Verticality.width() / 2.0 - 91 - 29), Verticality.height() - 23, 24, 22, 29, 24); // Left
			}
			else drawTexture(matrixStack, (int) (Verticality.width() / 2.0 + 91), Verticality.height() - 23, 53, 22, 29, 24); // Right
		}
		else drawTexture(matrixStack, x, y, u, v, width, height);
	}
}

@Mixin(InGameHud.class)
abstract
class ItemAdjustor extends DrawableHelper {
	@Shadow protected abstract void renderHotbarItem(MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed);

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/util/math/MatrixStack;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"))
	private void renderHotbarItem(InGameHud inGameHud, MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed) {
		if (Verticality.enabled()) {
			double xRelative = (x + 8) - Verticality.width() / 2.0, yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);
			renderHotbarItem(
					matrixStack,
					(int) Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8,
					(int) Math.round(Verticality.height() / 2.0 + xRelative) - 8,
					tickDelta, playerEntity, itemStack, seed
			);
		}
		else renderHotbarItem(matrixStack, x, y, tickDelta, playerEntity, itemStack, seed);
	}

	@ModifyArgs(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/util/math/MatrixStack;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1),
					to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false)
			)
	)
	private void fixOffhandItem(Args args) {
		int x = args.get(2);
		if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getMainArm() == Arm.LEFT)
			// Don't use 'MinecraftClient.getInstance().options.getMainArm()' as vanilla doesn't use it neither, otherwise causing the offhand item out-of-phase
			args.set(2, (int) Math.round(x - 2 * ((x + 8) - Verticality.width() / 2.0))); // Revert the x-coordinate of the item
		else args.set(2, x);
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;getBobbingAnimatedDoubleTime()I"
			)
	)
	private void renderHotbarItemPre(MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		matrixStack.push();
		Verticality.translateIcon(matrixStack, y, false);
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void renderHotbarItemPost(MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void renderHotbarItemOverlayPre(MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		matrixStack.push();
		Verticality.translateIcon(matrixStack, y, false);
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderHotbarItemOverlayPost(MatrixStack matrixStack, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		matrixStack.pop();
	}
}

@Mixin(InGameHud.class)
class BarAdjustor {
	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			)
	)
	private void renderStatusBarsPre(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderStatusBarsPost(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"
			)
	)
	private void renderHealthBarPre(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderHealthBarPost(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderMountHealth",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			)
	)
	private void renderMountHealthPre(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderMountHealth",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderMountHealthPost(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			)
	)
	private void renderMountJumpBarPre(JumpingMount mount, MatrixStack matrixStack, int x, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderMountJumpBarPost(JumpingMount mount, MatrixStack matrixStack, int x, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			)
	)
	private void renderExperienceBarPre(MatrixStack matrixStack, int x, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderExperienceBarPost(MatrixStack matrixStack, int x, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(
			method = "renderHeldItemTooltip",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"
			)
	)
	private void renderHeldItemTooltipPre(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.push();
		matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.later(), 0);
	}

	@Inject(
			method = "renderHeldItemTooltip",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderHeldItemTooltipPost(MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"))
	private void fixHelpItemTooltipPre(MatrixStack matrixStack, CallbackInfo ci) {
		if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
			matrixStack.push();
			matrixStack.translate(0, Verticality.TOOLTIP_OFFSET * Verticality.later(), 0);
		}
	}

	@Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", shift = At.Shift.AFTER))
	private void fixHelpItemTooltipPost(MatrixStack matrixStack, CallbackInfo ci) {
		if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
			matrixStack.pop();
		}
	}
}
