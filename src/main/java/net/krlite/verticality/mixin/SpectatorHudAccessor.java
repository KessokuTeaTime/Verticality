package net.krlite.verticality.mixin;

import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpectatorHud.class)
public interface SpectatorHudAccessor {
    @Accessor
    SpectatorMenu getSpectatorMenu();
}
