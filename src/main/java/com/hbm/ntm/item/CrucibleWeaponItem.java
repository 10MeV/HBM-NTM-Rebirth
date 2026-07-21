package com.hbm.ntm.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.network.HbmLegacyItemAnimationReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmShadyUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

/** Source carrier for the weapon {@code ItemCrucible}, not the Crucible machine. */
public final class CrucibleWeaponItem extends HbmAbilitySwordItem implements HbmLegacyItemAnimationReceiver {
    private static final String KEY_EQUIPPED = "eqipped";
    private static final short TOOL_ANIMATION_SWING = 0;
    private static final short TOOL_ANIMATION_EQUIP = 1;
    private static final Tier CRUCIBLE_TIER = new Tier() {
        @Override public int getUses() { return 3; }
        @Override public float getSpeed() { return 50.0F; }
        @Override public float getAttackDamageBonus() { return 100.0F; }
        @Override public int getLevel() { return 10; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public CrucibleWeaponItem(Item.Properties properties) {
        super(CRUCIBLE_TIER, 5_000.0F, 1.0D, properties.stacksTo(1));
    }

    @Override
    public boolean canOperate(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof ServerPlayer player && HbmShadyUtil.TANKISH.equals(player.getUUID().toString())) {
            stack.setDamageValue(0);
        }
        if (entity instanceof ServerPlayer player && canOperate(stack)) {
            ModMessages.sendLegacyItemAnimation(player, TOOL_ANIMATION_SWING, 0, 0);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!selected) {
            stack.getOrCreateTag().putBoolean(KEY_EQUIPPED, false);
            return;
        }
        if (!stack.getOrCreateTag().getBoolean(KEY_EQUIPPED) && canOperate(stack)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WEAPON_C_DEPLOY.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            ModMessages.sendLegacyItemAnimation(player, TOOL_ANIMATION_EQUIP, 0, 0);
        }
        stack.getOrCreateTag().putBoolean(KEY_EQUIPPED, true);
    }

    @Override
    public void handleLegacyItemAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex,
            int itemIndex) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.LegacyToolAnimationClient.handleCrucible(
                        stack, selectedSlot, animationType, itemIndex));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName("com.hbm.ntm.client.renderer.LegacyToolItemRendererBridge");
            bridge.getMethod("acceptCrucible", Consumer.class).invoke(null, consumer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Missing Crucible client renderer bridge", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Crucible client renderer bridge failed", exception.getCause());
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (!canOperate(stack)) {
            if (!attacker.level().isClientSide && attacker instanceof net.minecraft.world.entity.player.Player player) {
                player.displayClientMessage(Component.literal("Not enough energy.").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        LegacySoundPlayer.playSoundAtEntity(attacker, "mob.zombie.woodbreak", 1.0F,
                0.75F + victim.getRandom().nextFloat() * 0.2F);
        boolean result = super.hurtEnemy(stack, victim, attacker);
        if (!attacker.level().isClientSide && !victim.isAlive()) {
            int count = Math.min((int) Math.ceil(victim.getMaxHealth() / 3.0D), 250);
            for (int index = 0; index < count * 4; index++) {
                ParticleUtil.spawnVanillaExtBlockDust(attacker.level(), victim.getX(),
                        victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                        0.0D, 0.1D, 0.0D, Blocks.REDSTONE_BLOCK);
            }
        }
        if (attacker instanceof net.minecraft.world.entity.player.Player player) {
            String name = player.getGameProfile().getName();
            if ("Tankish".equals(name) || "Tankish020".equals(name)) {
                return true;
            }
        }
        return result;
    }
}
