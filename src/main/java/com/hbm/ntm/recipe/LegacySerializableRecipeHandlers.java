package com.hbm.ntm.recipe;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LegacySerializableRecipeHandlers {
    public static final String MANUAL_SOURCE = "<manual>";

    private static final List<Handler> HANDLERS = List.of(
            press("PressRecipes", "hbmPress.json", "modern hbm_ntm_rebirth:press covers a safe subset; full legacy import still needs stamp/meta coverage"),
            unsupported("BlastFurnaceRecipes", "hbmBlastFurnaceLegacy.json", Category.BLAST,
                    "legacy blast furnace has a deprecated dedicated map format; 5714 hbmBlastFurnace.json belongs to BlastFurnaceRecipesNT"),
            modern("ShredderRecipes", "hbmShredder.json", Category.MACHINE, "shredder",
                    "modern hbm_ntm_rebirth:shredder serializer exists for datapack recipes; legacy bulk importer is still separate work"),
            modern("SolderingRecipes", "hbmSoldering.json", Category.MACHINE, "soldering_station",
                    "modern hbm_ntm_rebirth:soldering_station serializer exists for datapack recipes; legacy bulk importer still needs custom table/input coverage"),
            modern("CombinationRecipes", "hbmCombination.json", Category.MACHINE, "combination_oven",
                    "modern hbm_ntm_rebirth:combination_oven serializer exists for datapack recipes; legacy bulk importer still needs custom table/input coverage"),
            modern("CentrifugeRecipes", "hbmCentrifuge.json", Category.MACHINE, "centrifuge",
                    "modern hbm_ntm_rebirth:centrifuge serializer exists for datapack recipes; legacy bulk importer is still separate work"),
            modern("CrystallizerRecipes", "hbmCrystallizer.json", Category.MACHINE, "crystallizer",
                    "modern hbm_ntm_rebirth:crystallizer serializer exists for peroxide item recipes; legacy bulk importer is still separate work"),
            modern("RefineryRecipes", "hbmRefinery.json", Category.FLUID, "refinery",
                    "modern hbm_ntm_rebirth:refinery serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("VacuumRefineryRecipes", "hbmVacRefinery.json", Category.FLUID, "vacuum_distill",
                    "modern hbm_ntm_rebirth:vacuum_distill serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("FractionRecipes", "hbmFractions.json", Category.FLUID, "fraction_tower",
                    "modern hbm_ntm_rebirth:fraction_tower serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("CrackingRecipes", "hbmCracking.json", Category.FLUID, "catalytic_cracker",
                    "modern hbm_ntm_rebirth:catalytic_cracker serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("ReformingRecipes", "hbmReforming.json", Category.FLUID, "catalytic_reformer",
                    "modern hbm_ntm_rebirth:catalytic_reformer serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("HydrotreatingRecipes", "hbmHydrotreating.json", Category.FLUID, "hydrotreater",
                    "modern hbm_ntm_rebirth:hydrotreater serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("LiquefactionRecipes", "hbmLiquefactor.json", Category.FLUID, "liquefaction",
                    "modern hbm_ntm_rebirth:liquefaction serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("SolidificationRecipes", "hbmSolidifier.json", Category.FLUID, "solidifier",
                    "modern hbm_ntm_rebirth:solidifier serializer exists for fluid-to-item recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("CokerRecipes", "hbmCoker.json", Category.FLUID, "coker",
                    "modern hbm_ntm_rebirth:coker serializer exists for coking item/byproduct recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("PyroOvenRecipes", "hbmPyrolysis.json", Category.FLUID, "pyro_oven",
                    "modern hbm_ntm_rebirth:pyro_oven serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("BreederRecipes", "hbmBreeder.json", Category.NUCLEAR, "breeding_reactor",
                    "modern hbm_ntm_rebirth:breeding_reactor serializer covers breeder input/output/flux recipes; LegacyReactorRecipeImportProvider imports the custom legacy table"),
            unsupported("CyclotronRecipes", "hbmCyclotron.json", Category.NUCLEAR,
                    "cyclotron recipe format is custom and not a GenericRecipe"),
            modern("FuelPoolRecipes", "hbmFuelpool.json", Category.NUCLEAR, "fuel_pool",
                    "modern hbm_ntm_rebirth:fuel_pool serializer covers deterministic cooling recipes; LegacyReactorRecipeImportProvider imports the custom legacy table"),
            modern("MixerRecipes", "hbmMixer.json", Category.FLUID, "mixer",
                    "modern hbm_ntm_rebirth:mixer serializer exists for datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy grouped bulk table"),
            modern("OutgasserRecipes", "hbmIrradiation.json", Category.NUCLEAR, "outgasser",
                    "modern hbm_ntm_rebirth:outgasser serializer covers RBMK/Fusion Breeder irradiation recipes; LegacyReactorIrradiationImportProvider imports the custom legacy table"),
            modern("FluidBreederRecipes", "hbmIrradiationFluids.json", Category.NUCLEAR,
                    "fusion_fluid_breeder",
                    "modern hbm_ntm_rebirth:fusion_fluid_breeder serializer covers the legacy Fusion Breeder fluid irradiation table; LegacyFusionFluidBreederImportProvider imports the custom legacy fluid-array format"),
            modern("CompressorRecipes", "hbmCompressor.json", Category.MACHINE, "compressor",
                    "modern hbm_ntm_rebirth:compressor serializer exists for pressure fluid recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table"),
            modern("ElectrolyserFluidRecipes", "hbmElectrolyzerFluid.json", Category.FLUID, "electrolyzer_fluid",
                    "modern hbm_ntm_rebirth:electrolyzer_fluid serializer exists; LegacyFluidProcessingRecipeImportProvider imports the dedicated legacy bulk table"),
            modern("ElectrolyserMetalRecipes", "hbmElectrolyzerMetal.json", Category.MACHINE, "electrolyzer_metal",
                    "modern hbm_ntm_rebirth:electrolyzer_metal serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table while skipping the excluded bedrock ore loop"),
            modern("ArcWelderRecipes", "hbmArcWelder.json", Category.MACHINE, "arc_welder",
                    "modern hbm_ntm_rebirth:arc_welder serializer exists for datapack recipes; legacy bulk importer is still separate work"),
            unsupported("RotaryFurnaceRecipes", "hbmRotaryFurnace.json", Category.MACHINE,
                    "rotary furnace uses dedicated furnace logic"),
            modern("ExposureChamberRecipes", "hbmExposureChamber.json", Category.NUCLEAR, "exposure_chamber",
                    "modern hbm_ntm_rebirth:exposure_chamber serializer covers particle/ingredient/output recipes; LegacyReactorIrradiationImportProvider imports the custom legacy table while old expensive-mode config remains datapack-deferred"),
            unsupported("ParticleAcceleratorRecipes", "hbmParticleAccelerator.json", Category.NUCLEAR,
                    "particle accelerator has special beam/focus recipe data"),
            modern("AmmoPressRecipes", "hbmAmmoPress.json", Category.MACHINE, "ammo_press",
                    "modern hbm_ntm_rebirth:ammo_press serializer exists for datapack recipes; legacy bulk importer still needs full ammunition metadata coverage"),
            unsupported("AnvilRecipes", "hbmAnvil.json", Category.OTHER,
                    "anvil recipe format is custom and outside GenericRecipe"),
            unsupported("PedestalRecipes", "hbmPedestal.json", Category.OTHER,
                    "pedestal recipe format is custom and outside GenericRecipe"),
            modern("AnnihilatorRecipes", "hbmAnnihilator.json", Category.OTHER, "annihilator",
                    "modern hbm_ntm_rebirth:annihilator serializer supports custom milestone recipes; 528 default milestones remain excluded"),
            generic(LegacyGenericRecipeHandlers.CRUCIBLE, "CrucibleRecipes"),
            generic(LegacyGenericRecipeHandlers.ASSEMBLY_MACHINE, "AssemblyMachineRecipes"),
            generic(LegacyGenericRecipeHandlers.CHEMICAL_PLANT, "ChemicalPlantRecipes"),
            generic(LegacyGenericRecipeHandlers.PUREX, "PUREXRecipes"),
            generic(LegacyGenericRecipeHandlers.FUSION, "FusionRecipes"),
            generic(LegacyGenericRecipeHandlers.PRECASS, "PrecAssRecipes"),
            generic(LegacyGenericRecipeHandlers.PLASMA_FORGE, "PlasmaForgeRecipes"),
            unsupported("MatDistribution", "hbmCrucibleSmelting.json", Category.MATERIAL,
                    "crucible smelting distribution is material-stack based and belongs to the crucible/material library"),
            modern("ArcFurnaceRecipes", "hbmArcFurnace.json", Category.MACHINE, "arc_furnace",
                    "modern hbm_ntm_rebirth:arc_furnace serializer/runtime exists for solid datapack recipes; full legacy liquid/material outputs remain deferred"));

    private LegacySerializableRecipeHandlers() {
    }

    public static List<Handler> all() {
        return HANDLERS;
    }

    public static List<Handler> supportedGeneric() {
        return HANDLERS.stream()
                .filter(handler -> handler.importStatus() == ImportStatus.SUPPORTED_GENERIC)
                .toList();
    }

    public static Optional<Handler> byFileName(String fileName) {
        String normalized = normalizeFileName(fileName);
        return HANDLERS.stream()
                .filter(handler -> normalizeFileName(handler.legacyFileName()).equals(normalized))
                .findFirst();
    }

    public static Optional<Handler> byLegacyClassName(String legacyClassName) {
        return HANDLERS.stream()
                .filter(handler -> handler.legacyClassName().equals(legacyClassName))
                .findFirst();
    }

    public static Handler requireSupportedGeneric(String fileName) {
        Handler handler = byFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown legacy serializable recipe file: " + fileName));
        if (handler.importStatus() != ImportStatus.SUPPORTED_GENERIC) {
            throw new IllegalArgumentException("Legacy recipe file is not supported by the generic importer: "
                    + handler.legacyFileName() + " (" + handler.notes() + ")");
        }
        return handler;
    }

    public static Coverage coverage() {
        int genericSupported = 0;
        int modernSerializer = 0;
        int unsupported = 0;
        for (Handler handler : HANDLERS) {
            switch (handler.importStatus()) {
                case SUPPORTED_GENERIC -> genericSupported++;
                case MODERN_SERIALIZER_ONLY -> modernSerializer++;
                case UNSUPPORTED -> unsupported++;
            }
        }
        return new Coverage(HANDLERS.size(), genericSupported, modernSerializer, unsupported);
    }

    private static Handler generic(LegacyGenericRecipeHandlers.Handler generic, String legacyClassName) {
        if (generic.supported()) {
            return new Handler(
                    legacyClassName,
                    generic.legacyFileName(),
                    Category.GENERIC,
                    ImportStatus.SUPPORTED_GENERIC,
                    generic.requireMachine().name().toLowerCase(Locale.ROOT),
                    "handled by LegacyGenericRecipeImporter -> " + generic.outputFolder());
        }
        return new Handler(
                legacyClassName,
                generic.legacyFileName(),
                Category.GENERIC,
                ImportStatus.UNSUPPORTED,
                "",
                generic.unsupportedReason().orElse("unsupported generic recipe handler"));
    }

    private static Handler press(String legacyClassName, String legacyFileName, String notes) {
        return modern(legacyClassName, legacyFileName, Category.MACHINE, "press", notes);
    }

    private static Handler modern(String legacyClassName, String legacyFileName, Category category,
            String modernRecipeType, String notes) {
        return new Handler(legacyClassName, legacyFileName, category, ImportStatus.MODERN_SERIALIZER_ONLY,
                modernRecipeType, notes);
    }

    private static Handler unsupported(String legacyClassName, String legacyFileName, Category category, String reason) {
        return new Handler(legacyClassName, legacyFileName, category, ImportStatus.UNSUPPORTED, "", reason);
    }

    private static String normalizeFileName(String fileName) {
        String normalized = fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public enum Category {
        MACHINE,
        FLUID,
        NUCLEAR,
        BLAST,
        GENERIC,
        MATERIAL,
        OTHER
    }

    public enum ImportStatus {
        SUPPORTED_GENERIC,
        MODERN_SERIALIZER_ONLY,
        UNSUPPORTED
    }

    public record Handler(String legacyClassName, String legacyFileName, Category category,
                          ImportStatus importStatus, String modernRecipeType, String notes) {
        public Handler {
            if (legacyClassName == null || legacyClassName.isBlank()) {
                throw new IllegalArgumentException("Legacy serializable recipe handler needs a class name");
            }
            if (legacyFileName == null || legacyFileName.isBlank()) {
                throw new IllegalArgumentException("Legacy serializable recipe handler needs a file name");
            }
            if (category == null) {
                throw new IllegalArgumentException("Legacy serializable recipe handler needs a category: " + legacyClassName);
            }
            if (importStatus == null) {
                throw new IllegalArgumentException("Legacy serializable recipe handler needs an import status: " + legacyClassName);
            }
            modernRecipeType = modernRecipeType == null ? "" : modernRecipeType;
            notes = notes == null ? "" : notes;
        }

        public boolean supportedByGenericImporter() {
            return importStatus == ImportStatus.SUPPORTED_GENERIC;
        }

        public String commandSummary() {
            String target = modernRecipeType.isBlank() ? "" : " -> " + modernRecipeType;
            return legacyFileName + " [" + legacyClassName + "] " + category + " " + importStatus + target
                    + (notes.isBlank() ? "" : " (" + notes + ")");
        }
    }

    public record Coverage(int totalHandlers, int genericSupported, int modernSerializerOnly, int unsupported) {
    }
}
