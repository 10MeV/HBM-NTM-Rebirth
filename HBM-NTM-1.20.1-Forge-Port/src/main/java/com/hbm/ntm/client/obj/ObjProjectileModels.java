package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjProjectileModels {
    public static final LegacyWavefrontModel PROJECTILES = model("projectiles", "rocket");
    public static final LegacyWavefrontModel LEADBURSTER = model("leadburster", "leadburster");

    public static final ResourceLocation HIMARS_STANDARD_TEXTURE = texture("himars_standard");
    public static final ResourceLocation HIMARS_SINGLE_TEXTURE = texture("himars_single");
    public static final ResourceLocation HIMARS_STANDARD_HE_TEXTURE = texture("himars_standard_he");
    public static final ResourceLocation HIMARS_STANDARD_LAVA_TEXTURE = texture("himars_standard_lava");
    public static final ResourceLocation HIMARS_STANDARD_MINI_NUKE_TEXTURE = texture("himars_standard_mini_nuke");
    public static final ResourceLocation HIMARS_STANDARD_TB_TEXTURE = texture("himars_standard_tb");
    public static final ResourceLocation HIMARS_STANDARD_WP_TEXTURE = texture("himars_standard_wp");
    public static final ResourceLocation HIMARS_SINGLE_LAVA_TEXTURE = texture("himars_single_lava");
    public static final ResourceLocation HIMARS_SINGLE_TB_TEXTURE = texture("himars_single_tb");
    public static final ResourceLocation BULLET_PISTOL_TEXTURE = texture("bullet_pistol");
    public static final ResourceLocation BULLET_RIFLE_TEXTURE = texture("bullet_rifle");
    public static final ResourceLocation BUCKSHOT_TEXTURE = texture("pellet_buckshot");
    public static final ResourceLocation FLECHETTE_TEXTURE = texture("flechette");
    public static final ResourceLocation GRENADE_TEXTURE = texture("grenade");
    public static final ResourceLocation ROCKET_TEXTURE = texture("rocket");
    public static final ResourceLocation ROCKET_MIRV_TEXTURE = texture("rocket_mirv");
    public static final ResourceLocation MINI_NUKE_TEXTURE = texture("mini_nuke");
    public static final ResourceLocation MINI_MIRV_TEXTURE = texture("mini_mirv");
    public static final ResourceLocation LEADBURSTER_TEXTURE = texture("leadburster");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/projectiles/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/projectiles/" + name + ".png");
    }

    private ObjProjectileModels() {
    }
}
