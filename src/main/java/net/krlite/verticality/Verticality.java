package net.krlite.verticality;

import dev.yurisuika.raised.Raised;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.krlite.equator.math.algebra.Curves;
import net.krlite.equator.math.algebra.Theory;
import net.krlite.equator.visual.animation.Slice;
import net.krlite.equator.visual.animation.animated.AnimatedDouble;
import net.krlite.equator.visual.animation.interpolated.InterpolatedDouble;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class Verticality implements ModInitializer {
	public static final String NAME = "Verticality", ID = "verticality";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final int HOTBAR_HEIGHT = 23, SPECTATOR_BAR_HEIGHT = 22, OFFHAND_WIDTH = 29, CENTER_DISTANCE_TO_BORDER = 11, TOOLTIP_OFFSET = 7;
	public static final float SCALAR = 1.5F, FONT_GAP_OFFSET = 0.5F;

	public static class Sounds {
		public static final SoundEvent GATE_LATCH = new SoundEvent(new Identifier(ID, "gate_latch"));
		public static final SoundEvent LIGHT_SWITCH = new SoundEvent(new Identifier(ID, "light_switch"));

		static void register() {
			Registry.register(Registry.SOUND_EVENT, GATE_LATCH.getId(), GATE_LATCH);
			Registry.register(Registry.SOUND_EVENT, LIGHT_SWITCH.getId(), LIGHT_SWITCH);
		}

		public static void playGateLatch() {
			play(GATE_LATCH);
		}

		public static void playLightSwitch() {
			play(LIGHT_SWITCH);
		}

		private static void play(SoundEvent sound) {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F));
		}
	}

	private static final HotbarPreferences PREFERENCES = new HotbarPreferences();
	private static final AnimatedDouble
			hotbar = new AnimatedDouble(1, 0, 450, Curves.Back.OUT.reverse()),
			chat = new AnimatedDouble(1, 0, 710, Curves.Back.ease(3.75).reverse());
	private static final InterpolatedDouble
			offset = new InterpolatedDouble(0, 0.013),
			swap = new InterpolatedDouble(0, 0.015);
	private static final InterpolatedDouble
			later = new InterpolatedDouble(0, 0.013),
			earlier = new InterpolatedDouble(0, 0.013);
	private static boolean enabled;
	private static float spectatorMenuHeight = 0;
	private static Supplier<Integer> raisedShift = () -> 0;

	static {
		hotbar.onPlay(() -> {
			hotbar.slice(Slice::reverse);
			chat.play();
		});

		hotbar.onTermination(() -> {
			if (notCompleted()) {
				PREFERENCES.enabled(enabled);
				hotbar.play();
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				// Positive (offset)	: moving downwards
				// Zero (offset)		: no offset
				// Negative (offset)	: moving upwards
				if (!client.player.getOffHandStack().isEmpty()) {
					if (client.options.getMainArm().getValue().getOpposite() == Arm.LEFT) {
						offset.target(upsideDown() ? -1 : 1);
					}
					else offset.target(upsideDown() ? 1 : -1);
				}
				else offset.target(0);

				// Positive (swap)		: enable swap
				// Zero (swap)			: disable swap
				if (client.options.getMainArm().getValue().getOpposite() == Arm.LEFT) {
					swap.target(upsideDown() ? 1 : 0);
				}
				else swap.target(upsideDown() ? 0 : 1);
			}

			later.target(fullyEnabled() ? 1 : 0);
			earlier.target(fullyDisabled() ? 0 : 1);
		});

		hotbar.play();
		chat.play();
		enabled = PREFERENCES.enabled();
	}

	@Override
	public void onInitialize() {
		PREFERENCES.save();
		Sounds.register();

		if (FabricLoader.getInstance().isModLoaded("raised")) {
			raisedShift = Raised::getHud;
		}
	}

	public static int raisedShift() {
		return raisedShift.get();
	}

	public static int height() {
		return MinecraftClient.getInstance().getWindow().getScaledHeight();
	}

	public static int width() {
		return MinecraftClient.getInstance().getWindow().getScaledWidth();
	}

	public static double hotbar() {
		return hotbar.value();
	}

	public static double progress() {
		return notCompleted() ? hotbar.progress() : (1 - hotbar.progress());
	}

	public static double chat() {
		return enabled ? chat.value() : 1 - chat.value();
	}

	public static double offset() {
		return offset.value();
	}

	public static double swap() {
		return swap.value();
	}

	public static double later() {
		return later.value();
	}

	public static double earlier() {
		return earlier.value();
	}

	public static boolean notCompleted() {
		return PREFERENCES.enabled() != enabled;
	}

	public static boolean unavailable() {
		return !hotbar.isCompleted();
	}

	public static boolean enabled() {
		return PREFERENCES.enabled();
	}

	public static boolean fullyEnabled() {
		return enabled() && enabled;
	}

	public static boolean fullyDisabled() {
		return !enabled() && !enabled;
	}

	public static boolean upsideDown() {
		return PREFERENCES.upsideDown();
	}

	public static boolean isSpectator() {
		return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isSpectator();
	}

	public static float spectatorMenuHeight() {
		return spectatorMenuHeight;
	}

	public static void spectatorMenuHeight(float height) {
		spectatorMenuHeight = height;
	}

	public static void switchEnabled() {
		enabled = !enabled;
		hotbar.play();
	}

	public static void switchUpsideDown() {
		PREFERENCES.switchUpsideDown();
	}

	public static void translateIcon(MatrixStack matrixStack, double y, boolean ignoreOffhand) {
		if (enabled()) {
			double offset = -2 * ((y + 8) - height() / 2.0);
			matrixStack.translate(
					-HOTBAR_HEIGHT * hotbar(),
					Theory.lerp(0, offset, swap()) + (ignoreOffhand ? 0 : (OFFHAND_WIDTH * offset() / 2)),
					0
			);
		}
		else {
			matrixStack.translate(0, HOTBAR_HEIGHT * hotbar(), 0);
		}
	}

	public static void drawSelectedSlot(DrawableHelper drawableHelper, MatrixStack matrixStack, int x, int y, int u, int v, int width, int height) {
		if (enabled()) {
			double offset = -2 * ((x + width / 2.0) - (int) (width() / 2.0));

			matrixStack.push();
			matrixStack.translate(
					Theory.lerp(0, offset, swap()),
					0, 0
			);
		}

		drawableHelper.drawTexture(matrixStack, x, y, u, v, width, height);

		if (enabled()) {
			matrixStack.pop();
		}
	}
}
