package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.client.anim.LegacyBusAnimationTransforms;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.ClientSednaGunEffects;
import com.hbm.ntm.client.LegacySednaAimProgress;
import com.hbm.ntm.client.obj.ObjTrinketModels;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.particle.SpentCasingDefinition;
import com.hbm.ntm.client.sound.LegacyClientSoundPlayer;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.Ni4NiGunItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.util.RayTraceUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SednaGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final double LEGACY_GUI_SLOT_PIXELS = 16.0D;
    private static final double LEGACY_GUI_UNIT = 1.0D / LEGACY_GUI_SLOT_PIXELS;
    private static final double FIRST_PERSON_SCREEN_UNIT = 0.25D;
    private static final int LEGACY_ANIM_RELOAD = 0;
    private static final int LEGACY_ANIM_CYCLE = 3;
    private static final int LEGACY_ANIM_JAMMED = 4;
    private static final int LEGACY_ANIM_CYCLE_DRY = 5;
    // AnimationEnums.GunAnimation preserves this ordinal order: ALT_CYCLE, SPINUP.
    private static final int LEGACY_ANIM_ALT_CYCLE = 6;
    private static final int LEGACY_ANIM_SPINUP = 7;
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
    private static final char[] FOLLY_BOOT_LETTERS = "VStarOS".toCharArray();
    private static final String FOLLY_TTY_POST = ChatFormatting.GREEN + "POST successful - Code 0";
    private static final String FOLLY_TTY_RAM_INSTALLED = ChatFormatting.GREEN + "8,388,608 bytes of RAM installed";
    private static final String FOLLY_TTY_RAM_AVAILABLE = ChatFormatting.GREEN + "5,187,427 bytes available";
    private static final String FOLLY_TTY_SPLINES = ChatFormatting.GREEN + "Reticulating splines...";
    private static final String FOLLY_TTY_NO_KEYBOARD = ChatFormatting.GREEN + "No keyboard found!";
    private static final String FOLLY_TTY_BOOTING = ChatFormatting.GREEN + "Booting from /dev/sda1...";
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
            specSpecial("gun_debug", "lilmac", "debug_gun", SpecialRender.DEBUG,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D),
                    "Gun", "Pivot", "Cylinder", "Bullets", "Casings", "Hammer"),
            specSpecial("gun_pepperbox", "pepperbox", "pepperbox", SpecialRender.PEPPERBOX,
                    inv(1.5D, 0.5D, 0.5D, 0.0D), fp(0.25D, 1.5D, -1.0D, -0.6D, 0.8D),
                    "Grip", "Cylinder", "Hammer", "Trigger"),
            specSpecial("gun_light_revolver", "bio_revolver", "bio_revolver", SpecialRender.ATLAS,
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D),
                    "Grip", "Barrel", "Latch", "Drum", "Hammer"),
            specSpecial("gun_light_revolver_atlas", "bio_revolver", "bio_revolver_atlas", SpecialRender.ATLAS,
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D),
                    "Grip", "Barrel", "Latch", "Drum", "Hammer"),
            specAkimbo("gun_light_revolver_dani", "bio_revolver", "dani_celestial", SpecialRender.DANI,
                    inv(1.125D, 0.0D, -2.0D, 0.0D), fp(0.125D, 0.875D, 0.0D, 0.0D, 0.0D)),
            specSpecial("gun_henry", "henry", "henry", SpecialRender.HENRY,
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.4D)),
            specSpecial("gun_henry_lincoln", "henry", "henry_lincoln", SpecialRender.HENRY,
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
            specSpecial("gun_flaregun", "flaregun", "flaregun", SpecialRender.FLAREGUN,
                    inv(1.0D, -0.5D, 0.0D, 0.0D), fp(0.125D, 0.875D, -1.0D, -1.2D, 1.6D)),
            specSpecial("gun_panzerschreck", "panzerschreck", "panzerschreck", SpecialRender.PANZERSCHRECK,
                    inv(1.5D, -0.5D, 0.5D, 0.0D),
                    fp(1.25D, 0.875D, -2.75D * 0.8D, -2.0D * 0.8D, 2.5D * 0.8D),
                    "Tube", "Shield"),
            specSpecial("gun_carbine", "carbine", "huntsman", SpecialRender.CARBINE,
                    inv(1.375D, -0.5D, 0.0D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.2D, 0.7D)),
            specSpecial("gun_minigun", "minigun", "minigun", SpecialRender.MINIGUN,
                    inv(0.875D, -0.25D, 0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -1.75D * 0.8D, 3.5D * 0.8D),
                    "Gun", "Grip", "Barrels"),
            specSpecial("gun_minigun_lacunae", "minigun", "minigun_lacunae", SpecialRender.MINIGUN,
                    inv(0.875D, -0.25D, 0.5D, 0.0D),
                    fp(0.375D, 0.875D, -1.75D * 0.8D, -1.75D * 0.8D, 3.5D * 0.8D),
                    "Gun", "Grip", "Barrels"),
            specAkimbo("gun_minigun_dual", "minigun", "minigun_dual", SpecialRender.MINIGUN_DUAL,
                    inv(0.875D, 0.0D, 0.0D, 0.0D), fp(0.375D, 0.875D, 0.0D, 0.0D, 0.0D),
                    "Gun", "GunDual", "Barrels"),
            specSpecial("gun_am180", "am180", "am180", SpecialRender.AM180,
                    inv(0.75D, 1.5D, 0.0D, 0.0D), fp(0.1875D, 0.875D, -0.8D, -0.8D, 0.8D),
                    "Gun", "Silencer", "Trigger", "Bolt", "Mag", "MagPlate"),
            specSpecial("gun_liberator", "liberator", "liberator", SpecialRender.LIBERATOR,
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.2D, -1.0D, 1.0D),
                    "Gun", "Barrel", "Shell1", "Shell2", "Shell3", "Shell4", "Latch"),
            specSpecial("gun_congolake", "congolake", "congolake", SpecialRender.CONGOLAKE,
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
            specSpecial("gun_autoshotgun", "shredder", "shredder", SpecialRender.SHREDDER,
                    inv(1.25D, -1.5D, 0.0D, 0.0D), fp(0.25D, 0.875D, -1.2D, -1.0D, 1.2D)),
            specSpecial("gun_autoshotgun_shredder", "shredder", "shredder_orig", SpecialRender.SHREDDER,
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
            specSpecial("gun_hangman", "hangman", "hangman", SpecialRender.HANGMAN,
                    inv(0.375D, -0.5D, 2.5D, 0.0D), fp(0.125D, 0.875D, -1.2D, -0.7D, 1.4D),
                    "Rifle", "Internals", "Lid", "Magazine", "Bullets"),
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
        if (displayContext.firstPerson() && hidesFirstPersonAtFullAim(stack)) {
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
        if (spec.specialRender() == SpecialRender.DEBUG) {
            renderDebug(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.TESLA_CANNON) {
            renderTeslaCannon(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FATMAN) {
            renderFatman(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FOLLY) {
            renderFolly(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.PEPPERBOX) {
            renderPepperbox(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.HENRY) {
            renderHenry(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.ATLAS) {
            renderAtlas(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.HANGMAN) {
            renderHangman(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.TAU) {
            renderTau(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.GREASEGUN) {
            renderGreasegun(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FLAREGUN) {
            renderFlaregun(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CONGOLAKE) {
            renderCongolake(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MARESLEG) {
            renderMaresleg(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LIBERATOR) {
            renderLiberator(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CARBINE) {
            renderCarbine(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MINIGUN) {
            renderMinigun(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.AM180) {
            renderAm180(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.UZI) {
            renderUzi(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STAR_F) {
            renderStarF(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.G3) {
            renderG3(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.AMAT) {
            renderAmat(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MK108) {
            renderMk108(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.SEXY) {
            renderSexy(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MAS36) {
            renderMas36(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.BOLTER) {
            renderBolter(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STG77) {
            renderStg77(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LASER_PISTOL) {
            renderLaserPistol(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.PANZERSCHRECK) {
            renderPanzerschreck(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.STINGER) {
            renderStinger(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.QUADRO) {
            renderQuadro(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.MISSILE_LAUNCHER) {
            renderMissileLauncher(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LASRIFLE) {
            renderLasrifle(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.ABERRATOR) {
            renderAberrator(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.LAG) {
            renderLag(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.M2) {
            renderM2(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.COILGUN) {
            renderCoilgun(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.FIREEXT) {
            renderFireExt(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.SPAS12) {
            renderSpas12(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.SHREDDER) {
            renderShredder(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.CHARGE_THROWER) {
            renderChargeThrower(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.DOUBLE_BARREL) {
            renderDoubleBarrel(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else if (spec.specialRender() == SpecialRender.HEAVY_REVOLVER) {
            renderHeavyRevolver(stack, displayContext, model, spec, poseStack, buffer, packedLight, packedOverlay);
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

    /**
     * Renders only the source-audited remote {@code flashMap} muzzle plume after the caller has applied the vanilla
     * holder-hand matrix. This keeps entity context out of the BEWLR while sharing its actual third-person display
     * transform and the unified transient quad backend.
     */
    public static void renderRemoteMuzzleFlash(ItemStack stack, ItemDisplayContext displayContext, long shotMillis,
            PoseStack poseStack, MultiBufferSource buffer) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem) || shotMillis <= 0L
                || (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            return;
        }
        String legacyName = gunItem.gunConfig().legacyName();
        if (!hasRemoteMuzzleFlash(legacyName)) {
            return;
        }
        RenderSpec spec = SPECS.get(legacyName);
        if (spec == null) {
            return;
        }
        LegacyWavefrontModel model = MODELS.computeIfAbsent(spec,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        String[] visibleParts = spec.visibleParts().toArray(String[]::new);
        AABB bounds = displayBounds(displayContext,
                visibleParts.length == 0 ? model.boundsAll() : model.boundsOnly(visibleParts), spec);
        if (bounds.getXsize() <= 0.0D || bounds.getYsize() <= 0.0D || bounds.getZsize() <= 0.0D) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(stack, displayContext, poseStack, bounds, spec);
        int receiverIndex = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? 0 : 1;
        switch (legacyName) {
            case "gun_debug" -> {
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                poseStack.translate(0.125D, 2.5D, 0.0D);
                ClientSednaGunEffects.renderGapFlash(shotMillis, poseStack, buffer);
            }
            case "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege" -> {
                poseStack.translate(0.125D, 2.5D, 0.0D);
                ClientSednaGunEffects.renderGapFlash(shotMillis, poseStack, buffer);
            }
            case "gun_uzi_akimbo" -> {
                if (!hasUpgrade(stack, receiverIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
                    renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 0.75D, 8.5D, 90.0F, 0.0F, 1.0F,
                            poseStack, buffer);
                }
            }
            case "gun_maresleg_akimbo" -> renderRemoteMuzzleFlash(shotMillis, 75L, 5.0D, 0.0D, 1.0D, 3.75D,
                    90.0F, 0.0F, 1.0F, poseStack, buffer);
            case "gun_minigun_dual" -> renderRemoteMuzzleFlash(shotMillis, 50L, 7.5D, 0.0D, 0.5D, 12.25D,
                    90.0F, 0.0F, 1.5F, poseStack, buffer);
            case "gun_aberrator_eott" -> {
                renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 2.0D, 4.0D, 90.0F, 0.0F, 0.75F,
                        poseStack, buffer);
                poseStack.pushPose();
                poseStack.translate(0.0D, 2.0D, -1.5D);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                ClientSednaGunEffects.renderFireball(shotMillis, poseStack, buffer);
                poseStack.popPose();
            }
            case "gun_light_revolver_dani" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 1.5D,
                    9.25D, 90.0F, 0.0F, 1.0F, poseStack, buffer);
            case "gun_star_f_akimbo" -> {
                if (!hasUpgrade(stack, receiverIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
                    renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 3.0D, 6.125D, 90.0F, 0.0F, 0.75F,
                            poseStack, buffer);
                }
            }
            case "gun_uzi" -> {
                if (!hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER)) {
                    renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 0.75D, 8.5D, 90.0F, 0.0F, 1.0F,
                            poseStack, buffer);
                }
            }
            case "gun_carbine" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 1.0D, 8.0D, 90.0F,
                    0.0F, 0.5F, poseStack, buffer);
            case "gun_pepperbox" -> {
                renderRemoteMuzzleFlash(shotMillis, 75L, 15.0D, 0.0D, 0.5D, 7.0D, 90.0F, 0.0F, 0.5F,
                        poseStack, buffer);
                renderRemoteMuzzleFlash(shotMillis, 75L, 15.0D, 0.0D, 0.5D, 7.0D, 90.0F, 45.0F, 0.5F,
                        poseStack, buffer);
            }
            case "gun_stinger" -> renderRemoteMuzzleFlash(shotMillis, 150L, 10.0D, 0.0D, 3.5D, -10.3795D,
                    90.0F, 0.0F, 0.75F, poseStack, buffer);
            case "gun_double_barrel", "gun_double_barrel_sacred_dragon" -> renderRemoteMuzzleFlash(shotMillis,
                    75L, 5.0D, 0.0D, 0.0D, 8.0D, 90.0F, 0.0F, 2.0F, poseStack, buffer);
            case "gun_g3" -> {
                if (!hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER)) {
                    renderRemoteMuzzleFlash(shotMillis, 75L, 10.0D, 0.0D, 0.0D, 12.0D, 90.0F, -25.0F, 0.75F,
                            poseStack, buffer);
                }
            }
            case "gun_greasegun" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 0.0D, 8.0D, 90.0F,
                    0.0F, 0.5F, poseStack, buffer);
            case "gun_lag" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, -10.25D, 1.0D, 0.0D, 0.0F,
                    0.0F, 1.0F, poseStack, buffer);
            case "gun_m2" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 1.625D, 5.0D, 90.0F, 0.0F,
                    0.5F, poseStack, buffer);
            case "gun_maresleg", "gun_maresleg_broken" -> renderRemoteMuzzleFlash(shotMillis, 75L, 5.0D,
                    0.0D, 1.0D, isMareslegShortened(stack, spec) ? 3.75D : 8.0D, 90.0F, 0.0F, 1.0F, poseStack,
                    buffer);
            case "gun_liberator" -> renderRemoteMuzzleFlash(shotMillis, 75L, 5.0D, 0.0D, 0.5D, 8.0D, 90.0F,
                    0.0F, 1.5F, poseStack, buffer);
            case "gun_henry", "gun_henry_lincoln" -> renderRemoteMuzzleFlash(shotMillis, 75L, 5.0D, 0.0D, 1.0D,
                    8.0D, 90.0F, 0.0F, 1.0F, poseStack, buffer);
            case "gun_mk108" -> renderRemoteMuzzleFlash(shotMillis, 50L, 5.0D, 0.0D, 0.0D, 8.125D, 90.0F,
                    0.0F, 1.0F, poseStack, buffer);
            case "gun_am180" -> {
                boolean silenced = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
                renderRemoteMuzzleFlash(shotMillis, silenced ? 75L : 50L, silenced ? 5.0D : 7.5D, 0.0D, 1.875D,
                        silenced ? 16.75D : 12.0D, 90.0F, 0.0F, silenced ? 0.5F : 0.75F, poseStack, buffer);
            }
            case "gun_spas12" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 1.5D, -11.0D, -90.0F,
                    0.0F, 1.0F, poseStack, buffer);
            case "gun_aberrator" -> {
                renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 2.0D, 4.0D, 90.0F, 0.0F, 0.75F,
                        poseStack, buffer);
                poseStack.pushPose();
                poseStack.translate(0.0D, 2.0D, -1.5D);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                ClientSednaGunEffects.renderFireball(shotMillis, poseStack, buffer);
                poseStack.popPose();
            }
            case "gun_hangman" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 0.0D, 29.0D, 90.0F,
                    0.0F, 2.0F, poseStack, buffer);
            case "gun_n_i_4_n_i" -> renderRemoteLaserFlash(shotMillis, 75L, 7.5D, 0xFFFFFF, 0.0D, 0.75D, 4.0D,
                    90.0F, 0.0F, 0.125F, poseStack, buffer);
            case "gun_star_f" -> {
                if (!hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER)) {
                    renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 3.0D, 6.25D, 90.0F, 0.0F, 0.75F,
                            poseStack, buffer);
                }
            }
            case "gun_congolake" -> renderRemoteMuzzleFlash(shotMillis, 150L, 7.5D, 0.0D, 1.75D, 4.25D, 90.0F,
                    0.0F, 0.5F, poseStack, buffer);
            case "gun_amat", "gun_amat_subtlety", "gun_amat_penance" -> {
                if (!isAmatSilenced(stack)) {
                    poseStack.pushPose();
                    poseStack.translate(0.0D, 0.5D, 11.0D);
                    LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                    ClientSednaGunEffects.renderGapFlash(shotMillis, poseStack, buffer);
                    poseStack.popPose();
                }
            }
            case "gun_light_revolver", "gun_light_revolver_atlas" -> renderRemoteMuzzleFlash(shotMillis, 75L,
                    7.5D, 0.0D, 1.5D, 9.25D, 90.0F, 0.0F, 1.0F, poseStack, buffer);
            case "gun_autoshotgun", "gun_autoshotgun_shredder" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D,
                    0.0D, 1.0D, 7.5D, 90.0F, 0.0F, 0.75F, poseStack, buffer);
            case "gun_autoshotgun_sexy", "gun_autoshotgun_heretic" -> renderRemoteMuzzleFlash(shotMillis, 150L,
                    7.5D, 0.0D, 0.0D, 8.0D, 90.0F, 0.0F, 1.0F, poseStack, buffer);
            case "gun_panzerschreck" -> renderRemoteMuzzleFlash(shotMillis, 150L, 7.5D, 0.0D, 0.0D, 6.5D,
                    90.0F, 0.0F, 0.75F, poseStack, buffer);
            case "gun_laser_pistol", "gun_laser_pistol_pew_pew", "gun_laser_pistol_morning_glory" -> {
                boolean emerald = "gun_laser_pistol_morning_glory".equals(legacyName);
                renderRemoteLaserFlash(shotMillis, 150L, 1.5D, emerald ? 0x008000 : 0xFF0000, 0.0D, 2.0D, 4.75D,
                        90.0F, 0.0F, 1.0F, poseStack, buffer);
                renderRemoteLaserFlash(shotMillis, 150L, 0.75D, emerald ? 0x80FF00 : 0xFF8000, 0.0D, 2.0D, 4.5D,
                        90.0F, 0.0F, 1.0F, poseStack, buffer);
            }
            case "gun_lasrifle" -> {
                renderRemoteLaserFlash(shotMillis, 150L, 1.5D, 0xFF0000, 0.0D, 1.5D, 12.0D, 90.0F, 0.0F, 1.0F,
                        poseStack, buffer);
                renderRemoteLaserFlash(shotMillis, 150L, 0.75D, 0xFF8000, 0.0D, 1.5D, 11.75D, 90.0F, 0.0F, 1.0F,
                        poseStack, buffer);
            }
            case "gun_minigun" -> renderRemoteMuzzleFlash(shotMillis, 50L, 7.55D, 0.0D, 0.5D, 12.25D, 90.0F,
                    0.0F, 1.5F, poseStack, buffer);
            case "gun_minigun_lacunae" -> {
                renderRemoteLaserFlash(shotMillis, 50L, 1.0D, 0xFF00FF, 0.0D, 0.0D, 12.25D, 90.0F, 0.0F, 1.0F,
                        poseStack, buffer);
                renderRemoteLaserFlash(shotMillis, 50L, 0.5D, 0xFF0080, 0.0D, 0.0D, 12.0D, 90.0F, 0.0F, 1.0F,
                        poseStack, buffer);
            }
            case "gun_mas36" -> renderRemoteMuzzleFlash(shotMillis, 75L, 10.0D, 0.0D, 1.0D, 8.0D, 90.0F,
                    0.0F, 0.5F, poseStack, buffer);
            case "gun_missile_launcher" -> renderRemoteMuzzleFlash(shotMillis, 75L, 7.5D, 0.0D, 1.0D, 6.75D,
                    90.0F, 0.0F, 0.75F, poseStack, buffer);
            case "gun_quadro" -> renderRemoteMuzzleFlash(shotMillis, 150L, 7.5D, 0.0D, 0.75D, 2.0D, 90.0F,
                    0.0F, 0.75F, poseStack, buffer);
            case "gun_stg77" -> {
                poseStack.translate(0.0D, 0.0D, 7.5D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                poseStack.scale(0.25F, 0.25F, 0.25F);
                LegacyPoseRotations.rotateXDegrees(poseStack, -5.0F);
                ClientSednaGunEffects.renderGapFlash(shotMillis, poseStack, buffer);
            }
            default -> {
                // Guarded by hasRemoteMuzzleFlash; retained for future source-audited additions.
            }
        }
        poseStack.popPose();
    }

    private static boolean hasRemoteMuzzleFlash(String legacyName) {
        return switch (legacyName) {
            case "gun_debug", "gun_uzi", "gun_uzi_akimbo", "gun_carbine", "gun_stinger", "gun_double_barrel",
                    "gun_double_barrel_sacred_dragon", "gun_g3", "gun_greasegun", "gun_lag", "gun_m2",
                    "gun_maresleg", "gun_maresleg_broken", "gun_maresleg_akimbo", "gun_mk108", "gun_am180",
                    "gun_liberator", "gun_spas12", "gun_aberrator", "gun_aberrator_eott", "gun_hangman",
                    "gun_n_i_4_n_i", "gun_pepperbox", "gun_panzerschreck", "gun_mas36", "gun_missile_launcher",
                    "gun_quadro", "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege",
                    "gun_henry", "gun_henry_lincoln", "gun_autoshotgun_sexy", "gun_autoshotgun_heretic", "gun_stg77",
                    "gun_star_f", "gun_star_f_akimbo", "gun_congolake", "gun_amat", "gun_amat_subtlety",
                    "gun_amat_penance", "gun_light_revolver", "gun_light_revolver_atlas", "gun_light_revolver_dani",
                    "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_laser_pistol", "gun_laser_pistol_pew_pew",
                    "gun_laser_pistol_morning_glory", "gun_lasrifle", "gun_minigun", "gun_minigun_dual",
                    "gun_minigun_lacunae" -> true;
            default -> false;
        };
    }

    /** Whether the third-person renderer owns two source receiver poses instead of one held-gun pose. */
    public static boolean isAkimbo(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return false;
        }
        RenderSpec spec = SPECS.get(gunItem.gunConfig().legacyName());
        return spec != null && spec.specialRender().akimbo();
    }

    private static void renderRemoteMuzzleFlash(long shotMillis, long durationMillis, double length, double x,
            double y, double z, float yawDegrees, float pitchDegrees, float scale, PoseStack poseStack,
            MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        if (yawDegrees != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, yawDegrees);
        }
        if (pitchDegrees != 0.0F) {
            LegacyPoseRotations.rotateXDegrees(poseStack, pitchDegrees);
        }
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
        ClientSednaGunEffects.renderMuzzleFlash(shotMillis, durationMillis, length, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderRemoteLaserFlash(long shotMillis, long durationMillis, double flashScale, int color,
            double x, double y, double z, float yawDegrees, float pitchDegrees, float scale, PoseStack poseStack,
            MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        if (yawDegrees != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, yawDegrees);
        }
        if (pitchDegrees != 0.0F) {
            LegacyPoseRotations.rotateXDegrees(poseStack, pitchDegrees);
        }
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
        ClientSednaGunEffects.renderLaserFlash(shotMillis, durationMillis, flashScale, color, poseStack, buffer);
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
                LegacyPoseRotations.rotateZDegrees(poseStack, -22.5F);
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
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equipX);
        poseStack.translate(0.0D, 2.0D, 2.0D);
        poseStack.translate(0.0D, 0.0D, recoilZ);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoilZ * 2.0D));
    }

    private static void rotateTeslaCog(PoseStack poseStack, double angle) {
        poseStack.translate(0.0D, -1.625D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) angle);
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
        boolean bus = legacyBusActive();
        if (!bus && teslaLegacyAnimation(stack) != LEGACY_ANIM_INSPECT) {
            return;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        if (!bus && millis > 2000.0D) {
            return;
        }

        double[] position = bus ? LegacyHbmAnimations.getRelevantTransformation("YOMI") : teslaYomiPosition(millis);
        double squeezeZ = bus ? LegacyHbmAnimations.getRelevantTransformation("SQUEEZE")[2] : teslaYomiSqueezeZ(millis);
        poseStack.pushPose();
        poseStack.translate(position[0], position[1], position[2]);
        LegacyPoseRotations.rotateYDegrees(poseStack, 135.0F);
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

    private static void renderFatman(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Handle", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            if (!isMagazineLoaded(stack)) {
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 3.0D);
                ObjWeaponModels.renderPart(model, "Piston", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
                poseStack.popPose();
            } else {
                ObjWeaponModels.renderPart(model, "Piston", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
                ObjWeaponModels.renderPart(model, "MiniNuke", fatmanNukeTexture(stack), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lid = LegacyHbmAnimations.getRelevantTransformation("LID");
        double[] nuke = LegacyHbmAnimations.getRelevantTransformation("NUKE");
        double[] piston = LegacyHbmAnimations.getRelevantTransformation("PISTON");
        double[] handle = LegacyHbmAnimations.getRelevantTransformation("HANDLE");
        double[] gauge = LegacyHbmAnimations.getRelevantTransformation("GAUGE");
        poseStack.translate(0.0D, 1.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, -1.0D, 2.0D);
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, handle[2]);
        ObjWeaponModels.renderPart(model, "Handle", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(.4375D, -.875D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) gauge[2]);
        poseStack.translate(-.4375D, .875D, 0.0D);
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(.25D, .125D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) lid[2]);
        poseStack.translate(-.25D, -.125D, 0.0D);
        ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        boolean loaded = isMagazineLoaded(stack);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, piston[2]);
        if (!loaded) {
            if (piston[2] == 0.0D) {
                poseStack.translate(0.0D, 0.0D, 3.0D);
            }
        }
        ObjWeaponModels.renderPart(model, "Piston", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        if (loaded || nuke[0] != 0.0D || nuke[1] != 0.0D || nuke[2] != 0.0D) {
            poseStack.pushPose();
            poseStack.translate(nuke[0], nuke[1], nuke[2]);
            ObjWeaponModels.renderPart(model, "MiniNuke", fatmanNukeTexture(stack), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
        }
    }

    private static ResourceLocation fatmanNukeTexture(ItemStack stack) {
        String type = loadedMagazineType(stack);
        if (type.isEmpty()) {
            type = primaryMagazineType(stack);
        }
        return "nuke_balefire".equals(type) ? FATMAN_BALEFIRE_TEXTURE : FATMAN_MININUKE_TEXTURE;
    }

    private static boolean isMagazineLoaded(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && firstLoadedMagazine(gunItem, tag) != null;
    }

    private static String loadedMagazineType(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return "";
        }
        CompoundTag tag = stack.getTag();
        SednaMagazineConfig magazine = tag == null ? null : firstLoadedMagazine(gunItem, tag);
        return resolvedMagazineType(tag, magazine);
    }

    /**
     * Reads the configured primary magazine type without treating an empty count as an absent type.
     * MagazineFullReload keeps that type while its reload animation is in progress, and Charge Thrower
     * needs it to render the source projectile during the AMMO rail.
     */
    private static String primaryMagazineType(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return "";
        }
        CompoundTag tag = stack.getTag();
        SednaMagazineConfig magazine = firstMagazine(gunItem);
        return resolvedMagazineType(tag, magazine);
    }

    private static String resolvedMagazineType(CompoundTag tag, SednaMagazineConfig magazine) {
        if (magazine == null) {
            return "";
        }
        List<BulletConfig> accepted = magazine.acceptedBulletConfigNames().stream()
                .map(LegacySednaRuntimeBulletConfigs::byName)
                .flatMap(java.util.Optional::stream)
                .toList();
        if (accepted.isEmpty()) {
            return "";
        }
        if (magazine.kind() != SednaMagazineConfig.Kind.BELT && tag != null) {
            BulletConfig stored = LegacySednaRuntimeBulletConfigs.byName(tag.getString(magazine.nbtTypeKey()))
                    .orElse(null);
            if (stored != null && accepted.contains(stored)) {
                return stored.legacyName();
            }
        }
        return accepted.get(0).legacyName();
    }

    private static double primaryMagazineFill(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        SednaMagazineConfig magazine = tag == null ? null : firstMagazine(gunItem);
        if (magazine == null || magazine.nbtCountKey().isBlank() || magazine.capacity() <= 0) {
            return 0.0D;
        }
        double fill = (double) tag.getInt(magazine.nbtCountKey()) / (double) Math.max(1, magazine.capacity());
        return Math.max(0.0D, Math.min(1.0D, fill));
    }

    private static int primaryMagazineAmount(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        SednaMagazineConfig magazine = tag == null ? null : firstMagazine(gunItem);
        return magazine == null || magazine.nbtCountKey().isBlank() ? 0 : tag.getInt(magazine.nbtCountKey());
    }

    private static int teslaFirstPersonCapacitorCount(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return 0;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        SednaMagazineConfig magazine = firstMagazineOfKind(gunItem, SednaMagazineConfig.Kind.BELT);
        return magazine == null ? 0 : beltAmmoCount(player, magazine);
    }

    private static double magazineFillByOwner(ItemStack stack, String legacyOwnerName) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        SednaMagazineConfig magazine = tag == null ? null : firstMagazineByOwner(gunItem, legacyOwnerName);
        if (magazine == null || magazine.nbtCountKey().isBlank() || magazine.capacity() <= 0) {
            return 0.0D;
        }
        double fill = (double) tag.getInt(magazine.nbtCountKey()) / (double) magazine.capacity();
        return Math.max(0.0D, Math.min(1.0D, fill));
    }

    private static SednaMagazineConfig firstLoadedMagazine(SednaGunItem gunItem, CompoundTag tag) {
        for (SednaGunConfig.GunModeConfig mode : gunItem.gunConfig().configs()) {
            for (SednaReceiverConfig receiver : mode.receivers()) {
                SednaMagazineConfig magazine = receiver.magazineOrNull();
                if (magazine != null && !magazine.nbtCountKey().isBlank()
                        && tag.getInt(magazine.nbtCountKey()) > 0) {
                    return magazine;
                }
            }
        }
        return null;
    }

    private static SednaMagazineConfig firstMagazine(SednaGunItem gunItem) {
        for (SednaGunConfig.GunModeConfig mode : gunItem.gunConfig().configs()) {
            for (SednaReceiverConfig receiver : mode.receivers()) {
                SednaMagazineConfig magazine = receiver.magazineOrNull();
                if (magazine != null) {
                    return magazine;
                }
            }
        }
        return null;
    }

    private static SednaMagazineConfig firstMagazineOfKind(SednaGunItem gunItem, SednaMagazineConfig.Kind kind) {
        for (SednaGunConfig.GunModeConfig mode : gunItem.gunConfig().configs()) {
            for (SednaReceiverConfig receiver : mode.receivers()) {
                SednaMagazineConfig magazine = receiver.magazineOrNull();
                if (magazine != null && magazine.kind() == kind) {
                    return magazine;
                }
            }
        }
        return null;
    }

    private static SednaMagazineConfig firstMagazineByOwner(SednaGunItem gunItem, String legacyOwnerName) {
        for (SednaGunConfig.GunModeConfig mode : gunItem.gunConfig().configs()) {
            for (SednaReceiverConfig receiver : mode.receivers()) {
                SednaMagazineConfig magazine = receiver.magazineOrNull();
                if (magazine != null && legacyOwnerName.equals(magazine.legacyOwnerName())) {
                    return magazine;
                }
            }
        }
        return null;
    }

    private static void renderFolly(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] load = LegacyHbmAnimations.getRelevantTransformation("LOAD");
        double[] shell = LegacyHbmAnimations.getRelevantTransformation("SHELL");
        double[] screw = LegacyHbmAnimations.getRelevantTransformation("SCREW");
        double[] breech = LegacyHbmAnimations.getRelevantTransformation("BREECH");
        poseStack.translate(0.0D, 1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) equip[0]));
        poseStack.translate(0.0D, -1.0D, 4.0D);
        poseStack.translate(0.0D, -2.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) load[0]);
        poseStack.translate(0.0D, 2.0D, 2.0D);
        ObjWeaponModels.renderPart(model, "Cannon", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        poseStack.translate(recoil[0], recoil[1], recoil[2]);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(shell[0], shell[1], shell[2]);
        ObjWeaponModels.renderPart(model, "Shell", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(breech[0], breech[1], breech[2]);
        ObjWeaponModels.renderPart(model, "Breech", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) screw[2]);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Cog", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        renderFollyAimingText(stack, poseStack, buffer);
    }

    private static void renderFollyAimingText(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer) {
        boolean aiming = LegacySednaAimProgress.settledFullyAimed();
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
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
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
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            renderLegacyModelText(font, splash, color, poseStack, buffer);
            poseStack.popPose();
        }

        if (follyHasTtyLines(elapsed)) {
            poseStack.pushPose();
            float fontSize = 0.005F;
            poseStack.translate(2.5D, 1.375D, -2.75D);
            poseStack.scale(fontSize, -fontSize, fontSize);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            renderFollyTtyLines(font, player, elapsed, color, poseStack, buffer);
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
        StringBuilder splash = new StringBuilder();
        for (int i = 0; i < FOLLY_BOOT_LETTERS.length; i++) {
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
            splash.append(FOLLY_BOOT_LETTERS[i]);
        }
        return splash.toString();
    }

    private static boolean follyHasTtyLines(long elapsedMillis) {
        return (elapsedMillis > 250L && elapsedMillis < 3000L) || elapsedMillis > 5000L;
    }

    private static void renderFollyTtyLines(Font font, Player player, long elapsedMillis, int color,
            PoseStack poseStack, MultiBufferSource buffer) {
        if (elapsedMillis < 3000L) {
            if (elapsedMillis > 250L) {
                renderFollyTtyLine(font, FOLLY_TTY_POST, color, poseStack, buffer);
            }
            if (elapsedMillis > 500L) {
                renderFollyTtyLine(font, FOLLY_TTY_RAM_INSTALLED, color, poseStack, buffer);
                renderFollyTtyLine(font, FOLLY_TTY_RAM_AVAILABLE, color, poseStack, buffer);
            }
            if (elapsedMillis > 750L) {
                renderFollyTtyLine(font, FOLLY_TTY_SPLINES, color, poseStack, buffer);
            }
            if (elapsedMillis > 1500L) {
                renderFollyTtyLine(font, FOLLY_TTY_NO_KEYBOARD, color, poseStack, buffer);
            }
            if (elapsedMillis > 2000L) {
                renderFollyTtyLine(font, FOLLY_TTY_BOOTING, color, poseStack, buffer);
            }
        }
        if (elapsedMillis > 5000L) {
            renderFollyTtyLine(font, follyTargetLine(player), color, poseStack, buffer);
            renderFollyTtyLine(font, ChatFormatting.GREEN + "Angle: " + ((int) (-player.getXRot() * 100.0F) / 100.0D),
                    color, poseStack, buffer);
        }
    }

    private static void renderFollyTtyLine(Font font, String line, int color, PoseStack poseStack,
            MultiBufferSource buffer) {
        renderLegacyModelText(font, line, color, poseStack, buffer);
        poseStack.translate(0.0D, font.lineHeight + 2.0D, 0.0D);
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

    /** Exact ItemRenderPepperbox first-person part hierarchy; static contexts intentionally omit loader/shot. */
    private static void renderPepperbox(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            ObjWeaponModels.renderPart(model, "Cylinder", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            ObjWeaponModels.renderPart(model, "Trigger", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] cylinder = LegacyHbmAnimations.getRelevantTransformation("ROTATE");
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER");
        double[] trigger = LegacyHbmAnimations.getRelevantTransformation("TRIGGER");
        double[] translate = LegacyHbmAnimations.getRelevantTransformation("TRANSLATE");
        double[] loader = LegacyHbmAnimations.getRelevantTransformation("LOADER");
        double[] shot = LegacyHbmAnimations.getRelevantTransformation("SHOT");

        poseStack.translate(translate[0], translate[1], translate[2]);
        poseStack.translate(0.0D, 0.0D, -5.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) recoil[0]));
        poseStack.translate(0.0D, 0.0D, 5.0D);

        if (loader[0] != 0.0D || loader[1] != 0.0D || loader[2] != 0.0D) {
            poseStack.pushPose();
            poseStack.translate(loader[0], loader[1], loader[2]);
            ObjWeaponModels.renderPart(model, "Speedloader", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            if (shot[0] != 0.0D) {
                ObjWeaponModels.renderPart(model, "Shot", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            }
            poseStack.popPose();
        }

        ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) cylinder[0]);
        ObjWeaponModels.renderPart(model, "Cylinder", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.375D, -1.875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) hammer[0]);
        poseStack.translate(0.0D, -0.375D, 1.875D);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -trigger[0] * 0.5D);
        ObjWeaponModels.renderPart(model, "Trigger", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderPepperboxEffects(gun, poseStack, buffer);
        }
    }

    /** Exact ItemRenderHenry first-person hierarchy shared by the normal and Lincoln textures. */
    private static void renderHenry(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] sight = LegacyHbmAnimations.getRelevantTransformation("SIGHT");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER");
        double[] lever = LegacyHbmAnimations.getRelevantTransformation("LEVER");
        double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] twist = LegacyHbmAnimations.getRelevantTransformation("TWIST");
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET");
        double[] yeet = LegacyHbmAnimations.getRelevantTransformation("YEET");
        double[] roll = LegacyHbmAnimations.getRelevantTransformation("ROLL");

        poseStack.translate(recoil[0] * 2.0D, recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 5.0D));
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) turn[2]);
        poseStack.translate(yeet[0], yeet[1], yeet[2]);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) roll[2]);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.translate(0.0D, -4.0D, 4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 4.0D, -4.0D);
        poseStack.translate(0.0D, 2.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) equip[0]));
        poseStack.translate(0.0D, -2.0D, 4.0D);

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.25D, -0.1875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) sight[0]);
        poseStack.translate(0.0D, -1.25D, 0.1875D);
        ObjWeaponModels.renderPart(model, "Sight", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.625D, -3.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-30.0D + hammer[0]));
        poseStack.translate(0.0D, -0.625D, 3.0D);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, -2.3125D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lever[0]);
        poseStack.translate(0.0D, -0.25D, 2.3125D);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) twist[2]);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Front", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(bullet[0], bullet[1], bullet[2] - 1.0D);
        ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderHenryEffects(gun, turn[2], poseStack, buffer);
        }
    }

    /** Exact ItemRenderAtlas first-person hierarchy shared by Light Revolver and Atlas. */
    private static void renderAtlas(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] reloadMove = LegacyHbmAnimations.getRelevantTransformation("RELOAD_MOVE");
        double[] reloadRot = LegacyHbmAnimations.getRelevantTransformation("RELOAD_ROT");
        double[] front = LegacyHbmAnimations.getRelevantTransformation("FRONT");
        double[] latch = LegacyHbmAnimations.getRelevantTransformation("LATCH");
        double[] drum = LegacyHbmAnimations.getRelevantTransformation("DRUM");
        double[] drumPush = LegacyHbmAnimations.getRelevantTransformation("DRUM_PUSH");
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER");

        poseStack.translate(recoil[0], recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 10.0D));
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
        poseStack.translate(0.0D, 0.0D, -7.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) equip[0]));
        poseStack.translate(0.0D, 0.0D, 7.0D);
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderRevolverSmoke(gun, 0, recoil[2], poseStack, buffer);
        }
        poseStack.translate(reloadMove[0], reloadMove[1], reloadMove[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) reloadRot[0]);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) reloadRot[2]);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) reloadRot[1]);

        ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) front[2]);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.3125D, -0.875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) latch[2]);
        poseStack.translate(0.0D, -2.3125D, 0.875D);
        ObjWeaponModels.renderPart(model, "Latch", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (drum[2] * 60.0D));
        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.translate(0.0D, 0.0D, drumPush[2]);
        ObjWeaponModels.renderPart(model, "Drum", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -4.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-45.0D + 45.0D * hammer[2]));
        poseStack.translate(0.0D, 0.0D, 4.5D);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderRevolverFlash(gun, 0, poseStack, buffer);
        }
    }

    /** Exact ItemRenderHangman first-person body and part hierarchy. */
    private static void renderHangman(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] roll = LegacyHbmAnimations.getRelevantTransformation("ROLL");
        double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
        double[] smack = LegacyHbmAnimations.getRelevantTransformation("SMACK");
        double[] lid = LegacyHbmAnimations.getRelevantTransformation("LID");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] bullets = LegacyHbmAnimations.getRelevantTransformation("BULLETS");

        poseStack.translate(1.2D, 0.0D, -1.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) turn[1]);
        poseStack.translate(-1.2D, 0.0D, 1.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) roll[2]);
        poseStack.translate(smack[0], smack[1], smack[2]);
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
        poseStack.translate(0.0D, -4.0D, -10.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 4.0D, 10.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);

        ObjWeaponModels.renderPart(model, "Rifle", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Internals", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(-2.1875D, -1.75D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) lid[2]);
        poseStack.translate(2.1875D, 1.75D, 0.0D);
        ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        if (bullets[0] == 0.0D) {
            ObjWeaponModels.renderPart(model, "Bullets", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderHangmanEffects(gun, poseStack, buffer);
        }
    }

    private static void renderSpas12(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "MainBody", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "PumpGrip", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            return;
        }

        double[] mainBody = LegacyHbmAnimations.getRelevantTransformation("MainBody");
        double[] pumpGrip = LegacyHbmAnimations.getRelevantTransformation("PumpGrip");
        double[] shell = LegacyHbmAnimations.getRelevantTransformation("Shell");
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");

        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        LegacyBusAnimationTransforms.apply(poseStack, mainBody);
        ObjWeaponModels.renderPart(model, "MainBody", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, pumpGrip);
        ObjWeaponModels.renderPart(model, "PumpGrip", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        SpentCasingDefinition casing = LegacySednaRuntimeBulletConfigs.byName(primaryMagazineType(stack))
                .map(config -> SpentCasingDefinition.fromName(config.spentCasingName()))
                .orElse(null);
        int foreColor = casing == null ? SpentCasingDefinition.COLOR_CASE_BRASS : casing.color(0);
        int shellColor = casing == null ? SpentCasingDefinition.COLOR_CASE_BRASS : casing.color(1);
        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, shell);
        ObjWeaponModels.renderPart(model, "Shell", ObjEffectModels.CASINGS_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay, (shellColor >>> 16) & 0xFF, (shellColor >>> 8) & 0xFF, shellColor & 0xFF, 0xFF);
        ObjWeaponModels.renderPart(model, "ShellFore", ObjEffectModels.CASINGS_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay, (foreColor >>> 16) & 0xFF, (foreColor >>> 8) & 0xFF, foreColor & 0xFF, 0xFF);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderSpas12Effects(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void renderShredder(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] speen = LegacyHbmAnimations.getRelevantTransformation("SPEEN");
        double[] cycle = LegacyHbmAnimations.getRelevantTransformation("CYCLE");

        poseStack.translate(0.0D, -2.0D, -6.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 2.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 0.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        poseStack.translate(0.0D, -1.0D, -0.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) speen[0]);
        poseStack.translate(0.0D, 1.0D, 0.5D);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, -1.0D, -0.5D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) cycle[2]);
        poseStack.translate(0.0D, 1.0D, 0.5D);
        ObjWeaponModels.renderPart(model, "Shells", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderShredderEffects(gun, poseStack, buffer);
        }
        renderShredderAimLabel(poseStack, buffer);
    }

    private static void renderShredderAimLabel(PoseStack poseStack, MultiBufferSource buffer) {
        if (!LegacySednaAimProgress.settledFullyAimed()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        float scale = 0.04F;
        float variance = 0.9F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.1F;
        int color = 0xFF000000 | (Math.round(variance * 255.0F) << 8);
        poseStack.pushPose();
        poseStack.translate((font.width("[> <]") / 2.0D) * scale, 3.25D, -1.75D);
        poseStack.scale(scale, -scale, scale);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        renderLegacyModelText(font, "[> <]", color, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderTau(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double recoil = displayContext.firstPerson() ? tauRecoilZ(stack) : 0.0D;
        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, -1.0D, -4.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) tauEquipX(stack));
            poseStack.translate(0.0D, 1.0D, 4.0D);
            poseStack.translate(0.0D, 0.0D, recoil);
            poseStack.translate(0.0D, 0.0D, -2.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil * 5.0D));
            poseStack.translate(0.0D, 0.0D, 2.0D);
        }
        ObjWeaponModels.renderPart(model, "Body", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) tauRotorZ(stack));
            poseStack.translate(0.0D, 0.25D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Rotor", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static double tauEquipX(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("EQUIP")[0];
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_EQUIP) {
            return millis <= 500.0D ? lerp(45.0D, 0.0D, sinFull(millis / 500.0D)) : 0.0D;
        }
        if (animation == LEGACY_ANIM_INSPECT) {
            if (millis <= 150.0D) return lerp(0.0D, 2.0D, sinDown(millis / 150.0D));
            if (millis <= 250.0D) return lerp(2.0D, 0.0D, sinFull((millis - 150.0D) / 100.0D));
        }
        return 0.0D;
    }

    private static double tauRecoilZ(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("RECOIL")[2];
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_CYCLE) {
            if (millis <= 50.0D) return lerp(0.0D, -0.5D, millis / 50.0D);
            if (millis <= 200.0D) return lerp(-0.5D, 0.0D, sinFull((millis - 50.0D) / 150.0D));
        }
        if (animation == LEGACY_ANIM_ALT_CYCLE) {
            if (millis <= 100.0D) return lerp(0.0D, -3.0D, sinDown(millis / 100.0D));
            if (millis <= 350.0D) return lerp(-3.0D, 0.0D, sinFull((millis - 100.0D) / 250.0D));
        }
        return 0.0D;
    }

    private static double tauRotorZ(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("ROTATE")[2];
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_CYCLE || animation == LEGACY_ANIM_ALT_CYCLE) {
            if (millis <= 50.0D) return lerp(0.0D, -5.0D, sinDown(millis / 50.0D));
            if (millis <= 150.0D) return lerp(-5.0D, 5.0D, sinFull((millis - 50.0D) / 100.0D));
            if (millis <= 200.0D) return lerp(5.0D, 0.0D, sinUp((millis - 150.0D) / 50.0D));
            return 0.0D;
        }
        if (animation == LEGACY_ANIM_INSPECT) {
            return millis <= 1500.0D ? lerp(0.0D, -1080.0D, sinDown(millis / 1500.0D)) : -1080.0D;
        }
        if (animation == LEGACY_ANIM_SPINUP) {
            if (millis <= 3000.0D) return lerp(0.0D, 2160.0D, sinUp(millis / 3000.0D));
            if (millis <= 13_000.0D) return lerp(0.0D, 14_400.0D, (millis - 3000.0D) / 10_000.0D);
            return 14_400.0D;
        }
        return 0.0D;
    }

    private static void renderGreasegun(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = hasUpgrade(stack, SednaWeaponModEvaluator.ID_GREASEGUN_CLEAN)
                ? GREASEGUN_CLEAN_TEXTURE
                : spec.textureLocation();
        if (displayContext.firstPerson()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] stock = LegacyHbmAnimations.getRelevantTransformation("STOCK");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            double[] flap = LegacyHbmAnimations.getRelevantTransformation("FLAP");
            double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
            double[] handle = LegacyHbmAnimations.getRelevantTransformation("HANDLE");
            double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
            double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
            double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET");
            poseStack.translate(0.0D, -3.0D, -3.0D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]); poseStack.translate(0.0D, 3.0D, 3.0D);
            poseStack.translate(0.0D, -3.0D, -3.0D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]); poseStack.translate(0.0D, 3.0D, 3.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) turn[2]); poseStack.translate(0.0D, 0.0D, recoil[2]);
            ObjWeaponModels.renderPart(model, "Gun", texture, poseStack, buffer, packedLight, packedOverlay);
            poseStack.pushPose(); poseStack.translate(0.0D, 0.0D, -4.0D - stock[2]); ObjWeaponModels.renderPart(model, "Stock", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
            poseStack.pushPose(); poseStack.translate(mag[0], mag[1], mag[2]); ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay); if (bullet[0] != 1.0D) ObjWeaponModels.renderPart(model, "Bullet", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
            poseStack.pushPose(); poseStack.translate(0.0D, -1.4375D, -0.125D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) handle[0]); poseStack.translate(0.0D, 1.4375D, 0.125D); ObjWeaponModels.renderPart(model, "Handle", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
            poseStack.pushPose(); poseStack.translate(0.0D, 0.53125D, 0.0D); LegacyPoseRotations.rotateZDegrees(poseStack, (float) flap[2]); poseStack.translate(0.0D, -0.5125D, 0.0D); ObjWeaponModels.renderPart(model, "Flap", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
            if (stack.getItem() instanceof SednaGunItem gun) {
                ClientSednaGunEffects.renderGreasegunEffects(gun, turn[2], poseStack, buffer);
            }
            return;
        }
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderMaresleg(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean shortened = isMareslegShortened(stack, spec);
        if (!displayContext.firstPerson()) {
            renderMareslegStatic(model, spec, shortened, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        renderMareslegFirstPersonParts(stack, model, spec, shortened, 0, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderMareslegStatic(LegacyWavefrontModel model, RenderSpec spec, boolean shortened,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

    private static void renderMareslegFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec, boolean shortened,
            int animationIndex, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", animationIndex);
        double[] lever = LegacyHbmAnimations.getRelevantTransformation("LEVER", animationIndex);
        double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN", animationIndex);
        double[] flip = LegacyHbmAnimations.getRelevantTransformation("FLIP", animationIndex);
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT", animationIndex);
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", animationIndex);
        double[] shell = LegacyHbmAnimations.getRelevantTransformation("SHELL", animationIndex);
        double[] flag = LegacyHbmAnimations.getRelevantTransformation("FLAG", animationIndex);

        poseStack.pushPose();
        poseStack.translate(recoil[0] * 2.0D, recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 5.0D));
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) turn[2]);
        pivotX(poseStack, 0.0D, 0.0D, -4.0D, (float) lift[0]);
        pivotX(poseStack, 0.0D, 0.0D, -4.0D, (float) -equip[0]);
        pivotX(poseStack, 0.0D, 0.0D, -2.0D, (float) -flip[0]);

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!shortened) {
            ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }

        poseStack.pushPose();
        pivotX(poseStack, 0.0D, 0.125D, -2.875D, (float) lever[0]);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(shell[0], shell[1] - 0.75D, shell[2]);
        ObjWeaponModels.renderPart(model, "Shell", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        if (flag[0] != 0.0D) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -0.5D, 0.0D);
            ObjWeaponModels.renderPart(model, "Shell", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
        }
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderMareslegEffects(gun, animationIndex, shortened, turn[2], flip[0], poseStack,
                    buffer);
        }
        poseStack.popPose();
    }

    private static void pivotX(PoseStack poseStack, double x, double y, double z, float degrees) {
        poseStack.translate(x, y, z);
        LegacyPoseRotations.rotateXDegrees(poseStack, degrees);
        poseStack.translate(-x, -y, -z);
    }

    private static void renderLiberator(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] latch = LegacyHbmAnimations.getRelevantTransformation("LATCH");
        double[] brk = LegacyHbmAnimations.getRelevantTransformation("BREAK");

        poseStack.pushPose();
        pivotX(poseStack, 0.0D, -1.0D, -3.0D, (float) equip[0]);
        pivotX(poseStack, 0.0D, -3.0D, -3.0D, (float) lift[0]);
        poseStack.translate(recoil[0] * 2.0D, recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 10.0D));

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        pivotX(poseStack, 0.0D, -0.5D, 0.75D, (float) brk[0]);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        for (int shell = 1; shell <= 4; shell++) {
            double[] transform = LegacyHbmAnimations.getRelevantTransformation("SHELL" + shell);
            poseStack.pushPose();
            poseStack.translate(transform[0], transform[1], transform[2]);
            ObjWeaponModels.renderPart(model, "Shell" + shell, spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
        }
        pivotX(poseStack, 0.0D, 1.15625D, 0.75D, (float) latch[0]);
        ObjWeaponModels.renderPart(model, "Latch", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderLiberatorEffects(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    /** Exact ItemRenderCarbine first-person body and part hierarchy. */
    private static void renderCarbine(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        if (!displayContext.firstPerson()) {
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
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] slide = LegacyHbmAnimations.getRelevantTransformation("SLIDE");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET");
        double[] rel = LegacyHbmAnimations.getRelevantTransformation("REL");
        double[] stab = LegacyHbmAnimations.getRelevantTransformation("STAB");
        poseStack.translate(0.0D, -1.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 2.0D);
        poseStack.translate(0.0D, 0.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 0.0D, 2.0D);
        poseStack.translate(stab[0], stab[1], stab[2]);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, slide[2]);
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.translate(rel[0], rel[1], rel[2]);
        if (bullet[0] != 1.0D) {
            ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.popPose();
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
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderCarbineEffects(gun, poseStack, buffer);
        }
    }

    /** Exact ItemRenderMinigun first-person body and barrel hierarchy for the two single-gun variants. */
    private static void renderMinigun(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Barrels", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            return;
        }
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] rotate = LegacyHbmAnimations.getRelevantTransformation("ROTATE");
        poseStack.translate(0.0D, 3.0D, -6.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, -3.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Grip", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) rotate[2]);
        ObjWeaponModels.renderPart(model, "Barrels", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            if ("gun_minigun_lacunae".equals(currentLegacyName(stack))) {
                ClientSednaGunEffects.renderMinigunLacunaeEffects(gun, poseStack, buffer);
            } else {
                ClientSednaGunEffects.renderMinigunEffects(gun, poseStack, buffer);
            }
        }
    }

    private static void renderAm180(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean silenced = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        Am180AnimationPose animation = displayContext.firstPerson()
                ? am180AnimationPose(stack) : Am180AnimationPose.IDENTITY;

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, -2.0D, -6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) animation.equipX());
            poseStack.translate(0.0D, 2.0D, 6.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) animation.turnZ());
            poseStack.translate(0.0D, 0.0D, animation.recoilZ());
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (silenced) {
            ObjWeaponModels.renderPart(model, "Silencer", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Trigger", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.0D, animation.boltZ());
        }
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(animation.magX(), animation.magY(), animation.magZ());
            poseStack.translate(0.0D, 2.0625D, 3.75D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) animation.magTurnX());
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) animation.magTurnZ());
            poseStack.translate(0.0D, -2.0625D, -3.75D);
            poseStack.translate(0.0D, 2.3125D, 1.5D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) animation.magSpinX());
            poseStack.translate(0.0D, -2.3125D, -1.5D);
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 1.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, -((float) (primaryMagazineAmount(stack) / 59.0D * 360.0D)));
            poseStack.translate(0.0D, 0.0D, -1.5D);
        }
        ObjWeaponModels.renderPart(model, "Mag", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson()) {
            poseStack.popPose();
        }

        ObjWeaponModels.renderPart(model, "MagPlate", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderAm180Effects(gun, silenced, animation.turnZ(), poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static Am180AnimationPose am180AnimationPose(ItemStack stack) {
        if (legacyBusActive()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            double[] magazine = LegacyHbmAnimations.getRelevantTransformation("MAG");
            double[] magTurn = LegacyHbmAnimations.getRelevantTransformation("MAGTURN");
            double[] magSpin = LegacyHbmAnimations.getRelevantTransformation("MAGSPIN");
            double[] bolt = LegacyHbmAnimations.getRelevantTransformation("BOLT");
            double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
            return new Am180AnimationPose(equip[0], recoil[2], magazine[0], magazine[1], magazine[2],
                    magTurn[0], magTurn[2], magSpin[0], bolt[2], turn[2]);
        }
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_EQUIP) {
            return new Am180AnimationPose(millis <= 500.0D ? lerp(45.0D, 0.0D, sinFull(millis / 500.0D)) : 0.0D,
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (animation == LEGACY_ANIM_CYCLE) {
            double recoil = isTeslaAiming(stack) ? -0.125D : -0.25D;
            double recoilZ = millis <= 15.0D ? lerp(0.0D, recoil, sinDown(millis / 15.0D))
                    : millis <= 50.0D ? lerp(recoil, 0.0D, sinFull((millis - 15.0D) / 35.0D)) : 0.0D;
            return new Am180AnimationPose(0.0D, recoilZ, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (animation == LEGACY_ANIM_CYCLE_DRY) {
            return new Am180AnimationPose(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D,
                    am180Bolt(millis, 550.0D), am180Turn(millis, 300.0D, 15.0D));
        }
        if (animation == LEGACY_ANIM_RELOAD) {
            return new Am180AnimationPose(0.0D, 0.0D, am180ReloadMagX(millis), am180ReloadMagY(millis),
                    am180ReloadMagZ(millis), am180ReloadMagTurnX(millis), am180ReloadMagTurnZ(millis), 0.0D,
                    am180Bolt(millis, 2250.0D), am180Turn(millis, 2000.0D, 15.0D));
        }
        if (animation == LEGACY_ANIM_JAMMED) {
            return new Am180AnimationPose(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D,
                    am180Bolt(millis, 750.0D), am180Turn(millis, 500.0D, 45.0D));
        }
        if (animation == LEGACY_ANIM_INSPECT) {
            return new Am180AnimationPose(0.0D, 0.0D, am180InspectMagX(millis), am180InspectMagY(millis),
                    am180InspectMagZ(millis), am180InspectMagTurnX(millis), am180InspectMagTurnZ(millis),
                    am180InspectMagSpinX(millis), 0.0D, 0.0D);
        }
        return Am180AnimationPose.IDENTITY;
    }

    private static double am180Bolt(double millis, double hold) {
        if (millis <= hold) {
            return 0.0D;
        }
        if (millis <= hold + 100.0D) {
            return lerp(0.0D, -1.5D, sinUp((millis - hold) / 100.0D));
        }
        if (millis <= hold + 200.0D) {
            return lerp(-1.5D, 0.0D, sinUp((millis - hold - 100.0D) / 100.0D));
        }
        return 0.0D;
    }

    private static double am180Turn(double millis, double wait, double angle) {
        if (millis <= wait) {
            return 0.0D;
        }
        if (millis <= wait + 250.0D) {
            return lerp(0.0D, angle, sinFull((millis - wait) / 250.0D));
        }
        if (millis <= wait + 650.0D) {
            return angle;
        }
        if (millis <= wait + 900.0D) {
            return lerp(angle, 0.0D, sinFull((millis - wait - 650.0D) / 250.0D));
        }
        return 0.0D;
    }

    private static double am180ReloadMagX(double millis) {
        if (millis <= 250.0D) return 0.0D;
        if (millis <= 500.0D) return lerp(0.0D, 2.0D, sinFull((millis - 250.0D) / 250.0D));
        if (millis <= 800.0D) return lerp(2.0D, -10.0D, sinUp((millis - 500.0D) / 300.0D));
        if (millis <= 1300.0D) return lerp(3.0D, 2.0D, sinFull((millis - 800.0D) / 500.0D));
        if (millis <= 1550.0D) return lerp(2.0D, 0.0D, sinFull((millis - 1300.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180ReloadMagY(double millis) {
        if (millis <= 500.0D) return 0.0D;
        if (millis <= 800.0D) return lerp(0.0D, 2.0D, sinUp((millis - 500.0D) / 300.0D));
        if (millis <= 1300.0D) return lerp(-6.0D, 0.0D, sinFull((millis - 800.0D) / 500.0D));
        return 0.0D;
    }

    private static double am180ReloadMagZ(double millis) {
        if (millis <= 250.0D) return 0.0D;
        if (millis <= 500.0D) return lerp(0.0D, -4.0D, sinFull((millis - 250.0D) / 250.0D));
        if (millis <= 1300.0D) return -4.0D;
        if (millis <= 1550.0D) return lerp(-4.0D, 0.0D, sinFull((millis - 1300.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180ReloadMagTurnX(double millis) {
        if (millis <= 250.0D) return lerp(0.0D, 15.0D, sinFull(millis / 250.0D));
        if (millis <= 1550.0D) return 15.0D;
        if (millis <= 1800.0D) return lerp(15.0D, 0.0D, sinFull((millis - 1550.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180ReloadMagTurnZ(double millis) {
        return millis <= 500.0D ? 0.0D : millis <= 800.0D
                ? lerp(0.0D, 70.0D, sinFull((millis - 500.0D) / 300.0D)) : 0.0D;
    }

    private static double am180InspectMagX(double millis) {
        if (millis <= 200.0D) return 0.0D;
        if (millis <= 400.0D) return lerp(0.0D, 4.0D, sinFull((millis - 200.0D) / 200.0D));
        if (millis <= 1300.0D) return 4.0D;
        if (millis <= 1550.0D) return lerp(4.0D, 0.0D, sinFull((millis - 1300.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180InspectMagY(double millis) {
        if (millis <= 400.0D) return millis <= 200.0D ? 0.0D : lerp(0.0D, -1.0D, sinFull((millis - 200.0D) / 200.0D));
        if (millis <= 450.0D) return lerp(-1.0D, -1.5D, (millis - 400.0D) / 50.0D);
        if (millis <= 550.0D) return lerp(-1.5D, 0.0D, (millis - 450.0D) / 100.0D);
        if (millis <= 800.0D) return lerp(0.0D, 6.0D, sinDown((millis - 550.0D) / 250.0D));
        if (millis <= 950.0D) return lerp(6.0D, 0.0D, sinUp((millis - 800.0D) / 150.0D));
        if (millis <= 1050.0D) return lerp(0.0D, -1.0D, sinDown((millis - 950.0D) / 100.0D));
        return millis <= 1300.0D ? -1.0D : 0.0D;
    }

    private static double am180InspectMagZ(double millis) {
        if (millis <= 200.0D) return 0.0D;
        if (millis <= 400.0D) return lerp(0.0D, -4.0D, sinFull((millis - 200.0D) / 200.0D));
        if (millis <= 1300.0D) return -4.0D;
        if (millis <= 1550.0D) return lerp(-4.0D, 0.0D, sinFull((millis - 1300.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180InspectMagTurnX(double millis) {
        if (millis <= 250.0D) return lerp(0.0D, 15.0D, sinFull(millis / 250.0D));
        if (millis <= 1650.0D) return 15.0D;
        if (millis <= 1900.0D) return lerp(15.0D, 0.0D, sinFull((millis - 1650.0D) / 250.0D));
        return 0.0D;
    }

    private static double am180InspectMagTurnZ(double millis) {
        return 0.0D;
    }

    private static double am180InspectMagSpinX(double millis) {
        if (millis <= 600.0D) return 0.0D;
        if (millis <= 1100.0D) return lerp(0.0D, -400.0D, sinFull((millis - 600.0D) / 500.0D));
        if (millis <= 1350.0D) return -400.0D;
        if (millis <= 1600.0D) return lerp(-400.0D, -360.0D, (millis - 1350.0D) / 250.0D);
        return -360.0D;
    }

    private static void renderUzi(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            renderUziFirstPersonParts(stack, model, 0, false, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
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
        if (displayContext.firstPerson()) {
            renderStarFFirstPersonParts(stack, model, spec.textureLocation(), 0, 1, poseStack, buffer, packedLight,
                    packedOverlay);
            return;
        }
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

    private static void renderG3(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean stock = !hasUpgrade(stack, SednaWeaponModEvaluator.ID_NO_STOCK);
        boolean silenced = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        boolean scoped = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        ResourceLocation texture = g3Texture(stack, spec);

        if (displayContext.firstPerson()) {
            renderG3FirstPerson(stack, model, texture, stock, silenced, scoped, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

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
        LegacyPoseRotations.rotateXDegrees(poseStack, -30.0F);
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

    private static void renderAmat(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            renderAmatFirstPersonParts(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
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

    /** Exact ItemRenderAmat first-person body, bolt, scope, and bipod hierarchy. */
    private static void renderAmatFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyHbmAnimations.Animation animation = LegacyHbmAnimations.getRelevantAnim();
        boolean deployed = animation == null || animation.animation().getBus("BIPOD") == null;
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] bipod = LegacyHbmAnimations.getRelevantTransformation("BIPOD");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] boltTurn = LegacyHbmAnimations.getRelevantTransformation("BOLT_TURN");
        double[] boltPull = LegacyHbmAnimations.getRelevantTransformation("BOLT_PULL");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] scopeThrow = LegacyHbmAnimations.getRelevantTransformation("SCOPE_THROW");
        double[] scopeSpin = LegacyHbmAnimations.getRelevantTransformation("SCOPE_SPIN");

        poseStack.translate(0.0D, 0.0D, recoil[2]);
        poseStack.translate(0.0D, -3.0D, -8.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 3.0D, 8.0D);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(scopeThrow[0], scopeThrow[1], scopeThrow[2]);
        poseStack.translate(0.0D, 1.5D, -4.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) scopeSpin[0]);
        poseStack.translate(0.0D, -1.5D, 4.5D);
        ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.625D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) boltTurn[2]);
        poseStack.translate(0.0D, -0.625D, 0.0D);
        poseStack.translate(0.0D, 0.0D, boltPull[2]);
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.3125D, -0.625D, -1.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (deployed ? 25.0D : bipod[1]));
        poseStack.translate(-0.3125D, 0.625D, 1.0D);
        ObjWeaponModels.renderPart(model, "BipodHingeLeft", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.translate(0.3125D, -0.625D, -1.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (deployed ? 80.0D : bipod[0]));
        poseStack.translate(-0.3125D, 0.625D, 1.0D);
        ObjWeaponModels.renderPart(model, "BipodLeft", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.3125D, -0.625D, -1.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (deployed ? -25.0D : -bipod[1]));
        poseStack.translate(0.3125D, 0.625D, 1.0D);
        ObjWeaponModels.renderPart(model, "BipodHingeRight", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.translate(-0.3125D, -0.625D, -1.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (deployed ? 80.0D : bipod[0]));
        poseStack.translate(0.3125D, 0.625D, 1.0D);
        ObjWeaponModels.renderPart(model, "BipodRight", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

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
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderAmatEffects(gun, isAmatSilenced(stack), poseStack, buffer);
        }
    }

    private static void renderMk108(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            renderMk108FirstPerson(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
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
        return mk108ShellPositions(1.0D);
    }

    private static double[][] mk108ShellPositions(double reloadProgress) {
        double p = 0.0625D;
        double x = p * 22.0D;
        double y = p * -46.0D;
        double angle = 0.0D;
        double vx = 0.0D;
        double vy = 0.53125D;
        double[] anglesLoaded = { 0.0D, 0.0D, -5.0D, 0.0D, -5.0D, 60.0D, 45.0D, -10.0D, 0.0D };
        double[] anglesUnloaded = { 0.0D, -30.0D, -60.0D, -45.0D, -45.0D, 0.0D, 0.0D, 0.0D, 0.0D };
        double[][] shells = new double[anglesLoaded.length][3];

        for (int i = 0; i < anglesLoaded.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90.0D;
            double delta = Mth.lerp(reloadProgress, anglesUnloaded[i], anglesLoaded[i]);
            angle += delta;
            double radians = -delta * Mth.DEG_TO_RAD;
            double nextVx = vx * Math.cos(radians) - vy * Math.sin(radians);
            double nextVy = vx * Math.sin(radians) + vy * Math.cos(radians);
            vx = nextVx;
            vy = nextVy;
            x += vx;
            y += vy;
        }
        return shells;
    }

    /** Exact ItemRenderFlaregun first-person part hierarchy on the shared OBJ backend. */
    private static void renderFlaregun(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER");
        double[] open = LegacyHbmAnimations.getRelevantTransformation("OPEN");
        double[] shell = LegacyHbmAnimations.getRelevantTransformation("SHELL");
        double[] flip = LegacyHbmAnimations.getRelevantTransformation("FLIP");

        poseStack.translate(recoil[0], recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 10.0D));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) flip[0]);
        poseStack.translate(0.0D, 0.0D, -8.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) equip[0]));
        poseStack.translate(0.0D, 0.0D, 8.0D);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.8125D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (hammer[0] - 15.0D));
        poseStack.translate(0.0D, -1.8125D, 4.0D);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.156D, 1.78D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) open[0]);
        poseStack.translate(0.0D, -2.156D, -1.78D);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(shell[0], shell[1], shell[2]);
        ObjWeaponModels.renderPart(model, "Flare", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderFlaregunSmoke(gun, poseStack, buffer);
        }
    }

    /** ItemRenderCongoLake authored-JSON hierarchy; the source shell uses the shared casing texture. */
    private static void renderCongolake(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Gun"));
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Pump"));
        ObjWeaponModels.renderPart(model, "Pump", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Sight"));
        ObjWeaponModels.renderPart(model, "Sight", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Loop"));
        ObjWeaponModels.renderPart(model, "Loop", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("GuardOuter"));
        ObjWeaponModels.renderPart(model, "GuardOuter", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("GuardInner"));
        ObjWeaponModels.renderPart(model, "GuardInner", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();

        CompoundTag tag = stack.getTag();
        boolean showShell = primaryMagazineAmount(stack) > 0 || tag == null || tag.getInt(LEGACY_LAST_ANIM_KEY) != LEGACY_ANIM_INSPECT;
        if (showShell) {
            SpentCasingDefinition casing = LegacySednaRuntimeBulletConfigs.byName(primaryMagazineType(stack))
                    .map(config -> SpentCasingDefinition.fromName(config.spentCasingName())).orElse(null);
            int shellColor = casing == null ? SpentCasingDefinition.COLOR_CASE_BRASS : casing.color(0);
            int foreColor = casing == null ? SpentCasingDefinition.COLOR_CASE_BRASS : casing.color(1);
            poseStack.pushPose();
            LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Shell"));
            ObjWeaponModels.renderPart(model, "Shell", ObjEffectModels.CASINGS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay, (shellColor >>> 16) & 255, (shellColor >>> 8) & 255, shellColor & 255, 255);
            ObjWeaponModels.renderPart(model, "ShellFore", ObjEffectModels.CASINGS_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay, (foreColor >>> 16) & 255, (foreColor >>> 8) & 255, foreColor & 255, 255);
            poseStack.popPose();
        }
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderCongolakeEffects(gun, LegacyHbmAnimations.getRelevantTransformation("Gun"),
                    poseStack, buffer);
        }
    }

    private static void renderMk108FirstPerson(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyHbmAnimations.Animation animation = LegacyHbmAnimations.getRelevantAnim();
        boolean doesYeet = animation != null && animation.animation().getBus("GRENH1") != null;
        boolean doesCycle = animation != null && animation.animation().getBus("CYCLE") != null;
        boolean reloading = animation != null && animation.animation().getBus("BELT") != null;
        boolean useShellCount = animation != null && animation.animation().getBus("SHELLS") != null;
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] cycle = LegacyHbmAnimations.getRelevantTransformation("CYCLE");
        double[] barrel = LegacyHbmAnimations.getRelevantTransformation("BARREL");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] lid = LegacyHbmAnimations.getRelevantTransformation("LID");
        double[] belt = LegacyHbmAnimations.getRelevantTransformation("BELT");
        double[] drum = LegacyHbmAnimations.getRelevantTransformation("DRUM");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] shellCount = LegacyHbmAnimations.getRelevantTransformation("SHELLS");

        if (doesYeet) {
            for (int i = 1; i <= 3; i++) {
                double[] horizontal = LegacyHbmAnimations.getRelevantTransformation("GRENH" + i);
                if (horizontal[0] <= -4.0D) continue;
                double[] vertical = LegacyHbmAnimations.getRelevantTransformation("GRENV" + i);
                double[] spin = LegacyHbmAnimations.getRelevantTransformation("GRENS" + i);
                poseStack.pushPose();
                poseStack.translate(horizontal[0], vertical[1], 0.0D);
                poseStack.translate(0.0D, 0.0D, -2.3125D);
                LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, -((float) spin[0]));
                poseStack.translate(0.0D, 0.0D, 2.3125D);
                ObjWeaponModels.renderPart(model, "Grenade", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
        poseStack.translate(0.0D, -1.0D, -8.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 8.0D);
        poseStack.translate(0.0D, 1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, -1.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose(); poseStack.translate(0.0D, 0.0D, barrel[2] * 2.0D);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        poseStack.pushPose(); poseStack.translate(0.0D, 0.6875D, -1.0D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) lid[0]); poseStack.translate(0.0D, -0.6875D, 1.0D);
        ObjWeaponModels.renderPart(model, "Lid", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(drum[0], drum[1], drum[2]);
        ObjWeaponModels.renderPart(model, "Drum", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        double[][] shells = mk108ShellPositions(reloading ? belt[0] : 1.0D);
        int shellAmount = useShellCount ? (int) shellCount[0] : primaryMagazineAmount(stack);
        double cycleProgress = doesCycle ? cycle[0] : 1.0D;
        for (int i = 0; i < shells.length - 1; i++) {
            double[] a = shells[i], b = shells[i + 1];
            renderMk108Shell(model, spec.textureLocation(), Mth.lerp(cycleProgress, a[0], b[0]), Mth.lerp(cycleProgress, a[1], b[1]), Mth.lerp(cycleProgress, a[2], b[2]), shells.length - i < shellAmount + 2, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderMk108Effects(gun, poseStack, buffer);
        }
    }

    private static void renderMk108Shell(LegacyWavefrontModel model, ResourceLocation texture, double x, double y,
            double rot, boolean shell, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) rot);
        ObjWeaponModels.renderPart(model, "Belt", texture, poseStack, buffer, packedLight, packedOverlay);
        if (shell) {
            ObjWeaponModels.renderPart(model, "Grenade", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderSexy(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            renderSexyFirstPerson(stack, model, spec, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
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
        int shellAmount = Integer.MAX_VALUE;
        for (int i = 0; i < shells.length; i++) {
            boolean shell = shells.length + 1 - i < shellAmount + 2;
            renderSexyShell(model, spec.textureLocation(), shells[i][0], shells[i][1], shells[i][2], shell,
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    /** Exact ItemRenderSexy first-person body, inspection-bottle, and generated belt hierarchy. */
    private static void renderSexyFirstPerson(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyHbmAnimations.Animation animation = LegacyHbmAnimations.getRelevantAnim();
        boolean doesCycle = animation != null && animation.animation().getBus("CYCLE") != null;
        boolean reloading = animation != null && animation.animation().getBus("BELT") != null;
        boolean useShellCount = animation != null && animation.animation().getBus("SHELLS") != null;
        boolean girlDinner = animation != null && animation.animation().getBus("BOTTLE") != null;
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lower = LegacyHbmAnimations.getRelevantTransformation("LOWER");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] cycle = LegacyHbmAnimations.getRelevantTransformation("CYCLE");
        double[] barrel = LegacyHbmAnimations.getRelevantTransformation("BARREL");
        double[] hood = LegacyHbmAnimations.getRelevantTransformation("HOOD");
        double[] lever = LegacyHbmAnimations.getRelevantTransformation("LEVER");
        double[] belt = LegacyHbmAnimations.getRelevantTransformation("BELT");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] magRot = LegacyHbmAnimations.getRelevantTransformation("MAGROT");
        double[] shellCount = LegacyHbmAnimations.getRelevantTransformation("SHELLS");
        double[] bottle = LegacyHbmAnimations.getRelevantTransformation("BOTTLE");
        double[] sip = LegacyHbmAnimations.getRelevantTransformation("SIP");

        if (girlDinner) {
            poseStack.pushPose();
            poseStack.translate(bottle[0], bottle[1], bottle[2]);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) sip[0]);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            LegacyPoseRotations.rotateXDegrees(poseStack, -15.0F);
            poseStack.translate(0.0D, -2.0D, 0.0D);
            poseStack.scale(1.5F, 1.5F, 1.5F);
            ObjWeaponModels.WHISKEY.renderAll(ObjWeaponModels.WHISKEY_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
        }

        poseStack.translate(0.0D, -1.0D, -8.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 8.0D);
        poseStack.translate(0.0D, 0.0D, -6.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lower[0]);
        poseStack.translate(0.0D, 0.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, barrel[2]);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -0.375D);
        poseStack.scale(1.0F, 1.0F, (float) (1.0D + 0.457247371D * barrel[2]));
        poseStack.translate(0.0D, 0.0D, 0.375D);
        ObjWeaponModels.renderPart(model, "RecoilSpring", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.4375D, -2.875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) hood[0]);
        poseStack.translate(0.0D, -0.4375D, 2.875D);
        ObjWeaponModels.renderPart(model, "Hood", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.46875D, -6.875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (lever[2] * 60.0D));
        poseStack.translate(0.0D, -0.46875D, 6.875D);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -6.75D);
        poseStack.scale(1.0F, 1.0F, (float) (1.0D - lever[2] * .25D));
        poseStack.translate(0.0D, 0.0D, 6.75D);
        ObjWeaponModels.renderPart(model, "LockSpring", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) magRot[2]);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        renderSexyBelt(stack, model, spec.textureLocation(), reloading ? belt[0] : 1.0D,
                doesCycle ? cycle[0] : 1.0D, useShellCount ? (int) shellCount[0] : primaryMagazineAmount(stack),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderSexyEffects(gun, poseStack, buffer);
        }
    }

    private static void renderSexyBelt(ItemStack stack, LegacyWavefrontModel model, ResourceLocation texture,
            double reloadProgress, double cycleProgress, int shellAmount, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] loaded = { 0.0D, 0.0D, 20.0D, 20.0D, 50.0D, 60.0D, 70.0D };
        double[] unloaded = { 0.0D, -10.0D, -50.0D, -60.0D, -60.0D, 0.0D, 0.0D };
        double[][] shells = new double[loaded.length][3];
        double x = .0625D * 17.0D;
        double y = .0625D * -26.0D;
        double angle = 0.0D;
        double vectorX = 0.0D;
        double vectorY = .4375D;
        for (int i = 0; i < shells.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90.0D;
            double delta = Mth.lerp(reloadProgress, unloaded[i], loaded[i]);
            angle += delta;
            double radians = Math.toRadians(-delta);
            double rotatedX = vectorX * Math.cos(radians) - vectorY * Math.sin(radians);
            vectorY = vectorX * Math.sin(radians) + vectorY * Math.cos(radians);
            vectorX = rotatedX;
            x += vectorX;
            y += vectorY;
        }
        for (int i = 0; i < shells.length - 1; i++) {
            double[] previous = shells[i];
            double[] next = shells[i + 1];
            boolean shell = shells.length - i < shellAmount + 2;
            renderSexyShell(model, texture, Mth.lerp(cycleProgress, previous[0], next[0]),
                    Mth.lerp(cycleProgress, previous[1], next[1]), Mth.lerp(cycleProgress, previous[2], next[2]),
                    shell, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSexyShell(LegacyWavefrontModel model, ResourceLocation texture, double x, double y,
            double rot, boolean shell, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, 0.375D + y, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) rot);
        poseStack.translate(0.0D, -0.375D, 0.0D);
        ObjWeaponModels.renderPart(model, "Belt", texture, poseStack, buffer, packedLight, packedOverlay);
        if (shell) {
            ObjWeaponModels.renderPart(model, "Shell", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    /** Exact ItemRenderMAS36 first-person body, reload-clip, and clipped-bullet-strip hierarchy. */
    private static void renderMas36(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
        boolean bayonet = hasUpgrade(stack, SednaWeaponModEvaluator.ID_MAS_BAYONET);
        if (!displayContext.firstPerson()) {
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
                if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                        && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                    poseStack.translate(0.0D, -1.0D, -6.0D);
                }
                ObjWeaponModels.renderPart(model, "Bayonet", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            return;
        }

        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] stock = LegacyHbmAnimations.getRelevantTransformation("STOCK");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] boltTurn = LegacyHbmAnimations.getRelevantTransformation("BOLT_TURN");
        double[] boltPull = LegacyHbmAnimations.getRelevantTransformation("BOLT_PULL");
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET");
        double[] showClip = LegacyHbmAnimations.getRelevantTransformation("SHOW_CLIP");
        double[] clip = LegacyHbmAnimations.getRelevantTransformation("CLIP");
        double[] bullets = LegacyHbmAnimations.getRelevantTransformation("BULLETS");
        double[] stab = LegacyHbmAnimations.getRelevantTransformation("STAB");
        poseStack.translate(0.0D, -3.0D, -3.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 3.0D, 3.0D);
        poseStack.translate(stab[0], stab[1], stab[2]);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (bayonet) {
            ObjWeaponModels.renderPart(model, "Bayonet", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, .3125D, -2.125D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) stock[0]);
        poseStack.translate(0.0D, -.3125D, 2.125D);
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, .0625D * 18.5D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) boltTurn[2]);
        poseStack.translate(0.0D, -.0625D * 18.5D, 0.0D);
        poseStack.translate(0.0D, 0.0D, boltPull[2]);
        ObjWeaponModels.renderPart(model, "Bolt", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(bullet[0], bullet[1], bullet[2]);
        ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (showClip[0] != 0.0D) {
            poseStack.pushPose();
            poseStack.translate(clip[0], clip[1], clip[2]);
            ObjWeaponModels.renderPart(model, "Clip", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(bullets[0], bullets[1], bullets[2]);
            if (bullets[0] == 0.0D) {
                model.renderPartClipped("Bullets", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                        255, 255, 255, 255, false, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                        LegacyWavefrontModel.UvTransform.DEFAULT, 0.0D, 1.0D, 0.0D, bullets[1] - .5D);
            } else {
                ObjWeaponModels.renderPart(model, "Bullets", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            poseStack.popPose();
        }
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderMas36Effects(gun, poseStack, buffer);
        }
    }

    private static void renderBolter(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        if (displayContext.firstPerson()) {
            poseStack.pushPose();
            if (legacyBusActive()) {
                double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
                double[] tilt = LegacyHbmAnimations.getRelevantTransformation("TILT");
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[0] * 5.0D));
                poseStack.translate(0.0D, 0.0D, recoil[0]);
                poseStack.translate(0.0D, tilt[0], 3.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) (tilt[0] * 35.0D));
                poseStack.translate(0.0D, 0.0D, -3.0D);
            }
            ObjWeaponModels.renderPart(model, "Body", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.pushPose();
            if (legacyBusActive()) {
                double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
                poseStack.translate(0.0D, 0.0D, 5.0D);
                double angle = -mag[0] * 60.0D * (mag[2] == 1.0D ? 2.5D : 1.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) angle);
                poseStack.translate(0.0D, 0.0D, -5.0D);
            }
            ObjWeaponModels.renderPart(model, "Mag", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            boolean magazineEjected = legacyBusActive()
                    && LegacyHbmAnimations.getRelevantTransformation("MAG")[2] == 1.0D;
            if (!magazineEjected) {
                ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            poseStack.popPose();
            renderBolterAmmoText(stack, poseStack, buffer);
            poseStack.popPose();
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
        LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
        renderLegacyModelText(font, text, 0xFFFF0000, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderStg77(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            renderStg77Static(model, spec, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
        if (HbmClientConfig.legacyGunAnimations()) {
            renderStg77Legacy(model, spec, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderStg77Json(model, spec, poseStack, buffer, packedLight, packedOverlay);
        }
        if (stack.getItem() instanceof SednaGunItem gun) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            ClientSednaGunEffects.renderStg77Effects(gun, equip[0], lift[0], recoil[2], poseStack, buffer);
        }
    }

    private static void renderG3FirstPerson(ItemStack stack, LegacyWavefrontModel model, ResourceLocation texture,
            boolean stock, boolean silenced, boolean scoped, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
        double[] speen = LegacyHbmAnimations.getRelevantTransformation("SPEEN");
        double[] bolt = LegacyHbmAnimations.getRelevantTransformation("BOLT");
        double[] plug = LegacyHbmAnimations.getRelevantTransformation("PLUG");
        double[] handle = LegacyHbmAnimations.getRelevantTransformation("HANDLE");
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET");
        poseStack.translate(0.0D, -2.0D, -6.0D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]); poseStack.translate(0.0D, 2.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, -4.0D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]); poseStack.translate(0.0D, 0.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Rifle", texture, poseStack, buffer, packedLight, packedOverlay);
        if (stock) ObjWeaponModels.renderPart(model, "Stock", texture, poseStack, buffer, packedLight, packedOverlay);
        if (!silenced) ObjWeaponModels.renderPart(model, "Flash_Hider", texture, poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Trigger", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose(); poseStack.translate(mag[0], mag[1], mag[2]); poseStack.translate(0.0D, -1.75D, -0.5D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) speen[2]); LegacyPoseRotations.rotateYDegrees(poseStack, (float) speen[1]); poseStack.translate(0.0D, 1.75D, 0.5D);
        ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay);
        if (bullet[0] == 0.0D) ObjWeaponModels.renderPart(model, "Bullet", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        poseStack.pushPose(); poseStack.translate(0.0D, 0.0D, bolt[2]); ObjWeaponModels.renderPart(model, "Guide_And_Bolt", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        poseStack.pushPose(); poseStack.translate(0.0D, 0.625D, plug[2]); LegacyPoseRotations.rotateZDegrees(poseStack, (float) handle[2]); poseStack.translate(0.0D, -0.625D, 0.0D); ObjWeaponModels.renderPart(model, "Plug", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 0.625D, 5.25D); LegacyPoseRotations.rotateZDegrees(poseStack, 22.5F); LegacyPoseRotations.rotateYDegrees(poseStack, (float) handle[1]); LegacyPoseRotations.rotateZDegrees(poseStack, -22.5F); poseStack.translate(0.0D, -0.625D, -5.25D); ObjWeaponModels.renderPart(model, "Handle", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        int mode = stack.getTag() == null ? 0 : stack.getTag().getInt("mode_0");
        poseStack.pushPose(); poseStack.translate(0.0D, -0.875D, -3.5D); LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-30.0D * (1.0D - mode))); poseStack.translate(0.0D, 0.875D, 3.5D); ObjWeaponModels.renderPart(model, "Selector", texture, poseStack, buffer, packedLight, packedOverlay); poseStack.popPose();
        if (silenced) ObjWeaponModels.renderPart(model, "Silencer", G3_ATTACHMENTS_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        if (scoped) ObjWeaponModels.renderPart(model, "Scope", G3_ATTACHMENTS_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderG3Effects(gun, silenced, poseStack, buffer);
        }
    }

    private static void renderStg77Static(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
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

    /** Default 1.7.10 route: the authored stg77.json hierarchy through LegacyBusAnimationLoader. */
    private static void renderStg77Json(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 4.0D);
        LegacyHbmAnimations.applyRelevantTransformation("Gun", poseStack);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        renderStg77JsonChild(model, spec, poseStack, buffer, packedLight, packedOverlay, "Magazine");
        renderStg77JsonChild(model, spec, poseStack, buffer, packedLight, packedOverlay, "Lever");
        renderStg77JsonChild(model, spec, poseStack, buffer, packedLight, packedOverlay, "Breech");
        renderStg77JsonChild(model, spec, poseStack, buffer, packedLight, packedOverlay, "Handle");
        renderStg77JsonChild(model, spec, poseStack, buffer, packedLight, packedOverlay, "Safety");
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 4.0D);
        LegacyHbmAnimations.applyRelevantTransformation("Gun", poseStack);
        LegacyHbmAnimations.applyRelevantTransformation("Barrel", poseStack);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderStg77JsonChild(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, String part) {
        poseStack.pushPose();
        LegacyHbmAnimations.applyRelevantTransformation(part, poseStack);
        ObjWeaponModels.renderPart(model, part, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /** Optional ClientConfig.GUN_ANIMS_LEGACY route, copied from ItemRenderSTG77's matrix order. */
    private static void renderStg77Legacy(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] bolt = LegacyHbmAnimations.getRelevantTransformation("BOLT");
        double[] handle = LegacyHbmAnimations.getRelevantTransformation("HANDLE");
        double[] safety = LegacyHbmAnimations.getRelevantTransformation("SAFETY");
        double[] inspectGun = LegacyHbmAnimations.getRelevantTransformation("INSPECT_GUN");
        double[] inspectBarrel = LegacyHbmAnimations.getRelevantTransformation("INSPECT_BARREL");
        double[] inspectMove = LegacyHbmAnimations.getRelevantTransformation("INSPECT_MOVE");
        double[] inspectLever = LegacyHbmAnimations.getRelevantTransformation("INSPECT_LEVER");

        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 0.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) inspectGun[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) inspectGun[0]);
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) inspectLever[2]);
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, bolt[2]);
        ObjWeaponModels.renderPart(model, "Breech", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.125D, 0.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) handle[2]);
        poseStack.translate(-0.125D, 0.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Handle", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(safety[0], 0.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Safety", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(inspectMove[0], inspectMove[1], inspectMove[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) inspectBarrel[0]);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) inspectBarrel[2]);
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderLaserPistol(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
            double[] jolt = LegacyHbmAnimations.getRelevantTransformation("JOLT");
            double[] swirl = LegacyHbmAnimations.getRelevantTransformation("SWIRL");
            poseStack.translate(0.0D, -1.0D, -6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
            poseStack.translate(0.0D, 1.0D, 6.0D);
            poseStack.translate(0.0D, 2.0D, -2.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
            poseStack.translate(0.0D, -2.0D, 2.0D);
            poseStack.translate(0.0D, -1.0D, -1.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) swirl[0]);
            poseStack.translate(0.0D, 1.0D, 1.0D);
            poseStack.translate(recoil[0], recoil[1], recoil[2]);
            poseStack.translate(jolt[0], jolt[1], jolt[2]);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] latch = LegacyHbmAnimations.getRelevantTransformation("LATCH");
            poseStack.translate(1.125D, 0.0D, -1.9125D);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) latch[1]);
            poseStack.translate(-1.125D, 0.0D, 1.9125D);
        }
        ObjWeaponModels.renderPart(model, "Latch", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (spec.visibleParts().contains("Capacitors")) {
            ObjWeaponModels.renderPart(model, "Capacitors", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (spec.visibleParts().contains("Tape")) {
            ObjWeaponModels.renderPart(model, "Tape", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (displayContext.firstPerson()) {
            poseStack.pushPose();
            if (legacyBusActive()) {
                double[] latch = LegacyHbmAnimations.getRelevantTransformation("LATCH");
                double[] battery = LegacyHbmAnimations.getRelevantTransformation("BATTERY");
                poseStack.translate(1.125D, 0.0D, -1.9125D);
                LegacyPoseRotations.rotateYDegrees(poseStack, (float) latch[1]);
                poseStack.translate(-1.125D, 0.0D, 1.9125D);
                poseStack.translate(battery[0], battery[1], battery[2]);
            }
            ObjWeaponModels.renderPart(model, "Battery", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
            if (stack.getItem() instanceof SednaGunItem gun) {
                ClientSednaGunEffects.renderLaserPistolEffects(gun,
                        "gun_laser_pistol_morning_glory".equals(currentLegacyName(stack)), poseStack, buffer);
            }
        }
        poseStack.popPose();
    }

    private static void renderPanzerschreck(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            applyPanzerschreckBodyAnimation(poseStack);
        }
        ObjWeaponModels.renderPart(model, "Tube", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_NO_SHIELD)) {
            ObjWeaponModels.renderPart(model, "Shield", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (displayContext.firstPerson()) {
            poseStack.pushPose();
            if (legacyBusActive()) {
                double[] rocket = LegacyHbmAnimations.getRelevantTransformation("ROCKET");
                poseStack.translate(rocket[0], rocket[1], rocket[2]);
            }
            ObjWeaponModels.renderPart(model, "Rocket", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
            if (stack.getItem() instanceof SednaGunItem gun) {
                ClientSednaGunEffects.renderPanzerschreckFlash(gun, poseStack, buffer);
            }
        }
        poseStack.popPose();
    }

    private static void renderStinger(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            if (LegacySednaAimProgress.settledFullyAimed()) {
                return;
            }
            poseStack.pushPose();
            if (legacyBusActive()) {
                applyPanzerschreckBodyAnimation(poseStack);
            }
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();

            poseStack.pushPose();
            if (legacyBusActive()) {
                double[] rocket = LegacyHbmAnimations.getRelevantTransformation("ROCKET");
                poseStack.translate(rocket[0], rocket[1] + 3.5D, rocket[2] - 3.0D);
            } else {
                poseStack.translate(0.0D, 3.5D, -3.0D);
            }
            ObjWeaponModels.renderPart(extraModel("panzerschreck", "panzerschreck", "panzerschreck"),
                    "Rocket", PANZERSCHRECK_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
            renderStingerNotAccurateText(poseStack, buffer);
            poseStack.popPose();
            if (stack.getItem() instanceof SednaGunItem gun) {
                ClientSednaGunEffects.renderStingerFlash(gun, poseStack, buffer);
            }
            poseStack.popPose();
        } else {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void applyPanzerschreckBodyAnimation(PoseStack poseStack) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] reload = LegacyHbmAnimations.getRelevantTransformation("RELOAD");
        poseStack.translate(0.0D, -1.0D, -1.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 1.0D);
        poseStack.translate(0.0D, -4.0D, -3.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) reload[0]);
        poseStack.translate(0.0D, 4.0D, 3.0D);
    }

    private static void renderStingerNotAccurateText(PoseStack poseStack, MultiBufferSource buffer) {
        String text = "Not accurate";
        Font font = Minecraft.getInstance().font;
        float scale = 0.04F;
        poseStack.pushPose();
        poseStack.translate(0.025D, -0.5D, (font.width(text) / 2.0D) * scale - 3.0D);
        poseStack.scale(scale, -scale, scale);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, -45.0F);
        renderLegacyModelText(font, text, 0xFFFF0000, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderQuadro(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            return;
        }
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
        double[] reloadPush = LegacyHbmAnimations.getRelevantTransformation("RELOAD_PUSH");
        double[] reloadRotate = LegacyHbmAnimations.getRelevantTransformation("RELOAD_ROTATE");

        poseStack.translate(0.0D, -1.0D, -1.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 1.0D, 1.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        poseStack.translate(0.0D, -1.0D, -1.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) reloadRotate[2]);
        poseStack.translate(0.0D, 1.0D, 1.0D);
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.translate(0.0D, 3.0D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (reloadPush[1] * 30.0D));
        poseStack.translate(0.0D, -3.0D, 0.0D);
        poseStack.translate(0.0D, 0.0D, reloadPush[0] * 3.0D);
        ObjWeaponModels.renderPart(model, "Rockets", QUADRO_ROCKET_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderQuadroFlash(gun, poseStack, buffer);
        }
        renderQuadroAimLabel(poseStack, buffer);
    }

    private static void renderQuadroAimLabel(PoseStack poseStack, MultiBufferSource buffer) {
        if (!LegacySednaAimProgress.settledFullyAimed()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String label = ">> <<";
        float scale = 0.04F;
        float rotation = -180.0F - (System.currentTimeMillis() / 2L) % 360L;
        poseStack.pushPose();
        poseStack.translate(-0.375D, 2.25D, 0.875D);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
        poseStack.translate(-(font.width(label) / 2.0D) * scale, 0.0D, 0.0D);
        poseStack.scale(scale, -scale, scale);
        renderLegacyModelText(font, label, 0xFF00FFFF, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderMissileLauncher(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            ObjWeaponModels.renderPart(model, "Front", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            if (isMagazineLoaded(stack)) {
                ObjWeaponModels.renderPart(model, "Missile", spec.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay);
            }
            return;
        }

        MissileLauncherAnimationPose animation = missileLauncherAnimationPose(stack);
        poseStack.pushPose();
        poseStack.translate(0.0D, -2.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) animation.equipX());
        poseStack.translate(0.0D, 2.0D, 2.0D);
        ObjWeaponModels.renderPart(model, "Launcher", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 1.6875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) animation.openX());
        poseStack.translate(0.0D, -0.25D, -1.6875D);
        ObjWeaponModels.renderPart(model, "Front", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, animation.barrelZ());
        ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(animation.missileX(), animation.missileY(), animation.missileZ());
        ObjWeaponModels.renderPart(model, "Missile", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderMissileLauncherFlash(gun, poseStack, buffer);
        }
        poseStack.popPose();

        renderMissileLauncherAutoLabel(stack, poseStack, buffer);
        poseStack.popPose();
    }

    private static MissileLauncherAnimationPose missileLauncherAnimationPose(ItemStack stack) {
        if (legacyBusActive()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] barrel = LegacyHbmAnimations.getRelevantTransformation("BARREL");
            double[] open = LegacyHbmAnimations.getRelevantTransformation("OPEN");
            double[] missile = LegacyHbmAnimations.getRelevantTransformation("MISSILE");
            return new MissileLauncherAnimationPose(equip[0], open[0], barrel[2], missile[0], missile[1], missile[2]);
        }

        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_EQUIP) {
            return new MissileLauncherAnimationPose(
                    millis <= 1000.0D ? lerp(60.0D, 0.0D, sinDown(millis / 1000.0D)) : 0.0D,
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (animation == LEGACY_ANIM_RELOAD) {
            return new MissileLauncherAnimationPose(missileLauncherReloadEquipX(millis), missileLauncherReloadOpenX(millis),
                    missileLauncherReloadBarrelZ(millis), missileLauncherReloadMissileX(millis), 0.0D,
                    missileLauncherReloadMissileZ(millis));
        }
        if (animation == LEGACY_ANIM_JAMMED || animation == LEGACY_ANIM_INSPECT) {
            return new MissileLauncherAnimationPose(missileLauncherJammedEquipX(millis), missileLauncherJammedOpenX(millis),
                    missileLauncherJammedBarrelZ(millis), 0.0D, 0.0D, 0.0D);
        }
        return MissileLauncherAnimationPose.IDENTITY;
    }

    private static double missileLauncherReloadBarrelZ(double millis) {
        if (millis <= 150.0D) {
            return lerp(0.0D, 1.5D, millis / 150.0D);
        }
        if (millis <= 2250.0D) {
            return 1.5D;
        }
        if (millis <= 2400.0D) {
            return lerp(1.5D, 0.0D, (millis - 2250.0D) / 150.0D);
        }
        return 0.0D;
    }

    private static double missileLauncherReloadOpenX(double millis) {
        if (millis <= 250.0D) {
            return 0.0D;
        }
        if (millis <= 750.0D) {
            return lerp(0.0D, 90.0D, sinFull((millis - 250.0D) / 500.0D));
        }
        if (millis <= 1750.0D) {
            return 90.0D;
        }
        if (millis <= 2250.0D) {
            return lerp(90.0D, 0.0D, sinFull((millis - 1750.0D) / 500.0D));
        }
        return 0.0D;
    }

    private static double missileLauncherReloadEquipX(double millis) {
        if (millis <= 2250.0D) {
            return 0.0D;
        }
        if (millis <= 2400.0D) {
            return lerp(0.0D, -1.0D, sinDown((millis - 2250.0D) / 150.0D));
        }
        if (millis <= 2550.0D) {
            return lerp(-1.0D, 0.0D, sinUp((millis - 2400.0D) / 150.0D));
        }
        return 0.0D;
    }

    private static double missileLauncherReloadMissileX(double millis) {
        if (millis <= 750.0D) {
            return -10.0D;
        }
        if (millis <= 1100.0D) {
            return lerp(3.0D, 0.0D, sinFull((millis - 750.0D) / 350.0D));
        }
        return 0.0D;
    }

    private static double missileLauncherReloadMissileZ(double millis) {
        if (millis <= 750.0D) {
            return 0.0D;
        }
        if (millis <= 1100.0D) {
            return lerp(2.0D, -6.0D, sinFull((millis - 750.0D) / 350.0D));
        }
        if (millis <= 1450.0D) {
            return lerp(-6.0D, 0.0D, sinUp((millis - 1100.0D) / 350.0D));
        }
        return 0.0D;
    }

    private static double missileLauncherJammedBarrelZ(double millis) {
        if (millis <= 150.0D) {
            return lerp(0.0D, 1.5D, millis / 150.0D);
        }
        if (millis <= 1500.0D) {
            return 1.5D;
        }
        if (millis <= 1650.0D) {
            return lerp(1.5D, 0.0D, (millis - 1500.0D) / 150.0D);
        }
        return 0.0D;
    }

    private static double missileLauncherJammedOpenX(double millis) {
        if (millis <= 250.0D) {
            return 0.0D;
        }
        if (millis <= 750.0D) {
            return lerp(0.0D, 90.0D, sinFull((millis - 250.0D) / 500.0D));
        }
        if (millis <= 1000.0D) {
            return 90.0D;
        }
        if (millis <= 1500.0D) {
            return lerp(90.0D, 0.0D, sinFull((millis - 1000.0D) / 500.0D));
        }
        return 0.0D;
    }

    private static double missileLauncherJammedEquipX(double millis) {
        if (millis <= 1500.0D) {
            return 0.0D;
        }
        if (millis <= 1650.0D) {
            return lerp(0.0D, -1.0D, sinDown((millis - 1500.0D) / 150.0D));
        }
        if (millis <= 1800.0D) {
            return lerp(-1.0D, 0.0D, sinUp((millis - 1650.0D) / 150.0D));
        }
        return 0.0D;
    }

    private static void renderMissileLauncherAutoLabel(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer) {
        if (!(stack.getItem() instanceof SednaGunItem) || !LegacySednaAimProgress.settledFullyAimed()
                || Minecraft.getInstance().player == null) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        float scale = 0.04F;
        float variance = 0.7F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.3F;
        int color = 0xFF000000 | (Math.round(variance * 255.0F) << 16);
        poseStack.pushPose();
        poseStack.translate(0.9375D, 2.25D, -0.5625D + (font.width("AUTO") / 2.0D) * scale);
        poseStack.scale(scale, -scale, scale);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderLegacyModelText(font, "AUTO", color, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderLasrifle(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean shotgun = SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_SHOTGUN);
        boolean capacitor = SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_CAPACITOR);
        boolean scope = !SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_LAS_AUTO);

        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            poseStack.translate(0.0D, -1.0D, -6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
            poseStack.translate(0.0D, 1.0D, 6.0D);
            poseStack.translate(recoil[0], recoil[1], recoil[2]);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (scope) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] lever = LegacyHbmAnimations.getRelevantTransformation("LEVER");
            poseStack.translate(0.0D, -0.375D, 2.375D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) lever[0]);
            poseStack.translate(0.0D, 0.375D, -2.375D);
        }
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG");
            poseStack.translate(mag[0], mag[1], mag[2]);
        }
        ObjWeaponModels.renderPart(model, "Battery", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
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
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderLasrifleEffects(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void renderAberrator(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model, RenderSpec spec,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext.firstPerson()) {
            renderAberratorFirstPersonParts(stack, model, spec, 1, 0, poseStack, buffer, packedLight, packedOverlay);
            return;
        }
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

    /** Exact ItemRenderAberrator/ItemRenderEOTT first-person hierarchy; side is 1 for the single gun. */
    private static void renderAberratorFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec, int side,
            int animationIndex, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", animationIndex);
        double[] rise = LegacyHbmAnimations.getRelevantTransformation("RISE", animationIndex);
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", animationIndex);
        double[] slide = LegacyHbmAnimations.getRelevantTransformation("SLIDE", animationIndex);
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET", animationIndex);
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER", animationIndex);
        double[] roll = LegacyHbmAnimations.getRelevantTransformation("ROLL", animationIndex);
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG", animationIndex);
        double[] magRoll = LegacyHbmAnimations.getRelevantTransformation("MAGROLL", animationIndex);
        double[] sight = LegacyHbmAnimations.getRelevantTransformation("SIGHT", animationIndex);

        poseStack.translate(0.0D, rise[1], 0.0D);
        poseStack.translate(0.0D, 1.0D, -2.25D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, -1.0D, 2.25D);
        poseStack.translate(0.0D, -1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) recoil[0]);
        poseStack.translate(0.0D, 1.0D, 4.0D);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (roll[2] * side));
        poseStack.translate(0.0D, -1.0D, 0.0D);

        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.4375D, -1.9375D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) sight[0]);
        poseStack.translate(0.0D, -2.4375D, 1.9375D);
        ObjWeaponModels.renderPart(model, "Sight", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(mag[0] * side, mag[1], mag[2]);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (magRoll[2] * side));
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjWeaponModels.renderPart(model, "Magazine", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(bullet[0], bullet[1], bullet[2]);
        ObjWeaponModels.renderPart(model, "Bullet", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, slide[2]);
        ObjWeaponModels.renderPart(model, "Slide", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.25D, -3.625D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-45.0D + hammer[0]));
        poseStack.translate(0.0D, -1.25D, 3.625D);
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderAberratorEffects(gun, animationIndex, side, recoil[0], roll[2], poseStack, buffer);
        }
    }

    private static void renderLag(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        poseStack.pushPose();
        if (animated) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] addTrans = LegacyHbmAnimations.getRelevantTransformation("ADD_TRANS");
            double[] addRot = LegacyHbmAnimations.getRelevantTransformation("ADD_ROT");
            poseStack.translate(4.0D, -4.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) -equip[0]);
            poseStack.translate(-4.0D, 4.0D, 0.0D);
            poseStack.translate(addTrans[0], addTrans[1], addTrans[2]);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) addRot[2]);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) addRot[1]);
        }
        renderLagPart(model, spec, poseStack, buffer, packedLight, packedOverlay, "Grip", animated);
        renderLagPart(model, spec, poseStack, buffer, packedLight, packedOverlay, "Slide", animated);
        renderLagPart(model, spec, poseStack, buffer, packedLight, packedOverlay, "Hammer", animated);
        if (displayContext.firstPerson()) {
            if (primaryMagazineAmount(stack) > 0) {
                renderLagPart(model, spec, poseStack, buffer, packedLight, packedOverlay, "Bullet", animated);
            }
            renderLagPart(model, spec, poseStack, buffer, packedLight, packedOverlay, "Magazine", animated);
            if (stack.getItem() instanceof SednaGunItem gun) {
                ClientSednaGunEffects.renderLagEffects(gun, poseStack, buffer);
            }
        }
        poseStack.popPose();
    }

    /** ItemRenderLAG applies each JSON bus directly; the source JSON's offsets preserve part pivots. */
    private static void renderLagPart(LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, String part, boolean animated) {
        poseStack.pushPose();
        if (animated) {
            LegacyHbmAnimations.applyRelevantTransformation(part, poseStack);
        }
        ObjWeaponModels.renderPart(model, part, spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderM2(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double equip = m2EquipX(stack);
            poseStack.translate(0.0D, 1.0D, -2.25D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip);
            poseStack.translate(0.0D, -1.0D, 2.25D);
            poseStack.translate(0.0D, 0.0D, m2RecoilZ(stack));
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderM2Effects(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static double m2EquipX(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("EQUIP")[0];
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_EQUIP) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        return millis <= 500.0D ? lerp(80.0D, 0.0D, sinFull(millis / 500.0D)) : 0.0D;
    }

    private static double m2RecoilZ(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("RECOIL")[2];
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_CYCLE) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis <= 25.0D) {
            return lerp(0.0D, -0.25D, millis / 25.0D);
        }
        if (millis <= 100.0D) {
            return lerp(-0.25D, 0.0D, (millis - 25.0D) / 75.0D);
        }
        return 0.0D;
    }

    private static void renderCoilgun(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        if (displayContext.firstPerson()) {
            double recoil = coilgunRecoil(stack);
            poseStack.translate(-1.5D - recoil * 0.5D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (recoil * 45.0D));
            poseStack.translate(1.5D, 0.0D, 0.0D);

            double reload = coilgunReload(stack);
            poseStack.translate(-2.5D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (reload * -45.0D));
            poseStack.translate(2.5D, 0.0D, 0.0D);
        }
        model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static double coilgunRecoil(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("RECOIL")[0];
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_CYCLE) {
            return 0.0D;
        }
        double target = isTeslaAiming(stack) ? 0.5D : 1.0D;
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis <= 100.0D) {
            return lerp(0.0D, target, millis / 100.0D);
        }
        if (millis <= 300.0D) {
            return lerp(target, 0.0D, (millis - 100.0D) / 200.0D);
        }
        return 0.0D;
    }

    private static double coilgunReload(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("RELOAD")[0];
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_EQUIP) {
            return millis <= 250.0D ? lerp(1.0D, 0.0D, millis / 250.0D) : 0.0D;
        }
        if (animation == LEGACY_ANIM_RELOAD) {
            if (millis <= 250.0D) {
                return lerp(0.0D, 1.0D, millis / 250.0D);
            }
            if (millis <= 750.0D) {
                return 1.0D;
            }
            if (millis <= 1_000.0D) {
                return lerp(1.0D, 0.0D, (millis - 750.0D) / 250.0D);
            }
        }
        return 0.0D;
    }

    private static void renderLegacyModelText(Font font, String text, int color, PoseStack poseStack,
            MultiBufferSource buffer) {
        font.drawInBatch(text, 0.0F, 0.0F, color, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    private static void renderChargeThrower(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        poseStack.pushPose();
        if (animated) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            double[] raise = LegacyHbmAnimations.getRelevantTransformation("RAISE");
            double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
            double[] roll = LegacyHbmAnimations.getRelevantTransformation("ROLL");
            poseStack.translate(0.0D, 0.0D, -7.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) -equip[0]);
            poseStack.translate(0.0D, 0.0D, 7.0D);
            poseStack.translate(0.0D, -7.0D, 4.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) raise[0]);
            poseStack.translate(0.0D, 7.0D, -4.0D);
            poseStack.translate(recoil[0], recoil[1], recoil[2]);
            poseStack.translate(0.0D, 0.0D, -2.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) turn[1]);
            poseStack.translate(0.0D, 0.0D, 2.0D);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) roll[2]);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE)) {
            ObjWeaponModels.renderPart(model, "Scope", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        String loadedType = primaryMagazineType(stack);
        boolean showAmmo = !loadedType.isBlank() && (!displayContext.firstPerson() || primaryMagazineAmount(stack) > 0
                || chargeThrowerReloading());
        if (showAmmo) {
            poseStack.pushPose();
            if (animated) {
                double[] ammo = LegacyHbmAnimations.getRelevantTransformation("AMMO");
                double[] twist = LegacyHbmAnimations.getRelevantTransformation("TWIST");
                poseStack.translate(ammo[0], ammo[1], ammo[2]);
                LegacyPoseRotations.rotateZDegrees(poseStack, (float) twist[2]);
            }
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
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static boolean chargeThrowerReloading() {
        LegacyHbmAnimations.Animation animation = LegacyHbmAnimations.getRelevantAnim();
        return animation != null && animation.animation().getBus("AMMO") != null;
    }

    private static void renderDoubleBarrel(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean sawedOff = isDoubleBarrelSawedOff(stack);
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        poseStack.pushPose();
        if (animated) {
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN");
            double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT");
            poseStack.translate(recoil[0] * 3.0D, recoil[1], recoil[2]);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 10.0D));
            poseStack.translate(0.0D, 0.0D, -4.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) -equip[0]);
            poseStack.translate(0.0D, 0.0D, 4.0D);
            poseStack.translate(0.0D, 0.0D, -4.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) turn[1]);
            poseStack.translate(0.0D, 0.0D, 4.0D);
            poseStack.translate(0.0D, 0.0D, -4.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) -lift[0]);
            poseStack.translate(0.0D, 0.0D, 4.0D);
        }
        ObjWeaponModels.renderPart(model, "Stock", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        if (animated) {
            double[] barrel = LegacyHbmAnimations.getRelevantTransformation("BARREL");
            poseStack.translate(0.0D, -0.4375D, -0.875D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) barrel[0]);
            poseStack.translate(0.0D, 0.4375D, 0.875D);
        }
        ObjWeaponModels.renderPart(model, "BarrelShort", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!sawedOff) {
            ObjWeaponModels.renderPart(model, "Barrel", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.pushPose();
        if (animated) {
            double[] buckle = LegacyHbmAnimations.getRelevantTransformation("BUCKLE");
            poseStack.translate(0.75D, 0.0D, -0.6875D);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) buckle[1]);
            poseStack.translate(-0.75D, 0.0D, 0.6875D);
        }
        ObjWeaponModels.renderPart(model, "Buckle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        if (animated) {
            double[] lever = LegacyHbmAnimations.getRelevantTransformation("LEVER");
            poseStack.translate(-0.3125D, 0.3125D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) lever[2]);
            poseStack.translate(0.3125D, -0.3125D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Lever", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (!animated || LegacyHbmAnimations.getRelevantTransformation("NO_AMMO")[0] == 0.0D) {
            poseStack.pushPose();
            if (animated) {
                double[] shells = LegacyHbmAnimations.getRelevantTransformation("SHELLS");
                double[] shellFlip = LegacyHbmAnimations.getRelevantTransformation("SHELL_FLIP");
                poseStack.translate(shells[0], shells[1], shells[2]);
                poseStack.translate(0.0D, 0.0D, -1.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) shellFlip[0]);
                poseStack.translate(0.0D, 0.0D, 1.0D);
            }
            ObjWeaponModels.renderPart(model, "Shells", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderDoubleBarrelEffects(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void renderHeavyRevolver(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        boolean scoped = isHeavyRevolverScoped(stack);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        poseStack.pushPose();
        double recoilZ = 0.0D;
        if (animated) {
            double[] spin = LegacyHbmAnimations.getRelevantTransformation("SPIN");
            double[] rotate = LegacyHbmAnimations.getRelevantTransformation("ROTATE");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            recoilZ = recoil[2];
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) spin[0]);
            poseStack.translate(6.0D, -3.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) rotate[0]);
            poseStack.translate(-6.0D, 3.0D, 0.0D);
            poseStack.translate(0.0D, 0.0D, recoil[2]);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (recoil[2] * 10.0D));
        }
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderHeavyRevolverSmoke(gun, recoilZ, poseStack, buffer);
        }
        if (animated) {
            double[] reloadLift = LegacyHbmAnimations.getRelevantTransformation("RELOAD_LIFT");
            double[] reloadJolt = LegacyHbmAnimations.getRelevantTransformation("RELOAD_JOLT");
            double[] reloadTilt = LegacyHbmAnimations.getRelevantTransformation("RELAOD_TILT");
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) reloadLift[0]);
            poseStack.translate(reloadJolt[0], 0.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) reloadTilt[0]);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.pushPose();
        if (animated) {
            double[] cylinderFlip = LegacyHbmAnimations.getRelevantTransformation("RELOAD_CYLINDER");
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) cylinderFlip[0]);
        }
        if (displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Pivot", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.translate(0.0D, 1.75D, 0.0D);
        if (animated) {
            double[] drum = LegacyHbmAnimations.getRelevantTransformation("DRUM");
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (drum[2] * -60.0D));
        }
        poseStack.translate(0.0D, -1.75D, 0.0D);
        ObjWeaponModels.renderPart(model, "Cylinder", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (animated) {
            double[] bullets = LegacyHbmAnimations.getRelevantTransformation("RELOAD_BULLETS");
            poseStack.translate(bullets[0], bullets[1], bullets[2]);
        }
        boolean hideBullets = animated && LegacyHbmAnimations.getRelevantTransformation("RELOAD_BULLETS_CON")[0] == 1.0D;
        if (!hideBullets) {
            ObjWeaponModels.renderPart(model, "Bullets", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Casings", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        if (!displayContext.firstPerson()) {
            ObjWeaponModels.renderPart(model, "Pivot", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.popPose();
        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(4.0D, 1.25D, 0.0D);
            double hammer = animated ? LegacyHbmAnimations.getRelevantTransformation("HAMMER")[2] : 0.0D;
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-30.0D + hammer * 30.0D));
            poseStack.translate(-4.0D, -1.25D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (scoped) {
            ObjWeaponModels.renderPart(model, "Scope", LILMAC_SCOPE_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
        }
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderHeavyRevolverFlash(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    /** Exact {@code ItemRenderDebug}: lilmac geometry, debug texture and its separate ROTATE rail. */
    private static void renderDebug(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        double recoilZ = 0.0D;
        if (animated) {
            double[] equipSpin = LegacyHbmAnimations.getRelevantTransformation("ROTATE");
            double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL");
            recoilZ = recoil[2];
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) equipSpin[0]);
            float aim = LegacySednaAimProgress.interpolated(Minecraft.getInstance().getFrameTime());
            poseStack.translate(-recoilZ * aim, 0.0D, recoilZ * (1.0D - aim));
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (recoilZ * 10.0D));
            if (stack.getItem() instanceof SednaGunItem gun) {
                poseStack.pushPose();
                poseStack.translate(-9.0D, 2.5D, 0.0D);
                LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-recoilZ * 10.0D));
                ClientSednaGunEffects.renderSmoke(gun, 0, 0.5D, poseStack, buffer);
                poseStack.popPose();
            }
            double[] reloadLift = LegacyHbmAnimations.getRelevantTransformation("RELOAD_LIFT");
            double[] reloadJolt = LegacyHbmAnimations.getRelevantTransformation("RELOAD_JOLT");
            double[] reloadTilt = LegacyHbmAnimations.getRelevantTransformation("RELAOD_TILT");
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) reloadLift[0]);
            poseStack.translate(reloadJolt[0], 0.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) reloadTilt[0]);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        if (animated) {
            LegacyPoseRotations.rotateXDegrees(poseStack,
                    (float) LegacyHbmAnimations.getRelevantTransformation("RELOAD_CYLINDER")[0]);
        }
        ObjWeaponModels.renderPart(model, "Pivot", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.75D, 0.0D);
        if (animated) {
            LegacyPoseRotations.rotateXDegrees(poseStack,
                    (float) (LegacyHbmAnimations.getRelevantTransformation("DRUM")[2] * -60.0D));
        }
        poseStack.translate(0.0D, -1.75D, 0.0D);
        ObjWeaponModels.renderPart(model, "Cylinder", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        if (animated) {
            double[] bullets = LegacyHbmAnimations.getRelevantTransformation("RELOAD_BULLETS");
            poseStack.translate(bullets[0], bullets[1], bullets[2]);
        }
        if (!animated || LegacyHbmAnimations.getRelevantTransformation("RELOAD_BULLETS_CON")[0] != 1.0D) {
            ObjWeaponModels.renderPart(model, "Bullets", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        }
        ObjWeaponModels.renderPart(model, "Casings", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        if (animated) {
            poseStack.translate(4.0D, 1.25D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack,
                    (float) (-30.0D + 30.0D * LegacyHbmAnimations.getRelevantTransformation("HAMMER")[2]));
            poseStack.translate(-4.0D, -1.25D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Hammer", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            poseStack.pushPose();
            poseStack.translate(0.125D, 2.5D, 0.0D);
            ClientSednaGunEffects.renderGapFlash(gun, 0, poseStack, buffer);
            poseStack.popPose();
        }
    }

    private static void renderFlamer(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean animated = displayContext.firstPerson() && legacyBusActive();
        poseStack.pushPose();
        if (animated) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            double[] rotate = LegacyHbmAnimations.getRelevantTransformation("ROTATE");
            poseStack.translate(0.0D, 2.0D, -6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) -equip[0]);
            poseStack.translate(0.0D, -2.0D, 6.0D);
            poseStack.translate(0.0D, 1.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) rotate[2]);
            poseStack.translate(0.0D, -1.0D, 0.0D);
        }

        poseStack.pushPose();
        if (animated) {
            LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Gun"));
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        if (spec.visibleParts().contains("HeatShield")) {
            ObjWeaponModels.renderPart(model, "HeatShield", spec.textureLocation(), poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.popPose();

        poseStack.pushPose();
        if (animated) {
            LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Tank"));
        }
        ObjWeaponModels.renderPart(model, "Tank", spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        if (animated) {
            LegacyBusAnimationTransforms.apply(poseStack, LegacyHbmAnimations.getRelevantTransformation("Gauge"));
        }
        if (displayContext.firstPerson()) {
            double fill = primaryMagazineFill(stack);
            poseStack.translate(1.25D, 1.25D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-135.0D + fill * 270.0D));
            poseStack.translate(-1.25D, -1.25D, 0.0D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderChemthrower(ItemStack stack, ItemDisplayContext displayContext,
            LegacyWavefrontModel model, RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext.firstPerson() && legacyBusActive()) {
            double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP");
            poseStack.translate(0.0D, -2.0D, -4.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) -equip[0]);
            poseStack.translate(0.0D, 2.0D, 4.0D);
        }
        if (!displayContext.firstPerson()) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
        ObjWeaponModels.renderPart(model, "Gun", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Hose", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        ObjWeaponModels.renderPart(model, "Nozzle", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double fill = magazineFillByOwner(stack, "gun_chemthrower");
            poseStack.translate(0.0D, 0.875D, 1.75D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (135.0D - fill * 270.0D));
            poseStack.translate(0.0D, -0.875D, -1.75D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderDrill(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double equip = 0.0D;
        double deploy = 0.0D;
        double lift = 0.0D;
        double spin = 0.0D;
        if (displayContext.firstPerson() && legacyBusActive()) {
            equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP")[0];
            deploy = LegacyHbmAnimations.getRelevantTransformation("DEPLOY")[0];
            lift = LegacyHbmAnimations.getRelevantTransformation("LIFT")[0];
            spin = LegacyHbmAnimations.getRelevantTransformation("SPIN")[0];
        }

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (15.0D * (1.0D - deploy * 0.5D)));
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-10.0D * (1.0D - deploy * 0.5D)));
            poseStack.translate(0.0D, 2.0D, -6.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (equip * -45.0D));
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (equip * -20.0D));
            poseStack.translate(0.0D, -2.0D, 6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift);
            poseStack.translate(0.0D, 0.0D, deploy);
        }
        ObjWeaponModels.renderPart(model, "Base", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double fill = magazineFillByOwner(stack, "gun_drill");
            poseStack.translate(1.0D, 2.0625D, -1.75D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-135.0D + fill * 270.0D));
            LegacyPoseRotations.rotateXDegrees(poseStack, -45.0F);
            poseStack.translate(-1.0D, -2.0625D, 1.75D);
        }
        ObjWeaponModels.renderPart(model, "Gauge", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        renderDrillPiston(model, "Piston1", spin, 0.0D, spec.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay);
        renderDrillPiston(model, "Piston2", spin, Math.PI * 2.0D / 3.0D, spec.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay);
        renderDrillPiston(model, "Piston3", spin, Math.PI * 4.0D / 3.0D, spec.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -spin);
        ObjWeaponModels.renderPart(model, "DrillBack", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) spin);
        ObjWeaponModels.renderPart(model, "DrillFront", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderDrillPiston(LegacyWavefrontModel model, String part, double spin, double phase,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        double pistonY = Math.sin((spin * 5.0D) * Math.PI / 180.0D + phase) * 0.125D - 0.125D;
        poseStack.translate(0.0D, pistonY, 0.0D);
        ObjWeaponModels.renderPart(model, part, texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderNi4Ni(ItemStack stack, ItemDisplayContext displayContext, LegacyWavefrontModel model,
            RenderSpec spec, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = Ni4NiGunItem.getColors(stack) == null ? spec.textureLocation()
                : ObjWeaponModels.N_I_4_N_I_GREYSCALE_TEXTURE;
        int[] colors = Ni4NiGunItem.getColors(stack);
        int dark = colors == null ? 0xFFFFFF : colors[0];
        int light = colors == null ? 0xFFFFFF : colors[1];
        int grip = colors == null ? 0xFFFFFF : colors[2];

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            double equip = ni4NiEquipX(stack);
            poseStack.translate(0.0D, 0.0D, -2.25D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip);
            poseStack.translate(0.0D, 0.0D, 2.25D);

            poseStack.translate(0.0D, -1.0D, -6.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) ni4NiRecoilX(stack));
            poseStack.translate(0.0D, 1.0D, 6.0D);
        }

        renderTintedPart(model, "FrameDark", texture, poseStack, buffer, packedLight, packedOverlay, dark);
        renderTintedPart(model, "Grip", texture, poseStack, buffer, packedLight, packedOverlay, grip);
        renderTintedPart(model, "FrameLight", texture, poseStack, buffer, packedLight, packedOverlay, light);

        poseStack.pushPose();
        if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 1.1875D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) ni4NiDrumZ(stack));
            poseStack.translate(0.0D, -1.1875D, 0.0D);
        }
        renderTintedPart(model, "Cylinder", texture, poseStack, buffer, packedLight, packedOverlay, light);
        ObjWeaponModels.renderPart(model, "CylinderHighlights", texture, poseStack, buffer, 0xF000F0, packedOverlay);
        poseStack.popPose();
        ObjWeaponModels.renderPart(model, "Barrel", texture, poseStack, buffer, 0xF000F0, packedOverlay);

        int coinCount = displayContext == ItemDisplayContext.GUI ? 4 : Ni4NiGunItem.getCoinCount(stack);
        renderNi4NiCoin(model, "Coin1", coinCount, 4, 8, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin2", coinCount, 3, 7, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin3", coinCount, 2, 6, poseStack, buffer, 0xF000F0, packedOverlay);
        renderNi4NiCoin(model, "Coin4", coinCount, 1, 5, poseStack, buffer, 0xF000F0, packedOverlay);
        if (displayContext.firstPerson() && stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderNi4NiLaserFlash(gun, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static double ni4NiEquipX(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("EQUIP")[0];
        int animation = teslaLegacyAnimation(stack);
        double millis = teslaLegacyAnimationMillis(stack);
        if (animation == LEGACY_ANIM_EQUIP) {
            return millis <= 500.0D ? lerp(-720.0D, 0.0D, millis / 500.0D) : 0.0D;
        }
        if (animation == LEGACY_ANIM_INSPECT) {
            if (millis <= 750.0D) {
                return lerp(0.0D, -1080.0D, millis / 750.0D);
            }
            if (millis <= 850.0D) {
                return -1080.0D;
            }
            if (millis <= 1600.0D) {
                return lerp(-1080.0D, 0.0D, (millis - 850.0D) / 750.0D);
            }
        }
        return 0.0D;
    }

    private static double ni4NiRecoilX(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("RECOIL")[0];
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_CYCLE) {
            return 0.0D;
        }
        double recoil = isTeslaAiming(stack) ? -5.0D : -30.0D;
        double millis = teslaLegacyAnimationMillis(stack);
        if (millis <= 100.0D) {
            return lerp(0.0D, recoil, sinDown(millis / 100.0D));
        }
        if (millis <= 250.0D) {
            return lerp(recoil, 0.0D, sinFull((millis - 100.0D) / 150.0D));
        }
        return 0.0D;
    }

    private static double ni4NiDrumZ(ItemStack stack) {
        if (legacyBusActive()) return LegacyHbmAnimations.getRelevantTransformation("DRUM")[2];
        if (teslaLegacyAnimation(stack) != LEGACY_ANIM_CYCLE) {
            return 0.0D;
        }
        double millis = teslaLegacyAnimationMillis(stack);
        return millis <= 50.0D ? 0.0D : millis <= 350.0D
                ? lerp(0.0D, 120.0D, sinFull((millis - 50.0D) / 300.0D)) : 120.0D;
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
                renderStarFAkimboStaticParts(stack, model, 1, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(1.0D, 1.0D, 0.0D);
                renderStarFAkimboStaticParts(stack, model, 0, poseStack, buffer, packedLight, packedOverlay);
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
            case STAR_F_AKIMBO -> renderStarFAkimboStaticParts(stack, model, leftHand ? 0 : 1,
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
                renderStarFAkimboStaticParts(stack, model, 1, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 5.0D);
                applyLegacyAkimboRightInventoryRotations(poseStack, true);
                poseStack.translate(-0.5D, 0.0D, 0.0D);
                if (anySilenced) {
                    poseStack.scale(0.625F, 0.625F, 0.625F);
                    poseStack.translate(0.0D, 0.0D, -4.0D);
                }
                renderStarFAkimboStaticParts(stack, model, 0, poseStack, buffer, packedLight, packedOverlay);
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

    private static void renderUziFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, int configIndex,
            boolean mirror, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_UZI_SATURN)
                ? UZI_SATURNITE_TEXTURE
                : UZI_TEXTURE;
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", configIndex);
        double[] stockFront = LegacyHbmAnimations.getRelevantTransformation("STOCKFRONT", configIndex);
        double[] stockBack = LegacyHbmAnimations.getRelevantTransformation("STOCKBACK", configIndex);
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", configIndex);
        double[] lift = LegacyHbmAnimations.getRelevantTransformation("LIFT", configIndex);
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG", configIndex);
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET", configIndex);
        double[] slide = LegacyHbmAnimations.getRelevantTransformation("SLIDE", configIndex);
        double[] yeet = LegacyHbmAnimations.getRelevantTransformation("YEET", configIndex);
        double[] speen = LegacyHbmAnimations.getRelevantTransformation("SPEEN", configIndex);

        poseStack.translate(yeet[0], yeet[1], yeet[2]);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (speen[0] * (mirror ? -1.0D : 1.0D)));
        poseStack.translate(0.0D, -2.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 2.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, -6.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) lift[0]);
        poseStack.translate(0.0D, 0.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, mirror ? "GunMirror" : "Gun", texture, poseStack, buffer, packedLight, packedOverlay);
        if (hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
            ObjWeaponModels.renderPart(model, "Silencer", texture, poseStack, buffer, packedLight, packedOverlay);
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.3125D, -5.75D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (180.0D - stockFront[0]));
        poseStack.translate(0.0D, -0.3125D, 5.75D);
        ObjWeaponModels.renderPart(model, "StockFront", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, -0.3125D, -3.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-200.0D - stockBack[0]));
        poseStack.translate(0.0D, 0.3125D, 3.0D);
        ObjWeaponModels.renderPart(model, "StockBack", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, slide[2]);
        ObjWeaponModels.renderPart(model, "Slide", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        ObjWeaponModels.renderPart(model, "Magazine", texture, poseStack, buffer, packedLight, packedOverlay);
        if (bullet[0] == 1.0D) {
            ObjWeaponModels.renderPart(model, "Bullet", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderUziEffects(gun, configIndex,
                    hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER), poseStack, buffer);
        }
    }

    private static void renderStarFAkimboStaticParts(ItemStack stack, LegacyWavefrontModel model, int configIndex,
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
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
    }

    private static void applyLegacyMinigunDualLeftInventoryRotations(PoseStack poseStack) {
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
    }

    private static void applyLegacyAkimboRightInventoryRotations(PoseStack poseStack, boolean includePitch) {
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
        if (includePitch) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        }
        LegacyPoseRotations.rotateYDegrees(poseStack, -45.0F);
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
        double hipX = 0.0D;
        double hipY = 0.0D;
        double hipZ = 0.0D;
        double aimX = 0.0D;
        double aimY = 0.0D;
        double aimZ = 0.0D;
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> {
                hipX = -1.5D * offset * side;
                hipY = -1.0D * offset;
                hipZ = 2.0D * offset;
                aimY = -3.875D / 8.0D;
                aimZ = 1.0D;
            }
            case MINIGUN_DUAL -> {
                hipX = -2.75D * offset * side;
                hipY = -1.75D * offset;
                hipZ = 2.5D * offset;
            }
            case EOTT -> {
                hipX = -1.0D * offset * side;
                hipY = -1.25D * offset;
                hipZ = 1.25D * offset;
                aimY = -5.25D / 8.0D;
                aimZ = 0.125D;
            }
            case DANI -> {
                hipX = -1.5D * offset * side;
                hipY = -0.75D * offset;
                hipZ = 1.0D * offset;
                aimY = -3.125D / 8.0D;
                aimZ = 0.25D;
            }
            case UZI_AKIMBO -> {
                hipX = -2.25D * offset * side;
                hipY = -1.5D * offset;
                hipZ = 2.5D * offset;
                aimY = -4.375D / 8.0D;
                aimZ = 1.0D;
            }
            case STAR_F_AKIMBO -> {
                hipX = -2.0D * offset * side;
                hipY = -1.75D * offset;
                hipZ = 2.5D * offset;
                aimY = -7.625D / 8.0D;
                aimZ = 1.0D;
            }
            default -> {
            }
        }
        float progress = LegacySednaAimProgress.interpolated(Minecraft.getInstance().getFrameTime());
        poseStack.translate(hipX + (aimX - hipX) * progress, hipY + (aimY - hipY) * progress,
                hipZ + (aimZ - hipZ) * progress);
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
    }

    private static void renderAkimboFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            int side, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean mirror = side == -1;
        switch (spec.specialRender()) {
            case MARESLEG_AKIMBO -> renderMareslegFirstPersonParts(stack, model, spec, true, mirror ? 0 : 1,
                    poseStack, buffer, packedLight, packedOverlay);
            case MINIGUN_DUAL -> renderMinigunDualFirstPersonParts(stack, model, spec, mirror ? 0 : 1, side,
                    poseStack, buffer, packedLight, packedOverlay);
            case EOTT -> renderAberratorFirstPersonParts(stack, model, spec, side, mirror ? 0 : 1,
                    poseStack, buffer, packedLight, packedOverlay);
            case DANI -> renderDaniFirstPersonParts(stack, model, mirror ? 0 : 1, side,
                    mirror ? DANI_CELESTIAL_TEXTURE : DANI_LUNAR_TEXTURE, poseStack, buffer, packedLight,
                    packedOverlay);
            case UZI_AKIMBO -> renderUziFirstPersonParts(stack, model, mirror ? 0 : 1, mirror,
                    poseStack, buffer, packedLight, packedOverlay);
            case STAR_F_AKIMBO -> renderStarFFirstPersonParts(stack, model, STAR_F_ELITE_TEXTURE, mirror ? 0 : 1,
                    side, poseStack, buffer, packedLight, packedOverlay);
            default -> model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    spec.visibleParts().toArray(String[]::new));
        }
    }

    /** Exact ItemRenderStarF/ItemRenderStarFAkimbo shared first-person part hierarchy. */
    private static void renderStarFFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, ResourceLocation texture,
            int configIndex, int side, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", configIndex);
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", configIndex);
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER", configIndex);
        double[] tilt = LegacyHbmAnimations.getRelevantTransformation("TILT", configIndex);
        double[] turn = LegacyHbmAnimations.getRelevantTransformation("TURN", configIndex);
        double[] mag = LegacyHbmAnimations.getRelevantTransformation("MAG", configIndex);
        double[] bullet = LegacyHbmAnimations.getRelevantTransformation("BULLET", configIndex);
        double[] slide = LegacyHbmAnimations.getRelevantTransformation("SLIDE", configIndex);

        poseStack.translate(0.0D, -2.0D, -8.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, 2.0D, 8.0D);
        poseStack.translate(0.0D, 1.0D, -3.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (turn[2] * side));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) tilt[0]);
        poseStack.translate(0.0D, -1.0D, 3.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, "Gun", texture, poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.75D, -4.25D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (60.0D * (hammer[0] - 1.0D)));
        poseStack.translate(0.0D, -1.75D, 4.25D);
        ObjWeaponModels.renderPart(model, "Hammer", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, slide[2] * 2.3125D);
        ObjWeaponModels.renderPart(model, "Slide", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(mag[0], mag[1], mag[2]);
        ObjWeaponModels.renderPart(model, "Mag", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(bullet[0], bullet[1], bullet[2]);
        ObjWeaponModels.renderPart(model, "Bullet", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        if (hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.375D, -0.25D);
            ObjWeaponModels.renderPart(extraModel("uzi", "uzi", "uzi"), "Silencer", UZI_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay);
            poseStack.popPose();
        }
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderStarFEffects(gun, configIndex,
                    hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_SILENCER), poseStack, buffer);
        }
    }

    /** Exact ItemRenderMinigunDual two-index first-person body and barrel hierarchy. */
    private static void renderMinigunDualFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, RenderSpec spec,
            int configIndex, int side, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", configIndex);
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", configIndex);
        double[] rotate = LegacyHbmAnimations.getRelevantTransformation("ROTATE", configIndex);
        poseStack.translate(0.0D, 3.0D, -6.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equip[0]);
        poseStack.translate(0.0D, -3.0D, 6.0D);
        poseStack.translate(0.0D, 0.0D, recoil[2]);
        ObjWeaponModels.renderPart(model, configIndex == 0 ? "GunDual" : "Gun", spec.textureLocation(), poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (rotate[2] * side));
        ObjWeaponModels.renderPart(model, "Barrels", spec.textureLocation(), poseStack, buffer, packedLight,
                packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderMinigunDualFlash(gun, configIndex, poseStack, buffer);
        }
    }

    /** Exact ItemRenderDANI per-configuration first-person revolver hierarchy. */
    private static void renderDaniFirstPersonParts(ItemStack stack, LegacyWavefrontModel model, int configIndex, int side,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        double[] recoil = LegacyHbmAnimations.getRelevantTransformation("RECOIL", configIndex);
        double[] reloadMove = LegacyHbmAnimations.getRelevantTransformation("RELOAD_MOVE", configIndex);
        double[] reloadRotation = LegacyHbmAnimations.getRelevantTransformation("RELOAD_ROT", configIndex);
        double[] equip = LegacyHbmAnimations.getRelevantTransformation("EQUIP", configIndex);
        double[] front = LegacyHbmAnimations.getRelevantTransformation("FRONT", configIndex);
        double[] latch = LegacyHbmAnimations.getRelevantTransformation("LATCH", configIndex);
        double[] drum = LegacyHbmAnimations.getRelevantTransformation("DRUM", configIndex);
        double[] drumPush = LegacyHbmAnimations.getRelevantTransformation("DRUM_PUSH", configIndex);
        double[] hammer = LegacyHbmAnimations.getRelevantTransformation("HAMMER", configIndex);

        poseStack.translate(recoil[0], recoil[1], recoil[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (recoil[2] * 10.0D));
        poseStack.translate(0.0D, -2.0D, -2.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) -equip[0]);
        poseStack.translate(0.0D, 2.0D, 2.0D);
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderRevolverSmoke(gun, configIndex, recoil[2], poseStack, buffer);
        }
        poseStack.translate(reloadMove[0], reloadMove[1], reloadMove[2]);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) reloadRotation[0]);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (reloadRotation[2] * side));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (reloadRotation[1] * side));
        ObjWeaponModels.renderPart(model, "Grip", texture, poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) front[2]);
        ObjWeaponModels.renderPart(model, "Barrel", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.3125D, -0.875D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) latch[2]);
        poseStack.translate(0.0D, -2.3125D, 0.875D);
        ObjWeaponModels.renderPart(model, "Latch", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (drum[2] * 60.0D));
        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.translate(0.0D, 0.0D, drumPush[2]);
        ObjWeaponModels.renderPart(model, "Drum", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -4.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-45.0D + 45.0D * hammer[2]));
        poseStack.translate(0.0D, 0.0D, 4.5D);
        ObjWeaponModels.renderPart(model, "Hammer", texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
        if (stack.getItem() instanceof SednaGunItem gun) {
            ClientSednaGunEffects.renderRevolverFlash(gun, configIndex, poseStack, buffer);
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
                applyLegacySpas12FirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHARGE_THROWER) {
                applyLegacyChargeThrowerFirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.CHEMTHROWER) {
                applyLegacyChemthrowerFirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.NI4NI) {
                applyLegacyFirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.DRILL) {
                applyLegacyDrillFirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.DEBUG) {
                applyLegacyFirstPersonDisplay(stack, displayContext, poseStack, spec);
                return;
            }
            if (spec.specialRender() == SpecialRender.ATLAS || spec.specialRender() == SpecialRender.HANGMAN) {
                applyLegacyFirstPersonDisplayBeforeModelScale(stack, displayContext, poseStack, spec);
                return;
            }
            applyLegacyFirstPersonDisplay(stack, displayContext, poseStack, spec);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float fitScale = (float) (0.82D / Math.max(1.0D, maxSize));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (180.0D + spec.modelYawDegrees()));
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
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) inventory.yRot());
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
        if (spec.inventoryRenderYawDegrees() != 0.0D) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) spec.inventoryRenderYawDegrees());
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
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        if (hasUpgrade(stack, SednaWeaponModEvaluator.ID_CARBINE_BAYONET)) {
            poseStack.scale(1.1875F, 1.1875F, 1.1875F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.translate(1.5D, 0.0D, 0.0D);
        } else {
            poseStack.scale(1.375F, 1.375F, 1.375F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.translate(-0.5D, 0.0D, 0.0D);
        }
    }

    private static void applyLegacyG3InventoryDisplay(ItemStack stack, PoseStack poseStack) {
        boolean stock = !hasUpgrade(stack, SednaWeaponModEvaluator.ID_NO_STOCK);
        boolean silenced = "gun_g3_zebra".equals(currentLegacyName(stack))
                || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SILENCER);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(stock ? 0.875F : 1.125F, stock ? 0.875F : 1.125F, stock ? 0.875F : 1.125F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, silenced ? (stock ? 50.0F : 55.0F) : 45.0F);
        poseStack.translate(stock ? (silenced ? 0.75D : -0.5D) : 2.5D, 0.5D, 0.0D);
    }

    private static void applyLegacyAmatInventoryDisplay(ItemStack stack, PoseStack poseStack) {
        boolean silenced = isAmatSilenced(stack);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(silenced ? 0.8175F : 0.9375F, silenced ? 0.8175F : 0.9375F,
                silenced ? 0.8175F : 0.9375F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.translate(-0.5D, 0.5D, silenced ? -1.0D : 0.0D);
    }

    private static void applyLegacyFireExtInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.translate(2.0D, 14.0D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, -135.0F);
        poseStack.scale((float) spec.inventory().scale(), (float) spec.inventory().scale(),
                (float) -spec.inventory().scale());
    }

    private static void applyLegacySpas12InventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyChargeThrowerInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
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
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyChemthrowerInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyDrillInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        LegacyPoseRotations.rotateZDegrees(poseStack, 225.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        LegacyPoseRotations.rotateXDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
    }

    private static void applyLegacyFirstPersonDisplay(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        if (spec.firstPersonYawDegrees() != 0.0D) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) spec.firstPersonYawDegrees());
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
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
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
        LegacyPoseRotations.rotateZDegrees(poseStack, 25.0F);
        poseStack.translate(spec.firstPerson().aimX(), spec.firstPerson().aimY(), spec.firstPerson().aimZ());
        LegacyPoseRotations.rotateYDegrees(poseStack, 80.0F);
        poseStack.scale((float) spec.firstPerson().renderScale(), (float) spec.firstPerson().renderScale(),
                (float) spec.firstPerson().renderScale());
    }

    private static void applyLegacySpas12FirstPersonDisplay(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
    }

    private static void applyLegacyChargeThrowerFirstPersonDisplay(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
    }

    private static void applyLegacyChemthrowerFirstPersonDisplay(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
    }

    private static void applyLegacyDrillFirstPersonDisplay(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        double deploy = legacyBusActive() ? LegacyHbmAnimations.getRelevantTransformation("DEPLOY")[0] : 0.0D;
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (15.0D * (1.0D - deploy * 0.5D)));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-10.0D * (1.0D - deploy * 0.5D)));
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

    /** Keeps source body rails before the renderer-local model scale. */
    private static void applyLegacyFirstPersonDisplayBeforeModelScale(ItemStack stack, ItemDisplayContext displayContext,
            PoseStack poseStack, RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        applyLegacyAimTranslation(stack, spec, poseStack);
        if (spec.firstPersonYawDegrees() != 0.0D) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) spec.firstPersonYawDegrees());
        }
    }

    /** Exact ItemRenderWeaponBase#standardAimingTransform inputs for every non-akimbo route that invokes it. */
    private static void applyLegacyAimTranslation(ItemStack stack, RenderSpec spec, PoseStack poseStack) {
        FirstPersonPose hip = spec.firstPerson();
        double targetX = hip.aimX();
        double targetY = hip.aimY();
        double targetZ = hip.aimZ();
        switch (currentLegacyName(stack)) {
            case "gun_pepperbox" -> {
                targetX = 0.0D;
                targetY = -2.5D / 8.0D;
                targetZ = 0.5D;
            }
            case "gun_debug" -> {
                targetX = 0.0D;
                targetY = -3.875D / 8.0D;
                targetZ = 0.0D;
            }
            case "gun_light_revolver", "gun_light_revolver_atlas" -> {
                targetX = 0.0D;
                targetY = -3.125D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_henry", "gun_henry_lincoln" -> {
                targetX = 0.0D;
                targetY = -5.0D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_greasegun" -> {
                targetX = 0.0D;
                targetY = -2.625D / 8.0D;
                targetZ = 1.125D;
            }
            case "gun_maresleg", "gun_maresleg_broken" -> {
                targetX = 0.0D;
                targetY = -3.875D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_flaregun" -> {
                targetX = 0.0D;
                targetY = -5.5D / 8.0D;
                targetZ = 0.5D;
            }
            case "gun_panzerschreck" -> {
                targetX = -0.9375D;
                targetY = -9.25D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_minigun", "gun_minigun_lacunae" -> {
                targetX = 0.0D;
                targetY = -6.25D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_am180" -> {
                targetX = 0.0D;
                targetY = -4.1875D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_liberator" -> {
                targetX = 0.0D;
                targetY = -4.625D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_congolake" -> {
                targetX = 0.0D;
                targetY = -10.0D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_lag" -> {
                targetX = 0.0D;
                targetY = -3.375D / 8.0D;
                targetZ = 0.5D;
            }
            case "gun_uzi" -> {
                targetX = 0.0D;
                targetY = -4.375D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_mk108" -> {
                targetX = -0.75D;
                targetY = -0.75D;
                targetZ = 1.5D;
            }
            case "gun_m2" -> {
                targetX = 0.0D;
                targetY = -12.5D / 8.0D;
                targetZ = 1.75D;
            }
            case "gun_aberrator" -> {
                targetX = 0.0D;
                targetY = -5.25D / 8.0D;
                targetZ = 0.125D;
            }
            case "gun_laser_pistol", "gun_laser_pistol_pew_pew", "gun_laser_pistol_morning_glory" -> {
                targetX = 0.0D;
                targetY = -10.0D / 8.0D;
                targetZ = 1.25D;
            }
            case "gun_autoshotgun", "gun_autoshotgun_shredder" -> {
                targetX = 0.0D;
                targetY = -6.25D / 8.0D;
                targetZ = 0.5D;
            }
            case "gun_quadro" -> {
                targetX = -1.5D * 0.8D;
                targetY = -3.0D * 0.8D;
                targetZ = 2.5D * 0.8D;
            }
            case "gun_autoshotgun_sexy", "gun_autoshotgun_heretic" -> {
                targetX = -0.5D;
                targetY = -0.5D;
                targetZ = 2.0D;
            }
            case "gun_hangman" -> {
                targetX = 0.0D;
                targetY = -1.5D / 8.0D;
                targetZ = 1.25D;
            }
            case "gun_bolter" -> {
                targetX = 0.0D;
                targetY = -10.5D / 8.0D;
                targetZ = 1.25D;
            }
            case "gun_missile_launcher" -> {
                targetX = -1.0D * 0.8D;
                targetY = -1.25D * 0.8D;
                targetZ = 0.0D;
            }
            case "gun_tau" -> {
                targetX = -1.75D * 0.8D;
                targetY = -1.75D * 0.8D;
                targetZ = 3.5D * 0.8D;
            }
            case "gun_tesla_cannon" -> {
                targetX = -1.3125D * 0.8D;
                targetY = 0.0D;
                targetZ = -0.5D * 0.8D;
            }
            case "gun_coilgun" -> {
                targetX = 0.0D;
                targetY = -7.5D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_flamer", "gun_flamer_topaz", "gun_flamer_daybreaker" -> {
                targetX = 0.0D;
                targetY = -4.625D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_fatman" -> {
                targetX = -1.0D * 0.8D;
                targetY = -1.25D * 0.8D;
                targetZ = 0.0D;
            }
            case "gun_folly" -> {
                targetX = -2.0D * 0.75D;
                targetY = -1.0D * 0.75D;
                targetZ = 2.25D * 0.8D;
            }
            case "gun_n_i_4_n_i" -> {
                targetX = 0.0D;
                targetY = -5.0D / 8.0D;
                targetZ = 0.125D;
            }
            case "gun_drill" -> {
                targetX = -1.0D * 0.8D;
                targetY = -1.75D * 0.8D;
                targetZ = 1.25D * 0.8D;
            }
            case "gun_spas12" -> {
                targetX = 0.0D;
                targetY = 0.0D;
                targetZ = 0.0D;
            }
            case "gun_stinger" -> {
                targetX = -2.625D * 0.8D;
                targetY = -6.5D;
                targetZ = -8.5D;
            }
            case "gun_chemthrower" -> {
                targetX = 0.0D;
                targetY = -4.375D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_double_barrel", "gun_double_barrel_sacred_dragon" -> {
                targetX = 0.0D;
                targetY = -2.0D / 8.0D;
                targetZ = 1.0D;
            }
            case "gun_carbine" -> {
                targetX = 0.0D;
                targetY = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE) ? -1.0D : -6.25D / 8.0D;
                targetZ = 0.25D;
            }
            case "gun_g3", "gun_g3_zebra" -> {
                boolean scoped = "gun_g3_zebra".equals(currentLegacyName(stack))
                        || hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
                targetX = 0.0D;
                targetY = scoped ? -5.53125D / 8.0D : -3.5625D / 8.0D;
                targetZ = scoped ? 1.46875D : 1.75D;
            }
            case "gun_amat", "gun_amat_subtlety", "gun_amat_penance" -> {
                targetX = 0.0D;
                targetY = -4.875D / 8.0D;
                targetZ = 1.875D;
            }
            case "gun_mas36" -> {
                boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
                targetX = scoped ? -0.2D : 0.0D;
                targetY = scoped ? -5.875D / 8.0D : -4.6825D / 8.0D;
                targetZ = scoped ? 1.125D : 0.75D;
            }
            case "gun_charge_thrower" -> {
                boolean scoped = hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
                targetX = scoped ? -0.15625D : -0.75D;
                targetY = scoped ? -6.5D / 8.0D : -0.625D;
                targetZ = scoped ? 1.6875D : 1.75D;
            }
            case "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege" -> {
                boolean scoped = isHeavyRevolverScoped(stack);
                targetX = 0.0D;
                targetY = scoped ? -4.75D / 8.0D : -3.875D / 8.0D;
                targetZ = scoped ? -0.25D : 0.0D;
            }
            case "gun_lasrifle" -> {
                boolean scoped = !hasUpgrade(stack, SednaWeaponModEvaluator.ID_LAS_AUTO);
                targetX = 0.0D;
                targetY = scoped ? -7.375D / 8.0D : -5.25D / 8.0D;
                targetZ = scoped ? 0.75D : 1.0D;
            }
            case "gun_stg77" -> {
                targetX = 0.0D;
                targetY = -5.75D / 8.0D;
                targetZ = 2.0D;
            }
            default -> {
            }
        }
        float progress = LegacySednaAimProgress.interpolated(Minecraft.getInstance().getFrameTime());
        poseStack.translate(hip.aimX() + (targetX - hip.aimX()) * progress,
                hip.aimY() + (targetY - hip.aimY()) * progress,
                hip.aimZ() + (targetZ - hip.aimZ()) * progress);
    }

    private static boolean hidesFirstPersonAtFullAim(ItemStack stack) {
        if (!LegacySednaAimProgress.settledFullyAimed()) {
            return false;
        }
        return switch (currentLegacyName(stack)) {
            case "gun_carbine", "gun_mas36", "gun_charge_thrower" ->
                    hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
            case "gun_g3" -> hasUpgrade(stack, SednaWeaponModEvaluator.ID_SCOPE);
            case "gun_g3_zebra", "gun_amat", "gun_amat_subtlety", "gun_amat_penance", "gun_stg77",
                    "gun_stinger" -> true;
            case "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege" ->
                    isHeavyRevolverScoped(stack);
            case "gun_lasrifle" -> !hasUpgrade(stack, SednaWeaponModEvaluator.ID_LAS_AUTO);
            default -> false;
        };
    }

    private record MissileLauncherAnimationPose(double equipX, double openX, double barrelZ, double missileX,
            double missileY, double missileZ) {
        private static final MissileLauncherAnimationPose IDENTITY = new MissileLauncherAnimationPose(0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D);
    }

    private record Am180AnimationPose(double equipX, double recoilZ, double magX, double magY, double magZ,
            double magTurnX, double magTurnZ, double magSpinX, double boltZ, double turnZ) {
        private static final Am180AnimationPose IDENTITY = new Am180AnimationPose(0.0D, 0.0D, 0.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private enum SpecialRender {
        NONE(false),
        DEBUG(false),
        TAU(false),
        TESLA_CANNON(false),
        FATMAN(false),
        PEPPERBOX(false),
        HENRY(false),
        ATLAS(false),
        HANGMAN(false),
        GREASEGUN(false),
        FLAREGUN(false),
        CONGOLAKE(false),
        MARESLEG(false),
        LIBERATOR(false),
        CARBINE(false),
        MINIGUN(false),
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
        SHREDDER(false),
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
