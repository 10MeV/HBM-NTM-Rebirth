package com.hbm.ntm.neutron;

import java.util.List;
import java.util.Optional;

public final class PileGraphiteBlockPlanner {
    public static final float LEGACY_HARDNESS = 5.0F;
    public static final float LEGACY_RESISTANCE = 10.0F;
    public static final int GRAPHITE_FLAMMABILITY_ENCOURAGEMENT = 30;
    public static final int GRAPHITE_FLAMMABILITY = 5;
    public static final int PILE_SOURCE_RAYS_PER_TICK = 12;
    public static final int SOURCE_RAY_FLUX = 1;
    public static final int PLUTONIUM_SOURCE_RAY_FLUX = 2;
    public static final int FUEL_INITIAL_PU239_PROGRESS_OFFSET = 1000;
    public static final String LEGACY_RANDOM_CLICK_SOUND = "random.click";

    private PileGraphiteBlockPlanner() {
    }

    public static Optional<BlockContract> blockContract(String legacyId) {
        String id = legacyId == null ? "" : legacyId;
        return switch (id) {
            case "block_graphite" -> Optional.of(graphiteBlock());
            case "block_graphite_drilled" -> Optional.of(drilledBlock(id, LegacyBlockClass.DRILLED, null));
            case "block_graphite_fuel" -> Optional.of(drilledBlock(
                    id,
                    LegacyBlockClass.FUEL,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.FUEL));
            case "block_graphite_plutonium" -> Optional.of(sourceBlock(
                    id,
                    LegacyBlockClass.PLUTONIUM_SOURCE,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.PLUTONIUM,
                    "block_graphite_plutonium_aluminum",
                    PLUTONIUM_SOURCE_RAY_FLUX));
            case "block_graphite_rod" -> Optional.of(drilledBlock(
                    id,
                    LegacyBlockClass.BORON_ROD,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.ROD));
            case "block_graphite_source" -> Optional.of(sourceBlock(
                    id,
                    LegacyBlockClass.SOURCE,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.SOURCE,
                    "block_graphite_source_aluminum",
                    SOURCE_RAY_FLUX));
            case "block_graphite_lithium" -> Optional.of(drilledBlock(
                    id,
                    LegacyBlockClass.BREEDING_FUEL,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM));
            case "block_graphite_tritium" -> Optional.of(drilledBlock(
                    id,
                    LegacyBlockClass.BREEDING_PRODUCT,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.TRITIUM));
            case "block_graphite_detector" -> Optional.of(drilledBlock(
                    id,
                    LegacyBlockClass.NEUTRON_DETECTOR,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.DETECTOR));
            default -> Optional.empty();
        };
    }

    public static TileEntityContract tileEntityPlan(String legacyId, int meta) {
        return blockContract(legacyId)
                .map(contract -> contract.tileEntity().withMeta(meta))
                .orElse(TileEntityContract.none());
    }

    public static IconPlan iconForSide(String legacyId, int meta, int side) {
        Optional<BlockContract> contract = blockContract(legacyId);
        if (contract.isEmpty()) {
            return new IconPlan(IconRole.MISSING, "");
        }
        return contract.get().icons().iconForSide(meta, side);
    }

    public static NeutronContract neutronContract(String legacyId, int meta) {
        Optional<BlockContract> contract = blockContract(legacyId);
        if (contract.isEmpty()) {
            return NeutronContract.none();
        }
        return contract.get().neutron().withMeta(meta);
    }

    public static DropContract dropContract(String legacyId, int meta) {
        Optional<BlockContract> contract = blockContract(legacyId);
        if (contract.isEmpty()) {
            return DropContract.none();
        }
        return contract.get().drops().withMeta(meta, contract.get().graphiteKind());
    }

    private static BlockContract graphiteBlock() {
        return new BlockContract(
                "block_graphite",
                LegacyBlockClass.GRAPHITE,
                null,
                "BlockGraphite",
                CreativeTabRole.BLOCK,
                false,
                false,
                graphiteMaterial(),
                new IconContract(
                        "block_graphite",
                        "block_graphite",
                        "",
                        "",
                        "",
                        false),
                TileEntityContract.none(),
                DropContract.normalBlockDrop("block_graphite"),
                InteractionContract.plainGraphite(),
                NeutronContract.passive());
    }

    private static BlockContract drilledBlock(
            String id,
            LegacyBlockClass blockClass,
            PileGraphiteInsertionPlanner.GraphiteBlockKind kind) {
        String javaClass = switch (blockClass) {
            case DRILLED -> "BlockGraphiteDrilled";
            case FUEL -> "BlockGraphiteFuel";
            case BORON_ROD -> "BlockGraphiteRod";
            case BREEDING_FUEL -> "BlockGraphiteBreedingFuel";
            case BREEDING_PRODUCT -> "BlockGraphiteBreedingProduct";
            case NEUTRON_DETECTOR -> "BlockGraphiteNeutronDetector";
            default -> "BlockGraphiteDrilledBase";
        };
        return new BlockContract(
                id,
                blockClass,
                kind,
                javaClass,
                CreativeTabRole.HIDDEN,
                true,
                hasTileEntity(blockClass),
                graphiteMaterial(),
                iconContract(id, blockClass),
                tileEntityContract(id, blockClass),
                DropContract.drilledBase(),
                interactionContract(blockClass),
                neutronContractFor(blockClass));
    }

    private static BlockContract sourceBlock(
            String id,
            LegacyBlockClass blockClass,
            PileGraphiteInsertionPlanner.GraphiteBlockKind kind,
            String aluminumTexture,
            int rayFlux) {
        return new BlockContract(
                id,
                blockClass,
                kind,
                "BlockGraphiteSource",
                CreativeTabRole.HIDDEN,
                true,
                true,
                graphiteMaterial(),
                new IconContract(id, "block_graphite", aluminumTexture, "", "", true),
                new TileEntityContract(
                        "tileentity_pile_source",
                        TileEntityCreation.PILE_SOURCE,
                        List.of(new InitialStateHint(InitialStateKind.SOURCE_RAY_FLUX, rayFlux))),
                DropContract.drilledBase(),
                InteractionContract.screwdriverRemovable(),
                new NeutronContract(NeutronInteractionRole.PILE_SOURCE, false, false, rayFlux));
    }

    private static MaterialContract graphiteMaterial() {
        return new MaterialContract(
                "Material.iron",
                "Block.soundTypeMetal",
                LEGACY_HARDNESS,
                LEGACY_RESISTANCE,
                GRAPHITE_FLAMMABILITY_ENCOURAGEMENT,
                GRAPHITE_FLAMMABILITY);
    }

    private static boolean hasTileEntity(LegacyBlockClass blockClass) {
        return switch (blockClass) {
            case FUEL, PLUTONIUM_SOURCE, SOURCE, BREEDING_FUEL, NEUTRON_DETECTOR -> true;
            default -> false;
        };
    }

    private static IconContract iconContract(String id, LegacyBlockClass blockClass) {
        return switch (blockClass) {
            case BORON_ROD -> new IconContract(
                    "block_graphite_rod_in",
                    "block_graphite",
                    "block_graphite_rod_in_aluminum",
                    "block_graphite_rod_out",
                    "block_graphite_rod_out_aluminum",
                    true);
            case FUEL -> new IconContract(id, "block_graphite", "block_graphite_fuel_aluminum", "", "", true);
            case BREEDING_FUEL -> new IconContract(id, "block_graphite", "block_graphite_lithium_aluminum", "", "", true);
            case BREEDING_PRODUCT -> new IconContract(id, "block_graphite", "block_graphite_tritium_aluminum", "", "", true);
            case NEUTRON_DETECTOR -> new IconContract(
                    id,
                    "block_graphite",
                    "block_graphite_detector_aluminum",
                    "block_graphite_detector_out",
                    "block_graphite_detector_out_aluminum",
                    true);
            default -> new IconContract(id, "block_graphite", "block_graphite_drilled_aluminum", "", "", true);
        };
    }

    private static TileEntityContract tileEntityContract(String id, LegacyBlockClass blockClass) {
        return switch (blockClass) {
            case FUEL -> new TileEntityContract(
                    "tileentity_pile_fuel",
                    TileEntityCreation.PILE_FUEL,
                    List.of());
            case BREEDING_FUEL -> new TileEntityContract(
                    "tileentity_pile_breedingfuel",
                    TileEntityCreation.PILE_BREEDING_FUEL,
                    List.of());
            case NEUTRON_DETECTOR -> new TileEntityContract(
                    "tileentity_pile_neutrondetector",
                    TileEntityCreation.PILE_NEUTRON_DETECTOR,
                    List.of(new InitialStateHint(InitialStateKind.DETECTOR_MAX_NEUTRONS_DEFAULT, 10)));
            default -> TileEntityContract.none();
        };
    }

    private static InteractionContract interactionContract(LegacyBlockClass blockClass) {
        return switch (blockClass) {
            case DRILLED -> new InteractionContract(
                    true,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "right click axial face inserts rods/shell/graphite directly; screwdriver removes aluminum shell only");
            case FUEL -> new InteractionContract(
                    false,
                    true,
                    true,
                    false,
                    false,
                    true,
                    true,
                    "screwdriver removes uranium/Pu-239 rod; hand drill prints heat/depletion/flux diagnostics; fan cools heat by 2.5%");
            case BORON_ROD -> new InteractionContract(
                    false,
                    true,
                    false,
                    true,
                    false,
                    false,
                    false,
                    "non-sneaking axial right click toggles active bit and matching rod chain");
            case BREEDING_FUEL -> new InteractionContract(
                    false,
                    true,
                    true,
                    false,
                    false,
                    false,
                    false,
                    "screwdriver removes lithium rod; hand drill prints depletion/flux diagnostics");
            case NEUTRON_DETECTOR -> new InteractionContract(
                    false,
                    true,
                    true,
                    false,
                    true,
                    false,
                    false,
                    "screwdriver removes detector or, while sneaking, prints flux threshold; defuser adjusts maxNeutrons");
            default -> InteractionContract.screwdriverRemovable();
        };
    }

    private static NeutronContract neutronContractFor(LegacyBlockClass blockClass) {
        return switch (blockClass) {
            case FUEL, BREEDING_FUEL -> new NeutronContract(NeutronInteractionRole.RECEIVER_STOPS_STREAM, false, false, 0);
            case NEUTRON_DETECTOR ->
                    new NeutronContract(NeutronInteractionRole.DETECTOR_RECEIVER, true, false, 0);
            case BORON_ROD -> new NeutronContract(NeutronInteractionRole.ROD_BLOCKER_WHEN_INACTIVE, false, true, 0);
            default -> NeutronContract.passive();
        };
    }

    public enum LegacyBlockClass {
        GRAPHITE,
        DRILLED,
        FUEL,
        PLUTONIUM_SOURCE,
        BORON_ROD,
        SOURCE,
        BREEDING_FUEL,
        BREEDING_PRODUCT,
        NEUTRON_DETECTOR
    }

    public enum CreativeTabRole {
        BLOCK,
        HIDDEN
    }

    public enum TileEntityCreation {
        NONE,
        PILE_FUEL,
        PILE_SOURCE,
        PILE_BREEDING_FUEL,
        PILE_NEUTRON_DETECTOR
    }

    public enum InitialStateKind {
        NONE,
        PU239_PROGRESS_PRELOAD,
        SOURCE_RAY_FLUX,
        DETECTOR_MAX_NEUTRONS_DEFAULT
    }

    public enum IconRole {
        AXIAL_FACE,
        AXIAL_FACE_ALUMINUM,
        AXIAL_FACE_ACTIVE,
        AXIAL_FACE_ACTIVE_ALUMINUM,
        SIDE_GRAPHITE,
        MISSING
    }

    public enum NeutronInteractionRole {
        NONE,
        PASSIVE,
        RECEIVER_STOPS_STREAM,
        DETECTOR_RECEIVER,
        ROD_BLOCKER_WHEN_INACTIVE,
        PILE_SOURCE
    }

    public record BlockContract(
            String legacyId,
            LegacyBlockClass legacyClass,
            PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind,
            String legacyJavaClass,
            CreativeTabRole creativeTab,
            boolean hiddenFromCreative,
            boolean blockContainer,
            MaterialContract material,
            IconContract icons,
            TileEntityContract tileEntity,
            DropContract drops,
            InteractionContract interaction,
            NeutronContract neutron) {
    }

    public record MaterialContract(
            String materialName,
            String stepSound,
            float hardness,
            float resistance,
            int flammabilityEncouragement,
            int flammability) {
    }

    public record IconContract(
            String axialTexture,
            String sideTexture,
            String axialAluminumTexture,
            String axialActiveTexture,
            String axialActiveAluminumTexture,
            boolean axisSensitive) {
        public IconPlan iconForSide(int meta, int side) {
            if (!axisSensitive) {
                return new IconPlan(IconRole.AXIAL_FACE, axialTexture);
            }
            int orientation = PileGraphiteMetadata.orientation(meta);
            boolean axialSide = side == orientation * 2 || side == orientation * 2 + 1;
            if (!axialSide) {
                return new IconPlan(IconRole.SIDE_GRAPHITE, sideTexture);
            }
            boolean aluminum = PileGraphiteMetadata.hasAluminum(meta);
            boolean active = PileGraphiteMetadata.isActive(meta);
            if (active && aluminum && !axialActiveAluminumTexture.isEmpty()) {
                return new IconPlan(IconRole.AXIAL_FACE_ACTIVE_ALUMINUM, axialActiveAluminumTexture);
            }
            if (active && !axialActiveTexture.isEmpty()) {
                return new IconPlan(IconRole.AXIAL_FACE_ACTIVE, axialActiveTexture);
            }
            if (aluminum && !axialAluminumTexture.isEmpty()) {
                return new IconPlan(IconRole.AXIAL_FACE_ALUMINUM, axialAluminumTexture);
            }
            return new IconPlan(IconRole.AXIAL_FACE, axialTexture);
        }
    }

    public record IconPlan(IconRole role, String texture) {
    }

    public record InitialStateHint(InitialStateKind kind, int value) {
    }

    public record TileEntityContract(
            String legacyTileEntityId,
            TileEntityCreation creation,
            List<InitialStateHint> initialStateHints) {
        private static TileEntityContract none() {
            return new TileEntityContract("", TileEntityCreation.NONE, List.of());
        }

        public TileEntityContract {
            initialStateHints = List.copyOf(initialStateHints);
        }

        private TileEntityContract withMeta(int meta) {
            if (creation != TileEntityCreation.PILE_FUEL || !PileGraphiteMetadata.isActive(meta)) {
                return this;
            }
            return new TileEntityContract(
                    legacyTileEntityId,
                    creation,
                    List.of(new InitialStateHint(
                            InitialStateKind.PU239_PROGRESS_PRELOAD,
                            FUEL_INITIAL_PU239_PROGRESS_OFFSET)));
        }
    }

    public record DropContract(
            boolean normalBlockDrop,
            String normalDropLegacyId,
            int graphiteIngotCount,
            boolean dropsAluminumShellFromMeta,
            boolean dropsInsertedItemFromKind,
            boolean aluminumShellDrop,
            PileGraphiteInsertionPlanner.InsertedItem insertedItemDrop) {
        private static DropContract none() {
            return new DropContract(
                    false,
                    "",
                    0,
                    false,
                    false,
                    false,
                    PileGraphiteInsertionPlanner.InsertedItem.NONE);
        }

        private static DropContract normalBlockDrop(String legacyId) {
            return new DropContract(
                    true,
                    legacyId,
                    0,
                    false,
                    false,
                    false,
                    PileGraphiteInsertionPlanner.InsertedItem.NONE);
        }

        private static DropContract drilledBase() {
            return new DropContract(
                    false,
                    "",
                    8,
                    true,
                    true,
                    false,
                    PileGraphiteInsertionPlanner.InsertedItem.NONE);
        }

        private DropContract withMeta(int meta, PileGraphiteInsertionPlanner.GraphiteBlockKind kind) {
            if (!dropsAluminumShellFromMeta && !dropsInsertedItemFromKind) {
                return this;
            }
            PileGraphiteInsertionPlanner.InsertedItem insertedItem = kind == null
                    ? PileGraphiteInsertionPlanner.InsertedItem.NONE
                    : kind.insertedItem(meta);
            return new DropContract(
                    normalBlockDrop,
                    normalDropLegacyId,
                    graphiteIngotCount,
                    dropsAluminumShellFromMeta,
                    dropsInsertedItemFromKind,
                    dropsAluminumShellFromMeta && PileGraphiteMetadata.hasAluminum(meta),
                    dropsInsertedItemFromKind ? insertedItem : PileGraphiteInsertionPlanner.InsertedItem.NONE);
        }
    }

    public record InteractionContract(
            boolean directRightClickInsertion,
            boolean screwdriverRemoval,
            boolean handDrillDiagnostic,
            boolean rodChainToggle,
            boolean defuserThresholdAdjustment,
            boolean fanCooling,
            boolean comparatorFromProgress,
            String note) {
        private static InteractionContract plainGraphite() {
            return new InteractionContract(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "hand drill turns graphite into block_graphite_drilled, emits one graphite ingot, particle burst, and graphite step sound");
        }

        private static InteractionContract screwdriverRemovable() {
            return new InteractionContract(
                    false,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    "screwdriver on axial face reverts to block_graphite_drilled and ejects inserted item");
        }
    }

    public record NeutronContract(
            NeutronInteractionRole role,
            boolean passthroughWhenActive,
            boolean blocksWhenInactive,
            int rayFlux) {
        private static NeutronContract none() {
            return new NeutronContract(NeutronInteractionRole.NONE, false, false, 0);
        }

        private static NeutronContract passive() {
            return new NeutronContract(NeutronInteractionRole.PASSIVE, false, false, 0);
        }

        private NeutronContract withMeta(int meta) {
            if (role == NeutronInteractionRole.DETECTOR_RECEIVER) {
                return new NeutronContract(role, PileGraphiteMetadata.isActive(meta), false, rayFlux);
            }
            if (role == NeutronInteractionRole.ROD_BLOCKER_WHEN_INACTIVE) {
                return new NeutronContract(role, false, !PileGraphiteMetadata.isActive(meta), rayFlux);
            }
            return this;
        }
    }
}
