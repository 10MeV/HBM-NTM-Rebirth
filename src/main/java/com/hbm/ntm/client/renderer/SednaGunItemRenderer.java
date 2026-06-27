package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.obj.ObjTrinketModels;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.sound.LegacyClientSoundPlayer;
import com.hbm.ntm.item.Ni4NiGunItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.StingerGunItem;
import com.hbm.ntm.util.RayTraceUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SednaGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final double LEGACY_GUI_SLOT_PIXELS = 16.0D;
    private static final double LEGACY_GUI_UNIT = 1.0D / LEGACY_GUI_SLOT_PIXELS;
    private static final double FIRST_PERSON_SCREEN_UNIT = 0.25D;
    private static final int LEGACY_ANIM_CYCLE = 3;
    private static final int LEGACY_ANIM_CYCLE_DRY = 5;
    private static final int LEGACY_ANIM_EQUIP = 9;
    private static final int LEGACY_ANIM_INSPECT = 10;
    private static final String LEGACY_LAST_ANIM_KEY = "lastanim_0";
    private static final String LEGACY_ANIM_TIMER_KEY = "animtimer_0";
    private static final ResourceLocation FATMAN_MININUKE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/fatman_mininuke.png");
    private static final ResourceLocation FATMAN_BALEFIRE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/fatman_balefire.png");
    private static final ResourceLocation DANI_CELESTIAL_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/dani_celestial.png");
    private static final ResourceLocation DANI_LUNAR_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/dani_lunar.png");
    private static final ResourceLocation FIREEXT_FOAM_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/fireext_foam.png");
    private static final ResourceLocation FIREEXT_SAND_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/fireext_sand.png");
    private static final ResourceLocation CHARGE_THROWER_HOOK_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/charge_thrower_hook.png");
    private static final ResourceLocation CHARGE_THROWER_MORTAR_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/charge_thrower_mortar.png");
    private static final ResourceLocation QUADRO_ROCKET_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/quadro_rocket.png");
    private static final ResourceLocation PANZERSCHRECK_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/panzerschreck.png");
    private static final ResourceLocation LASRIFLE_MODS_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/lasrifle_mods.png");
    private static final ResourceLocation G3_ATTACHMENTS_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/g3_attachments.png");
    private static final ResourceLocation G3_GREEN_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/g3_polymer_green.png");
    private static final ResourceLocation G3_BLACK_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/g3_polymer_black.png");
    private static final ResourceLocation UZI_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/uzi.png");
    private static final ResourceLocation UZI_SATURNITE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/uzi_saturnite.png");
    private static final ResourceLocation STAR_F_ELITE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/star_f_elite.png");
    private static final ResourceLocation GREASEGUN_CLEAN_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/greasegun_clean.png");
    private static final ResourceLocation CARBINE_SCOPE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/carbine_scope.png");
    private static final ResourceLocation CARBINE_BAYONET_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/carbine_bayonet.png");
    private static final ResourceLocation LILMAC_SCOPE_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/lilmac_scope.png");
    private static long follyAimStartMillis;
    private static boolean follyJingle;
    private static boolean follyWasAiming;

    private static final Map<String, RenderSpec> SPECS = Map.ofEntries(
            specOnly("gun_pepperbox", "pepperbox", "pepperbox",
                    inv(1.5D, 0.5D, 0.5D, 0.0D), fp(0.25D, 1.5D, -1.0D, -0.6D, 0.8D),
                    "Grip", "Cylinder", "Hammer", "Trigger"),
            spec("gun_light_revolver", "bio_revolver", "bio_revolver",
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D)),
            spec("gun_light_revolver_atlas", "bio_revolver", "bio_revolver_atlas",
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D)),
            specAkimbo("gun_light_revolver_dani", "bio_revolver", "dani_celestial", SpecialRender.DANI,
                    inv(1.125D, 0.0D, -2.0D, 0.0D), fp(0.125D, 0.875D, 0.0D, 0.0D, 0.0D)),
            spec("gun_henry", "henry", "henry",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.4D)),
            spec("gun_henry_lincoln", "henry", "henry_lincoln",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.4D)),
            specSpecial("gun_heavy_revolver", "lilmac", "heavy_revolver", SpecialRender.HEAVY_REVOLVER,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D),
                    "Gun", "Cylinder", "Bullets", "Casings", "Pivot", "Hammer", "Scope"),
            specSpecial("gun_heavy_revolver_lilmac", "lilmac", "lilmac", SpecialRender.HEAVY_REVOLVER,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D),
                    "Gun", "Cylinder", "Bullets", "Casings", "Pivot", "Hammer", "Scope"),
            specSpecial("gun_heavy_revolver_protege", "lilmac", "protege", SpecialRender.HEAVY_REVOLVER,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D),
                    "Gun", "Cylinder", "Bullets", "Casings", "Pivot", "Hammer", "Scope"),
            specSpecial("gun_greasegun", "greasegun", "greasegun", SpecialRender.GREASEGUN,
                    inv(1.5D, -0.5D, 2.0D, 0.0D), fp(0.375D, 0.875D, -1.5D * 0.8D, -1.0D * 0.8D, 1.75D * 0.8D)),
            specSpecial("gun_maresleg", "maresleg", "maresleg", SpecialRender.MARESLEG,
                    inv(1.4375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)),
            specAkimbo("gun_maresleg_akimbo", "maresleg", "maresleg", SpecialRender.MARESLEG_AKIMBO,
                    inv(2.5D, 0.0D, 0.0D, 0.0D), fp(0.375D, 0.875D, 0.0D, 0.0D, 0.0D),
                    "Gun", "Lever"),
            specSpecial("gun_maresleg_broken", "maresleg", "maresleg_broken", SpecialRender.MARESLEG,
                    inv(1.4375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)),
            spec("gun_flaregun", "flaregun", "flaregun",
                    inv(1.0D, -0.5D, 0.0D, 0.0D), fp(0.125D, 0.875D, -1.0D, -1.2D, 1.6D)),
            specSpecial("gun_panzerschreck", "panzerschreck", "panzerschreck", SpecialRender.PANZERSCHRECK,
                    inv(1.5D, -0.5D, 0.5D, 0.0D),
                    fp(1.25D, 0.875D, -2.75D * 0.8D, -2.0D * 0.8D, 2.5D * 0.8D),
                    "Tube", "Shield"),
            specSpecial("gun_carbine", "carbine", "huntsman", SpecialRender.CARBINE,
                    inv(1.375D, -0.5D, 0.0D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.2D, 0.7D)),
            specOnly("gun_minigun", "minigun", "minigun",
                    inv(0.875D, -0.25D, 0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -1.75D * 0.8D, 3.5D * 0.8D),
                    "Gun", "Grip", "Barrels"),
            specOnly("gun_minigun_lacunae", "minigun", "minigun_lacunae",
                    inv(0.875D, -0.25D, 0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -1.75D * 0.8D, 3.5D * 0.8D),
                    "Gun", "Grip", "Barrels"),
            specAkimbo("gun_minigun_dual", "minigun", "minigun_dual", SpecialRender.MINIGUN_DUAL,
                    inv(0.875D, 0.0D, 0.0D, 0.0D), fp(0.375D, 0.875D, 0.0D, 0.0D, 0.0D),
                    "Gun", "GunDual", "Barrels"),
            specSpecial("gun_am180", "am180", "am180", SpecialRender.AM180,
                    inv(0.75D, 1.5D, 0.0D, 0.0D), fp(0.1875D, 0.875D, -0.8D, -0.8D, 0.8D),
                    "Gun", "Silencer", "Trigger", "Bolt", "Mag", "MagPlate"),
            spec("gun_liberator", "liberator", "liberator",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.2D, -1.0D, 1.0D)),
            spec("gun_congolake", "congolake", "congolake",
                    inv(2.5D, 0.0D, -1.25D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.6D, 1.0D)),
            specSpecial("gun_lag", "mike_hawk", "lag", SpecialRender.LAG,
                    inv(1.5D, 2.5D, 1.0D, 0.0D), fp(0.25D, 0.875D, -1.2D, -0.8D, 1.2D),
                    "Grip", "Slide", "Hammer"),
            specSpecial("gun_uzi", "uzi", "uzi", SpecialRender.UZI,
                    inv(1.5D, 0.0D, 1.0D, 0.0D), fp(0.25D, 0.875D, -1.4D, -1.2D, 2.0D)),
            specAkimbo("gun_uzi_akimbo", "uzi", "uzi", SpecialRender.UZI_AKIMBO,
                    inv(1.5D, 0.0D, 0.0D, 0.0D), fp(0.25D, 0.875D, 0.0D, 0.0D, 0.0D),
                    "Gun", "GunMirror", "StockBack", "StockFront", "Slide", "Magazine", "Silencer"),
            specSpecial("gun_spas12", "spas-12", "spas-12", SpecialRender.SPAS12,
                    inv(2.0D, 4.25D, -0.5D, 0.0D), fp(0.5D, 0.875D, -1.0D, -1.4D, -0.4D)),
            specSpecial("gun_stinger", "stinger", "stinger", SpecialRender.STINGER,
                    inv(1.0625D, 0.25D, -2.5D, 0.0D, 225.0D),
                    fp(1.5D, 0.875D, -3.75D * 0.8D, -9.0D * 0.8D, -3.5D * 0.8D)),
            specSpecial("gun_star_f", "star_f", "star_f", SpecialRender.STAR_F,
                    inv(1.5D, -1.0D, -0.5D, 0.0D), fp(0.25D, 0.875D, -1.4D, -1.4D, 2.0D)),
            specAkimbo("gun_star_f_akimbo", "star_f", "star_f_elite", SpecialRender.STAR_F_AKIMBO,
                    inv(1.5D, 0.0D, 0.0D, 0.0D), fp(0.25D, 0.875D, 0.0D, 0.0D, 0.0D),
                    "Gun", "Slide", "Mag", "Hammer"),
            specSpecial("gun_g3", "g3", "g3", SpecialRender.G3,
                    inv(0.875D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 2.2D)),
            specSpecial("gun_g3_zebra", "g3", "g3_zebra", SpecialRender.G3,
                    inv(0.875D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 2.2D)),
            specSpecial("gun_mk108", "mk108", "mk108", SpecialRender.MK108,
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -1.2D, 2.0D),
                    "Gun", "Barrel", "Lid", "Drum", "Belt", "Grenade"),
            specSpecial("gun_amat", "amat", "amat", SpecialRender.AMAT,
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            specSpecial("gun_amat_subtlety", "amat", "amat_subtlety", SpecialRender.AMAT,
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            specSpecial("gun_amat_penance", "amat", "amat_penance", SpecialRender.AMAT,
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            specSpecial("gun_m2", "m2_browning", "m2_browning", SpecialRender.M2,
                    inv(2.625D, 0.5D, -1.25D, 0.0D),
                    fp(0.75D, 0.875D, -1.5D * 0.8D, -2.5D * 0.8D, 1.75D * 0.8D)),
            specSpecial("gun_aberrator", "aberrator", "aberrator", SpecialRender.ABERRATOR,
                    inv(2.5D, -0.5D, -1.0D, 0.0D),
                    fp(0.25D, 1.0D, -1.0D * 0.8D, -1.25D * 0.8D, 1.25D * 0.8D),
                    "Gun", "Hammer", "Magazine", "Slide", "Sight"),
            specAkimbo("gun_aberrator_eott", "aberrator", "eott", SpecialRender.EOTT,
                    inv(2.5D, 0.0D, 0.0D, 0.0D), fp(0.25D, 1.0D, 0.0D, 0.0D, 0.0D),
                    "Gun", "Hammer", "Magazine", "Slide", "Sight"),
            specSpecial("gun_laser_pistol", "laser_pistol", "laser_pistol", SpecialRender.LASER_PISTOL,
                    inv(1.75D, 0.0D, -0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -2.0D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Latch"),
            specSpecial("gun_laser_pistol_pew_pew", "laser_pistol", "laser_pistol_pew_pew",
                    SpecialRender.LASER_PISTOL,
                    inv(1.75D, 0.0D, -0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -2.0D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Latch", "Capacitors", "Tape"),
            specSpecial("gun_laser_pistol_morning_glory", "laser_pistol", "laser_pistol_morning_glory",
                    SpecialRender.LASER_PISTOL,
                    inv(1.75D, 0.0D, -0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -2.0D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Latch"),
            spec("gun_autoshotgun", "shredder", "shredder",
                    inv(1.25D, -1.5D, 0.0D, 0.0D), fp(0.25D, 0.875D, -1.2D, -1.0D, 1.2D)),
            spec("gun_autoshotgun_shredder", "shredder", "shredder_orig",
                    inv(1.25D, -1.5D, 0.0D, 0.0D),
                    fp(0.25D, 0.875D, -1.5D * 0.8D, -1.25D * 0.8D, 1.5D * 0.8D)),
            specSpecial("gun_quadro", "quadro", "quadro", SpecialRender.QUADRO,
                    inv(4.75D, 0.0D, -1.0D, 0.0D),
                    fp(1.75D, 0.875D, -2.5D * 0.8D, -3.5D * 0.8D, 2.5D * 0.8D),
                    "Launcher"),
            specSpecial("gun_autoshotgun_sexy", "sexy", "sexy_real_no_fake", SpecialRender.SEXY,
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -0.6D, 2.4D),
                    "Gun", "Barrel", "RecoilSpring", "Hood", "Lever", "LockSpring", "Magazine", "Belt", "Shell"),
            specSpecial("gun_autoshotgun_heretic", "sexy", "sexy_heretic", SpecialRender.SEXY,
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -0.6D, 2.4D),
                    "Gun", "Barrel", "RecoilSpring", "Hood", "Lever", "LockSpring", "Magazine", "Belt", "Shell"),
            specSpecial("gun_stg77", "stg77", "stg77", SpecialRender.STG77,
                    inv(1.375D, -0.5D, 0.5D, 0.0D), fp(0.5D, 0.875D, -1.2D, -0.8D, 2.0D),
                    "Gun", "Barrel", "Lever", "Magazine", "Safety", "Handle", "Breech"),
            spec("gun_hangman", "hangman", "hangman",
                    inv(0.375D, -0.5D, 2.5D, 0.0D), fp(0.125D, 0.875D, -1.2D, -0.7D, 1.4D)),
            specSpecial("gun_mas36", "mas36", "mas36", SpecialRender.MAS36,
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.2D, -1.0D, 1.4D)),
            specSpecial("gun_bolter", "bolter", "bolter", SpecialRender.BOLTER,
                    inv(2.75D, -0.25D, -0.5D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.6D, 2.0D),
                    "Body", "Mag", "Bullet", "Casing"),
            specSpecial("gun_missile_launcher", "missile_launcher", "missile_launcher",
                    SpecialRender.MISSILE_LAUNCHER,
                    inv(1.5D, 0.0D, -0.5D, 0.0D),
                    fp(0.5D, 0.875D, -1.5D * 0.8D, -1.25D * 0.8D, 0.5D * 0.8D),
                    "Launcher", "Barrel", "Front"),
            specSpecial("gun_lasrifle", "lasrifle", "lasrifle", SpecialRender.LASRIFLE,
                    inv(1.03125D, 0.75D, 0.0D, 0.0D),
                    fp(0.3125D, 0.875D, -1.5D * 0.8D, -1.5D * 0.8D, 2.5D * 0.8D),
                    "Gun", "Stock", "Scope", "Lever", "Battery", "Barrel"),
            specSpecial("gun_tau", "tau", "tau", SpecialRender.TAU,
                    inv(2.0D, -0.25D, 0.5D, 0.0D),
                    fp(0.75D, 0.875D, -1.75D * 0.8D, -1.75D * 0.8D, 3.5D * 0.8D),
                    "Body", "Rotor"),
            specTesla("gun_tesla_cannon", "tesla_cannon", "tesla_cannon",
                    inv(1.25D, 0.0D, 0.5D, 0.0D),
                    fp(0.75D, 0.875D, -1.75D * 0.8D, -0.5D * 0.8D, 1.75D * 0.8D)),
            specSpecial("gun_coilgun", "coilgun", "coilgun", SpecialRender.COILGUN,
                    inv(4.0D, -0.25D, -0.25D, 0.0D),
                    fp(0.75D, 0.875D, -1.25D * 0.8D, -1.5D * 0.8D, 2.5D * 0.8D)),
            specSpecial("gun_flamer", "flamethrower", "flamethrower", SpecialRender.FLAMER,
                    inv(1.25D, -1.0D, 1.0D, 0.0D),
                    fp(0.375D, 0.875D, -1.5D * 0.8D, -1.5D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Tank", "Gauge"),
            specSpecial("gun_flamer_topaz", "flamethrower", "flamethrower_topaz", SpecialRender.FLAMER,
                    inv(1.25D, -1.0D, 1.0D, 0.0D),
                    fp(0.375D, 0.875D, -1.5D * 0.8D, -1.5D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Tank", "Gauge"),
            specSpecial("gun_flamer_daybreaker", "flamethrower", "flamethrower_daybreaker", SpecialRender.FLAMER,
                    inv(1.25D, -1.0D, 1.0D, 0.0D),
                    fp(0.375D, 0.875D, -1.5D * 0.8D, -1.5D * 0.8D, 2.75D * 0.8D),
                    "Gun", "Tank", "Gauge", "HeatShield"),
            specSpecial("gun_chemthrower", "chemthrower", "chemthrower", SpecialRender.CHEMTHROWER,
                    inv(2.0D, 0.875D, 0.0D, 0.0D),
                    fp(0.75D, 0.875D, -2.5D * 0.8D, -2.5D * 0.8D, 2.5D * 0.8D),
                    "Gun", "Hose", "Nozzle", "Gauge"),
            specFatman("gun_fatman", "fatman", "fatman",
                    inv(1.375D, 0.0D, -0.5D, 0.0D),
                    fp(0.5D, 0.875D, -1.5D * 0.8D, -1.25D * 0.8D, 0.5D * 0.8D)),
            specSpecial("gun_folly", "folly", "moonlight", SpecialRender.FOLLY,
                    inv(1.25D, 0.0D, -0.5D, 0.0D),
                    fp(0.75D, 0.875D, -2.5D * 0.8D, -1.5D * 0.8D, 2.75D * 0.8D)),
            specSpecial("gun_fireext", "fireext", "fireext_normal", SpecialRender.FIREEXT,
                    inv(4.5D, 0.0D, 0.0D, 0.0D),
                    fp(0.35D, 0.0D, 0.5D, -0.5D, -0.5D)),
            specSpecial("gun_charge_thrower", "charge_thrower", "charge_thrower",
                    SpecialRender.CHARGE_THROWER,
                    inv(1.25D, 0.0D, 0.0D, -0.625D),
                    fp(0.5D, 0.875D, -1.5D * 0.8D, -1.25D * 0.8D, 3.5D * 0.8D),
                    "Gun", "Scope", "Hook", "Mortar", "Oomph"),
            specSpecial("gun_n_i_4_n_i", "n_i_4_n_i", "n_i_4_n_i",
                    SpecialRender.NI4NI,
                    inv(2.5D, 0.0D, 0.0D, 0.0D),
                    fp(0.3125D, 1.0D, -1.0D * 0.8D, -1.0D * 0.8D, 1.0D * 0.8D),
                    "FrameDark", "Grip", "FrameLight", "Cylinder", "CylinderHighlights", "Barrel",
                    "Coin1", "Coin2", "Coin3", "Coin4"),
            specSpecial("gun_drill", "drill", "drill", SpecialRender.DRILL,
                    inv(1.25D, -0.5D, 0.0D, 0.0D),
                    fp(0.375D, 0.875D, -1.25D * 0.8D, -1.75D * 0.8D, 1.75D * 0.8D),
                    "Base", "Gauge", "Piston1", "Piston2", "Piston3", "DrillBack", "DrillFront"),
            specSpecial("gun_double_barrel", "sacred_dragon", "double_barrel", SpecialRender.DOUBLE_BARREL,
                    inv(1.375D, 0.0D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D),
                    "Stock", "BarrelShort", "Barrel", "Buckle", "Lever", "Shells"),
            specSpecial("gun_double_barrel_sacred_dragon", "sacred_dragon", "double_barrel_sacred_dragon",
                    SpecialRender.DOUBLE_BARREL,
                    inv(1.375D, 0.0D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D),
                    "Stock", "BarrelShort", "Barrel", "Buckle", "Lever", "Shells"));

    private static final Map<RenderSpec, LegacyWavefrontModel> MODELS = new ConcurrentHashMap<>();
    private static final Map<String, LegacyWavefrontModel> EXTRA_MODELS = new ConcurrentHashMap<>();

    public static final SednaGunItemRenderer INSTANCE = new SednaGunItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private SednaGunItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return;
        }
        RenderSpec spec = SPECS.get(gunItem.gunConfig().legacyName());
        if (spec == null) {
            return;
        }

        LegacyWavefrontModel model = MODELS.computeIfAbsent(spec,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        String[] visibleParts = spec.visibleParts().toArray(String[]::new);
        AABB modelBounds = visibleParts.length == 0 ? model.boundsAll() : model.boundsOnly(visibleParts);
        AABB bounds = displayBounds(displayContext, modelBounds, spec);
        if (bounds.getXsize() <= 0.0D || bounds.getYsize() <= 0.0D || bounds.getZsize() <= 0.0D) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(stack, displayContext, poseStack, bounds, spec);
        if (spec.specialRender() == SpecialRender.TESLA_CANNON) {
            renderTeslaCannon(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FATMAN) {
            renderFatman(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FOLLY) {
            renderFolly(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.TAU) {
            renderTau(model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.GREASEGUN) {
            renderGreasegun(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MARESLEG) {
            renderMaresleg(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CARBINE) {
            renderCarbine(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.AM180) {
            renderAm180(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.UZI) {
            renderUzi(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STAR_F) {
            renderStarF(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.G3) {
            renderG3(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.AMAT) {
            renderAmat(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MK108) {
            renderMk108(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.SEXY) {
            renderSexy(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MAS36) {
            renderMas36(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.BOLTER) {
            renderBolter(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STG77) {
            renderStg77(model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LASER_PISTOL) {
            renderLaserPistol(displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.PANZERSCHRECK) {
            renderPanzerschreck(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STINGER) {
            renderStinger(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.QUADRO) {
            renderQuadro(displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MISSILE_LAUNCHER) {
            renderMissileLauncher(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LASRIFLE) {
            renderLasrifle(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.ABERRATOR) {
            renderAberrator(displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LAG) {
            renderLag(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.M2) {
            renderM2(model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.COILGUN) {
            renderCoilgun(model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FIREEXT) {
            renderFireExt(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.SPAS12) {
            renderSpas12(displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CHARGE_THROWER) {
            renderChargeThrower(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.DOUBLE_BARREL) {
            renderDoubleBarrel(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.HEAVY_REVOLVER) {
            renderHeavyRevolver(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FLAMER) {
            renderFlamer(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CHEMTHROWER) {
            renderChemthrower(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.NI4NI) {
            renderNi4Ni(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.DRILL) {
            renderDrill(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender().akimbo()) {
            renderAkimbo(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (visibleParts.length == 0) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        } else {
            model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay, visibleParts);
        }
        poseStack.popPose();
    }

    private static AABB displayBounds(ItemDisplayContext displayContext, AABB bounds, RenderSpec spec) {
        return bounds;
    }

    private static void renderTeslaCannon(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        boolean firstPerson = displayContext.firstPerson();
        double cogAngle = firstPerson ? teslaCogAngle(stack) : 0.0D;

        if (firstPerson) {
            applyTeslaFirstPersonBodyAnimation(stack, poseStack);
        }

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Extension", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (firstPerson) {
            rotateTeslaCog(poseStack, cogAngle);
        }
        ObjWeaponModels.renderPart(model, "Cog", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        if (firstPerson) {
            rotateTeslaCog(poseStack, cogAngle);
        }
        int capacitors = firstPerson ? teslaFirstPersonVisibleCapacitors(stack) : 10;
        for (int i = 0; i < capacitors; i++) {
            ObjWeaponModels.renderPart(model, "Capacitor", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            if (i < 4) {
                poseStack.translate(0.0D, -1.625D, 0.0D);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-22.5F));
                poseStack.translate(0.0D, 1.625D, 0.0D);
            } else {
                if (firstPerson && i == 4) {
                    rotateTeslaCog(poseStack, -cogAngle);
                    poseStack.translate(-cogAngle * 0.5D / 22.5D, 0.0D, 0.0D);
                }
                poseStack.translate(0.5D, 0.0D, 0.0D);
            }
        }
        poseStack.popPose();

        if (firstPerson) {
            renderTeslaYomi(stack, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void applyTeslaFirstPersonBodyAnimation(ItemStack stack, PoseStack poseStack) {
        double equipX = legacyBusActive() ? LegacyHbmAnimations.getRelevantTransformation("EQUIP")[0]
                : teslaFallbackEquipX(stack);
        double recoilZ = legacyBusActive() ? LegacyHbmAnimations.getRelevantTransformation("RECOIL")[2]
                : teslaFallbackRecoilZ(stack);
        poseStack.translate(0.0D, -2.0D, -2.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) equipX));
        poseStack.translate(0.0D, 2.0D, 2.0D);
        poseStack.translate(0.0D, 0.0D, recoilZ);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (recoilZ * 2.0D)));
    }

    private static void rotateTeslaCog(PoseStack poseStack, double angle) {
        poseStack.translate(0.0D, -1.625D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) angle));
        poseStack.translate(0.0D, 1.625D, 0.0D);
    }

    private static double teslaCogAngle(ItemStack stack) {
        return legacyBusActive() ? LegacyHbmAnimations.getRelevantTransformation("CYCLE")[2]
                : teslaFallbackCogAngle(stack);
    }

    private static int teslaFirstPersonVisibleCapacitors(ItemStack stack) {
        int animatedCount = legacyBusActive() ? (int) LegacyHbmAnimations.getRelevantTransformation("COUNT")[0] : 0;
        return Math.min(Math.max(animatedCount, teslaFirstPersonCapacitorCount(stack)), 8);
    }

    private static boolean legacyBusActive() {
        return LegacyHbmAnimations.getRelevantAnim() != null;
    }

    private static double teslaFallbackEquipX(ItemStack stack) {
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_EQUIP) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis > 1000.0D) {
            return 0.0D;
        }
        return lerp(60.0D, 0.0D, sinDown(millis / 1000.0D));
    }

    private static double teslaFallbackRecoilZ(ItemStack stack) {
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_CYCLE) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        double recoil = isTeslaAiming(stack) ? -0.5D : -1.0D;
        if (millis <= 100.0D) {
            return lerp(0.0D, recoil, sinDown(millis / 100.0D));
        }
        if (millis <= 350.0D) {
            return lerp(recoil, 0.0D, sinFull((millis - 100.0D) / 250.0D));
        }
        return 0.0D;
    }

    private static double teslaFallbackCogAngle(ItemStack stack) {
        int animation = teslaLegacyAnimation(stack);
        if (animation != LEGACY_ANIM_CYCLE && animation != LEGACY_ANIM_CYCLE_DRY) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis <= 150.0D) {
            return 0.0D;
        }
        if (millis <= 500.0D) {
            return lerp(0.0D, 22.5D, (millis - 150.0D) / 350.0D);
        }
        return 0.0D;
    }

    private static void renderTeslaYomi(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_INSPECT) {
            return;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis > 2000.0D) {
            return;
        }

        double[] position = teslaYomiPosition(millis);
        double squeezeZ = teslaYomiSqueezeZ(millis);
        poseStack.pushPose();
        poseStack.translate(position[0], position[1], position[2]);
        poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
        poseStack.scale(1.0F, 1.0F, (float) squeezeZ);
        ObjTrinketModels.YOMI_LEGACY.renderAll(ObjTrinketModels.YOMI_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static double[] teslaYomiPosition(double millis) {
        if (millis <= 500.0D) {
            double progress = sinDown(millis / 500.0D);
            return new double[] { lerp(8.0D, 4.0D, progress), lerp(-4.0D, -1.0D, progress), 0.0D };
        }
        if (millis <= 1500.0D) {
            return new double[] { 4.0D, -1.0D, 0.0D };
        }
        double progress = sinUp((millis - 1500.0D) / 500.0D);
        return new double[] { lerp(4.0D, 6.0D, progress), lerp(-1.0D, -6.0D, progress), 0.0D };
    }

    private static double teslaYomiSqueezeZ(double millis) {
        if (millis <= 750.0D) {
            return 1.0D;
        }
        if (millis <= 875.0D) {
            return lerp(1.0D, 0.5D, (millis - 750.0D) / 125.0D);
        }
        if (millis <= 1000.0D) {
            return lerp(0.5D, 1.0D, (millis - 875.0D) / 125.0D);
        }
        return 1.0D;
    }

    private static int teslaLegacyAnimation(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? -1 : tag.getInt(LEGACY_LAST_ANIM_KEY);
    }

    private static double teslaLegacyAnimationMillis(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? 0.0D : tag.getInt(LEGACY_ANIM_TIMER_KEY) * 50.0D;
    }

    private static boolean isTeslaAiming(ItemStack stack) {
        return stack.getItem() instanceof SednaGunItem gunItem && gunItem.legacyIsAiming(stack);
    }

    private static double lerp(double start, double end, double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        return start + (end - start) * clamped;
    }

    private static double sinDown(double progress) {
        return Math.sin(Math.max(0.0D, Math.min(1.0D, progress)) * Math.PI / 2.0D);
    }

    private static double sinUp(double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        return -Math.sin((clamped * Math.PI + Math.PI) / 2.0D) + 1.0D;
    }

    private static double sinFull(double progress) {
        return (-Math.cos(Math.max(0.0D, Math.min(1.0D, progress)) * Math.PI) + 1.0D) / 2.0D;
    }

    private static void renderFatman(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Handle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        boolean loaded = isMagazineLoaded(stack);
        poseStack.pushPose();
        if (!loaded) {
            poseStack.translate(0.0D, 0.0D, 3.0D);
        }
        ObjWeaponModels.renderPart(model, "Piston", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        if (loaded) {
            ResourceLocation nukeTexture = "nuke_balefire".equals(loadedMagazineType(stack))
                    ? FATMAN_BALEFIRE_TEXTURE
                    : FATMAN_MININUKE_TEXTURE;
            ObjWeaponModels.renderPart(model, "MiniNuke", nukeTexture, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static boolean isMagazineLoaded(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem) || stack.getTag() == null) {
            return false;
        }
        return gunItem.gunConfig().magazines().stream()
                .anyMatch(magazine -> !magazine.nbtCountKey().isBlank()
                        && stack.getTag().getInt(magazine.nbtCountKey()) > 0);
    }

    private static String loadedMagazineType(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem) || stack.getTag() == null) {
            return "";
        }
        return gunItem.gunConfig().magazines().stream()
                .filter(magazine -> !magazine.nbtCountKey().isBlank()
                        && stack.getTag().getInt(magazine.nbtCountKey()) > 0)
                .map(magazine -> stack.getTag().getString(magazine.nbtTypeKey()))
                .findFirst()
                .orElse("");
    }

    private static double primaryMagazineFill(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem) || stack.getTag() == null) {
            return 0.0D;
        }
        return gunItem.gunConfig().magazines().stream()
                .findFirst()
                .filter(magazine -> !magazine.nbtCountKey().isBlank() && magazine.capacity() > 0)
                .map(magazine -> (double) stack.getTag().getInt(magazine.nbtCountKey())
                        / (double) Math.max(1, magazine.capacity()))
                .map(fill -> Math.max(0.0D, Math.min(1.0D, fill)))
                .orElse(0.0D);
    }

    private static int primaryMagazineAmount(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem) || stack.getTag() == null) {
            return 0;
        }
        return gunItem.gunConfig().magazines().stream()
                .findFirst()
                .filter(magazine -> !magazine.nbtCountKey().isBlank())
                .map(magazine -> stack.getTag().getInt(magazine.nbtCountKey()))
                .orElse(0);
    }

    private static int teslaFirstPersonCapacitorCount(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return 0;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return gunItem.gunConfig().magazines().stream()
                .filter(magazine -> magazine.kind() == SednaMagazineConfig.Kind.BELT)
                .findFirst()
                .map(magazine -> beltAmmoCount(player, magazine))
                .orElse(0);
    }

    private static void renderFolly(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        ObjWeaponModels.renderPart(model, "Cannon", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Shell", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Breech", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Cog", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        renderFollyAimingText(stack, poseStack, buffer);
    }

    private static void renderFollyAimingText(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer) {
        boolean aiming = stack.getItem() instanceof SednaGunItem gunItem && gunItem.legacyIsAiming(stack);
        if (aiming && !follyWasAiming) {
            follyAimStartMillis = System.currentTimeMillis();
        }
        if (!aiming) {
            follyJingle = false;
            follyWasAiming = false;
            return;
        }
        follyWasAiming = true;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - follyAimStartMillis;
        Font font = minecraft.font;
        int color = follyTextColor(player);

        if (elapsed > 5000L) {
            String msg = primaryMagazineAmount(stack) > 0 ? "+" : "No ammo";
            poseStack.pushPose();
            float crosshairSize = 0.01F;
            poseStack.translate((font.width(msg) / 2.0D) * crosshairSize + 2.0D,
                    1.0D + font.lineHeight * crosshairSize / 2.0D, -2.75D);
            poseStack.scale(crosshairSize, -crosshairSize, crosshairSize);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            renderLegacyModelText(font, msg, color, poseStack, buffer);
            poseStack.popPose();
        }

        String splash = follyBootSplash(elapsed);
        if (!splash.isEmpty()) {
            if (!follyJingle) {
                LegacyClientSoundPlayer.playSoundClient(player.getX(), player.getY(), player.getZ(),
                        "hbm:weapon.fire.vstar", SoundSource.PLAYERS, 0.5F, 1.0F);
                follyJingle = true;
            }
            poseStack.pushPose();
            float splashSize = 0.02F;
            poseStack.translate((font.width(splash) / 2.0D) * splashSize + 2.0D,
                    1.0D + font.lineHeight * splashSize / 2.0D, -2.75D);
            poseStack.scale(splashSize, -splashSize, splashSize);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            renderLegacyModelText(font, splash, color, poseStack, buffer);
            poseStack.popPose();
        }

        List<String> tty = follyTtyLines(player, elapsed);
        if (!tty.isEmpty()) {
            poseStack.pushPose();
            float fontSize = 0.005F;
            poseStack.translate(2.5D, 1.375D, -2.75D);
            poseStack.scale(fontSize, -fontSize, fontSize);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            for (String line : tty) {
                renderLegacyModelText(font, line, color, poseStack, buffer);
                poseStack.translate(0.0D, font.lineHeight + 2.0D, 0.0D);
            }
            poseStack.popPose();
        }
    }

    private static int follyTextColor(Player player) {
        float variance = 0.85F + player.getRandom().nextFloat() * 0.15F;
        int red = Math.min(255, Math.max(0, Math.round(variance * 255.0F)));
        int green = Math.min(255, Math.max(0, Math.round(variance * 0.5F * 255.0F)));
        return 0xFF000000 | (red << 16) | (green << 8);
    }

    private static String follyBootSplash(long elapsedMillis) {
        if (elapsedMillis > 5000L || elapsedMillis < 3000L) {
            return "";
        }
        int splashIndex = (int) ((elapsedMillis - 3000L) * 35L / 2000L) - 10;
        char[] letters = "VStarOS".toCharArray();
        StringBuilder splash = new StringBuilder();
        for (int i = 0; i < letters.length; i++) {
            if (i < splashIndex - 1) {
                splash.append(ChatFormatting.LIGHT_PURPLE);
            }
            if (i == splashIndex - 1) {
                splash.append(ChatFormatting.AQUA);
            }
            if (i == splashIndex) {
                splash.append(ChatFormatting.WHITE);
            }
            if (i == splashIndex + 1) {
                splash.append(ChatFormatting.AQUA);
            }
            if (i == splashIndex + 2) {
                splash.append(ChatFormatting.LIGHT_PURPLE);
            }
            if (i > splashIndex + 2) {
                splash.append(ChatFormatting.BLACK);
            }
            splash.append(letters[i]);
        }
        return splash.toString();
    }

    private static List<String> follyTtyLines(Player player, long elapsedMillis) {
        List<String> tty = new ArrayList<>();
        if (elapsedMillis < 3000L) {
            if (elapsedMillis > 250L) {
                tty.add(ChatFormatting.GREEN + "POST successful - Code 0");
            }
            if (elapsedMillis > 500L) {
                tty.add(ChatFormatting.GREEN + "8,388,608 bytes of RAM installed");
                tty.add(ChatFormatting.GREEN + "5,187,427 bytes available");
            }
            if (elapsedMillis > 750L) {
                tty.add(ChatFormatting.GREEN + "Reticulating splines...");
            }
            if (elapsedMillis > 1500L) {
                tty.add(ChatFormatting.GREEN + "No keyboard found!");
            }
            if (elapsedMillis > 2000L) {
                tty.add(ChatFormatting.GREEN + "Booting from /dev/sda1...");
            }
        }
        if (elapsedMillis > 5000L) {
            tty.add(follyTargetLine(player));
            tty.add(ChatFormatting.GREEN + "Angle: " + ((int) (-player.getXRot() * 100.0F) / 100.0D));
        }
        return tty;
    }

    private static String follyTargetLine(Player player) {
        HitResult hit = RayTraceUtil.getMouseOver(player, 250.0D);
        String target = ChatFormatting.GREEN + "Target: ";
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            return target + pos.getX() + "/" + pos.getY() + "/" + pos.getZ();
        }
        if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit) {
            return target + entityHit.getEntity().getName().getString();
        }
        return target + "N/A";
    }

    private static int beltAmmoCount(Player player, SednaMagazineConfig magazine) {
        if (player.getAbilities().instabuild) {
            return 8;
        }
        int count = 0;
        for (String configName : magazine.acceptedBulletConfigNames()) {
            BulletConfig config = LegacySednaRuntimeBulletConfigs.byName(configName).orElse(null);
            if (config == null) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(config.ammo().itemId());
            if (item == null) {
                continue;
            }
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(item)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    private static void renderFireExt(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        model.renderAll(fireExtTexture(stack, spec), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderSpas12(ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        } else if (displayContext != ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        ObjWeaponModels.renderPart(model, "MainBody", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "PumpGrip", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderTau(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Body", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Rotor", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderGreasegun(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = hasUpgrade(stack, SednaWeaponModEvaluator.ID_GREASEGUN_CLEAN)
                ? GREASEGUN_CLEAN_TEXTURE
                : spec.textureLocation();
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderMaresleg(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean shortened = isMareslegShortened(stack, spec);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!shortened) {
            ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderCarbine(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", CARBINE_SCOPE_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        } else {
            ObjWeaponModels.renderPart(model, "IronSight", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_CARBINE_BAYONET)) {
            ObjWeaponModels.renderPart(model, "Bayonet", CARBINE_BAYONET_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderAm180(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean silenced = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (silenced) {
            ObjWeaponModels.renderPart(model, "Silencer", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Trigger", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.0D, 1.5D);
            poseStack.mulPose(Axis.YN.rotationDegrees((float) (primaryMagazineAmount(stack) / 59.0D * 360.0D)));
            poseStack.translate(0.0D, 0.0D, -1.5D);
        }
        ObjWeaponModels.renderPart(model, "Mag", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        ObjWeaponModels.renderPart(model, "MagPlate", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderUzi(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean silenced = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        if (silenced && displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.translate(0.0D, 0.0D, -4.0D);
        }
        ResourceLocation texture = hasUpgrade(stack, SednaWeaponModEvaluator.ID_UZI_SATURN)
                ? UZI_SATURNITE_TEXTURE
                : spec.textureLocation();
        ObjWeaponModels.renderPart(model, "Gun", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "StockBack", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "StockFront", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay);
        if (silenced) {
            ObjWeaponModels.renderPart(model, "Silencer", texture, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderStarF(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean silenced = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        if (silenced && displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.translate(0.0D, 0.0D, -6.0D);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Mag", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (silenced) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.375D, -0.25D);
            ObjWeaponModels.renderPart(extraModel("uzi", "uzi", "uzi"), "Silencer", UZI_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderG3(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean stock = !hasUpgrade(stack, SednaWeaponModEvaluator.ID_NO_STOCK);
        boolean silenced = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        boolean scoped = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        ResourceLocation texture = g3Texture(stack, spec);

        ObjWeaponModels.renderPart(model, "Rifle", texture, poseStack, buffer, packedLight, packedOverlay);
        if (stock) {
            ObjWeaponModels.renderPart(model, "Stock", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay);
        if (!silenced) {
            ObjWeaponModels.renderPart(model, "Flash_Hider", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Guide_And_Bolt", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Handle", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Trigger", texture, poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, -0.875D, -3.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.translate(0.0D, 0.875D, 3.5D);
        ObjWeaponModels.renderPart(model, "Selector", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        if (silenced) {
            ObjWeaponModels.renderPart(model, "Silencer", G3_ATTACHMENTS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", G3_ATTACHMENTS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderAmat(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "BipodLeft", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "BipodHingeLeft", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "BipodRight", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "BipodHingeRight", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        if (isAmatSilenced(stack)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.625D, -4.3125D);
            poseStack.scale(1.25F, 1.25F, 1.25F);
            ObjWeaponModels.renderPart(extraModel("g3", "g3", "g3"), "Silencer", G3_ATTACHMENTS_TEXTURE, poseStack,
                    buffer, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            ObjWeaponModels.renderPart(model, "MuzzleBrake", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderMk108(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Drum", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        double[][] shells = mk108LoadedShellPositions();
        int shellAmount = displayContext.firstPerson() ? primaryMagazineAmount(stack) : Integer.MAX_VALUE;
        for (int i = 0; i < shells.length - 1; i++) {
            boolean shell = !displayContext.firstPerson() || shells.length - i < shellAmount + 2;
            renderMk108Shell(model, spec.textureLocation(), shells[i][0], shells[i][1], shells[i][2], shell,
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static double[][] mk108LoadedShellPositions() {
        double p = 0.0625D;
        double x = p * 22.0D;
        double y = p * -46.0D;
        double angle = 0.0D;
        double vx = 0.0D;
        double vy = 0.53125D;
        double[] anglesLoaded = { 0.0D, 0.0D, -5.0D, 0.0D, -5.0D, 60.0D, 45.0D, -10.0D, 0.0D };
        double[][] shells = new double[anglesLoaded.length][3];

        for (int i = 0; i < anglesLoaded.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90.0D;
            double delta = anglesLoaded[i];
            angle += delta;
            double radians = Math.toRadians(-delta);
            double nextVx = vx * Math.cos(radians) - vy * Math.sin(radians);
            double nextVy = vx * Math.sin(radians) + vy * Math.cos(radians);
            vx = nextVx;
            vy = nextVy;
            x += vx;
            y += vy;
        }
        return shells;
    }

    private static void renderMk108Shell(LegacyWavefrontModel model, ResourceLocation texture, double x, double y,
            double rot, boolean shell, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
        ObjWeaponModels.renderPart(model, "Belt", texture, poseStack, buffer, packedLight, packedOverlay);
        if (shell) {
            ObjWeaponModels.renderPart(model, "Grenade", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderSexy(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "RecoilSpring", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hood", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "LockSpring", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        double p = 0.0625D;
        double[][] shells = {
                { p * 0.0D, p * -6.0D, 90.0D },
                { p * 5.0D, p * 1.0D, 30.0D },
                { p * 12.0D, p * -1.0D, -30.0D },
                { p * 17.0D, p * -6.0D, -60.0D },
                { p * 17.0D, p * -13.0D, -90.0D },
                { p * 17.0D, p * -20.0D, -90.0D }
        };
        int shellAmount = displayContext.firstPerson() ? primaryMagazineAmount(stack) : Integer.MAX_VALUE;
        for (int i = 0; i < shells.length; i++) {
            boolean shell = !displayContext.firstPerson() || shells.length + 1 - i < shellAmount + 2;
            renderSexyShell(model, spec.textureLocation(), shells[i][0], shells[i][1], shells[i][2], shell,
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSexyShell(LegacyWavefrontModel model, ResourceLocation texture, double x, double y,
            double rot, boolean shell, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, 0.375D + y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
        poseStack.translate(0.0D, -0.375D, 0.0D);
        ObjWeaponModels.renderPart(model, "Belt", texture, poseStack, buffer, packedLight, packedOverlay);
        if (shell) {
            ObjWeaponModels.renderPart(model, "Shell", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderMas36(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        boolean bayonet = hasUpgrade(stack, SednaWeaponModEvaluator.ID_MAS_BAYONET);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (bayonet) {
            if (!displayContext.firstPerson()
                    && displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.translate(0.0D, -1.0D, -6.0D);
            }
            ObjWeaponModels.renderPart(model, "Bayonet", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderBolter(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Body", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Mag", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            renderBolterAmmoText(stack, poseStack, buffer);
        } else {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderBolterAmmoText(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer) {
        String text = Integer.toString(primaryMagazineAmount(stack));
        Font font = Minecraft.getInstance().font;
        float scale = 0.04F;
        poseStack.pushPose();
        poseStack.translate(0.025D - (font.width(text) / 2.0D) * scale, 2.11D, 2.91D);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        renderLegacyModelText(font, text, 0xFFFF0000, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderStg77(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Safety", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Handle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Breech", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderLaserPistol(ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Latch", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (spec.visibleParts().contains("Capacitors")) {
            ObjWeaponModels.renderPart(model, "Capacitors", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (spec.visibleParts().contains("Tape")) {
            ObjWeaponModels.renderPart(model, "Tape", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Battery", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderPanzerschreck(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Tube", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_NO_SHIELD)) {
            ObjWeaponModels.renderPart(model, "Shield", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Rocket", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderStinger(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            if (stack.getItem() instanceof StingerGunItem stinger && stinger.shouldRenderLegacyStingerCrosshair(stack)) {
                return;
            }
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0.0D, 3.5D, -3.0D);
            ObjWeaponModels.renderPart(extraModel("panzerschreck", "panzerschreck", "panzerschreck"),
                    "Rocket", PANZERSCHRECK_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
            renderStingerNotAccurateText(poseStack, buffer);
            poseStack.popPose();
        } else {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderStingerNotAccurateText(PoseStack poseStack, MultiBufferSource buffer) {
        String text = "Not accurate";
        Font font = Minecraft.getInstance().font;
        float scale = 0.04F;
        poseStack.pushPose();
        poseStack.translate(0.025D, -0.5D, (font.width(text) / 2.0D) * scale - 3.0D);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XN.rotationDegrees(45.0F));
        renderLegacyModelText(font, text, 0xFFFF0000, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderQuadro(ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Rockets", QUADRO_ROCKET_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderMissileLauncher(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Front", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson() || isMagazineLoaded(stack)) {
            ObjWeaponModels.renderPart(model, "Missile", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderLasrifle(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean shotgun = SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_SHOTGUN);
        boolean capacitor = SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_CAPACITOR);
        boolean scope = !SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_AUTO);

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (scope) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Battery", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!shotgun) {
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        LegacyWavefrontModel mods = extraModel("lasrifle_mods", "lasrifle_mods", "lasrifle_mods");
        if (shotgun) {
            ObjWeaponModels.renderPart(mods, "BarrelShotgun", LASRIFLE_MODS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (capacitor) {
            ObjWeaponModels.renderPart(mods, "UnderBarrel", LASRIFLE_MODS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderAberrator(ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Sight", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderLag(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson()) {
            if (primaryMagazineAmount(stack) > 0) {
                ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderM2(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderCoilgun(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderLegacyModelText(Font font, String text, int color, PoseStack poseStack,
            MultiBufferSource buffer) {
        font.drawInBatch(text, 0.0F, 0.0F, color, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    private static void renderChargeThrower(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE)) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        String loadedType = loadedMagazineType(stack);
        if ("ct_hook".equals(loadedType)) {
            ObjWeaponModels.renderPart(model, "Hook", CHARGE_THROWER_HOOK_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        } else if ("ct_mortar".equals(loadedType)) {
            ObjWeaponModels.renderPart(model, "Mortar", CHARGE_THROWER_MORTAR_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        } else if ("ct_mortar_charge".equals(loadedType)) {
            ObjWeaponModels.renderPart(model, "Mortar", CHARGE_THROWER_MORTAR_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Oomph", CHARGE_THROWER_MORTAR_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderDoubleBarrel(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean sawedOff = isDoubleBarrelSawedOff(stack);
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "BarrelShort", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!sawedOff) {
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Buckle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Shells", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderHeavyRevolver(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean scoped = isHeavyRevolverScoped(stack);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Cylinder", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Bullets", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Casings", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Pivot", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", LILMAC_SCOPE_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderFlamer(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Tank", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double fill = primaryMagazineFill(stack);
            poseStack.translate(1.25D, 1.25D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-135.0D + fill * 270.0D)));
            poseStack.translate(-1.25D, -1.25D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        if (spec.visibleParts().contains("HeatShield")) {
            ObjWeaponModels.renderPart(model, "HeatShield", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderChemthrower(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hose", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Nozzle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double fill = 0.0D;
            if (stack.getItem() instanceof SednaGunItem gunItem && stack.getTag() != null) {
                fill = gunItem.gunConfig().magazines().stream()
                        .filter(magazine -> "gun_chemthrower".equals(magazine.legacyOwnerName()))
                        .findFirst()
                        .map(magazine -> (double) stack.getTag().getInt(magazine.nbtCountKey())
                                / (double) Math.max(1, magazine.capacity()))
                        .orElse(0.0D);
            }
            poseStack.translate(0.0D, 0.875D, 1.75D);
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (135.0D - fill * 270.0D)));
            poseStack.translate(0.0D, -0.875D, -1.75D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
    }

    private static void renderDrill(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Base", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double fill = 0.0D;
            if (stack.getItem() instanceof SednaGunItem gunItem && stack.getTag() != null) {
                fill = gunItem.gunConfig().magazines().stream()
                        .filter(magazine -> "gun_drill".equals(magazine.legacyOwnerName()))
                        .findFirst()
                        .map(magazine -> (double) stack.getTag().getInt(magazine.nbtCountKey())
                                / (double) Math.max(1, magazine.capacity()))
                        .orElse(0.0D);
            }
            poseStack.translate(1.0D, 2.0625D, -1.75D);
            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-135.0D + fill * 270.0D)));
            poseStack.mulPose(Axis.XP.rotationDegrees(-45.0F));
            poseStack.translate(-1.0D, -2.0625D, 1.75D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        ObjWeaponModels.renderPart(model, "Piston1", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Piston2", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Piston3", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "DrillBack", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "DrillFront", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderNi4Ni(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = Ni4NiGunItem.getColors(stack) == null ? spec.textureLocation()
                : ObjWeaponModels.N_I_4_N_I_GREYSCALE_TEXTURE;
        int[] colors = Ni4NiGunItem.getColors(stack);
        int dark = colors == null ? 0xFFFFFF : colors[0];
        int light = colors == null ? 0xFFFFFF : colors[1];
        int grip = colors == null ? 0xFFFFFF : colors[2];

        renderTintedPart(model, "FrameDark", texture, poseStack, buffer, packedLight, packedOverlay, dark);
        renderTintedPart(model, "Grip", texture, poseStack, buffer, packedLight, packedOverlay, grip);
        renderTintedPart(model, "FrameLight", texture, poseStack, buffer, packedLight, packedOverlay, light);
        renderTintedPart(model, "Cylinder", texture, poseStack, buffer, packedLight, packedOverlay, light);
        ObjWeaponModels.renderPart(model, "CylinderHighlights", texture, poseStack, buffer, 0xF000F0, packedOverlay);
        ObjWeaponModels.renderPart(model, "Barrel", texture, poseStack, buffer, 0xF000F0, packedOverlay);

        int coinCount = displayContext == ItemDisplayContext.GUI ? 4 : Ni4NiGunItem.getCoinCount(stack);
        renderNi4NiCoin(model, "Coin1", coinCount, 4, 8, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin2", coinCount, 3, 7, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin3", coinCount, 2, 6, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin4", coinCount, 1, 5, poseStack, buffer, 0xF000F0, packedOverlay);
    }

    private static void renderTintedPart(LegacyWavefrontModel model, String part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int color) {
        ObjWeaponModels.renderPart(model, part, texture, poseStack, buffer, packedLight, packedOverlay,
                (color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
    }

    private static void renderNi4NiCoin(LegacyWavefrontModel model, String part, int coinCount, int threshold,
            int redThreshold, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (coinCount < threshold) {
            return;
        }
        int red = coinCount >= redThreshold ? 255 : 0;
        ObjWeaponModels.renderPart(model, part, specCoinTexture(), poseStack, buffer, packedLight, packedOverlay,
                red, 255, 0, 255);
    }

    private static ResourceLocation specCoinTexture() {
        return ObjWeaponModels.N_I_4_N_I_TEXTURE;
    }

    private static boolean hasUpgrade(ItemStack stack, int id) {
        return SednaWeaponModEvaluator.hasUpgrade(stack, 0, id);
    }

    private static boolean hasUpgrade(ItemStack stack, int configIndex, int id) {
        return SednaWeaponModEvaluator.hasUpgrade(stack, configIndex, id);
    }

    private static String currentLegacyName(ItemStack stack) {
        return stack.getItem() instanceof SednaGunItem gunItem ? gunItem.gunConfig().legacyName() : "";
    }

    private static boolean isMareslegShortened(ItemStack stack, RenderSpec spec) {
        return spec.textureLocation().getPath().endsWith("maresleg_broken.png")
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SAWED_OFF);
    }

    private static boolean isDoubleBarrelSawedOff(ItemStack stack) {
        return "gun_double_barrel_sacred_dragon".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SAWED_OFF);
    }

    private static boolean isHeavyRevolverScoped(ItemStack stack) {
        return "gun_heavy_revolver_lilmac".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
    }

    private static boolean isAmatSilenced(ItemStack stack) {
        return "gun_amat_penance".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
    }

    private static ResourceLocation g3Texture(ItemStack stack, RenderSpec spec) {
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_FURNITURE_GREEN)) {
            return G3_GREEN_TEXTURE;
        }
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_FURNITURE_BLACK)) {
            return G3_BLACK_TEXTURE;
        }
        return spec.textureLocation();
    }

    private static LegacyWavefrontModel extraModel(String key, String modelName, String textureName) {
        return EXTRA_MODELS.computeIfAbsent(key,
                ignored -> new LegacyWavefrontModel(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/" + modelName + ".obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/" + textureName + ".png"))
                        .asVBO());
    }

    private static ResourceLocation fireExtTexture(ItemStack stack, RenderSpec spec) {
        return switch (loadedMagazineType(stack)) {
            case "fext_foam" -> FIREEXT_FOAM_TEXTURE;
            case "fext_sand" -> FIREEXT_SAND_TEXTURE;
            default -> spec.textureLocation();
        };
    }

    private static void renderAkimbo(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext == ItemDisplayContext.GUI) {
            renderAkimboInventory(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (displayContext.firstPerson()) {
            renderAkimboFirstPerson(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (displayContext == ItemDisplayContext.GROUND) {
            renderAkimboEntity(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            renderAkimboThirdPerson(stack, model, spec, displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderAkimboThirdPerson(stack, model, spec, false, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderAkimboEntity(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> renderDualEntityPartSet(model, spec.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, -1.0D, 1.0D, "Gun", "Lever");
            case EOTT -> renderDualEntityPartSet(model, spec.textureLocation(), poseStack, buffer,
                    packedLight, packedOverlay, -1.0D, 1.0D, "Gun", "Hammer", "Magazine", "Slide", "Sight");
            case DANI -> {
                poseStack.pushPose();
                poseStack.translate(-2.0D, 1.0D, 0.0D);
                model.renderAll(DANI_LUNAR_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(2.0D, 1.0D, 0.0D);
                model.renderAll(DANI_CELESTIAL_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
            case UZI_AKIMBO -> {
                boolean anySilenced = hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_SILENCER)
                        || hasUpgrade(stack, 1, SednaWeaponModEvaluator.ID_SILENCER);
                if (anySilenced) {
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                }
                poseStack.pushPose();
                poseStack.translate(-1.0D, 1.0D, 0.0D);
                renderUziAkimboParts(stack, model, 1, false, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(1.0D, 1.0D, 0.0D);
                renderUziAkimboParts(stack, model, 0, true, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
            case STAR_F_AKIMBO -> {
                boolean anySilenced = hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_SILENCER)
                        || hasUpgrade(stack, 1, SednaWeaponModEvaluator.ID_SILENCER);
                if (anySilenced) {
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                }
                poseStack.pushPose();
                poseStack.translate(-1.0D, 1.0D, 0.0D);
                renderStarFAkimboParts(stack, model, 1, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(1.0D, 1.0D, 0.0D);
                renderStarFAkimboParts(stack, model, 0, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
            case MINIGUN_DUAL -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, "Gun", "Barrels");
            default -> renderAkimboThirdPerson(stack, model, spec, false, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderDualEntityPartSet(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            double leftX, double rightX, String... parts) {
        poseStack.pushPose();
        poseStack.translate(leftX, 1.0D, 0.0D);
        ObjWeaponModels.renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, parts);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(rightX, 1.0D, 0.0D);
        ObjWeaponModels.renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, parts);
        poseStack.popPose();
    }

    private static void renderAkimboThirdPerson(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, "Gun", "Lever");
            case MINIGUN_DUAL -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, leftHand ? "GunDual" : "Gun", "Barrels");
            case EOTT -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    "Gun", "Hammer", "Magazine", "Slide", "Sight");
            case DANI -> model.renderAll(leftHand ? DANI_CELESTIAL_TEXTURE : DANI_LUNAR_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            case UZI_AKIMBO -> renderUziAkimboParts(stack, model, leftHand ? 0 : 1, leftHand,
                    poseStack, buffer, packedLight, packedOverlay);
            case STAR_F_AKIMBO -> renderStarFAkimboParts(stack, model, leftHand ? 0 : 1,
                    poseStack, buffer, packedLight, packedOverlay);
            default -> {
                if (spec.visibleParts().isEmpty()) {
                    model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
                } else {
                    model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                            spec.visibleParts().toArray(String[]::new));
                }
            }
        }
    }

    private static void renderAkimboInventory(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> {
                renderDualInventoryPartSet(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        -1.0D, 0.0D, 5.0D, 1.0D, "Gun", "Lever");
            }
            case MINIGUN_DUAL -> {
                poseStack.pushPose();
                applyLegacyMinigunDualLeftInventoryRotations(poseStack);
                ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        "GunDual", "Barrels");
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 8.0D);
                applyLegacyAkimboRightInventoryRotations(poseStack, false);
                ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        "Gun", "Barrels");
                poseStack.popPose();
            }
            case EOTT -> {
                poseStack.translate(0.0D, 1.0D, 0.0D);
                renderDualInventoryPartSet(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        -1.0D, 0.0D, 5.0D, 1.0D, "Gun", "Hammer", "Magazine", "Slide", "Sight");
            }
            case DANI -> {
                renderDaniInventory(model, poseStack, buffer, packedLight, packedOverlay);
            }
            case UZI_AKIMBO -> {
                boolean anySilenced = hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_SILENCER)
                        || hasUpgrade(stack, 1, SednaWeaponModEvaluator.ID_SILENCER);
                poseStack.pushPose();
                applyLegacyAkimboLeftInventoryRotations(poseStack);
                poseStack.translate(0.0D, 1.0D, 0.0D);
                if (anySilenced) {
                    poseStack.scale(0.625F, 0.625F, 0.625F);
                    poseStack.translate(0.0D, 0.0D, -4.0D);
                }
                renderUziAkimboParts(stack, model, 1, false, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 5.0D);
                applyLegacyAkimboRightInventoryRotations(poseStack, true);
                poseStack.translate(0.0D, 1.0D, 0.0D);
                if (anySilenced) {
                    poseStack.scale(0.625F, 0.625F, 0.625F);
                    poseStack.translate(0.0D, 0.0D, -4.0D);
                }
                renderUziAkimboParts(stack, model, 0, true, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
            case STAR_F_AKIMBO -> {
                boolean anySilenced = hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_SILENCER)
                        || hasUpgrade(stack, 1, SednaWeaponModEvaluator.ID_SILENCER);
                poseStack.pushPose();
                applyLegacyAkimboLeftInventoryRotations(poseStack);
                poseStack.translate(0.5D, 0.0D, 0.0D);
                if (anySilenced) {
                    poseStack.scale(0.625F, 0.625F, 0.625F);
                    poseStack.translate(0.0D, 0.0D, -4.0D);
                }
                renderStarFAkimboParts(stack, model, 1, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 5.0D);
                applyLegacyAkimboRightInventoryRotations(poseStack, true);
                poseStack.translate(-0.5D, 0.0D, 0.0D);
                if (anySilenced) {
                    poseStack.scale(0.625F, 0.625F, 0.625F);
                    poseStack.translate(0.0D, 0.0D, -4.0D);
                }
                renderStarFAkimboParts(stack, model, 0, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
            default -> {
                if (spec.visibleParts().isEmpty()) {
                    model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
                } else {
                    model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                            spec.visibleParts().toArray(String[]::new));
                }
            }
        }
    }

    private static void renderDaniInventory(LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyLegacyAkimboLeftInventoryRotations(poseStack);
        poseStack.translate(2.0D, 0.0D, 0.0D);
        model.renderAll(DANI_CELESTIAL_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 5.0D);
        applyLegacyAkimboRightInventoryRotations(poseStack, true);
        poseStack.translate(-2.0D, 0.0D, 0.0D);
        model.renderAll(DANI_LUNAR_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderDualInventoryPartSet(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            double leftX, double leftY, double secondZ, double rightX, String... parts) {
        poseStack.pushPose();
        applyLegacyAkimboLeftInventoryRotations(poseStack);
        poseStack.translate(leftX, leftY, 0.0D);
        ObjWeaponModels.renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, parts);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, secondZ);
        applyLegacyAkimboRightInventoryRotations(poseStack, true);
        poseStack.translate(rightX, leftY, 0.0D);
        ObjWeaponModels.renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, parts);
        poseStack.popPose();
    }

    private static void renderUziAkimboParts(ItemStack stack, LegacyWavefrontModel model, int configIndex,
            boolean mirror, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_UZI_SATURN)
                ? UZI_SATURNITE_TEXTURE
                : UZI_TEXTURE;
        ObjWeaponModels.renderPart(model, mirror ? "GunMirror" : "Gun", texture, poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "StockBack", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "StockFront", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay);
        if (hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
            ObjWeaponModels.renderPart(model, "Silencer", texture, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderStarFAkimboParts(ItemStack stack, LegacyWavefrontModel model, int configIndex,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjWeaponModels.renderPart(model, "Gun", STAR_F_ELITE_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Slide", STAR_F_ELITE_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Mag", STAR_F_ELITE_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hammer", STAR_F_ELITE_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
        if (hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.375D, -0.25D);
            ObjWeaponModels.renderPart(extraModel("uzi", "uzi", "uzi"), "Silencer", UZI_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void applyLegacyAkimboLeftInventoryRotations(PoseStack poseStack) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
    }

    private static void applyLegacyMinigunDualLeftInventoryRotations(PoseStack poseStack) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
    }

    private static void applyLegacyAkimboRightInventoryRotations(PoseStack poseStack, boolean includePitch) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        if (includePitch) {
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
    }

    private static void renderAkimboFirstPerson(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (int side = -1; side <= 1; side += 2) {
            poseStack.pushPose();
            applyLegacyAkimboFirstPersonPose(spec, side, poseStack);
            renderAkimboFirstPersonParts(stack, model, spec, side, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void applyLegacyAkimboFirstPersonPose(RenderSpec spec, int side, PoseStack poseStack) {
        double offset = 0.8D;
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> poseStack.translate(-1.5D * offset * side, -1.0D * offset, 2.0D * offset);
            case MINIGUN_DUAL -> poseStack.translate(-2.75D * offset * side, -1.75D * offset, 2.5D * offset);
            case EOTT -> poseStack.translate(-1.0D * offset * side, -1.25D * offset, 1.25D * offset);
            case DANI -> poseStack.translate(-1.5D * offset * side, -0.75D * offset, 1.0D * offset);
            case UZI_AKIMBO -> poseStack.translate(-2.25D * offset * side, -1.5D * offset, 2.5D * offset);
            case STAR_F_AKIMBO -> poseStack.translate(-2.0D * offset * side, -1.75D * offset, 2.5D * offset);
            default -> {
            }
        }
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
    }

    private static void renderAkimboFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            int side, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean mirror = side == -1;
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, "Gun", "Lever");
            case MINIGUN_DUAL -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay, mirror ? "GunDual" : "Gun", "Barrels");
            case EOTT -> ObjWeaponModels.renderOnly(model, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    "Gun", "Hammer", "Magazine", "Bullet", "Slide", "Sight");
            case DANI -> model.renderAll(mirror ? DANI_CELESTIAL_TEXTURE : DANI_LUNAR_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            case UZI_AKIMBO -> renderUziAkimboParts(stack, model, mirror ? 0 : 1, mirror,
                    poseStack, buffer, packedLight, packedOverlay);
            case STAR_F_AKIMBO -> renderStarFAkimboParts(stack, model, mirror ? 0 : 1,
                    poseStack, buffer, packedLight, packedOverlay);
            default -> model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    spec.visibleParts().toArray(String[]::new));
        }
    }

    private static void applyDisplay(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, AABB bounds,
            RenderSpec spec) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        if (displayContext == ItemDisplayContext.GUI) {
            if (spec.specialRender().akimbo()) {
                applyLegacyInventorySetupOnly(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.MARESLEG) {
                applyLegacyMareslegInventoryDisplay(stack, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CARBINE) {
                applyLegacyCarbineInventoryDisplay(stack, poseStack);
                return;
            }
            if (spec.specialRender() == SpecialRender.G3) {
                applyLegacyG3InventoryDisplay(stack, poseStack);
                return;
            }
            if (spec.specialRender() == SpecialRender.AMAT) {
                applyLegacyAmatInventoryDisplay(stack, poseStack);
                return;
            }
            if (spec.specialRender() == SpecialRender.FIREEXT) {
                applyLegacyFireExtInventoryDisplay(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.SPAS12) {
                applyLegacySpas12InventoryDisplay(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHARGE_THROWER) {
                applyLegacyChargeThrowerInventoryDisplay(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.DOUBLE_BARREL) {
                applyLegacyDoubleBarrelInventoryDisplay(stack, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.HEAVY_REVOLVER) {
                applyLegacyHeavyRevolverInventoryDisplay(stack, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHEMTHROWER) {
                applyLegacyChemthrowerInventoryDisplay(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.NI4NI) {
                applyLegacyNi4NiInventoryDisplay(poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.DRILL) {
                applyLegacyDrillInventoryDisplay(poseStack, spec);
                return;
            }
            applyLegacyInventoryDisplay(poseStack, spec);
            return;
        }

        if (displayContext.firstPerson()) {
            if (spec.specialRender().akimbo()) {
                applyLegacyFirstPersonSetupOnly(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.FIREEXT) {
                applyLegacyFireExtFirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.SPAS12) {
                applyLegacySpas12FirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHARGE_THROWER) {
                applyLegacyChargeThrowerFirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHEMTHROWER) {
                applyLegacyChemthrowerFirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.NI4NI) {
                applyLegacyFirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.DRILL) {
                applyLegacyDrillFirstPersonDisplay(displayContext, poseStack, spec);
                return;
            }
            applyLegacyFirstPersonDisplay(displayContext, poseStack, spec);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float fitScale = (float) (0.82D / Math.max(1.0D, maxSize));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (180.0D + spec.modelYawDegrees())));
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.65F, 0.65F, 0.65F);
        }
    }

    private static void applyLegacyInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) inventory.yRot()));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
        if (spec.inventoryRenderYawDegrees() != 0.0D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) spec.inventoryRenderYawDegrees()));
        }
    }

    private static void applyLegacyInventorySetupOnly(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
    }

    private static void applyLegacyMareslegInventoryDisplay(ItemStack stack, PoseStack poseStack, RenderSpec spec) {
        if (isMareslegShortened(stack, spec)) {
            applyLegacyInventoryDisplay(poseStack, new RenderSpec(spec.modelLocation(), spec.textureLocation(),
                    spec.modelYawDegrees(), spec.firstPersonYawDegrees(), spec.inventoryRenderYawDegrees(),
                    spec.specialRender(), inv(2.5D, -1.0D, 0.0D, 0.0D), spec.firstPerson(), spec.visibleParts()));
        } else {
            applyLegacyInventoryDisplay(poseStack, spec);
        }
    }

    private static void applyLegacyCarbineInventoryDisplay(ItemStack stack, PoseStack poseStack) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_CARBINE_BAYONET)) {
            poseStack.scale(1.1875F, 1.1875F, 1.1875F);
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(1.5D, 0.0D, 0.0D);
        } else {
            poseStack.scale(1.375F, 1.375F, 1.375F);
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(-0.5D, 0.0D, 0.0D);
        }
    }

    private static void applyLegacyG3InventoryDisplay(ItemStack stack, PoseStack poseStack) {
        boolean stock = !hasUpgrade(stack, SednaWeaponModEvaluator.ID_NO_STOCK);
        boolean silenced = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(stock ? 0.875F : 1.125F, stock ? 0.875F : 1.125F, stock ? 0.875F : 1.125F);
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(silenced ? (stock ? 50.0F : 55.0F) : 45.0F));
        poseStack.translate(stock ? (silenced ? 0.75D : -0.5D) : 2.5D, 0.5D, 0.0D);
    }

    private static void applyLegacyAmatInventoryDisplay(ItemStack stack, PoseStack poseStack) {
        boolean silenced = isAmatSilenced(stack);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(silenced ? 0.8175F : 0.9375F, silenced ? 0.8175F : 0.9375F,
                silenced ? 0.8175F : 0.9375F);
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(-0.5D, 0.5D, silenced ? -1.0D : 0.0D);
    }

    private static void applyLegacyFireExtInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.translate(2.0D, 14.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-135.0F));
        poseStack.scale((float) spec.inventory().scale(), (float) spec.inventory().scale(),
                (float) -spec.inventory().scale());
    }

    private static void applyLegacySpas12InventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyChargeThrowerInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyDoubleBarrelInventoryDisplay(ItemStack stack, PoseStack poseStack, RenderSpec spec) {
        if (isDoubleBarrelSawedOff(stack)) {
            applyLegacyInventoryDisplay(poseStack, new RenderSpec(spec.modelLocation(), spec.textureLocation(),
                    spec.modelYawDegrees(), spec.firstPersonYawDegrees(), spec.inventoryRenderYawDegrees(),
                    spec.specialRender(), inv(2.0D, -2.0D, 0.5D, 0.0D), spec.firstPerson(), spec.visibleParts()));
        } else {
            applyLegacyInventoryDisplay(poseStack, spec);
        }
    }

    private static void applyLegacyHeavyRevolverInventoryDisplay(ItemStack stack, PoseStack poseStack,
            RenderSpec spec) {
        if (isHeavyRevolverScoped(stack)) {
            applyLegacyInventoryDisplay(poseStack, new RenderSpec(spec.modelLocation(), spec.textureLocation(),
                    spec.modelYawDegrees(), spec.firstPersonYawDegrees(), spec.inventoryRenderYawDegrees(),
                    spec.specialRender(), inv(1.125D, 0.0D, -0.5D, 0.0D), spec.firstPerson(), spec.visibleParts()));
        } else {
            applyLegacyInventoryDisplay(poseStack, spec);
        }
    }

    private static void applyLegacyNi4NiInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyChemthrowerInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyDrillInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyFirstPersonDisplay(ItemDisplayContext displayContext, PoseStack poseStack,
            RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        if (spec.firstPersonYawDegrees() != 0.0D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) spec.firstPersonYawDegrees()));
        }
    }

    private static void applyLegacyFirstPersonSetupOnly(ItemDisplayContext displayContext, PoseStack poseStack,
            RenderSpec spec) {
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, spec.firstPerson().setupZ());
    }

    private static void applyLegacyFireExtFirstPersonDisplay(ItemDisplayContext displayContext, PoseStack poseStack,
            RenderSpec spec) {
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(25.0F));
        poseStack.translate(spec.firstPerson().aimX(), spec.firstPerson().aimY(), spec.firstPerson().aimZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(80.0F));
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
    }

    private static void applyLegacySpas12FirstPersonDisplay(ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
    }

    private static void applyLegacyChargeThrowerFirstPersonDisplay(ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
    }

    private static void applyLegacyChemthrowerFirstPersonDisplay(ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }

    private static void applyLegacyDrillFirstPersonDisplay(ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        poseStack.mulPose(Axis.YP.rotationDegrees(15.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
    }

    private static Map.Entry<String, RenderSpec> spec(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson) {
        return specYaw(legacyName, modelName, textureName, 0.0D, inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specYaw(String legacyName, String modelName, String textureName,
            double modelYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson) {
        return specOnly(legacyName, modelName, textureName, modelYawDegrees, inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specFirstPersonYaw(String legacyName, String modelName,
            String textureName, double firstPersonYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson) {
        return specOnly(legacyName, modelName, textureName, 0.0D, firstPersonYawDegrees, inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specTesla(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson) {
        return specSpecial(legacyName, modelName, textureName, SpecialRender.TESLA_CANNON, inventory, firstPerson,
                "Gun", "Extension", "Cog", "Capacitor");
    }

    private static Map.Entry<String, RenderSpec> specFatman(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson) {
        return specSpecial(legacyName, modelName, textureName, SpecialRender.FATMAN, inventory, firstPerson,
                "Launcher", "Handle", "Gauge", "Lid", "Piston", "MiniNuke");
    }

    private static Map.Entry<String, RenderSpec> specAkimbo(String legacyName, String modelName, String textureName,
            SpecialRender specialRender, InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return specSpecial(legacyName, modelName, textureName, specialRender, inventory, firstPerson, visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specRenderYaw(String legacyName, String modelName,
            String textureName, double renderYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson) {
        return specOnly(legacyName, modelName, textureName, renderYawDegrees, renderYawDegrees, renderYawDegrees,
                inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specSpecial(String legacyName, String modelName, String textureName,
            SpecialRender specialRender, InventoryPose inventory, FirstPersonPose firstPerson,
            String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, 0.0D, 0.0D, 0.0D, specialRender, inventory, firstPerson,
                visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, 0.0D, inventory, firstPerson, visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            double modelYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, modelYawDegrees, modelYawDegrees, inventory, firstPerson,
                visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            double modelYawDegrees, double firstPersonYawDegrees, InventoryPose inventory,
            FirstPersonPose firstPerson, String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, modelYawDegrees, firstPersonYawDegrees, 0.0D, inventory,
                firstPerson, visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            double modelYawDegrees, double firstPersonYawDegrees, double inventoryRenderYawDegrees,
            InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, modelYawDegrees, firstPersonYawDegrees,
                inventoryRenderYawDegrees, SpecialRender.NONE, inventory, firstPerson, visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            double modelYawDegrees, double firstPersonYawDegrees, double inventoryRenderYawDegrees,
            SpecialRender specialRender, InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return Map.entry(legacyName, new RenderSpec(
                new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/" + modelName + ".obj"),
                new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/" + textureName + ".png"),
                modelYawDegrees,
                firstPersonYawDegrees,
                inventoryRenderYawDegrees,
                specialRender,
                inventory,
                firstPerson,
                List.of(visibleParts)));
    }

    private static InventoryPose inv(double scale, double x, double y, double z) {
        return new InventoryPose(scale, 45.0D, x, y, z);
    }

    private static InventoryPose inv(double scale, double x, double y, double z, double yRot) {
        return new InventoryPose(scale, yRot, x, y, z);
    }

    private static FirstPersonPose fp(double renderScale, double setupZ, double aimX, double aimY, double aimZ) {
        return new FirstPersonPose(renderScale, setupZ, aimX, aimY, aimZ);
    }

    private record RenderSpec(ResourceLocation modelLocation, ResourceLocation textureLocation, double modelYawDegrees,
            double firstPersonYawDegrees, double inventoryRenderYawDegrees, SpecialRender specialRender,
            InventoryPose inventory, FirstPersonPose firstPerson, List<String> visibleParts) {
    }

    private enum SpecialRender {
        NONE(false),
        TAU(false),
        TESLA_CANNON(false),
        FATMAN(false),
        GREASEGUN(false),
        MARESLEG(false),
        CARBINE(false),
        AM180(false),
        UZI(false),
        STAR_F(false),
        G3(false),
        AMAT(false),
        MK108(false),
        SEXY(false),
        MAS36(false),
        BOLTER(false),
        STG77(false),
        LASER_PISTOL(false),
        PANZERSCHRECK(false),
        STINGER(false),
        QUADRO(false),
        MISSILE_LAUNCHER(false),
        LASRIFLE(false),
        ABERRATOR(false),
        LAG(false),
        M2(false),
        COILGUN(false),
        FOLLY(false),
        MARESLEG_AKIMBO(true),
        MINIGUN_DUAL(true),
        EOTT(true),
        DANI(true),
        UZI_AKIMBO(true),
        STAR_F_AKIMBO(true),
        FIREEXT(false),
        SPAS12(false),
        CHARGE_THROWER(false),
        DOUBLE_BARREL(false),
        HEAVY_REVOLVER(false),
        FLAMER(false),
        CHEMTHROWER(false),
        NI4NI(false),
        DRILL(false);

        private final boolean akimbo;

        SpecialRender(boolean akimbo) {
            this.akimbo = akimbo;
        }

        boolean akimbo() {
            return akimbo;
        }
    }

    private record InventoryPose(double scale, double yRot, double x, double y, double z) {
    }

    private record FirstPersonPose(double renderScale, double setupZ, double aimX, double aimY, double aimZ) {
    }

}
