package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RTTYDeviceState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public abstract class RadioTorchDeviceBlockEntity extends RadioTorchBlockEntity implements RadioTorchRedstoneSource {
    protected final RTTYDeviceState radio = new RTTYDeviceState();

    protected RadioTorchDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYDeviceState radioState() {
        return radio;
    }

    @Override
    public int redstoneOutput() {
        return radio.lastState();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        if (!radio.channel().isEmpty()) {
            lines.add(LegacyLookOverlayLines.freq(radio.channel()));
        }
        lines.add(LegacyLookOverlayLines.signal(radio.lastState()));
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public boolean applyRadioConfiguration(CompoundTag tag) {
        return finishRadioConfiguration(radio.applyControl(tag), true);
    }

    @Override
    public List<Component> describeRadioConfiguration() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("channel=" + radio.channel()
                + " polling=" + radio.polling()
                + " customMap=" + radio.customMap()
                + " lastState=" + radio.lastState()
                + " lastUpdate=" + radio.lastUpdate()));
        for (int i = 0; i < 16; i++) {
            String mapped = radio.mapping(i);
            if (!mapped.isEmpty()) {
                lines.add(Component.literal("m" + i + "=" + mapped));
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
