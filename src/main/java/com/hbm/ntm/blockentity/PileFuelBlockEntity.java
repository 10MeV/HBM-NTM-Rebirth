package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.neutron.PileFuelState;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInteractionPlanner;
import com.hbm.ntm.neutron.PileNeutronColumn;
import com.hbm.ntm.neutron.PileNeutronHandler;
import com.hbm.ntm.neutron.PileNeutronReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileFuelBlockEntity extends BlockEntity implements PileNeutronColumn, PileNeutronReceiver {
    private final PileFuelState fuelState = new PileFuelState();

    public PileFuelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PILE_FUEL.get(), pos, state);
    }

    public PileFuelState fuelState() {
        return fuelState;
    }

    @Override
    public void receiveNeutrons(int neutrons) {
        fuelState.receiveNeutrons(neutrons);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PileFuelBlockEntity blockEntity) {
        int meta = PileGraphiteDrilledBaseBlock.legacyMeta(state);
        PileGraphiteBlockEntityPlanner.FuelBlockEntityTickPlan plan =
                PileGraphiteBlockEntityPlanner.planFuelTick(pos, meta, blockEntity.fuelState, level.random);
        for (PileGraphiteBlockEntityPlanner.RayCastRequest ray : plan.rayCasts()) {
            PileNeutronHandler.castRandomRay(blockEntity, ray.flux());
        }
        if (plan.lifecycle() != null) {
            for (PileGraphiteInteractionPlanner.BlockMutation mutation : plan.lifecycle().blockMutations()) {
                if (!"gas_radon_dense".equals(mutation.legacyBlockId())) {
                    PileGraphiteDrilledBaseBlock.setLegacyBlock(
                            level,
                            mutation.pos(),
                            mutation.legacyBlockId(),
                            mutation.newMeta(),
                            null);
                }
            }
        }
        if (!plan.redstoneUpdates().isEmpty()) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        blockEntity.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        fuelState.saveFuelTag(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelState.loadFuelTag(tag);
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
