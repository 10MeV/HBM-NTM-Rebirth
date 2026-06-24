package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Set;

public final class ObjMachineModels {
    private static final Set<String> ROOT_DIRECT_MODELS = Set.of(
            "core_emitter", "core_injector", "core_receiver", "epress_body", "epress_head",
            "fluidtank", "fluidtank_exploded", "press_body", "press_head", "radar_base",
            "radiolysis", "refinery", "refinery_exploded", "tank");
    private static final Set<String> BLOCK_DIRECT_MODELS = Set.of("charger", "refueler");
    private static final Set<String> MACHINE_DIRECT_MODELS = Set.of(
            "acidizer", "ammo_press", "annihilator", "arc_furnace", "arc_welder", "autocal",
            "assembly_factory", "assembly_machine", "autosaw", "bat9000", "battery", "bigasstank", "boiler",
            "boiler_burst", "catalytic_cracker", "catalytic_reformer", "centrifuge",
            "chemical_factory", "chemical_plant", "chimney_brick", "chimney_industrial",
            "chungus", "coker", "combustion_engine", "compressor", "condenser",
            "conveyor_press", "cyclotron", "derrick", "dieselgen", "drone",
            "electric_heater", "electrolyser", "elevator", "exposure_chamber", "fan", "fel",
            "fensu", "fensu2", "firebox", "flare_stack", "fracking_tower", "furnace_steel",
            "gascent", "heatex", "heating_oven", "hephaestus", "hydrotreater", "igen",
            "industrial_boiler", "industrial_turbine", "intake", "liquefactor",
            "machine_deuterium_tower", "microwave", "mining_drill", "mining_laser", "mixer",
            "oilburner", "orbus", "ore_slopper", "piston_inserter", "pump", "pumpjack",
            "purex", "pyrooven", "radar", "radar_large", "radar_screen", "radgen",
            "rotary_furnace", "rtg", "sawmill", "silex", "solar_boiler", "solar_mirror",
            "soldering_station", "solidifier", "steam_engine", "stirling", "strand_caster", "telex",
            "thresher",
            "tower_large", "tower_small", "turbine", "turbinegas", "turbofan",
            "vacuum_distill", "vending_machine", "wood_burner");
    private static final Set<String> DIRECT_MODEL_TEXTURES = Set.of(
            "acidizer", "ammo_press", "annihilator", "arc_furnace", "arc_welder", "autocal",
            "assembly_factory", "assembly_machine", "autosaw", "bat9000", "battery_socket",
            "bigasstank", "blast_furnace", "boiler", "catalytic_cracker", "catalytic_reformer",
            "centrifuge", "charger", "chemical_factory", "chemical_plant", "chimney_brick",
            "chimney_industrial", "chungus",
            "coker", "combination_oven", "combustion_engine", "compressor", "compressor_compact",
            "condenser", "conveyor_press", "crucible_heat", "cyclotron", "derrick", "dieselgen",
            "drain", "drone", "drum_gray", "electric_heater", "electrolyser", "elevator",
            "exposure_chamber", "fan", "fel", "fensu", "fensu2", "firebox",
            "flare_stack", "fracking_tower", "fraction_spacer", "fraction_tower", "furnace_iron",
            "furnace_steel", "gascent", "heater_heatex", "heating_oven", "hephaestus", "igen",
            "hydrotreater", "industrial_boiler", "industrial_turbine", "intake", "liquefactor",
            "machine_deuterium_tower", "microwave", "mining_drill", "mining_laser_base",
            "mining_laser_laser", "mining_laser_pivot", "mixer", "oilburner",
            "orbus", "ore_slopper", "piston_inserter", "pump_electric", "pump_steam", "pumpjack",
            "purex", "pyrooven", "radar_base", "radar_large", "radar_screen", "radgen", "refueler",
            "rotary_furnace", "rtg", "sawmill", "silex", "solar_boiler",
            "solar_mirror", "soldering_station", "solidifier", "steam_engine", "strand_caster", "telex",
            "thresher",
            "stirling", "tower_large", "tower_small", "turbine", "turbinegas", "turbofan",
            "vacuum_distill", "vending_machine", "wood_burner");

    public static final ObjModelPart PRESS_HEAD = ObjModelLibrary.directBlockPart("press_head")
            .withRenderType(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
            .withLightMultiplier(0.82F)
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjPartModel PRESS = new ObjPartModel()
            .part("Cube.001_Cube.002", PRESS_HEAD, "Head")
            .legacyOrder("Cube_Cube.000", "Cube.001_Cube.002");
    public static final LegacyWavefrontModel PRESS_BODY_LEGACY = legacyModel("press_body");
    public static final LegacyWavefrontModel PRESS_HEAD_LEGACY = legacyModel("press_head");
    public static final LegacyWavefrontModel EPRESS_BODY = legacyModel("epress_body");
    public static final LegacyWavefrontModel EPRESS_HEAD = legacyModel("epress_head");
    public static final LegacyWavefrontModel VENDING_MACHINE = legacyModel("vending_machine").noSmooth().asVBO();
    public static final LegacyWavefrontModel ASSEMBLY_MACHINE_LEGACY = legacyModel("assembly_machine").asVBO();
    public static final ObjModelPart BATTERY_SOCKET_SOCKET = directPart("battery_socket_socket")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_SOCKET_SUPPORTS = directPart("battery_socket_supports")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_PACK_BATTERY = directPart("battery_pack_battery")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_PACK_CAPACITOR = directPart("battery_pack_capacitor")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjPartModel BATTERY_SOCKET = new ObjPartModel()
            .part("Socket", BATTERY_SOCKET_SOCKET)
            .part("Supports", BATTERY_SOCKET_SUPPORTS)
            .part("Battery", BATTERY_PACK_BATTERY)
            .part("Capacitor", BATTERY_PACK_CAPACITOR)
            .legacyOrder("Supports", "Capacitor", "Battery", "Socket");
    public static final ObjModelPart ASSEMBLY_MACHINE_BASE = directPart("assembly_machine_base")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_FRAME = directPart("assembly_machine_frame")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_RING = directPart("assembly_machine_ring")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_ARM_LOWER_1 = directPart("assembly_machine_arm_lower_1")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_ARM_LOWER_2 = directPart("assembly_machine_arm_lower_2")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_ARM_UPPER_1 = directPart("assembly_machine_arm_upper_1")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_ARM_UPPER_2 = directPart("assembly_machine_arm_upper_2")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_HEAD_1 = directPart("assembly_machine_head_1")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_HEAD_2 = directPart("assembly_machine_head_2")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_SPIKE_1 = directPart("assembly_machine_spike_1")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart ASSEMBLY_MACHINE_SPIKE_2 = directPart("assembly_machine_spike_2")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjPartModel ASSEMBLY_MACHINE = new ObjPartModel()
            .part("Base", ASSEMBLY_MACHINE_BASE)
            .part("Frame", ASSEMBLY_MACHINE_FRAME)
            .part("Ring", ASSEMBLY_MACHINE_RING, "Ring2")
            .part("ArmLower1", ASSEMBLY_MACHINE_ARM_LOWER_1)
            .part("ArmLower2", ASSEMBLY_MACHINE_ARM_LOWER_2)
            .part("ArmUpper1", ASSEMBLY_MACHINE_ARM_UPPER_1)
            .part("ArmUpper2", ASSEMBLY_MACHINE_ARM_UPPER_2)
            .part("Head1", ASSEMBLY_MACHINE_HEAD_1)
            .part("Head2", ASSEMBLY_MACHINE_HEAD_2)
            .part("Spike1", ASSEMBLY_MACHINE_SPIKE_1)
            .part("Spike2", ASSEMBLY_MACHINE_SPIKE_2)
            .legacyOrder("Base", "Frame", "Ring", "ArmLower1", "ArmUpper1", "Head1", "Spike1",
                    "ArmLower2", "ArmUpper2", "Head2", "Spike2");
    public static final ObjModelPart RADAR_SCREEN = directPart("radar_screen")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart SOLAR_MIRROR = directPart("solar_mirror")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart DRAIN = directPart("drain")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart INTAKE = directPart("intake")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart COMBINATION_OVEN = directPart("combination_oven")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart RTG = directPart("rtg")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart TELEX = directPart("telex")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FRACTION_TOWER = directPart("fraction_tower")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FAN = directPart("fan")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FURNACE_IRON = directPart("furnace_iron")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart ELEVATOR = directPart("elevator")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart CRUCIBLE = directPart("crucible")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart DRUM = directPart("drum")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FRACTION_SPACER = directPart("fraction_spacer")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEATING_OVEN = directPart("heating_oven")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart CHIMNEY_BRICK = directPart("chimney_brick")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart TURBINE = directPart("turbine")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart DIESELGEN = directPart("dieselgen")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FIREBOX = directPart("firebox")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FIREBOX_INNER_EMPTY = directPart("firebox_inner_empty")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FIREBOX_INNER_BURNING = directPart("firebox_inner_burning")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FIREBOX_DOOR = directPart("firebox_door")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FIREBOX_MAIN = directPart("firebox_main")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjPartModel FIREBOX_PARTS = new ObjPartModel()
            .part("InnerEmpty", FIREBOX_INNER_EMPTY)
            .part("InnerBurning", FIREBOX_INNER_BURNING)
            .part("Door", FIREBOX_DOOR)
            .part("Main", FIREBOX_MAIN)
            .legacyOrder("InnerEmpty", "InnerBurning", "Door", "Main");
    public static final ObjModelPart OILBURNER = directPart("oilburner")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart ELECTRIC_HEATER = directPart("electric_heater")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEATEX = directPart("heatex")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BOILER = directPart("boiler")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BOILER_BURST = directPart("boiler_burst")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart INDUSTRIAL_BOILER = directPart("industrial_boiler")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEPHAESTUS = directPart("hephaestus")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEPHAESTUS_ROTOR = directPart("hephaestus_rotor")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEPHAESTUS_CORE = directPart("hephaestus_core")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart HEPHAESTUS_MAIN = directPart("hephaestus_main")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjPartModel HEPHAESTUS_PARTS = new ObjPartModel()
            .part("Rotor", HEPHAESTUS_ROTOR)
            .part("Core", HEPHAESTUS_CORE)
            .part("Main", HEPHAESTUS_MAIN)
            .legacyOrder("Rotor", "Core", "Main");
    public static final ObjModelPart DERRICK = directPart("derrick")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart PUMPJACK = directPart("pumpjack")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart FRACKING_TOWER = directPart("fracking_tower")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart OILFLARE = directPart("flare_stack")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart CHIMNEY_INDUSTRIAL = directPart("chimney_industrial")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_REDD = directPart("fensu2")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_REDD_BASE = directPart("fensu2_base")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_REDD_WHEEL = directPart("fensu2_wheel")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_REDD_LIGHTS = directPart("fensu2_lights")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjModelPart BATTERY_REDD_PLASMA = directPart("fensu2_plasma")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);
    public static final ObjPartModel BATTERY_REDD_PARTS = new ObjPartModel()
            .part("Base", BATTERY_REDD_BASE)
            .part("Wheel", BATTERY_REDD_WHEEL)
            .part("Lights", BATTERY_REDD_LIGHTS)
            .part("Plasma", BATTERY_REDD_PLASMA)
            .legacyOrder("Base", "Wheel", "Lights", "Plasma");
    public static final ObjModelPart FENSU = directPart("fensu")
            .withOrigin(ObjPartTransform.BLOCK_CENTER);

    public static final LegacyWavefrontModel REFINERY = legacyModel("refinery").asVBO();
    public static final LegacyWavefrontModel REFINERY_EXPLODED = legacyModel("refinery_exploded", "refinery").asVBO();
    public static final LegacyWavefrontModel FLUIDTANK = legacyModel("fluidtank").asVBO();
    public static final LegacyWavefrontModel FLUIDTANK_EXPLODED = legacyModel("fluidtank_exploded", "fluidtank").asVBO();
    public static final LegacyWavefrontModel VACUUM_DISTILL = legacyModel("vacuum_distill").asVBO();
    public static final LegacyWavefrontModel CATALYTIC_CRACKER = legacyModel("catalytic_cracker").asVBO();
    public static final LegacyWavefrontModel CATALYTIC_REFORMER = legacyModel("catalytic_reformer").asVBO();
    public static final LegacyWavefrontModel HYDROTREATER = legacyModel("hydrotreater").asVBO();
    public static final LegacyWavefrontModel LIQUEFACTOR = legacyModel("liquefactor").asVBO();
    public static final LegacyWavefrontModel SOLIDIFIER = legacyModel("solidifier").asVBO();
    public static final LegacyWavefrontModel COMPRESSOR = legacyModel("compressor").asVBO();
    public static final LegacyWavefrontModel COKER = legacyModel("coker").asVBO();
    public static final LegacyWavefrontModel PYROOVEN = legacyModel("pyrooven").asVBO();
    public static final LegacyWavefrontModel BAT9000 = legacyModel("bat9000").asVBO();
    public static final LegacyWavefrontModel BIGASSTANK = legacyModel("bigasstank").asVBO();
    public static final LegacyWavefrontModel ORBUS = legacyModel("orbus").asVBO();
    public static final LegacyWavefrontModel TURBOFAN = legacyModel("turbofan").asVBO();
    public static final LegacyWavefrontModel TURBINEGAS = legacyModel("turbinegas").asVBO();
    public static final LegacyWavefrontModel STEAM_ENGINE = legacyModel("steam_engine").asVBO();
    public static final LegacyWavefrontModel TURBINE_LEGACY = legacyModel("turbine").asVBO();
    public static final LegacyWavefrontModel INDUSTRIAL_TURBINE = legacyModel("industrial_turbine").asVBO();
    public static final LegacyWavefrontModel CHUNGUS = legacyModel("chungus").asVBO();
    public static final LegacyWavefrontModel TOWER_SMALL = legacyModel("tower_small").asVBO();
    public static final LegacyWavefrontModel TOWER_LARGE = legacyModel("tower_large").asVBO();
    public static final LegacyWavefrontModel CONDENSER = legacyModel("condenser").asVBO();
    public static final LegacyWavefrontModel WOOD_BURNER = legacyModel("wood_burner").asVBO();
    public static final LegacyWavefrontModel COMBUSTION_ENGINE = legacyModel("combustion_engine").asVBO();
    public static final LegacyWavefrontModel PUMP = legacyModel("pump").asVBO();
    public static final LegacyWavefrontModel AMMO_PRESS = legacyModel("ammo_press").asVBO();
    public static final LegacyWavefrontModel ANNIHILATOR = legacyModel("annihilator").asVBO();
    public static final LegacyWavefrontModel ASSEMBLY_FACTORY = legacyModel("assembly_factory").asVBO();
    public static final LegacyWavefrontModel CHEMICAL_PLANT = legacyModel("chemical_plant").asVBO();
    public static final LegacyWavefrontModel CHEMICAL_FACTORY = legacyModel("chemical_factory").asVBO();
    public static final LegacyWavefrontModel PUREX = legacyModel("purex").asVBO();
    public static final LegacyWavefrontModel MIXER = legacyModel("mixer").asVBO();
    public static final LegacyWavefrontModel FIREBOX_LEGACY = legacyModel("firebox").noSmooth().asVBO();
    public static final LegacyWavefrontModel HEATING_OVEN_LEGACY = legacyModel("heating_oven").noSmooth().asVBO();
    public static final LegacyWavefrontModel OILBURNER_LEGACY = legacyModel("oilburner").asVBO();
    public static final LegacyWavefrontModel ELECTRIC_HEATER_LEGACY = legacyModel("electric_heater").noSmooth().asVBO();
    public static final LegacyWavefrontModel HEATEX_LEGACY = legacyModel("heatex", "heater_heatex").asVBO();
    public static final LegacyWavefrontModel BOILER_LEGACY = legacyModel("boiler").asVBO();
    public static final LegacyWavefrontModel BOILER_BURST_LEGACY = legacyModel("boiler_burst", "boiler").asVBO();
    public static final LegacyWavefrontModel INDUSTRIAL_BOILER_LEGACY = legacyModel("industrial_boiler").asVBO();
    public static final LegacyWavefrontModel HEPHAESTUS_LEGACY = legacyModel("hephaestus").asVBO();
    public static final LegacyWavefrontModel RTG_LEGACY = legacyModel("rtg").noSmooth().asVBO();
    public static final LegacyWavefrontModel DERRICK_LEGACY = legacyModel("derrick").asVBO();
    public static final LegacyWavefrontModel PUMPJACK_LEGACY = legacyModel("pumpjack").asVBO();
    public static final LegacyWavefrontModel FRACKING_TOWER_LEGACY = legacyModel("fracking_tower").asVBO();
    public static final LegacyWavefrontModel OILFLARE_LEGACY = legacyModel("flare_stack").asVBO();
    public static final LegacyWavefrontModel CHIMNEY_BRICK_LEGACY = legacyModel("chimney_brick").asVBO();
    public static final LegacyWavefrontModel CHIMNEY_INDUSTRIAL_LEGACY = legacyModel("chimney_industrial").asVBO();
    public static final LegacyWavefrontModel INTAKE_LEGACY = legacyModel("intake").asVBO();
    public static final LegacyWavefrontModel ELEVATOR_LEGACY = legacyModel("elevator").asVBO();
    public static final LegacyWavefrontModel DIESELGEN_LEGACY = legacyModel("dieselgen").asVBO();
    public static final LegacyWavefrontModel BATTERY_SOCKET_LEGACY = legacyModel("battery", "battery_socket").asVBO();
    public static final LegacyWavefrontModel BATTERY_REDD_LEGACY = legacyModel("fensu2").asVBO();
    public static final LegacyWavefrontModel FENSU_LEGACY = legacyModel("fensu").asVBO();
    public static final LegacyWavefrontModel FAN_LEGACY = legacyModel("fan").asVBO();
    public static final LegacyWavefrontModel RADAR_BODY_LEGACY = legacyModel("radar_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_LEGACY = legacyModel("radar", "radar_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_LARGE_LEGACY = legacyModel("radar_large").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_SCREEN_LEGACY = legacyModel("radar_screen").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOLAR_MIRROR_LEGACY = legacyModel("solar_mirror").noSmooth();
    public static final LegacyWavefrontModel TELEX_LEGACY = legacyModel("telex").asVBO();
    public static final LegacyWavefrontModel AUTOCAL_LEGACY = legacyModel("autocal").asVBO();
    public static final LegacyWavefrontModel ARC_WELDER = legacyModel("arc_welder").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOLDERING_STATION = legacyModel("soldering_station").noSmooth().asVBO();
    public static final LegacyWavefrontModel ARC_FURNACE = legacyModel("arc_furnace").asVBO();
    public static final LegacyWavefrontModel TANK = legacyModel("tank").asVBO();
    public static final LegacyWavefrontModel CENTRIFUGE = legacyModel("centrifuge").asVBO();
    public static final LegacyWavefrontModel GASCENT = legacyModel("gascent").asVBO();
    public static final LegacyWavefrontModel SILEX = legacyModel("silex").asVBO();
    public static final LegacyWavefrontModel FEL = legacyModel("fel").asVBO();
    public static final LegacyWavefrontModel AUTOSAW = legacyModel("autosaw").noSmooth().asVBO();
    public static final LegacyWavefrontModel THRESHER = legacyModel("thresher").noSmooth().asVBO();
    public static final LegacyWavefrontModel MINING_DRILL = legacyModel("mining_drill").asVBO();
    public static final LegacyWavefrontModel ORE_SLOPPER = legacyModel("ore_slopper").asVBO();
    public static final LegacyWavefrontModel MINING_LASER = legacyModel("mining_laser", "mining_laser_base").asVBO();
    public static final LegacyWavefrontModel ACIDIZER = legacyModel("acidizer").asVBO();
    public static final LegacyWavefrontModel CYCLOTRON = legacyModel("cyclotron").asVBO();
    public static final LegacyWavefrontModel EXPOSURE_CHAMBER = legacyModel("exposure_chamber").asVBO();
    public static final LegacyWavefrontModel DEUTERIUM_TOWER = legacyModel("machine_deuterium_tower").asVBO();
    public static final LegacyWavefrontModel RADGEN = legacyModel("radgen").asVBO();
    public static final LegacyWavefrontModel RADIOLYSIS = legacyModel("radiolysis").asVBO();
    public static final LegacyWavefrontModel ROTARY_FURNACE = legacyModel("rotary_furnace").asVBO();
    public static final LegacyWavefrontModel ELECTROLYSER = legacyModel("electrolyser").asVBO();
    public static final LegacyWavefrontModel CHARGER = legacyModel("charger").asVBO();
    public static final LegacyWavefrontModel REFUELER = legacyModel("refueler");
    public static final LegacyWavefrontModel SOLAR_BOILER = legacyModel("solar_boiler").asVBO();
    public static final LegacyWavefrontModel DFC_EMITTER = legacyModel("core_emitter");
    public static final LegacyWavefrontModel DFC_RECEIVER = legacyModel("core_receiver");
    public static final LegacyWavefrontModel DFC_INJECTOR = legacyModel("core_injector");
    public static final LegacyWavefrontModel STIRLING = legacyModel("stirling").asVBO();
    public static final LegacyWavefrontModel SAWMILL = legacyModel("sawmill").asVBO();
    public static final LegacyWavefrontModel STRAND_CASTER = legacyModel("strand_caster");
    public static final LegacyWavefrontModel FURNACE_STEEL = legacyModel("furnace_steel").asVBO();
    public static final LegacyWavefrontModel CONVEYOR_PRESS = legacyModel("conveyor_press");
    public static final LegacyWavefrontModel MICROWAVE = legacyModel("microwave").asVBO();
    public static final LegacyWavefrontModel PISTON_INSERTER = legacyModel("piston_inserter");
    public static final LegacyWavefrontModel IGEN = legacyModel("igen");
    public static final LegacyWavefrontModel DELIVERY_DRONE = legacyModel("drone");

    public static final ResourceLocation FLUIDTANK_INNER_TEXTURE = machineTexture("fluidtank_inner");
    public static final ResourceLocation LEGACY_FLUIDTANK_INNER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/tank/tank_inner.png");
    public static final ResourceLocation BATTERY_SOCKET_TEXTURE = machineTexture("battery_socket");
    public static final ResourceLocation ASSEMBLY_MACHINE_TEXTURE = machineTexture("assembly_machine");
    public static final ResourceLocation BATTERY_SC_TEXTURE = machineTexture("battery_sc");
    public static final ResourceLocation BATTERY_REDD_TEXTURE = machineTexture("fensu2");
    public static final ResourceLocation DRUM_GRAY_TEXTURE = machineTexture("drum_gray");
    public static final ResourceLocation HEATING_OVEN_TEXTURE = machineTexture("heating_oven");
    public static final ResourceLocation HEATEX_TEXTURE = machineTexture("heater_heatex");
    public static final ResourceLocation OILBURNER_TEXTURE = machineTexture("oilburner");
    public static final ResourceLocation ELECTRIC_HEATER_TEXTURE = machineTexture("electric_heater");
    public static final ResourceLocation BOILER_TEXTURE = machineTexture("boiler");
    public static final ResourceLocation INDUSTRIAL_BOILER_TEXTURE = machineTexture("industrial_boiler");
    public static final ResourceLocation CHIMNEY_BRICK_TEXTURE = machineTexture("chimney_brick");
    public static final ResourceLocation CHIMNEY_INDUSTRIAL_TEXTURE = machineTexture("chimney_industrial");
    public static final ResourceLocation PUMP_STEAM_TEXTURE = machineTexture("pump_steam");
    public static final ResourceLocation COKER_TEXTURE = machineTexture("coker");
    public static final ResourceLocation COMBUSTION_ENGINE_TEXTURE = machineTexture("combustion_engine");
    public static final ResourceLocation HEPHAESTUS_TEXTURE = machineTexture("hephaestus");
    public static final ResourceLocation OILFLARE_TEXTURE = machineTexture("flare_stack");
    public static final ResourceLocation CHUNGUS_TEXTURE = machineTexture("chungus");
    public static final ResourceLocation TURBOFAN_BACK_TEXTURE = machineTexture("turbofan_back");
    public static final ResourceLocation TURBOFAN_AFTERBURNER_TEXTURE = machineTexture("turbofan_afterburner");
    public static final ResourceLocation TURBOFAN_BLADES_TEXTURE = machineTexture("turbofan_blades");
    public static final ResourceLocation PUMP_ELECTRIC_TEXTURE = machineTexture("pump_electric");
    public static final ResourceLocation AMMO_PRESS_TEXTURE = machineTexture("ammo_press");
    public static final ResourceLocation ANNIHILATOR_TEXTURE = machineTexture("annihilator");
    public static final ResourceLocation ANNIHILATOR_BELT_TEXTURE = machineTexture("annihilator_belt");
    public static final ResourceLocation ASSEMBLY_FACTORY_SPARKS_TEXTURE = machineTexture("assembly_factory_sparks");
    public static final ResourceLocation CHEMICAL_PLANT_FLUID_TEXTURE = machineTexture("chemical_plant_fluid");
    public static final ResourceLocation MIXER_TEXTURE = machineTexture("mixer");
    public static final ResourceLocation CRYSTALLIZER_TEXTURE = machineTexture("acidizer");
    public static final ResourceLocation MINING_LASER_BASE_TEXTURE = machineTexture("mining_laser_base");
    public static final ResourceLocation MINING_LASER_PIVOT_TEXTURE = machineTexture("mining_laser_pivot");
    public static final ResourceLocation MINING_LASER_LASER_TEXTURE = machineTexture("mining_laser_laser");
    public static final ResourceLocation STIRLING_TEXTURE = machineTexture("stirling");
    public static final ResourceLocation STIRLING_STEEL_TEXTURE = machineTexture("stirling_steel");
    public static final ResourceLocation STIRLING_CREATIVE_TEXTURE = machineTexture("stirling_creative");
    public static final ResourceLocation ASHPIT_TEXTURE = machineTexture("ashpit");
    public static final ResourceLocation SAWMILL_TEXTURE = machineTexture("sawmill");
    public static final ResourceLocation STRAND_CASTER_TEXTURE = machineTexture("strand_caster");
    public static final ResourceLocation FURNACE_STEEL_TEXTURE = machineTexture("furnace_steel");
    public static final ResourceLocation CONVEYOR_PRESS_TEXTURE = machineTexture("conveyor_press");
    public static final ResourceLocation CONVEYOR_PRESS_BELT_TEXTURE = machineTexture("conveyor_press_belt");
    public static final ResourceLocation ROTARY_FURNACE_TEXTURE = machineTexture("rotary_furnace");
    public static final ResourceLocation MICROWAVE_TEXTURE = machineTexture("microwave");
    public static final ResourceLocation REFUELER_TEXTURE = machineTexture("refueler");
    public static final ResourceLocation PISTON_INSERTER_TEXTURE = machineTexture("piston_inserter");
    public static final ResourceLocation LEGACY_FLUIDTANK_FRAME_TEXTURE = machineTexture("tank");
    public static final ResourceLocation UF6_TANK_TEXTURE = machineTexture("uf6_tank");
    public static final ResourceLocation PUF6_TANK_TEXTURE = machineTexture("puf6_tank");
    public static final ResourceLocation CENTRIFUGE_TEXTURE = machineTexture("centrifuge");
    public static final ResourceLocation CHARGER_TEXTURE = machineTexture("charger");
    public static final ResourceLocation ARC_WELDER_TEXTURE = machineTexture("arc_welder");
    public static final ResourceLocation ARC_FURNACE_TEXTURE = machineTexture("arc_furnace");
    public static final ResourceLocation GASCENT_TEXTURE = machineTexture("gascent");
    public static final ResourceLocation AUTOSAW_TEXTURE = machineTexture("autosaw");
    public static final ResourceLocation THRESHER_TEXTURE = machineTexture("thresher");
    public static final ResourceLocation IGEN_TEXTURE = machineTexture("igen");
    public static final ResourceLocation DELIVERY_DRONE_TEXTURE = machineTexture("drone");
    public static final ResourceLocation DELIVERY_DRONE_EXPRESS_TEXTURE = machineTexture("drone_express");
    public static final ResourceLocation DELIVERY_DRONE_REQUEST_TEXTURE = machineTexture("drone_request");
    public static final ResourceLocation PRESS_BODY_TEXTURE = machineTexture("press_body");
    public static final ResourceLocation PRESS_HEAD_TEXTURE = machineTexture("press_head");
    public static final ResourceLocation EPRESS_BODY_TEXTURE = machineTexture("epress_body");
    public static final ResourceLocation EPRESS_HEAD_TEXTURE = machineTexture("epress_head");
    public static final ResourceLocation ELEVATOR_TEXTURE = machineTexture("elevator");
    public static final ResourceLocation FENSU_TEXTURE = machineTexture("fensu");
    public static final ResourceLocation FAN_TEXTURE = machineTexture("fan");
    public static final ResourceLocation SOLAR_MIRROR_TEXTURE = machineTexture("solar_mirror");
    public static final ResourceLocation TELEX_TEXTURE = machineTexture("telex");
    public static final ResourceLocation AUTOCAL_TEXTURE = machineTexture("autocal");
    public static final ResourceLocation VENDING_MACHINE_TEXTURE = machineTexture("vending_machine");

    public static ObjModelPart part(String name) {
        return part(name, RenderType.cutout());
    }

    public static ObjModelPart part(String name, RenderType renderType) {
        return ObjModelLibrary.blockPart("machines/" + name, renderType);
    }

    public static ObjModelLibrary.ObjModelPartBuilder partBuilder(String name, RenderType renderType) {
        return ObjModelLibrary.blockPartBuilder("machines/" + name, renderType);
    }

    public static ObjModelLibrary.ObjModelPartBuilder directPart(String name) {
        return partBuilder(name, RenderType.cutout()).direct();
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return legacyModel(name, name);
    }

    public static LegacyWavefrontModel legacyModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                machineModel(modelName),
                machineTexture(textureName)).asVBO();
    }

    private static ResourceLocation machineModel(String name) {
        if (ROOT_DIRECT_MODELS.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj");
        }
        if (MACHINE_DIRECT_MODELS.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "models/machines/" + name + ".obj");
        }
        if (BLOCK_DIRECT_MODELS.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/" + name + ".obj");
        }
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/" + name + ".obj");
    }

    public static ResourceLocation machineTexture(String name) {
        switch (name) {
            case "core_emitter", "core_injector", "core_receiver", "epress_body", "epress_head",
                    "press_body", "press_head", "radiolysis", "refinery", "tank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
            }
            case "fluidtank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/tank.png");
            }
            case "fluidtank_inner" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/tank/tank_inner.png");
            }
            case "uf6_tank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/uf6_tank.png");
            }
            case "puf6_tank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/puf6_tank.png");
            }
            default -> {
            }
        }
        if (DIRECT_MODEL_TEXTURES.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/" + name + ".png");
        }
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + name + ".png");
    }

    private ObjMachineModels() {
    }
}
