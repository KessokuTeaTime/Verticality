package band.kessokuteatime.verticality.mixin;

import band.kessokuteatime.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudItemAdjustor {
    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed);

    @Redirect(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
            )
    )
    private void renderHotbarItem(InGameHud inGameHud, DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) {
        if (Verticality.enabled()) {
            double
                    xRelative = (x + 8) - Verticality.width() / 2.0,
                    yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);

            renderHotbarItem(
                    context,
                    (int) (Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8),
                    (int) (Math.round(Verticality.height() / 2.0 + xRelative) - 8),
                    tickDelta, player, stack, seed
            );
        } else {
            renderHotbarItem(context, x, y, tickDelta, player, stack, seed);
        }
    }

    @ModifyArgs(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1),
                    to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false)
            )
    )
    private void fixOffhandItem(Args args) {
        int x = args.get(1), y = args.get(2);

        if (Verticality.alternativeLayoutPartiallyEnabled()) {
            // x
            args.set(1, (Verticality.width() + Verticality.HOTBAR_WIDTH * (Verticality.enabled() ? 1 : -1)) / 2 - Verticality.HOTBAR_ITEM_GAP * (Verticality.enabled() ? 1 : -1) - Verticality.ITEM_SIZE * (Verticality.enabled() ? 1 : 0));
            // y
            args.set(2, y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT));
        } else if (Verticality.isMainArmLeft() && Verticality.enabled()) {
            args.set(1, (int) Math.round(x - 2 * ((x + 8) - Verticality.width() / 2.0))); // Revert the x-coordinate of the item
        }

        drawingOffhandItem = true;
    }

    @Unique
    private boolean drawingOffhandItem = false;

    @Inject(
            method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getBobbingAnimationTime()I"
            )
    )
    private void drawItemPre(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.translateIcon(context, y, false, drawingOffhandItem && Verticality.alternativeLayoutPartiallyEnabled());
    }

    @Inject(
            method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
            )
    )
    private void drawItemPost(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
            )
    )
    private void drawItemInSlotPre(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.translateIcon(context, y, false, drawingOffhandItem && Verticality.alternativeLayoutPartiallyEnabled());

        drawingOffhandItem = false;
    }

    @Inject(
            method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void drawItemInSlotPost(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;"
            )
    )
    private void drawAttackIndicatorPre(float tickDelta, DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();

        Verticality.compatibleWithRaised(context);
        if (Verticality.enabled() && !Verticality.alternativeLayoutEnabled())
            context.getMatrices().translate(-Verticality.raisedHudShift(), 0, 0);
        if (Verticality.alternativeLayoutEnabled() && Verticality.isMainArmLeft())
            context.getMatrices().translate(Verticality.HOTBAR_WIDTH + 28, 0, 0);

        context.getMatrices().translate(
                Verticality.enabled()
                        ? Verticality.alternativeLayoutEnabled()
                        ? -(Verticality.width() + Verticality.HOTBAR_WIDTH) / 2.0 + (Verticality.HOTBAR_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT + Verticality.HOTBAR_FULL_HEIGHT) + Verticality.raisedHudShiftEdge()
                        : 0
                        : Verticality.alternativeLayoutOffset().x(),
                -Verticality.raisedHudShiftEdge(), 0
        );
    }

    @Inject(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"
            ),
            remap = false
    )
    private void drawAttackIndicatorPost(float tickDelta, DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
