package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RTTYDeviceState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioTorchReceiverBlockEntity extends RadioTorchDeviceBlockEntity {
    public RadioTorchReceiverBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_RECEIVER.get(), pos, state);
    }

    public RadioTorchReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchReceiverBlockEntity torch) {
        if (torch.radio.channel().isEmpty()) {
            return;
        }
        RTTYSystem.RTTYChannel channel = RTTYSystem.listen(level, torch.radio.channel());
        RTTYDeviceState.RedstoneReceiveResult result = torch.radio.receiveRedstoneSignal(channel, level.getGameTime());
        if (!result.received() || result.selfDestruct()) {
            return;
        }
        if (torch.radio.applyLastState(result.redstoneLevel())) {
            torch.setChangedAndSync(true);
        }
    }
}
