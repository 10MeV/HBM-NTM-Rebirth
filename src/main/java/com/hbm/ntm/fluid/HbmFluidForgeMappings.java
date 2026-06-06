package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public final class HbmFluidForgeMappings {
    private static final Map<FluidType, Fluid> TO_FORGE = new IdentityHashMap<>();
    private static final Map<Fluid, FluidType> FROM_FORGE = new IdentityHashMap<>();
    private static final List<TagAlias> TAG_ALIASES = new ArrayList<>();

    static {
        register(HbmFluids.WATER, Fluids.WATER);
        register(HbmFluids.LAVA, Fluids.LAVA);
        registerDefaultTagAliases();
    }

    public static void bootstrap() {
        ModFluids.registerMappings();
    }

    public static void register(FluidType hbmType, Fluid forgeFluid) {
        if (hbmType == null || hbmType == HbmFluids.NONE || forgeFluid == null || forgeFluid == Fluids.EMPTY) {
            return;
        }
        TO_FORGE.put(hbmType, forgeFluid);
        FROM_FORGE.put(forgeFluid, hbmType);
    }

    public static void registerTagAlias(ResourceLocation tagId, FluidType hbmType) {
        if (tagId == null || hbmType == null || hbmType == HbmFluids.NONE) {
            return;
        }
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagId);
        for (TagAlias alias : TAG_ALIASES) {
            if (alias.tag().equals(tag) && alias.hbmType() == hbmType) {
                return;
            }
        }
        TAG_ALIASES.add(new TagAlias(tag, hbmType));
    }

    public static FluidType fromForge(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HbmFluids.NONE;
        }
        return fromForge(stack.getFluid());
    }

    public static FluidType fromForge(Fluid fluid) {
        FluidType direct = FROM_FORGE.get(fluid);
        if (direct != null) {
            return direct;
        }
        if (fluid == null || fluid == Fluids.EMPTY) {
            return HbmFluids.NONE;
        }
        for (TagAlias alias : TAG_ALIASES) {
            if (fluid.builtInRegistryHolder().is(alias.tag())) {
                return alias.hbmType();
            }
        }
        return HbmFluids.NONE;
    }

    public static FluidStack toForge(FluidType type, int amount) {
        Fluid fluid = TO_FORGE.get(type);
        if (fluid == null || amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    public static boolean canExport(FluidType type) {
        return TO_FORGE.containsKey(type);
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

    private record TagAlias(TagKey<Fluid> tag, FluidType hbmType) {
    }

    private HbmFluidForgeMappings() {
    }
}
