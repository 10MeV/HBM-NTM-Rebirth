package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class ObjMachineModels {
    public static final ObjModelPart PRESS_HEAD = ObjModelLibrary.directBlockPart("press_head")
            .withRenderType(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
            .withLightMultiplier(0.82F)
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjPartModel PRESS = new ObjPartModel()
            .part("Cube.001_Cube.002", PRESS_HEAD, "Head")
            .legacyOrder("Cube_Cube.000", "Cube.001_Cube.002");
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

    public static final LegacyWavefrontModel REFINERY = legacyModel("refinery");
    public static final LegacyWavefrontModel REFINERY_EXPLODED = legacyModel("refinery_exploded", "refinery");
    public static final LegacyWavefrontModel FLUIDTANK = legacyModel("fluidtank");
    public static final LegacyWavefrontModel FLUIDTANK_EXPLODED = legacyModel("fluidtank_exploded", "fluidtank");
    public static final LegacyWavefrontModel VACUUM_DISTILL = legacyModel("vacuum_distill");
    public static final LegacyWavefrontModel CATALYTIC_CRACKER = legacyModel("catalytic_cracker");
    public static final LegacyWavefrontModel CATALYTIC_REFORMER = legacyModel("catalytic_reformer");
    public static final LegacyWavefrontModel HYDROTREATER = legacyModel("hydrotreater");
    public static final LegacyWavefrontModel LIQUEFACTOR = legacyModel("liquefactor");
    public static final LegacyWavefrontModel SOLIDIFIER = legacyModel("solidifier");
    public static final LegacyWavefrontModel COMPRESSOR = legacyModel("compressor");
    public static final LegacyWavefrontModel COKER = legacyModel("coker");
    public static final LegacyWavefrontModel PYROOVEN = legacyModel("pyrooven");
    public static final LegacyWavefrontModel BAT9000 = legacyModel("bat9000");
    public static final LegacyWavefrontModel BIGASSTANK = legacyModel("bigasstank");
    public static final LegacyWavefrontModel ORBUS = legacyModel("orbus");
    public static final LegacyWavefrontModel TURBOFAN = legacyModel("turbofan");
    public static final LegacyWavefrontModel TURBINEGAS = legacyModel("turbinegas");
    public static final LegacyWavefrontModel STEAM_ENGINE = legacyModel("steam_engine");
    public static final LegacyWavefrontModel INDUSTRIAL_TURBINE = legacyModel("industrial_turbine");
    public static final LegacyWavefrontModel CHUNGUS = legacyModel("chungus");
    public static final LegacyWavefrontModel TOWER_SMALL = legacyModel("tower_small");
    public static final LegacyWavefrontModel TOWER_LARGE = legacyModel("tower_large");
    public static final LegacyWavefrontModel CONDENSER = legacyModel("condenser");
    public static final LegacyWavefrontModel WOOD_BURNER = legacyModel("wood_burner");
    public static final LegacyWavefrontModel COMBUSTION_ENGINE = legacyModel("combustion_engine");
    public static final LegacyWavefrontModel PUMP = legacyModel("pump");
    public static final LegacyWavefrontModel AMMO_PRESS = legacyModel("ammo_press");
    public static final LegacyWavefrontModel ANNIHILATOR = legacyModel("annihilator");
    public static final LegacyWavefrontModel ASSEMBLY_FACTORY = legacyModel("assembly_factory");
    public static final LegacyWavefrontModel CHEMICAL_PLANT = legacyModel("chemical_plant");
    public static final LegacyWavefrontModel CHEMICAL_FACTORY = legacyModel("chemical_factory");
    public static final LegacyWavefrontModel PUREX = legacyModel("purex");
    public static final LegacyWavefrontModel MIXER = legacyModel("mixer");
    public static final LegacyWavefrontModel FIREBOX_LEGACY = legacyModel("firebox").noSmooth().asVBO();
    public static final LegacyWavefrontModel HEATING_OVEN_LEGACY = legacyModel("heating_oven").noSmooth().asVBO();
    public static final LegacyWavefrontModel ELECTRIC_HEATER_LEGACY = legacyModel("electric_heater").noSmooth().asVBO();
    public static final LegacyWavefrontModel RTG_LEGACY = legacyModel("rtg").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_BODY_LEGACY = legacyModel("radar_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_LEGACY = legacyModel("radar", "radar_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_LARGE_LEGACY = legacyModel("radar_large").noSmooth().asVBO();
    public static final LegacyWavefrontModel RADAR_SCREEN_LEGACY = legacyModel("radar_screen").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOLAR_MIRROR_LEGACY = legacyModel("solar_mirror").noSmooth();
    public static final LegacyWavefrontModel ARC_WELDER = legacyModel("arc_welder").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOLDERING_STATION = legacyModel("soldering_station").noSmooth().asVBO();
    public static final LegacyWavefrontModel ARC_FURNACE = legacyModel("arc_furnace");
    public static final LegacyWavefrontModel CENTRIFUGE = legacyModel("centrifuge");
    public static final LegacyWavefrontModel GASCENT = legacyModel("gascent");
    public static final LegacyWavefrontModel SILEX = legacyModel("silex");
    public static final LegacyWavefrontModel FEL = legacyModel("fel");
    public static final LegacyWavefrontModel AUTOSAW = legacyModel("autosaw").noSmooth().asVBO();
    public static final LegacyWavefrontModel MINING_DRILL = legacyModel("mining_drill");
    public static final LegacyWavefrontModel ORE_SLOPPER = legacyModel("ore_slopper");
    public static final LegacyWavefrontModel MINING_LASER = legacyModel("mining_laser");
    public static final LegacyWavefrontModel ACIDIZER = legacyModel("acidizer");
    public static final LegacyWavefrontModel CYCLOTRON = legacyModel("cyclotron");
    public static final LegacyWavefrontModel EXPOSURE_CHAMBER = legacyModel("exposure_chamber");
    public static final LegacyWavefrontModel DEUTERIUM_TOWER = legacyModel("machine_deuterium_tower");
    public static final LegacyWavefrontModel RADGEN = legacyModel("radgen");
    public static final LegacyWavefrontModel RADIOLYSIS = legacyModel("radiolysis");
    public static final LegacyWavefrontModel ROTARY_FURNACE = legacyModel("rotary_furnace");
    public static final LegacyWavefrontModel ELECTROLYSER = legacyModel("electrolyser");
    public static final LegacyWavefrontModel CHARGER = legacyModel("charger");
    public static final LegacyWavefrontModel REFUELER = legacyModel("refueler");
    public static final LegacyWavefrontModel SOLAR_BOILER = legacyModel("solar_boiler");
    public static final LegacyWavefrontModel DFC_EMITTER = legacyModel("core_emitter");
    public static final LegacyWavefrontModel DFC_RECEIVER = legacyModel("core_receiver");
    public static final LegacyWavefrontModel DFC_INJECTOR = legacyModel("core_injector");
    public static final LegacyWavefrontModel STIRLING = legacyModel("stirling");
    public static final LegacyWavefrontModel SAWMILL = legacyModel("sawmill");
    public static final LegacyWavefrontModel STRAND_CASTER = legacyModel("strand_caster");
    public static final LegacyWavefrontModel FURNACE_STEEL = legacyModel("furnace_steel");
    public static final LegacyWavefrontModel CONVEYOR_PRESS = legacyModel("conveyor_press");
    public static final LegacyWavefrontModel MICROWAVE = legacyModel("microwave");
    public static final LegacyWavefrontModel PISTON_INSERTER = legacyModel("piston_inserter");
    public static final LegacyWavefrontModel IGEN = legacyModel("igen");
    public static final LegacyWavefrontModel DELIVERY_DRONE = legacyModel("drone");

    public static final ResourceLocation FLUIDTANK_INNER_TEXTURE = machineTexture("fluidtank_inner");
    public static final ResourceLocation TURBOFAN_BACK_TEXTURE = machineTexture("turbofan_back");
    public static final ResourceLocation TURBOFAN_AFTERBURNER_TEXTURE = machineTexture("turbofan_afterburner");
    public static final ResourceLocation TURBOFAN_BLADES_TEXTURE = machineTexture("turbofan_blades");
    public static final ResourceLocation PUMP_ELECTRIC_TEXTURE = machineTexture("pump_electric");
    public static final ResourceLocation ANNIHILATOR_BELT_TEXTURE = machineTexture("annihilator_belt");
    public static final ResourceLocation ASSEMBLY_FACTORY_SPARKS_TEXTURE = machineTexture("assembly_factory_sparks");
    public static final ResourceLocation CHEMICAL_PLANT_FLUID_TEXTURE = machineTexture("chemical_plant_fluid");
    public static final ResourceLocation MINING_LASER_PIVOT_TEXTURE = machineTexture("mining_laser_pivot");
    public static final ResourceLocation MINING_LASER_LASER_TEXTURE = machineTexture("mining_laser_laser");
    public static final ResourceLocation STIRLING_TEXTURE = machineTexture("stirling");
    public static final ResourceLocation STIRLING_STEEL_TEXTURE = machineTexture("stirling_steel");
    public static final ResourceLocation STIRLING_CREATIVE_TEXTURE = machineTexture("stirling_creative");
    public static final ResourceLocation SAWMILL_TEXTURE = machineTexture("sawmill");
    public static final ResourceLocation STRAND_CASTER_TEXTURE = machineTexture("strand_caster");
    public static final ResourceLocation FURNACE_STEEL_TEXTURE = machineTexture("furnace_steel");
    public static final ResourceLocation CONVEYOR_PRESS_TEXTURE = machineTexture("conveyor_press");
    public static final ResourceLocation CONVEYOR_PRESS_BELT_TEXTURE = machineTexture("conveyor_press_belt");
    public static final ResourceLocation MICROWAVE_TEXTURE = machineTexture("microwave");
    public static final ResourceLocation PISTON_INSERTER_TEXTURE = machineTexture("piston_inserter");
    public static final ResourceLocation IGEN_TEXTURE = machineTexture("igen");
    public static final ResourceLocation DELIVERY_DRONE_TEXTURE = machineTexture("drone");
    public static final ResourceLocation DELIVERY_DRONE_EXPRESS_TEXTURE = machineTexture("drone_express");
    public static final ResourceLocation DELIVERY_DRONE_REQUEST_TEXTURE = machineTexture("drone_request");

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
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/" + modelName + ".obj"),
                machineTexture(textureName));
    }

    public static ResourceLocation machineTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + name + ".png");
    }

    private ObjMachineModels() {
    }
}
