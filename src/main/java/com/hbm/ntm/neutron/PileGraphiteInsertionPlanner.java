package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PileGraphiteInsertionPlanner {
    public static final int MAX_PUSH_DISTANCE = 3;

    private PileGraphiteInsertionPlanner() {
    }

    public static InsertionPlan planInsertion(
            BlockPos origin,
            Direction direction,
            InsertedItem insertedItem,
            ColumnStateLookup lookup) {
        if (origin == null || direction == null || insertedItem == null || lookup == null) {
            return InsertionPlan.fail(FailureReason.INVALID_INPUT);
        }
        GraphiteBlockKind insertedBlock = insertedItem.insertedBlock();
        if (insertedBlock == null) {
            return InsertionPlan.fail(FailureReason.UNKNOWN_INSERTED_ITEM);
        }

        ColumnState originState = lookup.get(origin);
        if (originState == null || !originState.isGraphiteDrilledBase()) {
            return InsertionPlan.fail(FailureReason.ORIGIN_NOT_GRAPHITE_DRILLED);
        }

        int pureMeta = PileGraphiteMetadata.orientation(originState.meta());
        if (!PileGraphiteMetadata.sideMatchesAxis(originState.meta(), direction)) {
            return InsertionPlan.fail(FailureReason.WRONG_AXIS);
        }

        FailureReason validationFailure = validatePushPath(origin, direction, pureMeta, lookup);
        if (validationFailure != null) {
            return InsertionPlan.fail(validationFailure);
        }

        List<BlockMutation> mutations = new ArrayList<>();
        Ejection ejection = null;
        GraphiteBlockKind carriedBlock = insertedBlock;
        int carriedMeta = pureMeta | insertedItem.targetMetaBits();
        BlockPos carriedTileDataSource = null;
        boolean carriedTileDataReset = carriedBlock.hasBlockEntity();

        for (int i = 0; i <= MAX_PUSH_DISTANCE; i++) {
            BlockPos pos = origin.relative(direction, i);
            ColumnState current = lookup.get(pos);

            if (current != null && current.isGraphiteDrilledBase()) {
                int writtenMeta = PileGraphiteMetadata.preserveAluminum(carriedMeta, current.meta());
                mutations.add(new BlockMutation(
                        pos,
                        carriedBlock,
                        writtenMeta,
                        tileActionFor(carriedBlock, carriedTileDataSource, carriedTileDataReset)));

                GraphiteBlockKind displacedBlock = current.graphiteBlock();
                int displacedMeta = current.meta();
                BlockPos displacedTileDataSource = displacedBlock.hasBlockEntity() ? pos : null;

                carriedBlock = displacedBlock;
                carriedMeta = displacedMeta;
                carriedTileDataSource = displacedTileDataSource;
                carriedTileDataReset = false;

                if (carriedBlock == GraphiteBlockKind.DRILLED) {
                    break;
                }
            } else {
                InsertedItem ejectedItem = carriedBlock.insertedItem(carriedMeta);
                if (ejectedItem != InsertedItem.NONE) {
                    ejection = new Ejection(pos.relative(direction.getOpposite()), direction, ejectedItem);
                }
                break;
            }
        }

        return InsertionPlan.success(mutations, ejection);
    }

    private static FailureReason validatePushPath(
            BlockPos origin,
            Direction direction,
            int pureMeta,
            ColumnStateLookup lookup) {
        for (int i = 0; i <= MAX_PUSH_DISTANCE; i++) {
            ColumnState state = lookup.get(origin.relative(direction, i));
            if (state != null && state.isGraphiteDrilledBase()) {
                if (PileGraphiteMetadata.orientation(state.meta()) != pureMeta) {
                    return FailureReason.WRONG_GRAPHITE_ORIENTATION;
                }
                if (state.graphiteBlock().insertedItem(state.meta()) == InsertedItem.NONE) {
                    return null;
                }
                if (i >= MAX_PUSH_DISTANCE) {
                    return FailureReason.PUSH_LIMIT_REACHED;
                }
            } else {
                if (state != null && state.isNormalCube()) {
                    return FailureReason.BLOCKED_BY_SOLID;
                }
                return null;
            }
        }
        return null;
    }

    private static TileDataAction tileActionFor(
            GraphiteBlockKind block,
            BlockPos sourcePos,
            boolean reset) {
        if (!block.hasBlockEntity()) {
            return TileDataAction.none();
        }
        if (sourcePos != null) {
            return TileDataAction.copyFrom(sourcePos);
        }
        if (reset) {
            return TileDataAction.reset();
        }
        return TileDataAction.none();
    }

    @FunctionalInterface
    public interface ColumnStateLookup {
        ColumnState get(BlockPos pos);
    }

    public enum InsertedItem {
        NONE(null, null, 0),
        URANIUM("pile_rod_uranium", GraphiteBlockKind.FUEL, 0),
        PU239("pile_rod_pu239", GraphiteBlockKind.FUEL, PileGraphiteMetadata.ACTIVE_MASK),
        PLUTONIUM("pile_rod_plutonium", GraphiteBlockKind.PLUTONIUM, 0),
        SOURCE("pile_rod_source", GraphiteBlockKind.SOURCE, 0),
        BORON("pile_rod_boron", GraphiteBlockKind.ROD, 0),
        LITHIUM("pile_rod_lithium", GraphiteBlockKind.LITHIUM, 0),
        TRITIUM("cell_tritium", GraphiteBlockKind.TRITIUM, 0),
        DETECTOR("pile_rod_detector", GraphiteBlockKind.DETECTOR, 0);

        private final String legacyItemId;
        private final GraphiteBlockKind insertedBlock;
        private final int targetMetaBits;

        InsertedItem(String legacyItemId, GraphiteBlockKind insertedBlock, int targetMetaBits) {
            this.legacyItemId = legacyItemId;
            this.insertedBlock = insertedBlock;
            this.targetMetaBits = targetMetaBits;
        }

        public String legacyItemId() {
            return legacyItemId;
        }

        public GraphiteBlockKind insertedBlock() {
            return insertedBlock;
        }

        public int targetMetaBits() {
            return targetMetaBits;
        }

        public static Optional<InsertedItem> fromLegacyItemId(String legacyItemId) {
            for (InsertedItem item : values()) {
                if (item.legacyItemId != null && item.legacyItemId.equals(legacyItemId)) {
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        }
    }

    public enum GraphiteBlockKind {
        DRILLED("block_graphite_drilled", false),
        FUEL("block_graphite_fuel", true),
        PLUTONIUM("block_graphite_plutonium", true),
        ROD("block_graphite_rod", false),
        SOURCE("block_graphite_source", true),
        LITHIUM("block_graphite_lithium", true),
        TRITIUM("block_graphite_tritium", false),
        DETECTOR("block_graphite_detector", true);

        private final String legacyBlockId;
        private final boolean hasBlockEntity;

        GraphiteBlockKind(String legacyBlockId, boolean hasBlockEntity) {
            this.legacyBlockId = legacyBlockId;
            this.hasBlockEntity = hasBlockEntity;
        }

        public String legacyBlockId() {
            return legacyBlockId;
        }

        public boolean hasBlockEntity() {
            return hasBlockEntity;
        }

        public InsertedItem insertedItem(int meta) {
            return switch (this) {
                case DRILLED -> InsertedItem.NONE;
                case FUEL -> PileGraphiteMetadata.isActive(meta) ? InsertedItem.PU239 : InsertedItem.URANIUM;
                case PLUTONIUM -> InsertedItem.PLUTONIUM;
                case ROD -> InsertedItem.BORON;
                case SOURCE -> InsertedItem.SOURCE;
                case LITHIUM -> InsertedItem.LITHIUM;
                case TRITIUM -> InsertedItem.TRITIUM;
                case DETECTOR -> InsertedItem.DETECTOR;
            };
        }

        public static Optional<GraphiteBlockKind> fromLegacyBlockId(String legacyBlockId) {
            for (GraphiteBlockKind kind : values()) {
                if (kind.legacyBlockId.equals(legacyBlockId)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }
    }

    public enum FailureReason {
        INVALID_INPUT,
        UNKNOWN_INSERTED_ITEM,
        ORIGIN_NOT_GRAPHITE_DRILLED,
        WRONG_AXIS,
        WRONG_GRAPHITE_ORIENTATION,
        PUSH_LIMIT_REACHED,
        BLOCKED_BY_SOLID
    }

    public enum TileDataMode {
        NONE,
        RESET,
        COPY_FROM
    }

    public record ColumnState(GraphiteBlockKind graphiteBlock, int meta, boolean normalCube) {
        public static ColumnState graphite(GraphiteBlockKind graphiteBlock, int meta) {
            return new ColumnState(graphiteBlock, meta, false);
        }

        public static ColumnState empty() {
            return new ColumnState(null, 0, false);
        }

        public static ColumnState solid() {
            return new ColumnState(null, 0, true);
        }

        public boolean isGraphiteDrilledBase() {
            return graphiteBlock != null;
        }

        public boolean isNormalCube() {
            return normalCube;
        }
    }

    public record TileDataAction(TileDataMode mode, BlockPos sourcePos) {
        private static TileDataAction none() {
            return new TileDataAction(TileDataMode.NONE, null);
        }

        private static TileDataAction reset() {
            return new TileDataAction(TileDataMode.RESET, null);
        }

        private static TileDataAction copyFrom(BlockPos sourcePos) {
            return new TileDataAction(TileDataMode.COPY_FROM, sourcePos);
        }
    }

    public record BlockMutation(BlockPos pos, GraphiteBlockKind newBlock, int newMeta, TileDataAction tileDataAction) {
    }

    public record Ejection(BlockPos sourcePos, Direction direction, InsertedItem item) {
    }

    public record InsertionPlan(
            boolean accepted,
            FailureReason failureReason,
            List<BlockMutation> mutations,
            Ejection ejection) {
        private static InsertionPlan success(List<BlockMutation> mutations, Ejection ejection) {
            return new InsertionPlan(true, null, List.copyOf(mutations), ejection);
        }

        private static InsertionPlan fail(FailureReason reason) {
            return new InsertionPlan(false, reason, List.of(), null);
        }

        public boolean hasEjection() {
            return ejection != null;
        }
    }
}
