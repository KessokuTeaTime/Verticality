package net.krlite.verticality.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.equator.math.algebra.Theory;
import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        double progress = Math.pow(Verticality.progress(), 2), alternativeProgress = Math.pow(Verticality.alternativeProgress(), 2);

        int alphaHovered = 255, alpha = 75;
        Color colorEnabled = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetEnabled(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));
        Color colorUpsideDown = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetUpsideDown(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));
        Color colorAlternative = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetAlternativeMode(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));

        if (Verticality.enabled()) {
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();

            context.getMatrices().push();
            context.getMatrices().translate(Verticality.raisedShift(), 0, 0);

            // 'Disable' widget
            widgetDisable:
            {
                context.getMatrices().push();
                context.getMatrices().translate(
                        13 * (1 - Verticality.transition()),
                        Theory.lerp(height / 2.0F, height - (22 + 13 * (1 - Verticality.transition())), Verticality.alternativeProgress()),
                        0
                );
                context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
                context.getMatrices().translate(0, Verticality.FONT_GAP_OFFSET, 0);

                context.drawText(
                        textRenderer, right,
                        (int) (-textRenderer.getWidth(right) / 2.0F * (float) (1 - Verticality.transition())),
                        (int) (-(textRenderer.fontHeight - 1) / 2.0F),
                        colorEnabled.getRGB(), false
                );

                context.getMatrices().pop();
            }

            // 'Upside Down'
            widgetUpsideDown:
            {
                context.getMatrices().push();
                context.getMatrices().translate(
                        13 + 17 * (1 - Verticality.transition()),
                        Theory.lerp(height / 2.0F, height - (22 + 13 * (1 - Verticality.transition())), Verticality.alternativeProgress()),
                        500
                );
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
                context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (180 * Verticality.swap())));

                context.drawText(
                        textRenderer, both,
                        (int) (-textRenderer.getWidth(both) / 2.0F),
                        (int) (-textRenderer.fontHeight / 2.0F * (float) (1 - Verticality.transition())),
                        colorUpsideDown.getRGB(),
                        false
                );

                context.getMatrices().pop();
            }

            // 'Alternative Mode'
            widgetAlternative:
            {
                context.getMatrices().push();
                context.getMatrices().translate(
                        13 * (1 - Verticality.transition()),
                        height - 13,
                        500
                );
                context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
                context.getMatrices().translate(0, Verticality.FONT_GAP_OFFSET, 0);
                context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (180 * alternativeProgress)));

                context.drawText(
                        textRenderer, alternativeMode,
                        (int) (-textRenderer.getWidth(alternativeMode) / 2.0F * (float) (1 - Verticality.transition())),
                        (int) (-(textRenderer.fontHeight - 1) / 2.0F),
                        colorAlternative.getRGB(), false
                );

                context.getMatrices().pop();
            }

            context.getMatrices().pop();

            RenderSystem.enableCull();
        } else {
            // 'Enable'
            context.getMatrices().push();
            context.getMatrices().translate(0, -Verticality.raisedShift(), 0);
            context.getMatrices().translate(
                    width / 2.0F,
                    height - 10 * (1 - Verticality.transition()),
                    0
            );
            context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
            context.getMatrices().translate(-Verticality.FONT_GAP_OFFSET, 0, 0);

            context.drawText(
                    textRenderer, left,
                    (int) (-textRenderer.getWidth(left) / 2.0F),
                    (int) (-textRenderer.fontHeight / 2.0F * (float) (1 - Verticality.transition())),
                    colorEnabled.getRGB(), false
            );

            context.getMatrices().pop();
        }
    }

    @Unique
    private boolean mouseIn(double mouseX, double mouseY, double xCentered, double yCentered, double w, double h) {
        return mouseX >= xCentered - w / 2 && mouseX <= xCentered + w / 2 && mouseY >= yCentered - h / 2 && mouseY <= yCentered + h / 2;
    }    @Unique
    private static final Text
            left = Text.of("<"),
            right = Text.of(">"),
            both = left.copy().append(" ").append(right),
            alternativeMode = Text.of("↓");

    @Unique
    private boolean mouseInWidgetEnabled(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        if (Verticality.enabled()) {
            // 'Disable'
            return mouseIn(
                    mouseX, mouseY,
                    13 + Verticality.raisedShift(),
                    Verticality.alternativeLayoutEnabled() ? height - (22 + 13 + Verticality.raisedShift()) : height / 2.0F,
                    textRenderer.getWidth(right) * Verticality.SCALAR,
                    textRenderer.fontHeight * Verticality.SCALAR
            );
        } else {
            // 'Enable'
            return mouseIn(
                    mouseX, mouseY,
                    width / 2.0F,
                    height - (10 + Verticality.raisedShift()),
                    textRenderer.getWidth(left) * Verticality.SCALAR,
                    textRenderer.fontHeight * Verticality.SCALAR
            );
        }
    }

    @Unique
    private boolean mouseInWidgetAlternativeMode(double mouseX, double mouseY) {
        if (Verticality.unavailable() || !Verticality.enabled()) return false;

        return mouseIn(
                mouseX, mouseY,
                13 + Verticality.raisedShift(),
                height - (13 + Verticality.raisedShift()),
                textRenderer.fontHeight,
                textRenderer.getWidth(alternativeMode)
        );
    }

    @Unique
    private boolean mouseInWidgetUpsideDown(double mouseX, double mouseY) {
        if (Verticality.unavailable() || !Verticality.enabled())
            return false;

        return mouseIn(
                mouseX, mouseY,
                30 + Verticality.raisedShift(),
                Verticality.alternativeLayoutEnabled() ? height - (22 + 13 + Verticality.raisedShift()) : height / 2.0F,
                textRenderer.fontHeight,
                textRenderer.getWidth(both)
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseInWidgetEnabled(mouseX, mouseY)) {
            Verticality.Sounds.playGateLatch();
            Verticality.switchEnabled();
        }

        if (mouseInWidgetAlternativeMode(mouseX, mouseY)) {
            Verticality.Sounds.playGateLatch();
            Verticality.switchAlternativeMode();
        }

        if (mouseInWidgetUpsideDown(mouseX, mouseY)) {
            Verticality.Sounds.playLightSwitch();
            Verticality.switchUpsideDown();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }




}
