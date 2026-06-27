package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ICFControllerBlockEntity extends HbmEnergyBlockEntity
        implements LegacyLookOverlayProvider, IInfoProviderEC {
    public static final int CAPACITOR_POWER = 2_500_000;
    public static final int TURBO_POWER = 5_000_000;
    private static final String TAG_POWER = "power";
    private static final String TAG_CAPACITOR_COUNT = "capacitorCount";
    private static final String TAG_TURBOCHARGER_COUNT = "turbochargerCount";
    private static final String TAG_LASER_LENGTH = "laserLength";
    private static final int ENERGY_SUBSCRIPTION_KEEPALIVE_TICKS = 20;

    private final List<BlockPos> ports = new ArrayList<>();
    private final List<BlockPos> assembledParts = new ArrayList<>();
    private List<EnergyPort> cachedEnergyPorts = List.of();
    private int laserLength;
    private int cellCount;
    private int emitterCount;
    private int capacitorCount;
    private int turbochargerCount;
    private long power;
    private boolean assembled;
    private boolean energySubscriptionDirty = true;
    private boolean energyReceiverSubscribed;
    private boolean restoringAssembly;
    private int lastEnergyPortSignature = Integer.MIN_VALUE;

    public ICFControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICF_CONTROLLER.get(), pos, state, new LegacyICFEnergyStorage());
        ((LegacyICFEnergyStorage) energy).bind(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFControllerBlockEntity controller) {
        controller.energy.setMaxPower(controller.getMaxPower());
        controller.energy.setTransferRates(controller.getMaxPower(), 0L);
        controller.ensureAssemblyPartsKnown(level);
        controller.refreshEnergyPortSubscription(level, pos);
        if (controller.assembled) {
            if (controller.legacySyncedPower() > 0L) {
                controller.fireLaser(level, state);
                controller.setPower(0L);
                controller.setChanged();
            } else {
                controller.laserLength = 0;
            }
        } else {
            controller.laserLength = 0;
        }
        controller.networkPackNT(50);
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

    public void setup(Set<BlockPos> assembledParts, Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters,
            Set<BlockPos> capacitors, Set<BlockPos> turbochargers) {
        this.cellCount = 0;
        this.emitterCount = 0;
        this.capacitorCount = 0;
        this.turbochargerCount = 0;
        this.ports.clear();
        this.assembledParts.clear();

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
        this.assembledParts.addAll(assembledParts);
        this.ports.addAll(ports);
        rebuildCachedEnergyPorts();
        markEnergySubscriptionDirty();
        assembled = true;
        setChanged();
    }

    public void clearAssembly() {
        clearAssemblyState();
        setChanged();
    }

    public void restoreAssembly() {
        if (restoringAssembly) {
            return;
        }
        if (level != null && !level.isClientSide) {
            ensureAssemblyPartsKnown(level);
            restoringAssembly = true;
            try {
                for (BlockPos partPos : List.copyOf(assembledParts)) {
                    if (!level.hasChunk(partPos.getX() >> 4, partPos.getZ() >> 4)) {
                        continue;
                    }
                    if (level.getBlockEntity(partPos) instanceof ICFAssembledBlockEntity assembledBlock
                            && assembledBlock.isLinkedTo(worldPosition)) {
                        assembledBlock.restoreOriginalBlock();
                    }
                }
            } finally {
                restoringAssembly = false;
            }
        }
        clearAssemblyState();
        setChanged();
    }

    private void clearAssemblyState() {
        if (energyReceiverSubscribed && level != null && !level.isClientSide) {
            HbmEnergyUtil.unsubscribeReceiverFromPorts(level, worldPosition, getEnergyPorts(), energy);
        }
        ports.clear();
        assembledParts.clear();
        cachedEnergyPorts = List.of();
        cellCount = 0;
        emitterCount = 0;
        capacitorCount = 0;
        turbochargerCount = 0;
        assembled = false;
        energyReceiverSubscribed = false;
        markEnergySubscriptionDirty();
    }

    @Override
    public long getMaxPower() {
        return (long) (Math.sqrt(capacitorCount) * HbmCommonConfig.icfLaserCapacitorPower()
                + Math.sqrt(Math.min(turbochargerCount, capacitorCount)) * HbmCommonConfig.icfLaserTurboPower());
    }

    @Override
    public long getPower() {
        return Math.min(power, getMaxPower());
    }

    @Override
    public void setPower(long power) {
        this.power = power;
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
        if (this.assembled && !assembled) {
            restoreAssembly();
            return;
        }
        this.assembled = assembled;
        if (!assembled) {
            clearAssemblyState();
        }
        markEnergySubscriptionDirty();
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
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, assembled && legacySyncedPower() > 0L);
        data.putInt(CompatEnergyControl.I_LASER_LENGTH, laserLength);
        data.putInt(CompatEnergyControl.I_CELL_COUNT, cellCount);
        data.putInt(CompatEnergyControl.I_EMITTER_COUNT, emitterCount);
        data.putInt(CompatEnergyControl.I_CAPACITOR_COUNT, capacitorCount);
        data.putInt(CompatEnergyControl.I_TURBOCHARGER_COUNT, turbochargerCount);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return cachedEnergyPorts;
    }

    private void rebuildCachedEnergyPorts() {
        List<EnergyPort> energyPorts = new ArrayList<>(ports.size() * Direction.values().length);
        for (BlockPos port : ports) {
            BlockPos relative = port.subtract(worldPosition);
            for (Direction side : Direction.values()) {
                energyPorts.add(new EnergyPort(relative.relative(side), side));
            }
        }
        cachedEnergyPorts = List.copyOf(energyPorts);
    }

    private void refreshEnergyPortSubscription(Level level, BlockPos pos) {
        boolean receiverActive = assembled && getMaxPower() > 0L;
        if (!shouldRefreshEnergyPortSubscription(level, receiverActive)) {
            return;
        }
        if (energyReceiverSubscribed && !receiverActive) {
            HbmEnergyUtil.unsubscribeReceiverFromPorts(level, pos, getEnergyPorts(), energy);
        }
        if (receiverActive) {
            subscribeEnergyReceiverToPorts(getEnergyPorts(), energy);
        }
        energyReceiverSubscribed = receiverActive;
        lastEnergyPortSignature = energyPortSignature(getEnergyPorts());
        energySubscriptionDirty = false;
    }

    private boolean shouldRefreshEnergyPortSubscription(Level level, boolean receiverActive) {
        int portSignature = energyPortSignature(getEnergyPorts());
        return energySubscriptionDirty
                || energyReceiverSubscribed != receiverActive
                || portSignature != lastEnergyPortSignature
                || Math.floorMod(level.getGameTime() + worldPosition.hashCode(), ENERGY_SUBSCRIPTION_KEEPALIVE_TICKS) == 0L;
    }

    private void markEnergySubscriptionDirty() {
        energySubscriptionDirty = true;
        markEnergyPortSubscriptionsDirty();
    }

    private static int energyPortSignature(Iterable<EnergyPort> ports) {
        int signature = 1;
        if (ports != null) {
            for (EnergyPort port : ports) {
                signature = 31 * signature + (port == null ? 0 : port.hashCode());
            }
        }
        return signature;
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
        ListTag assembledPartList = new ListTag();
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
        for (BlockPos part : assembledParts) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", part.getX());
            entry.putInt("y", part.getY());
            entry.putInt("z", part.getZ());
            assembledPartList.add(entry);
        }
        tag.put("assembledParts", assembledPartList);
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
        rebuildCachedEnergyPorts();
        assembledParts.clear();
        ListTag partList = tag.getList("assembledParts", Tag.TAG_COMPOUND);
        for (int i = 0; i < partList.size(); i++) {
            CompoundTag entry = partList.getCompound(i);
            assembledParts.add(new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")));
        }
        markEnergySubscriptionDirty();
    }

    private void ensureAssemblyPartsKnown(Level level) {
        if (!assembled || !assembledParts.isEmpty() || level.isClientSide) {
            return;
        }
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        boolean rebuildPorts = ports.isEmpty();
        for (Direction direction : Direction.values()) {
            queue.add(worldPosition.relative(direction));
        }
        while (!queue.isEmpty() && visited.size() < 1024) {
            BlockPos current = queue.remove();
            if (!visited.add(current) || !level.getBlockState(current).is(ModBlocks.ICF_BLOCK.get())) {
                continue;
            }
            if (!(level.getBlockEntity(current) instanceof ICFAssembledBlockEntity assembledBlock)
                    || !assembledBlock.isLinkedTo(worldPosition)) {
                continue;
            }
            assembledParts.add(current.immutable());
            if (rebuildPorts && assembledBlock.isPort()) {
                ports.add(current.immutable());
            }
            for (Direction direction : Direction.values()) {
                queue.add(current.relative(direction));
            }
        }
        if (!assembledParts.isEmpty()) {
            rebuildCachedEnergyPorts();
            markEnergySubscriptionDirty();
            setChanged();
        }
    }

    private void fireLaser(Level level, BlockState state) {
        Direction dir = state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING);
        for (int i = 1; i < 50; i++) {
            laserLength = i;
            BlockPos scan = worldPosition.relative(dir, i);
            BlockState scanState = level.getBlockState(scan);
            if (MultiblockHelper.resolveCoreBlockEntity(level, scan) instanceof ICFReactorBlockEntity reactor) {
                reactor.receiveLaser(getPower(), getMaxPower());
                break;
            }
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
        double y1 = Math.min(worldPosition.getY(), worldPosition.getY() + dir.getStepY() * length) + 0.2D;
        double y2 = Math.max(worldPosition.getY(), worldPosition.getY() + dir.getStepY() * length) + 0.8D;
        double z1 = Math.min(worldPosition.getZ(), worldPosition.getZ() + dir.getStepZ() * length) + 0.2D;
        double z2 = Math.max(worldPosition.getZ(), worldPosition.getZ() + dir.getStepZ() * length) + 0.8D;
        return new AABB(x1, y1, z1, x2, y2, z2);
    }

    private long legacySyncedPower() {
        return power;
    }

    private static final class LegacyICFEnergyStorage extends HbmEnergyStorage {
        private ICFControllerBlockEntity owner;

        private LegacyICFEnergyStorage() {
            super(0L, Long.MAX_VALUE, 0L);
        }

        private void bind(ICFControllerBlockEntity owner) {
            this.owner = owner;
        }

        @Override
        public long getPower() {
            return owner == null ? super.getPower() : owner.getPower();
        }

        @Override
        public void setPower(long power) {
            if (owner == null) {
                super.setPower(power);
            } else {
                owner.setPower(power);
            }
        }

        @Override
        public long getMaxPower() {
            return owner == null ? super.getMaxPower() : owner.getMaxPower();
        }

        @Override
        public void setMaxPower(long maxPower) {
            // Legacy TileEntityICFController keeps raw power even when assembled capacity changes.
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putLong(DEFAULT_POWER_TAG, owner == null ? super.getPower() : owner.legacySyncedPower());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            setPower(tag.getLong(DEFAULT_POWER_TAG));
        }
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
