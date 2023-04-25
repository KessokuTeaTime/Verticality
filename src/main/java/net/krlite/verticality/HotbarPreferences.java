package net.krlite.verticality;

import net.fabricmc.loader.api.FabricLoader;
import net.krlite.pierced.annotation.Comment;
import net.krlite.pierced.annotation.Silent;
import net.krlite.pierced.config.Pierced;

import java.io.File;

class HotbarPreferences extends Pierced {
	public HotbarPreferences() {
		super(HotbarPreferences.class, config);
		load();
	}

	@Comment("Whether or not the vertical hotbar is enabled")
	private boolean enabled = false;

	@Comment("Whether or not the vertical hotbar is upside-down")
	@Comment("Making the hotbar more customizable")
	@Comment
	@Comment("Offhand slot at:")
	@Comment("	Main hand right	- false: top,		true: bottom")
	@Comment("	Main hand left	- false: bottom,	true: top")
	private boolean upsideDown = false;

	private static final @Silent File config = FabricLoader.getInstance().getConfigDir().resolve("verticality.toml").toFile();

	public boolean enabled() {
		return enabled;
	}

	public boolean upsideDown() {
		return upsideDown;
	}

	public void enabled(boolean enabled) {
		this.enabled = enabled;
		save();
	}

	public void upsideDown(boolean upsideDown) {
		this.upsideDown = upsideDown;
		save();
	}

	public void switchUpsideDown() {
		upsideDown(!upsideDown());
	}
}
