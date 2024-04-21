package band.kessokuteatime.verticality.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.equator.math.algebra.Theory;
import band.kessokuteatime.verticality.UIRect;
import band.kessokuteatime.verticality.Verticality;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private static Identifier uiTexture(String name) {
        return new Identifier(Verticality.ID, "textures/ui/" + name + ".png");
    }

    @Unique
    private static final Identifier
            ENABLE = uiTexture("enable"), DISABLE = uiTexture("disable"), UPSIDE_DOWN = uiTexture("upside_down"),
            ALTERNATIVE_LAYOUT_HORIZONTAL = uiTexture("alternative_layout_horizontal"), ALTERNATIVE_LAYOUT_VERTICAL = uiTexture("alternative_layout_vertical");

    @Unique
    private UIRect widgetDisable() {
        return UIRect.fromCenter(
                widgetAlternativeLayoutVertical().center().x() - 3.25,
                Theory.lerp(Verticality.height() / 2.0, Verticality.height() - Verticality.HOTBAR_WIDTH / 2.0 - Verticality.raisedHudShiftEdge(), Verticality.alternativeTransition()),
                3, 10, DISABLE
        );
    }

    @Unique
    private UIRect widgetUpsideDown() {
        return UIRect.fromCenter(
                widgetAlternativeLayoutVertical().center().x() + 4.25,
                widgetDisable().center().y(),
                2, 10, UPSIDE_DOWN
        );
    }

    @Unique
    private UIRect widgetEnable() {
        return UIRect.fromCenter(
                Theory.lerp(Verticality.width() / 2.0, Verticality.HOTBAR_WIDTH / 2.0, Verticality.alternativeTransition()),
                widgetAlternativeLayoutHorizontal().center().y(),
                3, 10, ENABLE
        );
    }

    @Unique
    private UIRect widgetAlternativeLayoutVertical() {
        return UIRect.fromCenter(
                Verticality.hotbarShift() * (1 - Verticality.transition()) - Verticality.HOTBAR_HEIGHT / 2.0,
                Verticality.height() - Verticality.HOTBAR_HEIGHT / 2.0 - Verticality.raisedHudShiftEdge(),
                10, 3, ALTERNATIVE_LAYOUT_VERTICAL
        );
    }

    @Unique
    private UIRect widgetAlternativeLayoutHorizontal() {
        return UIRect.fromCenter(
                Verticality.HOTBAR_HEIGHT / 2.0 + Verticality.raisedHudShiftEdge(),
                Verticality.height() - (Verticality.hotbarShift() * (1 - Verticality.transition()) - Verticality.HOTBAR_HEIGHT / 2.0),
                3, 10, ALTERNATIVE_LAYOUT_HORIZONTAL
        );
    }

    @Unique
    private UIRect widgetAlternativeLayout() {
        return Verticality.enabled() ? widgetAlternativeLayoutVertical() : widgetAlternativeLayoutHorizontal();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        double progress = Math.pow(Verticality.progress(), 2);
        float alphaHovered = 1, alpha = 0.3F;

        float
                alphaEnable = (float) ((mouseInWidgetEnable(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)),
                alphaUpsideDown = (float) ((mouseInWidgetUpsideDown(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)),
                alphaAlternativeLayout = (float) ((mouseInWidgetAlternativeLayout(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress));

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        context.getMatrices().push();

        if (Verticality.enabled()) {
            // 'Disable'
            widgetDisable:
            {
                context.getMatrices().push();

                widgetDisable().translateToCenter(context.getMatrices(), 0);
                context.getMatrices().scale(Verticality.UI_SCALAR, Verticality.UI_SCALAR, Verticality.UI_SCALAR);

                widgetDisable().drawAtZero(context, alphaEnable);

                context.getMatrices().pop();
            }

            // 'Upside Down'
            widgetUpsideDown:
            {
                context.getMatrices().push();

                widgetUpsideDown().translateToCenter(context.getMatrices(), 500);
                context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (180 * Verticality.swap())));
                context.getMatrices().scale(Verticality.UI_SCALAR, Verticality.UI_SCALAR, Verticality.UI_SCALAR);

                widgetUpsideDown().drawAtZero(context, alphaUpsideDown);

                context.getMatrices().pop();
            }
        } else {
            // 'Enable'
            context.getMatrices().push();

            widgetEnable().translateToCenter(context.getMatrices(), 0);
            context.getMatrices().scale(Verticality.UI_SCALAR, Verticality.UI_SCALAR, Verticality.UI_SCALAR);

            widgetEnable().drawAtZero(context, alphaEnable);

            context.getMatrices().pop();
        }

        // 'Alternative Layout'
        widgetAlternativeLayout:
        {
            context.getMatrices().push();
            
            widgetAlternativeLayout().translateToCenter(context.getMatrices(), 500);

            context.getMatrices().multiply((Verticality.enabled() ? RotationAxis.POSITIVE_X : RotationAxis.POSITIVE_Y)
                    .rotationDegrees((float) (180 * Verticality.alternativeTransition())));
            context.getMatrices().scale(Verticality.UI_SCALAR, Verticality.UI_SCALAR, Verticality.UI_SCALAR);

            widgetAlternativeLayout().drawAtZero(context, alphaAlternativeLayout);

            context.getMatrices().pop();
        }

        context.getMatrices().pop();

        RenderSystem.enableCull();
    }

    @Unique
    private boolean mouseInWidgetEnable(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        return (Verticality.enabled() ? widgetDisable() : widgetEnable())
                .scaleFromCenter(Verticality.UI_SCALAR)
                .contains(mouseX, mouseY);
    }

    @Unique
    private boolean mouseInWidgetAlternativeLayout(double mouseX, double mouseY) {
        if (Verticality.unavailable()) return false;

        return widgetAlternativeLayout()
                .scaleFromCenter(Verticality.UI_SCALAR)
                .contains(mouseX, mouseY);
    }

    @Unique
    private boolean mouseInWidgetUpsideDown(double mouseX, double mouseY) {
        if (Verticality.unavailable() || !Verticality.enabled())
            return false;

        return widgetUpsideDown()
                .scaleFromCenter(Verticality.UI_SCALAR)
                .contains(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mouseInWidgetEnable(mouseX, mouseY)) {
            Verticality.Sounds.play(Verticality.Sounds.GATE_LATCH);
            Verticality.switchEnabled();
        }

        if (mouseInWidgetAlternativeLayout(mouseX, mouseY)) {
            Verticality.Sounds.play(Verticality.Sounds.GATE_LATCH);
            Verticality.switchAlternativeMode();
        }

        if (mouseInWidgetUpsideDown(mouseX, mouseY)) {
            Verticality.Sounds.play(Verticality.Sounds.LIGHT_SWITCH);
            Verticality.switchUpsideDown();
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
