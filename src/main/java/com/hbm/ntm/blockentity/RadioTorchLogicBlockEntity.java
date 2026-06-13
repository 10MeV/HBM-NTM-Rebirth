package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RTTYLogicState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchLogicBlockEntity extends RadioTorchBlockEntity implements RadioTorchRedstoneSource {
    private final RTTYLogicState radio = new RTTYLogicState();

    public RadioTorchLogicBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_LOGIC.get(), pos, state);
    }

    public RadioTorchLogicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYLogicState logicState() {
        return radio;
    }

    @Override
    public int redstoneOutput() {
        return radio.lastState();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchLogicBlockEntity torch) {
        if (!torch.radio.channel().isEmpty()) {
            RTTYSystem.RTTYChannel channel = RTTYSystem.listen(level, torch.radio.channel());
            RTTYLogicState.LogicReceiveResult result = torch.radio.receiveLogicSignal(channel, level.getGameTime());
            if (result.received() && torch.radio.applyLastState(result.redstoneLevel())) {
                torch.setChangedAndSync(true);
            }
        }
        torch.networkPackLegacyRadioTorch();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        if (!radio.channel().isEmpty()) {
            lines.add(LegacyLookOverlayLines.freq(radio.channel()));
        }
        lines.add(LegacyLookOverlayLines.signal(0, radio.lastState()));
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
                + " descending=" + radio.descending()
                + " lastState=" + radio.lastState()
                + " lastUpdate=" + radio.lastUpdate()));
        for (int i = 0; i < 16; i++) {
            String mapped = radio.mapping(i);
            if (!mapped.isEmpty() || radio.condition(i) != 0) {
                lines.add(Component.literal("rule " + i
                        + ": c" + i + "=" + radio.condition(i)
                        + " m" + i + "=" + mapped));
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
