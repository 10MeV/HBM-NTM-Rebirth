package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AutosawBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, LegacyLookOverlayProvider {
    private static final int MIN_DIST = 2;
    private static final int MAX_DIST = 9;
    private static final int FELL_HORIZONTAL_RANGE = 10;
    private static final int FELL_BFS_RADIUS = MAX_DIST + FELL_HORIZONTAL_RANGE;
    private static final int FELL_VERTICAL_RANGE = 32;
    private static final int FELL_MAX_BASE_DEPTH = FELL_VERTICAL_RANGE / 2;
    private static final int TANK_CAPACITY = 100;
    private static final double CUT_ANGLE = Math.toRadians(5.0D);
    private static final int[][] EIGHTEEN_DIRS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
    };
    private static final List<FluidType> FUELS = List.of(
            HbmFluids.WOODOIL,
            HbmFluids.ETHANOL,
            HbmFluids.FISHOIL,
            HbmFluids.HEAVYOIL,
            HbmFluids.COALCREOSOTE);
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(0, 0, -1, Direction.NORTH),
            FluidPort.of(0, 0, 1, Direction.SOUTH),
            FluidPort.of(-1, 0, 0, Direction.WEST),
            FluidPort.of(1, 0, 0, Direction.EAST),
            FluidPort.of(0, -1, 0, Direction.DOWN));

    private final HbmFluidTank tank;
    private boolean suspended;
    private boolean on;
    private int forceSkip;
    private int sawState;
    private float yaw;
    private float lastYaw;
    private float pitch;
    private float lastPitch;
    private float spin;
    private float lastSpin;
    private float targetYaw;
    private float targetPitch;
    private int turnProgress;
    private Object audioLoop;

    public AutosawBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WOODOIL, TANK_CAPACITY));
    }

    private AutosawBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.AUTOSAW.get(), pos, state, List.of(tank));
        this.tank = tank;
        this.tank.conform(new HbmFluidStack(HbmFluids.WOODOIL, 0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutosawBlockEntity autosaw) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, autosaw);
        boolean oldOn = autosaw.on;
        int oldFill = autosaw.tank.getFill();
        if (autosaw.suspended || !autosaw.acceptsFuel(autosaw.tank.getTankType())) {
            autosaw.on = false;
        } else if (level.getGameTime() % 20L == 0L) {
            autosaw.on = autosaw.tank.drain(1, false) > 0;
        }
        if (autosaw.on && !autosaw.suspended) {
            autosaw.runSaw(level, pos);
        }
        autosaw.networkPackNT(20);
        if (oldOn != autosaw.on || oldFill != autosaw.tank.getFill()) {
            autosaw.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AutosawBlockEntity autosaw) {
        autosaw.lastYaw = autosaw.yaw;
        autosaw.lastPitch = autosaw.pitch;
        autosaw.lastSpin = autosaw.spin;
        if (autosaw.turnProgress > 0) {
            autosaw.yaw += Mth.wrapDegrees(autosaw.targetYaw - autosaw.yaw) / autosaw.turnProgress;
            autosaw.pitch += Mth.wrapDegrees(autosaw.targetPitch - autosaw.pitch) / autosaw.turnProgress;
            autosaw.turnProgress--;
        } else {
            autosaw.yaw = autosaw.targetYaw;
            autosaw.pitch = autosaw.targetPitch;
        }
        if (autosaw.on) {
            autosaw.spin += 15.0F;
            if (autosaw.spin >= 360.0F) {
                autosaw.spin -= 360.0F;
                autosaw.lastSpin -= 360.0F;
            }
            if (level.getGameTime() % 4L == 0L) {
                Vec3 exhaust = new Vec3(0.625D, 0.0D, 1.625D).yRot((float) -Math.toRadians(autosaw.yaw));
                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + 0.5D + exhaust.x,
                        pos.getY() + 2.0625D,
                        pos.getZ() + 0.5D + exhaust.z,
                        0.0D, 0.05D, 0.0D);
            }
        }
        autosaw.audioLoop = LegacyMachineAudioBridge.updateLoop(autosaw.audioLoop, autosaw,
                "hbm:block.turbofanOperate", autosaw.on, 32.0D, 24.0F, 0.35F, 0.9F);
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public float getYaw(float partialTick) {
        return lastYaw + (yaw - lastYaw) * partialTick;
    }

    public float getPitch(float partialTick) {
        return lastPitch + (pitch - lastPitch) * partialTick;
    }

    public float getSpin(float partialTick) {
        return lastSpin + (spin - lastSpin) * partialTick;
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public float getYaw() {
        return yaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    public float getPitch() {
        return pitch;
    }

    public float getLastSpin() {
        return lastSpin;
    }

    public float getSpin() {
        return spin;
    }

    public void toggleSuspended() {
        suspended = !suspended;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void markFluidSettingsChanged() {
        if (!acceptsFuel(tank.getTankType())) {
            tank.setTankType(HbmFluids.WOODOIL);
        }
        onFluidContentsChanged();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!acceptsFuel(type)) {
            return amount;
        }
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return acceptsFuel(type) ? tank.getSpace() : 0L;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return acceptsFuel(type) && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.UP ? HbmFluidSideMode.NONE : HbmFluidSideMode.INPUT;
    }

    private boolean acceptsFuel(FluidType type) {
        return FUELS.contains(type);
    }

    private void runSaw(Level level, BlockPos pos) {
        lastYaw = yaw;
        lastPitch = pitch;
        lastSpin = spin;
        Vec3 tip = bladeTip(pos);
        AABB blade = new AABB(tip.x - 1.0D, tip.y - 0.25D, tip.z - 1.0D,
                tip.x + 1.0D, tip.y + 0.25D, tip.z + 1.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, blade)) {
            DamageSource source = level.damageSources().generic();
            if (EntityDamageUtil.attackEntityFromNt(living, source, 100.0F)) {
                level.playSound(null, living.blockPosition(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                        SoundSource.HOSTILE, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);
                int count = Math.min((int) Math.ceil(living.getMaxHealth() / 4.0F), 250);
                ParticleUtil.spawnVanillaRedstoneBlockDustBurst(level, living.getX(),
                        living.getY() + living.getBbHeight() * 0.5D, living.getZ(), count * 4, 0.1D);
            }
        }

        if (sawState == 0) {
            yaw += 1.0F;
            if (yaw >= 360.0F) {
                yaw -= 360.0F;
                lastYaw -= 360.0F;
            }
            if (forceSkip > 0) {
                forceSkip--;
            } else if (findTargetInSweep(level, pos)) {
                sawState = 1;
            }
        }

        int hitY = (int) Math.floor(tip.y);
        int hitX0 = (int) Math.floor(tip.x - 0.5D);
        int hitZ0 = (int) Math.floor(tip.z - 0.5D);
        int hitX1 = (int) Math.floor(tip.x + 0.5D);
        int hitZ1 = (int) Math.floor(tip.z + 0.5D);
        interactWithBlade(level, new BlockPos(hitX0, hitY, hitZ0));
        interactWithBlade(level, new BlockPos(hitX1, hitY, hitZ0));
        interactWithBlade(level, new BlockPos(hitX0, hitY, hitZ1));
        interactWithBlade(level, new BlockPos(hitX1, hitY, hitZ1));

        if (sawState == 1) {
            pitch += 2.0F;
            if (pitch > 80.0F) {
                pitch = 80.0F;
                sawState = 2;
            }
        }
        if (sawState == 2) {
            pitch -= 2.0F;
            if (pitch <= 0.0F) {
                pitch = 0.0F;
                sawState = 0;
            }
        }
    }

    private Vec3 bladeTip(BlockPos pos) {
        Vec3 pivot = new Vec3(pos.getX() + 0.5D, pos.getY() + 1.75D, pos.getZ() + 0.5D);
        float armAngle = (float) Math.toRadians(80.0F - pitch);
        float turn = (float) -Math.toRadians(yaw);
        Vec3 upperArm = new Vec3(0.0D, 0.0D, -4.0D).xRot(armAngle).yRot(turn);
        Vec3 lowerArm = new Vec3(0.0D, 0.0D, -4.0D).xRot(-armAngle).yRot(turn);
        Vec3 armTip = new Vec3(0.0D, 0.0D, -2.0D).yRot(turn);
        return pivot.add(upperArm).add(lowerArm).add(armTip);
    }

    private boolean findTargetInSweep(Level level, BlockPos pos) {
        double rotationYawRads = Math.toRadians((yaw + 270.0F) % 360.0F);
        for (int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
            for (int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
                int sqrDst = dx * dx + dz * dz;
                if (sqrDst <= MIN_DIST * MIN_DIST || sqrDst > MAX_DIST * MAX_DIST) {
                    continue;
                }
                double angle = Math.atan2(dz, dx);
                double relAngle = Math.abs(angle - rotationYawRads);
                relAngle = Math.abs((relAngle + Math.PI) % (2.0D * Math.PI) - Math.PI);
                if (relAngle > CUT_ANGLE) {
                    continue;
                }
                if (isSawTarget(level.getBlockState(pos.offset(dx, 1, dz)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void interactWithBlade(Level level, BlockPos pos) {
        if (tryCutBlock(level, pos)) {
            if (sawState == 1) {
                sawState = 2;
            }
            return;
        }
        if (sawState == 1 && level.getBlockState(pos).isSolidRender(level, pos)) {
            sawState = 2;
            forceSkip = 5;
        }
    }

    private boolean tryCutBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getPistonPushReaction() == PushReaction.BLOCK) {
            return false;
        }
        if (state.getBlock() instanceof BushBlock || state.is(BlockTags.LEAVES)) {
            cutCrop(level, pos, state);
            return true;
        }
        if (state.is(BlockTags.LOGS)) {
            fellTree(level, pos);
            return true;
        }
        return false;
    }

    private boolean isSawTarget(BlockState state) {
        return !shouldIgnore(state)
                && (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.getBlock() instanceof BushBlock);
    }

    private boolean shouldIgnore(BlockState state) {
        return false;
    }

    private void cutCrop(Level level, BlockPos pos, BlockState state) {
        level.levelEvent(2001, pos, Block.getId(state));
        Block.dropResources(state, level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void fellTree(Level level, BlockPos hit) {
        int sawY = hit.getY();
        BlockPos hitCol = columnKey(hit);
        Map<BlockPos, BlockPos> trunks = new HashMap<>();

        for (int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
            for (int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
                if (dx * dx + dz * dz > MAX_DIST * MAX_DIST) {
                    continue;
                }
                BlockPos scan = worldPosition.offset(dx, sawY - worldPosition.getY(), dz);
                if (!level.getBlockState(scan).is(BlockTags.LOGS)) {
                    continue;
                }
                BlockPos base = trunkBase(level, scan, sawY);
                BlockState sapling = saplingForLog(level.getBlockState(base));
                if (sapling != null && canPlaceSapling(level, base, sapling)) {
                    trunks.put(columnKey(scan), base);
                }
            }
        }

        trunks.putIfAbsent(hitCol, trunkBase(level, hit, sawY));

        Map<BlockPos, BlockPos> blockOwner = new HashMap<>();
        ArrayDeque<OwnedTreePos> deque = new ArrayDeque<>();
        for (Map.Entry<BlockPos, BlockPos> trunk : trunks.entrySet()) {
            deque.addFirst(new OwnedTreePos(trunk.getValue(), trunk.getKey()));
        }

        int minY = Math.max(level.getMinBuildHeight(), sawY - FELL_MAX_BASE_DEPTH);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, sawY + FELL_VERTICAL_RANGE);
        while (!deque.isEmpty()) {
            OwnedTreePos current = deque.pollFirst();
            if (blockOwner.containsKey(current.pos())) {
                continue;
            }
            blockOwner.put(current.pos(), current.ownerColumn());
            for (int[] dir : EIGHTEEN_DIRS) {
                BlockPos next = current.pos().offset(dir[0], dir[1], dir[2]);
                int neighborDx = next.getX() - worldPosition.getX();
                int neighborDz = next.getZ() - worldPosition.getZ();
                if (neighborDx * neighborDx + neighborDz * neighborDz > FELL_BFS_RADIUS * FELL_BFS_RADIUS
                        || next.getY() < minY || next.getY() > maxY || blockOwner.containsKey(next)) {
                    continue;
                }
                BlockState nextState = level.getBlockState(next);
                if (!nextState.is(BlockTags.LOGS) && !nextState.is(BlockTags.LEAVES)
                        && !(nextState.getBlock() instanceof LeavesBlock)) {
                    continue;
                }
                OwnedTreePos entry = new OwnedTreePos(next.immutable(), current.ownerColumn());
                if (dir[0] == 0 && dir[2] == 0) {
                    deque.addFirst(entry);
                } else {
                    deque.addLast(entry);
                }
            }
        }

        for (Map.Entry<BlockPos, BlockPos> entry : blockOwner.entrySet()) {
            if (!entry.getValue().equals(hitCol)) {
                continue;
            }
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) && isWithinWorkingArea(pos)) {
                BlockState sapling = saplingForLog(state);
                if (sapling != null && canPlaceSapling(level, pos, sapling)) {
                    level.destroyBlock(pos, true);
                    level.setBlock(pos, sapling, Block.UPDATE_ALL);
                    continue;
                }
            }
            level.destroyBlock(pos, true);
        }
    }

    private BlockPos trunkBase(Level level, BlockPos pos, int sawY) {
        BlockPos base = pos;
        while (sawY - base.getY() < FELL_MAX_BASE_DEPTH && level.getBlockState(base.below()).is(BlockTags.LOGS)) {
            base = base.below();
        }
        return base.immutable();
    }

    private boolean isWithinWorkingArea(BlockPos pos) {
        int dx = pos.getX() - worldPosition.getX();
        int dz = pos.getZ() - worldPosition.getZ();
        int distSq = dx * dx + dz * dz;
        return distSq > MIN_DIST * MIN_DIST && distSq <= MAX_DIST * MAX_DIST;
    }

    @Nullable
    private static BlockState saplingForLog(BlockState log) {
        Block block = log.getBlock();
        if (block == Blocks.SPRUCE_LOG || block == Blocks.SPRUCE_WOOD) {
            return Blocks.SPRUCE_SAPLING.defaultBlockState();
        }
        if (block == Blocks.BIRCH_LOG || block == Blocks.BIRCH_WOOD) {
            return Blocks.BIRCH_SAPLING.defaultBlockState();
        }
        if (block == Blocks.JUNGLE_LOG || block == Blocks.JUNGLE_WOOD) {
            return Blocks.JUNGLE_SAPLING.defaultBlockState();
        }
        if (block == Blocks.ACACIA_LOG || block == Blocks.ACACIA_WOOD) {
            return Blocks.ACACIA_SAPLING.defaultBlockState();
        }
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.DARK_OAK_WOOD) {
            return Blocks.DARK_OAK_SAPLING.defaultBlockState();
        }
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_WOOD) {
            return Blocks.OAK_SAPLING.defaultBlockState();
        }
        return null;
    }

    private static boolean canPlaceSapling(Level level, BlockPos pos, BlockState sapling) {
        return sapling.canSurvive(level, pos);
    }

    private static BlockPos columnKey(BlockPos pos) {
        return new BlockPos(pos.getX(), -1, pos.getZ());
    }

    private record OwnedTreePos(BlockPos pos, BlockPos ownerColumn) {
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, new int[] { tank.getTankType().getId() });
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable net.minecraft.world.entity.player.Player player,
            boolean recursive) {
        if (tag == null || !tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            return false;
        }
        java.util.OptionalInt id = HbmFluidCopiable.copiedFluidIdAt(tag, index);
        if (id.isEmpty()) {
            return false;
        }
        FluidType type = HbmFluids.fromId(id.getAsInt());
        if (!acceptsFuel(type)) {
            return false;
        }
        tank.setTankType(type);
        onFluidContentsChanged();
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        lines.add(tank.getTankType().getDisplayName().copy()
                .append(Component.literal(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB")
                        .withStyle(ChatFormatting.RESET)));
        if (suspended) {
            lines.add(Component.literal("! ")
                    .append(Component.translatable("block.hbm_ntm_rebirth.machine_autosaw.suspended"))
                    .append(Component.literal(" !"))
                    .withStyle(ChatFormatting.RED));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isSuspended", suspended);
        tag.putBoolean("isOn", on);
        tag.putInt("skip", forceSkip);
        tag.putInt("state", sawState);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        tank.writeToNbt(tag, "t");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        } else if (!tag.contains("hbm_fluids") && (tag.contains("tank") || tag.contains("tank_type"))) {
            tank.readFromNbt(tag, "tank");
        }
        suspended = tag.getBoolean("isSuspended");
        on = tag.getBoolean("isOn");
        forceSkip = tag.getInt("skip");
        sawState = tag.getInt("state");
        yaw = tag.getFloat("yaw");
        lastYaw = yaw;
        targetYaw = yaw;
        pitch = tag.getFloat("pitch");
        lastPitch = pitch;
        targetPitch = pitch;
        turnProgress = 0;
        if (!acceptsFuel(tank.getTankType())) {
            tank.setTankType(HbmFluids.WOODOIL);
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isOn", on);
        tag.putBoolean("isSuspended", suspended);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        tank.writeToNbt(tag, "t");
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        on = tag.getBoolean("isOn");
        suspended = tag.getBoolean("isSuspended");
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        }
        targetYaw = tag.getFloat("yaw");
        targetPitch = tag.getFloat("pitch");
        turnProgress = 3;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-12, 0, -12), worldPosition.offset(13, 10, 13));
    }
}
