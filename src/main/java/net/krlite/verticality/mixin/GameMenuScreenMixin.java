package net.krlite.verticality.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.equator.math.algebra.Theory;
import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2d;
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

    @Unique
    private Vector2d centerWidgetDisableLeft() {
        return new Vector2d(Verticality.HOTBAR_HEIGHT / 2.0 * (1 - Verticality.transition()), height / 2.0);
    }

    @Unique
    private Vector2d centerWidgetDisableLeftBottom() {
        return new Vector2d(centerWidgetDisableLeft().x(), height - (Verticality.HOTBAR_HEIGHT / 2.0 + Verticality.WIDGET_GAP_LARGE));
    }

    @Unique
    private Vector2d centerWidgetUpsideDownLeft() {
        return new Vector2d(Verticality.HOTBAR_HEIGHT / 2.0 + Verticality.WIDGET_GAP * (1 - Verticality.transition()), height / 2.0);
    }

    @Unique
    private Vector2d centerWidgetUpsideDownLeftBottom() {
        return new Vector2d(centerWidgetUpsideDownLeft().x(), height - Verticality.HOTBAR_HEIGHT / 2.0);
    }

    @Unique
    private Vector2d centerWidgetEnableBottom() {
        return new Vector2d(width / 2.0, height - Verticality.HOTBAR_HEIGHT / 2.0 * (1 - Verticality.transition()));
    }

    @Unique
    private Vector2d centerWidgetEnableLeftBottom() {
        return new Vector2d(Verticality.HOTBAR_HEIGHT / 2.0 + Verticality.WIDGET_GAP_LARGE, centerWidgetEnableBottom().y());
    }

    @Unique
    private Vector2d centerWidgetAlternativeLayoutVertical() {
        return new Vector2d(Verticality.HOTBAR_HEIGHT / 2.0 * (1 - Verticality.transition()), height - Verticality.HOTBAR_HEIGHT / 2.0);
    }

    @Unique
    private Vector2d centerWidgetAlternativeLayoutHorizontal() {
        return new Vector2d(Verticality.HOTBAR_HEIGHT / 2.0, height - Verticality.HOTBAR_HEIGHT / 2.0 * (1 - Verticality.transition()));
    }

    @Unique
    private double fontOffset(double scalar, double progress) {
        return Verticality.FONT_OFFSET * scalar * (1 - progress);
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
                        centerWidgetDisableLeft().x(),
                        Theory.lerp(centerWidgetDisableLeft().y(), centerWidgetDisableLeftBottom().y(), Verticality.alternativeTransition()),
                        0
                );
                context.getMatrices().translate(0, fontOffset(1, 0), 0);

                context.getMatrices().scale(Verticality.FONT_SCALAR, Verticality.FONT_SCALAR, Verticality.FONT_SCALAR);

                context.drawText(
                        textRenderer, rightArrow,
                        (int) (-textRenderer.getWidth(rightArrow) / 2.0F),
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
                        centerWidgetUpsideDownLeft().x(),
                        Theory.lerp(centerWidgetUpsideDownLeft().y(), centerWidgetUpsideDownLeftBottom().y(), Verticality.alternativeTransition()),
                        500
                );
                context.getMatrices().translate(0, fontOffset(1, Verticality.swap()), 0);

                context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (180 * Verticality.swap())));
                context.getMatrices().scale(Verticality.FONT_SCALAR, Verticality.FONT_SCALAR, Verticality.FONT_SCALAR);

                context.drawText(
                        textRenderer, upDownArrow,
                        (int) (-textRenderer.getWidth(upDownArrow) / 2.0F),
                        (int) (-textRenderer.fontHeight / 2.0F),
                        colorUpsideDown.getRGB(),
                        false
                );

                context.getMatrices().pop();
            }
        } else {
            // 'Enable'
            context.getMatrices().push();

            context.getMatrices().translate(
                    Theory.lerp(centerWidgetEnableBottom().x(), centerWidgetEnableLeftBottom().x(), Verticality.alternativeTransition()),
                    centerWidgetEnableBottom().y(),
                    0
            );
            context.getMatrices().translate(0, fontOffset(1, 0), 0);

            context.getMatrices().scale(Verticality.FONT_SCALAR, Verticality.FONT_SCALAR, Verticality.FONT_SCALAR);

            context.drawText(
                    textRenderer, leftArrow,
                    (int) (-textRenderer.getWidth(leftArrow) / 2.0F),
                    (int) (-textRenderer.fontHeight / 2.0F),
                    colorEnabled.getRGB(), false
            );

            context.getMatrices().pop();
        }

        // 'Alternative Layout'
        widgetAlternativeLayout:
        {
            context.getMatrices().push();
            
            context.getMatrices().translate(
                    Verticality.enabled() ? centerWidgetAlternativeLayoutVertical().x() : centerWidgetAlternativeLayoutHorizontal().x(),
                    Verticality.enabled() ? centerWidgetAlternativeLayoutVertical().y() : centerWidgetAlternativeLayoutHorizontal().y(),
                    500
            );
            context.getMatrices().translate(0, fontOffset(1, Verticality.enabled() ? Verticality.alternativeTransition() : 0), 0);

            context.getMatrices().multiply((Verticality.enabled() ? RotationAxis.POSITIVE_X : RotationAxis.POSITIVE_Y)
                    .rotationDegrees((float) (180 * Verticality.alternativeTransition())));
            context.getMatrices().scale(Verticality.FONT_SCALAR, Verticality.FONT_SCALAR, Verticality.FONT_SCALAR);

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
    private boolean mouseIn(double mouseX, double mouseY, Vector2d center, double width, double height) {
        return mouseX >= center.x() - width / 2
                && mouseX <= center.x() + width / 2
                && mouseY >= center.y() - height / 2
                && mouseY <= center.y() + height / 2;
    }

    @Unique
    private boolean mouseInWidgetEnabled(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        if (Verticality.enabled()) {
            // 'Disable'
            return mouseIn(
                    mouseX, mouseY,
                    (Verticality.alternativeLayoutEnabled()
                            ? centerWidgetDisableLeftBottom()
                            : centerWidgetDisableLeft()).add(Verticality.raisedShift(), 0),
                    textRenderer.getWidth(rightArrow) * Verticality.FONT_SCALAR,
                    textRenderer.fontHeight * Verticality.FONT_SCALAR
            );
        } else {
            // 'Enable'
            return mouseIn(
                    mouseX, mouseY,
                    (Verticality.alternativeLayoutEnabled()
                            ? centerWidgetEnableLeftBottom()
                            : centerWidgetEnableBottom()).add(0, -Verticality.raisedShift()),
                    textRenderer.getWidth(leftArrow) * Verticality.FONT_SCALAR,
                    textRenderer.fontHeight * Verticality.FONT_SCALAR
            );
        }
    }

    @Unique
    private boolean mouseInWidgetAlternativeLayout(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        return mouseIn(
                mouseX, mouseY,
                Verticality.enabled()
                        ? centerWidgetAlternativeLayoutVertical().add(Verticality.raisedShift(), 0)
                        : centerWidgetAlternativeLayoutHorizontal().add(0, -Verticality.raisedShift()),
                textRenderer.getWidth(alternativeLayoutArrow()) * Verticality.FONT_SCALAR,
                textRenderer.fontHeight * Verticality.FONT_SCALAR
        );
    }

    @Unique
    private boolean mouseInWidgetUpsideDown(double mouseX, double mouseY) {
        if (Verticality.unavailable() || !Verticality.enabled())
            return false;

        return mouseIn(
                mouseX, mouseY,
                (Verticality.alternativeLayoutEnabled()
                        ? centerWidgetUpsideDownLeftBottom()
                        : centerWidgetUpsideDownLeft()).add(Verticality.raisedShift(), 0),
                textRenderer.getWidth(upDownArrow) * Verticality.FONT_SCALAR,
                textRenderer.fontHeight * Verticality.FONT_SCALAR
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
