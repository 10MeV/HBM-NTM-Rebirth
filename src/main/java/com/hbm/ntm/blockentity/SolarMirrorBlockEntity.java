package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SolarMirrorBlockEntity extends BlockEntity {
    private BlockPos target = BlockPos.ZERO;
    private boolean on;

    public SolarMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_MIRROR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarMirrorBlockEntity mirror) {
        if (level.isClientSide) {
            mirror.clientTick(level, pos);
        } else {
            mirror.serverTick(level, pos, state);
        }
    }

    private void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean wasOn = on;
        on = computeActive(level, pos);
        if (on) {
            BlockEntity targetEntity = level.getBlockEntity(target.below());
            if (targetEntity instanceof SolarBoilerBlockEntity boiler) {
                int sun = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken() - 11;
                boiler.addHeat(sun);
            }
        }
        if (wasOn != on || level.getGameTime() % 20L == 0L) {
            setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void clientTick(Level level, BlockPos pos) {
        BlockEntity targetEntity = level.getBlockEntity(target.below());
        if (on && targetEntity instanceof SolarBoilerBlockEntity boiler) {
            boiler.registerSolarMirrorBeam(pos);
        }
    }

    private boolean computeActive(Level level, BlockPos pos) {
        if (target.getY() < pos.getY()) {
            return false;
        }
        int sun = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken() - 11;
        return sun > 0 && level.canSeeSky(pos.above())
                && level.getBlockEntity(target.below()) instanceof SolarBoilerBlockEntity;
    }

    public BlockPos getTarget() {
        return target;
    }

    public boolean isTargetAbove() {
        return target.getY() > worldPosition.getY();
    }

    public boolean isOn() {
        return on;
    }

    public void setTarget(BlockPos target) {
        if (this.target.equals(target)) {
            return;
        }
        this.target = target.immutable();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("targetX", target.getX());
        tag.putInt("targetY", target.getY());
        tag.putInt("targetZ", target.getZ());
        tag.putBoolean("isOn", on);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        target = new BlockPos(tag.getInt("targetX"), tag.getInt("targetY"), tag.getInt("targetZ"));
        on = tag.getBoolean("isOn");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-25, -25, -25), worldPosition.offset(26, 26, 26));
    }
}
