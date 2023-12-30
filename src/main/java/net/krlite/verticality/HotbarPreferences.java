package net.krlite.verticality;

import net.fabricmc.loader.api.FabricLoader;
import net.krlite.pierced.annotation.Comment;
import net.krlite.pierced.annotation.Silent;
import net.krlite.pierced.config.Pierced;

import java.io.File;

class HotbarPreferences extends Pierced {
	private static final @Silent File file = FabricLoader.getInstance().getConfigDir().resolve(Verticality.ID + ".toml").toFile();

	public HotbarPreferences() {
		super(HotbarPreferences.class, file);
		load();
	}

	@Comment("Whether to verticalize the hotbar")
	private boolean enabled = false;

	public boolean enabled() {
		return enabled;
	}

	public void enabled(boolean enabled) {
		this.enabled = enabled;
		save();
	}

	@Comment("Whether to use the alternative layout")
	@Comment
	@Comment("When enabled, the status bars will be verticalized and placed against the right edge of the hotbar.")
	@Comment("The offhand slot will be placed against the lower corner of the bars and the iconic information will be replaced by plain texts.")
	@Comment("At the same time, status bar functions provided by mods like AppleSkin may no longer work.")
	private boolean alternativeLayout = false;

	public boolean alternativeLayout() {
		return alternativeLayout;
	}

	public void alternativeLayout(boolean alternativeMode) {
		this.alternativeLayout = alternativeMode;
		save();
	}

	@Comment("Whether to flip the vertical hotbar")
	@Comment
	@Comment("When using right hand as the main hand, the offhand slot is at:")
	@Comment("	true  - bottom of the hotbar")
	@Comment("	false - top of the hotbar")
	private boolean upsideDown = false;

	public boolean upsideDown() {
		return upsideDown;
	}

	public void upsideDown(boolean upsideDown) {
		this.upsideDown = upsideDown;
		save();
	}

	public void switchUpsideDown() {
		upsideDown(!upsideDown());
	}
}
