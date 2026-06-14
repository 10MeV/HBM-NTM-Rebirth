package com.hbm.ntm.item;

import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.energy.HbmBatteryItemCapabilityProvider;
import com.hbm.ntm.util.HbmTextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class FsbPoweredArmorItem extends FsbArmorItem implements FsbPoweredArmor {
    private final long baseMaxCharge;
    private final long chargeRate;
    private final long consumption;
    private final long drain;

    public FsbPoweredArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain) {
        this(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                FullSetTraits.NONE);
    }

    public FsbPoweredArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, false, 0, fullSetTraits);
        this.baseMaxCharge = Math.max(0L, baseMaxCharge);
        this.chargeRate = Math.max(0L, chargeRate);
        this.consumption = Math.max(0L, consumption);
        this.drain = Math.max(0L, drain);
    }

    @Override
    public ResourceLocation fsbMaterialId(ItemStack stack) {
        return super.fsbMaterialId(stack);
    }

    @Override
    public boolean isArmorEnabled(ItemStack stack) {
        return FsbPoweredArmor.super.isArmorEnabled(stack);
    }

    @Override
    public long getBaseMaxCharge(ItemStack stack) {
        return baseMaxCharge;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return chargeRate;
    }

    @Override
    public long getConsumption(ItemStack stack) {
        return consumption;
    }

    @Override
    public long getDrain(ItemStack stack) {
        return drain;
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!level.isClientSide && drain > 0L && !player.getAbilities().instabuild && hasFullSet(player)) {
            dischargeBattery(stack, drain);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) < getMaxCharge(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (getMaxCharge(stack) <= 0L) {
            return 0;
        }
        double fraction = Math.max(0.0D, Math.min(1.0D, (double) getCharge(stack) / (double) getMaxCharge(stack)));
        return Math.round(13.0F * (float) fraction);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        double fraction = getMaxCharge(stack) <= 0L ? 0.0D
                : Math.max(0.0D, Math.min(1.0D, (double) getCharge(stack) / (double) getMaxCharge(stack)));
        return Mth.hsvToRgb((float) fraction / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmBatteryItemCapabilityProvider(stack, this);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Charge: " + HbmTextUtil.shortNumber(getCharge(stack)) + " / "
                + HbmTextUtil.shortNumber(getMaxCharge(stack)) + "HE").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Charge rate: " + HbmTextUtil.shortNumber(getChargeRate(stack)) + "HE/t")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
