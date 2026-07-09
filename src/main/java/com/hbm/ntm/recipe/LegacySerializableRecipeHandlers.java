package com.hbm.ntm.recipe;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LegacySerializableRecipeHandlers {
    public static final String MANUAL_SOURCE = "<manual>";

    private static final List<Handler> HANDLERS = List.of(
            special("PressRecipes", "hbmPress.json", Category.MACHINE, "press",
                    "modern hbm_ntm_rebirth:press serializer covers the 48/48 source-backed default table with HbmIngredient provenance; LegacyPressRecipeImportProvider imports old hbmPress.json templates and LegacyPressRecipeExportProvider writes the old hbmPress.json shape for tooling/debug parity"),
            special("BlastFurnaceRecipes", "hbmBlastFurnaceLegacy.json", Category.BLAST, "difurnace",
                    "deprecated 1.7.10 BlastFurnaceRecipes is the active DiFurnace/RTG DiFurnace table; 13 non-LBSM defaults are materialized as hbm_ntm_rebirth:difurnace datapack recipes, old no-sink registerBlastFurnace compat emits difurnace JSON, and LegacyDiFurnaceRecipeImportProvider/ExportProvider read and write the old hbmBlastFurnaceLegacy.json input1/input2/output/dictframe/hidden shape for tooling/debug parity"),
            special("BlastFurnaceRecipesNT", "hbmBlastFurnace.json", Category.BLAST, "blast_furnace",
                    "modern hbm_ntm_rebirth:blast_furnace serializer exists for datapack recipes; 14 of 14 1.7.10 defaults are materialized, LegacyBlastFurnaceRecipeImportProvider imports old GenericRecipes inputItem/outputItem templates, and LegacyBlastFurnaceRecipeExportProvider writes the active old hbmBlastFurnace.json shape; deprecated hbmBlastFurnaceLegacy.json is tracked separately as difurnace"),
            special("ShredderRecipes", "hbmShredder.json", Category.MACHINE, "shredder",
                    "modern hbm_ntm_rebirth:shredder serializer exists for datapack recipes; 302 source-backed defaults/registerPost-derived tag recipes are materialized, LegacyShredderRecipeImportProvider imports old hbmShredder.json input/output ItemStack templates, and LegacyShredderRecipeExportProvider writes the old default-table hbmShredder.json shape while registerPost tag recipes stay outside that legacy file surface"),
            special("SolderingRecipes", "hbmSoldering.json", Category.MACHINE, "soldering_station",
                    "modern hbm_ntm_rebirth:soldering_station serializer exists for datapack recipes; 26 non-528/non-LBSM defaults are materialized, LegacySolderingRecipeImportProvider imports old hbmSoldering.json toppings/pcb/solder/fluid/output templates, and LegacySolderingRecipeExportProvider writes the old hbmSoldering.json shape for tooling/debug parity"),
            special("CombinationRecipes", "hbmCombination.json", Category.MACHINE, "combination_oven",
                    "modern hbm_ntm_rebirth:combination_oven serializer exists for datapack recipes; 23 reliable non-bedrock-ore defaults are materialized, LegacySpecialMachineRecipeImportProvider imports old hbmCombination.json input/output/fluid templates, and LegacySpecialMachineRecipeExportProvider writes the old hbmCombination.json shape while old bedrock ore loops stay excluded"),
            special("CentrifugeRecipes", "hbmCentrifuge.json", Category.MACHINE, "centrifuge",
                    "modern hbm_ntm_rebirth:centrifuge serializer exists for datapack recipes; 56 reliable 1.7.10 defaults are materialized, LegacyCentrifugeRecipeImportProvider imports old hbmCentrifuge.json AStack/input-output-array templates, and LegacyCentrifugeRecipeExportProvider writes the old hbmCentrifuge.json shape for tooling/debug parity while excluded LBSM/config variants, old/new bedrock ore loops, and Certus Quartz compat stay out"),
            special("CrystallizerRecipes", "hbmCrystallizer.json", Category.MACHINE, "crystallizer",
                    "modern hbm_ntm_rebirth:crystallizer serializer exists for item+fluid datapack recipes; 107 reliable 1.7.10 defaults are materialized, LegacySpecialMachineRecipeImportProvider imports old hbmCrystallizer.json input/fluid/output/productivity templates, and LegacySpecialMachineRecipeExportProvider writes the old hbmCrystallizer.json shape while old bedrock ore loops, current bedrock ore processing loop exclusions, and non-tag compat branches stay excluded"),
            special("RefineryRecipes", "hbmRefinery.json", Category.FLUID, "refinery",
                    "modern hbm_ntm_rebirth:refinery serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmRefinery.json shape for tooling/debug parity"),
            special("VacuumRefineryRecipes", "hbmVacRefinery.json", Category.FLUID, "vacuum_distill",
                    "modern hbm_ntm_rebirth:vacuum_distill serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmVacRefinery.json shape for tooling/debug parity"),
            special("FractionRecipes", "hbmFractions.json", Category.FLUID, "fraction_tower",
                    "modern hbm_ntm_rebirth:fraction_tower serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmFractions.json shape for tooling/debug parity"),
            special("CrackingRecipes", "hbmCracking.json", Category.FLUID, "catalytic_cracker",
                    "modern hbm_ntm_rebirth:catalytic_cracker serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmCracking.json shape for tooling/debug parity"),
            special("ReformingRecipes", "hbmReforming.json", Category.FLUID, "catalytic_reformer",
                    "modern hbm_ntm_rebirth:catalytic_reformer serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmReforming.json shape for tooling/debug parity"),
            special("HydrotreatingRecipes", "hbmHydrotreating.json", Category.FLUID, "hydrotreater",
                    "modern hbm_ntm_rebirth:hydrotreater serializer exists for oil-processing datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmHydrotreating.json shape for tooling/debug parity"),
            special("LiquefactionRecipes", "hbmLiquefactor.json", Category.FLUID, "liquefaction",
                    "modern hbm_ntm_rebirth:liquefaction serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmLiquefactor.json shape for tooling/debug parity"),
            special("SolidificationRecipes", "hbmSolidifier.json", Category.FLUID, "solidifier",
                    "modern hbm_ntm_rebirth:solidifier serializer exists for fluid-to-item recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmSolidifier.json shape for tooling/debug parity"),
            special("CokerRecipes", "hbmCoker.json", Category.FLUID, "coker",
                    "modern hbm_ntm_rebirth:coker serializer exists for coking item/byproduct recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmCoker.json shape for tooling/debug parity"),
            special("PyroOvenRecipes", "hbmPyrolysis.json", Category.FLUID, "pyro_oven",
                    "modern hbm_ntm_rebirth:pyro_oven serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmPyrolysis.json shape for tooling/debug parity"),
            special("BreederRecipes", "hbmBreeder.json", Category.NUCLEAR, "breeding_reactor",
                    "modern hbm_ntm_rebirth:breeding_reactor serializer covers breeder input/output/flux recipes; LegacyReactorRecipeImportProvider imports the custom legacy table and LegacyReactorRecipeExportProvider writes the old hbmBreeder.json shape for tooling/debug parity"),
            special("CyclotronRecipes", "hbmCyclotron.json", Category.NUCLEAR, "cyclotron",
                    "modern hbm_ntm_rebirth:cyclotron serializer covers particle/input/output/antimatter recipes; default table is materialized as datapack JSON, LegacySpecialMachineRecipeImportProvider imports old hbmCyclotron.json templates, and LegacySpecialMachineRecipeExportProvider writes the old hbmCyclotron.json shape for tooling/debug parity"),
            special("FuelPoolRecipes", "hbmFuelpool.json", Category.NUCLEAR, "fuel_pool",
                    "modern hbm_ntm_rebirth:fuel_pool serializer covers deterministic cooling recipes; LegacyReactorRecipeImportProvider imports the custom legacy table and LegacyReactorRecipeExportProvider writes the old hbmFuelpool.json shape for tooling/debug parity"),
            special("MixerRecipes", "hbmMixer.json", Category.FLUID, "mixer",
                    "modern hbm_ntm_rebirth:mixer serializer exists for datapack recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy grouped bulk table and LegacyFluidProcessingRecipeExportProvider writes the old grouped hbmMixer.json shape for tooling/debug parity"),
            special("OutgasserRecipes", "hbmIrradiation.json", Category.NUCLEAR, "outgasser",
                    "modern hbm_ntm_rebirth:outgasser serializer covers RBMK/Fusion Breeder irradiation recipes; LegacyReactorIrradiationImportProvider imports the custom legacy table and LegacyReactorIrradiationExportProvider writes the old hbmIrradiation.json shape for tooling/debug parity"),
            special("FluidBreederRecipes", "hbmIrradiationFluids.json", Category.NUCLEAR,
                    "fusion_fluid_breeder",
                    "modern hbm_ntm_rebirth:fusion_fluid_breeder serializer covers the legacy Fusion Breeder fluid irradiation table; LegacyFusionFluidBreederImportProvider imports the custom legacy fluid-array format and LegacyFusionFluidBreederExportProvider writes the old hbmIrradiationFluids.json shape for tooling/debug parity"),
            special("CompressorRecipes", "hbmCompressor.json", Category.MACHINE, "compressor",
                    "modern hbm_ntm_rebirth:compressor serializer exists for pressure fluid recipes; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmCompressor.json shape without duration for tooling/debug parity; 5 explicit defaults are materialized while the +1PU fallback remains runtime behavior"),
            special("ElectrolyserFluidRecipes", "hbmElectrolyzerFluid.json", Category.FLUID, "electrolyzer_fluid",
                    "modern hbm_ntm_rebirth:electrolyzer_fluid serializer exists; LegacyFluidProcessingRecipeImportProvider imports the dedicated legacy bulk table and LegacyFluidProcessingRecipeExportProvider writes the old hbmElectrolyzerFluid.json shape for tooling/debug parity"),
            special("ElectrolyserMetalRecipes", "hbmElectrolyzerMetal.json", Category.MACHINE, "electrolyzer_metal",
                    "modern hbm_ntm_rebirth:electrolyzer_metal serializer exists; LegacyFluidProcessingRecipeImportProvider imports the legacy bulk table while skipping the excluded bedrock ore loop, and LegacyFluidProcessingRecipeExportProvider writes the old hbmElectrolyzerMetal.json shape for tooling/debug parity"),
            special("ArcWelderRecipes", "hbmArcWelder.json", Category.MACHINE, "arc_welder",
                    "modern hbm_ntm_rebirth:arc_welder serializer exists for datapack recipes; 47/47 reliable defaults are materialized, LegacyArcWelderRecipeImportProvider imports old hbmArcWelder.json templates, and LegacyArcWelderRecipeExportProvider writes the old hbmArcWelder.json shape for tooling/debug parity"),
            special("RotaryFurnaceRecipes", "hbmRotaryFurnace.json", Category.MACHINE, "rotary_furnace",
                    "modern hbm_ntm_rebirth:rotary_furnace serializer exists for material-output datapack recipes; 12/12 reliable defaults are materialized, LegacyRotaryFurnaceRecipeImportProvider imports old hbmRotaryFurnace.json templates, and LegacyRotaryFurnaceRecipeExportProvider writes the old hbmRotaryFurnace.json shape for tooling/debug parity"),
            special("ExposureChamberRecipes", "hbmExposureChamber.json", Category.NUCLEAR, "exposure_chamber",
                    "modern hbm_ntm_rebirth:exposure_chamber serializer covers particle/ingredient/output recipes; LegacyReactorIrradiationImportProvider imports the custom legacy table and LegacyReactorIrradiationExportProvider writes the old hbmExposureChamber.json shape while old expensive-mode config remains datapack-deferred"),
            special("ParticleAcceleratorRecipes", "hbmParticleAccelerator.json", Category.NUCLEAR,
                    "particle_accelerator",
                    "modern hbm_ntm_rebirth:particle_accelerator serializer covers two inputs, momentum, one or two outputs, source order, and 11 materialized defaults; LegacyParticleAcceleratorRecipeImportProvider imports old hbmParticleAccelerator.json templates and skips malformed one-input entries, and LegacyParticleAcceleratorRecipeExportProvider writes the old hbmParticleAccelerator.json shape for tooling/debug parity"),
            special("AmmoPressRecipes", "hbmAmmoPress.json", Category.MACHINE, "ammo_press",
                    "modern hbm_ntm_rebirth:ammo_press serializer exists for datapack recipes; 89/89 defaults are materialized, LegacyAmmoPressRecipeImportProvider imports old hbmAmmoPress.json templates with fixed 9-slot input grids and legacy fluid-container dict support, and LegacyAmmoPressRecipeExportProvider writes the old hbmAmmoPress.json shape for tooling/debug parity"),
            special("AnvilRecipes", "hbmAnvil.json", Category.OTHER, "anvil_construction",
                    "modern hbm_ntm_rebirth:anvil_construction serializer covers old hbmAnvil.json constructionRecipes; LegacyAnvilRecipeImportProvider imports inputs/outputs/tier/overlay and LegacyAnvilRecipeExportProvider writes the old hbmAnvil.json shape while smithing defaults remain separate anvil_smithing datapack recipes"),
            special("PedestalRecipes", "hbmPedestal.json", Category.OTHER, "pedestal",
                    "modern hbm_ntm_rebirth:pedestal serializer/runtime/facade exists; 12 source-backed defaults are materialized, LegacyPedestalRecipeImportProvider imports old hbmPedestal.json nine-slot input/output/extra/set templates, and LegacyPedestalRecipeExportProvider writes the old hbmPedestal.json shape while the 5 item_secret red-room/dungeon reward-chain defaults are explicitly excluded"),
            special("AnnihilatorRecipes", "hbmAnnihilator.json", Category.OTHER, "annihilator",
                    "modern hbm_ntm_rebirth:annihilator serializer supports custom milestone recipes; LegacyAnnihilatorRecipeImportProvider imports old key/milestones templates and LegacyAnnihilatorRecipeExportProvider writes the old hbmAnnihilator.json shape for tooling/debug parity while 528 default milestones remain excluded"),
            special("CrucibleRecipes", "hbmCrucible.json", Category.MACHINE, "crucible",
                    "modern hbm_ntm_rebirth:crucible serializer covers old material-stack alloy recipes; 13/13 non-GT6 defaults are materialized, LegacyCrucibleRecipeImportProvider imports old hbmCrucible.json name/frequency/icon/input/output templates, and LegacyCrucibleRecipeExportProvider writes the old hbmCrucible.json shape for tooling/debug parity while GT6-only steel variants stay excluded"),
            generic(LegacyGenericRecipeHandlers.ASSEMBLY_MACHINE, "AssemblyMachineRecipes"),
            generic(LegacyGenericRecipeHandlers.CHEMICAL_PLANT, "ChemicalPlantRecipes"),
            generic(LegacyGenericRecipeHandlers.PUREX, "PUREXRecipes"),
            generic(LegacyGenericRecipeHandlers.FUSION, "FusionRecipes"),
            generic(LegacyGenericRecipeHandlers.PRECASS, "PrecAssRecipes"),
            generic(LegacyGenericRecipeHandlers.PLASMA_FORGE, "PlasmaForgeRecipes"),
            special("MatDistribution", "hbmCrucibleSmelting.json", Category.MATERIAL, "crucible_smelting",
                    "modern hbm_ntm_rebirth:crucible_smelting serializer covers MatDistribution fixed item/ore material distributions; LegacyCrucibleSmeltingRecipeImportProvider imports old hbmCrucibleSmelting.json templates, and LegacyCrucibleSmeltingRecipeExportProvider writes the old hbmCrucibleSmelting.json shape for tooling/debug parity while automatic material-shape smelting remains runtime fallback"),
            unsupported("CustomMachineRecipes", "hbmCustomMachines.json", Category.MACHINE,
                    "legacy 1.7.10 custom-machine recipe templates are part of the custom-machine feature family and are hard-excluded by project rule"),
            special("ArcFurnaceRecipes", "hbmArcFurnace.json", Category.MACHINE, "arc_furnace",
                    "modern hbm_ntm_rebirth:arc_furnace serializer/runtime covers solid outputs and arc material outputs; 12 reliable defaults are materialized, LegacyArcFurnaceRecipeImportProvider imports old input/solid/fluid templates, and LegacyArcFurnaceRecipeExportProvider writes the old hbmArcFurnace.json shape for tooling/debug parity"));

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
        int specialImporter = 0;
        int modernSerializer = 0;
        int unsupported = 0;
        for (Handler handler : HANDLERS) {
            switch (handler.importStatus()) {
                case SUPPORTED_GENERIC -> genericSupported++;
                case SUPPORTED_SPECIAL_IMPORT -> specialImporter++;
                case MODERN_SERIALIZER_ONLY -> modernSerializer++;
                case UNSUPPORTED -> unsupported++;
            }
        }
        return new Coverage(HANDLERS.size(), genericSupported, specialImporter, modernSerializer, unsupported);
    }

    private static Handler generic(LegacyGenericRecipeHandlers.Handler generic, String legacyClassName) {
        if (generic.supported()) {
            return new Handler(
                    legacyClassName,
                generic.legacyFileName(),
                Category.GENERIC,
                ImportStatus.SUPPORTED_GENERIC,
                generic.requireMachine().name().toLowerCase(Locale.ROOT),
                "handled by LegacyGenericRecipeImporter -> " + generic.outputFolder()
                        + "; LegacyGenericRecipeExportProvider exports current datapack recipes back to the old GenericRecipes JSON shape for tooling/debug parity");
        }
        return new Handler(
                legacyClassName,
                generic.legacyFileName(),
                Category.GENERIC,
                ImportStatus.UNSUPPORTED,
                "",
                generic.unsupportedReason().orElse("unsupported generic recipe handler"));
    }

    private static Handler modern(String legacyClassName, String legacyFileName, Category category,
            String modernRecipeType, String notes) {
        return new Handler(legacyClassName, legacyFileName, category, ImportStatus.MODERN_SERIALIZER_ONLY,
                modernRecipeType, notes);
    }

    private static Handler special(String legacyClassName, String legacyFileName, Category category,
            String modernRecipeType, String notes) {
        return new Handler(legacyClassName, legacyFileName, category, ImportStatus.SUPPORTED_SPECIAL_IMPORT,
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
        SUPPORTED_SPECIAL_IMPORT,
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

    public record Coverage(int totalHandlers, int genericSupported, int specialImporter,
                           int modernSerializerOnly, int unsupported) {
    }
}
