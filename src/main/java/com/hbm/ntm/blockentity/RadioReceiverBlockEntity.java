package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.menu.RadioReceiverMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.NoteBuilder;
import com.hbm.ntm.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RadioReceiverBlockEntity extends BlockEntity
        implements MenuProvider, HbmLegacyLoadedTile, HbmLegacyControlReceiver {
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_IS_ON = "isOn";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private String channel = "";
    private boolean on;

    public RadioReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIOREC.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioReceiverBlockEntity receiver) {
        if (receiver.on && !receiver.channel.isEmpty()) {
            RTTYSystem.RTTYChannel rttyChannel = RTTYSystem.listen(level, receiver.channel);
            if (rttyChannel != null && rttyChannel.timeStamp() == level.getGameTime() - 1L) {
                receiver.playSignal(rttyChannel.signalString());
            }
        }
        receiver.networkPackNT(15);
    }

    public String channel() {
        return channel;
    }

    public boolean isOn() {
        return on;
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) < 256.0D;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        boolean changed = false;
        if (data.contains(TAG_CHANNEL)) {
            channel = cleanChannel(data.getString(TAG_CHANNEL));
            changed = true;
        }
        if (data.contains(TAG_IS_ON)) {
            on = data.getBoolean(TAG_IS_ON);
            changed = true;
        }
        if (changed) {
            setChangedAndSync();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadioReceiverMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putString(TAG_CHANNEL, channel);
        tag.putBoolean(TAG_IS_ON, on);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        channel = cleanChannel(tag.getString(TAG_CHANNEL));
        on = tag.getBoolean(TAG_IS_ON);
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
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
        tag.putString(TAG_CHANNEL, channel);
        tag.putBoolean(TAG_IS_ON, on);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        channel = cleanChannel(tag.getString(TAG_CHANNEL));
        on = tag.getBoolean(TAG_IS_ON);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 1.0D, pos.getY(), pos.getZ() - 1.0D, pos.getX() + 2.0D,
                pos.getY() + 2.0D, pos.getZ() + 2.0D);
    }

    private void playSignal(String signal) {
        if (level == null || level.isClientSide) {
            return;
        }
        Tuple.Triplet<NoteBuilder.Instrument, NoteBuilder.Note, NoteBuilder.Octave>[] notes =
                NoteBuilder.translate(signal);
        for (Tuple.Triplet<NoteBuilder.Instrument, NoteBuilder.Note, NoteBuilder.Octave> note : notes) {
            int noteId = note.getY().ordinal() + note.getZ().ordinal() * 12;
            float pitch = (float) Math.pow(2.0D, (noteId - 12) / 12.0D);
            level.playSound(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, soundFor(note.getX()), SoundSource.BLOCKS, 3.0F, pitch);
        }
    }

    private static SoundEvent soundFor(NoteBuilder.Instrument instrument) {
        return switch (instrument) {
            case BASSDRUM -> SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            case SNARE -> SoundEvents.NOTE_BLOCK_SNARE.value();
            case CLICKS -> SoundEvents.NOTE_BLOCK_HAT.value();
            case BASSGUITAR -> SoundEvents.NOTE_BLOCK_BASS.value();
            default -> SoundEvents.NOTE_BLOCK_HARP.value();
        };
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static String cleanChannel(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 10 ? value.substring(0, 10) : value;
    }
}
