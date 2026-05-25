package com.hbm.ntm.blockentity;

import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.LegacyProxyMode;
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
    private static final String TAG_PROXY_INVENTORY = "ProxyInventory";
    private static final String TAG_PROXY_POWER = "ProxyPower";
    private static final String TAG_PROXY_CONDUCTOR = "ProxyConductor";
    private static final String TAG_PROXY_FLUID = "ProxyFluid";
    private static final String TAG_PROXY_HEAT = "ProxyHeat";
    private static final String TAG_PROXY_MOLTEN_METAL = "ProxyMoltenMetal";
    private static final String TAG_PROXY_ALL = "ProxyAll";

    @Nullable
    private BlockPos corePos;
    private LegacyProxyMode proxyMode = LegacyProxyMode.none();
    private boolean dropCoreOnRemoval;

    public MultiblockDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_DUMMY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultiblockDummyBlockEntity blockEntity) {
        if (blockEntity.corePos == null) {
            level.removeBlock(pos, false);
            return;
        }
        if (!level.hasChunkAt(blockEntity.corePos)) {
            return;
        }
        if (!(level.getBlockState(blockEntity.corePos).getBlock() instanceof MultiblockCoreBlock)) {
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
        return proxyMode.isProxy();
    }

    public void setProxy(boolean proxy) {
        setProxyMode(proxy ? LegacyProxyMode.all() : LegacyProxyMode.none());
    }

    public LegacyProxyMode getProxyMode() {
        return proxyMode;
    }

    public void setProxyMode(LegacyProxyMode proxyMode) {
        this.proxyMode = proxyMode == null ? LegacyProxyMode.none() : proxyMode;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public InteractionResult forwardUse(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        if (level == null || corePos == null || corePos.equals(worldPosition) || !level.hasChunkAt(corePos)) {
            return InteractionResult.PASS;
        }
        BlockState coreState = level.getBlockState(corePos);
        if (!(coreState.getBlock() instanceof MultiblockCoreBlock)) {
            return InteractionResult.PASS;
        }
        return coreState.use(level, player, hand, hit.withPosition(corePos));
    }

    public void setDropCoreOnRemoval(boolean dropCoreOnRemoval) {
        this.dropCoreOnRemoval = dropCoreOnRemoval;
    }

    public void destroyCore() {
        destroyCore(dropCoreOnRemoval);
    }

    public void destroyCore(boolean drop) {
        dropCoreOnRemoval = false;
        if (level != null && corePos != null && !corePos.equals(worldPosition)
                && level.hasChunkAt(corePos)
                && level.getBlockState(corePos).getBlock() instanceof MultiblockCoreBlock) {
            level.destroyBlock(corePos, drop);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (proxyMode.allows(capability) && level != null && corePos != null && !corePos.equals(worldPosition)
                && level.hasChunkAt(corePos)) {
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
        tag.putBoolean(TAG_PROXY, proxyMode.isProxy());
        tag.putBoolean(TAG_PROXY_INVENTORY, proxyMode.inventory());
        tag.putBoolean(TAG_PROXY_POWER, proxyMode.power());
        tag.putBoolean(TAG_PROXY_CONDUCTOR, proxyMode.conductor());
        tag.putBoolean(TAG_PROXY_FLUID, proxyMode.fluid());
        tag.putBoolean(TAG_PROXY_HEAT, proxyMode.heat());
        tag.putBoolean(TAG_PROXY_MOLTEN_METAL, proxyMode.moltenMetal());
        tag.putBoolean(TAG_PROXY_ALL, proxyMode.allCapabilities());
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
        proxyMode = readProxyMode(tag);
        if (tag.contains(TAG_CORE_POS)) {
            corePos = NbtUtils.readBlockPos(tag.getCompound(TAG_CORE_POS));
        } else if (tag.contains(TAG_LEGACY_TARGET_X) && tag.contains(TAG_LEGACY_TARGET_Y) && tag.contains(TAG_LEGACY_TARGET_Z)) {
            corePos = new BlockPos(tag.getInt(TAG_LEGACY_TARGET_X), tag.getInt(TAG_LEGACY_TARGET_Y), tag.getInt(TAG_LEGACY_TARGET_Z));
        } else {
            corePos = null;
        }
    }

    private static LegacyProxyMode readProxyMode(CompoundTag tag) {
        if (!tag.getBoolean(TAG_PROXY)) {
            return LegacyProxyMode.none();
        }
        if (tag.getBoolean(TAG_PROXY_ALL) || !hasProxyModeTags(tag)) {
            return LegacyProxyMode.all();
        }
        return LegacyProxyMode.passive()
                .withInventory(tag.getBoolean(TAG_PROXY_INVENTORY))
                .withPower(tag.getBoolean(TAG_PROXY_POWER))
                .withConductor(tag.getBoolean(TAG_PROXY_CONDUCTOR))
                .withFluid(tag.getBoolean(TAG_PROXY_FLUID))
                .withHeat(tag.getBoolean(TAG_PROXY_HEAT))
                .withMoltenMetal(tag.getBoolean(TAG_PROXY_MOLTEN_METAL));
    }

    private static boolean hasProxyModeTags(CompoundTag tag) {
        return tag.contains(TAG_PROXY_INVENTORY)
                || tag.contains(TAG_PROXY_POWER)
                || tag.contains(TAG_PROXY_CONDUCTOR)
                || tag.contains(TAG_PROXY_FLUID)
                || tag.contains(TAG_PROXY_HEAT)
                || tag.contains(TAG_PROXY_MOLTEN_METAL)
                || tag.contains(TAG_PROXY_ALL);
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
