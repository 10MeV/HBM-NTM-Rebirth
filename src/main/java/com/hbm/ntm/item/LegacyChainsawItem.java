package com.hbm.ntm.item;

import com.hbm.ntm.ability.IBaseAbility;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.network.HbmLegacyItemAnimationReceiver;
import com.hbm.ntm.network.ModMessages;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

/** Source-faithful {@code ItemChainsaw} animation receiver layered on the existing fueled-ability tool. */
public final class LegacyChainsawItem extends HbmFueledAbilityToolItem implements HbmLegacyItemAnimationReceiver {
    private static final short TOOL_ANIMATION_SWING = 0;

    public LegacyChainsawItem(float attackDamageModifier, double movementModifier, Tier tier, Item.Properties properties,
            int maxFuel, int consumption, int fillRate, FluidType... acceptedFuels) {
        super(attackDamageModifier, movementModifier, tier, List.<TagKey<Block>>of(BlockTags.MINEABLE_WITH_AXE),
                properties, maxFuel, consumption, fillRate, acceptedFuels);
    }

    @Override
    public LegacyChainsawItem addAbility(IBaseAbility ability, int level) {
        super.addAbility(ability, level);
        return this;
    }

    @Override
    public LegacyChainsawItem setShears() {
        super.setShears();
        return this;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof ServerPlayer player && canOperate(stack)) {
            ModMessages.sendLegacyItemAnimation(player, TOOL_ANIMATION_SWING, 0, 0);
        }
        return false;
    }

    @Override
    public void handleLegacyItemAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex,
            int itemIndex) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.LegacyToolAnimationClient.handleChainsaw(
                        stack, selectedSlot, animationType, itemIndex));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName("com.hbm.ntm.client.renderer.LegacyToolItemRendererBridge");
            bridge.getMethod("acceptChainsaw", Consumer.class).invoke(null, consumer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Missing chainsaw client renderer bridge", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Chainsaw client renderer bridge failed", exception.getCause());
        }
    }
}
