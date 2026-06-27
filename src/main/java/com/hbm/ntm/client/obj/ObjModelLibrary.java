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
    public static final LegacyWavefrontModel MACHINE_PRESS_BODY_LEGACY = ObjMachineModels.PRESS_BODY_LEGACY;
    public static final LegacyWavefrontModel MACHINE_PRESS_HEAD_LEGACY = ObjMachineModels.PRESS_HEAD_LEGACY;
    public static final LegacyWavefrontModel MACHINE_EPRESS_BODY = ObjMachineModels.EPRESS_BODY;
    public static final LegacyWavefrontModel MACHINE_EPRESS_HEAD = ObjMachineModels.EPRESS_HEAD;
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
    public static final ResourceLocation MACHINE_DRUM_GRAY_TEXTURE = ObjMachineModels.DRUM_GRAY_TEXTURE;
    public static final ObjModelPart MACHINE_FRACTION_SPACER = ObjMachineModels.FRACTION_SPACER;
    public static final ObjModelPart MACHINE_HEATING_OVEN = ObjMachineModels.HEATING_OVEN;
    public static final ResourceLocation MACHINE_HEATING_OVEN_TEXTURE = ObjMachineModels.HEATING_OVEN_TEXTURE;
    public static final ObjModelPart MACHINE_CHIMNEY_BRICK = ObjMachineModels.CHIMNEY_BRICK;
    public static final ResourceLocation MACHINE_CHIMNEY_BRICK_TEXTURE = ObjMachineModels.CHIMNEY_BRICK_TEXTURE;
    public static final ObjModelPart MACHINE_TURBINE = ObjMachineModels.TURBINE;
    public static final ObjModelPart MACHINE_DIESELGEN = ObjMachineModels.DIESELGEN;
    public static final ObjModelPart MACHINE_FIREBOX = ObjMachineModels.FIREBOX;
    public static final ObjModelPart MACHINE_OILBURNER = ObjMachineModels.OILBURNER;
    public static final ResourceLocation MACHINE_OILBURNER_TEXTURE = ObjMachineModels.OILBURNER_TEXTURE;
    public static final ObjModelPart MACHINE_ELECTRIC_HEATER = ObjMachineModels.ELECTRIC_HEATER;
    public static final ResourceLocation MACHINE_ELECTRIC_HEATER_TEXTURE = ObjMachineModels.ELECTRIC_HEATER_TEXTURE;
    public static final ObjModelPart MACHINE_HEATEX = ObjMachineModels.HEATEX;
    public static final ResourceLocation MACHINE_HEATEX_TEXTURE = ObjMachineModels.HEATEX_TEXTURE;
    public static final ObjModelPart MACHINE_BOILER = ObjMachineModels.BOILER;
    public static final ResourceLocation MACHINE_BOILER_TEXTURE = ObjMachineModels.BOILER_TEXTURE;
    public static final ObjModelPart MACHINE_BOILER_BURST = ObjMachineModels.BOILER_BURST;
    public static final ObjModelPart MACHINE_INDUSTRIAL_BOILER = ObjMachineModels.INDUSTRIAL_BOILER;
    public static final ResourceLocation MACHINE_INDUSTRIAL_BOILER_TEXTURE = ObjMachineModels.INDUSTRIAL_BOILER_TEXTURE;
    public static final ObjModelPart MACHINE_HEPHAESTUS = ObjMachineModels.HEPHAESTUS;
    public static final ResourceLocation MACHINE_HEPHAESTUS_TEXTURE = ObjMachineModels.HEPHAESTUS_TEXTURE;
    public static final ObjModelPart MACHINE_DERRICK = ObjMachineModels.DERRICK;
    public static final ObjModelPart MACHINE_PUMPJACK = ObjMachineModels.PUMPJACK;
    public static final ObjModelPart MACHINE_FRACKING_TOWER = ObjMachineModels.FRACKING_TOWER;
    public static final ObjModelPart MACHINE_OILFLARE = ObjMachineModels.OILFLARE;
    public static final ResourceLocation MACHINE_OILFLARE_TEXTURE = ObjMachineModels.OILFLARE_TEXTURE;
    public static final ObjModelPart MACHINE_CHIMNEY_INDUSTRIAL = ObjMachineModels.CHIMNEY_INDUSTRIAL;
    public static final ResourceLocation MACHINE_CHIMNEY_INDUSTRIAL_TEXTURE = ObjMachineModels.CHIMNEY_INDUSTRIAL_TEXTURE;
    public static final ObjModelPart MACHINE_BATTERY_REDD = ObjMachineModels.BATTERY_REDD;
    public static final ResourceLocation MACHINE_BATTERY_SOCKET_TEXTURE = ObjMachineModels.BATTERY_SOCKET_TEXTURE;
    public static final ResourceLocation MACHINE_BATTERY_SC_TEXTURE = ObjMachineModels.BATTERY_SC_TEXTURE;
    public static final ResourceLocation MACHINE_BATTERY_REDD_TEXTURE = ObjMachineModels.BATTERY_REDD_TEXTURE;
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
    public static final ResourceLocation MACHINE_COKER_TEXTURE = ObjMachineModels.COKER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_PYROOVEN = ObjMachineModels.PYROOVEN;
    public static final LegacyWavefrontModel MACHINE_BAT9000 = ObjMachineModels.BAT9000;
    public static final LegacyWavefrontModel MACHINE_BIGASSTANK = ObjMachineModels.BIGASSTANK;
    public static final LegacyWavefrontModel MACHINE_ORBUS = ObjMachineModels.ORBUS;
    public static final LegacyWavefrontModel MACHINE_TURBOFAN = ObjMachineModels.TURBOFAN;
    public static final LegacyWavefrontModel MACHINE_TURBINEGAS = ObjMachineModels.TURBINEGAS;
    public static final LegacyWavefrontModel MACHINE_STEAM_ENGINE = ObjMachineModels.STEAM_ENGINE;
    public static final LegacyWavefrontModel MACHINE_TURBINE_LEGACY = ObjMachineModels.TURBINE_LEGACY;
    public static final LegacyWavefrontModel MACHINE_INDUSTRIAL_TURBINE = ObjMachineModels.INDUSTRIAL_TURBINE;
    public static final LegacyWavefrontModel MACHINE_CHUNGUS = ObjMachineModels.CHUNGUS;
    public static final ResourceLocation MACHINE_CHUNGUS_TEXTURE = ObjMachineModels.CHUNGUS_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_TOWER_SMALL = ObjMachineModels.TOWER_SMALL;
    public static final LegacyWavefrontModel MACHINE_TOWER_LARGE = ObjMachineModels.TOWER_LARGE;
    public static final LegacyWavefrontModel MACHINE_CONDENSER = ObjMachineModels.CONDENSER;
    public static final LegacyWavefrontModel MACHINE_WOOD_BURNER = ObjMachineModels.WOOD_BURNER;
    public static final LegacyWavefrontModel MACHINE_COMBUSTION_ENGINE = ObjMachineModels.COMBUSTION_ENGINE;
    public static final ResourceLocation MACHINE_COMBUSTION_ENGINE_TEXTURE = ObjMachineModels.COMBUSTION_ENGINE_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_PUMP = ObjMachineModels.PUMP;
    public static final ResourceLocation MACHINE_PUMP_STEAM_TEXTURE = ObjMachineModels.PUMP_STEAM_TEXTURE;
    public static final ResourceLocation MACHINE_PUMP_ELECTRIC_TEXTURE = ObjMachineModels.PUMP_ELECTRIC_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_AMMO_PRESS = ObjMachineModels.AMMO_PRESS;
    public static final ResourceLocation MACHINE_AMMO_PRESS_TEXTURE = ObjMachineModels.AMMO_PRESS_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_ANNIHILATOR = ObjMachineModels.ANNIHILATOR;
    public static final ResourceLocation MACHINE_ANNIHILATOR_TEXTURE = ObjMachineModels.ANNIHILATOR_TEXTURE;
    public static final ResourceLocation MACHINE_ANNIHILATOR_BELT_TEXTURE = ObjMachineModels.ANNIHILATOR_BELT_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_ASSEMBLY_FACTORY = ObjMachineModels.ASSEMBLY_FACTORY;
    public static final LegacyWavefrontModel MACHINE_CHEMICAL_PLANT = ObjMachineModels.CHEMICAL_PLANT;
    public static final LegacyWavefrontModel MACHINE_CHEMICAL_FACTORY = ObjMachineModels.CHEMICAL_FACTORY;
    public static final LegacyWavefrontModel MACHINE_PUREX = ObjMachineModels.PUREX;
    public static final LegacyWavefrontModel MACHINE_MIXER = ObjMachineModels.MIXER;
    public static final ResourceLocation MACHINE_MIXER_TEXTURE = ObjMachineModels.MIXER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_FIREBOX_LEGACY = ObjMachineModels.FIREBOX_LEGACY;
    public static final LegacyWavefrontModel MACHINE_HEATING_OVEN_LEGACY = ObjMachineModels.HEATING_OVEN_LEGACY;
    public static final LegacyWavefrontModel MACHINE_OILBURNER_LEGACY = ObjMachineModels.OILBURNER_LEGACY;
    public static final LegacyWavefrontModel MACHINE_ELECTRIC_HEATER_LEGACY = ObjMachineModels.ELECTRIC_HEATER_LEGACY;
    public static final LegacyWavefrontModel MACHINE_HEATEX_LEGACY = ObjMachineModels.HEATEX_LEGACY;
    public static final LegacyWavefrontModel MACHINE_BOILER_LEGACY = ObjMachineModels.BOILER_LEGACY;
    public static final LegacyWavefrontModel MACHINE_BOILER_BURST_LEGACY = ObjMachineModels.BOILER_BURST_LEGACY;
    public static final LegacyWavefrontModel MACHINE_INDUSTRIAL_BOILER_LEGACY = ObjMachineModels.INDUSTRIAL_BOILER_LEGACY;
    public static final LegacyWavefrontModel MACHINE_HEPHAESTUS_LEGACY = ObjMachineModels.HEPHAESTUS_LEGACY;
    public static final LegacyWavefrontModel MACHINE_RTG_LEGACY = ObjMachineModels.RTG_LEGACY;
    public static final LegacyWavefrontModel MACHINE_DERRICK_LEGACY = ObjMachineModels.DERRICK_LEGACY;
    public static final LegacyWavefrontModel MACHINE_PUMPJACK_LEGACY = ObjMachineModels.PUMPJACK_LEGACY;
    public static final LegacyWavefrontModel MACHINE_FRACKING_TOWER_LEGACY = ObjMachineModels.FRACKING_TOWER_LEGACY;
    public static final LegacyWavefrontModel MACHINE_OILFLARE_LEGACY = ObjMachineModels.OILFLARE_LEGACY;
    public static final LegacyWavefrontModel MACHINE_CHIMNEY_BRICK_LEGACY = ObjMachineModels.CHIMNEY_BRICK_LEGACY;
    public static final LegacyWavefrontModel MACHINE_CHIMNEY_INDUSTRIAL_LEGACY = ObjMachineModels.CHIMNEY_INDUSTRIAL_LEGACY;
    public static final LegacyWavefrontModel MACHINE_INTAKE_LEGACY = ObjMachineModels.INTAKE_LEGACY;
    public static final LegacyWavefrontModel MACHINE_ELEVATOR_LEGACY = ObjMachineModels.ELEVATOR_LEGACY;
    public static final LegacyWavefrontModel MACHINE_DIESELGEN_LEGACY = ObjMachineModels.DIESELGEN_LEGACY;
    public static final LegacyWavefrontModel MACHINE_BATTERY_SOCKET_LEGACY = ObjMachineModels.BATTERY_SOCKET_LEGACY;
    public static final ResourceLocation MACHINE_BATTERY_SOCKET_LEGACY_TEXTURE = ObjMachineModels.BATTERY_SOCKET_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_BATTERY_REDD_LEGACY = ObjMachineModels.BATTERY_REDD_LEGACY;
    public static final ResourceLocation MACHINE_BATTERY_REDD_LEGACY_TEXTURE = ObjMachineModels.BATTERY_REDD_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_FENSU_LEGACY = ObjMachineModels.FENSU_LEGACY;
    public static final LegacyWavefrontModel MACHINE_RADAR_BODY_LEGACY = ObjMachineModels.RADAR_BODY_LEGACY;
    public static final LegacyWavefrontModel MACHINE_RADAR_LEGACY = ObjMachineModels.RADAR_LEGACY;
    public static final LegacyWavefrontModel MACHINE_RADAR_LARGE_LEGACY = ObjMachineModels.RADAR_LARGE_LEGACY;
    public static final LegacyWavefrontModel MACHINE_RADAR_SCREEN_LEGACY = ObjMachineModels.RADAR_SCREEN_LEGACY;
    public static final LegacyWavefrontModel MACHINE_SOLAR_MIRROR_LEGACY = ObjMachineModels.SOLAR_MIRROR_LEGACY;
    public static final LegacyWavefrontModel MACHINE_ARC_WELDER_LEGACY = ObjMachineModels.ARC_WELDER;
    public static final ResourceLocation MACHINE_ARC_WELDER_LEGACY_TEXTURE = ObjMachineModels.ARC_WELDER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_SOLDERING_STATION_LEGACY = ObjMachineModels.SOLDERING_STATION;
    public static final LegacyWavefrontModel MACHINE_ARC_WELDER = ObjMachineModels.ARC_WELDER;
    public static final ResourceLocation MACHINE_ARC_WELDER_TEXTURE = ObjMachineModels.ARC_WELDER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_SOLDERING_STATION = ObjMachineModels.SOLDERING_STATION;
    public static final LegacyWavefrontModel MACHINE_ARC_FURNACE = ObjMachineModels.ARC_FURNACE;
    public static final ResourceLocation MACHINE_ARC_FURNACE_TEXTURE = ObjMachineModels.ARC_FURNACE_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_TANK = ObjMachineModels.TANK;
    public static final ResourceLocation MACHINE_UF6_TANK_TEXTURE = ObjMachineModels.UF6_TANK_TEXTURE;
    public static final ResourceLocation MACHINE_PUF6_TANK_TEXTURE = ObjMachineModels.PUF6_TANK_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_CENTRIFUGE = ObjMachineModels.CENTRIFUGE;
    public static final ResourceLocation MACHINE_CENTRIFUGE_TEXTURE = ObjMachineModels.CENTRIFUGE_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_GASCENT = ObjMachineModels.GASCENT;
    public static final ResourceLocation MACHINE_GASCENT_TEXTURE = ObjMachineModels.GASCENT_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_SILEX = ObjMachineModels.SILEX;
    public static final LegacyWavefrontModel MACHINE_FEL = ObjMachineModels.FEL;
    public static final LegacyWavefrontModel MACHINE_AUTOSAW = ObjMachineModels.AUTOSAW;
    public static final ResourceLocation MACHINE_AUTOSAW_TEXTURE = ObjMachineModels.AUTOSAW_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_MINING_DRILL = ObjMachineModels.MINING_DRILL;
    public static final LegacyWavefrontModel MACHINE_ORE_SLOPPER = ObjMachineModels.ORE_SLOPPER;
    public static final LegacyWavefrontModel MACHINE_MINING_LASER = ObjMachineModels.MINING_LASER;
    public static final LegacyWavefrontModel MACHINE_ACIDIZER = ObjMachineModels.ACIDIZER;
    public static final ResourceLocation MACHINE_CRYSTALLIZER_TEXTURE = ObjMachineModels.CRYSTALLIZER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_CYCLOTRON = ObjMachineModels.CYCLOTRON;
    public static final LegacyWavefrontModel MACHINE_EXPOSURE_CHAMBER = ObjMachineModels.EXPOSURE_CHAMBER;
    public static final LegacyWavefrontModel MACHINE_DEUTERIUM_TOWER = ObjMachineModels.DEUTERIUM_TOWER;
    public static final LegacyWavefrontModel MACHINE_RADGEN = ObjMachineModels.RADGEN;
    public static final LegacyWavefrontModel MACHINE_RADIOLYSIS = ObjMachineModels.RADIOLYSIS;
    public static final LegacyWavefrontModel MACHINE_ROTARY_FURNACE = ObjMachineModels.ROTARY_FURNACE;
    public static final ResourceLocation MACHINE_ROTARY_FURNACE_TEXTURE = ObjMachineModels.ROTARY_FURNACE_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_ELECTROLYSER = ObjMachineModels.ELECTROLYSER;
    public static final LegacyWavefrontModel MACHINE_CHARGER = ObjMachineModels.CHARGER;
    public static final ResourceLocation MACHINE_CHARGER_TEXTURE = ObjMachineModels.CHARGER_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_REFUELER = ObjMachineModels.REFUELER;
    public static final LegacyWavefrontModel MACHINE_SOLAR_BOILER = ObjMachineModels.SOLAR_BOILER;
    public static final LegacyWavefrontModel MACHINE_DFC_EMITTER = ObjMachineModels.DFC_EMITTER;
    public static final LegacyWavefrontModel MACHINE_DFC_RECEIVER = ObjMachineModels.DFC_RECEIVER;
    public static final LegacyWavefrontModel MACHINE_DFC_INJECTOR = ObjMachineModels.DFC_INJECTOR;
    public static final LegacyWavefrontModel MACHINE_STIRLING = ObjMachineModels.STIRLING;
    public static final LegacyWavefrontModel MACHINE_SAWMILL = ObjMachineModels.SAWMILL;
    public static final LegacyWavefrontModel MACHINE_STRAND_CASTER = ObjMachineModels.STRAND_CASTER;
    public static final LegacyWavefrontModel MACHINE_FURNACE_STEEL = ObjMachineModels.FURNACE_STEEL;
    public static final LegacyWavefrontModel MACHINE_CONVEYOR_PRESS = ObjMachineModels.CONVEYOR_PRESS;
    public static final ResourceLocation MACHINE_CONVEYOR_PRESS_TEXTURE = ObjMachineModels.CONVEYOR_PRESS_TEXTURE;
    public static final ResourceLocation MACHINE_CONVEYOR_PRESS_BELT_TEXTURE = ObjMachineModels.CONVEYOR_PRESS_BELT_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_MICROWAVE = ObjMachineModels.MICROWAVE;
    public static final ResourceLocation MACHINE_MICROWAVE_TEXTURE = ObjMachineModels.MICROWAVE_TEXTURE;
    public static final LegacyWavefrontModel MACHINE_PISTON_INSERTER = ObjMachineModels.PISTON_INSERTER;
    public static final LegacyWavefrontModel MACHINE_IGEN = ObjMachineModels.IGEN;
    public static final LegacyWavefrontModel MACHINE_DELIVERY_DRONE = ObjMachineModels.DELIVERY_DRONE;

    public static final ObjModelPart NETWORK_CONNECTOR = ObjNetworkModels.CONNECTOR;
    public static final ObjModelPart NETWORK_CONNECTOR_SUPER = ObjNetworkModels.CONNECTOR_SUPER;
    public static final ObjModelPart NETWORK_FLUID_DIODE = ObjNetworkModels.FLUID_DIODE;
    public static final ResourceLocation NETWORK_FLUID_DIODE_TEXTURE = ObjNetworkModels.FLUID_DIODE_TEXTURE;
    public static final ObjModelPart NETWORK_PIPE_ANCHOR = ObjNetworkModels.PIPE_ANCHOR;
    public static final ObjModelPart NETWORK_PYLON_LARGE = ObjNetworkModels.PYLON_LARGE;
    public static final ObjModelPart NETWORK_PYLON_MEDIUM = ObjNetworkModels.PYLON_MEDIUM;
    public static final ObjModelPart NETWORK_SUBSTATION = ObjNetworkModels.SUBSTATION;
    public static final LegacyWavefrontModel NETWORK_CONNECTOR_LEGACY = ObjNetworkModels.CONNECTOR_LEGACY;
    public static final LegacyWavefrontModel NETWORK_CONNECTOR_SUPER_LEGACY = ObjNetworkModels.CONNECTOR_SUPER_LEGACY;
    public static final LegacyWavefrontModel NETWORK_PYLON_LARGE_LEGACY = ObjNetworkModels.PYLON_LARGE_LEGACY;
    public static final LegacyWavefrontModel NETWORK_PYLON_MEDIUM_LEGACY = ObjNetworkModels.PYLON_MEDIUM_LEGACY;
    public static final LegacyWavefrontModel NETWORK_SUBSTATION_LEGACY = ObjNetworkModels.SUBSTATION_LEGACY;
    public static final LegacyWavefrontModel NETWORK_PIPE_ANCHOR_LEGACY = ObjNetworkModels.PIPE_ANCHOR_LEGACY;
    public static final LegacyWavefrontModel NETWORK_FLUID_DIODE_LEGACY = ObjNetworkModels.FLUID_DIODE_LEGACY;
    public static final ObjModelPart DOOR_SILO_HATCH = ObjDoorModels.SILO_HATCH;
    public static final ObjModelPart DOOR_SILO_HATCH_LARGE = ObjDoorModels.SILO_HATCH_LARGE;
    public static final LegacyWavefrontModel DOOR_SILO_HATCH_LEGACY = ObjDoorModels.SILO_HATCH_LEGACY;
    public static final LegacyWavefrontModel DOOR_SILO_HATCH_LARGE_LEGACY = ObjDoorModels.SILO_HATCH_LARGE_LEGACY;
    public static final ObjModelPart DOOR_BLAST_DOOR_BASE = ObjDoorModels.BLAST_DOOR_BASE;
    public static final ObjModelPart DOOR_BLAST_DOOR_TOOTH = ObjDoorModels.BLAST_DOOR_TOOTH;
    public static final ObjModelPart DOOR_BLAST_DOOR_SLIDER = ObjDoorModels.BLAST_DOOR_SLIDER;
    public static final ObjModelPart DOOR_BLAST_DOOR_BLOCK = ObjDoorModels.BLAST_DOOR_BLOCK;
    public static final LegacyWavefrontModel DOOR_BLAST_DOOR_BASE_LEGACY = ObjDoorModels.BLAST_DOOR_BASE_LEGACY;
    public static final LegacyWavefrontModel DOOR_BLAST_DOOR_TOOTH_LEGACY = ObjDoorModels.BLAST_DOOR_TOOTH_LEGACY;
    public static final LegacyWavefrontModel DOOR_BLAST_DOOR_SLIDER_LEGACY = ObjDoorModels.BLAST_DOOR_SLIDER_LEGACY;
    public static final LegacyWavefrontModel DOOR_BLAST_DOOR_BLOCK_LEGACY = ObjDoorModels.BLAST_DOOR_BLOCK_LEGACY;
    public static final LegacyWavefrontModel PHEO_FIRE_DOOR = ObjPheoDoorModels.FIRE_DOOR;
    public static final LegacyWavefrontModel PHEO_AIRLOCK_DOOR = ObjPheoDoorModels.AIRLOCK_DOOR;
    public static final LegacyWavefrontModel PHEO_BLAST_DOOR = ObjPheoDoorModels.BLAST_DOOR;
    public static final LegacyWavefrontModel PHEO_CONTAINMENT_DOOR = ObjPheoDoorModels.CONTAINMENT_DOOR;
    public static final LegacyWavefrontModel PHEO_SEAL_DOOR = ObjPheoDoorModels.SEAL_DOOR;
    public static final LegacyWavefrontModel PHEO_SECURE_DOOR = ObjPheoDoorModels.SECURE_DOOR;
    public static final LegacyWavefrontModel PHEO_SLIDING_DOOR = ObjPheoDoorModels.SLIDING_DOOR;
    public static final LegacyWavefrontModel PHEO_VEHICLE_DOOR = ObjPheoDoorModels.VEHICLE_DOOR;
    public static final LegacyWavefrontModel PHEO_WATER_DOOR = ObjPheoDoorModels.WATER_DOOR;
    public static final LegacyWavefrontModel PHEO_VAULT_DOOR = ObjPheoDoorModels.VAULT_DOOR;

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
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_LEGS_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_LEGS_LEGACY;
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TABLE_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_TABLE_LEGACY;
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_BASE_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_BASE_LEGACY;
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_LEGACY;
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY;
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_LEGACY = ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_LEGACY;
    public static final LegacyWavefrontModel MISSILE_PAD = ObjLaunchModels.MISSILE_PAD;
    public static final LegacyWavefrontModel MISSILE_ERECTOR = ObjLaunchModels.MISSILE_ERECTOR;
    public static final LegacyWavefrontModel MISSILE_ASSEMBLY = ObjLaunchModels.MISSILE_ASSEMBLY;
    public static final ResourceLocation MISSILE_ASSEMBLY_TEXTURE = ObjLaunchModels.MISSILE_ASSEMBLY_TEXTURE;
    public static final LegacyWavefrontModel LAUNCH_STRUT = ObjLaunchModels.STRUT;
    public static final ResourceLocation LAUNCH_STRUT_TEXTURE = ObjLaunchModels.STRUT_TEXTURE;
    public static final LegacyWavefrontModel COMPACT_LAUNCHER = ObjLaunchModels.COMPACT_LAUNCHER;
    public static final LegacyWavefrontModel LAUNCH_TABLE_BASE_LEGACY = ObjLaunchModels.LAUNCH_TABLE_BASE_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_PAD_LEGACY = ObjLaunchModels.LAUNCH_TABLE_LARGE_PAD_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_PAD_LEGACY = ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_LEGACY = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_LEGACY = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY_LEGACY = ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_LEGACY = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_LEGACY = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_LEGACY;
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY_LEGACY = ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY_LEGACY;
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
    public static final LegacyWavefrontModel FUSION_TORUS_LEGACY = ObjFusionModels.TORUS_LEGACY;
    public static final LegacyWavefrontModel FUSION_KLYSTRON_LEGACY = ObjFusionModels.KLYSTRON_LEGACY;
    public static final LegacyWavefrontModel FUSION_BREEDER_LEGACY = ObjFusionModels.BREEDER_LEGACY;
    public static final LegacyWavefrontModel FUSION_COLLECTOR_LEGACY = ObjFusionModels.COLLECTOR_LEGACY;
    public static final LegacyWavefrontModel FUSION_BOILER_LEGACY = ObjFusionModels.BOILER_LEGACY;
    public static final LegacyWavefrontModel FUSION_MHDT_LEGACY = ObjFusionModels.MHDT_LEGACY;
    public static final LegacyWavefrontModel FUSION_COUPLER_LEGACY = ObjFusionModels.COUPLER_LEGACY;
    public static final LegacyWavefrontModel FUSION_PLASMA_FORGE_LEGACY = ObjFusionModels.PLASMA_FORGE_LEGACY;

    public static final ObjModelPart CAGE_LAMP = ObjLightModels.CAGE_LAMP;
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = ObjLightModels.FLUORESCENT_LAMP_SINGLE;
    public static final ObjModelPart FLOOD_LAMP = ObjLightModels.FLOOD_LAMP;
    public static final ObjModelPart FLOODLIGHT_BASE = ObjLightModels.FLOODLIGHT_BASE;
    public static final ObjModelPart FLOODLIGHT_LIGHTS = ObjLightModels.FLOODLIGHT_LIGHTS;
    public static final ObjModelPart FLOODLIGHT_LAMPS = ObjLightModels.FLOODLIGHT_LAMPS;
    public static final ObjModelPart DEMON_LAMP = ObjLightModels.DEMON_LAMP;
    public static final LegacyWavefrontModel CAGE_LAMP_LEGACY = ObjLightModels.CAGE_LAMP_LEGACY;
    public static final LegacyWavefrontModel FLUORESCENT_LAMP_LEGACY = ObjLightModels.FLUORESCENT_LAMP_LEGACY;
    public static final LegacyWavefrontModel FLOOD_LAMP_LEGACY = ObjLightModels.FLOOD_LAMP_LEGACY;
    public static final LegacyWavefrontModel FLOODLIGHT_LEGACY = ObjLightModels.FLOODLIGHT_LEGACY;
    public static final LegacyWavefrontModel DEMON_LAMP_LEGACY = ObjLightModels.DEMON_LAMP_LEGACY;

    public static final LegacyWavefrontModel BLOCK_SCAFFOLD = ObjBlockModels.SCAFFOLD;
    public static final LegacyWavefrontModel BLOCK_BEAM = ObjBlockModels.BEAM;
    public static final LegacyWavefrontModel BLOCK_BARREL = ObjBlockModels.BARREL;
    public static final LegacyWavefrontModel BLOCK_POLE = ObjBlockModels.POLE;
    public static final LegacyWavefrontModel BLOCK_PIPE = ObjBlockModels.PIPE;
    public static final LegacyWavefrontModel BLOCK_TAPE_RECORDER = ObjBlockModels.TAPE_RECORDER;
    public static final LegacyWavefrontModel BLOCK_BARBED_WIRE = ObjBlockModels.BARBED_WIRE;
    public static final LegacyWavefrontModel BLOCK_SPIKES = ObjBlockModels.SPIKES;
    public static final LegacyWavefrontModel BLOCK_ANTENNA_TOP = ObjBlockModels.ANTENNA_TOP;
    public static final LegacyWavefrontModel BLOCK_CONSERVE_CRATE = ObjBlockModels.CONSERVE_CRATE;
    public static final LegacyWavefrontModel BLOCK_PIPE_RIM = ObjBlockModels.PIPE_RIM;
    public static final LegacyWavefrontModel BLOCK_PIPE_QUAD = ObjBlockModels.PIPE_QUAD;
    public static final LegacyWavefrontModel BLOCK_PIPE_FRAME = ObjBlockModels.PIPE_FRAME;
    public static final LegacyWavefrontModel BLOCK_RTTY = ObjBlockModels.RTTY;
    public static final LegacyWavefrontModel BLOCK_CRT = ObjBlockModels.CRT;
    public static final LegacyWavefrontModel BLOCK_TOASTER = ObjBlockModels.TOASTER;
    public static final LegacyWavefrontModel BLOCK_DECO_COMPUTER = ObjBlockModels.DECO_COMPUTER;
    public static final LegacyWavefrontModel BLOCK_HEV_BATTERY = ObjBlockModels.HEV_BATTERY;
    public static final LegacyWavefrontModel BLOCK_ANVIL = ObjBlockModels.ANVIL;
    public static final LegacyWavefrontModel BLOCK_CRYSTAL_POWER = ObjBlockModels.CRYSTAL_POWER;
    public static final LegacyWavefrontModel BLOCK_CRYSTAL_ENERGY = ObjBlockModels.CRYSTAL_ENERGY;
    public static final LegacyWavefrontModel BLOCK_CRYSTAL_ROBUST = ObjBlockModels.CRYSTAL_ROBUST;
    public static final LegacyWavefrontModel BLOCK_CRYSTAL_TRIXITE = ObjBlockModels.CRYSTAL_TRIXITE;
    public static final LegacyWavefrontModel BLOCK_CABLE_NEO = ObjBlockModels.CABLE_NEO;
    public static final LegacyWavefrontModel BLOCK_PIPE_NEO = ObjBlockModels.PIPE_NEO;
    public static final ResourceLocation BLOCK_PIPE_NEO_TEXTURE = ObjBlockModels.PIPE_NEO_TEXTURE;
    public static final ResourceLocation BLOCK_PIPE_SILVER_TEXTURE = ObjBlockModels.PIPE_SILVER_TEXTURE;
    public static final LegacyWavefrontModel BLOCK_DIFURNACE_EXTENSION = ObjBlockModels.DIFURNACE_EXTENSION;
    public static final LegacyWavefrontModel BLOCK_SPLITTER = ObjBlockModels.SPLITTER;
    public static final LegacyWavefrontModel BLOCK_CRANE_BUFFER = ObjBlockModels.CRANE_BUFFER;
    public static final LegacyWavefrontModel BLOCK_RAIL_NARROW_STRAIGHT = ObjBlockModels.RAIL_NARROW_STRAIGHT;
    public static final LegacyWavefrontModel BLOCK_RAIL_NARROW_CURVE = ObjBlockModels.RAIL_NARROW_CURVE;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_STRAIGHT = ObjBlockModels.RAIL_STANDARD_STRAIGHT;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_STRAIGHT_SHORT = ObjBlockModels.RAIL_STANDARD_STRAIGHT_SHORT;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_CURVE = ObjBlockModels.RAIL_STANDARD_CURVE;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_CURVE_WIDE7 = ObjBlockModels.RAIL_STANDARD_CURVE_WIDE7;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_CURVE_WIDE9 = ObjBlockModels.RAIL_STANDARD_CURVE_WIDE9;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_RAMP = ObjBlockModels.RAIL_STANDARD_RAMP;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_BUFFER = ObjBlockModels.RAIL_STANDARD_BUFFER;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_SWITCH = ObjBlockModels.RAIL_STANDARD_SWITCH;
    public static final LegacyWavefrontModel BLOCK_RAIL_STANDARD_SWITCH_FLIPPED = ObjBlockModels.RAIL_STANDARD_SWITCH_FLIPPED;
    public static final LegacyWavefrontModel BLOCK_CAPACITOR = ObjBlockModels.CAPACITOR;
    public static final LegacyWavefrontModel BLOCK_FUNNEL = ObjBlockModels.FUNNEL;
    public static final LegacyWavefrontModel BLOCK_CHARGE_DYNAMITE = ObjBlockModels.CHARGE_DYNAMITE;
    public static final LegacyWavefrontModel BLOCK_CHARGE_C4 = ObjBlockModels.CHARGE_C4;

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
    public static final LegacyWavefrontModel NUKE_GADGET = ObjNukeModels.GADGET;
    public static final LegacyWavefrontModel NUKE_BOY = ObjNukeModels.BOY;
    public static final LegacyWavefrontModel NUKE_BOY_LEGACY = ObjNukeModels.BOY_LEGACY;
    public static final LegacyWavefrontModel NUKE_MAN = ObjNukeModels.MAN;
    public static final LegacyWavefrontModel NUKE_TSAR = ObjNukeModels.TSAR;
    public static final LegacyWavefrontModel NUKE_MIKE = ObjNukeModels.MIKE;
    public static final LegacyWavefrontModel NUKE_PROTOTYPE = ObjNukeModels.PROTOTYPE;
    public static final LegacyWavefrontModel NUKE_FLEIJA = ObjNukeModels.FLEIJA;
    public static final LegacyWavefrontModel NUKE_SOLINIUM = ObjNukeModels.SOLINIUM;
    public static final LegacyWavefrontModel NUKE_N2 = ObjNukeModels.N2;
    public static final LegacyWavefrontModel NUKE_BOMB_MULTI_LEGACY = ObjNukeModels.BOMB_MULTI_LEGACY;

    public static final LegacyWavefrontModel PROJECTILES = ObjProjectileModels.PROJECTILES;
    public static final LegacyWavefrontModel PROJECTILE_LEADBURSTER = ObjProjectileModels.LEADBURSTER;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_BLANK = ObjProjectileModels.DEBRIS_BLANK;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_ELEMENT = ObjProjectileModels.DEBRIS_ELEMENT;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_FUEL = ObjProjectileModels.DEBRIS_FUEL;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_ROD = ObjProjectileModels.DEBRIS_ROD;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_LID = ObjProjectileModels.DEBRIS_LID;
    public static final LegacyWavefrontModel PROJECTILE_DEBRIS_GRAPHITE = ObjProjectileModels.DEBRIS_GRAPHITE;
    public static final LegacyWavefrontModel PROJECTILE_ZIRNOX_DEBRIS_BLANK = ObjProjectileModels.ZIRNOX_DEBRIS_BLANK;
    public static final LegacyWavefrontModel PROJECTILE_ZIRNOX_DEBRIS_CONCRETE = ObjProjectileModels.ZIRNOX_DEBRIS_CONCRETE;
    public static final LegacyWavefrontModel PROJECTILE_ZIRNOX_DEBRIS_ELEMENT = ObjProjectileModels.ZIRNOX_DEBRIS_ELEMENT;
    public static final LegacyWavefrontModel PROJECTILE_ZIRNOX_DEBRIS_EXCHANGER = ObjProjectileModels.ZIRNOX_DEBRIS_EXCHANGER;
    public static final LegacyWavefrontModel PROJECTILE_ZIRNOX_DEBRIS_SHRAPNEL = ObjProjectileModels.ZIRNOX_DEBRIS_SHRAPNEL;
    public static final LegacyWavefrontModel PROJECTILE_BOMBLET_ZETA = ObjProjectileModels.BOMBLET_ZETA;
    public static final LegacyWavefrontModel TRINKET_LANTERN = ObjTrinketModels.LANTERN;
    public static final ResourceLocation TRINKET_LANTERN_TEXTURE = ObjTrinketModels.LANTERN_TEXTURE;
    public static final ResourceLocation TRINKET_LANTERN_RUSTY_TEXTURE = ObjTrinketModels.LANTERN_RUSTY_TEXTURE;
    public static final LegacyWavefrontModel TRINKET_BOBBLE_LEGACY = ObjTrinketModels.BOBBLE_LEGACY;
    public static final LegacyWavefrontModel TRINKET_YOMI_LEGACY = ObjTrinketModels.YOMI_LEGACY;
    public static final LegacyWavefrontModel TRINKET_HUNDUN_LEGACY = ObjTrinketModels.HUNDUN_LEGACY;
    public static final LegacyWavefrontModel TRINKET_DERG_LEGACY = ObjTrinketModels.DERG_LEGACY;
    public static final LegacyWavefrontModel TRINKET_SNOWGLOBE_LEGACY = ObjTrinketModels.SNOWGLOBE_LEGACY;
    public static final LegacyWavefrontModel TRINKET_CHIP_LEGACY = ObjTrinketModels.CHIP_LEGACY;
    public static final LegacyWavefrontModel EFFECT_SPHERE_RUV = ObjEffectModels.SPHERE_RUV;
    public static final LegacyWavefrontModel EFFECT_SPHERE_IUV = ObjEffectModels.SPHERE_IUV;
    public static final LegacyWavefrontModel EFFECT_SPHERE_UV = ObjEffectModels.SPHERE_UV;
    public static final LegacyWavefrontModel EFFECT_SPHERE_NEW = ObjEffectModels.SPHERE_NEW;
    public static final LegacyWavefrontModel EFFECT_SPHERE = ObjEffectModels.SPHERE;
    public static final LegacyWavefrontModel EFFECT_RING = ObjEffectModels.RING;
    public static final LegacyWavefrontModel EFFECT_CASINGS = ObjEffectModels.CASINGS;
    public static final LegacyWavefrontModel UTILITY_GEIGER_COUNTER = ObjUtilityModels.GEIGER_COUNTER;
    public static final LegacyWavefrontModel UTILITY_FORCEFIELD_TOP = ObjUtilityModels.FORCEFIELD_TOP;
    public static final ResourceLocation UTILITY_GEIGER_TEXTURE = ObjUtilityModels.GEIGER_TEXTURE;
    public static final ResourceLocation UTILITY_FORCEFIELD_BASE_TEXTURE = ObjUtilityModels.FORCEFIELD_BASE_TEXTURE;
    public static final ResourceLocation UTILITY_FORCEFIELD_TOP_TEXTURE = ObjUtilityModels.FORCEFIELD_TOP_TEXTURE;
    public static final LegacyWavefrontModel UTILITY_SAT_FOEQ_BURNING = ObjUtilityModels.SAT_FOEQ_BURNING;
    public static final LegacyWavefrontModel UTILITY_SAT_FOEQ_FIRE = ObjUtilityModels.SAT_FOEQ_FIRE;
    public static final LegacyWavefrontModel UTILITY_SAT_DOCK = ObjUtilityModels.SAT_DOCK;
    public static final LegacyWavefrontModel UTILITY_TESLA = ObjUtilityModels.TESLA;
    public static final ResourceLocation UTILITY_TESLA_TEXTURE = ObjUtilityModels.TESLA_TEXTURE;
    public static final LegacyWavefrontModel UTILITY_FILE_CABINET = ObjUtilityModels.FILE_CABINET;
    public static final LegacyWavefrontModel ENTITY_TESLACRAB = ObjEntityModels.TESLACRAB;
    public static final LegacyWavefrontModel ENTITY_MASKMAN = ObjEntityModels.MASKMAN;
    public static final LegacyWavefrontModel ENTITY_BLOCKSPIDER = ObjEntityModels.BLOCKSPIDER;
    public static final LegacyWavefrontModel ENTITY_TAINTCRAB = ObjEntityModels.TAINTCRAB;
    public static final LegacyWavefrontModel ENTITY_UFO = ObjEntityModels.UFO;
    public static final LegacyWavefrontModel ENTITY_MINI_UFO = ObjEntityModels.MINI_UFO;
    public static final LegacyWavefrontModel ENTITY_SIEGE_UFO = ObjEntityModels.SIEGE_UFO;
    public static final LegacyWavefrontModel ENTITY_GLYPHID = ObjEntityModels.GLYPHID;
    public static final LegacyWavefrontModel ENTITY_QUADCOPTER = ObjEntityModels.QUADCOPTER;
    public static final LegacyWavefrontModel ENTITY_C130 = ObjEntityModels.C130;
    public static final LegacyWavefrontModel ENTITY_BOXCAR = ObjEntityModels.BOXCAR;
    public static final LegacyWavefrontModel ENTITY_DUCHESS_GAMBIT = ObjEntityModels.DUCHESS_GAMBIT;
    public static final LegacyWavefrontModel ENTITY_DORNIER = ObjEntityModels.DORNIER;
    public static final LegacyWavefrontModel ENTITY_B29 = ObjEntityModels.B29;
    public static final LegacyWavefrontModel ENTITY_BOT_PRIME_HEAD = ObjEntityModels.BOT_PRIME_HEAD;
    public static final LegacyWavefrontModel ENTITY_BOT_PRIME_BODY = ObjEntityModels.BOT_PRIME_BODY;
    public static final LegacyWavefrontModel ENTITY_PLASTIC_BAG = ObjEntityModels.PLASTIC_BAG;
    public static final LegacyWavefrontModel ENTITY_TUNNELER = ObjEntityModels.TUNNELER;
    public static final LegacyWavefrontModel ENTITY_CAPSULE = ObjEntityModels.CAPSULE;
    public static final LegacyWavefrontModel VEHICLE_CART = ObjVehicleModels.CART;
    public static final LegacyWavefrontModel VEHICLE_CART_DESTROYER = ObjVehicleModels.CART_DESTROYER;
    public static final LegacyWavefrontModel VEHICLE_CART_POWDER = ObjVehicleModels.CART_POWDER;
    public static final LegacyWavefrontModel VEHICLE_TRAM = ObjVehicleModels.TRAM;
    public static final LegacyWavefrontModel VEHICLE_TRAM_TRAILER = ObjVehicleModels.TRAM_TRAILER;
    public static final LegacyWavefrontModel WEAPON_SHIMMER_SLEDGE = ObjWeaponModels.SHIMMER_SLEDGE;
    public static final LegacyWavefrontModel WEAPON_SHIMMER_AXE = ObjWeaponModels.SHIMMER_AXE;
    public static final LegacyWavefrontModel WEAPON_STOPSIGN = ObjWeaponModels.STOPSIGN;
    public static final LegacyWavefrontModel WEAPON_GAVEL = ObjWeaponModels.GAVEL;
    public static final LegacyWavefrontModel WEAPON_CRUCIBLE = ObjWeaponModels.CRUCIBLE;
    public static final LegacyWavefrontModel WEAPON_CHAINSAW = ObjWeaponModels.CHAINSAW;
    public static final LegacyWavefrontModel WEAPON_BOLTGUN = ObjWeaponModels.BOLTGUN;
    public static final LegacyWavefrontModel WEAPON_BOLTER = ObjWeaponModels.BOLTER;
    public static final LegacyWavefrontModel WEAPON_DETONATOR_LASER = ObjWeaponModels.DETONATOR_LASER;
    public static final LegacyWavefrontModel WEAPON_FIREEXT = ObjWeaponModels.FIREEXT;
    public static final LegacyWavefrontModel WEAPON_COILGUN = ObjWeaponModels.COILGUN;
    public static final LegacyWavefrontModel WEAPON_PEPPERBOX = ObjWeaponModels.PEPPERBOX;
    public static final LegacyWavefrontModel WEAPON_BIO_REVOLVER = ObjWeaponModels.BIO_REVOLVER;
    public static final LegacyWavefrontModel WEAPON_HENRY = ObjWeaponModels.HENRY;
    public static final LegacyWavefrontModel WEAPON_GREASEGUN = ObjWeaponModels.GREASEGUN;
    public static final LegacyWavefrontModel WEAPON_MARESLEG = ObjWeaponModels.MARESLEG;
    public static final LegacyWavefrontModel WEAPON_FLAREGUN = ObjWeaponModels.FLAREGUN;
    public static final LegacyWavefrontModel WEAPON_AM180 = ObjWeaponModels.AM180;
    public static final LegacyWavefrontModel WEAPON_LIBERATOR = ObjWeaponModels.LIBERATOR;
    public static final LegacyWavefrontModel WEAPON_CONGOLAKE = ObjWeaponModels.CONGOLAKE;
    public static final LegacyWavefrontModel WEAPON_FLAMETHROWER = ObjWeaponModels.FLAMETHROWER;
    public static final LegacyWavefrontModel WEAPON_LILMAC = ObjWeaponModels.LILMAC;
    public static final LegacyWavefrontModel WEAPON_CARBINE = ObjWeaponModels.CARBINE;
    public static final LegacyWavefrontModel WEAPON_UZI = ObjWeaponModels.UZI;
    public static final LegacyWavefrontModel WEAPON_SPAS_12 = ObjWeaponModels.SPAS_12;
    public static final LegacyWavefrontModel WEAPON_PANZERSCHRECK = ObjWeaponModels.PANZERSCHRECK;
    public static final LegacyWavefrontModel WEAPON_STAR_F = ObjWeaponModels.STAR_F;
    public static final LegacyWavefrontModel WEAPON_G3 = ObjWeaponModels.G3;
    public static final LegacyWavefrontModel WEAPON_STINGER = ObjWeaponModels.STINGER;
    public static final LegacyWavefrontModel WEAPON_MK108 = ObjWeaponModels.MK108;
    public static final LegacyWavefrontModel WEAPON_CHEMTHROWER = ObjWeaponModels.CHEMTHROWER;
    public static final LegacyWavefrontModel WEAPON_AMAT = ObjWeaponModels.AMAT;
    public static final LegacyWavefrontModel WEAPON_M2 = ObjWeaponModels.M2;
    public static final LegacyWavefrontModel WEAPON_SHREDDER = ObjWeaponModels.SHREDDER;
    public static final LegacyWavefrontModel WEAPON_SEXY = ObjWeaponModels.SEXY;
    public static final LegacyWavefrontModel WEAPON_WHISKEY = ObjWeaponModels.WHISKEY;
    public static final LegacyWavefrontModel WEAPON_QUADRO = ObjWeaponModels.QUADRO;
    public static final LegacyWavefrontModel WEAPON_MINIGUN = ObjWeaponModels.MINIGUN;
    public static final LegacyWavefrontModel WEAPON_MISSILE_LAUNCHER = ObjWeaponModels.MISSILE_LAUNCHER;
    public static final LegacyWavefrontModel WEAPON_TESLA_CANNON = ObjWeaponModels.TESLA_CANNON;
    public static final LegacyWavefrontModel WEAPON_LASER_PISTOL = ObjWeaponModels.LASER_PISTOL;
    public static final LegacyWavefrontModel WEAPON_STG77 = ObjWeaponModels.STG77;
    public static final LegacyWavefrontModel WEAPON_TAU = ObjWeaponModels.TAU;
    public static final LegacyWavefrontModel WEAPON_FATMAN = ObjWeaponModels.FATMAN;
    public static final LegacyWavefrontModel WEAPON_LASRIFLE = ObjWeaponModels.LASRIFLE;
    public static final LegacyWavefrontModel WEAPON_LASRIFLE_MODS = ObjWeaponModels.LASRIFLE_MODS;
    public static final LegacyWavefrontModel WEAPON_HANGMAN = ObjWeaponModels.HANGMAN;
    public static final LegacyWavefrontModel WEAPON_FOLLY = ObjWeaponModels.FOLLY;
    public static final LegacyWavefrontModel WEAPON_DOUBLE_BARREL = ObjWeaponModels.DOUBLE_BARREL;
    public static final LegacyWavefrontModel WEAPON_ABERRATOR = ObjWeaponModels.ABERRATOR;
    public static final LegacyWavefrontModel WEAPON_MAS36 = ObjWeaponModels.MAS36;
    public static final LegacyWavefrontModel WEAPON_CHARGE_THROWER = ObjWeaponModels.CHARGE_THROWER;
    public static final LegacyWavefrontModel WEAPON_DRILL = ObjWeaponModels.DRILL;
    public static final LegacyWavefrontModel WEAPON_N_I_4_N_I = ObjWeaponModels.N_I_4_N_I;
    public static final LegacyWavefrontModel WEAPON_LANCE = ObjWeaponModels.LANCE;
    public static final LegacyWavefrontModel WEAPON_GRENADES = ObjWeaponModels.GRENADES;
    public static final LegacyWavefrontModel WEAPON_BUILDING = ObjWeaponModels.BUILDING;
    public static final LegacyWavefrontModel WEAPON_TORPEDO = ObjWeaponModels.TORPEDO;
    public static final LegacyWavefrontModel WEAPON_TOM_MAIN = ObjWeaponModels.TOM_MAIN;
    public static final LegacyWavefrontModel WEAPON_TOM_FLAME = ObjWeaponModels.TOM_FLAME;
    public static final LegacyWavefrontModel ARMOR_BJ = ObjArmorModels.BJ;
    public static final LegacyWavefrontModel ARMOR_HEV = ObjArmorModels.HEV;
    public static final LegacyWavefrontModel ARMOR_AJR = ObjArmorModels.AJR;
    public static final LegacyWavefrontModel ARMOR_T51 = ObjArmorModels.T51;
    public static final LegacyWavefrontModel ARMOR_HAT = ObjArmorModels.HAT;
    public static final LegacyWavefrontModel ARMOR_NO9 = ObjArmorModels.NO9;
    public static final LegacyWavefrontModel ARMOR_GOGGLES = ObjArmorModels.GOGGLES;
    public static final LegacyWavefrontModel ARMOR_FAU = ObjArmorModels.FAU;
    public static final LegacyWavefrontModel ARMOR_DNT = ObjArmorModels.DNT;
    public static final LegacyWavefrontModel ARMOR_STEAMSUIT = ObjArmorModels.STEAMSUIT;
    public static final LegacyWavefrontModel ARMOR_DIESELSUIT = ObjArmorModels.DIESELSUIT;
    public static final LegacyWavefrontModel ARMOR_REMNANT = ObjArmorModels.REMNANT;
    public static final LegacyWavefrontModel ARMOR_NCR = ObjArmorModels.NCR;
    public static final LegacyWavefrontModel ARMOR_BISMUTH = ObjArmorModels.BISMUTH;
    public static final LegacyWavefrontModel ARMOR_PLAYER_FEM = ObjArmorModels.PLAYER_FEM;
    public static final LegacyWavefrontModel ARMOR_MOD_TESLA = ObjArmorModels.MOD_TESLA;
    public static final LegacyWavefrontModel ARMOR_WINGS = ObjArmorModels.WINGS;
    public static final LegacyWavefrontModel ARMOR_AXEPACK = ObjArmorModels.AXEPACK;
    public static final LegacyWavefrontModel ARMOR_TAIL = ObjArmorModels.TAIL;
    public static final LegacyWavefrontModel ARMOR_ENVSUIT = ObjArmorModels.ENVSUIT;
    public static final LegacyWavefrontModel ARMOR_TAURUN = ObjArmorModels.TAURUN;
    public static final LegacyWavefrontModel ARMOR_TRENCHMASTER = ObjArmorModels.TRENCHMASTER;
    public static final LegacyWavefrontModel MISSILE_V2 = ObjMissilePartModels.MISSILE_V2;
    public static final LegacyWavefrontModel MISSILE_ABM = ObjMissilePartModels.MISSILE_ABM;
    public static final LegacyWavefrontModel MISSILE_STEALTH = ObjMissilePartModels.MISSILE_STEALTH;
    public static final LegacyWavefrontModel MISSILE_STRONG = ObjMissilePartModels.MISSILE_STRONG;
    public static final LegacyWavefrontModel MISSILE_HUGE = ObjMissilePartModels.MISSILE_HUGE;
    public static final LegacyWavefrontModel MISSILE_ATLAS = ObjMissilePartModels.MISSILE_ATLAS;
    public static final LegacyWavefrontModel MISSILE_MICRO = ObjMissilePartModels.MISSILE_MICRO;
    public static final LegacyWavefrontModel MISSILE_SHUTTLE = ObjMissilePartModels.MISSILE_SHUTTLE;
    public static final LegacyWavefrontModel MISSILE_MINER_ROCKET = ObjMissilePartModels.MINER_ROCKET;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_10_KEROSENE = ObjMissilePartModels.MP_T_10_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_10_SOLID = ObjMissilePartModels.MP_T_10_SOLID;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_10_XENON = ObjMissilePartModels.MP_T_10_XENON;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_KEROSENE = ObjMissilePartModels.MP_T_15_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_KEROSENE_DUAL = ObjMissilePartModels.MP_T_15_KEROSENE_DUAL;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_KEROSENE_TRIPLE = ObjMissilePartModels.MP_T_15_KEROSENE_TRIPLE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_SOLID = ObjMissilePartModels.MP_T_15_SOLID;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_SOLID_HEXDECUPLE = ObjMissilePartModels.MP_T_15_SOLID_HEXDECUPLE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_BALEFIRE_SHORT = ObjMissilePartModels.MP_T_15_BALEFIRE_SHORT;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_BALEFIRE = ObjMissilePartModels.MP_T_15_BALEFIRE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_15_BALEFIRE_LARGE = ObjMissilePartModels.MP_T_15_BALEFIRE_LARGE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_20_KEROSENE = ObjMissilePartModels.MP_T_20_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_20_KEROSENE_DUAL = ObjMissilePartModels.MP_T_20_KEROSENE_DUAL;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_20_KEROSENE_TRIPLE = ObjMissilePartModels.MP_T_20_KEROSENE_TRIPLE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_20_SOLID = ObjMissilePartModels.MP_T_20_SOLID;
    public static final LegacyWavefrontModel MISSILE_PART_MP_T_20_SOLID_MULTI = ObjMissilePartModels.MP_T_20_SOLID_MULTI;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_10_FLAT = ObjMissilePartModels.MP_S_10_FLAT;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_10_CRUISE = ObjMissilePartModels.MP_S_10_CRUISE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_10_SPACE = ObjMissilePartModels.MP_S_10_SPACE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_15_FLAT = ObjMissilePartModels.MP_S_15_FLAT;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_15_THIN = ObjMissilePartModels.MP_S_15_THIN;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_15_SOYUZ = ObjMissilePartModels.MP_S_15_SOYUZ;
    public static final LegacyWavefrontModel MISSILE_PART_MP_S_20 = ObjMissilePartModels.MP_S_20;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_10_KEROSENE = ObjMissilePartModels.MP_F_10_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_10_LONG_KEROSENE = ObjMissilePartModels.MP_F_10_LONG_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_10_15_KEROSENE = ObjMissilePartModels.MP_F_10_15_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_15_KEROSENE = ObjMissilePartModels.MP_F_15_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_15_HYDROGEN = ObjMissilePartModels.MP_F_15_HYDROGEN;
    public static final LegacyWavefrontModel MISSILE_PART_MP_F_15_20_KEROSENE = ObjMissilePartModels.MP_F_15_20_KEROSENE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_HE = ObjMissilePartModels.MP_W_10_HE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_INCENDIARY = ObjMissilePartModels.MP_W_10_INCENDIARY;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_BUSTER = ObjMissilePartModels.MP_W_10_BUSTER;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_NUCLEAR = ObjMissilePartModels.MP_W_10_NUCLEAR;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_NUCLEAR_LARGE = ObjMissilePartModels.MP_W_10_NUCLEAR_LARGE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_10_TAINT = ObjMissilePartModels.MP_W_10_TAINT;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_HE = ObjMissilePartModels.MP_W_15_HE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_INCENDIARY = ObjMissilePartModels.MP_W_15_INCENDIARY;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_NUCLEAR = ObjMissilePartModels.MP_W_15_NUCLEAR;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_BOXCAR = ObjMissilePartModels.MP_W_15_BOXCAR;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_N2 = ObjMissilePartModels.MP_W_15_N2;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_BALEFIRE = ObjMissilePartModels.MP_W_15_BALEFIRE;
    public static final LegacyWavefrontModel MISSILE_PART_MP_W_15_TURBINE = ObjMissilePartModels.MP_W_15_TURBINE;
    public static final ResourceLocation MISSILE_PART_UNIVERSAL_TEXTURE = ObjMissilePartModels.UNIVERSAL_TEXTURE;
    public static final ResourceLocation MISSILE_PART_BOXCAR_TEXTURE = ObjMissilePartModels.BOXCAR_TEXTURE;

    public static final LegacyWavefrontModel PA_SOURCE = ObjParticleAcceleratorModels.SOURCE;
    public static final LegacyWavefrontModel PA_BEAMLINE = ObjParticleAcceleratorModels.BEAMLINE;
    public static final LegacyWavefrontModel PA_RFC = ObjParticleAcceleratorModels.RFC;
    public static final LegacyWavefrontModel PA_QUADRUPOLE = ObjParticleAcceleratorModels.QUADRUPOLE;
    public static final LegacyWavefrontModel PA_DIPOLE = ObjParticleAcceleratorModels.DIPOLE;
    public static final LegacyWavefrontModel PA_DETECTOR = ObjParticleAcceleratorModels.DETECTOR;
    public static final ResourceLocation PA_SOURCE_TEXTURE = ObjParticleAcceleratorModels.SOURCE_TEXTURE;
    public static final ResourceLocation PA_BEAMLINE_TEXTURE = ObjParticleAcceleratorModels.BEAMLINE_TEXTURE;
    public static final ResourceLocation PA_RFC_TEXTURE = ObjParticleAcceleratorModels.RFC_TEXTURE;
    public static final ResourceLocation PA_QUADRUPOLE_TEXTURE = ObjParticleAcceleratorModels.QUADRUPOLE_TEXTURE;
    public static final ResourceLocation PA_DIPOLE_TEXTURE = ObjParticleAcceleratorModels.DIPOLE_TEXTURE;
    public static final ResourceLocation PA_DETECTOR_TEXTURE = ObjParticleAcceleratorModels.DETECTOR_TEXTURE;

    public static final LegacyWavefrontModel REACTOR_SMALL_BASE = ObjReactorModels.SMALL_BASE;
    public static final LegacyWavefrontModel REACTOR_SMALL_RODS = ObjReactorModels.SMALL_RODS;
    public static final LegacyWavefrontModel REACTOR_BREEDER = ObjReactorModels.BREEDER;
    public static final LegacyWavefrontModel REACTOR_ICF = ObjReactorModels.ICF;
    public static final LegacyWavefrontModel REACTOR_WATZ = ObjReactorModels.WATZ;
    public static final LegacyWavefrontModel REACTOR_WATZ_PUMP = ObjReactorModels.WATZ_PUMP;
    public static final LegacyWavefrontModel REACTOR_LPW2 = ObjReactorModels.LPW2;
    public static final LegacyWavefrontModel REACTOR_ZIRNOX = ObjReactorModels.ZIRNOX;
    public static final LegacyWavefrontModel REACTOR_ZIRNOX_DESTROYED = ObjReactorModels.ZIRNOX_DESTROYED;
    public static final ResourceLocation REACTOR_WATZ_TEXTURE = ObjReactorModels.WATZ_TEXTURE;
    public static final ResourceLocation REACTOR_WATZ_PUMP_TEXTURE = ObjReactorModels.WATZ_PUMP_TEXTURE;
    public static final ResourceLocation REACTOR_ZIRNOX_TEXTURE = ObjReactorModels.ZIRNOX_TEXTURE;
    public static final ResourceLocation REACTOR_ZIRNOX_DESTROYED_TEXTURE = ObjReactorModels.ZIRNOX_DESTROYED_TEXTURE;

    public static final LegacyWavefrontModel RBMK_ELEMENT = ObjRbmkModels.ELEMENT;
    public static final LegacyWavefrontModel RBMK_ELEMENT_RODS = ObjRbmkModels.ELEMENT_RODS;
    public static final LegacyWavefrontModel RBMK_ELEMENT_RODS_VBO = ObjRbmkModels.ELEMENT_RODS_VBO;
    public static final LegacyWavefrontModel RBMK_RODS = ObjRbmkModels.RODS;
    public static final LegacyWavefrontModel RBMK_RODS_VBO = ObjRbmkModels.RODS_VBO;
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
    public static final ResourceLocation RBMK_CRANE_CONSOLE_TEXTURE = ObjRbmkModels.CRANE_CONSOLE_TEXTURE;
    public static final ResourceLocation RBMK_CRANE_TEXTURE = ObjRbmkModels.CRANE_TEXTURE;
    public static final ResourceLocation RBMK_AUTOLOADER_TEXTURE = ObjRbmkModels.AUTOLOADER_TEXTURE;
    public static final ResourceLocation RBMK_CONSOLE_TEXTURE = ObjRbmkModels.CONSOLE_TEXTURE;
    public static final ResourceLocation RBMK_KEYPAD_TEXTURE = ObjRbmkModels.KEYPAD_TEXTURE;
    public static final ResourceLocation RBMK_GAUGE_TEXTURE = ObjRbmkModels.GAUGE_TEXTURE;
    public static final ResourceLocation RBMK_NUMITRON_TEXTURE = ObjRbmkModels.NUMITRON_TEXTURE;
    public static final ResourceLocation RBMK_NUMITRON_LIGHTS_TEXTURE = ObjRbmkModels.NUMITRON_LIGHTS_TEXTURE;
    public static final ResourceLocation RBMK_LEVER_TEXTURE = ObjRbmkModels.LEVER_TEXTURE;
    public static final ResourceLocation RBMK_INDICATOR_TEXTURE = ObjRbmkModels.INDICATOR_TEXTURE;
    public static final ResourceLocation RBMK_TERMINAL_TEXTURE = ObjRbmkModels.TERMINAL_TEXTURE;
    public static final ResourceLocation RBMK_ELEMENT_TEXTURE = ObjRbmkModels.ELEMENT_TEXTURE;
    public static final ResourceLocation RBMK_ELEMENT_INNER_TEXTURE = ObjRbmkModels.ELEMENT_INNER_TEXTURE;
    public static final ResourceLocation RBMK_ELEMENT_FUEL_TEXTURE = ObjRbmkModels.ELEMENT_FUEL_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_STANDARD_TEXTURE = ObjRbmkModels.CONTROL_STANDARD_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_AUTO_TEXTURE = ObjRbmkModels.CONTROL_AUTO_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_RED_TEXTURE = ObjRbmkModels.CONTROL_RED_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_YELLOW_TEXTURE = ObjRbmkModels.CONTROL_YELLOW_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_GREEN_TEXTURE = ObjRbmkModels.CONTROL_GREEN_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_BLUE_TEXTURE = ObjRbmkModels.CONTROL_BLUE_TEXTURE;
    public static final ResourceLocation RBMK_CONTROL_PURPLE_TEXTURE = ObjRbmkModels.CONTROL_PURPLE_TEXTURE;
    public static final ResourceLocation RBMK_DEBRIS_TEXTURE = ObjRbmkModels.DEBRIS_TEXTURE;
    public static final ResourceLocation RBMK_DEBRIS_BURNING_TEXTURE = ObjRbmkModels.DEBRIS_BURNING_TEXTURE;
    public static final ResourceLocation RBMK_DEBRIS_RADIATING_TEXTURE = ObjRbmkModels.DEBRIS_RADIATING_TEXTURE;
    public static final ResourceLocation RBMK_DEBRIS_DIGAMMA_TEXTURE = ObjRbmkModels.DEBRIS_DIGAMMA_TEXTURE;

    public static ObjModelPart blockPart(String name) {
        return blockPart(name, RenderType.cutout());
    }

    public static ObjModelPart blockPart(String name, RenderType renderType) {
        return new ObjModelPart(blockModel(name), renderType);
    }

    public static ObjModelPart blockPart(String name, RenderType renderType, boolean translucent) {
        return new ObjModelPart(blockModel(name), renderType, ObjPartTransform.IDENTITY, 1.0F, false, translucent);
    }

    public static ObjModelPart blockTranslucentPart(String name) {
        return blockPart(name, RenderType.translucent(), true);
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

    public static ResourceLocation missileTexture(String legacyTextureName) {
        return ObjMissilePartModels.missileTexture(legacyTextureName);
    }

    public static ResourceLocation missilePartTexture(String legacyTexturePath) {
        return ObjMissilePartModels.missilePartTexture(legacyTexturePath);
    }

    public static ResourceLocation missilePartTextureForItem(String legacyItemName) {
        return ObjMissilePartModels.textureForPart(legacyItemName);
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

    public record ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType, float lightMultiplier,
            boolean directRender, boolean translucent) {
        public ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType) {
            this(modelLocation, renderType, 1.0F, false, renderType == RenderType.translucent());
        }

        public ObjModelPartBuilder withRenderType(RenderType renderType) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender,
                    renderType == RenderType.translucent());
        }

        public ObjModelPartBuilder withLightMultiplier(float lightMultiplier) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender, translucent);
        }

        public ObjModelPartBuilder direct() {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, true, translucent);
        }

        public ObjModelPartBuilder markTranslucent() {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender, true);
        }

        public ObjModelPart withOrigin(ObjPartTransform transform) {
            return new ObjModelPart(modelLocation, renderType, transform, lightMultiplier, directRender, translucent);
        }
    }
}
