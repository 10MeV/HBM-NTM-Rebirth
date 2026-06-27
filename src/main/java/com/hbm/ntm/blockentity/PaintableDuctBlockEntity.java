package com.hbm.ntm.blockentity;

import java.util.ArrayList;
import java.util.List;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public interface PaintableDuctBlockEntity {
    String TAG_SETTINGS_PAINT_BLOCK = "paintblock";
    String TAG_SETTINGS_PAINT_META = "paintmeta";
    String TAG_SETTINGS_PAINT_BLOCK_NAME = "paint_block";

    @Nullable
    BlockState getPaintedState();

    int getPaintedMeta();

    void setPaintedState(@Nullable BlockState state, int legacyMeta);

    default boolean hasPaintedState() {
        return getPaintedState() != null;
    }

    default CompoundTag addPaintSettings(CompoundTag tag) {
        BlockState painted = getPaintedState();
        if (painted != null) {
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(painted.getBlock());
            if (key != null) {
                tag.putString(TAG_SETTINGS_PAINT_BLOCK_NAME, key.toString());
            }
            int legacyId = Block.getId(painted);
            if (legacyId != 0) {
                tag.putInt(TAG_SETTINGS_PAINT_BLOCK, legacyId);
            }
            tag.putInt(TAG_SETTINGS_PAINT_META, getPaintedMeta() & 15);
        }
        return tag;
    }

    default boolean pastePaintSettings(CompoundTag tag) {
        if (tag == null || (!tag.contains(TAG_SETTINGS_PAINT_BLOCK_NAME)
                && !tag.contains(TAG_SETTINGS_PAINT_BLOCK))) {
            return false;
        }
        BlockState state = null;
        if (tag.contains(TAG_SETTINGS_PAINT_BLOCK_NAME)) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_SETTINGS_PAINT_BLOCK_NAME));
            Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && block != Blocks.AIR) {
                state = block.defaultBlockState();
            }
        }
        if (state == null && tag.contains(TAG_SETTINGS_PAINT_BLOCK)) {
            BlockState legacyState = Block.stateById(tag.getInt(TAG_SETTINGS_PAINT_BLOCK));
            if (!legacyState.isAir()) {
                state = legacyState;
            }
        }
        if (state != null) {
            state = stateFromLegacyMeta(state.getBlock(), tag.getInt(TAG_SETTINGS_PAINT_META));
        }
        setPaintedState(state, tag.getInt(TAG_SETTINGS_PAINT_META) & 15);
        return state != null;
    }

    default List<Component> paintSettingsDisplayInfo() {
        BlockState painted = getPaintedState();
        if (painted == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>(1);
        lines.add(painted.getBlock().getName());
        return lines;
    }

    static BlockState stateFromLegacyMeta(Block block, int legacyMeta) {
        if (block.asItem() instanceof FluidDuctVariantBlockItem duct) {
            return duct.stateForLegacyMetadata(legacyMeta);
        }
        if (block.asItem() instanceof LegacyStateBlockItem stateItem) {
            return stateItem.stateForVariant(legacyMeta);
        }
        return block.defaultBlockState();
    }
}
