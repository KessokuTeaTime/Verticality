package band.kessokuteatime.verticality.mixin.appleskin;

import band.kessokuteatime.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.class)
public class HudOverlayHandlerMixin {
    @Unique
    private void verticallyShiftBar(DrawContext context) {
        Verticality.verticallyShiftBarPre(context, false);
    }

    @ModifyArg(
            method = "enableAlpha",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"
            ),
            index = 3
    )
    private float fade(float alpha) {
        return (float) (alpha * (1 - Verticality.alternativeTransition()));
    }

    @ModifyArg(
            method = "drawHealthOverlay(Lnet/minecraft/client/gui/DrawContext;FFLnet/minecraft/client/MinecraftClient;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"
            ),
            index = 3
    )
    private float fadeHealthOverlay(float alpha) {
        return (float) (alpha * (1 - Verticality.alternativeTransition()));
    }

    @ModifyArg(
            method = "drawHungerOverlay(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/MinecraftClient;IIFZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"
            ),
            index = 3
    )
    private float fadeHungerOverlay(float alpha) {
        return (float) (alpha * (1 - Verticality.alternativeTransition()));
    }

    @Inject(
            method = "onRender",
            at = @At("HEAD")
    )
    private void renderHudOverlayPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        verticallyShiftBar(context);
    }

    @Inject(
            method = "onRender",
            at = @At("RETURN")
    )
    private void renderHudOverlayPost(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(
            method = "onPreRender",
            at = @At("HEAD")
    )
    private void preRenderHudOverlayPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        verticallyShiftBar(context);
    }

    @Inject(
            method = "onPreRender",
            at = @At("RETURN")
    )
    private void preRenderHudOverlayPost(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
