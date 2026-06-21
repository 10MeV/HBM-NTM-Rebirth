package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeAsbestos;
import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.hazard.type.HazardTypeBlinding;
import com.hbm.hazard.type.HazardTypeCoal;
import com.hbm.hazard.type.HazardTypeDigamma;
import com.hbm.hazard.type.HazardTypeExplosive;
import com.hbm.hazard.type.HazardTypeHot;
import com.hbm.hazard.type.HazardTypeHydroactive;
import com.hbm.hazard.type.HazardTypeRadiation;
import com.hbm.ntm.radiation.RadiationConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy package facade for 1.7.10 HazardRegistry constants and helpers.
 */
@Deprecated(forRemoval = false)
public final class HazardRegistry {
    public static final float gen_S = 10_000.0F;
    public static final float gen_H = 2_000.0F;
    public static final float gen_10D = 100.0F;
    public static final float gen_100D = 80.0F;
    public static final float gen_1Y = 50.0F;
    public static final float gen_10Y = 30.0F;
    public static final float gen_100Y = 10.0F;
    public static final float gen_1K = 7.5F;
    public static final float gen_10K = 6.25F;
    public static final float gen_100K = 5.0F;
    public static final float gen_1M = 2.5F;
    public static final float gen_10M = 1.5F;
    public static final float gen_100M = 1.0F;
    public static final float gen_1B = 0.5F;
    public static final float gen_10B = 0.1F;

    public static final float co60 = RadiationConstants.CO60;
    public static final float sr90 = RadiationConstants.SR90;
    public static final float tc99 = RadiationConstants.TC99;
    public static final float i131 = RadiationConstants.I131;
    public static final float xe135 = RadiationConstants.XE135;
    public static final float cs137 = RadiationConstants.CS137;
    public static final float au198 = RadiationConstants.AU198;
    public static final float pb209 = RadiationConstants.PB209;
    public static final float at209 = RadiationConstants.AT209;
    public static final float po210 = RadiationConstants.PO210;
    public static final float ra226 = RadiationConstants.RA226;
    public static final float ac227 = RadiationConstants.AC227;
    public static final float th232 = RadiationConstants.TH232;
    public static final float thf = RadiationConstants.TH_FUEL;
    public static final float u = RadiationConstants.U;
    public static final float u233 = RadiationConstants.U233;
    public static final float u235 = RadiationConstants.U235;
    public static final float u238 = RadiationConstants.U238;
    public static final float uf = RadiationConstants.U_FUEL;
    public static final float uzh = RadiationConstants.UZH;
    public static final float np237 = RadiationConstants.NP237;
    public static final float npf = RadiationConstants.NP_FUEL;
    public static final float pu = RadiationConstants.PU;
    public static final float purg = RadiationConstants.PU_REACTOR_GRADE;
    public static final float pu238 = RadiationConstants.PU238;
    public static final float pu239 = RadiationConstants.PU239;
    public static final float pu240 = RadiationConstants.PU240;
    public static final float pu241 = RadiationConstants.PU241;
    public static final float puf = RadiationConstants.PU_FUEL;
    public static final float am241 = RadiationConstants.AM241;
    public static final float am242 = RadiationConstants.AM242;
    public static final float amrg = RadiationConstants.AM_MIX;
    public static final float amf = RadiationConstants.AM_FUEL;
    public static final float mox = RadiationConstants.MOX_FUEL;
    public static final float sa326 = RadiationConstants.SA326;
    public static final float sa327 = RadiationConstants.SA327;
    public static final float saf = RadiationConstants.SA_FUEL;
    public static final float sas3 = RadiationConstants.SAS3;
    public static final float gh336 = 5.0F;
    public static final float mud = 1.0F;
    public static final float radsource_mult = RadiationConstants.RADSOURCE_MULTIPLIER;
    public static final float pobe = RadiationConstants.PO210_BE;
    public static final float rabe = RadiationConstants.RA226_BE;
    public static final float pube = RadiationConstants.PU238_BE;
    public static final float zfb_bi = u235 * 0.35F;
    public static final float zfb_pu241 = pu241 * 0.5F;
    public static final float zfb_am_mix = amrg * 0.5F;
    public static final float bf = RadiationConstants.BALEFIRE;
    public static final float bfb = 500_000.0F;

    public static final float sr = RadiationConstants.SCHRARANIUM;
    public static final float sb = RadiationConstants.SCHRARANIUM;
    public static final float trx = RadiationConstants.TRIXITE;
    public static final float trn = RadiationConstants.TRINITITE;
    public static final float wst = RadiationConstants.WASTE;
    public static final float wstv = RadiationConstants.WASTE_VITRIFIED;
    public static final float yc = RadiationConstants.YELLOWCAKE;
    public static final float fo = RadiationConstants.FALLOUT;

    public static final float nugget = RadiationConstants.NUGGET;
    public static final float ingot = RadiationConstants.INGOT;
    public static final float gem = RadiationConstants.INGOT;
    public static final float plate = ingot;
    public static final float plateCast = plate * 3.0F;
    public static final float powder_mult = RadiationConstants.POWDER_MULTIPLIER;
    public static final float powder = ingot * powder_mult;
    public static final float powder_tiny = nugget * powder_mult;
    public static final float ore = ingot;
    public static final float block = RadiationConstants.BLOCK;
    public static final float crystal = block;
    public static final float billet = RadiationConstants.BILLET;
    public static final float rtg = RadiationConstants.RTG;
    public static final float rod = RadiationConstants.ROD;
    public static final float rod_dual = RadiationConstants.ROD_DUAL;
    public static final float rod_quad = RadiationConstants.ROD_QUAD;
    public static final float rod_rbmk = RadiationConstants.ROD_RBMK;

    public static final HazardTypeBase RADIATION = new HazardTypeRadiation();
    public static final HazardTypeBase DIGAMMA = new HazardTypeDigamma();
    public static final HazardTypeBase HOT = new HazardTypeHot();
    public static final HazardTypeBase BLINDING = new HazardTypeBlinding();
    public static final HazardTypeBase ASBESTOS = new HazardTypeAsbestos();
    public static final HazardTypeBase COAL = new HazardTypeCoal();
    public static final HazardTypeBase HYDROACTIVE = new HazardTypeHydroactive();
    public static final HazardTypeBase EXPLOSIVE = new HazardTypeExplosive();

    public static void registerItems() {
        com.hbm.ntm.radiation.HazardRegistry.registerDefaults();
    }

    public static void registerTrafos() {
        // Modern default registration already installs the source-backed NBT -> container -> ME chain.
    }

    public static HazardData makeData() {
        return new HazardData();
    }

    public static HazardData makeData(HazardTypeBase hazard) {
        return new HazardData().addEntry(hazard);
    }

    public static HazardData makeData(HazardTypeBase hazard, float level) {
        return new HazardData().addEntry(hazard, level);
    }

    public static HazardData makeData(HazardTypeBase hazard, float level, boolean override) {
        return new HazardData().addEntry(hazard, level, override);
    }

    public static void registerOtherFuel(Item fuel, float base, float target, boolean blinding) {
        com.hbm.ntm.radiation.HazardRegistry.registerFuelRadiation(fuel, base, target, blinding);
    }

    public static void registerOtherFuel(Item fuel, int meta, float base, float target, boolean blinding) {
        com.hbm.ntm.radiation.HazardRegistry.registerFuelRadiation(stack(fuel, meta), base, target, blinding);
    }

    public static void registerOtherFuel(ItemStack fuel, float base, float target, boolean blinding) {
        com.hbm.ntm.radiation.HazardRegistry.registerFuelRadiation(fuel, base, target, blinding);
    }

    public static void registerRTGPellet(Item pellet, float base, float target) {
        registerRTGPellet(pellet, base, target, 0.0F, 0.0F);
    }

    public static void registerRTGPellet(Item pellet, float base, float target, float hot) {
        registerRTGPellet(pellet, base, target, hot, 0.0F);
    }

    public static void registerRTGPellet(Item pellet, float base, float target, float hot, float blinding) {
        com.hbm.ntm.radiation.HazardRegistry.registerRtgPellet(pellet, base, target, hot, blinding);
    }

    public static void registerRTGPellet(ItemStack pellet, float base, float target) {
        registerRTGPellet(pellet, base, target, 0.0F, 0.0F);
    }

    public static void registerRTGPellet(ItemStack pellet, float base, float target, float hot) {
        registerRTGPellet(pellet, base, target, hot, 0.0F);
    }

    public static void registerRTGPellet(ItemStack pellet, float base, float target, float hot, float blinding) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(RADIATION, base).addMod(new com.hbm.hazard.modifier.HazardModifierRTGRadiation(target)));
        if (hot > 0.0F) {
            data.addEntry(HOT, hot);
        }
        if (blinding > 0.0F) {
            data.addEntry(BLINDING, blinding);
        }
        HazardSystem.register(pellet, data);
    }

    public static void registerRBMK(Item rod, float base, float dep, boolean hot, boolean linear, float blinding, float digamma) {
        com.hbm.ntm.radiation.HazardRegistry.registerRbmkFuel(rod, base, dep, hot, linear, blinding, digamma);
    }

    public static void registerRBMK(ItemStack rod, float base, float dep, boolean hot, boolean linear, float blinding, float digamma) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(RADIATION, base).addMod(new com.hbm.hazard.modifier.HazardModifierRBMKRadiation(dep, linear)));
        if (hot) {
            data.addEntry(new HazardEntry(HOT, 0.0F).addMod(new com.hbm.hazard.modifier.HazardModifierRBMKHot()));
        }
        if (blinding > 0.0F) {
            data.addEntry(BLINDING, blinding);
        }
        if (digamma > 0.0F) {
            data.addEntry(DIGAMMA, digamma);
        }
        HazardSystem.register(rod, data);
    }

    public static void registerRBMKRod(Item rod, float base, float dep) {
        registerRBMK(rod, base, dep, true, false, 0.0F, 0.0F);
    }

    public static void registerRBMKRod(Item rod, float base, float dep, float blinding) {
        registerRBMK(rod, base, dep, true, false, blinding, 0.0F);
    }

    public static void registerRBMKRod(Item rod, float base, float dep, boolean linear) {
        registerRBMK(rod, base, dep, true, linear, 0.0F, 0.0F);
    }

    public static void registerRBMKPellet(Item pellet, float base, float dep) {
        com.hbm.ntm.radiation.HazardRegistry.registerRbmkPellet(pellet, base, dep, 0.0F, 0.0F);
    }

    public static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear) {
        registerRBMKPellet(pellet, base, dep, linear, 0.0F, 0.0F);
    }

    public static void registerRBMKPellet(Item pellet, float base, float dep, float blinding, float digamma) {
        com.hbm.ntm.radiation.HazardRegistry.registerRbmkPellet(pellet, base, dep, blinding, digamma);
    }

    public static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear, float blinding, float digamma) {
        registerRBMKPellet(pellet, base, dep, blinding, digamma);
    }

    public static void registerRBMKPellet(ItemStack pellet, int legacyMeta, float base, float dep) {
        registerRBMKPellet(pellet, legacyMeta, base, dep, false, 0.0F, 0.0F);
    }

    public static void registerRBMKPellet(ItemStack pellet, int legacyMeta, float base, float dep, boolean linear) {
        registerRBMKPellet(pellet, legacyMeta, base, dep, linear, 0.0F, 0.0F);
    }

    public static void registerRBMKPellet(ItemStack pellet, int legacyMeta, float base, float dep, boolean linear,
                                          float blinding, float digamma) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(RADIATION, base).addMod(new com.hbm.hazard.modifier.HazardModifierRBMKRadiation(dep, linear)));
        if (blinding > 0.0F) {
            data.addEntry(BLINDING, blinding);
        }
        if (digamma > 0.0F) {
            data.addEntry(DIGAMMA, digamma);
        }
        ItemStack stack = pellet.copy();
        stack.setDamageValue(legacyMeta);
        HazardSystem.register(stack, data);
    }

    public static void registerPWRFuel(Item fuel, Item hotFuel, Item depletedFuel, int meta, float baseRad) {
        HazardSystem.register(stack(fuel, meta), makeData(RADIATION, baseRad));
        HazardSystem.register(stack(hotFuel, meta), makeData(RADIATION, baseRad * 10.0F).addEntry(HOT, 5.0F));
        HazardSystem.register(stack(depletedFuel, meta), makeData(RADIATION, baseRad * 10.0F));
    }

    public static void registerBreedingRodRadiation(Item rod, Item rodDual, Item rodQuad, int meta, float base) {
        HazardSystem.register(stack(rod, meta), makeData(RADIATION, base));
        HazardSystem.register(stack(rodDual, meta), makeData(RADIATION, base * rod_dual));
        HazardSystem.register(stack(rodQuad, meta), makeData(RADIATION, base * rod_quad));
    }

    private static ItemStack stack(Item item, int meta) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(meta);
        return stack;
    }

    private HazardRegistry() {
    }
}
