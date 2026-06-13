package com.hbm.inventory.fluid;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.Collection;

/**
 * Legacy package facade for the 1.7.10 fluid registry.
 */
@Deprecated(forRemoval = false)
public final class Fluids {
    public static final FluidType NONE = HbmFluids.NONE;
    public static final FluidType AIR = HbmFluids.AIR;
    public static final FluidType AIRBLAST = HbmFluids.AIRBLAST;
    public static final FluidType WATER = HbmFluids.WATER;
    public static final FluidType STEAM = HbmFluids.STEAM;
    public static final FluidType HOTSTEAM = HbmFluids.HOTSTEAM;
    public static final FluidType SUPERHOTSTEAM = HbmFluids.SUPERHOTSTEAM;
    public static final FluidType ULTRAHOTSTEAM = HbmFluids.ULTRAHOTSTEAM;
    public static final FluidType COOLANT = HbmFluids.COOLANT;
    public static final FluidType COOLANT_HOT = HbmFluids.COOLANT_HOT;
    public static final FluidType PERFLUOROMETHYL = HbmFluids.PERFLUOROMETHYL;
    public static final FluidType PERFLUOROMETHYL_COLD = HbmFluids.PERFLUOROMETHYL_COLD;
    public static final FluidType PERFLUOROMETHYL_HOT = HbmFluids.PERFLUOROMETHYL_HOT;
    public static final FluidType LAVA = HbmFluids.LAVA;
    public static final FluidType DEUTERIUM = HbmFluids.DEUTERIUM;
    public static final FluidType TRITIUM = HbmFluids.TRITIUM;
    public static final FluidType OIL = HbmFluids.OIL;
    public static final FluidType CRACKOIL = HbmFluids.CRACKOIL;
    public static final FluidType COALOIL = HbmFluids.COALOIL;
    public static final FluidType OIL_DS = HbmFluids.OIL_DS;
    public static final FluidType CRACKOIL_DS = HbmFluids.CRACKOIL_DS;
    public static final FluidType HOTOIL = HbmFluids.HOTOIL;
    public static final FluidType HOTCRACKOIL = HbmFluids.HOTCRACKOIL;
    public static final FluidType HOTOIL_DS = HbmFluids.HOTOIL_DS;
    public static final FluidType HOTCRACKOIL_DS = HbmFluids.HOTCRACKOIL_DS;
    public static final FluidType HEAVYOIL = HbmFluids.HEAVYOIL;
    public static final FluidType BITUMEN = HbmFluids.BITUMEN;
    public static final FluidType SMEAR = HbmFluids.SMEAR;
    public static final FluidType HEATINGOIL = HbmFluids.HEATINGOIL;
    public static final FluidType RECLAIMED = HbmFluids.RECLAIMED;
    public static final FluidType LUBRICANT = HbmFluids.LUBRICANT;
    public static final FluidType NAPHTHA = HbmFluids.NAPHTHA;
    public static final FluidType NAPHTHA_CRACK = HbmFluids.NAPHTHA_CRACK;
    public static final FluidType NAPHTHA_DS = HbmFluids.NAPHTHA_DS;
    public static final FluidType DIESEL = HbmFluids.DIESEL;
    public static final FluidType DIESEL_CRACK = HbmFluids.DIESEL_CRACK;
    public static final FluidType LIGHTOIL = HbmFluids.LIGHTOIL;
    public static final FluidType LIGHTOIL_CRACK = HbmFluids.LIGHTOIL_CRACK;
    public static final FluidType LIGHTOIL_DS = HbmFluids.LIGHTOIL_DS;
    public static final FluidType KEROSENE = HbmFluids.KEROSENE;
    public static final FluidType GAS = HbmFluids.GAS;
    public static final FluidType PETROLEUM = HbmFluids.PETROLEUM;
    public static final FluidType LPG = HbmFluids.LPG;
    public static final FluidType AROMATICS = HbmFluids.AROMATICS;
    public static final FluidType UNSATURATEDS = HbmFluids.UNSATURATEDS;
    public static final FluidType BIOGAS = HbmFluids.BIOGAS;
    public static final FluidType BIOFUEL = HbmFluids.BIOFUEL;
    public static final FluidType NITAN = HbmFluids.NITAN;
    public static final FluidType UF6 = HbmFluids.UF6;
    public static final FluidType PUF6 = HbmFluids.PUF6;
    public static final FluidType SAS3 = HbmFluids.SAS3;
    public static final FluidType SCHRABIDIC = HbmFluids.SCHRABIDIC;
    public static final FluidType AMAT = HbmFluids.AMAT;
    public static final FluidType ASCHRAB = HbmFluids.ASCHRAB;
    public static final FluidType PEROXIDE = HbmFluids.PEROXIDE;
    public static final FluidType WATZ = HbmFluids.WATZ;
    public static final FluidType CRYOGEL = HbmFluids.CRYOGEL;
    public static final FluidType HYDROGEN = HbmFluids.HYDROGEN;
    public static final FluidType OXYGEN = HbmFluids.OXYGEN;
    public static final FluidType XENON = HbmFluids.XENON;
    public static final FluidType BALEFIRE = HbmFluids.BALEFIRE;
    public static final FluidType MERCURY = HbmFluids.MERCURY;
    public static final FluidType PAIN = HbmFluids.PAIN;
    public static final FluidType WASTEFLUID = HbmFluids.WASTEFLUID;
    public static final FluidType WASTEGAS = HbmFluids.WASTEGAS;
    public static final FluidType PETROIL = HbmFluids.PETROIL;
    public static final FluidType PETROIL_LEADED = HbmFluids.PETROIL_LEADED;
    public static final FluidType GASOLINE = HbmFluids.GASOLINE;
    public static final FluidType GASOLINE_LEADED = HbmFluids.GASOLINE_LEADED;
    public static final FluidType COALGAS = HbmFluids.COALGAS;
    public static final FluidType COALGAS_LEADED = HbmFluids.COALGAS_LEADED;
    public static final FluidType SPENTSTEAM = HbmFluids.SPENTSTEAM;
    public static final FluidType FRACKSOL = HbmFluids.FRACKSOL;
    public static final FluidType PLASMA_DT = HbmFluids.PLASMA_DT;
    public static final FluidType PLASMA_HD = HbmFluids.PLASMA_HD;
    public static final FluidType PLASMA_HT = HbmFluids.PLASMA_HT;
    public static final FluidType PLASMA_DH3 = HbmFluids.PLASMA_DH3;
    public static final FluidType PLASMA_XM = HbmFluids.PLASMA_XM;
    public static final FluidType PLASMA_BF = HbmFluids.PLASMA_BF;
    public static final FluidType CARBONDIOXIDE = HbmFluids.CARBONDIOXIDE;
    public static final FluidType HELIUM3 = HbmFluids.HELIUM3;
    public static final FluidType DEATH = HbmFluids.DEATH;
    public static final FluidType ETHANOL = HbmFluids.ETHANOL;
    public static final FluidType HEAVYWATER = HbmFluids.HEAVYWATER;
    public static final FluidType SALIENT = HbmFluids.SALIENT;
    public static final FluidType XPJUICE = HbmFluids.XPJUICE;
    public static final FluidType ENDERJUICE = HbmFluids.ENDERJUICE;
    public static final FluidType SULFURIC_ACID = HbmFluids.SULFURIC_ACID;
    public static final FluidType MUG = HbmFluids.MUG;
    public static final FluidType MUG_HOT = HbmFluids.MUG_HOT;
    public static final FluidType WOODOIL = HbmFluids.WOODOIL;
    public static final FluidType COALCREOSOTE = HbmFluids.COALCREOSOTE;
    public static final FluidType SEEDSLURRY = HbmFluids.SEEDSLURRY;
    public static final FluidType NITRIC_ACID = HbmFluids.NITRIC_ACID;
    public static final FluidType SOLVENT = HbmFluids.SOLVENT;
    public static final FluidType BLOOD = HbmFluids.BLOOD;
    public static final FluidType BLOOD_HOT = HbmFluids.BLOOD_HOT;
    public static final FluidType PHEROMONE = HbmFluids.PHEROMONE;
    public static final FluidType PHEROMONE_M = HbmFluids.PHEROMONE_M;
    public static final FluidType SYNGAS = HbmFluids.SYNGAS;
    public static final FluidType OXYHYDROGEN = HbmFluids.OXYHYDROGEN;
    public static final FluidType RADIOSOLVENT = HbmFluids.RADIOSOLVENT;
    public static final FluidType CHLORINE = HbmFluids.CHLORINE;
    public static final FluidType HEAVYOIL_VACUUM = HbmFluids.HEAVYOIL_VACUUM;
    public static final FluidType REFORMATE = HbmFluids.REFORMATE;
    public static final FluidType LIGHTOIL_VACUUM = HbmFluids.LIGHTOIL_VACUUM;
    public static final FluidType SOURGAS = HbmFluids.SOURGAS;
    public static final FluidType XYLENE = HbmFluids.XYLENE;
    public static final FluidType HEATINGOIL_VACUUM = HbmFluids.HEATINGOIL_VACUUM;
    public static final FluidType DIESEL_REFORM = HbmFluids.DIESEL_REFORM;
    public static final FluidType DIESEL_CRACK_REFORM = HbmFluids.DIESEL_CRACK_REFORM;
    public static final FluidType KEROSENE_REFORM = HbmFluids.KEROSENE_REFORM;
    public static final FluidType REFORMGAS = HbmFluids.REFORMGAS;
    public static final FluidType COLLOID = HbmFluids.COLLOID;
    public static final FluidType PHOSGENE = HbmFluids.PHOSGENE;
    public static final FluidType MUSTARDGAS = HbmFluids.MUSTARDGAS;
    public static final FluidType IONGEL = HbmFluids.IONGEL;
    public static final FluidType OIL_COKER = HbmFluids.OIL_COKER;
    public static final FluidType NAPHTHA_COKER = HbmFluids.NAPHTHA_COKER;
    public static final FluidType GAS_COKER = HbmFluids.GAS_COKER;
    public static final FluidType FLUE = HbmFluids.FLUE;
    public static final FluidType EGG = HbmFluids.EGG;
    public static final FluidType CHOLESTEROL = HbmFluids.CHOLESTEROL;
    public static final FluidType ESTRADIOL = HbmFluids.ESTRADIOL;
    public static final FluidType FISHOIL = HbmFluids.FISHOIL;
    public static final FluidType SUNFLOWEROIL = HbmFluids.SUNFLOWEROIL;
    public static final FluidType NITROGLYCERIN = HbmFluids.NITROGLYCERIN;
    public static final FluidType REDMUD = HbmFluids.REDMUD;
    public static final FluidType CHLOROCALCITE_SOLUTION = HbmFluids.CHLOROCALCITE_SOLUTION;
    public static final FluidType CHLOROCALCITE_MIX = HbmFluids.CHLOROCALCITE_MIX;
    public static final FluidType CHLOROCALCITE_CLEANED = HbmFluids.CHLOROCALCITE_CLEANED;
    public static final FluidType POTASSIUM_CHLORIDE = HbmFluids.POTASSIUM_CHLORIDE;
    public static final FluidType CALCIUM_CHLORIDE = HbmFluids.CALCIUM_CHLORIDE;
    public static final FluidType CALCIUM_SOLUTION = HbmFluids.CALCIUM_SOLUTION;
    public static final FluidType SMOKE = HbmFluids.SMOKE;
    public static final FluidType SMOKE_LEADED = HbmFluids.SMOKE_LEADED;
    public static final FluidType SMOKE_POISON = HbmFluids.SMOKE_POISON;
    public static final FluidType HELIUM4 = HbmFluids.HELIUM4;
    public static final FluidType HEAVYWATER_HOT = HbmFluids.HEAVYWATER_HOT;
    public static final FluidType SODIUM = HbmFluids.SODIUM;
    public static final FluidType SODIUM_HOT = HbmFluids.SODIUM_HOT;
    public static final FluidType LEAD = HbmFluids.LEAD;
    public static final FluidType LEAD_HOT = HbmFluids.LEAD_HOT;
    public static final FluidType THORIUM_SALT = HbmFluids.THORIUM_SALT;
    public static final FluidType THORIUM_SALT_HOT = HbmFluids.THORIUM_SALT_HOT;
    public static final FluidType THORIUM_SALT_DEPLETED = HbmFluids.THORIUM_SALT_DEPLETED;
    public static final FluidType FULLERENE = HbmFluids.FULLERENE;
    public static final FluidType STELLAR_FLUX = HbmFluids.STELLAR_FLUX;
    public static final FluidType VITRIOL = HbmFluids.VITRIOL;
    public static final FluidType SLOP = HbmFluids.SLOP;
    public static final FluidType LYE = HbmFluids.LYE;
    public static final FluidType SODIUM_ALUMINATE = HbmFluids.SODIUM_ALUMINATE;
    public static final FluidType BAUXITE_SOLUTION = HbmFluids.BAUXITE_SOLUTION;
    public static final FluidType ALUMINA = HbmFluids.ALUMINA;
    public static final FluidType CONCRETE = HbmFluids.CONCRETE;
    public static final FluidType DHC = HbmFluids.DHC;

    @Deprecated public static final FluidType ACID = HbmFluids.ACID;

    public static final float SOOT_UNREFINED_OIL = PollutionHandler.SOOT_PER_SECOND * 0.1F;
    public static final float SOOT_REFINED_OIL = PollutionHandler.SOOT_PER_SECOND * 0.025F;
    public static final float SOOT_GAS = PollutionHandler.SOOT_PER_SECOND * 0.005F;
    public static final float LEAD_FUEL = PollutionHandler.HEAVY_METAL_PER_SECOND * 0.025F;
    public static final float POISON_OIL = PollutionHandler.POISON_PER_SECOND * 0.0025F;
    public static final float POISON_EXTREME = PollutionHandler.POISON_PER_SECOND * 0.025F;
    public static final float POISON_MINOR = PollutionHandler.POISON_PER_SECOND * 0.001F;

    public static final FT_Polluting P_OIL = new FT_Polluting()
            .burn(PollutionHandler.PollutionType.SOOT, SOOT_UNREFINED_OIL)
            .release(PollutionHandler.PollutionType.POISON, POISON_OIL);
    public static final FT_Polluting P_FUEL = new FT_Polluting()
            .burn(PollutionHandler.PollutionType.SOOT, SOOT_REFINED_OIL)
            .release(PollutionHandler.PollutionType.POISON, POISON_OIL);
    public static final FT_Polluting P_FUEL_LEADED = new FT_Polluting()
            .burn(PollutionHandler.PollutionType.SOOT, SOOT_REFINED_OIL)
            .burn(PollutionHandler.PollutionType.HEAVYMETAL, LEAD_FUEL)
            .release(PollutionHandler.PollutionType.POISON, POISON_OIL)
            .release(PollutionHandler.PollutionType.HEAVYMETAL, LEAD_FUEL * 0.1F);
    public static final FT_Polluting P_GAS = new FT_Polluting()
            .burn(PollutionHandler.PollutionType.SOOT, SOOT_GAS)
            .release(PollutionHandler.PollutionType.POISON, POISON_OIL);
    public static final FT_Polluting P_LIQUID_GAS = new FT_Polluting()
            .burn(PollutionHandler.PollutionType.SOOT, SOOT_GAS * 2.0F);

    public static void init() {
        // Modern HbmFluids initializes during class loading and HbmNtm bootstrap.
    }

    public static void reloadFluids() {
        HbmFluids.bootstrap();
    }

    public static FluidType fromID(int id) {
        return HbmFluids.fromId(id);
    }

    public static FluidType fromId(int id) {
        return HbmFluids.fromId(id);
    }

    public static FluidType fromName(String name) {
        return HbmFluids.fromName(name);
    }

    public static FluidType fromNameCompat(String name) {
        return HbmFluids.fromName(name);
    }

    public static String toNameCompat(FluidType type) {
        return type == null ? HbmFluids.NONE.getName() : type.getName();
    }

    public static FluidType[] getAll() {
        Collection<FluidType> all = HbmFluids.all();
        return all.toArray(new FluidType[0]);
    }

    public static FluidType[] getInNiceOrder() {
        return HbmFluids.niceOrder().toArray(new FluidType[0]);
    }

    public static Collection<FluidType> customFluids() {
        return HbmFluids.customFluids();
    }

    public static Collection<FluidType> foreignFluids() {
        return HbmFluids.foreignFluids();
    }

    public static class CD_Canister {
        public int color;

        public CD_Canister(int color) {
            this.color = color;
        }
    }

    public static class CD_Gastank {
        public int bottleColor;
        public int labelColor;

        public CD_Gastank(int bottle, int label) {
            this.bottleColor = bottle;
            this.labelColor = label;
        }
    }

    private Fluids() {
    }
}
