package com.hbm.ntm.entity.logic;

import com.hbm.ntm.energy.HbmEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public class EmpLogicEntity extends Entity {
    private static final int RADIUS = 100;
    private static final int LIFETIME = 10 * 60 * 20;
    private static final String TAG_AGE = "age";

    private List<BlockPos> machines;
    private int age;

    public EmpLogicEntity(EntityType<? extends EmpLogicEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (machines == null) {
            allocate();
        } else {
            shock();
        }
        age++;
        if (age > LIFETIME) {
            discard();
        }
    }

    private void allocate() {
        machines = new ArrayList<>();
        int originX = (int) getX();
        int originY = (int) getY();
        int originZ = (int) getZ();
        int radiusSquared = RADIUS * RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            int xSquared = x * x;
            for (int y = -RADIUS; y <= RADIUS; y++) {
                int ySquared = y * y;
                int worldY = originY + y;
                if (worldY < level().getMinBuildHeight() || worldY >= level().getMaxBuildHeight()) {
                    continue;
                }
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (xSquared + ySquared + z * z <= radiusSquared) {
                        cursor.set(originX + x, worldY, originZ + z);
                        if (level().isLoaded(cursor) && canEmp(level().getBlockEntity(cursor))) {
                            machines.add(cursor.immutable());
                        }
                    }
                }
            }
        }
    }

    private void shock() {
        for (BlockPos pos : machines) {
            BlockEntity blockEntity = level().isLoaded(pos) ? level().getBlockEntity(pos) : null;
            if (emp(blockEntity) && level().random.nextInt(20) == 0 && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
                                Blocks.PURPLE_STAINED_GLASS.defaultBlockState()),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        8, 0.25D, 0.25D, 0.25D, 0.05D);
            }
        }
    }

    private static boolean canEmp(BlockEntity blockEntity) {
        if (blockEntity instanceof HbmEnergyHandler) {
            return true;
        }
        if (blockEntity == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction).orElse(null);
            if (energy != null && energy.canExtract()) {
                return true;
            }
        }
        IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        return energy != null && energy.canExtract();
    }

    private static boolean emp(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return false;
        }
        boolean handled = false;
        if (blockEntity instanceof HbmEnergyHandler energyHandler) {
            energyHandler.setPower(0L);
            handled = true;
        }
        for (Direction direction : Direction.values()) {
            IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction).orElse(null);
            if (energy != null && energy.canExtract()) {
                energy.extractEnergy(energy.getEnergyStored(), false);
                handled = true;
            }
        }
        IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (energy != null && energy.canExtract()) {
            energy.extractEnergy(energy.getEnergyStored(), false);
            handled = true;
        }
        if (handled) {
            blockEntity.setChanged();
        }
        return handled;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt(TAG_AGE);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TAG_AGE, age);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
