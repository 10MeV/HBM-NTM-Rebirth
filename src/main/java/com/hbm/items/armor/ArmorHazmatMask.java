package com.hbm.items.armor;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.item.HazmatMaskArmorItem;
import com.hbm.ntm.radiation.ArmorUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

/**
 * Legacy package facade for the 1.7.10 hazmat hood gas-mask item.
 */
@Deprecated(forRemoval = false)
public class ArmorHazmatMask extends HazmatMaskArmorItem {
    public ArmorHazmatMask(ArmorMaterial material) {
        super(material, new Properties());
    }

    public ArmorHazmatMask(ArmorMaterial material, int slot, String texture) {
        this(material);
    }

    @Override
    public ArrayList<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(stack);
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
    public boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ArmorUtil.addGasMaskTooltip(stack, null, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ItemStack filter = getFilter(stack, player);
            if (!filter.isEmpty()) {
                if (!level.isClientSide) {
                    ArmorUtil.removeGasMaskFilterToInventory(stack, player);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return super.use(level, player, hand);
    }
}
