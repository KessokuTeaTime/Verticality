package net.krlite.verticality.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.verticality.Verticality;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setZOffset(I)V", ordinal = 0, shift = At.Shift.AFTER))
private void renderHotbarPre(float tickDelta, MatrixStack matrixStack, CallbackInfo ci) {
	matrixStack.push();
}

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setZOffset(I)V", ordinal = 0, shift = At.Shift.AFTER))
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
			matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
		}
		else {
			matrixStack.translate(0, Verticality.HOTBAR_HEIGHT * Verticality.hotbar(), 0);
		}
	}

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setZOffset(I)V", ordinal = 1, shift = At.Shift.BEFORE))
	private void renderHotbarPost(float tickDelta, MatrixStack matrixStack, CallbackInfo ci) {
		matrixStack.pop();
	}

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 1))
	private void drawSelectedSlot(InGameHud inGameHud, MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		Verticality.drawSelectedSlot(inGameHud, matrixStack, x, y, u, v, width, height);
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V")
			)
	)
	private void drawOffhandSlot(InGameHud inGameHud, MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		if (Verticality.enabled()) {
			if ((MinecraftClient.getInstance().options.mainArm.getOpposite() == Arm.LEFT) == !Verticality.upsideDown()) {
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
	@Shadow protected abstract void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"))
	private void renderHotbarItem(InGameHud inGameHud, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed) {
		if (Verticality.enabled()) {
			double xRelative = (x + 8) - Verticality.width() / 2.0, yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);
			renderHotbarItem(
					(int) Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8,
					(int) Math.round(Verticality.height() / 2.0 + xRelative) - 8,
					tickDelta, playerEntity, itemStack, seed
			);
		}
		else renderHotbarItem(x, y, tickDelta, playerEntity, itemStack, seed);
	}

	@ModifyArgs(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
			),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
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
					target = "Lnet/minecraft/item/ItemStack;getBobbingAnimationTime()I"
			)
	)
	private void renderHotbarItemPre(int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		RenderSystem.getModelViewStack().push();
		Verticality.translateIcon(RenderSystem.getModelViewStack(), y, false);
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void renderHotbarItemPost(int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		RenderSystem.getModelViewStack().pop();
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void renderHotbarItemOverlayPre(int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		RenderSystem.getModelViewStack().push();
		Verticality.translateIcon(RenderSystem.getModelViewStack(), y, false);
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderHotbarItemOverlayPost(int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed, CallbackInfo ci) {
		RenderSystem.getModelViewStack().pop();
		RenderSystem.applyModelViewMatrix();
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
	private void renderMountJumpBarPre(MatrixStack matrixStack, int x, CallbackInfo ci) {
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
	private void renderMountJumpBarPost(MatrixStack matrixStack, int x, CallbackInfo ci) {
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
