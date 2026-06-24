package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LegacyGenericRecipeHandlers {
    public static final Handler ASSEMBLY_MACHINE = supported(
            "hbmAssemblyMachine.json",
            GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
            "assembly_machine");
    public static final Handler CHEMICAL_PLANT = supported(
            "hbmChemicalPlant.json",
            GenericMachineRecipe.Machine.CHEMICAL_PLANT,
            "chemical_plant");
    public static final Handler PUREX = supported(
            "hbmPUREX.json",
            GenericMachineRecipe.Machine.PUREX,
            "purex");
    public static final Handler PRECASS = supported(
            "hbmPrecisionAssembly.json",
            GenericMachineRecipe.Machine.PRECASS,
            "precass");

    public static final Handler PLASMA_FORGE = supported(
            "hbmPlasmaForge.json",
            GenericMachineRecipe.Machine.PLASMA_FORGE,
            "plasma_forge");
    public static final Handler FUSION = supported(
            "hbmFusion.json",
            GenericMachineRecipe.Machine.FUSION_REACTOR,
            "fusion_reactor");
    public static final Handler CRUCIBLE = unsupported(
            "hbmCrucible.json",
            "uses material-stack crucible format, mold recipes, and smelting helpers instead of GenericRecipe fields");

    private static final List<Handler> HANDLERS = List.of(
            ASSEMBLY_MACHINE,
            CHEMICAL_PLANT,
            PUREX,
            PRECASS,
            PLASMA_FORGE,
            FUSION,
            CRUCIBLE);

    private LegacyGenericRecipeHandlers() {
    }

    public static List<Handler> all() {
        return HANDLERS;
    }

    public static List<Handler> supported() {
        return HANDLERS.stream()
                .filter(Handler::supported)
                .toList();
    }

    public static Optional<Handler> byFileName(String fileName) {
        String normalized = normalizeFileName(fileName);
        return HANDLERS.stream()
                .filter(handler -> normalizeFileName(handler.legacyFileName()).equals(normalized))
                .findFirst();
    }

    public static Handler requireSupported(String fileName) {
        Handler handler = byFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown legacy generic recipe file: " + fileName
                        + ". Supported: " + supportedFileNames()));
        if (!handler.supported()) {
            throw new IllegalArgumentException("Legacy generic recipe file is not supported yet: "
                    + handler.legacyFileName() + " (" + handler.unsupportedReason().orElse("no reason recorded") + ")");
        }
        return handler;
    }

    public static String supportedFileNames() {
        return String.join(", ", supported().stream().map(Handler::legacyFileName).toList());
    }

    private static Handler supported(String legacyFileName, GenericMachineRecipe.Machine machine, String folder) {
        return new Handler(legacyFileName, Optional.of(machine), new ResourceLocation(HbmNtm.MOD_ID, folder), Optional.empty());
    }

    private static Handler unsupported(String legacyFileName, String reason) {
        return new Handler(legacyFileName, Optional.empty(), null, Optional.of(reason));
    }

    private static String normalizeFileName(String fileName) {
        String normalized = fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public record Handler(String legacyFileName,
                          Optional<GenericMachineRecipe.Machine> machine,
                          ResourceLocation outputFolder,
                          Optional<String> unsupportedReason) {
        public Handler {
            machine = machine == null ? Optional.empty() : machine;
            unsupportedReason = unsupportedReason == null ? Optional.empty() : unsupportedReason;
            if (machine.isPresent() && outputFolder == null) {
                throw new IllegalArgumentException("Supported legacy generic recipe handler needs an output folder: "
                        + legacyFileName);
            }
            if (machine.isEmpty() && unsupportedReason.isEmpty()) {
                throw new IllegalArgumentException("Unsupported legacy generic recipe handler needs a reason: "
                        + legacyFileName);
            }
        }

        public boolean supported() {
            return machine.isPresent();
        }

        public GenericMachineRecipe.Machine requireMachine() {
            return machine.orElseThrow(() -> new IllegalStateException("Unsupported legacy generic recipe handler: "
                    + legacyFileName));
        }

        public String commandSummary() {
            if (supported()) {
                return legacyFileName + " -> " + requireMachine().name().toLowerCase(Locale.ROOT)
                        + " folder=" + outputFolder;
            }
            return legacyFileName + " unsupported: " + unsupportedReason.orElse("");
        }
    }
}
