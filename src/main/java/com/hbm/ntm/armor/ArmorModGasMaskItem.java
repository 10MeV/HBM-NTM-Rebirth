package com.hbm.ntm.armor;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.ArmorUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArmorModGasMaskItem extends ArmorModItem implements GasMask {
    private final boolean mono;

    public ArmorModGasMaskItem(Properties properties, boolean mono) {
        super(properties, ArmorModHandler.ArmorModSlot.HELMET_ONLY, true, false, false, false);
        this.mono = mono;
    }

    @Override
    public List<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        if (mono) {
            return List.of(HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA);
        }
        return List.of(HazardClass.GAS_BLISTERING);
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(stack);
    }

    @Override
    public boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return true;
    }

    @Override
    public void installFilter(ItemStack stack, LivingEntity entity, ItemStack filter) {
        ArmorUtil.installGasMaskFilter(stack, filter);
    }

    @Override
    public void damageFilter(ItemStack stack, LivingEntity entity, int damage) {
        ArmorUtil.damageGasMaskFilter(stack, damage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ItemStack filter = getFilter(stack, player);
            if (!filter.isEmpty()) {
                if (!level.isClientSide) {
                    ArmorUtil.removeFilter(stack);
                    if (!player.getInventory().add(filter)) {
                        player.drop(filter, true);
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return super.use(level, player, hand);
    }
}
