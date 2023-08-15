package net.krlite.verticality.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.krlite.verticality.Verticality;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
	@Unique
	private static final Text left = Text.of("<"), right = Text.of(">"), both = left.copy().append(" ").append(right);

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		double progress = Math.pow(Verticality.progress(), 2);
		int alphaHovered = 255, alpha = 75;
		Color colorEnabled = new Color(255, 255, 255, (int) Math.max(5, (isInEnabled(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));
		Color colorUpsideDown = new Color(255, 255, 255, (int) Math.max(5, (isInUpsideDown(mouseX, mouseY) ? alphaHovered : alpha) * (1 - progress)));

		if (Verticality.enabled()) {
			RenderSystem.enableDepthTest();
			RenderSystem.disableCull();

			matrixStack.push();
			matrixStack.translate(Verticality.raisedShift(), 0, 0);

			// 'Disable' widget
			matrixStack.push();
			matrixStack.translate(13 * (1 - Verticality.hotbar()), height / 2.0F, 0);
			matrixStack.scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
			matrixStack.translate(0, Verticality.FONT_GAP_OFFSET, 0);

			textRenderer.draw(matrixStack, right, -textRenderer.getWidth(right) / 2.0F * (float) (1 - Verticality.hotbar()), -(textRenderer.fontHeight - 1) / 2.0F, colorEnabled.getRGB());

			matrixStack.pop();

			// 'Upside Down' widget
			matrixStack.push();
			matrixStack.translate(13 + 17 * (1 - Verticality.hotbar()), height / 2.0F, 500);
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (180 * Verticality.swap())));

			textRenderer.draw(matrixStack, both, -textRenderer.getWidth(both) / 2.0F, -textRenderer.fontHeight / 2.0F * (float) (1 - Verticality.hotbar()), colorUpsideDown.getRGB());

			matrixStack.pop();

			matrixStack.pop();

			RenderSystem.enableCull();
		}
		else {
			// 'Enable' widget
			matrixStack.push();
			matrixStack.translate(0, -Verticality.raisedShift(), 0);
			matrixStack.translate(width / 2.0F, height - 10 * (1 - Verticality.hotbar()), 0);
			matrixStack.scale(Verticality.SCALAR, Verticality.SCALAR, Verticality.SCALAR);
			matrixStack.translate(-Verticality.FONT_GAP_OFFSET, 0, 0);

			textRenderer.draw(matrixStack, left, -textRenderer.getWidth(left) / 2.0F, -textRenderer.fontHeight / 2.0F * (float) (1 - Verticality.hotbar()), colorEnabled.getRGB());

			matrixStack.pop();
		}
	}

	@Unique
	private boolean isInEnabled(double mouseX, double mouseY) {
		if (Verticality.unavailable()) return false;

		if (Verticality.enabled()) {
			// 'Disable'
			double xCentered = 13 + Verticality.raisedShift(), yCentered = height / 2.0F, w = textRenderer.getWidth(right) * Verticality.SCALAR, h = textRenderer.fontHeight * Verticality.SCALAR;
			return mouseX >= xCentered - w / 2 && mouseX <= xCentered + w / 2 && mouseY >= yCentered - h / 2 && mouseY <= yCentered + h / 2;
		} else {
			// 'Enable'
			double xCentered = width / 2.0F, yCentered = height - 10 - Verticality.raisedShift(), w = textRenderer.getWidth(left) * Verticality.SCALAR, h = textRenderer.fontHeight * Verticality.SCALAR;
			return mouseX >= xCentered - w / 2 && mouseX <= xCentered + w / 2 && mouseY >= yCentered - h / 2 && mouseY <= yCentered + h / 2;
		}
	}

	@Unique
	private boolean isInUpsideDown(double mouseX, double mouseY) {
		if (Verticality.unavailable()) return false;

		// 'Upside Down'
		double xCentered = 30 + Verticality.raisedShift(), yCentered = height / 2.0F, w = textRenderer.fontHeight, h = textRenderer.getWidth(both);
		return mouseX >= xCentered - w / 2 && mouseX <= xCentered + w / 2 && mouseY >= yCentered - h / 2 && mouseY <= yCentered + h / 2;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isInEnabled(mouseX, mouseY)) {
			Verticality.Sounds.playGateLatch();
			Verticality.switchEnabled();
		}

		if (isInUpsideDown(mouseX, mouseY)) {
			Verticality.Sounds.playLightSwitch();
			Verticality.switchUpsideDown();
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}
}
