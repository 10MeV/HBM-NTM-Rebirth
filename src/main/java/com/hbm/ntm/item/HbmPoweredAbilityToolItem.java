package com.hbm.ntm.item;

import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryItemCapabilityProvider;
import com.hbm.ntm.energy.HbmChargeableItem;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class HbmPoweredAbilityToolItem extends HbmAbilityToolItem implements HbmChargeableItem {
    private final long maxCharge;
    private final long chargeRate;
    private final long consumption;

    private HbmPoweredAbilityToolItem(float attackDamageModifier, double movementModifier, Tier tier,
                                      List<TagKey<Block>> mineableBlocks, Properties properties,
                                      long maxCharge, long chargeRate, long consumption) {
        super(attackDamageModifier, movementModifier, tier, mineableBlocks, properties);
        this.maxCharge = Math.max(0L, maxCharge);
        this.chargeRate = Math.max(0L, chargeRate);
        this.consumption = Math.max(0L, consumption);
    }

    public static HbmPoweredAbilityToolItem pickaxe(float attackDamageModifier, double movementModifier, Tier tier,
                                                    Properties properties, long maxCharge, long chargeRate,
                                                    long consumption) {
        return new HbmPoweredAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_PICKAXE), properties, maxCharge, chargeRate, consumption);
    }

    public static HbmPoweredAbilityToolItem axe(float attackDamageModifier, double movementModifier, Tier tier,
                                                Properties properties, long maxCharge, long chargeRate,
                                                long consumption) {
        return new HbmPoweredAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_AXE), properties, maxCharge, chargeRate, consumption);
    }

    public static HbmPoweredAbilityToolItem shovel(float attackDamageModifier, double movementModifier, Tier tier,
                                                   Properties properties, long maxCharge, long chargeRate,
                                                   long consumption) {
        return new HbmPoweredAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_SHOVEL), properties, maxCharge, chargeRate, consumption);
    }

    public static HbmPoweredAbilityToolItem miner(float attackDamageModifier, double movementModifier, Tier tier,
                                                  Properties properties, long maxCharge, long chargeRate,
                                                  long consumption) {
        return new HbmPoweredAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL),
                properties, maxCharge, chargeRate, consumption);
    }

    @Override
    public boolean canOperate(ItemStack stack) {
        return getCharge(stack) >= consumption;
    }

    @Override
    public void hurtAbilityTool(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            dischargeBattery(stack, consumption);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!canOperate(stack)) {
            return false;
        }
        boolean mined = super.mineBlock(stack, level, state, pos, entity);
        if (mined && !level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && entity instanceof Player player) {
            hurtAbilityTool(stack, player);
        }
        return mined;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        boolean operated = canOperate(stack);
        boolean result = super.hurtEnemy(stack, victim, attacker);
        if (operated && !attacker.level().isClientSide) {
            if (attacker instanceof Player player) {
                hurtAbilityTool(stack, player);
            } else {
                dischargeBattery(stack, consumption);
            }
        }
        return result;
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
    public long getMaxCharge(ItemStack stack) {
        return maxCharge;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return chargeRate;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        return 0L;
    }

    @Override
    public long getCharge(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        if (stack.hasTag()) {
            return clampCharge(stack.getTag().getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG));
        }
        stack.getOrCreateTag().putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, maxCharge);
        return maxCharge;
    }

    @Override
    public void setCharge(ItemStack stack, long charge) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, clampCharge(charge));
        }
    }

    @Override
    public long chargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long before = getCharge(stack);
        setCharge(stack, before + amount);
        return getCharge(stack) - before;
    }

    @Override
    public long dischargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long before = getCharge(stack);
        setCharge(stack, before - amount);
        return before - getCharge(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Charge: " + shortNumber(getCharge(stack)) + " / "
                + shortNumber(getMaxCharge(stack)) + "HE").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private long clampCharge(long charge) {
        return Math.max(0L, Math.min(charge, maxCharge));
    }

    private static String shortNumber(long value) {
        double result;
        String suffix;
        double abs = Math.abs((double) value);
        if (abs >= 1_000_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000.0D) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000.0D) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000.0D) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000.0D) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D
                : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }
}
