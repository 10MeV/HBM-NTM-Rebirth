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
            unsupported("SolderingRecipes", "hbmSoldering.json", Category.MACHINE,
                    "soldering station uses a custom table format and legacy template inputs"),
            unsupported("CombinationRecipes", "hbmCombination.json", Category.MACHINE,
                    "combination oven uses a dedicated recipe list and custom IO layout"),
            modern("CentrifugeRecipes", "hbmCentrifuge.json", Category.MACHINE, "centrifuge",
                    "modern hbm_ntm_rebirth:centrifuge serializer exists for datapack recipes; legacy bulk importer is still separate work"),
            modern("CrystallizerRecipes", "hbmCrystallizer.json", Category.MACHINE, "crystallizer",
                    "modern hbm_ntm_rebirth:crystallizer serializer exists for peroxide item recipes; legacy bulk importer is still separate work"),
            unsupported("RefineryRecipes", "hbmRefinery.json", Category.FLUID,
                    "refinery format is fluid-chain specific and not a GenericRecipe"),
            unsupported("VacuumRefineryRecipes", "hbmVacRefinery.json", Category.FLUID,
                    "vacuum refinery format is fluid-chain specific and not a GenericRecipe"),
            unsupported("FractionRecipes", "hbmFractions.json", Category.FLUID,
                    "fraction tower has dedicated multi-fluid fractioning semantics"),
            unsupported("CrackingRecipes", "hbmCracking.json", Category.FLUID,
                    "cracking recipe format belongs to the oil/fluid processing library"),
            unsupported("ReformingRecipes", "hbmReforming.json", Category.FLUID,
                    "reforming recipe format belongs to the oil/fluid processing library"),
            unsupported("HydrotreatingRecipes", "hbmHydrotreating.json", Category.FLUID,
                    "hydrotreating recipe format belongs to the oil/fluid processing library"),
            modern("LiquefactionRecipes", "hbmLiquefactor.json", Category.FLUID, "liquefaction",
                    "modern hbm_ntm_rebirth:liquefaction serializer exists; legacy bulk importer is still separate work"),
            unsupported("SolidificationRecipes", "hbmSolidifier.json", Category.FLUID,
                    "solidifier uses mold/fluid solidification logic and no modern serializer yet"),
            unsupported("CokerRecipes", "hbmCoker.json", Category.FLUID,
                    "coker uses oil/fuel conversion semantics and no modern serializer yet"),
            modern("PyroOvenRecipes", "hbmPyrolysis.json", Category.FLUID, "pyro_oven",
                    "modern hbm_ntm_rebirth:pyro_oven serializer exists; legacy bulk importer is still separate work"),
            unsupported("BreederRecipes", "hbmBreeder.json", Category.NUCLEAR,
                    "breeder recipe format is nuclear fuel specific"),
            unsupported("CyclotronRecipes", "hbmCyclotron.json", Category.NUCLEAR,
                    "cyclotron recipe format is custom and not a GenericRecipe"),
            unsupported("FuelPoolRecipes", "hbmFuelpool.json", Category.NUCLEAR,
                    "fuel pool recipe format is nuclear fuel specific"),
            unsupported("MixerRecipes", "hbmMixer.json", Category.FLUID,
                    "mixer has custom fluid/item mixing semantics"),
            unsupported("OutgasserRecipes", "hbmIrradiation.json", Category.NUCLEAR,
                    "outgasser/irradiation recipe format is radiation processing specific"),
            unsupported("FluidBreederRecipes", "hbmIrradiationFluids.json", Category.NUCLEAR,
                    "fluid irradiation recipe format is radiation processing specific"),
            unsupported("CompressorRecipes", "hbmCompressor.json", Category.MACHINE,
                    "compressor has a dedicated legacy format and no modern serializer yet"),
            unsupported("ElectrolyserFluidRecipes", "hbmElectrolyzerFluid.json", Category.FLUID,
                    "fluid electrolyzer uses dedicated fluid conversion format"),
            unsupported("ElectrolyserMetalRecipes", "hbmElectrolyzerMetal.json", Category.MACHINE,
                    "metal electrolyzer uses custom item/fluid outputs"),
            modern("ArcWelderRecipes", "hbmArcWelder.json", Category.MACHINE, "arc_welder",
                    "modern hbm_ntm_rebirth:arc_welder serializer exists for datapack recipes; legacy bulk importer is still separate work"),
            unsupported("RotaryFurnaceRecipes", "hbmRotaryFurnace.json", Category.MACHINE,
                    "rotary furnace uses dedicated furnace logic"),
            unsupported("ExposureChamberRecipes", "hbmExposureChamber.json", Category.NUCLEAR,
                    "exposure chamber uses radiation/exposure-specific recipe data"),
            unsupported("ParticleAcceleratorRecipes", "hbmParticleAccelerator.json", Category.NUCLEAR,
                    "particle accelerator has special beam/focus recipe data"),
            unsupported("AmmoPressRecipes", "hbmAmmoPress.json", Category.MACHINE,
                    "ammo press uses ammunition-specific metadata and assembly rules"),
            unsupported("AnvilRecipes", "hbmAnvil.json", Category.OTHER,
                    "anvil recipe format is custom and outside GenericRecipe"),
            unsupported("PedestalRecipes", "hbmPedestal.json", Category.OTHER,
                    "pedestal recipe format is custom and outside GenericRecipe"),
            unsupported("AnnihilatorRecipes", "hbmAnnihilator.json", Category.OTHER,
                    "annihilator recipe format is custom and outside GenericRecipe"),
            generic(LegacyGenericRecipeHandlers.CRUCIBLE, "CrucibleRecipes"),
            generic(LegacyGenericRecipeHandlers.ASSEMBLY_MACHINE, "AssemblyMachineRecipes"),
            generic(LegacyGenericRecipeHandlers.CHEMICAL_PLANT, "ChemicalPlantRecipes"),
            generic(LegacyGenericRecipeHandlers.PUREX, "PUREXRecipes"),
            generic(LegacyGenericRecipeHandlers.FUSION, "FusionRecipes"),
            generic(LegacyGenericRecipeHandlers.PRECASS, "PrecAssRecipes"),
            generic(LegacyGenericRecipeHandlers.PLASMA_FORGE, "PlasmaForgeRecipes"),
            unsupported("MatDistribution", "hbmCrucibleSmelting.json", Category.MATERIAL,
                    "crucible smelting distribution is material-stack based and belongs to the crucible/material library"),
            unsupported("CustomMachineRecipes", "hbmCustomMachines.json", Category.MACHINE,
                    "custom machines are dynamic legacy machine definitions and need a separate runtime"),
            unsupported("ArcFurnaceRecipes", "hbmArcFurnace.json", Category.MACHINE,
                    "arc furnace uses custom furnace and byproduct semantics"));

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
