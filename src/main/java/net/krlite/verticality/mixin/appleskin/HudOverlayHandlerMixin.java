package net.krlite.verticality.mixin.appleskin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.class)
public class HudOverlayHandlerMixin {
    @Inject(
            method = "onRender",
            at = @At("HEAD")
    )
    private void renderHudOverlayPre(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0, Verticality.hotbarShift() * Verticality.later(), 0);
    }

    @Inject(
            method = "onRender",
            at = @At("RETURN")
    )
    private void renderHudOverlayPost(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
