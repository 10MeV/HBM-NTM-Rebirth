package com.hbm.ntm.blockentity;

import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MultiblockDummyBlockEntity extends BlockEntity {
    private static final String TAG_CORE_POS = "CorePos";
    private static final String TAG_LEGACY_TARGET_X = "tx";
    private static final String TAG_LEGACY_TARGET_Y = "ty";
    private static final String TAG_LEGACY_TARGET_Z = "tz";

    @Nullable
    private BlockPos corePos;

    public MultiblockDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_DUMMY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultiblockDummyBlockEntity blockEntity) {
        if (blockEntity.corePos == null || !(level.getBlockState(blockEntity.corePos).getBlock() instanceof MultiblockCoreBlock)) {
            level.removeBlock(pos, false);
        }
    }

    @Nullable
    public BlockPos getCorePos() {
        return corePos;
    }

    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos.immutable();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public InteractionResult forwardUse(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        if (level == null || corePos == null || corePos.equals(worldPosition)) {
            return InteractionResult.PASS;
        }
        BlockState coreState = level.getBlockState(corePos);
        if (!(coreState.getBlock() instanceof MultiblockCoreBlock)) {
            return InteractionResult.PASS;
        }
        return coreState.use(level, player, hand, hit.withPosition(corePos));
    }

    public void destroyCore() {
        if (level != null && corePos != null && !corePos.equals(worldPosition)
                && level.getBlockState(corePos).getBlock() instanceof MultiblockCoreBlock) {
            level.destroyBlock(corePos, true);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (corePos != null) {
            tag.put(TAG_CORE_POS, NbtUtils.writeBlockPos(corePos));
            tag.putInt(TAG_LEGACY_TARGET_X, corePos.getX());
            tag.putInt(TAG_LEGACY_TARGET_Y, corePos.getY());
            tag.putInt(TAG_LEGACY_TARGET_Z, corePos.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_CORE_POS)) {
            corePos = NbtUtils.readBlockPos(tag.getCompound(TAG_CORE_POS));
        } else if (tag.contains(TAG_LEGACY_TARGET_X) && tag.contains(TAG_LEGACY_TARGET_Y) && tag.contains(TAG_LEGACY_TARGET_Z)) {
            corePos = new BlockPos(tag.getInt(TAG_LEGACY_TARGET_X), tag.getInt(TAG_LEGACY_TARGET_Y), tag.getInt(TAG_LEGACY_TARGET_Z));
        } else {
            corePos = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
