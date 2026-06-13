package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyChargeBlock;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LegacyChargeBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private static final String TAG_STARTED = "started";
    private static final String TAG_TIMER = "timer";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private boolean started;
    private int timer;

    public LegacyChargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_CHARGE.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyChargeBlockEntity blockEntity) {
        if (!blockEntity.started) {
            return;
        }

        blockEntity.timer--;
        if (blockEntity.timer > 0 && blockEntity.timer % 20 == 0) {
            level.playSound(null, pos, ModSounds.WEAPON_FSTBMB_PING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (blockEntity.timer <= 0) {
            if (state.getBlock() instanceof LegacyChargeBlock charge) {
                charge.detonate(level, pos);
            }
            return;
        }

        blockEntity.setChanged();
        if (blockEntity.timer % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public boolean isStarted() {
        return started;
    }

    public int getTimer() {
        return timer;
    }

    public void setStarted(boolean started) {
        this.started = started;
        sync();
    }

    public void setTimer(int timer) {
        this.timer = Math.max(0, timer);
        sync();
    }

    public void cycleTimer() {
        setTimer(switch (timer) {
            case 0 -> 100;
            case 100 -> 200;
            case 200 -> 300;
            case 300 -> 600;
            case 600 -> 1200;
            case 1200 -> 3600;
            case 3600 -> 6000;
            default -> 0;
        });
    }

    public String minutesDisplay() {
        return twoDigits(timer / 1200);
    }

    public String secondsDisplay() {
        return twoDigits((timer / 20) % 60);
    }

    public String timerDisplay() {
        return minutesDisplay() + ":" + secondsDisplay();
    }

    private static String twoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putBoolean(TAG_STARTED, started);
        tag.putInt(TAG_TIMER, timer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        started = tag.getBoolean(TAG_STARTED);
        timer = tag.getInt(TAG_TIMER);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeInt(timer);
        data.writeBoolean(started);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        timer = data.readInt();
        started = data.readBoolean();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
