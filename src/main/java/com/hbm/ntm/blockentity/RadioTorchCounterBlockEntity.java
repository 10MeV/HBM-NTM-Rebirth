package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RTTYCounterState;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchCounterBlockEntity extends RadioTorchBlockEntity {
    private final RTTYCounterState radio = new RTTYCounterState();

    public RadioTorchCounterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_COUNTER.get(), pos, state);
    }

    public RadioTorchCounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYCounterState counterState() {
        return radio;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchCounterBlockEntity torch) {
        BlockEntity attached = level.getBlockEntity(torch.attachedPos());
        if (attached == null) {
            return;
        }
        attached.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            int count = countItems(handler);
            int[] counts = new int[RTTYCounterState.SLOT_COUNT];
            for (int i = 0; i < counts.length; i++) {
                counts[i] = count;
            }
            if (torch.radio.broadcastCounts(level, counts) > 0) {
                torch.setChangedAndSync(false);
            }
        });
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty()) {
                lines.add(LegacyLookOverlayLines.freq(i + 1, radio.channel(i)));
                lines.add(LegacyLookOverlayLines.signal(i + 1, radio.lastCount(i)));
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
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty() || radio.lastCount(i) != 0) {
                lines.add(Component.literal("slot " + i
                        + ": c" + i + "=" + radio.channel(i)
                        + " last=" + radio.lastCount(i)));
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

    private static int countItems(IItemHandler handler) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
