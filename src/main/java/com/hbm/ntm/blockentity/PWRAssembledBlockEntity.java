package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PWRAssembledBlockEntity extends BlockEntity implements RORValueProvider, RORInteractive {
    private ResourceLocation originalBlockId;
    private BlockState originalState;
    private BlockPos corePos;
    private boolean port;

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
        this.originalState = state;
        this.originalBlockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        this.corePos = corePos.immutable();
        this.port = port;
        setChanged();
    }

    public boolean isPort() {
        return port;
    }

    public void invalidateController() {
        if (level != null && corePos != null
                && level.getBlockEntity(corePos) instanceof PWRControllerBlockEntity controller) {
            controller.setAssembled(false);
        }
    }

    public void suppressRestore() {
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
        if (tag.contains("block")) {
            originalBlockId = ResourceLocation.tryParse(tag.getString("block"));
        }
        if (tag.contains("state")) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
            originalBlockId = ForgeRegistries.BLOCKS.getKey(originalState.getBlock());
        }
        if (tag.contains("cX")) {
            corePos = new BlockPos(tag.getInt("cX"), tag.getInt("cY"), tag.getInt("cZ"));
        }
        port = tag.getBoolean("port");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        PWRControllerBlockEntity core = getLoadedCore();
        if (port && isPortCapability(capability) && core != null) {
            return core.getCapability(capability, side);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String[] getFunctionInfo() {
        return PWRControllerBlockEntity.ROR;
    }

    @Override
    public String provideRORValue(String name) {
        if (!port || !hasOriginalBlock()) {
            return "";
        }
        PWRControllerBlockEntity core = getLoadedCore();
        return core != null ? core.provideRORValue(name) : null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if (!port || !hasOriginalBlock()) {
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

    private boolean hasOriginalBlock() {
        return originalBlockId != null || originalState != null;
    }

    private static boolean isPortCapability(Capability<?> capability) {
        return capability == ForgeCapabilities.ITEM_HANDLER || capability == ForgeCapabilities.FLUID_HANDLER;
    }
}
