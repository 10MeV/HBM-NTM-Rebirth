package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmForgeFluidType;
import com.hbm.ntm.fluid.LegacyWorldForgeFluidType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, HbmNtm.MOD_ID);
    public static final DeferredRegister<net.minecraftforge.fluids.FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, HbmNtm.MOD_ID);

    private static final Map<FluidType, HbmFluidRegistryEntry> ENTRIES = new LinkedHashMap<>();

    public static final HbmFluidRegistryEntry STEAM = register(HbmFluids.STEAM);
    public static final HbmFluidRegistryEntry HOTSTEAM = register(HbmFluids.HOTSTEAM);
    public static final HbmFluidRegistryEntry SUPERHOTSTEAM = register(HbmFluids.SUPERHOTSTEAM);
    public static final HbmFluidRegistryEntry COOLANT = register(HbmFluids.COOLANT);
    public static final HbmFluidRegistryEntry COOLANT_HOT = register(HbmFluids.COOLANT_HOT);
    public static final HbmFluidRegistryEntry OIL = register(HbmFluids.OIL);
    public static final HbmFluidRegistryEntry GAS = register(HbmFluids.GAS);
    public static final HbmFluidRegistryEntry PEROXIDE = register(HbmFluids.PEROXIDE);
    public static final HbmFluidRegistryEntry SCHRABIDIC = register(HbmFluids.SCHRABIDIC);
    public static final HbmFluidRegistryEntry SULFURIC_ACID = register(HbmFluids.SULFURIC_ACID);
    public static final HbmFluidRegistryEntry HYDROGEN = register(HbmFluids.HYDROGEN);
    public static final HbmFluidRegistryEntry DEUTERIUM = register(HbmFluids.DEUTERIUM);
    public static final HbmFluidRegistryEntry TRITIUM = register(HbmFluids.TRITIUM);
    private static final ForgeFlowingFluid.Properties[] MUD_PROPERTIES = new ForgeFlowingFluid.Properties[1];
    public static final RegistryObject<LegacyWorldForgeFluidType> MUD_FLUID_TYPE = FLUID_TYPES.register(
            "mud_fluid",
            () -> new LegacyWorldForgeFluidType(net.minecraftforge.fluids.FluidType.Properties.create()
                    .descriptionId("fluid." + HbmNtm.MOD_ID + ".mud_fluid")
                    .density(2500)
                    .viscosity(3000)
                    .lightLevel(5)
                    .temperature(2773),
                    texture("block/mud_still"), texture("block/mud_flowing")));
    public static final RegistryObject<ForgeFlowingFluid.Source> MUD_FLUID =
            FLUIDS.register("mud_fluid", () -> new ForgeFlowingFluid.Source(MUD_PROPERTIES[0]));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> MUD_FLUID_FLOWING =
            FLUIDS.register("mud_fluid_flowing", () -> new ForgeFlowingFluid.Flowing(MUD_PROPERTIES[0]));
    private static final ForgeFlowingFluid.Properties[] TOXIC_PROPERTIES = new ForgeFlowingFluid.Properties[1];
    public static final RegistryObject<LegacyWorldForgeFluidType> TOXIC_FLUID_TYPE = FLUID_TYPES.register(
            "toxic_fluid",
            () -> new LegacyWorldForgeFluidType(net.minecraftforge.fluids.FluidType.Properties.create()
                    .descriptionId("fluid." + HbmNtm.MOD_ID + ".toxic_fluid")
                    .density(2500)
                    .viscosity(2000)
                    .lightLevel(15)
                    .temperature(2773),
                    texture("block/toxic_still"), texture("block/toxic_flowing")));
    public static final RegistryObject<ForgeFlowingFluid.Source> TOXIC_FLUID =
            FLUIDS.register("toxic_fluid", () -> new ForgeFlowingFluid.Source(TOXIC_PROPERTIES[0]));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> TOXIC_FLUID_FLOWING =
            FLUIDS.register("toxic_fluid_flowing", () -> new ForgeFlowingFluid.Flowing(TOXIC_PROPERTIES[0]));
    private static final ForgeFlowingFluid.Properties[] VOLCANIC_LAVA_PROPERTIES =
            new ForgeFlowingFluid.Properties[1];
    public static final RegistryObject<LegacyWorldForgeFluidType> VOLCANIC_LAVA_FLUID_TYPE = FLUID_TYPES.register(
            "volcanic_lava_fluid",
            () -> new LegacyWorldForgeFluidType(net.minecraftforge.fluids.FluidType.Properties.create()
                    .descriptionId("fluid." + HbmNtm.MOD_ID + ".volcanic_lava_fluid")
                    .density(3000)
                    .viscosity(3000)
                    .lightLevel(15)
                    .temperature(1300),
                    texture("block/volcanic_lava_still"), texture("block/volcanic_lava_flowing")));
    public static final RegistryObject<ForgeFlowingFluid.Source> VOLCANIC_LAVA_FLUID =
            FLUIDS.register("volcanic_lava_fluid", () -> new ForgeFlowingFluid.Source(VOLCANIC_LAVA_PROPERTIES[0]));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> VOLCANIC_LAVA_FLUID_FLOWING =
            FLUIDS.register("volcanic_lava_fluid_flowing",
                    () -> new ForgeFlowingFluid.Flowing(VOLCANIC_LAVA_PROPERTIES[0]));
    private static final ForgeFlowingFluid.Properties[] RAD_LAVA_PROPERTIES = new ForgeFlowingFluid.Properties[1];
    public static final RegistryObject<LegacyWorldForgeFluidType> RAD_LAVA_FLUID_TYPE = FLUID_TYPES.register(
            "rad_lava_fluid",
            () -> new LegacyWorldForgeFluidType(net.minecraftforge.fluids.FluidType.Properties.create()
                    .descriptionId("fluid." + HbmNtm.MOD_ID + ".rad_lava_fluid")
                    .density(3000)
                    .viscosity(3000)
                    .lightLevel(15)
                    .temperature(1300),
                    texture("block/rad_lava_still"), texture("block/rad_lava_flowing")));
    public static final RegistryObject<ForgeFlowingFluid.Source> RAD_LAVA_FLUID =
            FLUIDS.register("rad_lava_fluid", () -> new ForgeFlowingFluid.Source(RAD_LAVA_PROPERTIES[0]));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> RAD_LAVA_FLUID_FLOWING =
            FLUIDS.register("rad_lava_fluid_flowing", () -> new ForgeFlowingFluid.Flowing(RAD_LAVA_PROPERTIES[0]));

    static {
        MUD_PROPERTIES[0] = new ForgeFlowingFluid.Properties(MUD_FLUID_TYPE, MUD_FLUID, MUD_FLUID_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
        TOXIC_PROPERTIES[0] = new ForgeFlowingFluid.Properties(TOXIC_FLUID_TYPE, TOXIC_FLUID, TOXIC_FLUID_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
        VOLCANIC_LAVA_PROPERTIES[0] = new ForgeFlowingFluid.Properties(VOLCANIC_LAVA_FLUID_TYPE,
                VOLCANIC_LAVA_FLUID, VOLCANIC_LAVA_FLUID_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
        RAD_LAVA_PROPERTIES[0] = new ForgeFlowingFluid.Properties(RAD_LAVA_FLUID_TYPE,
                RAD_LAVA_FLUID, RAD_LAVA_FLUID_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
        registerRemainingHbmFluids();
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    public static void registerMappings() {
        for (HbmFluidRegistryEntry entry : ENTRIES.values()) {
            HbmFluidForgeMappings.register(entry.hbmType(), entry.source().get());
        }
    }

    public static HbmFluidRegistryEntry getEntry(FluidType hbmType) {
        return ENTRIES.get(hbmType);
    }

    public static ForgeFlowingFluid.Properties mudProperties() {
        return MUD_PROPERTIES[0];
    }

    public static ForgeFlowingFluid.Properties toxicProperties() {
        return TOXIC_PROPERTIES[0];
    }

    public static ForgeFlowingFluid.Properties volcanicLavaProperties() {
        return VOLCANIC_LAVA_PROPERTIES[0];
    }

    public static ForgeFlowingFluid.Properties radLavaProperties() {
        return RAD_LAVA_PROPERTIES[0];
    }

    private static HbmFluidRegistryEntry register(FluidType hbmType) {
        if (ENTRIES.containsKey(hbmType)) {
            return ENTRIES.get(hbmType);
        }
        String name = hbmType.toPath();
        ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];
        RegistryObject<HbmForgeFluidType> forgeType = FLUID_TYPES.register(name, () -> new HbmForgeFluidType(hbmType));
        RegistryObject<ForgeFlowingFluid.Source> source =
                FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(properties[0]));
        RegistryObject<ForgeFlowingFluid.Flowing> flowing =
                FLUIDS.register(name + "_flowing", () -> new ForgeFlowingFluid.Flowing(properties[0]));
        properties[0] = new ForgeFlowingFluid.Properties(forgeType, source, flowing);
        HbmFluidRegistryEntry entry = new HbmFluidRegistryEntry(hbmType, forgeType, source, flowing, properties[0]);
        ENTRIES.put(hbmType, entry);
        return entry;
    }

    private static void registerRemainingHbmFluids() {
        for (FluidType type : HbmFluids.all()) {
            if (type == HbmFluids.NONE || type == HbmFluids.WATER || type == HbmFluids.LAVA || type.hasNoId()) {
                continue;
            }
            register(type);
        }
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    public record HbmFluidRegistryEntry(
            FluidType hbmType,
            RegistryObject<HbmForgeFluidType> forgeType,
            RegistryObject<ForgeFlowingFluid.Source> source,
            RegistryObject<ForgeFlowingFluid.Flowing> flowing,
            ForgeFlowingFluid.Properties properties) {
        public Supplier<? extends Fluid> sourceFluid() {
            return source;
        }
    }

    private ModFluids() {
    }
}
