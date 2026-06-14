package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.block.PileGraphiteRodBlock;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.neutron.PileGraphiteMetadata;
import com.hbm.ntm.neutron.PileGraphiteNeutronRules;
import com.hbm.ntm.neutron.PileGraphiteTogglePlanner;
import com.hbm.ntm.neutron.PileNeutronColumn;
import com.hbm.ntm.neutron.PileNeutronDetectorState;
import com.hbm.ntm.neutron.PileNeutronPassthroughReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileNeutronDetectorBlockEntity extends PileGraphiteBlockEntity
        implements PileNeutronColumn, PileNeutronPassthroughReceiver {
    private final PileNeutronDetectorState detectorState = new PileNeutronDetectorState();

    public PileNeutronDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PILE_NEUTRON_DETECTOR.get(), pos, state);
    }

    public PileNeutronDetectorState detectorState() {
        return detectorState;
    }

    public void setMaxNeutrons(int maxNeutrons) {
        detectorState.setMaxNeutrons(maxNeutrons);
        setChanged();
    }

    @Override
    public void receiveNeutrons(int neutrons) {
        detectorState.receiveNeutrons(neutrons);
        setChanged();
    }

    @Override
    public boolean allowsPileNeutronPassthrough() {
        return level != null
                && PileGraphiteNeutronRules.detectorAllowsPassthrough(
                        PileGraphiteDrilledBaseBlock.legacyMeta(getBlockState()));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PileNeutronDetectorBlockEntity blockEntity) {
        int meta = PileGraphiteDrilledBaseBlock.legacyMeta(state);
        PileGraphiteBlockEntityPlanner.DetectorBlockEntityTickPlan plan =
                PileGraphiteBlockEntityPlanner.planDetectorTick(
                        pos,
                        meta,
                        blockEntity.detectorState,
                        probe -> PileGraphiteDrilledBaseBlock.chainState(
                                level,
                                probe,
                                PileGraphiteInsertionPlanner.GraphiteBlockKind.ROD));
        if (plan.lifecycle() != null && plan.lifecycle().togglePlan().hasMutations()) {
            for (PileGraphiteTogglePlanner.ToggleMutation mutation : plan.lifecycle().togglePlan().mutations()) {
                BlockState target = level.getBlockState(mutation.pos());
                if (target.getBlock() instanceof PileGraphiteRodBlock) {
                    level.setBlock(
                            mutation.pos(),
                            PileGraphiteDrilledBaseBlock.withLegacyMeta(target, mutation.newMeta()),
                            Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
                }
            }
        }
        for (PileGraphiteBlockEntityPlanner.SoundPlan sound : plan.sounds()) {
            playPlannedSound(level, sound);
        }
        blockEntity.setChanged();
    }

    private static void playPlannedSound(Level level, PileGraphiteBlockEntityPlanner.SoundPlan sound) {
        if ("hbm:item.techBleep".equals(sound.legacySoundId())) {
            LegacySoundPlayer.playLegacyTechBleep(level, sound.pos(), SoundSource.BLOCKS, sound.volume(), sound.pitch());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        detectorState.saveDetectorTag(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        detectorState.loadDetectorTag(tag);
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
