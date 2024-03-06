package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.JumpingMount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudBarAdjustor {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTextBackground(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;III)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private void renderOverlayMessage(DrawContext context, float tickDelta, CallbackInfo ci) {
        context.getMatrices().translate(
                0,
                Verticality.hotbarShift() * Verticality.later() + (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP) * Verticality.alternativeTransition(),
                0
        );
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private void renderStatusBarsPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, true);
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderStatusBarsPost(DrawContext context, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"
            )
    )
    private void renderHealthBarPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, true);
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderHealthBarPost(DrawContext context, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderMountHealth",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private void renderMountHealthPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, true);
    }

    @Inject(
            method = "renderMountHealth",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderMountHealthPost(DrawContext context, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderMountJumpBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private void renderMountJumpBarBackgroundPre(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, false);

        context.getMatrices().translate(Verticality.alternativeLayoutOffset().x(), 0, 0);
    }

    @Inject(
            method = "renderMountJumpBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderMountJumpBarBackgroundPost(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderMountJumpBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"
            )
    )
    private void renderMountJumpBarPre(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, false);

        context.getMatrices().translate(Verticality.alternativeLayoutOffset().x(), 0, 0);
    }

    @Inject(
            method = "renderMountJumpBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderMountJumpBarPost(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private void renderExperienceBarBackgroundPre(DrawContext context, int x, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, false);

        context.getMatrices().translate(Verticality.alternativeLayoutOffset().x(), 0, 0);
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderExperienceBarBackgroundPost(DrawContext context, int x, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"
            )
    )
    private void renderExperienceBarProgressPre(DrawContext context, int x, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, false);

        context.getMatrices().translate(Verticality.alternativeLayoutOffset().x(), 0, 0);
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderExperienceBarProgressPost(DrawContext context, int x, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            )
    )
    private void renderExperienceBarTextPre(DrawContext context, int x, CallbackInfo ci) {
        context.getMatrices().push();
        Verticality.verticallyShiftBarPre(context, true);
    }

    @Redirect(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            )
    )
    // In compatibility with ImmediatelyFast
    private int renderExperienceBarText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
        if (!Verticality.alternativeLayoutPartiallyEnabled())
            return context.drawText(textRenderer, text, x, y, color, shadow);
        return 0;
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I",
                    shift = At.Shift.AFTER
            )
    )
    private void renderExperienceBarTextPost(DrawContext context, int x, CallbackInfo ci) {
        Verticality.verticallyShiftBarPost(context);
        context.getMatrices().pop();
    }

    @Inject(
            method = "renderHeldItemTooltip",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
            )
    )
    private void renderHeldItemTooltipPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(
                0,
                Verticality.hotbarShift() * Verticality.later(),
                0
        );
    }

    @Inject(
            method = "renderHeldItemTooltip",
            at = @At(
                    value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
                    shift = At.Shift.AFTER
            )
    )
    private void renderHeldItemTooltipPost(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void fixHelpItemTooltipPre(DrawContext context, CallbackInfo ci) {
        if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, Verticality.TOOLTIP_OFFSET * Verticality.later(), 0);
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", shift = At.Shift.AFTER))
    private void fixHelpItemTooltipPost(DrawContext context, CallbackInfo ci) {
        if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
            context.getMatrices().pop();
        }
    }
}
