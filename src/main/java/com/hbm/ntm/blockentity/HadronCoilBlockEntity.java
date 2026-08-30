package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HadronCoilBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

/**
 * Client model-data carrier only. Legacy BlockHadronCoil had no TileEntity,
 * inventory, saved fields, tick, GUI or capability; Forge's block-model API
 * needs this empty modern carrier to supply its CTContext-equivalent mask.
 */
public final class HadronCoilBlockEntity extends BlockEntity {
    public HadronCoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HADRON_COIL.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) HadronCoilBlock.refreshConnectedTextureNeighborhood(level, worldPosition);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(HadronCoilConnectedTextureData.CONNECTION_MASK,
                HadronCoilConnectedTextureData.connectionMask(level, worldPosition)).build();
    }
}
