package com.hbm.ntm.blockentity;

import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class MultiblockDummyBlockEntity extends BlockEntity {
    private static final String TAG_CORE_POS = "CorePos";
    private static final String TAG_LEGACY_TARGET_X = "tx";
    private static final String TAG_LEGACY_TARGET_Y = "ty";
    private static final String TAG_LEGACY_TARGET_Z = "tz";
    private static final String TAG_PROXY = "Proxy";

    @Nullable
    private BlockPos corePos;
    private boolean proxy;

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

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (proxy && level != null && corePos != null && !corePos.equals(worldPosition)) {
            BlockEntity coreEntity = level.getBlockEntity(corePos);
            if (coreEntity != null && !coreEntity.isRemoved()) {
                return coreEntity.getCapability(capability, side);
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_PROXY, proxy);
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
        proxy = tag.getBoolean(TAG_PROXY);
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
