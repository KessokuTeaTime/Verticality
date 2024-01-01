package net.krlite.verticality.mixin;

import net.krlite.verticality.Verticality;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE;

	@Shadow public abstract TextRenderer getTextRenderer();

	@Shadow @Final private MinecraftClient client;

	@Shadow @Final private static Identifier JUMP_BAR_BACKGROUND_TEXTURE;
	@Shadow @Final private static Identifier JUMP_BAR_COOLDOWN_TEXTURE;
	@Shadow @Final private static Identifier JUMP_BAR_PROGRESS_TEXTURE;
	@Shadow @Final private static Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE;
	@Shadow @Final private static Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE;

	@Shadow protected abstract void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half);

	@Shadow private long heartJumpEndTick;
	@Shadow private int ticks;
	@Shadow private int renderHealthValue;

	@Shadow protected abstract int getHeartCount(LivingEntity entity);

	@Shadow protected abstract LivingEntity getRiddenEntity();

	@Shadow @Final private static Identifier FOOD_EMPTY_HUNGER_TEXTURE;
	@Shadow @Final private static Identifier FOOD_HALF_HUNGER_TEXTURE;
	@Shadow @Final private static Identifier FOOD_FULL_HUNGER_TEXTURE;
	@Shadow @Final private static Identifier FOOD_EMPTY_TEXTURE;
	@Shadow @Final private static Identifier FOOD_HALF_TEXTURE;
	@Shadow @Final private static Identifier FOOD_FULL_TEXTURE;
	@Shadow @Final private Random random;
	@Shadow @Final private static Identifier VEHICLE_CONTAINER_HEART_TEXTURE;
	@Shadow @Final private static Identifier VEHICLE_HALF_HEART_TEXTURE;
	@Shadow @Final private static Identifier VEHICLE_FULL_HEART_TEXTURE;
	@Shadow @Final private static Identifier ARMOR_HALF_TEXTURE;
	@Shadow @Final private static Identifier ARMOR_FULL_TEXTURE;
	@Shadow @Final private static Identifier AIR_BURSTING_TEXTURE;
	@Shadow @Final private static Identifier AIR_TEXTURE;
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
					context, MathHelper.floor(MathHelper.ceil(client.player.getHealth()) / 2.0),
					(c, pos) -> {
						boolean blinking = heartJumpEndTick > (long) ticks && (this.heartJumpEndTick - (long) this.ticks) / 3L % 2L == 1L;
						boolean hardcore = client.player.getWorld().getLevelProperties().isHardcore();

						InGameHud.HeartType heartType = InGameHud.HeartType.fromPlayerState(client.player);
						drawHeart(c, InGameHud.HeartType.CONTAINER, pos.x(), pos.y(), hardcore, blinking, false);
						drawHeart(c, heartType, pos.x(), pos.y(), hardcore, blinking, renderHealthValue % 2 == 1);
					},
					(c, pos, text) -> drawBorderedText(c, text, pos, 0xFFFFFF),
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
				Identifier empty, half, full;
				if (client.player.hasStatusEffect(StatusEffects.HUNGER)) {
					empty = FOOD_EMPTY_HUNGER_TEXTURE;
					half = FOOD_HALF_HUNGER_TEXTURE;
					full = FOOD_FULL_HUNGER_TEXTURE;
				} else {
					empty = FOOD_EMPTY_TEXTURE;
					half = FOOD_HALF_TEXTURE;
					full = FOOD_FULL_TEXTURE;
				}

				int yOffset = client.player.getHungerManager().getSaturationLevel() <= 0 && this.ticks % (foodLevel * 3 + 1) == 0 ? random.nextInt(3) - 1 : 0;
				UnaryOperator<Vector2i> ditheredPosOperator = pos -> pos.add(0, yOffset);

				drawInfo(
						context, MathHelper.floor(foodLevel / 2.0),
						(c, pos) -> {
							Vector2i ditheredPos = ditheredPosOperator.apply(pos);
							c.drawGuiTexture(empty, ditheredPos.x(), ditheredPos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE);
							c.drawGuiTexture(foodLevel % 2 == 1 ? half : full, ditheredPos.x(), ditheredPos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE);
						},
						(c, pos, text) -> drawBorderedText(c, text, ditheredPosOperator.apply(pos), 0xFFFFFF),
						true, false
				);
			} else {
				// Mount health
				drawInfo(
						context, MathHelper.floor(mountHealth / 2.0),
						(c, pos) -> {
							c.drawGuiTexture(VEHICLE_CONTAINER_HEART_TEXTURE, pos.x(), pos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE);
							c.drawGuiTexture(foodLevel % 2 == 1 ? VEHICLE_HALF_HEART_TEXTURE : VEHICLE_FULL_HEART_TEXTURE, pos.x(), pos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE);
						},
						(c, pos, text) -> drawBorderedText(c, text, pos, 0xFFFFFF),
						true, false
				);
			}
		}

		// Armor
		armor:
		{
			int armor = client.player.getArmor();
			drawInfo(
					context, MathHelper.floor(armor / 2.0),
					(c, pos) -> c.drawGuiTexture(armor % 2 == 1 ? ARMOR_HALF_TEXTURE : ARMOR_FULL_TEXTURE, pos.x(), pos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE),
					(c, pos, text) -> drawBorderedText(c, text, pos, 0xFFFFFF),
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
						(c, pos) -> c.drawGuiTexture(submerged && stable != total ? AIR_BURSTING_TEXTURE : AIR_TEXTURE, pos.x(), pos.y(), Verticality.INFO_ICON_SIZE, Verticality.INFO_ICON_SIZE),
						(c, pos, text) -> drawBorderedText(c, text, pos, 0xFFFFFF),
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

		drawGuiTexture(
				context, JUMP_BAR_BACKGROUND_TEXTURE,
				Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT, 0, 0,
				x, y, Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT,
				Verticality.swapped()
		);
		if (mount.getJumpCooldown() > 0) {
			drawGuiTexture(
					context, JUMP_BAR_COOLDOWN_TEXTURE,
					Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT, 0, 0,
					x + xOffset, y, width, Verticality.SINGLE_BAR_HEIGHT,
					Verticality.swapped()
			);
		} else if (width > 0) {
			drawGuiTexture(
					context, JUMP_BAR_PROGRESS_TEXTURE,
					Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT, 0, 0,
					x + xOffset, y, width, Verticality.SINGLE_BAR_HEIGHT,
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

			context.drawGuiTexture(EXPERIENCE_BAR_BACKGROUND_TEXTURE, x, y, Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT);
			if (x > 0) {
				context.drawGuiTexture(
						EXPERIENCE_BAR_PROGRESS_TEXTURE,
						Verticality.HOTBAR_WIDTH, Verticality.SINGLE_BAR_HEIGHT, (int) offset, 0,
                        (int) (x + offset), y, width, Verticality.SINGLE_BAR_HEIGHT
				);
			}
		}
	}

	@Unique
	private void drawGuiTexture(DrawContext context, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, boolean flipByX) {
		Sprite sprite = ((DrawContextAccessor) context).getGuiAtlasManager().getSprite(texture);

		float
				uBegin = sprite.getFrameU((float) k / i),
				uEnd = sprite.getFrameU((float) (k + width) / i),
				vBegin = sprite.getFrameV((float) l / j),
				vEnd = sprite.getFrameV((float) (l + height) / j);

		((DrawContextInvoker) context).invokeDrawTexturedQuad(
				sprite.getAtlasId(),
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
		// Alternative layout
		context.getMatrices().translate(
				Verticality.alternativeLayoutOffsetX(),
				Verticality.alternativeLayoutOffsetY(),
				0
		);

		if (Verticality.alternativeLayoutPartiallyEnabled() && !client.options.hudHidden && Objects.requireNonNull(client.interactionManager).hasStatusBars()) {
			context.getMatrices().push();
			context.getMatrices().translate(
					Verticality.enabled() ? -Verticality.hotbarShift() * Verticality.transition() : 0,
					Verticality.enabled() ? 0 : Verticality.hotbarShift() * Verticality.transition(),
					0
			);

			if (Verticality.enabled()) {
				// Compatibility with Raised
				context.getMatrices().translate(
						Verticality.raisedHudShift(),
						Verticality.raisedHudShift(),
						0
				);
			}

			renderAlternativeLayoutInfo(context);

			context.getMatrices().pop();
		}

		if (Verticality.enabled()) {
			context.getMatrices().translate(
					 Verticality.CENTER_DISTANCE_TO_BORDER
							 + (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER)
							 - Verticality.hotbarShift() * Verticality.transition(),
					(Verticality.height() - Verticality.width() + Verticality.OFFHAND_WIDTH * Verticality.offset()) / 2.0,
					0
			);

			// Compatibility with Raised
			context.getMatrices().translate(
					Verticality.raisedHudShift(),
					Verticality.raisedHudShift(),
					0
			);

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
		}
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void drawSelectedSlot(DrawContext context, Identifier identifier, int x, int y, int width, int height) {
		Verticality.drawSelectedSlot(context, identifier, x, y, width, height);
	}

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
			)
	)
	private void drawOffhandSlot(DrawContext context, Identifier identifier, int x, int y, int width, int height) {
		context.getMatrices().push();

		if (Verticality.enabled()) {
			if (Verticality.alternativeLayoutPartiallyEnabled()) {
				context.drawGuiTexture(
						HOTBAR_OFFHAND_RIGHT_TEXTURE,
						(int) ((Verticality.width() + Verticality.HOTBAR_WIDTH) / 2.0 - Verticality.OFFHAND_WIDTH),
						y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT),
						width, height
				);
			} else {
				final boolean offhandLeft = (MinecraftClient.getInstance().options.getMainArm().getValue().getOpposite() == Arm.LEFT) == !Verticality.upsideDown();

				if (offhandLeft) {
					context.drawGuiTexture(
							HOTBAR_OFFHAND_LEFT_TEXTURE,
							(int) (Verticality.width() / 2.0 - 91 - 29), Verticality.height() - 23,
							width, height
					);
				} else {
					context.drawGuiTexture(
							HOTBAR_OFFHAND_RIGHT_TEXTURE,
							(int) (Verticality.width() / 2.0 + 91), Verticality.height() - 23,
							width, height
					);
				}
			}
		} else {
			if (Verticality.alternativeLayoutPartiallyEnabled()) {
				context.drawGuiTexture(
						HOTBAR_OFFHAND_LEFT_TEXTURE,
						(int) ((Verticality.width() - Verticality.HOTBAR_WIDTH) / 2.0),
						y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT),
						width, height
				);
			} else {
				context.drawGuiTexture(identifier, x, y, width, height);
			}
		}

		context.getMatrices().pop();
	}
}

@Mixin(InGameHud.class)
abstract
class ItemAdjustor {
	@Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity playerEntity, ItemStack itemStack, int seed);

	@Redirect(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
			)
	)
	private void renderHotbarItem(InGameHud inGameHud, DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) {
		if (Verticality.enabled()) {
			double
					xRelative = (x + 8) - Verticality.width() / 2.0,
					yRelative = (y + 8) - (Verticality.height() - Verticality.CENTER_DISTANCE_TO_BORDER);

			renderHotbarItem(
					context,
					(int) (Math.round(Verticality.CENTER_DISTANCE_TO_BORDER - yRelative) - 8),
					(int) (Math.round(Verticality.height() / 2.0 + xRelative) - 8),
					tickDelta, player, stack, seed
			);
		}
		else {
			renderHotbarItem(context, x, y, tickDelta, player, stack, seed);
		}
	}

	@ModifyArgs(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
			),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1),
					to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false)
			)
	)
	private void fixOffhandItem(Args args) {
		int x = args.get(2), y = args.get(3);

		if (Verticality.alternativeLayoutPartiallyEnabled()) {
			// x
			args.set(2, (Verticality.width() + Verticality.HOTBAR_WIDTH * (Verticality.enabled() ? 1 : -1)) / 2 - Verticality.HOTBAR_ITEM_GAP * (Verticality.enabled() ? 1 : -1) - Verticality.ITEM_SIZE * (Verticality.enabled() ? 1 : 0));
			// y
			args.set(3, y - (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP + Verticality.SINGLE_BAR_HEIGHT));
		} else if (Verticality.isMainArmLeft() && Verticality.enabled()) {
			args.set(2, (int) Math.round(x - 2 * ((x + 8) - Verticality.width() / 2.0))); // Revert the x-coordinate of the item
		}

		drawingOffhandItem = true;
	}

	@Unique
	private boolean drawingOffhandItem = false;

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;getBobbingAnimationTime()I"
			)
	)
	private void drawItemPre(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.translateIcon(context, y, false, drawingOffhandItem && Verticality.alternativeLayoutPartiallyEnabled());
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void drawItemPost(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
			)
	)
	private void drawItemInSlotPre(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.translateIcon(context, y, false, drawingOffhandItem && Verticality.alternativeLayoutPartiallyEnabled());

		drawingOffhandItem = false;
	}

	@Inject(
			method = "renderHotbarItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
					shift = At.Shift.AFTER
			)
	)
	private void drawItemInSlotPost(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
		context.getMatrices().pop();
	}
}

@Mixin(InGameHud.class)
abstract
class BarAdjustor {
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTextBackground(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;III)V",
					shift = At.Shift.BEFORE
			)
	)
	private void renderOverlay(DrawContext context, float tickDelta, CallbackInfo ci) {
		context.getMatrices().translate(
				0,
				Verticality.hotbarShift() * Verticality.later() + (Verticality.HOTBAR_FULL_HEIGHT + Verticality.GAP) * Verticality.alternativeTransition(),
				0
		);
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			)
	)
	private void renderStatusBarsPre(DrawContext context, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, true);
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderStatusBarsPost(DrawContext context, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"
			)
	)
	private void renderHealthBarPre(DrawContext context, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, true);
	}

	@Inject(
			method = "renderStatusBars",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderHealthBarPost(DrawContext context, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderMountHealth",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			)
	)
	private void renderMountHealthPre(DrawContext context, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, true);
	}

	@Inject(
			method = "renderMountHealth",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderMountHealthPost(DrawContext context, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			)
	)
	private void renderMountJumpBarBackgroundPre(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, false);

		context.getMatrices().translate(Verticality.alternativeLayoutOffsetX(), 0, 0);
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderMountJumpBarBackgroundPost(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"
			)
	)
	private void renderMountJumpBarPre(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, false);

		context.getMatrices().translate(Verticality.alternativeLayoutOffsetX(), 0, 0);
	}

	@Inject(
			method = "renderMountJumpBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderMountJumpBarPost(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
			)
	)
	private void renderExperienceBarBackgroundPre(DrawContext context, int x, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, false);

		context.getMatrices().translate(Verticality.alternativeLayoutOffsetX(), 0, 0);
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderExperienceBarBackgroundPost(DrawContext context, int x, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"
			)
	)
	private void renderExperienceBarProgressPre(DrawContext context, int x, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, false);

		context.getMatrices().translate(Verticality.alternativeLayoutOffsetX(), 0, 0);
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V",
					shift = At.Shift.AFTER
			)
	)
	private void renderExperienceBarProgressPost(DrawContext context, int x, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
			)
	)
	private void renderExperienceBarTextPre(DrawContext context, int x, CallbackInfo ci) {
		context.getMatrices().push();
		Verticality.verticallyShiftBarPre(context, true);
	}

	@Inject(
			method = "renderExperienceBar",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderExperienceBarTextPost(DrawContext context, int x, CallbackInfo ci) {
		Verticality.verticallyShiftBarPost(context);
		context.getMatrices().pop();
	}

	@Inject(
			method = "renderHeldItemTooltip",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private void renderHeldItemTooltipPre(DrawContext context, CallbackInfo ci) {
		context.getMatrices().push();
		context.getMatrices().translate(
				0,
				Verticality.hotbarShift() * Verticality.later(),
				0
		);
	}

	@Inject(
			method = "renderHeldItemTooltip",
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
					shift = At.Shift.AFTER
			)
	)
	private void renderHeldItemTooltipPost(DrawContext context, CallbackInfo ci) {
		context.getMatrices().pop();
	}

	@Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
	private void fixHelpItemTooltipPre(DrawContext context, CallbackInfo ci) {
		if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
			context.getMatrices().push();
			context.getMatrices().translate(0, Verticality.TOOLTIP_OFFSET * Verticality.later(), 0);
		}
	}

	@Inject(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", shift = At.Shift.AFTER))
	private void fixHelpItemTooltipPost(DrawContext context, CallbackInfo ci) {
		if (MinecraftClient.getInstance().interactionManager != null && !MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
			context.getMatrices().pop();
		}
	}
}
