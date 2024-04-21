package band.kessokuteatime.verticality.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DrawContext.class)
public interface DrawContextInvoker {
    @Invoker("drawTexturedQuad")
    public void invokeDrawTexturedQuad(
            Identifier texture,
            int xBegin, int xEnd,
            int yBegin, int yEnd,
            int z,
            float uBegin, float uEnd,
            float vBegin, float vEnd
    );
}
