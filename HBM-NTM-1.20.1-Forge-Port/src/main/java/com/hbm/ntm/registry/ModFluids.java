package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmForgeFluidType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
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
    public static final HbmFluidRegistryEntry SULFURIC_ACID = register(HbmFluids.SULFURIC_ACID);
    public static final HbmFluidRegistryEntry HYDROGEN = register(HbmFluids.HYDROGEN);
    public static final HbmFluidRegistryEntry DEUTERIUM = register(HbmFluids.DEUTERIUM);
    public static final HbmFluidRegistryEntry TRITIUM = register(HbmFluids.TRITIUM);

    static {
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
        HbmFluidRegistryEntry entry = new HbmFluidRegistryEntry(hbmType, forgeType, source, flowing);
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

    public record HbmFluidRegistryEntry(
            FluidType hbmType,
            RegistryObject<HbmForgeFluidType> forgeType,
            RegistryObject<ForgeFlowingFluid.Source> source,
            RegistryObject<ForgeFlowingFluid.Flowing> flowing) {
        public Supplier<? extends Fluid> sourceFluid() {
            return source;
        }
    }

    private ModFluids() {
    }
}
