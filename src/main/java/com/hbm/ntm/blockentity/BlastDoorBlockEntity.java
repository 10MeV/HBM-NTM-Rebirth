package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.BlastDoorBlock;
import com.hbm.ntm.network.HbmClientTileEventReceiver;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.LegacyLockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Exact gameplay carrier for 1.7.10 {@code TileEntityBlastDoor}.
 *
 * <p>Its permanently occupied cells are core and y+6. During opening only y+1..y+5 are removed;
 * during closing those same cells are restored in reverse order. This deliberately does not use
 * the generic DoorDecl state machine: old BlastDoor has its own five-second, vertical contract.</p>
 */
public class BlastDoorBlockEntity extends BlockEntity implements HbmClientTileEventReceiver {
    public static final int STATE_CLOSED = 0;
    public static final int STATE_MOVING = 1;
    public static final int STATE_OPEN = 2;
    private static final int TRANSITION_TICKS = 100;
    private static final int STEP_TICKS = 20;
    private static final ThreadLocal<Boolean> CLEARING_DUMMIES = ThreadLocal.withInitial(() -> false);

    private final LegacyLockState lockState = new LegacyLockState();
    private boolean opening;
    private int state;
    private long sysTime;
    private int timer;
    private boolean redstoned;
    private long clientAnimationStartMillis;

    public BlastDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_DOOR.get(), pos, state);
    }

    public static boolean isClearingDummies() {
        return CLEARING_DUMMIES.get();
    }

    public int state() {
        return state;
    }

    public boolean isOpening() {
        return opening;
    }

    public long clientAnimationStartMillis() {
        return clientAnimationStartMillis;
    }

    public boolean isLocked() {
        return lockState.isLocked();
    }

    public LegacyLockState lockState() {
        return lockState;
    }

    public boolean tryApplyPadlock(Player player, ItemStack held) {
        if (!lockState.tryApplyPadlock(player, held, lockHooks())) {
            return false;
        }
        lockNeighbors(new HashSet<>());
        runtimeChanged(false);
        return true;
    }

    public boolean tryToggle(@Nullable Player player) {
        if (state == STATE_CLOSED) {
            if (!canAccess(player)) {
                return false;
            }
            open();
            openNeighbors(new HashSet<>());
            return true;
        }
        if (state == STATE_OPEN) {
            if (!canAccess(player)) {
                return false;
            }
            close();
            closeNeighbors(new HashSet<>());
            return true;
        }
        return false;
    }

    public boolean createInitialFrameOrRemoveCore() {
        if (level == null || level.isClientSide) {
            return false;
        }
        if (worldPosition.getY() + 6 >= level.getMaxBuildHeight()) {
            level.destroyBlock(worldPosition, true);
            return false;
        }
        for (int y = 1; y <= 6; y++) {
            placeDummy(worldPosition.above(y));
        }
        return true;
    }

    public void removeAllDummies() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean previous = CLEARING_DUMMIES.get();
        CLEARING_DUMMIES.set(true);
        try {
            for (int y = 1; y <= 6; y++) {
                removeDummy(worldPosition.above(y));
            }
        } finally {
            CLEARING_DUMMIES.set(previous);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, BlastDoorBlockEntity door) {
        if (level.isClientSide) {
            return;
        }

        // Keep the original operator precedence: the core's redstone line honours the lock, the permanent top cell
        // is evaluated separately and therefore remains the old top-powered override.
        boolean corePowered = level.hasNeighborSignal(pos);
        boolean topPowered = level.hasNeighborSignal(pos.above(6));
        if ((!door.lockState.isLocked() && corePowered) || topPowered) {
            if (!door.redstoned) {
                door.tryToggle(null);
            }
            door.redstoned = true;
        } else {
            door.redstoned = false;
        }

        if (door.state != STATE_MOVING) {
            door.timer = 0;
        } else {
            door.timer++;
            if (door.opening) {
                if (door.timer >= 0) door.removeDummy(pos.above(1));
                if (door.timer >= STEP_TICKS) door.removeDummy(pos.above(2));
                if (door.timer >= STEP_TICKS * 2) door.removeDummy(pos.above(3));
                if (door.timer >= STEP_TICKS * 3) door.removeDummy(pos.above(4));
                if (door.timer >= STEP_TICKS * 4) door.removeDummy(pos.above(5));
            } else {
                if (door.timer >= STEP_TICKS) door.placeDummy(pos.above(5));
                if (door.timer >= STEP_TICKS * 2) door.placeDummy(pos.above(4));
                if (door.timer >= STEP_TICKS * 3) door.placeDummy(pos.above(3));
                if (door.timer >= STEP_TICKS * 4) door.placeDummy(pos.above(2));
                if (door.timer >= TRANSITION_TICKS) door.placeDummy(pos.above(1));
            }
            if (door.timer >= TRANSITION_TICKS) {
                if (door.opening) {
                    door.finishOpen();
                } else {
                    door.finishClose();
                }
            }
        }
        // Tiled legacy packet cadence is intentional: it feeds late tracking clients without invoking load(empty).
        door.runtimeChanged(false);
    }

    private void open() {
        if (state != STATE_CLOSED) {
            return;
        }
        opening = true;
        state = STATE_MOVING;
        sysTime = System.currentTimeMillis();
        LegacySoundPlayer.playLegacyReactorStart(level, worldPosition, 0.5F, 0.75F);
        runtimeChanged(true);
    }

    private void close() {
        if (state != STATE_OPEN) {
            return;
        }
        opening = false;
        state = STATE_MOVING;
        sysTime = System.currentTimeMillis();
        LegacySoundPlayer.playLegacyReactorStart(level, worldPosition, 0.5F, 0.75F);
        runtimeChanged(true);
    }

    private void finishOpen() {
        state = STATE_OPEN;
        LegacySoundPlayer.playSoundEffect(level, worldPosition, "hbm:block.reactorStop", 0.5F, 1.0F);
    }

    private void finishClose() {
        state = STATE_CLOSED;
        LegacySoundPlayer.playSoundEffect(level, worldPosition, "hbm:block.reactorStop", 0.5F, 1.0F);
    }

    private boolean canAccess(@Nullable Player player) {
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        RandomSource random = level == null ? RandomSource.create() : level.random;
        return lockState.canAccess(player, held, random, lockHooks());
    }

    private void openNeighbors(Set<BlockPos> visited) {
        if (!visited.add(worldPosition)) return;
        forEachDoorNeighbor(door -> {
            if (door.state == STATE_CLOSED && (!door.lockState.isLocked()
                    || door.lockState.pins() == lockState.pins())) {
                door.open();
                door.openNeighbors(visited);
            }
        });
    }

    private void closeNeighbors(Set<BlockPos> visited) {
        if (!visited.add(worldPosition)) return;
        forEachDoorNeighbor(door -> {
            if (door.state == STATE_OPEN && (!door.lockState.isLocked()
                    || door.lockState.pins() == lockState.pins())) {
                door.close();
                door.closeNeighbors(visited);
            }
        });
    }

    private void lockNeighbors(Set<BlockPos> visited) {
        if (!visited.add(worldPosition)) return;
        forEachDoorNeighbor(door -> {
            if (!door.lockState.isLocked()) {
                door.lockState.setPins(lockState.pins());
                door.lockState.setLockModifier(lockState.lockModifier());
                door.lockState.lock(door.lockHooks());
                door.lockNeighbors(visited);
            }
        });
    }

    private void forEachDoorNeighbor(java.util.function.Consumer<BlastDoorBlockEntity> consumer) {
        if (level == null) return;
        for (var direction : new net.minecraft.core.Direction[]{net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.NORTH}) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof BlastDoorBlockEntity door) {
                consumer.accept(door);
            }
        }
    }

    private void placeDummy(BlockPos pos) {
        if (level == null || pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) return;
        BlockState current = level.getBlockState(pos);
        if (!current.canBeReplaced() && !current.is(ModBlocks.BLAST_DOOR_DUMMY.get())) {
            level.destroyBlock(pos, false);
        }
        if (!level.getBlockState(pos).is(ModBlocks.BLAST_DOOR_DUMMY.get())) {
            level.setBlock(pos, ModBlocks.BLAST_DOOR_DUMMY.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (level.getBlockEntity(pos) instanceof BlastDoorDummyBlockEntity dummy) {
            dummy.setCorePos(worldPosition);
        }
    }

    private void removeDummy(BlockPos pos) {
        if (level != null && level.getBlockState(pos).is(ModBlocks.BLAST_DOOR_DUMMY.get())) {
            boolean previous = CLEARING_DUMMIES.get();
            CLEARING_DUMMIES.set(true);
            try {
                level.removeBlock(pos, false);
            } finally {
                CLEARING_DUMMIES.set(previous);
            }
        }
    }

    private LegacyLockState.Hooks lockHooks() {
        return new LegacyLockState.Hooks() {
            @Override public void stateChanged() { BlastDoorBlockEntity.this.runtimeChanged(false); }
            @Override public void playSound(Player player, String id, float volume, float pitch) {
                LegacySoundPlayer.playSoundAtEntity(player, id, net.minecraft.sounds.SoundSource.BLOCKS, volume, pitch);
            }
        };
    }

    private void runtimeChanged(boolean resetClientTime) {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            BlockState current = getBlockState();
            serverLevel.sendBlockUpdated(worldPosition, current, current, Block.UPDATE_CLIENTS);
            ModMessages.sendVaultDoorEvent(this, opening, state, resetClientTime, 0);
        }
    }

    @Override
    public void handleClientTileEvent(net.minecraft.resources.ResourceLocation eventType, CompoundTag data) {
        if (!HbmNetworkActions.VAULT_DOOR.equals(eventType)) return;
        opening = data.getBoolean("opening");
        state = clampState(data.getInt("state"));
        if (data.getBoolean("resetClientTime")) {
            clientAnimationStartMillis = System.currentTimeMillis();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isOpening", opening);
        tag.putInt("state", state);
        tag.putLong("sysTime", sysTime);
        tag.putInt("timer", timer);
        tag.putBoolean("redstoned", redstoned);
        lockState.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        opening = tag.getBoolean("isOpening");
        state = clampState(tag.getInt("state"));
        sysTime = tag.getLong("sysTime");
        timer = Math.max(0, Math.min(TRANSITION_TICKS, tag.getInt("timer")));
        redstoned = tag.getBoolean("redstoned");
        lockState.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() { return runtimeSnapshot(); }
    @Override public void handleUpdateTag(CompoundTag tag) { applyRuntimeSnapshot(tag); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) applyRuntimeSnapshot(packet.getTag());
    }

    private CompoundTag runtimeSnapshot() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isOpening", opening);
        tag.putInt("state", state);
        tag.putLong("sysTime", sysTime);
        tag.putInt("timer", timer);
        tag.putBoolean("redstoned", redstoned);
        return tag;
    }

    private void applyRuntimeSnapshot(CompoundTag tag) {
        opening = tag.getBoolean("isOpening");
        state = clampState(tag.getInt("state"));
        sysTime = tag.getLong("sysTime");
        timer = Math.max(0, Math.min(TRANSITION_TICKS, tag.getInt("timer")));
        redstoned = tag.getBoolean("redstoned");
        if (state == STATE_MOVING && clientAnimationStartMillis == 0L) clientAnimationStartMillis = System.currentTimeMillis();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 7, 1));
    }

    private static int clampState(int state) {
        return state >= STATE_CLOSED && state <= STATE_OPEN ? state : STATE_CLOSED;
    }
}
