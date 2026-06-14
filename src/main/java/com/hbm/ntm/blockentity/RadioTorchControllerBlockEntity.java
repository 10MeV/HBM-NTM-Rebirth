package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RTTYControllerState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RadioTorchControllerBlockEntity extends RadioTorchBlockEntity {
    private final RTTYControllerState radio = new RTTYControllerState();

    public RadioTorchControllerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_CONTROLLER.get(), pos, state);
    }

    public RadioTorchControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYControllerState controllerState() {
        return radio;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchControllerBlockEntity torch) {
        if (!torch.radio.channel().isEmpty()) {
            BlockEntity attached = level.getBlockEntity(torch.attachedPos());
            if (attached instanceof RORInteractive interactive && ROR.hasFunctionInfo(interactive)) {
                RTTYSystem.RTTYChannel channel = RTTYSystem.listen(level, torch.radio.channel());
                RTTYControllerState.ControllerRunResult result =
                        torch.radio.runFromChannel(interactive, channel, level.getGameTime());
                if (result.selfDestruct()) {
                    torch.selfDestruct();
                    return;
                }
                if (result.ran() || result.exceptionMessage() != null) {
                    torch.setChangedAndSync(false);
                }
            }
        }
        torch.networkPackLegacyRadioTorch();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, radio.channel().isEmpty()
                ? List.of()
                : List.of(LegacyLookOverlayLines.freq(radio.channel())));
    }

    @Override
    public boolean applyRadioConfiguration(CompoundTag tag) {
        return finishRadioConfiguration(radio.applyControl(tag), false);
    }

    @Override
    public List<Component> describeRadioConfiguration() {
        return List.of(Component.literal("channel=" + radio.channel()
                + " polling=" + radio.polling()
                + " previous=" + radio.previous()));
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
