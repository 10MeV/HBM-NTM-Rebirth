package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ObjModelLibrary {
    private static final Set<ResourceLocation> MODELS = new LinkedHashSet<>();

    public static final ObjModelPart PRESS_HEAD = ObjMachineModels.PRESS_HEAD;
    public static final ObjPartModel PRESS = ObjMachineModels.PRESS;
    public static final ObjModelPart MACHINE_RADAR_SCREEN = ObjMachineModels.RADAR_SCREEN;
    public static final ObjModelPart MACHINE_SOLAR_MIRROR = ObjMachineModels.SOLAR_MIRROR;
    public static final ObjModelPart MACHINE_DRAIN = ObjMachineModels.DRAIN;
    public static final ObjModelPart MACHINE_INTAKE = ObjMachineModels.INTAKE;
    public static final ObjModelPart MACHINE_COMBINATION_OVEN = ObjMachineModels.COMBINATION_OVEN;
    public static final ObjModelPart MACHINE_RTG = ObjMachineModels.RTG;
    public static final ObjModelPart MACHINE_TELEX = ObjMachineModels.TELEX;
    public static final ObjModelPart MACHINE_FRACTION_TOWER = ObjMachineModels.FRACTION_TOWER;
    public static final ObjModelPart MACHINE_FAN = ObjMachineModels.FAN;
    public static final ObjModelPart MACHINE_FURNACE_IRON = ObjMachineModels.FURNACE_IRON;
    public static final ObjModelPart MACHINE_ELEVATOR = ObjMachineModels.ELEVATOR;
    public static final ObjModelPart MACHINE_CRUCIBLE = ObjMachineModels.CRUCIBLE;
    public static final ObjModelPart MACHINE_DRUM = ObjMachineModels.DRUM;
    public static final ObjModelPart MACHINE_FRACTION_SPACER = ObjMachineModels.FRACTION_SPACER;
    public static final ObjModelPart MACHINE_HEATING_OVEN = ObjMachineModels.HEATING_OVEN;
    public static final ObjModelPart MACHINE_CHIMNEY_BRICK = ObjMachineModels.CHIMNEY_BRICK;
    public static final ObjModelPart MACHINE_TURBINE = ObjMachineModels.TURBINE;
    public static final ObjModelPart MACHINE_DIESELGEN = ObjMachineModels.DIESELGEN;
    public static final ObjModelPart MACHINE_FIREBOX = ObjMachineModels.FIREBOX;
    public static final ObjModelPart MACHINE_OILBURNER = ObjMachineModels.OILBURNER;
    public static final ObjModelPart MACHINE_ELECTRIC_HEATER = ObjMachineModels.ELECTRIC_HEATER;
    public static final ObjModelPart MACHINE_HEATEX = ObjMachineModels.HEATEX;
    public static final ObjModelPart MACHINE_BOILER = ObjMachineModels.BOILER;
    public static final ObjModelPart MACHINE_BOILER_BURST = ObjMachineModels.BOILER_BURST;
    public static final ObjModelPart MACHINE_INDUSTRIAL_BOILER = ObjMachineModels.INDUSTRIAL_BOILER;
    public static final ObjModelPart MACHINE_HEPHAESTUS = ObjMachineModels.HEPHAESTUS;
    public static final ObjModelPart MACHINE_DERRICK = ObjMachineModels.DERRICK;
    public static final ObjModelPart MACHINE_PUMPJACK = ObjMachineModels.PUMPJACK;
    public static final ObjModelPart MACHINE_FRACKING_TOWER = ObjMachineModels.FRACKING_TOWER;
    public static final ObjModelPart MACHINE_OILFLARE = ObjMachineModels.OILFLARE;
    public static final ObjModelPart MACHINE_CHIMNEY_INDUSTRIAL = ObjMachineModels.CHIMNEY_INDUSTRIAL;
    public static final ObjModelPart MACHINE_BATTERY_REDD = ObjMachineModels.BATTERY_REDD;
    public static final ObjModelPart MACHINE_FENSU = ObjMachineModels.FENSU;
    public static final ObjPartModel MACHINE_ASSEMBLY_MACHINE = ObjMachineModels.ASSEMBLY_MACHINE;
    public static final LegacyWavefrontModel MACHINE_REFINERY = ObjMachineModels.REFINERY;
    public static final LegacyWavefrontModel MACHINE_REFINERY_EXPLODED = ObjMachineModels.REFINERY_EXPLODED;
    public static final LegacyWavefrontModel MACHINE_FLUIDTANK = ObjMachineModels.FLUIDTANK;
    public static final LegacyWavefrontModel MACHINE_FLUIDTANK_EXPLODED = ObjMachineModels.FLUIDTANK_EXPLODED;
    public static final LegacyWavefrontModel MACHINE_VACUUM_DISTILL = ObjMachineModels.VACUUM_DISTILL;
    public static final LegacyWavefrontModel MACHINE_CATALYTIC_CRACKER = ObjMachineModels.CATALYTIC_CRACKER;
    public static final LegacyWavefrontModel MACHINE_CATALYTIC_REFORMER = ObjMachineModels.CATALYTIC_REFORMER;
    public static final LegacyWavefrontModel MACHINE_HYDROTREATER = ObjMachineModels.HYDROTREATER;
    public static final LegacyWavefrontModel MACHINE_LIQUEFACTOR = ObjMachineModels.LIQUEFACTOR;
    public static final LegacyWavefrontModel MACHINE_SOLIDIFIER = ObjMachineModels.SOLIDIFIER;
    public static final LegacyWavefrontModel MACHINE_COMPRESSOR = ObjMachineModels.COMPRESSOR;
    public static final LegacyWavefrontModel MACHINE_COKER = ObjMachineModels.COKER;
    public static final LegacyWavefrontModel MACHINE_PYROOVEN = ObjMachineModels.PYROOVEN;
    public static final LegacyWavefrontModel MACHINE_BAT9000 = ObjMachineModels.BAT9000;
    public static final LegacyWavefrontModel MACHINE_BIGASSTANK = ObjMachineModels.BIGASSTANK;
    public static final LegacyWavefrontModel MACHINE_ORBUS = ObjMachineModels.ORBUS;
    public static final LegacyWavefrontModel MACHINE_TURBOFAN = ObjMachineModels.TURBOFAN;
    public static final LegacyWavefrontModel MACHINE_TURBINEGAS = ObjMachineModels.TURBINEGAS;
    public static final LegacyWavefrontModel MACHINE_STEAM_ENGINE = ObjMachineModels.STEAM_ENGINE;
    public static final LegacyWavefrontModel MACHINE_INDUSTRIAL_TURBINE = ObjMachineModels.INDUSTRIAL_TURBINE;
    public static final LegacyWavefrontModel MACHINE_CHUNGUS = ObjMachineModels.CHUNGUS;
    public static final LegacyWavefrontModel MACHINE_TOWER_SMALL = ObjMachineModels.TOWER_SMALL;
    public static final LegacyWavefrontModel MACHINE_TOWER_LARGE = ObjMachineModels.TOWER_LARGE;
    public static final LegacyWavefrontModel MACHINE_CONDENSER = ObjMachineModels.CONDENSER;
    public static final LegacyWavefrontModel MACHINE_WOOD_BURNER = ObjMachineModels.WOOD_BURNER;
    public static final LegacyWavefrontModel MACHINE_COMBUSTION_ENGINE = ObjMachineModels.COMBUSTION_ENGINE;
    public static final LegacyWavefrontModel MACHINE_PUMP = ObjMachineModels.PUMP;
    public static final LegacyWavefrontModel MACHINE_AMMO_PRESS = ObjMachineModels.AMMO_PRESS;
    public static final LegacyWavefrontModel MACHINE_ANNIHILATOR = ObjMachineModels.ANNIHILATOR;
    public static final LegacyWavefrontModel MACHINE_ASSEMBLY_FACTORY = ObjMachineModels.ASSEMBLY_FACTORY;
    public static final LegacyWavefrontModel MACHINE_CHEMICAL_PLANT = ObjMachineModels.CHEMICAL_PLANT;
    public static final LegacyWavefrontModel MACHINE_CHEMICAL_FACTORY = ObjMachineModels.CHEMICAL_FACTORY;
    public static final LegacyWavefrontModel MACHINE_PUREX = ObjMachineModels.PUREX;
    public static final LegacyWavefrontModel MACHINE_MIXER = ObjMachineModels.MIXER;
    public static final LegacyWavefrontModel MACHINE_ARC_WELDER = ObjMachineModels.ARC_WELDER;
    public static final LegacyWavefrontModel MACHINE_SOLDERING_STATION = ObjMachineModels.SOLDERING_STATION;
    public static final LegacyWavefrontModel MACHINE_ARC_FURNACE = ObjMachineModels.ARC_FURNACE;
    public static final LegacyWavefrontModel MACHINE_CENTRIFUGE = ObjMachineModels.CENTRIFUGE;
    public static final LegacyWavefrontModel MACHINE_GASCENT = ObjMachineModels.GASCENT;
    public static final LegacyWavefrontModel MACHINE_SILEX = ObjMachineModels.SILEX;
    public static final LegacyWavefrontModel MACHINE_FEL = ObjMachineModels.FEL;
    public static final LegacyWavefrontModel MACHINE_AUTOSAW = ObjMachineModels.AUTOSAW;
    public static final LegacyWavefrontModel MACHINE_MINING_DRILL = ObjMachineModels.MINING_DRILL;
    public static final LegacyWavefrontModel MACHINE_ORE_SLOPPER = ObjMachineModels.ORE_SLOPPER;
    public static final LegacyWavefrontModel MACHINE_MINING_LASER = ObjMachineModels.MINING_LASER;
    public static final LegacyWavefrontModel MACHINE_ACIDIZER = ObjMachineModels.ACIDIZER;
    public static final LegacyWavefrontModel MACHINE_CYCLOTRON = ObjMachineModels.CYCLOTRON;
    public static final LegacyWavefrontModel MACHINE_EXPOSURE_CHAMBER = ObjMachineModels.EXPOSURE_CHAMBER;
    public static final LegacyWavefrontModel MACHINE_DEUTERIUM_TOWER = ObjMachineModels.DEUTERIUM_TOWER;
    public static final LegacyWavefrontModel MACHINE_RADGEN = ObjMachineModels.RADGEN;
    public static final LegacyWavefrontModel MACHINE_RADIOLYSIS = ObjMachineModels.RADIOLYSIS;
    public static final LegacyWavefrontModel MACHINE_ROTARY_FURNACE = ObjMachineModels.ROTARY_FURNACE;
    public static final LegacyWavefrontModel MACHINE_ELECTROLYSER = ObjMachineModels.ELECTROLYSER;
    public static final LegacyWavefrontModel MACHINE_CHARGER = ObjMachineModels.CHARGER;
    public static final LegacyWavefrontModel MACHINE_REFUELER = ObjMachineModels.REFUELER;
    public static final LegacyWavefrontModel MACHINE_SOLAR_BOILER = ObjMachineModels.SOLAR_BOILER;
    public static final LegacyWavefrontModel MACHINE_DFC_EMITTER = ObjMachineModels.DFC_EMITTER;
    public static final LegacyWavefrontModel MACHINE_DFC_RECEIVER = ObjMachineModels.DFC_RECEIVER;
    public static final LegacyWavefrontModel MACHINE_DFC_INJECTOR = ObjMachineModels.DFC_INJECTOR;

    public static final ObjModelPart NETWORK_CONNECTOR = ObjNetworkModels.CONNECTOR;
    public static final ObjModelPart NETWORK_CONNECTOR_SUPER = ObjNetworkModels.CONNECTOR_SUPER;
    public static final ObjModelPart NETWORK_FLUID_DIODE = ObjNetworkModels.FLUID_DIODE;
    public static final ObjModelPart NETWORK_PIPE_ANCHOR = ObjNetworkModels.PIPE_ANCHOR;
    public static final ObjModelPart NETWORK_PYLON_LARGE = ObjNetworkModels.PYLON_LARGE;
    public static final ObjModelPart NETWORK_PYLON_MEDIUM = ObjNetworkModels.PYLON_MEDIUM;
    public static final ObjModelPart NETWORK_SUBSTATION = ObjNetworkModels.SUBSTATION;

    public static final ObjModelPart DOOR_SILO_HATCH = ObjDoorModels.SILO_HATCH;
    public static final ObjModelPart DOOR_SILO_HATCH_LARGE = ObjDoorModels.SILO_HATCH_LARGE;
    public static final ObjModelPart DOOR_BLAST_DOOR_BASE = ObjDoorModels.BLAST_DOOR_BASE;
    public static final ObjModelPart DOOR_BLAST_DOOR_TOOTH = ObjDoorModels.BLAST_DOOR_TOOTH;
    public static final ObjModelPart DOOR_BLAST_DOOR_SLIDER = ObjDoorModels.BLAST_DOOR_SLIDER;
    public static final ObjModelPart DOOR_BLAST_DOOR_BLOCK = ObjDoorModels.BLAST_DOOR_BLOCK;

    public static final ObjModelPart LAUNCH_TABLE_BASE = ObjLaunchModels.LAUNCH_TABLE_BASE;
    public static final ObjModelPart LAUNCH_TABLE_LARGE_PAD = ObjLaunchModels.LAUNCH_TABLE_LARGE_PAD;
    public static final ObjModelPart LAUNCH_TABLE_SMALL_PAD = ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD;
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_BASE = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_BASE;
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR;
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY;
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_BASE = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_BASE;
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR;
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY;
    public static final ObjModelPart SOYUZ_LAUNCHER_LEGS = ObjLaunchModels.SOYUZ_LAUNCHER_LEGS;
    public static final ObjModelPart SOYUZ_LAUNCHER_TABLE = ObjLaunchModels.SOYUZ_LAUNCHER_TABLE;
    public static final ObjModelPart SOYUZ_LAUNCHER_TOWER_BASE = ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_BASE;
    public static final ObjModelPart SOYUZ_LAUNCHER_TOWER = ObjLaunchModels.SOYUZ_LAUNCHER_TOWER;
    public static final ObjModelPart SOYUZ_LAUNCHER_SUPPORT_BASE = ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_BASE;
    public static final ObjModelPart SOYUZ_LAUNCHER_SUPPORT = ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT;
    public static final LegacyWavefrontModel SOYUZ = ObjSoyuzModels.SOYUZ;
    public static final LegacyWavefrontModel SOYUZ_LANDER = ObjSoyuzModels.LANDER;
    public static final LegacyWavefrontModel SOYUZ_MODULE = ObjSoyuzModels.MODULE;

    public static final ObjPartModel FUSION_TORUS = ObjFusionModels.TORUS_PARTS;
    public static final ObjPartModel FUSION_KLYSTRON = ObjFusionModels.KLYSTRON_PARTS;
    public static final ObjPartModel FUSION_KLYSTRON_CREATIVE = ObjFusionModels.KLYSTRON_CREATIVE_PARTS;
    public static final ObjPartModel FUSION_BREEDER = ObjFusionModels.BREEDER_PARTS;
    public static final ObjModelPart FUSION_COLLECTOR = ObjFusionModels.COLLECTOR;
    public static final ObjModelPart FUSION_BOILER = ObjFusionModels.BOILER;
    public static final ObjPartModel FUSION_MHDT = ObjFusionModels.MHDT_PARTS;
    public static final ObjModelPart FUSION_COUPLER = ObjFusionModels.COUPLER;
    public static final ObjPartModel FUSION_PLASMA_FORGE = ObjFusionModels.PLASMA_FORGE_PARTS;

    public static final ObjModelPart CAGE_LAMP = ObjLightModels.CAGE_LAMP;
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = ObjLightModels.FLUORESCENT_LAMP_SINGLE;
    public static final ObjModelPart FLOOD_LAMP = ObjLightModels.FLOOD_LAMP;
    public static final ObjModelPart FLOODLIGHT_BASE = ObjLightModels.FLOODLIGHT_BASE;
    public static final ObjModelPart FLOODLIGHT_LIGHTS = ObjLightModels.FLOODLIGHT_LIGHTS;
    public static final ObjModelPart FLOODLIGHT_LAMPS = ObjLightModels.FLOODLIGHT_LAMPS;
    public static final ObjModelPart DEMON_LAMP = ObjLightModels.DEMON_LAMP;

    public static final LegacyWavefrontModel TURRET_CHEKHOV = ObjTurretModels.CHEKHOV;
    public static final LegacyWavefrontModel TURRET_JEREMY = ObjTurretModels.JEREMY;
    public static final LegacyWavefrontModel TURRET_TAUON = ObjTurretModels.TAUON;
    public static final LegacyWavefrontModel TURRET_RICHARD = ObjTurretModels.RICHARD;
    public static final LegacyWavefrontModel TURRET_HOWARD = ObjTurretModels.HOWARD;
    public static final LegacyWavefrontModel TURRET_HOWARD_DAMAGED = ObjTurretModels.HOWARD_DAMAGED;
    public static final LegacyWavefrontModel TURRET_MAXWELL = ObjTurretModels.MAXWELL;
    public static final LegacyWavefrontModel TURRET_FRITZ = ObjTurretModels.FRITZ;
    public static final LegacyWavefrontModel TURRET_ARTY = ObjTurretModels.ARTY;
    public static final LegacyWavefrontModel TURRET_HIMARS = ObjTurretModels.HIMARS;
    public static final LegacyWavefrontModel TURRET_SENTRY = ObjTurretModels.SENTRY;

    public static final LegacyWavefrontModel BOMB_MINE_AP = ObjBombModels.MINE_AP;
    public static final LegacyWavefrontModel BOMB_MINE_MARELET = ObjBombModels.MINE_MARELET;
    public static final LegacyWavefrontModel BOMB_MINE_FAT = ObjBombModels.MINE_FAT;
    public static final LegacyWavefrontModel BOMB_MINE_NAVAL = ObjBombModels.MINE_NAVAL;
    public static final LegacyWavefrontModel BOMB_FAT_MAN = ObjBombModels.FAT_MAN;
    public static final LegacyWavefrontModel BOMB_FLEIJA = ObjBombModels.FLEIJA;
    public static final LegacyWavefrontModel BOMB_GADGET = ObjBombModels.GADGET;
    public static final LegacyWavefrontModel BOMB_IVYMIKE = ObjBombModels.IVYMIKE;
    public static final LegacyWavefrontModel BOMB_TSAR = ObjBombModels.TSAR;
    public static final LegacyWavefrontModel BOMB_UFP = ObjBombModels.UFP;
    public static final LegacyWavefrontModel BOMB_N2 = ObjBombModels.N2;
    public static final LegacyWavefrontModel BOMB_FSTBMB = ObjBombModels.FSTBMB;
    public static final LegacyWavefrontModel BOMB_DUD_BALEFIRE = ObjBombModels.DUD_BALEFIRE;
    public static final LegacyWavefrontModel BOMB_DUD_CONVENTIONAL = ObjBombModels.DUD_CONVENTIONAL;
    public static final LegacyWavefrontModel BOMB_DUD_NUKE = ObjBombModels.DUD_NUKE;
    public static final LegacyWavefrontModel BOMB_DUD_SALTED = ObjBombModels.DUD_SALTED;

    public static final LegacyWavefrontModel PROJECTILES = ObjProjectileModels.PROJECTILES;
    public static final LegacyWavefrontModel PROJECTILE_LEADBURSTER = ObjProjectileModels.LEADBURSTER;

    public static final LegacyWavefrontModel PA_SOURCE = ObjParticleAcceleratorModels.SOURCE;
    public static final LegacyWavefrontModel PA_BEAMLINE = ObjParticleAcceleratorModels.BEAMLINE;
    public static final LegacyWavefrontModel PA_RFC = ObjParticleAcceleratorModels.RFC;
    public static final LegacyWavefrontModel PA_QUADRUPOLE = ObjParticleAcceleratorModels.QUADRUPOLE;
    public static final LegacyWavefrontModel PA_DIPOLE = ObjParticleAcceleratorModels.DIPOLE;
    public static final LegacyWavefrontModel PA_DETECTOR = ObjParticleAcceleratorModels.DETECTOR;

    public static final LegacyWavefrontModel REACTOR_SMALL_BASE = ObjReactorModels.SMALL_BASE;
    public static final LegacyWavefrontModel REACTOR_SMALL_RODS = ObjReactorModels.SMALL_RODS;
    public static final LegacyWavefrontModel REACTOR_BREEDER = ObjReactorModels.BREEDER;
    public static final LegacyWavefrontModel REACTOR_ICF = ObjReactorModels.ICF;
    public static final LegacyWavefrontModel REACTOR_WATZ = ObjReactorModels.WATZ;
    public static final LegacyWavefrontModel REACTOR_WATZ_PUMP = ObjReactorModels.WATZ_PUMP;
    public static final LegacyWavefrontModel REACTOR_LPW2 = ObjReactorModels.LPW2;
    public static final LegacyWavefrontModel REACTOR_ZIRNOX = ObjReactorModels.ZIRNOX;
    public static final LegacyWavefrontModel REACTOR_ZIRNOX_DESTROYED = ObjReactorModels.ZIRNOX_DESTROYED;

    public static final LegacyWavefrontModel RBMK_ELEMENT = ObjRbmkModels.ELEMENT;
    public static final LegacyWavefrontModel RBMK_ELEMENT_RODS = ObjRbmkModels.ELEMENT_RODS;
    public static final LegacyWavefrontModel RBMK_RODS = ObjRbmkModels.RODS;
    public static final LegacyWavefrontModel RBMK_CRANE_CONSOLE = ObjRbmkModels.CRANE_CONSOLE;
    public static final LegacyWavefrontModel RBMK_CRANE = ObjRbmkModels.CRANE;
    public static final LegacyWavefrontModel RBMK_AUTOLOADER = ObjRbmkModels.AUTOLOADER;
    public static final LegacyWavefrontModel RBMK_CONSOLE = ObjRbmkModels.CONSOLE;
    public static final LegacyWavefrontModel RBMK_BUTTON = ObjRbmkModels.BUTTON;
    public static final LegacyWavefrontModel RBMK_GAUGE = ObjRbmkModels.GAUGE;
    public static final LegacyWavefrontModel RBMK_NUMITRON = ObjRbmkModels.NUMITRON;
    public static final LegacyWavefrontModel RBMK_LEVER = ObjRbmkModels.LEVER;
    public static final LegacyWavefrontModel RBMK_INDICATOR = ObjRbmkModels.INDICATOR;
    public static final LegacyWavefrontModel RBMK_TERMINAL = ObjRbmkModels.TERMINAL;
    public static final LegacyWavefrontModel RBMK_DEBRIS = ObjRbmkModels.DEBRIS;

    public static ObjModelPart blockPart(String name) {
        return blockPart(name, RenderType.cutout());
    }

    public static ObjModelPart blockPart(String name, RenderType renderType) {
        return new ObjModelPart(blockModel(name), renderType);
    }

    public static ObjModelPartBuilder blockCenteredPart(String name) {
        return blockPartBuilder(name, RenderType.cutout());
    }

    public static ObjModelPartBuilder blockPartBuilder(String name, RenderType renderType) {
        return new ObjModelPartBuilder(blockModel(name), renderType);
    }

    public static ObjModelPartBuilder directBlockPart(String name) {
        return blockCenteredPart(name).direct();
    }

    public static ObjModelPart trinketPart(String name, RenderType renderType) {
        return ObjTrinketModels.part(name, renderType);
    }

    public static ObjModelPart machinePart(String name) {
        return ObjMachineModels.part(name);
    }

    public static ObjModelPart machinePart(String name, RenderType renderType) {
        return ObjMachineModels.part(name, renderType);
    }

    public static ObjModelPart networkPart(String name) {
        return ObjNetworkModels.part(name);
    }

    public static ObjModelPart doorPart(String name) {
        return ObjDoorModels.part(name);
    }

    public static ObjModelPart fusionPart(String name) {
        return ObjFusionModels.part(name);
    }

    public static ObjModelPart launchPart(String name) {
        return ObjLaunchModels.part(name);
    }

    public static LegacyWavefrontModel missilePartModel(String legacyModelName) {
        return ObjMissilePartModels.model(legacyModelName);
    }

    public static ObjMissilePartModels.LegacyMissilePart missilePart(String legacyItemName) {
        return ObjMissilePartModels.part(legacyItemName);
    }

    public static ObjModelPartBuilder machinePartBuilder(String name, RenderType renderType) {
        return ObjMachineModels.partBuilder(name, renderType);
    }

    public static ObjModelPartBuilder directMachinePart(String name) {
        return ObjMachineModels.directPart(name);
    }

    public static ResourceLocation blockModel(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }

    static void register(ResourceLocation modelLocation) {
        MODELS.add(modelLocation);
    }

    public static Set<ResourceLocation> models() {
        return Collections.unmodifiableSet(MODELS);
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation model : MODELS) {
            event.register(model);
        }
    }

    private ObjModelLibrary() {
    }

    public record ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType, float lightMultiplier, boolean directRender) {
        public ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType) {
            this(modelLocation, renderType, 1.0F, false);
        }

        public ObjModelPartBuilder withRenderType(RenderType renderType) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender);
        }

        public ObjModelPartBuilder withLightMultiplier(float lightMultiplier) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender);
        }

        public ObjModelPartBuilder direct() {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, true);
        }

        public ObjModelPart withOrigin(ObjPartTransform transform) {
            return new ObjModelPart(modelLocation, renderType, transform, lightMultiplier, directRender);
        }
    }
}
