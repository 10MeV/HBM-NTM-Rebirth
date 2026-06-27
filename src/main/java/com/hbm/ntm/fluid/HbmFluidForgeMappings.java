package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class HbmFluidForgeMappings {
    private static final Map<FluidType, Fluid> TO_FORGE = new IdentityHashMap<>();
    private static final Map<Fluid, FluidType> FROM_FORGE = new IdentityHashMap<>();
    private static final List<TagAlias> TAG_ALIASES = new ArrayList<>();

    static {
        resetMappings();
        registerDefaultTagAliases();
    }

    public static void bootstrap() {
        bootstrap(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
    }

    public static void bootstrap(java.nio.file.Path configDir) {
        resetMappings();
        ModFluids.registerMappings();
        resetTagAliases();
        HbmFluidForgeAliasConfig.initialize(configDir);
    }

    public static void register(FluidType hbmType, Fluid forgeFluid) {
        if (hbmType == null || hbmType == HbmFluids.NONE || forgeFluid == null || forgeFluid == Fluids.EMPTY) {
            return;
        }
        Fluid previous = TO_FORGE.put(hbmType, forgeFluid);
        if (previous != null && previous != forgeFluid && FROM_FORGE.get(previous) == hbmType) {
            FROM_FORGE.remove(previous);
        }
        FROM_FORGE.put(forgeFluid, hbmType);
    }

    public static void registerImportAlias(Fluid forgeFluid, FluidType hbmType) {
        if (hbmType == null || hbmType == HbmFluids.NONE || forgeFluid == null || forgeFluid == Fluids.EMPTY) {
            return;
        }
        FROM_FORGE.put(forgeFluid, hbmType);
    }

    public static void unregister(FluidType hbmType) {
        Fluid forgeFluid = TO_FORGE.remove(hbmType);
        if (forgeFluid != null && FROM_FORGE.get(forgeFluid) == hbmType) {
            FROM_FORGE.remove(forgeFluid);
        }
    }

    public static void registerTagAlias(ResourceLocation tagId, FluidType hbmType) {
        if (tagId == null || hbmType == null || hbmType == HbmFluids.NONE) {
            return;
        }
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagId);
        TAG_ALIASES.removeIf(alias -> alias.tag().equals(tag));
        TAG_ALIASES.add(new TagAlias(tag, hbmType));
    }

    public static FluidType fromTagAlias(ResourceLocation tagId) {
        if (tagId == null) {
            return HbmFluids.NONE;
        }
        for (TagAlias alias : TAG_ALIASES) {
            if (alias.tag().location().equals(tagId)) {
                return alias.hbmType();
            }
        }
        return HbmFluids.NONE;
    }

    public static FluidType fromForge(FluidStack stack) {
        return inspectImportMapping(stack).hbmType();
    }

    public static FluidType fromForge(Fluid fluid) {
        return inspectImportMapping(fluid).hbmType();
    }

    public static ForgeImportMappingReport inspectImportMapping(FluidStack stack) {
        boolean stackPresent = stack != null && !stack.isEmpty();
        return inspectImportMapping(stackPresent ? stack.getFluid() : Fluids.EMPTY,
                stackPresent ? stack.getAmount() : 0, stackPresent);
    }

    public static ForgeImportMappingReport inspectImportMapping(Fluid fluid) {
        return inspectImportMapping(fluid, 0, fluid != null && fluid != Fluids.EMPTY);
    }

    private static ForgeImportMappingReport inspectImportMapping(Fluid fluid, int amount, boolean stackPresent) {
        if (fluid == null || fluid == Fluids.EMPTY || !stackPresent) {
            return ForgeImportMappingReport.empty(Math.max(0, amount));
        }
        FluidType direct = FROM_FORGE.get(fluid);
        if (direct != null) {
            return new ForgeImportMappingReport(true, forgeFluidId(fluid), Math.max(0, amount),
                    direct, MappingKind.DIRECT, null);
        }
        for (TagAlias alias : TAG_ALIASES) {
            if (fluid.builtInRegistryHolder().is(alias.tag())) {
                return new ForgeImportMappingReport(true, forgeFluidId(fluid), Math.max(0, amount),
                        alias.hbmType(), MappingKind.TAG_ALIAS, alias.tag().location());
            }
        }
        return new ForgeImportMappingReport(true, forgeFluidId(fluid), Math.max(0, amount),
                HbmFluids.NONE, MappingKind.NONE, null);
    }

    public static FluidType fromForgeExport(FluidStack stack) {
        return inspectForgeExportMapping(stack).hbmType();
    }

    public static FluidType fromForgeExport(Fluid fluid) {
        return inspectForgeExportMapping(fluid).hbmType();
    }

    public static ForgeExportRequestMappingReport inspectForgeExportMapping(FluidStack stack) {
        boolean stackPresent = stack != null && !stack.isEmpty();
        return inspectForgeExportMapping(stackPresent ? stack.getFluid() : Fluids.EMPTY,
                stackPresent ? stack.getAmount() : 0, stackPresent);
    }

    public static ForgeExportRequestMappingReport inspectForgeExportMapping(Fluid fluid) {
        return inspectForgeExportMapping(fluid, 0, fluid != null && fluid != Fluids.EMPTY);
    }

    private static ForgeExportRequestMappingReport inspectForgeExportMapping(Fluid fluid, int amount, boolean requestPresent) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return ForgeExportRequestMappingReport.empty(Math.max(0, amount));
        }
        for (Entry<FluidType, Fluid> entry : TO_FORGE.entrySet()) {
            if (entry.getValue() == fluid) {
                return new ForgeExportRequestMappingReport(requestPresent, forgeFluidId(fluid),
                        Math.max(0, amount), entry.getKey(), MappingKind.DIRECT);
            }
        }
        return new ForgeExportRequestMappingReport(requestPresent, forgeFluidId(fluid),
                Math.max(0, amount), HbmFluids.NONE, MappingKind.NONE);
    }

    public static FluidStack toForge(FluidType type, int amount) {
        return inspectHbmExportMapping(type, amount).forgeStack();
    }

    public static HbmExportMappingReport inspectHbmExportMapping(FluidType type, int amount) {
        FluidType normalized = type == null ? HbmFluids.NONE : type;
        int normalizedAmount = Math.max(0, amount);
        Fluid fluid = TO_FORGE.get(normalized);
        if (fluid == null || normalized == HbmFluids.NONE || normalizedAmount <= 0) {
            return new HbmExportMappingReport(normalized, normalizedAmount, false, null, FluidStack.EMPTY);
        }
        return new HbmExportMappingReport(normalized, normalizedAmount, true,
                forgeFluidId(fluid), new FluidStack(fluid, normalizedAmount));
    }

    public static boolean canExport(FluidType type) {
        return TO_FORGE.containsKey(type);
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(TO_FORGE.size(), FROM_FORGE.size(), TAG_ALIASES.size());
    }

    private static void resetTagAliases() {
        TAG_ALIASES.clear();
        registerDefaultTagAliases();
    }

    private static void resetMappings() {
        TO_FORGE.clear();
        FROM_FORGE.clear();
        register(HbmFluids.WATER, Fluids.WATER);
        register(HbmFluids.LAVA, Fluids.LAVA);
    }

    private static void registerDefaultTagAliases() {
        tagAlias("water", HbmFluids.WATER);
        tagAlias("lava", HbmFluids.LAVA);
        tagAlias("steam", HbmFluids.STEAM);
        tagAlias("hot_steam", HbmFluids.HOTSTEAM);
        tagAlias("superheated_steam", HbmFluids.SUPERHOTSTEAM);
        tagAlias("super_hot_steam", HbmFluids.SUPERHOTSTEAM);
        tagAlias("crude_oil", HbmFluids.OIL);
        tagAlias("oil", HbmFluids.OIL);
        tagAlias("heavy_oil", HbmFluids.HEAVYOIL);
        tagAlias("diesel", HbmFluids.DIESEL);
        tagAlias("kerosene", HbmFluids.KEROSENE);
        tagAlias("gasoline", HbmFluids.GASOLINE);
        tagAlias("sulfuric_acid", HbmFluids.SULFURIC_ACID);
        tagAlias("nitric_acid", HbmFluids.NITRIC_ACID);
        tagAlias("hydrogen", HbmFluids.HYDROGEN);
        tagAlias("deuterium", HbmFluids.DEUTERIUM);
        tagAlias("tritium", HbmFluids.TRITIUM);
        tagAlias("oxygen", HbmFluids.OXYGEN);
        tagAlias("chlorine", HbmFluids.CHLORINE);
        tagAlias("carbon_dioxide", HbmFluids.CARBONDIOXIDE);
        tagAlias("heavy_water", HbmFluids.HEAVYWATER);
        tagAlias("ethanol", HbmFluids.ETHANOL);
        tagAlias("biofuel", HbmFluids.BIOFUEL);
        tagAlias("lubricant", HbmFluids.LUBRICANT);
        tagAlias("mercury", HbmFluids.MERCURY);
    }

    private static void tagAlias(String path, FluidType hbmType) {
        registerTagAlias(new ResourceLocation("forge", path), hbmType);
    }

    private static ResourceLocation forgeFluidId(Fluid fluid) {
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        return id == null ? new ResourceLocation("minecraft", "empty") : id;
    }

    private record TagAlias(TagKey<Fluid> tag, FluidType hbmType) {
    }

    public enum MappingKind {
        EMPTY,
        DIRECT,
        TAG_ALIAS,
        NONE
    }

    public record ForgeImportMappingReport(
            boolean stackPresent,
            ResourceLocation forgeFluidId,
            int amountMb,
            FluidType hbmType,
            MappingKind mappingKind,
            ResourceLocation matchedTag) {
        public ForgeImportMappingReport {
            forgeFluidId = forgeFluidId == null ? new ResourceLocation("minecraft", "empty") : forgeFluidId;
            amountMb = Math.max(0, amountMb);
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            mappingKind = mappingKind == null ? MappingKind.NONE : mappingKind;
        }

        private static ForgeImportMappingReport empty(int amount) {
            return new ForgeImportMappingReport(false, new ResourceLocation("minecraft", "empty"),
                    amount, HbmFluids.NONE, MappingKind.EMPTY, null);
        }

        public boolean mapped() {
            return hbmType != HbmFluids.NONE;
        }
    }

    public record ForgeExportRequestMappingReport(
            boolean requestPresent,
            ResourceLocation forgeFluidId,
            int amountMb,
            FluidType hbmType,
            MappingKind mappingKind) {
        public ForgeExportRequestMappingReport {
            forgeFluidId = forgeFluidId == null ? new ResourceLocation("minecraft", "empty") : forgeFluidId;
            amountMb = Math.max(0, amountMb);
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            mappingKind = mappingKind == null ? MappingKind.NONE : mappingKind;
        }

        private static ForgeExportRequestMappingReport empty(int amount) {
            return new ForgeExportRequestMappingReport(false, new ResourceLocation("minecraft", "empty"),
                    amount, HbmFluids.NONE, MappingKind.EMPTY);
        }

        public boolean mapped() {
            return hbmType != HbmFluids.NONE;
        }
    }

    public record HbmExportMappingReport(
            FluidType hbmType,
            int amountMb,
            boolean exportMapped,
            ResourceLocation forgeFluidId,
            FluidStack forgeStack) {
        public HbmExportMappingReport {
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            amountMb = Math.max(0, amountMb);
            forgeStack = forgeStack == null ? FluidStack.EMPTY : forgeStack.copy();
        }
    }

    public record Diagnostics(int exportMappings, int importMappings, int tagAliases) {
        public String summary() {
            return "Forge fluid mappings export=" + exportMappings + " import=" + importMappings
                    + " tagAliases=" + tagAliases;
        }
    }

    private HbmFluidForgeMappings() {
    }
}
