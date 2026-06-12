package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.neutron.PileFuelState;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInteractionPlanner;
import com.hbm.ntm.neutron.PileGraphiteLifecyclePlanner;
import com.hbm.ntm.neutron.PileNeutronColumn;
import com.hbm.ntm.neutron.PileNeutronHandler;
import com.hbm.ntm.neutron.PileNeutronReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileFuelBlockEntity extends PileGraphiteBlockEntity implements PileNeutronColumn, PileNeutronReceiver {
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
            executeLifecycle(level, plan.lifecycle());
        }
        if (!plan.redstoneUpdates().isEmpty()) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        blockEntity.setChanged();
    }

    private static void executeLifecycle(Level level, PileGraphiteLifecyclePlanner.FuelLifecyclePlan lifecycle) {
        if (lifecycle.explosion() != null) {
            executeExplosion(level, lifecycle.explosion());
        }
        for (PileGraphiteInteractionPlanner.BlockMutation mutation : lifecycle.blockMutations()) {
            PileGraphiteDrilledBaseBlock.setLegacyBlock(
                    level,
                    mutation.pos(),
                    mutation.legacyBlockId(),
                    mutation.newMeta(),
                    null);
        }
        if (lifecycle.smokeParticle() != null) {
            spawnSmokeParticle(level, lifecycle.smokeParticle());
        }
    }

    private static void executeExplosion(Level level, PileGraphiteLifecyclePlanner.ExplosionPlan explosion) {
        BlockPos explosionPos = explosion.pos();
        level.explode(
                null,
                explosionPos.getX() + 0.5D,
                explosionPos.getY() + 0.5D,
                explosionPos.getZ() + 0.5D,
                explosion.radius(),
                explosion.flaming(),
                explosion.smoking() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    private static void spawnSmokeParticle(Level level, PileGraphiteLifecyclePlanner.ParticlePlan particle) {
        BlockPos particlePos = particle.pos();
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_VANILLA_EXT);
        data.putString("mode", ParticleUtil.VANILLA_SMOKE);
        data.putDouble("mX", 0.0D);
        data.putDouble("mY", 0.05D);
        data.putDouble("mZ", 0.0D);
        ParticleUtil.spawnAux(
                level,
                particlePos.getX() + 0.25D + level.random.nextDouble() * 0.5D,
                particlePos.getY() + 1.0D,
                particlePos.getZ() + 0.25D + level.random.nextDouble() * 0.5D,
                data,
                20.0D);
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
