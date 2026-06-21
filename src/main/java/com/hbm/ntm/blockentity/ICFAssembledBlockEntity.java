package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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

public class ICFAssembledBlockEntity extends BlockEntity implements HbmEnergyConnector {
    private ResourceLocation originalBlockId;
    private BlockState originalState;
    private BlockPos corePos;
    private boolean port;

    public ICFAssembledBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICF_BLOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFAssembledBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L || blockEntity.corePos == null) {
            return;
        }
        if (!level.hasChunk(blockEntity.corePos.getX() >> 4, blockEntity.corePos.getZ() >> 4)) {
            return;
        }
        BlockEntity core = level.getBlockEntity(blockEntity.corePos);
        if (!(core instanceof ICFControllerBlockEntity controller) || !controller.isAssembled()) {
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

    public void invalidateController() {
        if (level != null && corePos != null && level.hasChunk(corePos.getX() >> 4, corePos.getZ() >> 4)
                && level.getBlockEntity(corePos) instanceof ICFControllerBlockEntity controller) {
            controller.setAssembled(false);
        }
    }

    public boolean isPort() {
        return port;
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
        return port && side != null && hasOriginalBlock() && core() != null;
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
        ICFControllerBlockEntity controller = core();
        if (port && side != null && hasOriginalBlock() && capability == ForgeCapabilities.ENERGY
                && controller != null) {
            return controller.getCapability(capability, side);
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    private ICFControllerBlockEntity core() {
        if (level == null || corePos == null || !level.hasChunk(corePos.getX() >> 4, corePos.getZ() >> 4)) {
            return null;
        }
        return level.getBlockEntity(corePos) instanceof ICFControllerBlockEntity controller ? controller : null;
    }

    private boolean hasOriginalBlock() {
        return originalBlockId != null || originalState != null;
    }
}
