package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ICFControllerBlockEntity extends HbmEnergyBlockEntity implements LegacyLookOverlayProvider {
    public static final int CAPACITOR_POWER = 2_500_000;
    public static final int TURBO_POWER = 5_000_000;
    private static final String TAG_POWER = "power";
    private static final String TAG_CAPACITOR_COUNT = "capacitorCount";
    private static final String TAG_TURBOCHARGER_COUNT = "turbochargerCount";
    private static final String TAG_LASER_LENGTH = "laserLength";

    private final List<BlockPos> ports = new ArrayList<>();
    private int laserLength;
    private int cellCount;
    private int emitterCount;
    private int capacitorCount;
    private int turbochargerCount;
    private boolean assembled;

    public ICFControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICF_CONTROLLER.get(), pos, state, new HbmEnergyStorage(0L, Long.MAX_VALUE, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFControllerBlockEntity controller) {
        controller.energy.setMaxPower(controller.getMaxPower());
        controller.energy.setTransferRates(controller.getMaxPower(), 0L);
        controller.networkPackNT(50);
        if (controller.assembled) {
            if (controller.getMaxPower() > 0L) {
                HbmEnergyUtil.subscribeReceiverToPorts(level, pos, controller.getEnergyPorts(), controller.energy);
            }
            if (controller.getPower() > 0L) {
                controller.fireLaser(level, state);
                controller.setPower(0L);
            } else {
                controller.laserLength = 0;
            }
        } else {
            controller.laserLength = 0;
        }
        if (level.getGameTime() % 20L == 0L) {
            controller.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ICFControllerBlockEntity controller) {
        if (controller.laserLength <= 0 || level.random.nextInt(5) != 0) {
            return;
        }
        Direction dir = state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING);
        Direction rot = dir.getClockWise();
        double offXZ = level.random.nextDouble() * 0.25D - 0.125D;
        double offY = level.random.nextDouble() * 0.25D - 0.125D;
        double dist = 0.55D;
        ParticleUtil.spawnVanillaExtRedDust(level,
                pos.getX() + 0.5D + dir.getStepX() * dist + rot.getStepX() * offXZ,
                pos.getY() + 0.5D + offY,
                pos.getZ() + 0.5D + dir.getStepZ() * dist + rot.getStepZ() * offXZ);
    }

    public void setup(Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters,
            Set<BlockPos> capacitors, Set<BlockPos> turbochargers) {
        this.cellCount = 0;
        this.emitterCount = 0;
        this.capacitorCount = 0;
        this.turbochargerCount = 0;
        this.ports.clear();

        Direction dir = getBlockState().getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING).getOpposite();
        Set<BlockPos> validCells = new HashSet<>();
        Set<BlockPos> validEmitters = new HashSet<>();
        Set<BlockPos> validCapacitors = new HashSet<>();
        for (int i = 0; i < cells.size(); i++) {
            BlockPos candidate = worldPosition.relative(dir, i + 1);
            if (cells.contains(candidate)) {
                cellCount++;
                validCells.add(candidate);
            } else {
                break;
            }
        }
        for (BlockPos emitter : emitters) {
            if (touchesAny(emitter, validCells)) {
                emitterCount++;
                validEmitters.add(emitter);
            }
        }
        for (BlockPos capacitor : capacitors) {
            if (touchesAny(capacitor, validEmitters)) {
                capacitorCount++;
                validCapacitors.add(capacitor);
            }
        }
        for (BlockPos turbo : turbochargers) {
            if (touchesAny(turbo, validCapacitors)) {
                turbochargerCount++;
            }
        }
        this.ports.addAll(ports);
        assembled = true;
        setChanged();
    }

    public void clearAssembly() {
        ports.clear();
        cellCount = 0;
        emitterCount = 0;
        capacitorCount = 0;
        turbochargerCount = 0;
        assembled = false;
        setChanged();
    }

    @Override
    public long getMaxPower() {
        return (long) (Math.sqrt(capacitorCount) * HbmCommonConfig.icfLaserCapacitorPower()
                + Math.sqrt(Math.min(turbochargerCount, capacitorCount)) * HbmCommonConfig.icfLaserTurboPower());
    }

    @Override
    public long getPower() {
        return Math.min(super.getPower(), getMaxPower());
    }

    public int getLaserLength() {
        return laserLength;
    }

    public int getCapacitorCount() {
        return capacitorCount;
    }

    public int getTurbochargerCount() {
        return turbochargerCount;
    }

    public boolean isAssembled() {
        return assembled;
    }

    public void setAssembled(boolean assembled) {
        this.assembled = assembled;
        setChanged();
    }

    public boolean canUse(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 256.0D;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(Component.literal(
                LegacyLookOverlayLines.shortNumber(getPower()) + "/"
                        + LegacyLookOverlayLines.shortNumber(getMaxPower()) + "HE")));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = getBlockState().getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING);
        List<EnergyPort> energyPorts = new ArrayList<>();
        for (BlockPos port : ports) {
            BlockPos relative = port.subtract(worldPosition);
            for (Direction side : Direction.values()) {
                energyPorts.add(new EnergyPort(relative.relative(side), side));
            }
        }
        return energyPorts;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() + 0.5D - 50.0D, worldPosition.getY(),
                worldPosition.getZ() + 0.5D - 50.0D,
                worldPosition.getX() + 0.5D + 50.0D, worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D + 50.0D);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, legacySyncedPower());
        tag.putInt(TAG_CAPACITOR_COUNT, capacitorCount);
        tag.putInt(TAG_TURBOCHARGER_COUNT, turbochargerCount);
        tag.putInt(TAG_LASER_LENGTH, laserLength);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_CAPACITOR_COUNT)) {
            capacitorCount = tag.getInt(TAG_CAPACITOR_COUNT);
        }
        if (tag.contains(TAG_TURBOCHARGER_COUNT)) {
            turbochargerCount = tag.getInt(TAG_TURBOCHARGER_COUNT);
        }
        energy.setMaxPower(getMaxPower());
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
        if (tag.contains(TAG_LASER_LENGTH)) {
            laserLength = tag.getInt(TAG_LASER_LENGTH);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeLong(legacySyncedPower());
        data.writeInt(capacitorCount);
        data.writeInt(turbochargerCount);
        data.writeInt(laserLength);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        long syncedPower = data.readLong();
        capacitorCount = data.readInt();
        turbochargerCount = data.readInt();
        laserLength = data.readInt();
        energy.setMaxPower(getMaxPower());
        setPower(syncedPower);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, legacySyncedPower());
        tag.putBoolean("assembled", assembled);
        tag.putInt("cellCount", cellCount);
        tag.putInt("emitterCount", emitterCount);
        tag.putInt("capacitorCount", capacitorCount);
        tag.putInt("turbochargerCount", turbochargerCount);
        ListTag list = new ListTag();
        tag.putInt("portCount", ports.size());
        for (int i = 0; i < ports.size(); i++) {
            BlockPos port = ports.get(i);
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", port.getX());
            entry.putInt("y", port.getY());
            entry.putInt("z", port.getZ());
            list.add(entry);
            tag.putIntArray("p" + i, new int[] {port.getX(), port.getY(), port.getZ()});
        }
        tag.put("ports", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        assembled = tag.getBoolean("assembled");
        cellCount = tag.getInt("cellCount");
        emitterCount = tag.getInt("emitterCount");
        capacitorCount = tag.getInt("capacitorCount");
        turbochargerCount = tag.getInt("turbochargerCount");
        energy.setMaxPower(getMaxPower());
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        } else if (tag.contains("Energy")) {
            setPower(tag.getCompound("Energy").getLong(HbmEnergyStorage.DEFAULT_POWER_TAG));
        }
        ports.clear();
        ListTag list = tag.getList("ports", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ports.add(new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")));
        }
        if (ports.isEmpty() && tag.contains("portCount")) {
            int portCount = tag.getInt("portCount");
            for (int i = 0; i < portCount; i++) {
                int[] port = tag.getIntArray("p" + i);
                if (port.length >= 3) {
                    ports.add(new BlockPos(port[0], port[1], port[2]));
                }
            }
        }
    }

    private void fireLaser(Level level, BlockState state) {
        Direction dir = state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING);
        for (int i = 1; i < 50; i++) {
            laserLength = i;
            BlockPos scan = worldPosition.relative(dir, i);
            BlockState scanState = level.getBlockState(scan);
            if (scanState.is(ModBlocks.ICF.get())) {
                BlockPos corePos = scan.relative(dir, 8).below(3);
                if (level.getBlockEntity(corePos) instanceof ICFReactorBlockEntity reactor) {
                    reactor.receiveLaser(getPower(), getMaxPower());
                }
                break;
            }
            if (!scanState.isAir()) {
                if (scanState.getBlock().getExplosionResistance() < 6000.0F) {
                    level.destroyBlock(scan, false);
                }
                break;
            }
        }
        AABB beam = beamAabb(dir, laserLength);
        for (Entity entity : level.getEntities(null, beam)) {
            entity.hurt(level.damageSources().inFire(), 50.0F);
            entity.setSecondsOnFire(5);
        }
    }

    private AABB beamAabb(Direction dir, int length) {
        double x1 = Math.min(worldPosition.getX(), worldPosition.getX() + dir.getStepX() * length) + 0.2D;
        double x2 = Math.max(worldPosition.getX(), worldPosition.getX() + dir.getStepX() * length) + 0.8D;
        double z1 = Math.min(worldPosition.getZ(), worldPosition.getZ() + dir.getStepZ() * length) + 0.2D;
        double z2 = Math.max(worldPosition.getZ(), worldPosition.getZ() + dir.getStepZ() * length) + 0.8D;
        return new AABB(x1, worldPosition.getY() + 0.2D, z1, x2, worldPosition.getY() + 0.8D, z2);
    }

    private long legacySyncedPower() {
        return super.getPower();
    }

    private static boolean touchesAny(BlockPos pos, Set<BlockPos> valid) {
        for (Direction direction : Direction.values()) {
            if (valid.contains(pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
