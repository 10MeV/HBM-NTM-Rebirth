package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class HbmNetworkActions {
    public static final ResourceLocation DESIGNATOR = hbm("designator");
    public static final ResourceLocation BOBMAZON_OFFER = hbm("bobmazon_offer");
    public static final ResourceLocation ANVIL_CRAFT = hbm("anvil_craft");
    public static final ResourceLocation SATELLITE_COORDINATE = hbm("satellite_coordinate");
    public static final ResourceLocation SATELLITE_LASER = hbm("satellite_laser");
    public static final ResourceLocation SATELLITE_PANEL = hbm("satellite_panel");
    public static final ResourceLocation NBT_ITEM_CONTROL = hbm("nbt_item_control");
    public static final ResourceLocation VAULT_DOOR = hbm("vault_door");
    public static final ResourceLocation SIREN = hbm("siren");
    public static final ResourceLocation FORCE_FIELD = hbm("force_field");

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private HbmNetworkActions() {
    }
}
