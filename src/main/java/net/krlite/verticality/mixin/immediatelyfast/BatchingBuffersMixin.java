package net.krlite.verticality.mixin.immediatelyfast;

import net.krlite.verticality.Verticality;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BatchingBuffers.class)
public class BatchingBuffersMixin {
    @Redirect(
            method = "endHudBatching(Lnet/raphimc/immediatelyfast/feature/core/BatchableImmediate;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/raphimc/immediatelyfast/feature/core/BatchableImmediate;draw()V"
            )
    )
    private static void omitBatching(BatchableImmediate batch) {
        if (!Verticality.omitImmediatelyFastBatching()) batch.draw();
        Verticality.omitImmediatelyFastBatching(false);
    }
}
