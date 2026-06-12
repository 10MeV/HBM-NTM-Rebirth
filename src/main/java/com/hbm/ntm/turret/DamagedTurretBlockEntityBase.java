package com.hbm.ntm.turret;

import com.hbm.ntm.energy.HbmEnergySideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DamagedTurretBlockEntityBase extends TurretBlockEntityBase {
    protected DamagedTurretBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0L);
    }

    @Override
    public List<ItemStack> getDrops() {
        return List.of();
    }

    @Override
    protected boolean hasPower() {
        return true;
    }

    @Override
    protected boolean isActive() {
        return true;
    }

    @Override
    protected boolean usesEnergy() {
        return false;
    }

    @Override
    protected long getConsumption() {
        return 0L;
    }

    @Override
    protected boolean entityAcceptableTarget(Entity entity) {
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return false;
        }
        return entity instanceof LivingEntity && entity.isAlive();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.NONE;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER || capability == ForgeCapabilities.ENERGY) {
            return LazyOptional.empty();
        }
        return super.getCapability(capability, side);
    }
}
