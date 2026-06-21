package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class FluidDuctPaintableBlockEntity extends FluidPipeBlockEntity implements PaintableDuctBlockEntity {
    private static final String TAG_PAINT_BLOCK = "block";
    private static final String TAG_PAINT_META = "meta";
    private static final String TAG_PAINT_BLOCK_NAME = "paint_block";

    @Nullable
    private BlockState paintedState;
    private int paintedMeta;

    public FluidDuctPaintableBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_DUCT_PAINTABLE.get(), pos, state);
    }

    protected FluidDuctPaintableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Nullable
    @Override
    public BlockState getPaintedState() {
        return paintedState;
    }

    @Override
    public int getPaintedMeta() {
        return paintedMeta;
    }

    @Override
    public void setPaintedState(@Nullable BlockState state, int legacyMeta) {
        if (state != null && state.isAir()) {
            state = null;
        }
        paintedState = state;
        paintedMeta = legacyMeta & 15;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getFluidSettings() {
        return addPaintSettings(super.getFluidSettings());
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        boolean fluidChanged = super.pasteFluidSettings(tag, index, player, recursive);
        boolean paintChanged = pastePaintSettings(tag);
        return fluidChanged || paintChanged;
    }

    @Override
    public List<Component> fluidSettingsDisplayInfo() {
        List<Component> lines = new ArrayList<>(super.fluidSettingsDisplayInfo());
        lines.addAll(paintSettingsDisplayInfo());
        return lines;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        savePaint(tag);
    }

    protected void savePaint(CompoundTag tag) {
        if (paintedState == null) {
            tag.remove(TAG_PAINT_BLOCK);
            tag.remove(TAG_PAINT_BLOCK_NAME);
        } else {
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(paintedState.getBlock());
            if (key != null) {
                tag.putString(TAG_PAINT_BLOCK_NAME, key.toString());
            }
            int legacyId = Block.getId(paintedState);
            if (legacyId != 0) {
                tag.putInt(TAG_PAINT_BLOCK, legacyId);
            }
        }
        tag.putInt(TAG_PAINT_META, paintedMeta & 15);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadPaint(tag);
    }

    protected void loadPaint(CompoundTag tag) {
        BlockState state = null;
        if (tag.contains(TAG_PAINT_BLOCK_NAME)) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_PAINT_BLOCK_NAME));
            Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && block != Blocks.AIR) {
                state = block.defaultBlockState();
            }
        }
        if (state == null && tag.contains(TAG_PAINT_BLOCK)) {
            BlockState legacyState = Block.stateById(tag.getInt(TAG_PAINT_BLOCK));
            if (!legacyState.isAir()) {
                state = legacyState;
            }
        }
        paintedState = state;
        paintedMeta = tag.getInt(TAG_PAINT_META) & 15;
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
