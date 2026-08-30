package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ICFAssembledBlock;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ICFAssembledBlockEntity extends BlockEntity implements HbmEnergyConnector {
    private ResourceLocation originalBlockId;
    private BlockState originalState;
    private BlockPos corePos;
    private boolean port;
    /**
     * Capability caches belong to this port position, not to the controller it
     * currently reaches. This keeps a cached external optional from surviving
     * a port replacement or chunk unload.
     */
    private final java.util.Map<PortCapabilityKey, LazyOptional<?>> forwardedCapabilities = new java.util.HashMap<>();

    public ICFAssembledBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICF_BLOCK.get(), pos, state);
    }

    /**
     * Mirrors BlockICF.TileEntityBlockICF#updateEntity: an assembled facade
     * only restores itself after the saved controller chunk is available, so
     * an unloaded controller cannot spuriously dismantle the laser assembly.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
            ICFAssembledBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L || blockEntity.corePos == null
                || !level.hasChunk(blockEntity.corePos.getX() >> 4, blockEntity.corePos.getZ() >> 4)) {
            return;
        }
        BlockEntity core = level.getBlockEntity(blockEntity.corePos);
        if (!(core instanceof ICFControllerBlockEntity controller) || !controller.isAssembled()) {
            blockEntity.restoreOriginalBlock();
        }
    }

    public void setOriginal(BlockState state, BlockPos corePos, boolean port) {
        invalidateForwardedCapabilities();
        this.originalState = state;
        this.originalBlockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        this.corePos = corePos.immutable();
        this.port = port;
        setChanged();
    }

    public void invalidateController() {
        if (level != null && corePos != null && level.hasChunk(corePos.getX() >> 4, corePos.getZ() >> 4)
                && level.getBlockEntity(corePos) instanceof ICFControllerBlockEntity controller) {
            controller.setAssembled(false);
        }
    }

    public boolean isPort() {
        return port;
    }

    public boolean isLinkedTo(BlockPos pos) {
        return corePos != null && corePos.equals(pos);
    }

    public void suppressRestore() {
        invalidateForwardedCapabilities();
        originalBlockId = null;
        originalState = null;
        corePos = null;
        port = false;
        setChanged();
    }

    public void restoreOriginalBlock() {
        if (level == null || level.isClientSide || originalBlockId == null) {
            return;
        }
        Block original = ForgeRegistries.BLOCKS.getValue(originalBlockId);
        if (original == null || original == ModBlocks.ICF_BLOCK.get()) {
            return;
        }
        BlockState restored = originalState != null && originalState.is(original)
                ? originalState
                : original.defaultBlockState();
        suppressRestore();
        level.setBlock(worldPosition, restored, Block.UPDATE_ALL);
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return side != null && getPortCore() != null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (originalBlockId != null) {
            tag.putString("block", originalBlockId.toString());
        }
        if (originalState != null) {
            tag.put("state", NbtUtils.writeBlockState(originalState));
        }
        if (corePos != null) {
            tag.putInt("cX", corePos.getX());
            tag.putInt("cY", corePos.getY());
            tag.putInt("cZ", corePos.getZ());
        }
        int legacyMeta = legacyLaserMeta(originalState != null ? originalState.getBlock() : null);
        if (legacyMeta >= 0) {
            tag.putInt("meta", legacyMeta);
        }
        tag.putBoolean("port", port);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        invalidateForwardedCapabilities();
        originalBlockId = null;
        originalState = null;
        corePos = null;
        if (tag.contains("block", Tag.TAG_STRING)) {
            originalBlockId = ResourceLocation.tryParse(tag.getString("block"));
        } else if (tag.contains("block", Tag.TAG_INT) && tag.getInt("block") != 0) {
            originalState = legacyLaserComponentState(tag.getInt("meta"));
            originalBlockId = ForgeRegistries.BLOCKS.getKey(originalState.getBlock());
        }
        if (tag.contains("state", Tag.TAG_COMPOUND)) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
            originalBlockId = ForgeRegistries.BLOCKS.getKey(originalState.getBlock());
        }
        if (tag.contains("cX")) {
            corePos = new BlockPos(tag.getInt("cX"), tag.getInt("cY"), tag.getInt("cZ"));
        }
        port = tag.contains("port") ? tag.getBoolean("port") : getBlockState().hasProperty(ICFAssembledBlock.PORT)
                && getBlockState().getValue(ICFAssembledBlock.PORT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY && side != null && getPortCore() != null) {
            return forwardedEnergyCapability(side).cast();
        }
        return super.getCapability(capability, side);
    }

    private LazyOptional<IEnergyStorage> forwardedEnergyCapability(Direction side) {
        PortCapabilityKey key = new PortCapabilityKey(ForgeCapabilities.ENERGY, side);
        LazyOptional<?> cached = forwardedCapabilities.get(key);
        if (cached != null) {
            if (cached.isPresent()) {
                return cached.cast();
            }
            forwardedCapabilities.remove(key, cached);
        }

        ICFControllerBlockEntity controller = getPortCore();
        LazyOptional<IEnergyStorage> controllerCapability = controller == null
                ? LazyOptional.empty()
                : controller.getCapability(ForgeCapabilities.ENERGY, side);
        if (!controllerCapability.isPresent()) {
            return LazyOptional.empty();
        }
        LazyOptional<IEnergyStorage> forwarded = LazyOptional.of(() -> new PortEnergyStorage(side));
        forwardedCapabilities.put(key, forwarded);
        controllerCapability.addListener(ignored -> invalidateForwardedCapability(key, forwarded));
        return forwarded;
    }

    private void invalidateForwardedCapability(PortCapabilityKey key, LazyOptional<?> capability) {
        capability.invalidate();
        forwardedCapabilities.remove(key, capability);
    }

    private void invalidateForwardedCapabilities() {
        forwardedCapabilities.values().forEach(LazyOptional::invalidate);
        forwardedCapabilities.clear();
    }

    @Override
    public void invalidateCaps() {
        invalidateForwardedCapabilities();
        super.invalidateCaps();
    }

    @Override
    public void setRemoved() {
        invalidateForwardedCapabilities();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        invalidateForwardedCapabilities();
        super.onChunkUnloaded();
    }

    @Nullable
    private ICFControllerBlockEntity core() {
        if (level == null || corePos == null || !level.hasChunk(corePos.getX() >> 4, corePos.getZ() >> 4)) {
            return null;
        }
        return level.getBlockEntity(corePos) instanceof ICFControllerBlockEntity controller ? controller : null;
    }

    @Nullable
    private ICFControllerBlockEntity getPortCore() {
        ICFControllerBlockEntity controller = core();
        return isActivePort() && controller != null && controller.isAssembled() ? controller : null;
    }

    private boolean isActivePort() {
        BlockState state = getBlockState();
        return port && hasOriginalBlock() && state.is(ModBlocks.ICF_BLOCK.get())
                && state.hasProperty(ICFAssembledBlock.PORT) && state.getValue(ICFAssembledBlock.PORT);
    }

    private boolean hasOriginalBlock() {
        return originalBlockId != null || originalState != null;
    }

    private static BlockState legacyLaserComponentState(int meta) {
        return switch (meta) {
            case 1 -> ModBlocks.ICF_LASER_PORT.get().defaultBlockState();
            case 2 -> ModBlocks.ICF_LASER_CELL.get().defaultBlockState();
            case 3 -> ModBlocks.ICF_LASER_EMITTER.get().defaultBlockState();
            case 4 -> ModBlocks.ICF_LASER_CAPACITOR.get().defaultBlockState();
            case 5 -> ModBlocks.ICF_LASER_TURBO.get().defaultBlockState();
            default -> ModBlocks.ICF_LASER_CASING.get().defaultBlockState();
        };
    }

    private static int legacyLaserMeta(@Nullable Block block) {
        if (block == ModBlocks.ICF_LASER_CASING.get()) return 0;
        if (block == ModBlocks.ICF_LASER_PORT.get()) return 1;
        if (block == ModBlocks.ICF_LASER_CELL.get()) return 2;
        if (block == ModBlocks.ICF_LASER_EMITTER.get()) return 3;
        if (block == ModBlocks.ICF_LASER_CAPACITOR.get()) return 4;
        if (block == ModBlocks.ICF_LASER_TURBO.get()) return 5;
        return -1;
    }

    private final class PortEnergyStorage implements IEnergyStorage {
        private final Direction side;

        private PortEnergyStorage(Direction side) {
            this.side = side;
        }

        @Nullable
        private IEnergyStorage current() {
            ICFControllerBlockEntity controller = getPortCore();
            return controller == null
                    ? null
                    : controller.getCapability(ForgeCapabilities.ENERGY, side).resolve().orElse(null);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            IEnergyStorage storage = current();
            return storage == null ? 0 : storage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            IEnergyStorage storage = current();
            return storage == null ? 0 : storage.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            IEnergyStorage storage = current();
            return storage == null ? 0 : storage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            IEnergyStorage storage = current();
            return storage == null ? 0 : storage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            IEnergyStorage storage = current();
            return storage != null && storage.canExtract();
        }

        @Override
        public boolean canReceive() {
            IEnergyStorage storage = current();
            return storage != null && storage.canReceive();
        }
    }

    private record PortCapabilityKey(Capability<?> capability, Direction side) {
    }
}
