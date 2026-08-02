package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PlatemetalBlock;
import com.hbm.ntm.block.RedWireCoatedCt;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

/** Renderer-only ModelData bridge for the shared legacy eight-neighbour CT implementation. */
public final class PlatemetalBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    public static final ModelProperty<RedWireCoatedCt.Data> CT_PROPERTY = new ModelProperty<>();

    public PlatemetalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLATEMETAL.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshConnectedTextureModelData();
    }

    @Override
    public @NotNull ModelData getModelData() {
        if (!(getBlockState().getBlock() instanceof PlatemetalBlock)) {
            return ModelData.EMPTY;
        }
        return ModelData.builder().with(CT_PROPERTY,
                RedWireCoatedCt.compute(level, worldPosition, getBlockState())).build();
    }

    public void refreshConnectedTextureModelData() {
        if (level != null && level.isClientSide && getBlockState().getBlock() instanceof PlatemetalBlock) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }
}
