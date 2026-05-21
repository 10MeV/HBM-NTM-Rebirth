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
