package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PileGraphiteRegistrationPlanner {
    public static final String BLOCK_REGISTRY = "ModBlocks.BLOCKS";
    public static final String BLOCK_ENTITY_REGISTRY = "ModBlockEntities.BLOCK_ENTITIES";
    public static final String BLOCK_ITEM_REGISTRY = "ModItems.ITEMS";
    public static final String BLOCK_LOOKUP = "ModBlocks.BLOCKS_BY_LEGACY_NAME";
    public static final String DEFAULT_MAP_COLOR = "MapColor.METAL";
    public static final String DEFAULT_SOUND_TYPE = "SoundType.METAL";
    public static final String DEFAULT_TOOL_TAG = "minecraft:mineable/pickaxe";
    public static final String DEFAULT_TIER_TAG = "minecraft:needs_iron_tool";

    private PileGraphiteRegistrationPlanner() {
    }

    public static List<RegistrationPlan> allRegistrationPlans() {
        List<RegistrationPlan> plans = new ArrayList<>();
        for (String legacyId : PileGraphiteResourcePlanner.legacyBlockIds()) {
            registrationPlan(legacyId).ifPresent(plans::add);
        }
        return List.copyOf(plans);
    }

    public static Optional<RegistrationPlan> registrationPlan(String legacyId) {
        Optional<PileGraphiteBlockPlanner.BlockContract> block =
                PileGraphiteBlockPlanner.blockContract(legacyId);
        Optional<PileGraphiteResourcePlanner.ResourcePlan> resource =
                PileGraphiteResourcePlanner.resourcePlan(legacyId);
        if (block.isEmpty() || resource.isEmpty()) {
            return Optional.empty();
        }
        PileGraphiteBlockPlanner.BlockContract contract = block.get();
        return Optional.of(new RegistrationPlan(
                contract.legacyId(),
                blockRegistration(contract),
                blockItemRegistration(contract, resource.get()),
                blockEntityRegistration(contract),
                stateContract(contract),
                dataGeneration(contract, resource.get()),
                legacyMigration(contract),
                rolloutSteps(contract)));
    }

    public static List<BlockEntityTypePlan> blockEntityTypes() {
        return List.of(
                new BlockEntityTypePlan(
                        "pile_fuel",
                        "PileFuelBlockEntity",
                        "tileentity_pile_fuel",
                        List.of("block_graphite_fuel"),
                        "PileGraphiteBlockEntityPlanner.planFuelTick"),
                new BlockEntityTypePlan(
                        "pile_source",
                        "PileSourceBlockEntity",
                        "tileentity_pile_source",
                        List.of("block_graphite_plutonium", "block_graphite_source"),
                        "PileGraphiteBlockEntityPlanner.planSourceTick"),
                new BlockEntityTypePlan(
                        "pile_breeding_fuel",
                        "PileBreedingFuelBlockEntity",
                        "tileentity_pile_breedingfuel",
                        List.of("block_graphite_lithium"),
                        "PileGraphiteBlockEntityPlanner.planBreedingFuelTick"),
                new BlockEntityTypePlan(
                        "pile_neutron_detector",
                        "PileNeutronDetectorBlockEntity",
                        "tileentity_pile_neutrondetector",
                        List.of("block_graphite_detector"),
                        "PileGraphiteBlockEntityPlanner.planDetectorTick"));
    }

    public static BatchRolloutPlan rolloutPlan() {
        return new BatchRolloutPlan(
                allRegistrationPlans(),
                blockEntityTypes(),
                List.of(
                        "copy legacy block_graphite*.png textures before enabling generated models",
                        "register blocks and block items before BlockEntityType builders",
                        "wire BlockState datagen from PileGraphiteResourcePlanner variants",
                        "wire custom loot for drilled-family drops instead of dropSelf",
                        "register server tickers only for blocks with TileEntityCreation != NONE",
                        "bind tool interactions through PileGraphiteBlockEntityPlanner and PileGraphiteInteractionPlanner",
                        "bind neutron block behavior through PileGraphiteBlockPlanner.neutronContract",
                        "add old metadata migration before world save upgrade testing"));
    }

    private static BlockRegistrationPlan blockRegistration(PileGraphiteBlockPlanner.BlockContract contract) {
        return new BlockRegistrationPlan(
                contract.legacyId(),
                modernBlockClass(contract.legacyClass()),
                BLOCK_REGISTRY,
                BLOCK_LOOKUP,
                new BlockPropertyPlan(
                        DEFAULT_MAP_COLOR,
                        contract.material().hardness(),
                        contract.material().resistance(),
                        DEFAULT_SOUND_TYPE,
                        contract.material().flammabilityEncouragement(),
                        contract.material().flammability(),
                        contract.blockContainer()),
                contract.hiddenFromCreative());
    }

    private static BlockItemRegistrationPlan blockItemRegistration(
            PileGraphiteBlockPlanner.BlockContract contract,
            PileGraphiteResourcePlanner.ResourcePlan resource) {
        return new BlockItemRegistrationPlan(
                contract.legacyId(),
                BLOCK_ITEM_REGISTRY,
                contract.hiddenFromCreative() ? CreativeTabPlan.HIDDEN : CreativeTabPlan.BLOCK_TAB,
                "BlockItem",
                resource.itemModel().path(),
                resource.itemModel().parent());
    }

    private static Optional<BlockEntityAttachmentPlan> blockEntityRegistration(
            PileGraphiteBlockPlanner.BlockContract contract) {
        if (contract.tileEntity().creation() == PileGraphiteBlockPlanner.TileEntityCreation.NONE) {
            return Optional.empty();
        }
        BlockEntityTypePlan type = blockEntityTypes().stream()
                .filter(candidate -> candidate.legacyTileEntityId().equals(contract.tileEntity().legacyTileEntityId()))
                .findFirst()
                .orElseThrow();
        return Optional.of(new BlockEntityAttachmentPlan(
                type.registryName(),
                type.modernClassName(),
                type.legacyTileEntityId(),
                type.tickerPlanner(),
                contract.tileEntity().initialStateHints()));
    }

    private static StateContractPlan stateContract(PileGraphiteBlockPlanner.BlockContract contract) {
        if (!contract.icons().axisSensitive()) {
            return new StateContractPlan(false, List.of(), List.of(new MetadataMigrationPlan(0, "", false, false, false)));
        }
        List<MetadataMigrationPlan> migrations = new ArrayList<>();
        for (PileGraphiteResourcePlanner.GraphiteAxis axis : PileGraphiteResourcePlanner.GraphiteAxis.values()) {
            for (boolean aluminum : List.of(false, true)) {
                for (boolean active : List.of(false, true)) {
                    int meta = axis.legacyMeta()
                            | (aluminum ? PileGraphiteMetadata.ALUMINUM_MASK : 0)
                            | (active ? PileGraphiteMetadata.ACTIVE_MASK : 0);
                    migrations.add(new MetadataMigrationPlan(
                            meta,
                            axis.serializedName(),
                            aluminum,
                            active,
                            contract.tileEntity().creation() != PileGraphiteBlockPlanner.TileEntityCreation.NONE));
                }
            }
        }
        return new StateContractPlan(true, List.of("axis", "aluminum", "active"), migrations);
    }

    private static DataGenerationPlan dataGeneration(
            PileGraphiteBlockPlanner.BlockContract contract,
            PileGraphiteResourcePlanner.ResourcePlan resource) {
        LootTablePlan loot = contract.drops().normalBlockDrop()
                ? new LootTablePlan(LootKind.DROP_SELF, List.of(contract.legacyId()))
                : new LootTablePlan(
                        LootKind.DRILLED_GRAPHITE,
                        List.of("ingot_graphite x8", "optional aluminum shell", "optional inserted pile rod/cell"));
        return new DataGenerationPlan(
                resource.blockstatePath(),
                resource.models().stream().map(PileGraphiteResourcePlanner.ModelPlan::modelName).toList(),
                resource.textureCopies().stream().map(PileGraphiteResourcePlanner.TextureCopyPlan::modernPath).toList(),
                resource.itemModel().path(),
                loot,
                List.of(DEFAULT_TOOL_TAG, DEFAULT_TIER_TAG),
                languageKey(contract.legacyId()));
    }

    private static LegacyMigrationPlan legacyMigration(PileGraphiteBlockPlanner.BlockContract contract) {
        return new LegacyMigrationPlan(
                contract.legacyId(),
                stateContract(contract).metadataMigrations(),
                contract.tileEntity().legacyTileEntityId(),
                contract.tileEntity().initialStateHints());
    }

    private static List<String> rolloutSteps(PileGraphiteBlockPlanner.BlockContract contract) {
        List<String> steps = new ArrayList<>();
        steps.add("register " + contract.legacyId() + " in " + BLOCK_REGISTRY);
        steps.add("register block item and legacy lookup entry");
        steps.add("copy textures and enable blockstate/model/item model datagen");
        steps.add(contract.drops().normalBlockDrop() ? "use dropSelf loot" : "use drilled graphite custom loot");
        steps.add("add pickaxe and iron-tool tags");
        if (contract.tileEntity().creation() != PileGraphiteBlockPlanner.TileEntityCreation.NONE) {
            steps.add("attach BlockEntityType " + contract.tileEntity().legacyTileEntityId());
            steps.add("wire server ticker through " + blockEntityRegistration(contract).orElseThrow().tickerPlanner());
        }
        if (contract.interaction().directRightClickInsertion() || contract.interaction().screwdriverRemoval()) {
            steps.add("route block use/tool action to graphite interaction planner");
        }
        if (contract.neutron().role() != PileGraphiteBlockPlanner.NeutronInteractionRole.PASSIVE) {
            steps.add("route neutron collision behavior to graphite neutron contract");
        }
        return List.copyOf(steps);
    }

    private static String modernBlockClass(PileGraphiteBlockPlanner.LegacyBlockClass legacyClass) {
        return switch (legacyClass) {
            case GRAPHITE -> "PileGraphiteBlock";
            case DRILLED -> "PileGraphiteDrilledBlock";
            case FUEL -> "PileGraphiteFuelBlock";
            case PLUTONIUM_SOURCE, SOURCE -> "PileGraphiteSourceBlock";
            case BORON_ROD -> "PileGraphiteRodBlock";
            case BREEDING_FUEL -> "PileGraphiteBreedingFuelBlock";
            case BREEDING_PRODUCT -> "PileGraphiteBreedingProductBlock";
            case NEUTRON_DETECTOR -> "PileGraphiteNeutronDetectorBlock";
        };
    }

    private static String languageKey(String legacyId) {
        return "block.hbm_ntm_rebirth." + legacyId;
    }

    public enum CreativeTabPlan {
        BLOCK_TAB,
        HIDDEN
    }

    public enum LootKind {
        DROP_SELF,
        DRILLED_GRAPHITE
    }

    public record BatchRolloutPlan(
            List<RegistrationPlan> blockPlans,
            List<BlockEntityTypePlan> blockEntityTypes,
            List<String> globalSteps) {
        public BatchRolloutPlan {
            blockPlans = List.copyOf(blockPlans);
            blockEntityTypes = List.copyOf(blockEntityTypes);
            globalSteps = List.copyOf(globalSteps);
        }
    }

    public record RegistrationPlan(
            String legacyId,
            BlockRegistrationPlan block,
            BlockItemRegistrationPlan blockItem,
            Optional<BlockEntityAttachmentPlan> blockEntity,
            StateContractPlan state,
            DataGenerationPlan dataGeneration,
            LegacyMigrationPlan legacyMigration,
            List<String> rolloutSteps) {
        public RegistrationPlan {
            rolloutSteps = List.copyOf(rolloutSteps);
        }
    }

    public record BlockRegistrationPlan(
            String registryName,
            String modernBlockClassName,
            String registryOwner,
            String legacyLookupOwner,
            BlockPropertyPlan properties,
            boolean hiddenFromCreative) {
    }

    public record BlockPropertyPlan(
            String mapColor,
            float hardness,
            float resistance,
            String soundType,
            int flammabilityEncouragement,
            int flammability,
            boolean hasBlockEntity) {
    }

    public record BlockItemRegistrationPlan(
            String registryName,
            String registryOwner,
            CreativeTabPlan creativeTab,
            String itemClassName,
            String itemModelPath,
            String itemModelParent) {
    }

    public record BlockEntityTypePlan(
            String registryName,
            String modernClassName,
            String legacyTileEntityId,
            List<String> ownerLegacyBlocks,
            String tickerPlanner) {
        public BlockEntityTypePlan {
            ownerLegacyBlocks = List.copyOf(ownerLegacyBlocks);
        }
    }

    public record BlockEntityAttachmentPlan(
            String registryName,
            String modernClassName,
            String legacyTileEntityId,
            String tickerPlanner,
            List<PileGraphiteBlockPlanner.InitialStateHint> initialStateHints) {
        public BlockEntityAttachmentPlan {
            initialStateHints = List.copyOf(initialStateHints);
        }
    }

    public record StateContractPlan(
            boolean usesModernStateProperties,
            List<String> stateProperties,
            List<MetadataMigrationPlan> metadataMigrations) {
        public StateContractPlan {
            stateProperties = List.copyOf(stateProperties);
            metadataMigrations = List.copyOf(metadataMigrations);
        }
    }

    public record MetadataMigrationPlan(
            int legacyMeta,
            String axis,
            boolean aluminum,
            boolean active,
            boolean preserveBlockEntity) {
    }

    public record DataGenerationPlan(
            String blockstatePath,
            List<String> modelNames,
            List<String> textureTargets,
            String itemModelPath,
            LootTablePlan lootTable,
            List<String> blockTags,
            String languageKey) {
        public DataGenerationPlan {
            modelNames = List.copyOf(modelNames);
            textureTargets = List.copyOf(textureTargets);
            blockTags = List.copyOf(blockTags);
        }
    }

    public record LootTablePlan(LootKind kind, List<String> drops) {
        public LootTablePlan {
            drops = List.copyOf(drops);
        }
    }

    public record LegacyMigrationPlan(
            String legacyBlockId,
            List<MetadataMigrationPlan> metadataMigrations,
            String legacyTileEntityId,
            List<PileGraphiteBlockPlanner.InitialStateHint> initialStateHints) {
        public LegacyMigrationPlan {
            metadataMigrations = List.copyOf(metadataMigrations);
            initialStateHints = List.copyOf(initialStateHints);
        }
    }
}
