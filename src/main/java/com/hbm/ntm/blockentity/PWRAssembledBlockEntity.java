package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.PWRAssembledBlock;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PWRAssembledBlockEntity extends BlockEntity implements RORValueProvider, RORInteractive {
    private ResourceLocation originalBlockId;
    private BlockState originalState;
    private BlockPos corePos;
    private boolean port;
    /**
     * This position owns the optionals it exposes. A controller-owned optional
     * must never escape through an assembled port, because automation caches
     * capabilities by the port position rather than by the controller.
     */
    private final Map<PortCapabilityKey, LazyOptional<?>> forwardedCapabilities = new HashMap<>();

    public PWRAssembledBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PWR_BLOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PWRAssembledBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L || blockEntity.corePos == null) {
            return;
        }
        if (!level.hasChunk(blockEntity.corePos.getX() >> 4, blockEntity.corePos.getZ() >> 4)) {
            return;
        }
        BlockEntity core = level.getBlockEntity(blockEntity.corePos);
        if (!(core instanceof PWRControllerBlockEntity controller) || !controller.isAssembled()) {
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

    public boolean isPort() {
        return port;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            PWRAssembledBlock.refreshConnectedTextureNeighborhood(level, worldPosition);
        }
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(PwrConnectedTextureData.CONNECTION_MASK,
                PwrConnectedTextureData.connectionMask(level, worldPosition)).build();
    }

    @Nullable
    public BlockState getOriginalState() {
        return originalState;
    }

    public void invalidateController() {
        if (level != null && corePos != null
                && level.getBlockEntity(corePos) instanceof PWRControllerBlockEntity controller) {
            controller.setAssembled(false);
        }
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
        if (original == null || original == ModBlocks.PWR_BLOCK.get()) {
            return;
        }
        BlockState restored = originalState != null && originalState.is(original)
                ? originalState
                : original.defaultBlockState();
        suppressRestore();
        level.setBlock(worldPosition, restored, Block.UPDATE_ALL);
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
        } else if (tag.contains("block", Tag.TAG_INT) && tag.getInt("block") == 0) {
            originalBlockId = null;
        }
        if (tag.contains("state", Tag.TAG_COMPOUND)) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
            originalBlockId = ForgeRegistries.BLOCKS.getKey(originalState.getBlock());
        }
        if (tag.contains("cX")) {
            corePos = new BlockPos(tag.getInt("cX"), tag.getInt("cY"), tag.getInt("cZ"));
        }
        port = tag.contains("port") ? tag.getBoolean("port") : getBlockState().hasProperty(PWRAssembledBlock.PORT)
                && getBlockState().getValue(PWRAssembledBlock.PORT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && getPortCore() != null) {
            return forwardedItemCapability(side).cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER && getPortCore() != null) {
            return forwardedFluidCapability(side).cast();
        }
        return super.getCapability(capability, side);
    }

    private LazyOptional<IItemHandler> forwardedItemCapability(@Nullable Direction side) {
        PortCapabilityKey key = new PortCapabilityKey(ForgeCapabilities.ITEM_HANDLER, side);
        LazyOptional<?> cached = forwardedCapabilities.get(key);
        if (cached != null) {
            if (cached.isPresent()) {
                return cached.cast();
            }
            forwardedCapabilities.remove(key, cached);
        }

        PWRControllerBlockEntity core = getPortCore();
        LazyOptional<IItemHandler> controllerCapability = core == null
                ? LazyOptional.empty()
                : core.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        if (!controllerCapability.isPresent()) {
            return LazyOptional.empty();
        }
        LazyOptional<IItemHandler> forwarded = LazyOptional.of(() -> new PortItemHandler(side));
        forwardedCapabilities.put(key, forwarded);
        controllerCapability.addListener(ignored -> invalidateForwardedCapability(key, forwarded));
        return forwarded;
    }

    private LazyOptional<IFluidHandler> forwardedFluidCapability(@Nullable Direction side) {
        PortCapabilityKey key = new PortCapabilityKey(ForgeCapabilities.FLUID_HANDLER, side);
        LazyOptional<?> cached = forwardedCapabilities.get(key);
        if (cached != null) {
            if (cached.isPresent()) {
                return cached.cast();
            }
            forwardedCapabilities.remove(key, cached);
        }

        PWRControllerBlockEntity core = getPortCore();
        LazyOptional<IFluidHandler> controllerCapability = core == null
                ? LazyOptional.empty()
                : core.getCapability(ForgeCapabilities.FLUID_HANDLER, side);
        if (!controllerCapability.isPresent()) {
            return LazyOptional.empty();
        }
        LazyOptional<IFluidHandler> forwarded = LazyOptional.of(() -> new PortFluidHandler(side));
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

    @Override
    public String[] getFunctionInfo() {
        return PWRControllerBlockEntity.ROR;
    }

    @Override
    public String provideRORValue(String name) {
        if (!port) {
            return "";
        }
        PWRControllerBlockEntity core = getLoadedCore();
        return core != null ? core.provideRORValue(name) : null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if (!port) {
            return "";
        }
        PWRControllerBlockEntity core = getLoadedCore();
        return core != null ? core.runRORFunction(name, params) : null;
    }

    @Nullable
    private PWRControllerBlockEntity getLoadedCore() {
        if (level == null || corePos == null || !level.hasChunk(corePos.getX() >> 4, corePos.getZ() >> 4)) {
            return null;
        }
        return level.getBlockEntity(corePos) instanceof PWRControllerBlockEntity core ? core : null;
    }

    @Nullable
    private PWRControllerBlockEntity getPortCore() {
        PWRControllerBlockEntity core = getLoadedCore();
        return isActivePort() && core != null && core.isAssembled() ? core : null;
    }

    private boolean isActivePort() {
        BlockState state = getBlockState();
        return port && hasOriginalBlock() && state.is(ModBlocks.PWR_BLOCK.get())
                && state.hasProperty(PWRAssembledBlock.PORT) && state.getValue(PWRAssembledBlock.PORT);
    }

    private boolean hasOriginalBlock() {
        return originalBlockId != null || originalState != null;
    }

    private final class PortItemHandler implements IItemHandler {
        private final Direction side;

        private PortItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Nullable
        private IItemHandler current() {
            PWRControllerBlockEntity core = getPortCore();
            return core == null ? null : core.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve().orElse(null);
        }

        @Override
        public int getSlots() {
            IItemHandler handler = current();
            return handler == null ? 0 : handler.getSlots();
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
            IItemHandler handler = current();
            return handler == null ? net.minecraft.world.item.ItemStack.EMPTY : handler.getStackInSlot(slot);
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack insertItem(int slot,
                @NotNull net.minecraft.world.item.ItemStack stack, boolean simulate) {
            IItemHandler handler = current();
            return handler == null ? stack : handler.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler handler = current();
            return handler == null ? net.minecraft.world.item.ItemStack.EMPTY : handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler handler = current();
            return handler == null ? 0 : handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull net.minecraft.world.item.ItemStack stack) {
            IItemHandler handler = current();
            return handler != null && handler.isItemValid(slot, stack);
        }
    }

    private final class PortFluidHandler implements IFluidHandler {
        private final Direction side;

        private PortFluidHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Nullable
        private IFluidHandler current() {
            PWRControllerBlockEntity core = getPortCore();
            return core == null ? null : core.getCapability(ForgeCapabilities.FLUID_HANDLER, side).resolve().orElse(null);
        }

        @Override
        public int getTanks() {
            IFluidHandler handler = current();
            return handler == null ? 0 : handler.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            IFluidHandler handler = current();
            return handler == null ? FluidStack.EMPTY : handler.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            IFluidHandler handler = current();
            return handler == null ? 0 : handler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            IFluidHandler handler = current();
            return handler != null && handler.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            IFluidHandler handler = current();
            return handler == null ? 0 : handler.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            IFluidHandler handler = current();
            return handler == null ? FluidStack.EMPTY : handler.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            IFluidHandler handler = current();
            return handler == null ? FluidStack.EMPTY : handler.drain(maxDrain, action);
        }
    }

    private record PortCapabilityKey(Capability<?> capability, @Nullable Direction side) {
    }
}
