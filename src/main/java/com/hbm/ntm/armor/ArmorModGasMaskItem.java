package com.hbm.ntm.armor;

import api.hbm.item.IGasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.ArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorModGasMaskItem extends ArmorModItem implements IGasMask {
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ArmorUtil.addGasMaskTooltip(stack, null, tooltip, flag);
        ArmorUtil.addGasMaskBlacklistTooltip(stack, null, tooltip);
    }

    @Override
    public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                               TooltipFlag flag) {
        tooltip.add(Component.literal("  ")
                .append(mod.getHoverName())
                .append(Component.literal(" (gas protection)"))
                .withStyle(ChatFormatting.GREEN));
        ArmorUtil.addGasMaskTooltip(mod, null, tooltip, flag);
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
