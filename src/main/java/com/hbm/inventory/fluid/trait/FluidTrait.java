package com.hbm.inventory.fluid.trait;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy package facade for the 1.7.10 fluid trait release-type enum.
 */
@Deprecated(forRemoval = false)
public final class FluidTrait {
    public static final List<Class<? extends com.hbm.ntm.fluid.trait.FluidTrait>> traitList = new ArrayList<>();
    public static final Map<String, Class<? extends com.hbm.ntm.fluid.trait.FluidTrait>> traitNameMap =
            new LinkedHashMap<>();

    static {
        registerTrait("corrosive", FT_Corrosive.class);
        registerTrait("flammable", FT_Flammable.class);
        registerTrait("combustible", FT_Combustible.class);
        registerTrait("polluting", FT_Polluting.class);
        registerTrait("heatable", FT_Heatable.class);
        registerTrait("coolable", FT_Coolable.class);
        registerTrait("pwrmoderator", FT_PWRModerator.class);
        registerTrait("poison", FT_Poison.class);
        registerTrait("toxin", FT_Toxin.class);
        registerTrait("ventradiation", FT_VentRadiation.class);
        registerTrait("pheromone", FT_Pheromone.class);
        registerTrait("gaseous", FluidTraitSimple.FT_Gaseous.class);
        registerTrait("gaseous_art", FluidTraitSimple.FT_Gaseous_ART.class);
        registerTrait("liquid", FluidTraitSimple.FT_Liquid.class);
        registerTrait("viscous", FluidTraitSimple.FT_Viscous.class);
        registerTrait("plasma", FluidTraitSimple.FT_Plasma.class);
        registerTrait("amat", FluidTraitSimple.FT_Amat.class);
        registerTrait("leadcontainer", FluidTraitSimple.FT_LeadContainer.class);
        registerTrait("delicious", FluidTraitSimple.FT_Delicious.class);
        registerTrait("noid", FluidTraitSimple.FT_NoID.class);
        registerTrait("nocontainer", FluidTraitSimple.FT_NoContainer.class);
        registerTrait("unsiphonable", FluidTraitSimple.FT_Unsiphonable.class);
    }

    public enum FluidReleaseType {
        VOID,
        BURN,
        SPILL;

        public com.hbm.ntm.fluid.FluidReleaseType modern() {
            return switch (this) {
                case VOID -> com.hbm.ntm.fluid.FluidReleaseType.VOID;
                case BURN -> com.hbm.ntm.fluid.FluidReleaseType.BURN;
                case SPILL -> com.hbm.ntm.fluid.FluidReleaseType.SPILL;
            };
        }

        public static FluidReleaseType fromModern(com.hbm.ntm.fluid.FluidReleaseType type) {
            if (type == null) {
                return SPILL;
            }
            return switch (type) {
                case VOID -> VOID;
                case BURN -> BURN;
                case SPILL -> SPILL;
            };
        }
    }

    private static void registerTrait(String name, Class<? extends com.hbm.ntm.fluid.trait.FluidTrait> clazz) {
        traitNameMap.put(name, clazz);
        traitList.add(clazz);
    }

    private FluidTrait() {
    }
}
