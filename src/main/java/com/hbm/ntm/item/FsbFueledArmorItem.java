package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.util.HbmTextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class FsbFueledArmorItem extends FsbArmorItem implements IFillableItem {
    private static final String TAG_FUEL = "fuel";

    private final FluidType fuelType;
    private final int maxFuel;
    private final int fillRate;
    private final int consumption;
    private final int drain;

    public FsbFueledArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, FluidType fuelType, int maxFuel, int fillRate, int consumption,
            int drain) {
        super(material, type, properties, fullSetEffects);
        this.fuelType = fuelType == null ? HbmFluids.NONE : fuelType;
        this.maxFuel = Math.max(0, maxFuel);
        this.fillRate = Math.max(0, fillRate);
        this.consumption = Math.max(0, consumption);
        this.drain = Math.max(0, drain);
    }

    @Override
    public boolean isArmorEnabled(ItemStack stack) {
        return getFill(stack) > 0;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        if (!level.isClientSide && drain > 0 && !player.getAbilities().instabuild
                && level.getGameTime() % 10L == 0L && hasFullSet(player)) {
            setFill(stack, Math.max(getFill(stack) - drain, 0));
        }
    }

    public void applyLegacyDamage(ItemStack stack, int damage) {
        if (damage > 0 && consumption > 0) {
            setFill(stack, Math.max(getFill(stack) - damage * consumption, 0));
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getFill(stack) < maxFuel;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (maxFuel <= 0) {
            return 0;
        }
        double fraction = Math.max(0.0D, Math.min(1.0D, (double) getFill(stack) / (double) maxFuel));
        return Math.round(13.0F * (float) fraction);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        double fraction = maxFuel <= 0 ? 0.0D
                : Math.max(0.0D, Math.min(1.0D, (double) getFill(stack) / (double) maxFuel));
        return Mth.hsvToRgb((float) (0.08D + fraction * 0.08D), 1.0F, 1.0F);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmFillableItemCapabilityProvider(stack, this, maxFuel);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return type == fuelType && getFill(stack) < maxFuel;
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (amount <= 0 || !acceptsFluid(type, stack)) {
            return Math.max(0, amount);
        }
        int moved = Math.min(Math.min(amount, fillRate), maxFuel - getFill(stack));
        setFill(stack, getFill(stack) + moved);
        return amount - moved;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return false;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        return 0;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return HbmFluids.NONE;
    }

    @Override
    public int getFill(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FUEL)) {
            setFill(stack, maxFuel);
            return maxFuel;
        }
        return Math.max(0, Math.min(maxFuel, tag.getInt(TAG_FUEL)));
    }

    public void setFill(ItemStack stack, int fill) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putInt(TAG_FUEL, Math.max(0, Math.min(maxFuel, fill)));
        }
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public int getFillRate() {
        return fillRate;
    }

    public int getConsumption() {
        return consumption;
    }

    public int getDrain() {
        return drain;
    }

    public FluidType getFuelType() {
        return fuelType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(fuelType.getDisplayName().copy().append(Component.literal(": "
                + HbmTextUtil.shortNumber(getFill(stack)) + " / " + HbmTextUtil.shortNumber(maxFuel)))
                .withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
