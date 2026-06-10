package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PileGraphiteResourcePlanner {
    public static final String LEGACY_TEXTURE_ROOT = "assets/hbm/textures/blocks/";
    public static final String MODERN_TEXTURE_ROOT = "assets/hbm_ntm_rebirth/textures/block/";
    public static final String CUBE_ALL_PARENT = "minecraft:block/cube_all";
    public static final String CUBE_COLUMN_PARENT = "minecraft:block/cube_column";
    public static final String BLOCK_ITEM_PARENT_PREFIX = "hbm_ntm_rebirth:block/";

    private static final List<String> LEGACY_BLOCK_IDS = List.of(
            "block_graphite",
            "block_graphite_drilled",
            "block_graphite_fuel",
            "block_graphite_plutonium",
            "block_graphite_rod",
            "block_graphite_source",
            "block_graphite_lithium",
            "block_graphite_tritium",
            "block_graphite_detector");

    private PileGraphiteResourcePlanner() {
    }

    public static List<String> legacyBlockIds() {
        return LEGACY_BLOCK_IDS;
    }

    public static List<ResourcePlan> allResourcePlans() {
        List<ResourcePlan> plans = new ArrayList<>();
        for (String id : LEGACY_BLOCK_IDS) {
            resourcePlan(id).ifPresent(plans::add);
        }
        return List.copyOf(plans);
    }

    public static Optional<ResourcePlan> resourcePlan(String legacyId) {
        Optional<PileGraphiteBlockPlanner.BlockContract> contract =
                PileGraphiteBlockPlanner.blockContract(legacyId);
        return contract.map(PileGraphiteResourcePlanner::resourcePlan);
    }

    private static ResourcePlan resourcePlan(PileGraphiteBlockPlanner.BlockContract contract) {
        List<StateVariantPlan> variants = stateVariants(contract);
        List<ModelPlan> models = modelPlans(contract, variants);
        return new ResourcePlan(
                contract.legacyId(),
                blockstatePath(contract.legacyId()),
                models,
                variants,
                itemModelPlan(contract, variants),
                textureCopyPlans(contract));
    }

    private static List<StateVariantPlan> stateVariants(PileGraphiteBlockPlanner.BlockContract contract) {
        if (!contract.icons().axisSensitive()) {
            return List.of(new StateVariantPlan(
                    "",
                    0,
                    GraphiteAxis.Y,
                    false,
                    false,
                    modelName(contract.legacyId(), false, false),
                    RotationPlan.none()));
        }

        List<StateVariantPlan> variants = new ArrayList<>();
        for (GraphiteAxis axis : GraphiteAxis.values()) {
            for (boolean aluminum : List.of(false, true)) {
                for (boolean active : List.of(false, true)) {
                    int meta = axis.legacyMeta() |
                            (aluminum ? PileGraphiteMetadata.ALUMINUM_MASK : 0) |
                            (active ? PileGraphiteMetadata.ACTIVE_MASK : 0);
                    variants.add(new StateVariantPlan(
                            stateKey(axis, aluminum, active),
                            meta,
                            axis,
                            aluminum,
                            active,
                            modelName(contract.legacyId(), aluminum, active),
                            axis.rotation()));
                }
            }
        }
        return List.copyOf(variants);
    }

    private static List<ModelPlan> modelPlans(
            PileGraphiteBlockPlanner.BlockContract contract,
            List<StateVariantPlan> variants) {
        Map<String, ModelPlan> models = new LinkedHashMap<>();
        if (!contract.icons().axisSensitive()) {
            models.put(contract.legacyId(), new ModelPlan(
                    contract.legacyId(),
                    CUBE_ALL_PARENT,
                    contract.icons().axialTexture(),
                    "",
                    contract.icons().axialTexture()));
            return List.copyOf(models.values());
        }

        for (StateVariantPlan variant : variants) {
            PileGraphiteBlockPlanner.IconPlan axialIcon =
                    contract.icons().iconForSide(variant.legacyMeta(), variant.axis().representativeLegacySide());
            models.putIfAbsent(variant.modelName(), new ModelPlan(
                    variant.modelName(),
                    CUBE_COLUMN_PARENT,
                    contract.icons().sideTexture(),
                    axialIcon.texture(),
                    ""));
        }
        return List.copyOf(models.values());
    }

    private static ItemModelPlan itemModelPlan(
            PileGraphiteBlockPlanner.BlockContract contract,
            List<StateVariantPlan> variants) {
        String parent = contract.icons().axisSensitive()
                ? modelName(contract.legacyId(), false, false)
                : contract.legacyId();
        return new ItemModelPlan(
                itemModelPath(contract.legacyId()),
                BLOCK_ITEM_PARENT_PREFIX + parent,
                contract.hiddenFromCreative());
    }

    private static List<TextureCopyPlan> textureCopyPlans(PileGraphiteBlockPlanner.BlockContract contract) {
        Set<String> textures = new LinkedHashSet<>();
        addTexture(textures, contract.icons().axialTexture());
        addTexture(textures, contract.icons().sideTexture());
        addTexture(textures, contract.icons().axialAluminumTexture());
        addTexture(textures, contract.icons().axialActiveTexture());
        addTexture(textures, contract.icons().axialActiveAluminumTexture());

        List<TextureCopyPlan> plans = new ArrayList<>();
        for (String texture : textures) {
            plans.add(new TextureCopyPlan(
                    texture,
                    LEGACY_TEXTURE_ROOT + texture + ".png",
                    MODERN_TEXTURE_ROOT + texture + ".png"));
        }
        return List.copyOf(plans);
    }

    private static void addTexture(Set<String> textures, String texture) {
        if (texture != null && !texture.isEmpty()) {
            textures.add(texture);
        }
    }

    private static String stateKey(GraphiteAxis axis, boolean aluminum, boolean active) {
        return "axis=" + axis.serializedName() + ",aluminum=" + aluminum + ",active=" + active;
    }

    private static String modelName(String legacyId, boolean aluminum, boolean active) {
        if (!aluminum && !active) {
            return legacyId;
        }
        String suffix = (active ? "_active" : "") + (aluminum ? "_aluminum" : "");
        return legacyId + suffix;
    }

    private static String blockstatePath(String legacyId) {
        return "assets/hbm_ntm_rebirth/blockstates/" + legacyId + ".json";
    }

    private static String itemModelPath(String legacyId) {
        return "assets/hbm_ntm_rebirth/models/item/" + legacyId + ".json";
    }

    public enum GraphiteAxis {
        Y(0, "y", 0, RotationPlan.none()),
        Z(1, "z", 2, new RotationPlan(90, 0)),
        X(2, "x", 4, new RotationPlan(90, 90));

        private final int legacyMeta;
        private final String serializedName;
        private final int representativeLegacySide;
        private final RotationPlan rotation;

        GraphiteAxis(int legacyMeta, String serializedName, int representativeLegacySide, RotationPlan rotation) {
            this.legacyMeta = legacyMeta;
            this.serializedName = serializedName;
            this.representativeLegacySide = representativeLegacySide;
            this.rotation = rotation;
        }

        public int legacyMeta() {
            return legacyMeta;
        }

        public String serializedName() {
            return serializedName;
        }

        public int representativeLegacySide() {
            return representativeLegacySide;
        }

        public RotationPlan rotation() {
            return rotation;
        }
    }

    public record ResourcePlan(
            String legacyBlockId,
            String blockstatePath,
            List<ModelPlan> models,
            List<StateVariantPlan> variants,
            ItemModelPlan itemModel,
            List<TextureCopyPlan> textureCopies) {
        public ResourcePlan {
            models = List.copyOf(models);
            variants = List.copyOf(variants);
            textureCopies = List.copyOf(textureCopies);
        }
    }

    public record StateVariantPlan(
            String stateKey,
            int legacyMeta,
            GraphiteAxis axis,
            boolean aluminum,
            boolean active,
            String modelName,
            RotationPlan rotation) {
    }

    public record ModelPlan(
            String modelName,
            String parent,
            String sideTexture,
            String endTexture,
            String allTexture) {
    }

    public record ItemModelPlan(String path, String parent, boolean hiddenFromCreative) {
    }

    public record TextureCopyPlan(String textureName, String legacyPath, String modernPath) {
    }

    public record RotationPlan(int xDegrees, int yDegrees) {
        private static RotationPlan none() {
            return new RotationPlan(0, 0);
        }

        public boolean hasRotation() {
            return xDegrees != 0 || yDegrees != 0;
        }
    }
}
