package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.api.redstoneoverradio.RTTYReaderState;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchReaderBlockEntity extends RadioTorchBlockEntity {
    private final RTTYReaderState radio = new RTTYReaderState();

    public RadioTorchReaderBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_READER.get(), pos, state);
    }

    public RadioTorchReaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYReaderState readerState() {
        return radio;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchReaderBlockEntity torch) {
        BlockEntity attached = torch.attachedBlockEntity(level);
        if (attached instanceof RORValueProvider provider
                && ROR.hasValueInfo(provider)
                && torch.radio.broadcastChangedValues(level, provider) > 0) {
            torch.setChangedAndSync(false);
        }
        torch.networkPackLegacyRadioTorch();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        for (int i = 0; i < RTTYReaderState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty() && !radio.name(i).isEmpty()) {
                lines.add(LegacyLookOverlayLines.freq(i + 1, radio.channel(i)));
                lines.add(net.minecraft.network.chat.Component.literal(radio.name(i)));
            }
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public boolean applyRadioConfiguration(CompoundTag tag) {
        return finishRadioConfiguration(radio.applyControl(tag), false);
    }

    @Override
    public List<Component> describeRadioConfiguration() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("polling=" + radio.polling()));
        for (int i = 0; i < RTTYReaderState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty() || !radio.name(i).isEmpty() || !radio.previous(i).isEmpty()) {
                lines.add(Component.literal("slot " + i
                        + ": c" + i + "=" + radio.channel(i)
                        + " n" + i + "=" + radio.name(i)
                        + " prev=" + radio.previous(i)));
            }
        }
        return lines;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        radio.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        radio.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
