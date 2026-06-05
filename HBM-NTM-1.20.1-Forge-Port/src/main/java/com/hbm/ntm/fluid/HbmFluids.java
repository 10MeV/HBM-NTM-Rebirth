package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.FluidTrait;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait.FuelGrade;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait.CoolingType;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.fluid.trait.PheromoneFluidTrait;
import com.hbm.ntm.fluid.trait.PoisonFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.fluid.trait.PwrModeratorFluidTrait;
import com.hbm.ntm.fluid.trait.ToxinFluidTrait;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

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
    public static final FluidType FLUE = register("FLUE", 0x131313, 1, 4, 1, FluidSymbol.NONE, GASEOUS);
    public static final FluidType AIRBLAST = register("AIRBLAST", 0xFFDADA, 0, 3, 0, FluidSymbol.NONE, GASEOUS).setTemperature(1200);

    static {
        registerLegacyBehaviorTraits();
    }

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

    public static List<FluidType> niceOrder() {
        List<FluidType> result = new ArrayList<>();
        for (String name : NICE_ORDER_NAMES) {
            FluidType type = BY_NAME.get(normalize(name));
            if (type != null && !result.contains(type)) {
                result.add(type);
            }
        }
        for (FluidType type : BY_ID.values()) {
            if (!result.contains(type)) {
                result.add(type);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static void registerLegacyBehaviorTraits() {
        addLegacyContainerTraits();
        addCorrosionAndSpecialTraits();
        addLegacyToxinTraits();
        addLegacyFuelTraits();
        addThermalTraits();
    }

    private static void addLegacyContainerTraits() {
        addGasTank(DEUTERIUM, 0x0000FF, 0xFFFFFF);
        addGasTank(TRITIUM, 0x000099, 0xE9FFAA);
        addGasTank(FLUE, 0xFF4545, 0xFFE97F);
        addCanister(OIL, 0x424242);
        addCanister(HEAVYOIL, 0x513F39);
        addCanister(BITUMEN, 0x5A5877);
        addCanister(SMEAR, 0x624F3B);
        addCanister(HEATINGOIL, 0x694235);
        addCanister(RECLAIMED, 0xF65723);
        addCanister(PETROIL, 0x2369F6);
        addCanister(LUBRICANT, 0xF1CC05);
        addCanister(NAPHTHA, 0x5F6D44);
        addCanister(DIESEL, 0xFF2C2C);
        addCanister(LIGHTOIL, 0xB46B52);
        addCanister(KEROSENE, 0xFF377D);
        addGasTank(GAS, 0xFF4545, 0xFFE97F);
        addGasTank(PETROLEUM, 0x5E7CFF, 0xFFE97F);
        addGasTank(BIOGAS, 0xC8FF1F, 0x303030);
        addCanister(BIOFUEL, 0x9EB623);
        addCanister(NITAN, 0x6B238C);
        addGasTank(HYDROGEN, 0x4286F4, 0xFFFFFF);
        addGasTank(OXYGEN, 0x98BDF9, 0xFFFFFF);
        addGasTank(XENON, 0x8C21FF, 0x303030);
        addCanister(GASOLINE, 0x2F7747);
        addCanister(COALGAS, 0x2E155F);
        addCanister(FRACKSOL, 0x4F887F);
        addGasTank(HELIUM3, 0xFD631F, 0xFFFFFF);
        addCanister(ETHANOL, 0xEAFFF3);
        addCanister(CRACKOIL, 0x424242);
        addCanister(COALOIL, 0x424242);
        addCanister(NAPHTHA_CRACK, 0x5F6D44);
        addCanister(LIGHTOIL_CRACK, 0xB46B52);
        addCanister(DIESEL_CRACK, 0xFF2C2C);
        addGasTank(AROMATICS, 0x68A09A, 0xEDCF27);
        addGasTank(UNSATURATEDS, 0x628FAE, 0xEDCF27);
        addCanister(PETROIL_LEADED, 0x2331F6);
        addCanister(GASOLINE_LEADED, 0x2F775A);
        addCanister(COALGAS_LEADED, 0x1E155F);
        addCanister(WOODOIL, 0xBF7E4F);
        addCanister(COALCREOSOTE, 0x285A3F);
        addCanister(SEEDSLURRY, 0x7CC35E);
        addCanister(SOLVENT, 0xE4E3EF);
        addGasTank(SYNGAS, 0xFFFFFF, 0x131313);
        addGasTank(CHLORINE, 0xBAB572, 0x887B34);
        addCanister(HEAVYOIL_VACUUM, 0x513F39);
        addCanister(REFORMATE, 0xD180D6);
        addCanister(LIGHTOIL_VACUUM, 0xB46B52);
        addGasTank(SOURGAS, 0xC9BE0D, 0x303030);
        addCanister(XYLENE, 0xA380D6);
        addCanister(HEATINGOIL_VACUUM, 0x694235);
        addCanister(DIESEL_REFORM, 0xFFC500);
        addCanister(DIESEL_CRACK_REFORM, 0xFFC500);
        addCanister(KEROSENE_REFORM, 0xFF377D);
        addGasTank(REFORMGAS, 0x9392FF, 0xFFB992);
        addGasTank(PHOSGENE, 0xCFC4A4, 0x361414);
        addGasTank(MUSTARDGAS, 0xBAB572, 0x361414);
        addGasTank(HELIUM4, 0xFD631F, 0xFFFF00);
        addCanister(OIL_DS, 0x424242);
        addCanister(CRACKOIL_DS, 0x424242);
        addCanister(NAPHTHA_DS, 0x5F6D44);
        addCanister(LIGHTOIL_DS, 0xB46B52);
    }

    private static void addCanister(FluidType type, int color) {
        container(type).withCanister(color);
    }

    private static void addGasTank(FluidType type, int bottleColor, int labelColor) {
        container(type).withGasTank(bottleColor, labelColor);
    }

    private static ContainerFluidTrait container(FluidType type) {
        ContainerFluidTrait trait = type.getTrait(ContainerFluidTrait.class);
        if (trait == null) {
            trait = new ContainerFluidTrait();
            type.addTraits(trait);
        }
        return trait;
    }

    private static void addCorrosionAndSpecialTraits() {
        UF6.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(0.2F), new CorrosiveFluidTrait(15));
        PUF6.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(0.1F), new CorrosiveFluidTrait(15));
        SAS3.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(1.0F), new CorrosiveFluidTrait(30));
        SCHRABIDIC.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(1.0F), new CorrosiveFluidTrait(75), new PoisonFluidTrait(true, 2));
        PEROXIDE.addTraits(new CorrosiveFluidTrait(40));
        WATZ.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(0.1F), new CorrosiveFluidTrait(60), pollutingOil().release(PollutionKind.POISON, poisonExtreme()));
        BALEFIRE.addTraits(new CorrosiveFluidTrait(50));
        MERCURY.addTraits(new PoisonFluidTrait(false, 2));
        PAIN.addTraits(new CorrosiveFluidTrait(30), new PoisonFluidTrait(true, 2));
        WASTEFLUID.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(0.5F));
        WASTEGAS.addTraits(new com.hbm.ntm.fluid.trait.VentRadiationFluidTrait(0.5F));
        FRACKSOL.addTraits(new CorrosiveFluidTrait(15), new PoisonFluidTrait(false, 0));
        DEATH.addTraits(new CorrosiveFluidTrait(80), new PoisonFluidTrait(true, 4));
        SULFURIC_ACID.addTraits(new CorrosiveFluidTrait(50));
        NITRIC_ACID.addTraits(new CorrosiveFluidTrait(60), new PollutingFluidTrait().release(PollutionKind.POISON, poisonExtreme()));
        SOLVENT.addTraits(new CorrosiveFluidTrait(30));
        RADIOSOLVENT.addTraits(new CorrosiveFluidTrait(50));
        CHLORINE.addTraits(new CorrosiveFluidTrait(25));
        SOURGAS.addTraits(new CorrosiveFluidTrait(10), new PoisonFluidTrait(false, 1));
        PHOSGENE.addTraits(new PollutingFluidTrait().release(PollutionKind.POISON, poisonExtreme()));
        MUSTARDGAS.addTraits(new PollutingFluidTrait().release(PollutionKind.POISON, poisonExtreme()));
        REDMUD.addTraits(new CorrosiveFluidTrait(60), new PollutingFluidTrait().release(PollutionKind.POISON, poisonExtreme()));
        CHLOROCALCITE_SOLUTION.addTraits(new CorrosiveFluidTrait(60));
        CHLOROCALCITE_MIX.addTraits(new CorrosiveFluidTrait(60));
        CHLOROCALCITE_CLEANED.addTraits(new CorrosiveFluidTrait(60));
        POTASSIUM_CHLORIDE.addTraits(new CorrosiveFluidTrait(60));
        CALCIUM_CHLORIDE.addTraits(new CorrosiveFluidTrait(60));
        CALCIUM_SOLUTION.addTraits(new CorrosiveFluidTrait(60));
        THORIUM_SALT.addTraits(new CorrosiveFluidTrait(65));
        THORIUM_SALT_HOT.addTraits(new CorrosiveFluidTrait(65));
        THORIUM_SALT_DEPLETED.addTraits(new CorrosiveFluidTrait(65));
        FULLERENE.addTraits(new CorrosiveFluidTrait(65), new PollutingFluidTrait().release(PollutionKind.POISON, poisonMinor()));
        PHEROMONE.addTraits(new PheromoneFluidTrait(1));
        PHEROMONE_M.addTraits(new PheromoneFluidTrait(2));
        LYE.addTraits(new CorrosiveFluidTrait(40));
        SODIUM_ALUMINATE.addTraits(new CorrosiveFluidTrait(30));
        BAUXITE_SOLUTION.addTraits(new CorrosiveFluidTrait(40));
        CARBONDIOXIDE.addTraits(new PollutingFluidTrait().release(PollutionKind.POISON, poisonMinor()));
    }

    private static void addLegacyToxinTraits() {
        CHLORINE.addTraits(new ToxinFluidTrait()
                .addEntry(new ToxinFluidTrait.DirectDamage(hbm("cloud"), 2.0F, 20, HazardClass.GAS_LUNG, false)));
        PHOSGENE.addTraits(new ToxinFluidTrait()
                .addEntry(new ToxinFluidTrait.DirectDamage(hbm("cloud"), 4.0F, 20, HazardClass.GAS_LUNG, false)));
        MUSTARDGAS.addTraits(new ToxinFluidTrait()
                .addEntry(new ToxinFluidTrait.DirectDamage(hbm("cloud"), 4.0F, 10, HazardClass.GAS_BLISTERING, false))
                .addEntry(new ToxinFluidTrait.EffectApplication(HazardClass.GAS_BLISTERING, true)
                        .addEffect(mc("wither"), 100, 1, false)
                        .addEffect(mc("nausea"), 100, 0, false)));
        ESTRADIOL.addTraits(new ToxinFluidTrait()
                .addEntry(new ToxinFluidTrait.EffectApplication(HazardClass.PARTICLE_FINE, false)
                        .addEffect(hbm("death"), 60 * 60 * 20, 0, false)));
        REDMUD.addTraits(new ToxinFluidTrait()
                .addEntry(new ToxinFluidTrait.EffectApplication(HazardClass.GAS_BLISTERING, false)
                        .addEffect(mc("wither"), 30 * 20, 2, false)));
    }

    private static void addLegacyFuelTraits() {
        long baseline = 100_000L;
        double demandVeryLow = 0.125D;
        double demandLow = 0.25D;
        double demandMedium = 0.5D;
        double demandHigh = 1.0D;
        double flammabilityLow = 0.5D;
        double flammabilityNormal = 1.0D;
        double flammabilityHigh = 2.0D;
        double complexityRefinery = 1.1D;
        double complexityFraction = 1.05D;
        double complexityCracking = 1.25D;
        double complexityCoker = 1.25D;
        double complexityChemplant = 1.1D;
        double complexityLubed = 1.15D;
        double complexityLeaded = 1.5D;
        double complexityVacuum = 3.0D;
        double complexityReform = 2.5D;
        double complexityHydro = 2.0D;

        registerCalculatedFuel(OIL, baseline * flammabilityLow * demandLow, 0.0D, null, pollutingOil());
        registerCalculatedFuel(OIL_DS, baseline * flammabilityLow * demandLow * complexityHydro, 0.0D, null, pollutingOil());
        registerCalculatedFuel(CRACKOIL, baseline * flammabilityLow * demandLow * complexityCracking, 0.0D, null, pollutingOil());
        registerCalculatedFuel(CRACKOIL_DS, baseline * flammabilityLow * demandLow * complexityCracking * complexityHydro, 0.0D, null, pollutingOil());
        registerCalculatedFuel(OIL_COKER, baseline * flammabilityLow * demandLow * complexityCoker, 0.0D, null, pollutingOil());
        registerCalculatedFuel(GAS, baseline * flammabilityNormal * demandVeryLow, 1.5D, FuelGrade.GAS, pollutingGas());
        registerCalculatedFuel(GAS_COKER, baseline * flammabilityNormal * demandVeryLow * complexityCoker, 1.5D, FuelGrade.GAS, pollutingGas());
        registerCalculatedFuel(HEAVYOIL, baseline / 0.5D * flammabilityLow * demandLow * complexityRefinery, 1.25D, FuelGrade.LOW, pollutingOil());
        registerCalculatedFuel(SMEAR, baseline / 0.35D * flammabilityLow * demandLow * complexityRefinery * complexityFraction, 1.25D, FuelGrade.LOW, pollutingOil());
        registerCalculatedFuel(RECLAIMED, baseline / 0.28D * flammabilityLow * demandLow * complexityRefinery * complexityFraction * complexityChemplant, 1.25D, FuelGrade.LOW, pollutingFuel());
        registerCalculatedFuel(PETROIL, baseline / 0.28D * flammabilityLow * demandLow * complexityRefinery * complexityFraction * complexityChemplant * complexityLubed, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(PETROIL_LEADED, baseline / 0.28D * flammabilityLow * demandLow * complexityRefinery * complexityFraction * complexityChemplant * complexityLubed * complexityLeaded, 1.5D, FuelGrade.MEDIUM, pollutingLeadedFuel());
        registerCalculatedFuel(HEATINGOIL, baseline / 0.31D * flammabilityNormal * demandLow * complexityRefinery * complexityFraction * complexityFraction, 1.25D, FuelGrade.LOW, pollutingOil());
        registerCalculatedFuel(NAPHTHA, baseline / 0.25D * flammabilityLow * demandLow * complexityRefinery, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(NAPHTHA_DS, baseline / 0.25D * flammabilityLow * demandLow * complexityRefinery * complexityHydro, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(NAPHTHA_CRACK, baseline / 0.40D * flammabilityLow * demandLow * complexityRefinery * complexityCracking, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(NAPHTHA_COKER, baseline / 0.25D * flammabilityLow * demandLow * complexityCoker, 1.5D, FuelGrade.MEDIUM, pollutingOil());
        registerCalculatedFuel(GASOLINE, baseline / 0.20D * flammabilityNormal * demandLow * complexityRefinery * complexityChemplant, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(GASOLINE_LEADED, baseline / 0.20D * flammabilityNormal * demandLow * complexityRefinery * complexityChemplant * complexityLeaded, 2.5D, FuelGrade.HIGH, pollutingLeadedFuel());
        registerCalculatedFuel(DIESEL, baseline / 0.21D * flammabilityNormal * demandLow * complexityRefinery * complexityFraction, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(DIESEL_CRACK, baseline / 0.28D * flammabilityNormal * demandLow * complexityRefinery * complexityCracking * complexityFraction, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(LIGHTOIL, baseline / 0.15D * flammabilityNormal * demandHigh * complexityRefinery, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(LIGHTOIL_DS, baseline / 0.15D * flammabilityNormal * demandHigh * complexityRefinery * complexityHydro, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(LIGHTOIL_CRACK, baseline / 0.30D * flammabilityNormal * demandHigh * complexityRefinery * complexityCracking, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(KEROSENE, baseline / 0.09D * flammabilityNormal * demandHigh * complexityRefinery * complexityFraction, 1.5D, FuelGrade.AERO, pollutingFuel());
        registerCalculatedFuel(PETROLEUM, baseline / 0.10D * flammabilityNormal * demandMedium * complexityRefinery, 1.5D, FuelGrade.GAS, pollutingGas());
        registerCalculatedFuel(AROMATICS, baseline / 0.15D * flammabilityLow * demandHigh * complexityRefinery * complexityCracking, 0.0D, null, pollutingGas());
        registerCalculatedFuel(UNSATURATEDS, baseline / 0.15D * flammabilityHigh * demandHigh * complexityRefinery * complexityCracking, 0.0D, null, pollutingGas());
        registerCalculatedFuel(LPG, baseline / 0.1D * flammabilityNormal * demandMedium * complexityRefinery * complexityChemplant, 2.5D, FuelGrade.HIGH, pollutingLiquidGas());
        registerCalculatedFuel(NITAN, heatEnergy(KEROSENE) * 25.0D, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(BALEFIRE, heatEnergy(KEROSENE) * 100.0D, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(HEAVYOIL_VACUUM, baseline / 0.4D * flammabilityLow * demandLow * complexityVacuum, 1.25D, FuelGrade.LOW, pollutingOil());
        registerCalculatedFuel(REFORMATE, baseline / 0.25D * flammabilityNormal * demandHigh * complexityVacuum, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(LIGHTOIL_VACUUM, baseline / 0.20D * flammabilityNormal * demandHigh * complexityVacuum, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(SOURGAS, baseline / 0.15D * flammabilityLow * demandVeryLow * complexityVacuum, 0.0D, null, pollutingGas());
        registerCalculatedFuel(XYLENE, baseline / 0.15D * flammabilityNormal * demandMedium * complexityVacuum * complexityFraction, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(HEATINGOIL_VACUUM, baseline / 0.24D * flammabilityNormal * demandLow * complexityVacuum * complexityFraction, 1.25D, FuelGrade.LOW, pollutingOil());
        registerCalculatedFuel(DIESEL_REFORM, heatEnergy(DIESEL) * complexityReform, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(DIESEL_CRACK_REFORM, heatEnergy(DIESEL_CRACK) * complexityReform, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(KEROSENE_REFORM, heatEnergy(KEROSENE) * complexityReform, 1.5D, FuelGrade.AERO, pollutingFuel());
        registerCalculatedFuel(REFORMGAS, baseline / 0.06D * flammabilityHigh * demandLow * complexityVacuum * complexityFraction, 1.5D, FuelGrade.GAS, pollutingGas());

        int coalHeat = 400_000;
        registerCalculatedFuel(COALOIL, coalHeat * (1000.0D / 100.0D) * flammabilityLow * demandLow * complexityChemplant, 0.0D, null, pollutingOil());
        long coaloil = heatEnergy(COALOIL);
        registerCalculatedFuel(COALGAS, coaloil / 0.3D * flammabilityNormal * demandMedium * complexityChemplant * complexityFraction, 1.5D, FuelGrade.MEDIUM, pollutingFuel());
        registerCalculatedFuel(COALGAS_LEADED, coaloil / 0.3D * flammabilityNormal * demandMedium * complexityChemplant * complexityFraction * complexityLeaded, 1.5D, FuelGrade.MEDIUM, pollutingLeadedFuel());
        FLUE.addTraits(new FlammableFluidTrait(10_000),
                new PollutingFluidTrait().burn(PollutionKind.SOOT, sootGas()).release(PollutionKind.SOOT, sootGas() * 25.0F));
        registerCalculatedFuel(ETHANOL, 275_000.0D, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(BIOGAS, 250_000.0D * flammabilityLow, 1.25D, FuelGrade.GAS, pollutingGas());
        registerCalculatedFuel(BIOFUEL, 500_000.0D, 2.5D, FuelGrade.HIGH, pollutingFuel());
        registerCalculatedFuel(WOODOIL, 110_000.0D, 0.0D, null, pollutingOil());
        registerCalculatedFuel(COALCREOSOTE, 250_000.0D, 0.0D, null, pollutingOil());
        registerCalculatedFuel(FISHOIL, 75_000.0D, 0.0D, null, pollutingFuel());
        registerCalculatedFuel(SUNFLOWEROIL, 50_000.0D, 0.0D, null, pollutingFuel());
        registerCalculatedFuel(SOLVENT, 100_000.0D, 0.0D, null, null);
        registerCalculatedFuel(RADIOSOLVENT, 150_000.0D, 0.0D, null, null);
        registerCalculatedFuel(SYNGAS, coalHeat * (1000.0D / 100.0D) * flammabilityLow * demandLow * complexityChemplant * 1.5D, 1.25D, FuelGrade.GAS, null);
        registerCalculatedFuel(OXYHYDROGEN, 5_000.0D, 3.0D, FuelGrade.GAS, null);
    }

    private static void addThermalTraits() {
        WATER.addTraits(new HeatableFluidTrait()
                .setEfficiency(HeatingType.BOILER, 1.0D)
                .setEfficiency(HeatingType.HEATEXCHANGER, 0.25D)
                .addStep(200, 1, STEAM, 100)
                .addStep(220, 1, HOTSTEAM, 10)
                .addStep(238, 1, SUPERHOTSTEAM, 1)
                .addStep(2500, 10, ULTRAHOTSTEAM, 1));

        STEAM.addTraits(new CoolableFluidTrait(SPENTSTEAM, 100, 1, 200).setEfficiency(CoolingType.TURBINE, 1.0D).setEfficiency(CoolingType.HEATEXCHANGER, 0.5D));
        HOTSTEAM.addTraits(new CoolableFluidTrait(STEAM, 1, 10, 2).setEfficiency(CoolingType.TURBINE, 1.0D).setEfficiency(CoolingType.HEATEXCHANGER, 0.5D));
        SUPERHOTSTEAM.addTraits(new CoolableFluidTrait(HOTSTEAM, 1, 10, 18).setEfficiency(CoolingType.TURBINE, 1.0D).setEfficiency(CoolingType.HEATEXCHANGER, 0.5D));
        ULTRAHOTSTEAM.addTraits(new CoolableFluidTrait(SUPERHOTSTEAM, 1, 10, 120).setEfficiency(CoolingType.TURBINE, 1.0D).setEfficiency(CoolingType.HEATEXCHANGER, 0.5D));

        AIR.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.BOILER, 1.0D).addStep(5, 1, AIRBLAST, 1));
        addHeatPair(OIL, HOTOIL, 10, 1, 1, 1, HeatingType.BOILER, HeatingType.HEATEXCHANGER);
        addHeatPair(OIL_DS, HOTOIL_DS, 10, 1, 1, 1, HeatingType.BOILER, HeatingType.HEATEXCHANGER);
        addHeatPair(CRACKOIL, HOTCRACKOIL, 10, 1, 1, 1, HeatingType.BOILER, HeatingType.HEATEXCHANGER);
        addHeatPair(CRACKOIL_DS, HOTCRACKOIL_DS, 10, 1, 1, 1, HeatingType.BOILER, HeatingType.HEATEXCHANGER);
        addCool(HOTOIL, OIL, 1, 1, 10, CoolingType.HEATEXCHANGER);
        addCool(HOTOIL_DS, OIL_DS, 1, 1, 10, CoolingType.HEATEXCHANGER);
        addCool(HOTCRACKOIL, CRACKOIL, 1, 1, 10, CoolingType.HEATEXCHANGER);
        addCool(HOTCRACKOIL_DS, CRACKOIL_DS, 1, 1, 10, CoolingType.HEATEXCHANGER);

        COOLANT.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.HEATEXCHANGER, 1.0D).setEfficiency(HeatingType.PWR, 1.0D).setEfficiency(HeatingType.ICF, 1.0D).addStep(300, 1, COOLANT_HOT, 1));
        addCool(COOLANT_HOT, COOLANT, 1, 1, 300, CoolingType.HEATEXCHANGER);
        PERFLUOROMETHYL_COLD.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.PA, 1.0D).addStep(300, 1, PERFLUOROMETHYL, 1));
        PERFLUOROMETHYL.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.HEATEXCHANGER, 1.0D).setEfficiency(HeatingType.PWR, 1.0D).setEfficiency(HeatingType.ICF, 1.0D).addStep(300, 1, PERFLUOROMETHYL_HOT, 1));
        addCool(PERFLUOROMETHYL_HOT, PERFLUOROMETHYL, 1, 1, 300, CoolingType.HEATEXCHANGER);
        MUG.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.HEATEXCHANGER, 1.0D).setEfficiency(HeatingType.PWR, 1.0D).setEfficiency(HeatingType.ICF, 1.25D).addStep(400, 1, MUG_HOT, 1), new PwrModeratorFluidTrait(1.15D));
        addCool(MUG_HOT, MUG, 1, 1, 400, CoolingType.HEATEXCHANGER);
        BLOOD.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.HEATEXCHANGER, 1.0D).setEfficiency(HeatingType.ICF, 1.25D).addStep(500, 1, BLOOD_HOT, 1));
        addCool(BLOOD_HOT, BLOOD, 1, 1, 500, CoolingType.HEATEXCHANGER);
        HEAVYWATER.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.PWR, 1.0D).addStep(300, 1, HEAVYWATER_HOT, 1), new PwrModeratorFluidTrait(1.25D));
        addCool(HEAVYWATER_HOT, HEAVYWATER, 1, 1, 300, CoolingType.HEATEXCHANGER);
        SODIUM.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.PWR, 2.5D).setEfficiency(HeatingType.ICF, 3.0D).addStep(400, 1, SODIUM_HOT, 1));
        addCool(SODIUM_HOT, SODIUM, 1, 1, 400, CoolingType.HEATEXCHANGER);
        LEAD.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.PWR, 0.75D).setEfficiency(HeatingType.ICF, 4.0D).addStep(800, 1, LEAD_HOT, 1), new PwrModeratorFluidTrait(0.75D));
        addCool(LEAD_HOT, LEAD, 1, 1, 680, CoolingType.HEATEXCHANGER);
        THORIUM_SALT.addTraits(new HeatableFluidTrait().setEfficiency(HeatingType.PWR, 1.0D).addStep(400, 1, THORIUM_SALT_HOT, 1), new PwrModeratorFluidTrait(2.5D));
        addCool(THORIUM_SALT_HOT, THORIUM_SALT_DEPLETED, 1, 1, 400, CoolingType.HEATEXCHANGER);
    }

    private static void addHeatPair(FluidType cold, FluidType hot, int heat, int amountRequired, int amountProduced, double efficiency, HeatingType... heatingTypes) {
        HeatableFluidTrait trait = new HeatableFluidTrait().addStep(heat, amountRequired, hot, amountProduced);
        for (HeatingType type : heatingTypes) {
            trait.setEfficiency(type, efficiency);
        }
        cold.addTraits(trait);
    }

    private static void addCool(FluidType hot, FluidType cold, int amountRequired, int amountProduced, int heat, CoolingType... coolingTypes) {
        CoolableFluidTrait trait = new CoolableFluidTrait(cold, amountRequired, amountProduced, heat);
        for (CoolingType type : coolingTypes) {
            trait.setEfficiency(type, 1.0D);
        }
        hot.addTraits(trait);
    }

    private static void registerCalculatedFuel(FluidType type, double base, double combustibleMultiplier, FuelGrade grade, FluidTrait pollution) {
        long flammable = round((long) base);
        long combustible = round((long) (base * combustibleMultiplier));
        type.addTraits(new FlammableFluidTrait(flammable));
        if (combustible > 0 && grade != null) {
            type.addTraits(new CombustibleFluidTrait(grade, combustible));
        }
        if (pollution != null) {
            type.addTraits(pollution);
        }
    }

    private static long heatEnergy(FluidType type) {
        FlammableFluidTrait trait = type.getTrait(FlammableFluidTrait.class);
        return trait == null ? 0L : trait.getHeatEnergyPerBucket();
    }

    private static long round(long value) {
        if (value > 10_000_000L) {
            return value - value % 100_000L;
        }
        if (value > 1_000_000L) {
            return value - value % 10_000L;
        }
        if (value > 100_000L) {
            return value - value % 1_000L;
        }
        if (value > 10_000L) {
            return value - value % 100L;
        }
        if (value > 1_000L) {
            return value - value % 10L;
        }
        return value;
    }

    private static PollutingFluidTrait pollutingOil() {
        return new PollutingFluidTrait().burn(PollutionKind.SOOT, sootUnrefinedOil()).release(PollutionKind.POISON, poisonOil());
    }

    private static PollutingFluidTrait pollutingFuel() {
        return new PollutingFluidTrait().burn(PollutionKind.SOOT, sootRefinedOil()).release(PollutionKind.POISON, poisonOil());
    }

    private static PollutingFluidTrait pollutingLeadedFuel() {
        return new PollutingFluidTrait()
                .burn(PollutionKind.SOOT, sootRefinedOil())
                .burn(PollutionKind.HEAVY_METAL, leadFuel())
                .release(PollutionKind.POISON, poisonOil())
                .release(PollutionKind.HEAVY_METAL, leadFuel() * 0.1F);
    }

    private static PollutingFluidTrait pollutingGas() {
        return new PollutingFluidTrait().burn(PollutionKind.SOOT, sootGas());
    }

    private static PollutingFluidTrait pollutingLiquidGas() {
        return new PollutingFluidTrait().burn(PollutionKind.SOOT, sootGas() * 2.0F);
    }

    private static float sootUnrefinedOil() {
        return sootPerSecond() * 0.1F;
    }

    private static float sootRefinedOil() {
        return sootPerSecond() * 0.025F;
    }

    private static float sootGas() {
        return sootPerSecond() * 0.005F;
    }

    private static float leadFuel() {
        return heavyMetalPerSecond() * 0.025F;
    }

    private static float poisonOil() {
        return poisonPerSecond() * 0.0025F;
    }

    private static float poisonExtreme() {
        return poisonPerSecond() * 0.025F;
    }

    private static float poisonMinor() {
        return poisonPerSecond() * 0.001F;
    }

    private static float sootPerSecond() {
        return 1.0F / 25.0F;
    }

    private static float heavyMetalPerSecond() {
        return 1.0F / 50.0F;
    }

    private static float poisonPerSecond() {
        return 1.0F / 50.0F;
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static final List<String> NICE_ORDER_NAMES = List.of(
            "NONE", "AIR", "AIRBLAST", "WATER", "HEAVYWATER", "HEAVYWATER_HOT", "LAVA",
            "STEAM", "HOTSTEAM", "SUPERHOTSTEAM", "ULTRAHOTSTEAM", "SPENTSTEAM",
            "CARBONDIOXIDE", "COOLANT", "COOLANT_HOT", "PERFLUOROMETHYL", "PERFLUOROMETHYL_COLD",
            "PERFLUOROMETHYL_HOT", "CRYOGEL", "MUG", "MUG_HOT", "BLOOD", "BLOOD_HOT",
            "SODIUM", "SODIUM_HOT", "LEAD", "LEAD_HOT", "THORIUM_SALT", "THORIUM_SALT_HOT",
            "THORIUM_SALT_DEPLETED", "HYDROGEN", "DEUTERIUM", "TRITIUM", "HELIUM3", "HELIUM4",
            "OXYGEN", "XENON", "CHLORINE", "MERCURY", "OIL", "OIL_DS", "CRACKOIL",
            "CRACKOIL_DS", "COALOIL", "OIL_COKER", "HOTOIL", "HOTOIL_DS", "HOTCRACKOIL",
            "HOTCRACKOIL_DS", "HEAVYOIL", "HEAVYOIL_VACUUM", "NAPHTHA", "NAPHTHA_DS",
            "NAPHTHA_CRACK", "NAPHTHA_COKER", "REFORMATE", "LIGHTOIL", "LIGHTOIL_DS",
            "LIGHTOIL_CRACK", "LIGHTOIL_VACUUM", "BITUMEN", "SMEAR", "HEATINGOIL",
            "HEATINGOIL_VACUUM", "RECLAIMED", "LUBRICANT", "GAS", "GAS_COKER", "PETROLEUM",
            "SOURGAS", "FLUE", "LPG", "SYNGAS", "OXYHYDROGEN", "AROMATICS", "UNSATURATEDS", "XYLENE",
            "REFORMGAS", "DIESEL", "DIESEL_REFORM", "DIESEL_CRACK", "DIESEL_CRACK_REFORM",
            "KEROSENE", "KEROSENE_REFORM", "PETROIL", "PETROIL_LEADED", "GASOLINE",
            "GASOLINE_LEADED", "COALGAS", "COALGAS_LEADED", "COALCREOSOTE", "WOODOIL",
            "BIOGAS", "BIOFUEL", "ETHANOL", "FISHOIL", "SUNFLOWEROIL", "NITAN", "DHC",
            "BALEFIRE", "SALIENT", "SEEDSLURRY", "COLLOID", "VITRIOL", "SLOP", "IONGEL",
            "PEROXIDE", "SULFURIC_ACID", "NITRIC_ACID", "SOLVENT", "RADIOSOLVENT", "SCHRABIDIC",
            "UF6", "PUF6", "SAS3", "PAIN", "DEATH", "WATZ", "REDMUD", "FULLERENE", "EGG",
            "CHOLESTEROL", "CHLOROCALCITE_SOLUTION", "CHLOROCALCITE_MIX", "CHLOROCALCITE_CLEANED",
            "POTASSIUM_CHLORIDE", "CALCIUM_CHLORIDE", "CALCIUM_SOLUTION", "SODIUM_ALUMINATE",
            "BAUXITE_SOLUTION", "ALUMINA", "CONCRETE", "FRACKSOL", "LYE", "PHOSGENE",
            "MUSTARDGAS", "ESTRADIOL", "NITROGLYCERIN", "AMAT", "ASCHRAB", "WASTEFLUID",
            "WASTEGAS", "XPJUICE", "ENDERJUICE", "STELLAR_FLUX", "PLASMA_DT", "PLASMA_HD",
            "PLASMA_HT", "PLASMA_DH3", "PLASMA_XM", "PLASMA_BF", "SMOKE", "SMOKE_LEADED",
            "SMOKE_POISON", "PHEROMONE", "PHEROMONE_M");

    private static String normalize(String name) {
        return name.toUpperCase(Locale.US);
    }

    private HbmFluids() {
    }
}
