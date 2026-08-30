package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.GenericDoorLogic;
import com.hbm.ntm.block.LegacyDoorDefinition;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.LegacyLockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Unregistered core of the 1.7.10 {@code TileEntityDoorGeneric} state
 * machine.  A future registered generic-door block must inject both its real
 * {@link BlockEntityType} and its {@link LegacyDoorDefinition}; this class
 * intentionally has no fallback type or guessed definition.
 */
public class GenericDoorBlockEntity extends BlockEntity {
    public static final byte STATE_CLOSED = 0;
    public static final byte STATE_OPEN = 1;
    public static final byte STATE_CLOSING = 2;
    public static final byte STATE_OPENING = 3;

    private static final String TAG_STATE = "state";
    private static final String TAG_OPEN_TICKS = "openTicks";
    private static final String TAG_ANIM_START_TIME = "animStartTime";
    private static final String TAG_REDSTONE = "redstoned";
    private static final String TAG_SHOULD_USE_BB = "shouldUseBB";
    private static final String TAG_SKIN = "skin";
    private static final String TAG_ACTIVATED_BLOCKS = "activatedBlocks";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    private final LegacyDoorDefinition definition;
    private final LegacyLockState lockState = new LegacyLockState();
    private final Set<BlockPos> activatedBlocks = new HashSet<>();

    private byte state = STATE_CLOSED;
    private int openTicks;
    private long animStartTime;
    private int redstonePower;
    private boolean shouldUseBoundingBox;
    private byte skinIndex;

    /**
     * Deliberate constructor injection prevents this pending class from ever
     * masquerading as a registered BlockEntity with a placeholder type.
     */
    public GenericDoorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  LegacyDoorDefinition definition) {
        super(Objects.requireNonNull(type, "type"), pos, state);
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    public LegacyDoorDefinition definition() {
        return definition;
    }

    public byte state() {
        return state;
    }

    public int openTicks() {
        return openTicks;
    }

    public long animStartTime() {
        return animStartTime;
    }

    public int redstonePower() {
        return redstonePower;
    }

    public boolean shouldUseBoundingBox() {
        return shouldUseBoundingBox;
    }

    public int skinIndex() {
        return Byte.toUnsignedInt(skinIndex);
    }

    public LegacyLockState lockState() {
        return lockState;
    }

    /** A stable copy of the cells currently counted as powered by the old redstone logic. */
    public Set<BlockPos> activatedRedstoneCells() {
        return Set.copyOf(activatedBlocks);
    }

    /**
     * The exact temporary extra-cell state that a future generic-door block
     * must reconcile with its dummies. Closed doors have no temporary cells;
     * open and transitional doors use the old range threshold.
     */
    public Set<BlockPos> dynamicOpenCells(Direction facing) {
        if (state == STATE_CLOSED) {
            return Set.of();
        }
        return GenericDoorLogic.openCells(definition, worldPosition, facing, openTicks);
    }

    /**
     * Mirrors the old range traversal's special self-cell branch. This is a
     * state output for the later dynamic-shape block, not a replacement for
     * any actual dummy placement.
     */
    public boolean dynamicRangeContainsCore(Direction facing) {
        return state != STATE_CLOSED
                && GenericDoorLogic.rangeContainsCore(definition, worldPosition, facing, openTicks);
    }

    public boolean tryToggle(@Nullable Player player) {
        if (lockState.isLocked() && player == null) {
            return false;
        }
        if (state == STATE_CLOSED && redstonePower > 0) {
            return false;
        }
        if (state == STATE_CLOSED) {
            if (!canAccess(player)) {
                return false;
            }
            setState(STATE_OPENING);
            return true;
        }
        if (state == STATE_OPEN) {
            if (!canAccess(player)) {
                return false;
            }
            setState(STATE_CLOSING);
            return true;
        }
        return false;
    }

    /** Source-compatible logic/remote passcode entry point. */
    public boolean tryToggle(int passcode) {
        if (lockState.isLocked() && passcode != lockState.pins()) {
            return false;
        }
        if (state == STATE_CLOSED) {
            setState(STATE_OPENING);
            return true;
        }
        if (state == STATE_OPEN) {
            setState(STATE_CLOSING);
            return true;
        }
        return false;
    }

    public void open() {
        if (state == STATE_CLOSED) {
            setState(STATE_OPENING);
        }
    }

    public void close() {
        if (state == STATE_OPEN) {
            setState(STATE_CLOSING);
        }
    }

    public boolean cycleSkinIndex() {
        if (!definition.hasSkins()) {
            return false;
        }
        skinIndex = (byte) ((Byte.toUnsignedInt(skinIndex) + 1) % definition.skinCount());
        stateChanged();
        return true;
    }

    public boolean tryApplyPadlock(@Nullable Player player, ItemStack held) {
        return lockState.tryApplyPadlock(player, held, lockHooks());
    }

    public boolean lock() {
        return lockState.lock(lockHooks());
    }

    public boolean unlock() {
        return lockState.unlock(lockHooks());
    }

    /** Exact old redstone activated-cell accounting, including its -1 falling-edge sentinel. */
    public void updateRedstonePower(BlockPos cell, boolean powered) {
        BlockPos immutableCell = Objects.requireNonNull(cell, "cell").immutable();
        boolean contained = activatedBlocks.contains(immutableCell);
        if (!contained && powered) {
            activatedBlocks.add(immutableCell);
            if (redstonePower == -1) {
                redstonePower = 0;
            }
            redstonePower++;
            stateChanged();
        } else if (contained && !powered) {
            activatedBlocks.remove(immutableCell);
            redstonePower--;
            if (redstonePower == 0) {
                redstonePower = -1;
            }
            stateChanged();
        }
    }

    /** Convenience adapter for a future block's neighbour callback. */
    public void updateRedstonePower(Level level, BlockPos cell) {
        updateRedstonePower(cell, level.hasNeighborSignal(cell));
    }

    /** Server ticker: transition timing and redstone edge response only. */
    public static void serverTick(Level level, BlockPos pos, BlockState blockState, GenericDoorBlockEntity door) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = false;
        if (door.state == STATE_OPENING) {
            door.openTicks++;
            if (door.openTicks >= door.definition.timeToOpen()) {
                door.openTicks = door.definition.timeToOpen();
            }
            changed = true;
        } else if (door.state == STATE_CLOSING) {
            door.openTicks--;
            if (door.openTicks <= 0) {
                door.openTicks = 0;
            }
            changed = true;
        }

        if (door.state == STATE_OPENING && door.openTicks == door.definition.timeToOpen()) {
            door.state = STATE_OPEN;
            changed = true;
        }
        if (door.state == STATE_CLOSING && door.openTicks == 0) {
            door.state = STATE_CLOSED;
            changed = true;
        }

        if (door.redstonePower == -1 && door.state == STATE_OPEN) {
            door.tryToggle(-1);
        } else if (door.redstonePower > 0 && door.state == STATE_CLOSED) {
            door.tryToggle(-1);
        }
        if (door.redstonePower == -1) {
            door.redstonePower = 0;
            changed = true;
        }
        if (changed) {
            door.stateChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(TAG_STATE, state);
        tag.putInt(TAG_OPEN_TICKS, openTicks);
        tag.putLong(TAG_ANIM_START_TIME, animStartTime);
        tag.putInt(TAG_REDSTONE, redstonePower);
        tag.putBoolean(TAG_SHOULD_USE_BB, shouldUseBoundingBox);
        if (definition.hasSkins()) {
            tag.putByte(TAG_SKIN, skinIndex);
        }
        ListTag cells = new ListTag();
        for (BlockPos cell : activatedBlocks) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(TAG_X, cell.getX());
            entry.putInt(TAG_Y, cell.getY());
            entry.putInt(TAG_Z, cell.getZ());
            cells.add(entry);
        }
        tag.put(TAG_ACTIVATED_BLOCKS, cells);
        lockState.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        state = readState(tag.getByte(TAG_STATE));
        openTicks = Math.max(0, Math.min(definition.timeToOpen(), tag.getInt(TAG_OPEN_TICKS)));
        animStartTime = tag.getLong(TAG_ANIM_START_TIME);
        redstonePower = tag.getInt(TAG_REDSTONE);
        shouldUseBoundingBox = tag.getBoolean(TAG_SHOULD_USE_BB);
        skinIndex = normalizeSkin(tag.getByte(TAG_SKIN));
        activatedBlocks.clear();
        for (Tag raw : tag.getList(TAG_ACTIVATED_BLOCKS, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            activatedBlocks.add(new BlockPos(entry.getInt(TAG_X), entry.getInt(TAG_Y), entry.getInt(TAG_Z)));
        }
        lockState.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return runtimeSnapshot();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        applyRuntimeSnapshot(tag);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            applyRuntimeSnapshot(tag);
        }
    }

    private CompoundTag runtimeSnapshot() {
        CompoundTag tag = new CompoundTag();
        tag.putByte(TAG_STATE, state);
        tag.putInt(TAG_OPEN_TICKS, openTicks);
        tag.putLong(TAG_ANIM_START_TIME, animStartTime);
        tag.putInt(TAG_REDSTONE, redstonePower);
        tag.putBoolean(TAG_SHOULD_USE_BB, shouldUseBoundingBox);
        tag.putByte(TAG_SKIN, skinIndex);
        return tag;
    }

    /** Reads only the runtime packet; it never calls {@link #load(CompoundTag)}. */
    private void applyRuntimeSnapshot(CompoundTag tag) {
        state = readState(tag.getByte(TAG_STATE));
        openTicks = Math.max(0, Math.min(definition.timeToOpen(), tag.getInt(TAG_OPEN_TICKS)));
        animStartTime = tag.getLong(TAG_ANIM_START_TIME);
        redstonePower = tag.getInt(TAG_REDSTONE);
        shouldUseBoundingBox = tag.getBoolean(TAG_SHOULD_USE_BB);
        skinIndex = normalizeSkin(tag.getByte(TAG_SKIN));
    }

    private boolean canAccess(@Nullable Player player) {
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        RandomSource random = level == null ? RandomSource.create() : level.random;
        return lockState.canAccess(player, held, random, lockHooks());
    }

    private LegacyLockState.Hooks lockHooks() {
        return new LegacyLockState.Hooks() {
            @Override
            public void stateChanged() {
                GenericDoorBlockEntity.this.stateChanged();
            }

            @Override
            public void playSound(Player player, String legacySoundId, float volume, float pitch) {
                LegacySoundPlayer.playSoundAtPlayer(player, legacySoundId, volume, pitch);
            }
        };
    }

    private void setState(byte newState) {
        if (state != newState) {
            state = newState;
            stateChanged();
        }
    }

    private void stateChanged() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            BlockState blockState = getBlockState();
            serverLevel.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    private byte normalizeSkin(byte skin) {
        if (!definition.hasSkins()) {
            return 0;
        }
        return (byte) Math.floorMod(Byte.toUnsignedInt(skin), definition.skinCount());
    }

    private static byte readState(byte value) {
        return value >= STATE_CLOSED && value <= STATE_OPENING ? value : STATE_CLOSED;
    }
}
