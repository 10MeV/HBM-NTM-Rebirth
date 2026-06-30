package com.hbm.ntm.energy;

import com.hbm.ntm.util.HbmTextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HbmLegacyEnergyCoreItem extends HbmBatteryItem {
    public HbmLegacyEnergyCoreItem(Properties properties, long maxCharge, long chargeRate, long dischargeRate) {
        super(properties, maxCharge, chargeRate, dischargeRate);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long charge = stack.hasTag() ? getCharge(stack) : getMaxCharge(stack);
        long percent = getMaxCharge(stack) <= 0L ? 0L : (charge * 100L) / getMaxCharge(stack);
        tooltip.add(Component.literal("Charge: " + HbmTextUtil.shortNumber(percent) + "%").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("(" + HbmTextUtil.shortNumber(charge) + "/"
                + HbmTextUtil.shortNumber(getMaxCharge(stack)) + "HE)"));
        tooltip.add(Component.literal("Charge rate: " + HbmTextUtil.shortNumber(getChargeRate(stack)) + "HE/t"));
        tooltip.add(Component.literal("Discharge rate: " + HbmTextUtil.shortNumber(getDischargeRate(stack)) + "HE/t"));
    }
}
