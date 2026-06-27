package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;

public abstract class FluidTrait {
    private static final Map<String, Class<? extends FluidTrait>> TRAITS_BY_LEGACY_NAME = new LinkedHashMap<>();
    private static final Map<Class<? extends FluidTrait>, String> LEGACY_NAMES_BY_TRAIT = new LinkedHashMap<>();

    static {
        registerLegacyName("corrosive", CorrosiveFluidTrait.class);
        registerLegacyName("flammable", FlammableFluidTrait.class);
        registerLegacyName("combustible", CombustibleFluidTrait.class);
        registerLegacyName("polluting", PollutingFluidTrait.class);
        registerLegacyName("heatable", HeatableFluidTrait.class);
        registerLegacyName("coolable", CoolableFluidTrait.class);
        registerLegacyName("pwrmoderator", PwrModeratorFluidTrait.class);
        registerLegacyName("poison", PoisonFluidTrait.class);
        registerLegacyName("toxin", ToxinFluidTrait.class);
        registerLegacyName("ventradiation", VentRadiationFluidTrait.class);
        registerLegacyName("pheromone", PheromoneFluidTrait.class);
        registerLegacyName("container", ContainerFluidTrait.class);
        registerLegacyName("gaseous", SimpleFluidTraits.Gaseous.class);
        registerLegacyName("gaseous_art", SimpleFluidTraits.GaseousAtRoomTemperature.class);
        registerLegacyName("liquid", SimpleFluidTraits.Liquid.class);
        registerLegacyName("viscous", SimpleFluidTraits.Viscous.class);
        registerLegacyName("plasma", SimpleFluidTraits.Plasma.class);
        registerLegacyName("amat", SimpleFluidTraits.Antimatter.class);
        registerLegacyName("leadcontainer", SimpleFluidTraits.LeadContainer.class);
        registerLegacyName("delicious", SimpleFluidTraits.Delicious.class);
        registerLegacyName("noid", SimpleFluidTraits.NoId.class);
        registerLegacyName("nocontainer", SimpleFluidTraits.NoContainer.class);
        registerLegacyName("unsiphonable", SimpleFluidTraits.Unsiphonable.class);
    }

    public void addInfo(List<Component> info) {
    }

    public void addHiddenInfo(List<Component> info) {
    }

    public String getLegacyName() {
        return legacyName(getClass());
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        writeJson(object);
        return object;
    }

    public void writeJson(JsonObject object) {
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
    }

    public void deserializeJSON(JsonObject object) {
    }

    public static Map<String, Class<? extends FluidTrait>> legacyTraitNames() {
        return Collections.unmodifiableMap(TRAITS_BY_LEGACY_NAME);
    }

    public static String legacyName(Class<? extends FluidTrait> traitClass) {
        String name = LEGACY_NAMES_BY_TRAIT.get(traitClass);
        if (name != null) {
            return name;
        }
        return traitClass.getSimpleName().replace("FluidTrait", "").toLowerCase(Locale.US);
    }

    private static void registerLegacyName(String name, Class<? extends FluidTrait> traitClass) {
        TRAITS_BY_LEGACY_NAME.put(name, traitClass);
        LEGACY_NAMES_BY_TRAIT.put(traitClass, name);
    }

}
