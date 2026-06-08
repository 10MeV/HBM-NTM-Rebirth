package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

public class HbmFueledAbilityToolItem extends HbmAbilityToolItem implements IFillableItem {
    private static final String TAG_FUEL = "fuel";

    private final int maxFuel;
    private final int consumption;
    private final int fillRate;
    private final Set<FluidType> acceptedFuels;

    private HbmFueledAbilityToolItem(float attackDamageModifier, double movementModifier, Tier tier,
                                     List<TagKey<Block>> mineableBlocks, Properties properties,
                                     int maxFuel, int consumption, int fillRate, FluidType... acceptedFuels) {
        super(attackDamageModifier, movementModifier, tier, mineableBlocks, properties);
        this.maxFuel = Math.max(0, maxFuel);
        this.consumption = Math.max(0, consumption);
        this.fillRate = Math.max(0, fillRate);
        this.acceptedFuels = new LinkedHashSet<>(List.of(acceptedFuels));
    }

    public static HbmFueledAbilityToolItem axe(float attackDamageModifier, double movementModifier, Tier tier,
                                               Properties properties, int maxFuel, int consumption, int fillRate,
                                               FluidType... acceptedFuels) {
        return new HbmFueledAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_AXE), properties, maxFuel, consumption, fillRate, acceptedFuels);
    }

    @Override
    public boolean canOperate(ItemStack stack) {
        return getFill(stack) >= consumption;
    }

    @Override
    public void hurtAbilityTool(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            setFill(stack, Math.max(getFill(stack) - consumption, 0));
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
                setFill(stack, Math.max(getFill(stack) - consumption, 0));
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
        double fraction = maxFuel <= 0 ? 0.0D : Math.max(0.0D, Math.min(1.0D, (double) getFill(stack) / (double) maxFuel));
        return Mth.hsvToRgb((float) (0.08D + fraction * 0.08D), 1.0F, 1.0F);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmFillableItemCapabilityProvider(stack, this, maxFuel);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return acceptedFuels.contains(type) && getFill(stack) < maxFuel;
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

    public ItemStack getEmptyTool() {
        ItemStack stack = new ItemStack(this);
        setFill(stack, 0);
        return stack;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fuel: " + getFill(stack) + "/" + maxFuel + "mB").withStyle(ChatFormatting.GOLD));
        for (FluidType fuel : acceptedFuels) {
            tooltip.add(Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                    .append(fuel.getDisplayName().copy().withStyle(ChatFormatting.YELLOW)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
