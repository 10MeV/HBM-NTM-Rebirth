package com.hbm.ntm.item;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.network.HbmLegacyItemAnimationReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.AchievementHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

/**
 * Source: {@code ItemBoltgun}.  The shared legacy-tool path owns tool-target
 * resolution; this class restores the source-only firing feedback and part
 * animation without creating another item-animation packet family.
 */
public final class LegacyBoltgunItem extends LegacyToolItem implements HbmLegacyItemAnimationReceiver {
    private static final short TOOL_ANIMATION_SWING = 0;

    public LegacyBoltgunItem(Item.Properties properties) {
        super(properties, Toolable.ToolType.BOLT);
    }

    @Override
    protected void onLegacyToolUseSuccess(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        var level = context.getLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_BOLTGUN.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        ParticleUtil.spawnVanillaExtLargeExplode(level, context.getClickLocation().x, context.getClickLocation().y,
                context.getClickLocation().z, 1.0F, 1);
        ModMessages.sendLegacyItemAnimation(player, TOOL_ANIMATION_SWING, 0, 0);
    }

    @Override
    protected boolean consumesDurabilityOnLegacyToolUse() {
        return false;
    }

    /**
     * Source: {@code ItemBoltgun#onLeftClickEntity}. Returning true prevents
     * Forge's ordinary melee hit, exactly as the legacy item callback did.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!target.isAlive()) {
            return false;
        }
        ItemStack bolt = findBolt(player);
        if (bolt == null) {
            return false;
        }
        if (!player.level().isClientSide) {
            bolt.shrink(1);
            var level = player.level();
            level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.ITEM_BOLTGUN.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            EntityDamageUtil.attackEntityFromIgnoreIFrame(target,
                    ModDamageSources.source(level, ModDamageSources.BOLT_GUN, player), 10.0F);
            if (!target.isAlive() && target instanceof ServerPlayer victim) {
                AchievementHandler.award(victim, AchievementHandler.GO_FISH);
            }
            ParticleUtil.spawnVanillaExtLargeExplode(level, target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 1.0F, 1);
            if (player instanceof ServerPlayer serverPlayer) {
                ModMessages.sendLegacyItemAnimation(serverPlayer, TOOL_ANIMATION_SWING, 0, 0);
            }
        }
        return true;
    }

    @Override
    public void handleLegacyItemAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex,
            int itemIndex) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.LegacyToolAnimationClient.handleBoltgun(
                        stack, selectedSlot, animationType, itemIndex));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName("com.hbm.ntm.client.renderer.LegacyToolItemRendererBridge");
            bridge.getMethod("acceptBoltgun", Consumer.class).invoke(null, consumer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Missing boltgun client renderer bridge", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Boltgun client renderer bridge failed", exception.getCause());
        }
    }

    private static ItemStack findBolt(Player player) {
        for (ItemStack candidate : player.getInventory().items) {
            if (candidate.is(ModItems.BOLT_SPIKE.get())
                    || candidate.is(ModItems.legacyItem("bolt_steel").get())
                    || candidate.is(ModItems.legacyItem("bolt_tungsten").get())
                    || candidate.is(ModItems.legacyItem("bolt_dura_steel").get())) {
                return candidate;
            }
        }
        return null;
    }
}
