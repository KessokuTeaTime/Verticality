package band.kessokuteatime.verticality.mixin;

import band.kessokuteatime.verticality.Verticality;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private Random random;
	@Shadow private long heartJumpEndTick;
	@Shadow private int ticks;

	@Shadow @Final private static Identifier WIDGETS_TEXTURE;
	@Shadow @Final private static Identifier ICONS;



	@Unique
	private static void drawHotbarOffhandLeftTexture(DrawContext context, int x, int y) {
		context.drawTexture(WIDGETS_TEXTURE, x, y, 24, 22, 29, 24);
	}

	@Unique
	private static void drawHotbarOffhandRightTexture(DrawContext context, int x, int y) {
		context.drawTexture(WIDGETS_TEXTURE, x, y, 53, 22, 29, 24);
	}



	@Unique
	private static void drawArmorFullTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 25 + 9, 9, 9, 9);
	}

	@Unique
	private static void drawArmorHalfTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 25, 9, 9, 9);
	}



	@Unique
	private static void drawFoodEmptyHungerTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9 * 13, 27, 9, 9);
	}

	@Unique
	private static void drawFoodHalfHungerTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9 * 8, 27, 9, 9);
	}

	@Unique
	private static void drawFoodFullHungerTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9 * 9, 27, 9, 9);
	}

	@Unique
	private static void drawFoodEmptyTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16, 27, 9, 9);
	}

	@Unique
	private static void drawFoodHalfTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9 * 4, 27, 9, 9);
	}

	@Unique
	private static void drawFoodFullTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9 * 5, 27, 9, 9);
	}



	@Unique
	private static void drawVehicleContainerHeartTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 52, 9, 9, 9);
	}

	@Unique
	private static void drawVehicleFullHeartTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 88, 9, 9, 9);
	}

	@Unique
	private static void drawVehicleHalfHeartTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 88 + 9, 9, 9, 9);
	}



	@Unique
	private static void drawAirTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16, 18, 9, 9);
	}

	@Unique
	private static void drawAirBurstingTexture(DrawContext context, int x, int y) {
		context.drawTexture(ICONS, x, y, 16 + 9, 18, 9, 9);
	}



	@Shadow public abstract TextRenderer getTextRenderer();
	@Shadow protected abstract int getHeartCount(LivingEntity entity);
	@Shadow protected abstract LivingEntity getRiddenEntity();
	@Shadow protected abstract void drawHeart(DrawContext drawContext, InGameHud.HeartType heartType, int x, int y, int v, boolean blinking, boolean half);

	@Shadow private int renderHealthValue;

	@Unique int stackedInfo = 0;

	@Unique
	private void drawInfo(
			DrawContext context, int number,
			BiConsumer<DrawContext, Vector2i> iconDrawer,
			TriConsumer<DrawContext, Vector2i, Text> textDrawer,
			boolean hasIcon, boolean stickToTail
	) {
		Text text = Text.of(String.valueOf(number));
		int
				textWidth = getTextRenderer().getWidth(text), totalWidth = textWidth + (hasIcon ? Verticality.GAP + 9 : 0), width = Math.min(totalWidth, Verticality.INFO_MAX_WIDTH),
				xBase, yBase, xOffset, yOffset;

		if (stickToTail) {
			xBase = Verticality.enabled()
					? Verticality.HOTBAR_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT + 1
					: (Verticality.width() + Verticality.HOTBAR_WIDTH) / 2;
			yBase = Verticality.enabled()
					? (Verticality.height() - Verticality.HOTBAR_WIDTH) / 2
					: Verticality.height() - (Verticality.HOTBAR_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT + Verticality.INFO_ICON_SIZE);
			xOffset = Verticality.enabled() ? 0 : -width;
			yOffset = 0;
		} else {
			xBase = Verticality.enabled()
					? Verticality.HOTBAR_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT + 1
					: (Verticality.width() - Verticality.HOTBAR_WIDTH) / 2 + (Verticality.isOffhandOccupied() ? Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP : 0);
			yBase = Verticality.enabled()
					? (Verticality.height() + Verticality.HOTBAR_WIDTH) / 2 - (Verticality.isOffhandOccupied() ? Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP : 0) - Verticality.INFO_ICON_SIZE
					: Verticality.height() - (Verticality.HOTBAR_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT + Verticality.GAP + Verticality.INFO_ICON_SIZE);
			xOffset = Verticality.enabled()
					? 0
					: (stackedInfo / 2) * (Verticality.INFO_MAX_WIDTH);
			yOffset = Verticality.enabled()
					? stackedInfo * -(Verticality.GAP + Verticality.INFO_ICON_SIZE)
					: (stackedInfo % 2) * -Verticality.INFO_ICON_SIZE;
		}

		int x = xBase + xOffset, y = yBase + yOffset;

		if (hasIcon) iconDrawer.accept(context, new Vector2i(x, y - 1));
		textDrawer.accept(context, new Vector2i(x + (hasIcon ? Verticality.INFO_ICON_SIZE + Verticality.GAP : 0), y), text);

		stackedInfo += stickToTail ? 0 : 1;
	}

	@Unique
	private void drawBorderedText(DrawContext context, Text text, Vector2i pos, int color) {
		context.drawText(getTextRenderer(), text, pos.x() + 1, pos.y(), 0, false);
		context.drawText(getTextRenderer(), text, pos.x() - 1, pos.y(), 0, false);
		context.drawText(getTextRenderer(), text, pos.x(), pos.y() + 1, 0, false);
		context.drawText(getTextRenderer(), text, pos.x(), pos.y() - 1, 0, false);

		context.drawText(getTextRenderer(), text, pos.x(), pos.y(), color, false);
	}

	@Unique
	private void renderAlternativeLayoutInfo(DrawContext context) {
		stackedInfo = 0;
		int textOffset = Verticality.enabled() ? 1 : -1;
		// Experience level (sticking to tail)
		experienceLevel:
		{
			drawInfo(
					context, Objects.requireNonNull(client.player).experienceLevel,
					(c, pos) -> {},
					(c, pos, text) -> drawBorderedText(c, text, pos.add(textOffset, textOffset), 0x80FF20),
					false, true
			);
		}

		// Health
		health:
		{
			drawInfo(
					context, MathHelper.ceil(MathHelper.ceil(client.player.getHealth()) / 2.0),
					(c, pos) -> {
						boolean blinking = heartJumpEndTick > (long) ticks && (this.heartJumpEndTick - (long) this.ticks) / 3L % 2L == 1L;
						int v = 9 * (client.player.getWorld().getLevelProperties().isHardcore() ? 5 : 0);

						InGameHud.HeartType heartType = InGameHud.HeartType.fromPlayerState(client.player);
						drawHeart(c, InGameHud.HeartType.CONTAINER, pos.x(), pos.y(), v, blinking, false);
						drawHeart(c, heartType, pos.x(), pos.y(), v, blinking, renderHealthValue % 2 == 1);
					},
					(c, pos, text) -> drawBorderedText(c, text, pos, 0xFF6262),
					true, false
			);
		}

		// Food & mount health
		foodAndMountHealth:
		{
			int foodLevel = client.player.getHungerManager().getFoodLevel();
			int mountHealth = getHeartCount(getRiddenEntity());
			if (mountHealth <= 0) {
				// Food
				BiConsumer<Integer, Integer> empty, half, full;
				if (client.player.hasStatusEffect(StatusEffects.HUNGER)) {
					empty = (x, y) -> drawFoodEmptyHungerTexture(context, x, y);
					half = (x, y) -> drawFoodHalfHungerTexture(context, x, y);
					full = (x, y) -> drawFoodFullHungerTexture(context, x, y);
				} else {
					empty = (x, y) -> drawFoodEmptyTexture(context, x, y);
					half = (x, y) -> drawFoodHalfTexture(context, x, y);
					full = (x, y) -> drawFoodFullTexture(context, x, y);
				}

				int yOffset = client.player.getHungerManager().getSaturationLevel() <= 0 && this.ticks % (foodLevel * 3 + 1) == 0 ? random.nextInt(3) - 1 : 0;
				UnaryOperator<Vector2i> ditheredPosOperator = pos -> pos.add(0, yOffset);

				drawInfo(
						context, MathHelper.ceil(foodLevel / 2.0),
						(c, pos) -> {
							Vector2i ditheredPos = ditheredPosOperator.apply(pos);
							empty.accept(ditheredPos.x(), ditheredPos.y());
							(foodLevel % 2 == 1 ? half : full).accept(ditheredPos.x(), ditheredPos.y());
						},
						(c, pos, text) -> drawBorderedText(c, text, ditheredPosOperator.apply(pos), 0xE8A264),
						true, false
				);
			} else {
				// Mount health
				drawInfo(
						context, MathHelper.ceil(mountHealth / 2.0),
						(c, pos) -> {
							drawVehicleContainerHeartTexture(c, pos.x(), pos.y());
							if (foodLevel % 2 == 1) {
								drawVehicleHalfHeartTexture(c, pos.x(), pos.y());
							} else {
								drawVehicleFullHeartTexture(c, pos.x(), pos.y());
							}
						},
						(c, pos, text) -> drawBorderedText(c, text, pos, 0xE97240),
						true, false
				);
			}
		}

		// Armor
		armor:
		{
			int armor = client.player.getArmor();
			drawInfo(
					context, MathHelper.ceil(armor / 2.0),
					(c, pos) -> {
						if (armor % 2 == 1) {
							drawArmorHalfTexture(c, pos.x(), pos.y());
						} else {
							drawArmorFullTexture(c, pos.x(), pos.y());
						}
					},
					(c, pos, text) -> drawBorderedText(c, text, pos, 0xE6E6F2),
					true, false
			);
		}

		// Air
		air:
		{
			boolean submerged = client.player.isSubmergedInWater();
			int maxAir = client.player.getMaxAir(), air = Math.min(client.player.getAir(), maxAir);

			if (submerged || air < maxAir) {
				int stable = MathHelper.ceil((air - 2) * 10.0 / maxAir), total = MathHelper.ceil(air * 10.0 / maxAir);
				drawInfo(
						context, stable,
						(c, pos) -> {
							if (submerged && stable != total) {
								drawAirBurstingTexture(c, pos.x(), pos.y());
							} else {
								drawAirTexture(c, pos.x(), pos.y());
							}
						},
						(c, pos, text) -> drawBorderedText(c, text, pos, 0x56B8FF),
						true, false
				);
			}
		}
	}

	@Unique
	private void renderUniqueMountJumpBar(JumpingMount mount, DrawContext context, int x) {
		int width = (int) (Objects.requireNonNull(this.client.player).getMountJumpStrength() * (Verticality.HOTBAR_WIDTH + 1));
		int y = Verticality.height() - 32 + 3;
		int xOffset = Verticality.swapped() ? Verticality.HOTBAR_WIDTH - width : 0;

		// Jump bar background
		drawTexture(
				context, ICONS,
				x, y, 0, 84,
				Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT,
				Verticality.swapped()
		);
		if (mount.getJumpCooldown() > 0) {
			// Jump bar cooldown
			drawTexture(
					context, ICONS,
					x + xOffset, y, 0, 74,
					width, Verticality.SINGLE_BAR_HEIGHT,
					Verticality.swapped()
			);
		} else if (width > 0) {
			// Jump bar progress
			drawTexture(
					context, ICONS,
					x + xOffset, y, 0, 89,
					width, Verticality.SINGLE_BAR_HEIGHT,
					Verticality.swapped()
			);
		}
	}

	@Unique
	private void renderUniqueExperienceBar(DrawContext context, int x) {
		int experience = Objects.requireNonNull(this.client.player).getNextLevelExperience();

		if (experience > 0) {
			int width = (int) (client.player.experienceProgress * (Verticality.HOTBAR_WIDTH + 1));
			int y = Verticality.height() - 32 + 3;
			double offset = (Verticality.HOTBAR_WIDTH - width) * Verticality.swap();

			// Experience bar background
			context.drawTexture(
					ICONS,
					x, y, 0, 64,
					Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT
			);
			if (x > 0) {
				// Experience bar progress
				context.drawTexture(
						ICONS,
                        (int) (x + offset), y, (int) offset, 69,
						width, Verticality.SINGLE_BAR_HEIGHT
				);
			}
		}
	}

	@Unique
	private void drawTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height, boolean flipByX) {
		float
				uBegin = u / 256.0F,
				uEnd = (u + width) / 256.0F,
				vBegin = v / 256.0F,
				vEnd = (v + height) / 256.0F;

		((DrawContextInvoker) context).invokeDrawTexturedQuad(
				texture,
				x, x + width,
				y, y + height,
				0,
				flipByX ? uEnd : uBegin, flipByX ? uBegin : uEnd,
				vBegin, vEnd
		);
	}

	@Inject(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
					shift = At.Shift.AFTER
			)
	)
	private void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
		Verticality.alternativeLayout(context);

		if (Verticality.alternativeLayoutPartiallyEnabled() && !client.options.hudHidden && Objects.requireNonNull(client.interactionManager).hasStatusBars()) {
			context.getMatrices().push();
			context.getMatrices().translate(
					Verticality.enabled() ? -Verticality.hotbarShift() * Verticality.transition() : 0,
					Verticality.enabled() ? 0 : Verticality.hotbarShift() * Verticality.transition(),
					0
			);
			Verticality.compatibleWithRaised(context);

			renderAlternativeLayoutInfo(context);

			context.getMatrices().pop();
		}

		Verticality.compatibleWithRaised(context);

		if (Verticality.enabled()) {
			context.getMatrices().translate(
					 Verticality.CENTER_DISTANCE_TO_BORDER
							 + (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER)
							 - Verticality.hotbarShift() * Verticality.transition(),
					(Verticality.height() - Verticality.width() + Verticality.OFFHAND_WIDTH * Verticality.offset()) / 2.0,
					0
			);
			context.getMatrices().translate(0, Verticality.raisedHudShiftEdge() * (1 - Verticality.alternativeTransition()), 0);

			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));

			if (Verticality.alternativeLayoutPartiallyEnabled()) {
				if (client.player != null) {
					int x = (Verticality.width() - Verticality.HOTBAR_WIDTH) / 2;
					JumpingMount jumpingMount = client.player.getJumpingMount();

					if (jumpingMount != null) {
						renderUniqueMountJumpBar(jumpingMount, context, x);
					} else if (client.interactionManager != null && client.interactionManager.hasExperienceBar()) {
						renderUniqueExperienceBar(context, x);
					}
				}
			}
		} else {
			context.getMatrices().translate(0, Verticality.hotbarShift() * Verticality.transition(), 0);
			context.getMatrices().translate(-Verticality.raisedHudShiftEdge() * (1 - Verticality.alternativeTransition()), 0, 0);
		}
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
					ordinal = 1
			)
	)
	private void drawSelectedSlot(DrawContext context, Identifier identifier, int x, int y, int u, int v, int width, int height) {
		Verticality.drawSelectedSlot(context, identifier, x, y, u, v, width, height);
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
			)
	)
	private void drawOffhandSlot(DrawContext context, Identifier identifier, int x, int y, int u, int v, int width, int height) {
		context.getMatrices().push();

		if (Verticality.enabled()) {
			if (Verticality.alternativeLayoutPartiallyEnabled()) {
				drawHotbarOffhandRightTexture(
						context,
						(int) ((Verticality.width() + Verticality.HOTBAR_WIDTH) / 2.0 - Verticality.OFFHAND_WIDTH),
						y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT)
				);
			} else {
				final boolean offhandLeft = (MinecraftClient.getInstance().options.getMainArm().getValue().getOpposite() == Arm.LEFT) == !Verticality.upsideDown();

				if (offhandLeft) {
					drawHotbarOffhandLeftTexture(context, (int) (Verticality.width() / 2.0 - 91 - 29), Verticality.height() - 23);
				} else {
					drawHotbarOffhandRightTexture(context, (int) (Verticality.width() / 2.0 + 91), Verticality.height() - 23);
				}
			}
		} else {
			if (Verticality.alternativeLayoutPartiallyEnabled()) {
				drawHotbarOffhandLeftTexture(
						context,
						(int) ((Verticality.width() - Verticality.HOTBAR_WIDTH) / 2.0),
						y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT)
				);
			} else {
				context.drawTexture(identifier, x, y, u, v, width, height);
			}
		}

		context.getMatrices().pop();
	}
}

