package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RedWireCoatedCt;
import com.hbm.ntm.block.RedWireCoatedBlock;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public class RedCableBlockEntity extends HbmEnergyNodeBlockEntity {
    public static final ModelProperty<RedWireCoatedCt.Data> RED_WIRE_COATED_CT_PROPERTY = new ModelProperty<>();

    public RedCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE.get(), pos, state);
    }

    public RedCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * {@code TileEntityCableBaseNT} uses IEnergyConductorMK2's default
     * PowerNode: its six legacy endpoints are declared independently from the
     * currently visible/compatible neighbours.  Nodespace performs the actual
     * reciprocal-node check when it joins a network.
     */
    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmEnergyNode.withStandardLegacyConnections(worldPosition);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshRedWireCoatedModelData();
    }

    @Override
    public @NotNull ModelData getModelData() {
        if (!(getBlockState().getBlock() instanceof RedWireCoatedBlock)) {
            return ModelData.EMPTY;
        }
        return ModelData.builder()
                .with(RED_WIRE_COATED_CT_PROPERTY, RedWireCoatedCt.compute(level, worldPosition, getBlockState()))
                .build();
    }

    public void refreshRedWireCoatedModelData() {
        if (level != null && level.isClientSide && getBlockState().getBlock() instanceof RedWireCoatedBlock) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }
}
