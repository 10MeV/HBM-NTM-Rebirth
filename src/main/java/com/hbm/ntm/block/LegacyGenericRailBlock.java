package com.hbm.ntm.block;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code RailGeneric}/{@code RailBooster} on Forge's vanilla-minecart rail path.
 * The old {@code flexible} field only selected a curve texture and tooltip: its
 * {@code isFlexibleRail} implementation returned {@code !isPowered()}, which is always true for
 * this unpowered rail base. Keep that source behaviour rather than making high-speed rails
 * mechanically straighter than they were.
 */
public final class LegacyGenericRailBlock extends RailBlock {
    private static final float LEGACY_BASE_SPEED = 0.4F;

    private final float maxSpeed;
    private final boolean showsTurnWarning;
    private final boolean booster;

    public LegacyGenericRailBlock(BlockBehaviour.Properties properties, float maxSpeed,
            boolean showsTurnWarning, boolean booster) {
        super(properties);
        this.maxSpeed = maxSpeed;
        this.showsTurnWarning = showsTurnWarning;
        this.booster = booster;
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return maxSpeed;
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (booster) {
            cart.setDeltaMovement(cart.getDeltaMovement().scale(1.15D));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        float speed = maxSpeed / LEGACY_BASE_SPEED;
        if (speed != 1.0F) {
            tooltip.add(Component.literal("Speed: " + (int) (speed * 100.0F) + "%")
                    .withStyle(speed > 1.0F ? ChatFormatting.BLUE : ChatFormatting.RED));
        }
        if (showsTurnWarning) {
            tooltip.add(Component.literal("Cannot be used for turns!").withStyle(ChatFormatting.RED));
        }
    }
}
