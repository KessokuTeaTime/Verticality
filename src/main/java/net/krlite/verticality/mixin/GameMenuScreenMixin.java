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

    @Unique
    private static final Text
            leftArrow = Text.of("<"),
            rightArrow = Text.of(">"),
            upDownArrow = Text.of("|"),
            alternativeLayoutArrowWhenEnabled = Text.of("↓"),
            alternativeLayoutArrowWhenDisabled = Text.of("←");

    @Unique
    private static Text alternativeLayoutArrow() {
        return Verticality.enabled() ? alternativeLayoutArrowWhenEnabled : alternativeLayoutArrowWhenDisabled;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        double progress = Math.pow(Verticality.progress(), 2);

        int alphaHovered = 255, alpha = 75;
        Color colorEnabled = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetEnabled(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));
        Color colorUpsideDown = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetUpsideDown(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));
        Color colorAlternativeLayout = new Color(255, 255, 255,
                (int) Math.max(5, (mouseInWidgetAlternativeLayout(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        context.getMatrices().push();
        context.getMatrices().translate(
                Verticality.enabled() ? Verticality.raisedShift() : 0,
                Verticality.enabled() ? 0 : -Verticality.raisedShift(),
                0
        );

        if (Verticality.enabled()) {
            // 'Disable' widget
            widgetDisable:
            {
                context.getMatrices().push();
                context.getMatrices().translate(
                        13 * (1 - Verticality.transition()),
                        Theory.lerp(height / 2.0F, height - (31 + 13), Verticality.alternativeTransition()),
                        0
                );
                context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
                context.getMatrices().translate(0, Verticality.FONT_GAP_OFFSET, 0);

                context.drawText(
                        textRenderer, rightArrow,
                        (int) (-textRenderer.getWidth(rightArrow) / 2.0F * (1 - Verticality.transition())),
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
                        13 + 15 * (1 - Verticality.transition()),
                        Theory.lerp(height / 2.0F, height - 13, Verticality.alternativeTransition()),
                        500
                );
                context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
                context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (180 * Verticality.swap())));

                context.getMatrices().translate(0, Verticality.FONT_GAP_OFFSET, 0);

                context.drawText(
                        textRenderer, upDownArrow,
                        (int) (-textRenderer.getWidth(upDownArrow) / 2.0F),
                        (int) (-textRenderer.fontHeight / 2.0F * (1 - Verticality.transition())),
                        colorUpsideDown.getRGB(),
                        false
                );

                context.getMatrices().pop();
            }
        } else {
            // 'Enable'
            context.getMatrices().push();
            context.getMatrices().translate(
                    Theory.lerp(width / 2.0F, 31 + 13, Verticality.alternativeTransition()),
                    height - 10 * (1 - Verticality.transition()),
                    0
            );
            context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
            context.getMatrices().translate(-Verticality.FONT_GAP_OFFSET, 0, 0);

            context.drawText(
                    textRenderer, leftArrow,
                    (int) (-textRenderer.getWidth(leftArrow) / 2.0F),
                    (int) (-textRenderer.fontHeight / 2.0F * (1 - Verticality.transition())),
                    colorEnabled.getRGB(), false
            );

            context.getMatrices().pop();
        }

        // 'Alternative Layout'
        widgetAlternativeLayout:
        {
            context.getMatrices().push();
            context.getMatrices().translate(
                    Verticality.enabled() ? 13 * (1 - Verticality.transition()) : 10,
                    height - (Verticality.enabled() ? 13 : (10 * (1 - Verticality.transition()))),
                    500
            );
            context.getMatrices().scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
            context.getMatrices().multiply((Verticality.enabled() ? RotationAxis.POSITIVE_X : RotationAxis.POSITIVE_Y)
                    .rotationDegrees((float) (180 * Verticality.alternativeTransition())));

            context.getMatrices().translate(
                    Verticality.enabled() ? 0 : Verticality.FONT_GAP_OFFSET,
                    Verticality.enabled() ? Verticality.FONT_GAP_OFFSET : 0,
                    0
            );

            context.drawText(
                    textRenderer, alternativeLayoutArrow(),
                    (int) (-textRenderer.getWidth(alternativeLayoutArrow()) / 2.0F),
                    (int) (-textRenderer.fontHeight / 2.0F),
                    colorAlternativeLayout.getRGB(), false
            );

            context.getMatrices().pop();
        }

        context.getMatrices().pop();

        RenderSystem.enableCull();
    }

    @Unique
    private boolean mouseIn(double mouseX, double mouseY, double xCentered, double yCentered, double w, double h) {
        return mouseX >= xCentered - w / 2 && mouseX <= xCentered + w / 2 && mouseY >= yCentered - h / 2 && mouseY <= yCentered + h / 2;
    }

    @Unique
    private boolean mouseInWidgetEnabled(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        if (Verticality.enabled()) {
            // 'Disable'
            return mouseIn(
                    mouseX, mouseY,
                    13 + Verticality.raisedShift(),
                    Verticality.alternativeLayoutEnabled() ? height - (31 + 13) : height / 2.0F + Verticality.FONT_GAP_OFFSET,
                    textRenderer.getWidth(rightArrow) * Verticality.SCALAR,
                    textRenderer.fontHeight * Verticality.SCALAR
            );
        } else {
            // 'Enable'
            return mouseIn(
                    mouseX, mouseY,
                    Verticality.alternativeLayoutEnabled() ? 31 + 13 : width / 2.0F - Verticality.FONT_GAP_OFFSET,
                    height - (10 + Verticality.raisedShift()),
                    textRenderer.getWidth(leftArrow) * Verticality.SCALAR,
                    textRenderer.fontHeight * Verticality.SCALAR
            );
        }
    }

    @Unique
    private boolean mouseInWidgetAlternativeLayout(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        return mouseIn(
                mouseX, mouseY,
                Verticality.enabled() ? 13 + Verticality.raisedShift() : (10 - Verticality.FONT_GAP_OFFSET),
                height - (Verticality.enabled() ? 13 - Verticality.FONT_GAP_OFFSET : (10 + Verticality.raisedShift())),
                textRenderer.getWidth(alternativeLayoutArrow()) * Verticality.SCALAR,
                textRenderer.fontHeight * Verticality.SCALAR
        );
    }

    @Unique
    private boolean mouseInWidgetUpsideDown(double mouseX, double mouseY) {
        if (Verticality.unavailable() || !Verticality.enabled())
            return false;

        return mouseIn(
                mouseX, mouseY,
                13 + 15 + Verticality.raisedShift(),
                Verticality.alternativeLayoutEnabled() ? height - (13 - Verticality.FONT_GAP_OFFSET) : height / 2.0F,
                textRenderer.getWidth(upDownArrow) * Verticality.SCALAR,
                textRenderer.fontHeight * Verticality.SCALAR
        );
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mouseInWidgetEnabled(mouseX, mouseY)) {
            Verticality.Sounds.playGateLatch();
            Verticality.switchEnabled();
        }

        if (mouseInWidgetAlternativeLayout(mouseX, mouseY)) {
            Verticality.Sounds.playGateLatch();
            Verticality.switchAlternativeMode();
        }

        if (mouseInWidgetUpsideDown(mouseX, mouseY)) {
            Verticality.Sounds.playLightSwitch();
            Verticality.switchUpsideDown();
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
