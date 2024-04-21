package band.kessokuteatime.verticality;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;

public record UIRect(double x, double y, double width, double height, Identifier ui) {
    public static UIRect fromCenter(double x, double y, double width, double height, Identifier ui) {
        return new UIRect(x - width / 2, y - height / 2, width, height, ui);
    }

    public Vector2d center() {
        return new Vector2d(x() + width() / 2, y() + height() / 2);
    }

    public UIRect x(double x) {
        return new UIRect(x, y(), width(), height(), ui());
    }

    public UIRect y(double y) {
        return new UIRect(x(), y, width(), height(), ui());
    }

    public UIRect shift(double x, double y) {
        return x(x() + x).y(y() + y);
    }

    public UIRect scaleFromCenter(double scalar) {
        return UIRect.fromCenter(center().x(), center().y(), width() * scalar, height() * scalar, ui());
    }

    public boolean contains(double x, double y) {
        return x >= x() && x <= x() + width() && y >= y() && y <= y() + height();
    }

    public void translateToCenter(MatrixStack matrixStack, double z) {
        matrixStack.translate(center().x(), center().y(), z);
    }

    public void drawAtZero(DrawContext context, float alpha) {
        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        RenderSystem.enableBlend();

        context.setShaderColor(1, 1, 1, alpha);
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderTexture(0, ui());
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        float xMin = (float) -width() / 2, yMin = (float) -height() / 2;
        float xMax = (float) width() / 2, yMax = (float) height() / 2;

        bufferBuilder.vertex(matrix4f, xMin, yMin, 0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, xMin, yMax, 0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, xMax, yMax, 0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, xMax, yMin, 0).texture(1, 0).next();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (!blendWasEnabled) RenderSystem.disableBlend();
    }
}
