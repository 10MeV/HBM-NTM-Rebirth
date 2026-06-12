package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public final class PileGraphiteInteractionPlanner {
    public static final String LEGACY_BLOCK_GRAPHITE = "block_graphite";
    public static final String LEGACY_SOUND_UPGRADE_PLUG = "hbm:item.upgradePlug";
    public static final String LEGACY_SOUND_TECH_BLEEP = "hbm:item.techBleep";
    public static final String LEGACY_PARTICLE_GRAPHITE_DRILL = "ParticleBurstPacket(block_graphite,0)";
    public static final String LEGACY_STEP_SOUND_GRAPHITE = "block_graphite.step";

    private PileGraphiteInteractionPlanner() {
    }

    public static InteractionPlan planHandDrillGraphite(BlockPos pos, Direction side) {
        if (pos == null || side == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        int meta = PileGraphiteMetadata.orientationForSide(side);
        return InteractionPlan.builder()
                .addBlockMutation(new BlockMutation(pos, graphiteDrilledId(), meta))
                .addEjection(new ItemEjection(pos, side, LegacyItemStack.graphiteIngot()))
                .addParticle(new ParticleCue(LEGACY_PARTICLE_GRAPHITE_DRILL, pos))
                .addSound(new SoundCue(LEGACY_STEP_SOUND_GRAPHITE, pos, 1.0F, 0.8F))
                .buildAccepted();
    }

    public static InteractionPlan planDrilledBlockActivation(
            BlockPos pos,
            int oldMeta,
            Direction side,
            HeldItem heldItem) {
        if (pos == null || side == null || heldItem == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        if (!PileGraphiteMetadata.sideMatchesAxis(oldMeta, side)) {
            return InteractionPlan.fail(FailureReason.WRONG_AXIS);
        }

        if (heldItem.insertedItem() != PileGraphiteInsertionPlanner.InsertedItem.NONE) {
            PileGraphiteInsertionPlanner.InsertedItem insertedItem = heldItem.insertedItem();
            int newMeta = oldMeta | insertedItem.targetMetaBits();
            return plugPlan(pos, insertedItem.insertedBlock().legacyBlockId(), newMeta, true);
        }
        if (heldItem == HeldItem.ALUMINUM_SHELL) {
            if (PileGraphiteMetadata.hasAluminum(oldMeta)) {
                return InteractionPlan.fail(FailureReason.ALREADY_ALUMINUM_SHROUDED);
            }
            return plugPlan(pos, graphiteDrilledId(), oldMeta | PileGraphiteMetadata.ALUMINUM_MASK, true);
        }
        if (heldItem == HeldItem.GRAPHITE_INGOT) {
            if (PileGraphiteMetadata.hasAluminum(oldMeta)) {
                return InteractionPlan.fail(FailureReason.ALREADY_ALUMINUM_SHROUDED);
            }
            return plugPlan(pos, LEGACY_BLOCK_GRAPHITE, 0, true);
        }
        return InteractionPlan.fail(FailureReason.UNSUPPORTED_HELD_ITEM);
    }

    public static InteractionPlan planScrewdriver(
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            int oldMeta,
            Direction side,
            boolean playerSneaking) {
        if (pos == null || blockKind == null || side == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        if (blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.DRILLED) {
            return planDrilledShellRemoval(pos, oldMeta, side);
        }
        if (blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.DETECTOR && playerSneaking) {
            return InteractionPlan.builder()
                    .addDiagnostic(DiagnosticRequest.DETECTOR_STATUS)
                    .buildAccepted();
        }
        if (!PileGraphiteMetadata.sideMatchesAxis(oldMeta, side)) {
            return InteractionPlan.noAction(FailureReason.WRONG_AXIS);
        }

        PileGraphiteInsertionPlanner.InsertedItem ejected = blockKind.insertedItem(oldMeta);
        if (ejected == PileGraphiteInsertionPlanner.InsertedItem.NONE) {
            return InteractionPlan.noAction(FailureReason.NO_INSERTED_ITEM);
        }

        int drilledMeta = blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM
                ? oldMeta
                : oldMeta & ~PileGraphiteMetadata.ACTIVE_MASK;
        return InteractionPlan.builder()
                .addBlockMutation(new BlockMutation(pos, graphiteDrilledId(), drilledMeta))
                .addEjection(new ItemEjection(pos, side, LegacyItemStack.insertedItem(ejected)))
                .buildAccepted();
    }

    public static InteractionPlan planDetectorDefuser(BlockPos pos, boolean playerSneaking, int currentMaxNeutrons) {
        if (pos == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        int delta = playerSneaking ? (currentMaxNeutrons > 1 ? -1 : 0) : 1;
        return InteractionPlan.builder()
                .detectorThresholdDelta(delta)
                .buildAccepted();
    }

    public static InteractionPlan planFuelHandDrillDiagnostic(BlockPos pos, boolean pu239Rich) {
        if (pos == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        InteractionPlan.Builder builder = InteractionPlan.builder()
                .addDiagnostic(DiagnosticRequest.FUEL_STATUS);
        if (pu239Rich) {
            builder.addDiagnostic(DiagnosticRequest.FUEL_PU239_RICH);
        }
        return builder.buildAccepted();
    }

    public static InteractionPlan planBreedingFuelHandDrillDiagnostic(BlockPos pos) {
        if (pos == null) {
            return InteractionPlan.fail(FailureReason.INVALID_INPUT);
        }
        return InteractionPlan.builder()
                .addDiagnostic(DiagnosticRequest.BREEDING_FUEL_STATUS)
                .buildAccepted();
    }

    private static InteractionPlan planDrilledShellRemoval(BlockPos pos, int oldMeta, Direction side) {
        if (!PileGraphiteMetadata.sideMatchesAxis(oldMeta, side)) {
            return InteractionPlan.noAction(FailureReason.WRONG_AXIS);
        }
        if (!PileGraphiteMetadata.hasAluminum(oldMeta) || PileGraphiteMetadata.isActive(oldMeta)) {
            return InteractionPlan.noAction(FailureReason.NO_ALUMINUM_SHROUD);
        }
        int cfg = PileGraphiteMetadata.orientation(oldMeta);
        return InteractionPlan.builder()
                .addBlockMutation(new BlockMutation(pos, graphiteDrilledId(), cfg))
                .addEjection(new ItemEjection(pos, side, LegacyItemStack.aluminumShell()))
                .addSound(new SoundCue(LEGACY_SOUND_UPGRADE_PLUG, pos, 1.0F, 0.85F))
                .buildAccepted();
    }

    private static InteractionPlan plugPlan(BlockPos pos, String legacyBlockId, int newMeta, boolean consumeHeldItem) {
        return InteractionPlan.builder()
                .consumeHeldItem(consumeHeldItem)
                .addBlockMutation(new BlockMutation(pos, legacyBlockId, newMeta))
                .addSound(new SoundCue(LEGACY_SOUND_UPGRADE_PLUG, pos, 1.0F, 1.0F))
                .buildAccepted();
    }

    private static String graphiteDrilledId() {
        return PileGraphiteInsertionPlanner.GraphiteBlockKind.DRILLED.legacyBlockId();
    }

    public enum HeldItem {
        NONE(null),
        GRAPHITE_INGOT(null),
        ALUMINUM_SHELL(null),
        URANIUM(PileGraphiteInsertionPlanner.InsertedItem.URANIUM),
        PU239(PileGraphiteInsertionPlanner.InsertedItem.PU239),
        PLUTONIUM(PileGraphiteInsertionPlanner.InsertedItem.PLUTONIUM),
        SOURCE(PileGraphiteInsertionPlanner.InsertedItem.SOURCE),
        BORON(PileGraphiteInsertionPlanner.InsertedItem.BORON),
        LITHIUM(PileGraphiteInsertionPlanner.InsertedItem.LITHIUM),
        TRITIUM(PileGraphiteInsertionPlanner.InsertedItem.TRITIUM),
        DETECTOR(PileGraphiteInsertionPlanner.InsertedItem.DETECTOR);

        private final PileGraphiteInsertionPlanner.InsertedItem insertedItem;

        HeldItem(PileGraphiteInsertionPlanner.InsertedItem insertedItem) {
            this.insertedItem = insertedItem == null
                    ? PileGraphiteInsertionPlanner.InsertedItem.NONE
                    : insertedItem;
        }

        public PileGraphiteInsertionPlanner.InsertedItem insertedItem() {
            return insertedItem;
        }
    }

    public enum FailureReason {
        INVALID_INPUT,
        WRONG_AXIS,
        UNSUPPORTED_HELD_ITEM,
        ALREADY_ALUMINUM_SHROUDED,
        NO_ALUMINUM_SHROUD,
        NO_INSERTED_ITEM
    }

    public enum DiagnosticRequest {
        FUEL_STATUS,
        FUEL_PU239_RICH,
        BREEDING_FUEL_STATUS,
        DETECTOR_STATUS
    }

    public record LegacyItemStack(String legacyItemId, int count, String legacyMetaKey) {
        public static LegacyItemStack graphiteIngot() {
            return new LegacyItemStack("ingot_graphite", 1, "");
        }

        public static LegacyItemStack aluminumShell() {
            return new LegacyItemStack("shell_aluminium", 1, "");
        }

        public static LegacyItemStack insertedItem(PileGraphiteInsertionPlanner.InsertedItem insertedItem) {
            return new LegacyItemStack(insertedItem.legacyItemId(), 1, "");
        }
    }

    public record BlockMutation(BlockPos pos, String legacyBlockId, int newMeta) {
    }

    public record ItemEjection(BlockPos sourcePos, Direction direction, LegacyItemStack stack) {
    }

    public record SoundCue(String legacySoundId, BlockPos pos, float volume, float pitch) {
    }

    public record ParticleCue(String legacyParticleId, BlockPos pos) {
    }

    public record InteractionPlan(
            boolean accepted,
            FailureReason failureReason,
            boolean consumeHeldItem,
            List<BlockMutation> blockMutations,
            List<ItemEjection> ejections,
            List<SoundCue> sounds,
            List<ParticleCue> particles,
            List<DiagnosticRequest> diagnostics,
            int detectorThresholdDelta) {
        private static Builder builder() {
            return new Builder();
        }

        private static InteractionPlan fail(FailureReason reason) {
            return new InteractionPlan(false, reason, false, List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        }

        private static InteractionPlan noAction(FailureReason reason) {
            return new InteractionPlan(true, reason, false, List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        }

        public boolean hasWorldMutation() {
            return !blockMutations.isEmpty() || detectorThresholdDelta != 0;
        }

        private static final class Builder {
            private boolean consumeHeldItem;
            private final java.util.ArrayList<BlockMutation> blockMutations = new java.util.ArrayList<>();
            private final java.util.ArrayList<ItemEjection> ejections = new java.util.ArrayList<>();
            private final java.util.ArrayList<SoundCue> sounds = new java.util.ArrayList<>();
            private final java.util.ArrayList<ParticleCue> particles = new java.util.ArrayList<>();
            private final java.util.ArrayList<DiagnosticRequest> diagnostics = new java.util.ArrayList<>();
            private int detectorThresholdDelta;

            private Builder consumeHeldItem(boolean consumeHeldItem) {
                this.consumeHeldItem = consumeHeldItem;
                return this;
            }

            private Builder addBlockMutation(BlockMutation mutation) {
                this.blockMutations.add(mutation);
                return this;
            }

            private Builder addEjection(ItemEjection ejection) {
                this.ejections.add(ejection);
                return this;
            }

            private Builder addSound(SoundCue sound) {
                this.sounds.add(sound);
                return this;
            }

            private Builder addParticle(ParticleCue particle) {
                this.particles.add(particle);
                return this;
            }

            private Builder addDiagnostic(DiagnosticRequest diagnostic) {
                this.diagnostics.add(diagnostic);
                return this;
            }

            private Builder detectorThresholdDelta(int delta) {
                this.detectorThresholdDelta = delta;
                return this;
            }

            private InteractionPlan buildAccepted() {
                return new InteractionPlan(
                        true,
                        null,
                        consumeHeldItem,
                        List.copyOf(blockMutations),
                        List.copyOf(ejections),
                        List.copyOf(sounds),
                        List.copyOf(particles),
                        List.copyOf(diagnostics),
                        detectorThresholdDelta);
            }
        }
    }
}
