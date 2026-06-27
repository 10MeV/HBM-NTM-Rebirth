package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RadioboxBlockEntity extends HbmEnergyBlockEntity {
    public static final long MAX_POWER = 500_000L;
    private static final long CONSUMPTION = 25_000L;
    private static final int RANGE = 15;
    private static final String TAG_POWER = "power";
    private static final String TAG_INFINITE = "infinite";

    private boolean infinite;

    public RadioboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIOBOX.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioboxBlockEntity box) {
        box.subscribeEnergyReceiverToAllSides();
        long oldPower = box.energy.getPower();

        if (state.hasProperty(RadioboxBlock.ACTIVE) && state.getValue(RadioboxBlock.ACTIVE)
                && (box.energy.getPower() >= CONSUMPTION || box.infinite)) {
            if (!box.infinite) {
                box.energy.setPower(box.energy.getPower() - CONSUMPTION);
            }
            AABB bounds = new AABB(
                    pos.getX() - RANGE, pos.getY() - RANGE, pos.getZ() - RANGE,
                    pos.getX() + RANGE, pos.getY() + RANGE, pos.getZ() + RANGE);
            List<Mob> entities = level.getEntitiesOfClass(Mob.class, bounds,
                    entity -> entity instanceof Enemy && !isLegacyFbi(entity));
            for (Mob entity : entities) {
                entity.hurt(ModDamageSources.source(level, ModDamageSources.ENERVATION), 20.0F);
            }
        }

        box.networkPackNT(15);
        if (oldPower != box.energy.getPower()) {
            box.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putBoolean(TAG_INFINITE, infinite);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        infinite = tag.getBoolean(TAG_INFINITE);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    private static boolean isLegacyFbi(Entity entity) {
        String simpleName = entity.getClass().getSimpleName();
        return "EntityFBI".equals(simpleName) || "EntityFBIDrone".equals(simpleName);
    }
}
