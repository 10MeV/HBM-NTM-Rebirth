package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjProjectileModels {
    public static final LegacyWavefrontModel PROJECTILES = directModel("projectiles", "rocket");
    public static final LegacyWavefrontModel LEADBURSTER = directModel("leadburster", "leadburster");
    public static final LegacyWavefrontModel DEBRIS_BLANK = model("deb_blank", "rbmk_blank_side");
    public static final LegacyWavefrontModel DEBRIS_ELEMENT = model("deb_element", "rbmk_side");
    public static final LegacyWavefrontModel DEBRIS_FUEL = model("deb_fuel", "rbmk_fuel");
    public static final LegacyWavefrontModel DEBRIS_ROD = model("deb_rod", "rbmk_control");
    public static final LegacyWavefrontModel DEBRIS_LID = model("deb_lid", "rbmk_blank_cover_top");
    public static final LegacyWavefrontModel DEBRIS_GRAPHITE = model("deb_graphite", "block_graphite");
    public static final LegacyWavefrontModel ZIRNOX_DEBRIS_BLANK = model("zirnox_deb_blank", "zirnox");
    public static final LegacyWavefrontModel ZIRNOX_DEBRIS_CONCRETE = model("zirnox_deb_concrete", "zirnox_destroyed");
    public static final LegacyWavefrontModel ZIRNOX_DEBRIS_ELEMENT = model("zirnox_deb_element", "zirnox_deb_element");
    public static final LegacyWavefrontModel ZIRNOX_DEBRIS_EXCHANGER = model("zirnox_deb_exchanger", "zirnox");
    public static final LegacyWavefrontModel ZIRNOX_DEBRIS_SHRAPNEL = model("zirnox_deb_shrapnel", "zirnox");
    public static final LegacyWavefrontModel BOMBLET_ZETA = model("bomblet_theta", "bomblet_zeta");

    public static final ResourceLocation HIMARS_STANDARD_TEXTURE = directProjectileTexture("himars_standard");
    public static final ResourceLocation HIMARS_SINGLE_TEXTURE = directProjectileTexture("himars_single");
    public static final ResourceLocation HIMARS_STANDARD_HE_TEXTURE = directProjectileTexture("himars_standard_he");
    public static final ResourceLocation HIMARS_STANDARD_LAVA_TEXTURE = directProjectileTexture("himars_standard_lava");
    public static final ResourceLocation HIMARS_STANDARD_MINI_NUKE_TEXTURE =
            directProjectileTexture("himars_standard_mini_nuke");
    public static final ResourceLocation HIMARS_STANDARD_TB_TEXTURE = directProjectileTexture("himars_standard_tb");
    public static final ResourceLocation HIMARS_STANDARD_WP_TEXTURE = directProjectileTexture("himars_standard_wp");
    public static final ResourceLocation HIMARS_SINGLE_LAVA_TEXTURE = directProjectileTexture("himars_single_lava");
    public static final ResourceLocation HIMARS_SINGLE_TB_TEXTURE = directProjectileTexture("himars_single_tb");
    public static final ResourceLocation BULLET_PISTOL_TEXTURE = directProjectileTexture("bullet_pistol");
    public static final ResourceLocation BULLET_RIFLE_TEXTURE = directProjectileTexture("bullet_rifle");
    public static final ResourceLocation BUCKSHOT_TEXTURE = directProjectileTexture("pellet_buckshot");
    public static final ResourceLocation FLECHETTE_TEXTURE = directProjectileTexture("flechette");
    public static final ResourceLocation GRENADE_TEXTURE = directProjectileTexture("grenade");
    public static final ResourceLocation ROCKET_TEXTURE = directProjectileTexture("rocket");
    public static final ResourceLocation ROCKET_MIRV_TEXTURE = directProjectileTexture("rocket_mirv");
    public static final ResourceLocation MINI_NUKE_TEXTURE = directProjectileTexture("mini_nuke");
    public static final ResourceLocation MINI_MIRV_TEXTURE = directProjectileTexture("mini_mirv");
    public static final ResourceLocation LEADBURSTER_TEXTURE = directProjectileTexture("leadburster");
    public static final ResourceLocation RBMK_DEBRIS_SIDE_TEXTURE = texture("rbmk_side");
    public static final ResourceLocation RBMK_DEBRIS_FUEL_TEXTURE = texture("rbmk_fuel");
    public static final ResourceLocation RBMK_DEBRIS_CONTROL_TEXTURE = texture("rbmk_control");
    public static final ResourceLocation RBMK_DEBRIS_BLANK_TEXTURE = texture("rbmk_blank_side");
    public static final ResourceLocation RBMK_DEBRIS_LID_TEXTURE = texture("rbmk_blank_cover_top");
    public static final ResourceLocation GRAPHITE_TEXTURE = texture("block_graphite");
    public static final ResourceLocation ZIRNOX_TEXTURE = texture("zirnox");
    public static final ResourceLocation ZIRNOX_DESTROYED_TEXTURE = texture("zirnox_destroyed");
    public static final ResourceLocation ZIRNOX_DEBRIS_ELEMENT_TEXTURE = texture("zirnox_deb_element");
    public static final ResourceLocation BOMBLET_ZETA_TEXTURE = texture("bomblet_zeta");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                modelLocation(modelName),
                texture(textureName));
    }

    private static ResourceLocation modelLocation(String modelName) {
        String path = switch (modelName) {
            case "deb_blank", "deb_element", "deb_fuel", "deb_rod", "deb_lid", "deb_graphite" ->
                    "models/projectiles/" + modelName + ".obj";
            case "zirnox_deb_blank" -> "models/zirnox/deb_blank.obj";
            case "zirnox_deb_concrete" -> "models/zirnox/deb_concrete.obj";
            case "zirnox_deb_element" -> "models/zirnox/deb_element.obj";
            case "zirnox_deb_exchanger" -> "models/zirnox/deb_exchanger.obj";
            case "zirnox_deb_shrapnel" -> "models/zirnox/deb_shrapnel.obj";
            case "bomblet_theta" -> "models/bomblet_theta.obj";
            default -> "models/block/projectiles/" + modelName + ".obj";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    public static LegacyWavefrontModel directModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/projectiles/" + modelName + ".obj"),
                directProjectileTexture(textureName));
    }

    public static ResourceLocation texture(String name) {
        String path = switch (name) {
            case "rbmk_side", "rbmk_fuel", "rbmk_control", "rbmk_blank_side", "rbmk_blank_cover_top" ->
                    "textures/block/rbmk/" + name + ".png";
            case "block_graphite" -> "textures/block/block_graphite.png";
            case "zirnox", "zirnox_destroyed" -> "textures/models/" + name + ".png";
            case "zirnox_deb_element" -> "textures/models/machines/zirnox_deb_element.png";
            case "bomblet_zeta" -> "textures/models/bomblet_zeta_texture.png";
            default -> "textures/block/projectiles/" + name + ".png";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    public static ResourceLocation directProjectileTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/" + name + ".png");
    }

    private ObjProjectileModels() {
    }
}
