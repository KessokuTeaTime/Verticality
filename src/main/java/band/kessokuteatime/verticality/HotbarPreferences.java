package band.kessokuteatime.verticality;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "verticality")
class HotbarPreferences implements ConfigData {
	public boolean enabled = false;
	public boolean alternativeLayoutEnabled = false;
	public boolean upsideDownEnabled = false;
	public void switchUpsideDown() {
		upsideDownEnabled = !upsideDownEnabled;
	}
}
