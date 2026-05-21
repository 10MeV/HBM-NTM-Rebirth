package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.FluidTrait;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.ANTIMATTER;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.GASEOUS;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.GASEOUS_AT_ROOM_TEMPERATURE;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.LEAD_CONTAINER;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.LIQUID;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.NO_CONTAINER;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.NO_ID;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.PLASMA;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.UNSIPHONABLE;
import static com.hbm.ntm.fluid.trait.SimpleFluidTraits.VISCOUS;

public final class HbmFluids {
    private static final Map<Integer, FluidType> BY_ID = new LinkedHashMap<>();
    private static final Map<String, FluidType> BY_NAME = new LinkedHashMap<>();
    private static int nextId;

    public static final FluidType NONE = register("NONE", 0x888888, 0, 0, 0, FluidSymbol.NONE);
    public static final FluidType WATER = register("WATER", 0x3333FF, 0, 0, 0, FluidSymbol.NONE, LIQUID, UNSIPHONABLE);
    public static final FluidType STEAM = register("STEAM", 0xE5E5E5, 3, 0, 0, FluidSymbol.NONE, GASEOUS, UNSIPHONABLE).setTemperature(100);
    public static final FluidType HOTSTEAM = register("HOTSTEAM", 0xE7D6D6, 4, 0, 0, FluidSymbol.NONE, GASEOUS, UNSIPHONABLE).setTemperature(300);
    public static final FluidType SUPERHOTSTEAM = register("SUPERHOTSTEAM", 0xE7B7B7, 4, 0, 0, FluidSymbol.NONE, GASEOUS, UNSIPHONABLE).setTemperature(450);
    public static final FluidType ULTRAHOTSTEAM = register("ULTRAHOTSTEAM", 0xE39393, 4, 0, 0, FluidSymbol.NONE, GASEOUS, UNSIPHONABLE).setTemperature(600);
    public static final FluidType COOLANT = register("COOLANT", 0xD8FCFF, 1, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType LAVA = register("LAVA", 0xFF3300, 4, 0, 0, FluidSymbol.NOWATER, LIQUID, VISCOUS).setTemperature(1200);
    public static final FluidType DEUTERIUM = register("DEUTERIUM", 0x0000FF, 3, 4, 0, FluidSymbol.NONE, GASEOUS);
    public static final FluidType TRITIUM = register("TRITIUM", 0x000099, 3, 4, 0, FluidSymbol.RADIATION, GASEOUS);
    public static final FluidType OIL = register("OIL", 0x020202, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HOTOIL = register("HOTOIL", 0x300900, 2, 3, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(350);
    public static final FluidType HEAVYOIL = register("HEAVYOIL", 0x141312, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType BITUMEN = register("BITUMEN", 0x1F2426, 2, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType SMEAR = register("SMEAR", 0x190F01, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HEATINGOIL = register("HEATINGOIL", 0x211806, 2, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType RECLAIMED = register("RECLAIMED", 0x332B22, 2, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType PETROIL = register("PETROIL", 0x44413D, 1, 3, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType LUBRICANT = register("LUBRICANT", 0x606060, 2, 1, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType NAPHTHA = register("NAPHTHA", 0x595744, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType DIESEL = register("DIESEL", 0xF2EED5, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType LIGHTOIL = register("LIGHTOIL", 0x8C7451, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType KEROSENE = register("KEROSENE", 0xFFA5D2, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType GAS = register("GAS", 0xFFFEED, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType PETROLEUM = register("PETROLEUM", 0x7CB7C9, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType LPG = register("LPG", 0x4747EA, 1, 3, 1, FluidSymbol.NONE, LIQUID);
    public static final FluidType BIOGAS = register("BIOGAS", 0xBFD37C, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType BIOFUEL = register("BIOFUEL", 0xEEF274, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType NITAN = register("NITAN", 0x8018AD, 2, 4, 1, FluidSymbol.NONE, LIQUID);
    public static final FluidType UF6 = register("UF6", 0xD1CEBE, 4, 0, 2, FluidSymbol.RADIATION, GASEOUS);
    public static final FluidType PUF6 = register("PUF6", 0x4C4C4C, 4, 0, 4, FluidSymbol.RADIATION, GASEOUS);
    public static final FluidType SAS3 = register("SAS3", 0x4FFFFC, 5, 0, 4, FluidSymbol.RADIATION, LIQUID);
    public static final FluidType SCHRABIDIC = register("SCHRABIDIC", 0x006B6B, 5, 0, 5, FluidSymbol.ACID, LIQUID);
    public static final FluidType AMAT = register("AMAT", 0x010101, 5, 0, 5, FluidSymbol.ANTIMATTER, ANTIMATTER, GASEOUS);
    public static final FluidType ASCHRAB = register("ASCHRAB", 0xB50000, 5, 0, 5, FluidSymbol.ANTIMATTER, ANTIMATTER, GASEOUS);
    public static final FluidType PEROXIDE = register("PEROXIDE", 0xFFF7AA, 3, 0, 3, FluidSymbol.OXIDIZER, LIQUID);
    public static final FluidType ACID = alias("ACID", PEROXIDE);
    public static final FluidType WATZ = register("WATZ", 0x86653E, 4, 0, 3, FluidSymbol.ACID, LIQUID, VISCOUS);
    public static final FluidType CRYOGEL = register("CRYOGEL", 0x32FFFF, 2, 0, 0, FluidSymbol.CRYOGENIC, LIQUID, VISCOUS).setTemperature(-170);
    public static final FluidType HYDROGEN = register("HYDROGEN", 0x4286F4, 3, 4, 0, FluidSymbol.CRYOGENIC, LIQUID, GASEOUS_AT_ROOM_TEMPERATURE).setTemperature(-260);
    public static final FluidType OXYGEN = register("OXYGEN", 0x98BDF9, 3, 0, 0, FluidSymbol.CRYOGENIC, LIQUID, GASEOUS_AT_ROOM_TEMPERATURE).setTemperature(-100);
    public static final FluidType XENON = register("XENON", 0xBA45E8, 0, 0, 0, FluidSymbol.ASPHYXIANT, GASEOUS);
    public static final FluidType BALEFIRE = register("BALEFIRE", 0x28E02E, 4, 4, 3, FluidSymbol.RADIATION, LIQUID, VISCOUS).setTemperature(1500);
    public static final FluidType MERCURY = register("MERCURY", 0x808080, 2, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType PAIN = register("PAIN", 0x938541, 2, 0, 1, FluidSymbol.ACID, LIQUID, VISCOUS).setTemperature(300);
    public static final FluidType WASTEFLUID = register("WASTEFLUID", 0x544400, 2, 0, 1, FluidSymbol.RADIATION, NO_CONTAINER, LIQUID, VISCOUS);
    public static final FluidType WASTEGAS = register("WASTEGAS", 0xB8B8B8, 2, 0, 1, FluidSymbol.RADIATION, NO_CONTAINER, GASEOUS);
    public static final FluidType GASOLINE = register("GASOLINE", 0x445772, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType COALGAS = register("COALGAS", 0x445772, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType SPENTSTEAM = register("SPENTSTEAM", 0x445772, 2, 0, 0, FluidSymbol.NONE, NO_CONTAINER, GASEOUS);
    public static final FluidType FRACKSOL = register("FRACKSOL", 0x798A6B, 1, 3, 3, FluidSymbol.ACID, LIQUID, VISCOUS);
    public static final FluidType PLASMA_DT = register("PLASMA_DT", 0xF7AFDE, 0, 4, 0, FluidSymbol.RADIATION, NO_CONTAINER, NO_ID, PLASMA).setTemperature(3250);
    public static final FluidType PLASMA_HD = register("PLASMA_HD", 0xF0ADF4, 0, 4, 0, FluidSymbol.RADIATION, NO_CONTAINER, NO_ID, PLASMA).setTemperature(2500);
    public static final FluidType PLASMA_HT = register("PLASMA_HT", 0xD1ABF2, 0, 4, 0, FluidSymbol.RADIATION, NO_CONTAINER, NO_ID, PLASMA).setTemperature(3000);
    public static final FluidType PLASMA_XM = register("PLASMA_XM", 0xC6A5FF, 0, 4, 1, FluidSymbol.RADIATION, NO_CONTAINER, NO_ID, PLASMA).setTemperature(4250);
    public static final FluidType PLASMA_BF = register("PLASMA_BF", 0xA7F1A3, 4, 5, 4, FluidSymbol.ANTIMATTER, NO_CONTAINER, NO_ID, PLASMA).setTemperature(8500);
    public static final FluidType CARBONDIOXIDE = register("CARBONDIOXIDE", 0x404040, 3, 0, 0, FluidSymbol.ASPHYXIANT, GASEOUS);
    public static final FluidType PLASMA_DH3 = register("PLASMA_DH3", 0xFF83AA, 0, 4, 0, FluidSymbol.RADIATION, NO_CONTAINER, NO_ID, PLASMA).setTemperature(3480);
    public static final FluidType HELIUM3 = register("HELIUM3", 0xFCF0C4, 0, 0, 0, FluidSymbol.ASPHYXIANT, GASEOUS);
    public static final FluidType DEATH = register("DEATH", 0x717A88, 2, 0, 1, FluidSymbol.ACID, LEAD_CONTAINER, LIQUID, VISCOUS).setTemperature(300);
    public static final FluidType ETHANOL = register("ETHANOL", 0xE0FFFF, 2, 3, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType HEAVYWATER = register("HEAVYWATER", 0x00A0B0, 1, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType CRACKOIL = register("CRACKOIL", 0x020202, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType COALOIL = register("COALOIL", 0x020202, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HOTCRACKOIL = register("HOTCRACKOIL", 0x300900, 2, 3, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(350);
    public static final FluidType NAPHTHA_CRACK = register("NAPHTHA_CRACK", 0x595744, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType LIGHTOIL_CRACK = register("LIGHTOIL_CRACK", 0x8C7451, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType DIESEL_CRACK = register("DIESEL_CRACK", 0xF2EED5, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType AROMATICS = register("AROMATICS", 0x68A09A, 1, 4, 1, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType UNSATURATEDS = register("UNSATURATEDS", 0x628FAE, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType SALIENT = register("SALIENT", 0x457F2D, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType XPJUICE = register("XPJUICE", 0xBBFF09, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType ENDERJUICE = register("ENDERJUICE", 0x127766, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType PETROIL_LEADED = register("PETROIL_LEADED", 0x44413D, 1, 3, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType GASOLINE_LEADED = register("GASOLINE_LEADED", 0x445772, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType COALGAS_LEADED = register("COALGAS_LEADED", 0x445772, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType SULFURIC_ACID = register("SULFURIC_ACID", 0xB0AA64, 3, 0, 2, FluidSymbol.ACID, LIQUID);
    public static final FluidType COOLANT_HOT = register("COOLANT_HOT", 0x99525E, 1, 0, 0, FluidSymbol.NONE, LIQUID).setTemperature(600);
    public static final FluidType MUG = register("MUG", 0x4B2D28, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType MUG_HOT = register("MUG_HOT", 0x6B2A20, 0, 0, 0, FluidSymbol.NONE, LIQUID).setTemperature(500);
    public static final FluidType WOODOIL = register("WOODOIL", 0x847D54, 2, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType COALCREOSOTE = register("COALCREOSOTE", 0x51694F, 3, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType SEEDSLURRY = register("SEEDSLURRY", 0x7CC35E, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType NITRIC_ACID = register("NITRIC_ACID", 0xBB7A1E, 3, 0, 2, FluidSymbol.OXIDIZER, LIQUID);
    public static final FluidType SOLVENT = register("SOLVENT", 0xE4E3EF, 2, 3, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType BLOOD = register("BLOOD", 0xB22424, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType BLOOD_HOT = register("BLOOD_HOT", 0xF22419, 3, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(666);
    public static final FluidType SYNGAS = register("SYNGAS", 0x131313, 1, 4, 2, FluidSymbol.NONE, GASEOUS);
    public static final FluidType OXYHYDROGEN = register("OXYHYDROGEN", 0x483FC1, 0, 4, 2, FluidSymbol.NONE, GASEOUS);
    public static final FluidType RADIOSOLVENT = register("RADIOSOLVENT", 0xA4D7DD, 3, 3, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType CHLORINE = register("CHLORINE", 0xBAB572, 3, 0, 0, FluidSymbol.OXIDIZER, GASEOUS);
    public static final FluidType HEAVYOIL_VACUUM = register("HEAVYOIL_VACUUM", 0x131214, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType REFORMATE = register("REFORMATE", 0x835472, 2, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType LIGHTOIL_VACUUM = register("LIGHTOIL_VACUUM", 0x8C8851, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType SOURGAS = register("SOURGAS", 0xC9BE0D, 4, 4, 0, FluidSymbol.ACID, GASEOUS);
    public static final FluidType XYLENE = register("XYLENE", 0x5C4E76, 2, 3, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HEATINGOIL_VACUUM = register("HEATINGOIL_VACUUM", 0x211D06, 2, 2, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType DIESEL_REFORM = register("DIESEL_REFORM", 0xCDC3C6, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType DIESEL_CRACK_REFORM = register("DIESEL_CRACK_REFORM", 0xCDC3CC, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType KEROSENE_REFORM = register("KEROSENE_REFORM", 0xFFA5F3, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType REFORMGAS = register("REFORMGAS", 0x6362AE, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType COLLOID = register("COLLOID", 0x787878, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType PHOSGENE = register("PHOSGENE", 0xCFC4A4, 4, 0, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType MUSTARDGAS = register("MUSTARDGAS", 0xBAB572, 4, 1, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType IONGEL = register("IONGEL", 0xB8FFFF, 1, 0, 4, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType OIL_COKER = register("OIL_COKER", 0x001802, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType NAPHTHA_COKER = register("NAPHTHA_COKER", 0x495944, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType GAS_COKER = register("GAS_COKER", 0xDEF4CA, 1, 4, 0, FluidSymbol.NONE, GASEOUS);
    public static final FluidType EGG = register("EGG", 0xD2C273, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType CHOLESTEROL = register("CHOLESTEROL", 0xD6D2BD, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType ESTRADIOL = register("ESTRADIOL", 0xCDD5D8, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType FISHOIL = register("FISHOIL", 0x4B4A45, 0, 1, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType SUNFLOWEROIL = register("SUNFLOWEROIL", 0xCBAD45, 0, 1, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType NITROGLYCERIN = register("NITROGLYCERIN", 0x92ACA6, 0, 4, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType REDMUD = register("REDMUD", 0xD85638, 3, 0, 4, FluidSymbol.NONE, LIQUID, VISCOUS, LEAD_CONTAINER);
    public static final FluidType CHLOROCALCITE_SOLUTION = register("CHLOROCALCITE_SOLUTION", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType CHLOROCALCITE_MIX = register("CHLOROCALCITE_MIX", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType CHLOROCALCITE_CLEANED = register("CHLOROCALCITE_CLEANED", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType POTASSIUM_CHLORIDE = register("POTASSIUM_CHLORIDE", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType CALCIUM_CHLORIDE = register("CALCIUM_CHLORIDE", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType CALCIUM_SOLUTION = register("CALCIUM_SOLUTION", 0x808080, 0, 0, 0, FluidSymbol.NONE, LIQUID, NO_CONTAINER);
    public static final FluidType SMOKE = register("SMOKE", 0x808080, 0, 0, 0, FluidSymbol.NONE, GASEOUS, NO_ID, NO_CONTAINER);
    public static final FluidType SMOKE_LEADED = register("SMOKE_LEADED", 0x808080, 0, 0, 0, FluidSymbol.NONE, GASEOUS, NO_ID, NO_CONTAINER);
    public static final FluidType SMOKE_POISON = register("SMOKE_POISON", 0x808080, 0, 0, 0, FluidSymbol.NONE, GASEOUS, NO_ID, NO_CONTAINER);
    public static final FluidType HELIUM4 = register("HELIUM4", 0xE54B0A, 0, 0, 0, FluidSymbol.ASPHYXIANT, GASEOUS);
    public static final FluidType HEAVYWATER_HOT = register("HEAVYWATER_HOT", 0x4D007B, 1, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(600);
    public static final FluidType SODIUM = register("SODIUM", 0xCCD4D5, 1, 2, 3, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(400);
    public static final FluidType SODIUM_HOT = register("SODIUM_HOT", 0xE2ADC1, 1, 2, 3, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(1200);
    public static final FluidType THORIUM_SALT = register("THORIUM_SALT", 0x7A5542, 2, 0, 3, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(800);
    public static final FluidType THORIUM_SALT_HOT = register("THORIUM_SALT_HOT", 0x3E3627, 2, 0, 3, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(1600);
    public static final FluidType THORIUM_SALT_DEPLETED = register("THORIUM_SALT_DEPLETED", 0x302D1C, 2, 0, 3, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(800);
    public static final FluidType FULLERENE = register("FULLERENE", 0xFF7FED, 3, 3, 3, FluidSymbol.NONE, LIQUID);
    public static final FluidType PHEROMONE = register("PHEROMONE", 0x5FA6E8, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType PHEROMONE_M = register("PHEROMONE_M", 0x48C9B0, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType OIL_DS = register("OIL_DS", 0x121212, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HOTOIL_DS = register("HOTOIL_DS", 0x3F180F, 2, 3, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(350);
    public static final FluidType CRACKOIL_DS = register("CRACKOIL_DS", 0x2A1C11, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType HOTCRACKOIL_DS = register("HOTCRACKOIL_DS", 0x3A1A28, 2, 3, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(350);
    public static final FluidType NAPHTHA_DS = register("NAPHTHA_DS", 0x63614E, 2, 1, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType LIGHTOIL_DS = register("LIGHTOIL_DS", 0x63543E, 1, 2, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType STELLAR_FLUX = register("STELLAR_FLUX", 0xE300FF, 0, 4, 4, FluidSymbol.ANTIMATTER, ANTIMATTER, GASEOUS);
    public static final FluidType VITRIOL = register("VITRIOL", 0x6E5222, 2, 0, 1, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType SLOP = register("SLOP", 0x929D45, 0, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS);
    public static final FluidType LEAD = register("LEAD", 0x666672, 4, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(350);
    public static final FluidType LEAD_HOT = register("LEAD_HOT", 0x776563, 4, 0, 0, FluidSymbol.NONE, LIQUID, VISCOUS).setTemperature(1500);
    public static final FluidType PERFLUOROMETHYL = register("PERFLUOROMETHYL", 0xBDC8DC, 1, 0, 1, FluidSymbol.NONE, LIQUID).setTemperature(15);
    public static final FluidType PERFLUOROMETHYL_COLD = register("PERFLUOROMETHYL_COLD", 0x99DADE, 1, 0, 1, FluidSymbol.NONE, LIQUID).setTemperature(-150);
    public static final FluidType PERFLUOROMETHYL_HOT = register("PERFLUOROMETHYL_HOT", 0xB899DE, 1, 0, 1, FluidSymbol.NONE, LIQUID).setTemperature(250);
    public static final FluidType LYE = register("LYE", 0xFFECCC, 3, 0, 1, FluidSymbol.ACID, LIQUID);
    public static final FluidType SODIUM_ALUMINATE = register("SODIUM_ALUMINATE", 0xFFD191, 3, 0, 1, FluidSymbol.ACID, LIQUID);
    public static final FluidType BAUXITE_SOLUTION = register("BAUXITE_SOLUTION", 0xE2560F, 3, 0, 3, FluidSymbol.ACID, LIQUID, VISCOUS);
    public static final FluidType ALUMINA = register("ALUMINA", 0xDDFFFF, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType AIR = register("AIR", 0xE7EAEB, 0, 0, 0, FluidSymbol.NONE, GASEOUS);
    public static final FluidType CONCRETE = register("CONCRETE", 0xA2A2A2, 0, 0, 0, FluidSymbol.NONE, LIQUID);
    public static final FluidType DHC = register("DHC", 0xD2AFFF, 0, 0, 0, FluidSymbol.NONE, GASEOUS);

    private static FluidType register(String name, int color, int poison, int flammability, int reactivity, FluidSymbol symbol, FluidTrait... traits) {
        FluidType type = new FluidType(nextId++, name, color, poison, flammability, reactivity, symbol);
        type.addTraits(traits);
        BY_ID.put(type.getId(), type);
        BY_NAME.put(normalize(name), type);
        return type;
    }

    private static FluidType alias(String name, FluidType type) {
        BY_NAME.put(normalize(name), type);
        return type;
    }

    public static void bootstrap() {
        HbmFluidForgeMappings.bootstrap();
    }

    public static FluidType fromId(int id) {
        return BY_ID.getOrDefault(id, NONE);
    }

    public static FluidType fromName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        return BY_NAME.getOrDefault(normalize(name), NONE);
    }

    public static Collection<FluidType> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    private static String normalize(String name) {
        return name.toUpperCase(Locale.US);
    }

    private HbmFluids() {
    }
}
