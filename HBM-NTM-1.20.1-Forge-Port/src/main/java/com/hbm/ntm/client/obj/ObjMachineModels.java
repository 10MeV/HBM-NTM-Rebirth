package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;
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

    private ObjMachineModels() {
    }
}
