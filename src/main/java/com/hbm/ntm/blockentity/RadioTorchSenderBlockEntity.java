package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RTTYDeviceState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioTorchSenderBlockEntity extends RadioTorchDeviceBlockEntity {
    public RadioTorchSenderBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_SENDER.get(), pos, state);
    }

    public RadioTorchSenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchSenderBlockEntity torch) {
        int input = torch.attachedRedstoneInput(level);
        RTTYDeviceState.BroadcastDecision decision = torch.radio.evaluateBroadcastInput(input);
        if (decision.shouldBroadcast()) {
            RTTYSystem.broadcast(level, decision.channel(), decision.signal());
        }
        if (decision.stateChanged() || decision.shouldBroadcast()) {
            torch.setChangedAndSync(false);
        }
        torch.networkPackLegacyRadioTorch();
    }
}
