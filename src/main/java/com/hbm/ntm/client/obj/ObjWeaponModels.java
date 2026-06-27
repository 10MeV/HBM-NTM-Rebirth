package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjWeaponModels {
    public static final LegacyWavefrontModel SHIMMER_SLEDGE = model("shimmer_sledge");
    public static final LegacyWavefrontModel SHIMMER_AXE = model("shimmer_axe");
    public static final LegacyWavefrontModel STOPSIGN = model("stopsign");
    public static final LegacyWavefrontModel GAVEL = model("gavel").asVBO();
    public static final LegacyWavefrontModel CRUCIBLE = model("crucible").asVBO();
    public static final LegacyWavefrontModel CHAINSAW = model("chainsaw").noSmooth().asVBO();
    public static final LegacyWavefrontModel BOLTGUN = model("boltgun").asVBO();
    public static final LegacyWavefrontModel BOLTER = model("bolter").asVBO();
    public static final LegacyWavefrontModel DETONATOR_LASER = model("detonator_laser").asVBO();
    public static final LegacyWavefrontModel FIREEXT = model("fireext").asVBO();
    public static final LegacyWavefrontModel COILGUN = model("coilgun").asVBO();
    public static final LegacyWavefrontModel PEPPERBOX = model("pepperbox").asVBO();
    public static final LegacyWavefrontModel BIO_REVOLVER = model("bio_revolver").asVBO();
    public static final LegacyWavefrontModel HENRY = model("henry").asVBO();
    public static final LegacyWavefrontModel GREASEGUN = model("greasegun").asVBO();
    public static final LegacyWavefrontModel MARESLEG = model("maresleg").asVBO();
    public static final LegacyWavefrontModel FLAREGUN = model("flaregun").asVBO();
    public static final LegacyWavefrontModel AM180 = model("am180").asVBO();
    public static final LegacyWavefrontModel LIBERATOR = model("liberator").asVBO();
    public static final LegacyWavefrontModel CONGOLAKE = model("congolake").asVBO();
    public static final LegacyWavefrontModel FLAMETHROWER = model("flamethrower").asVBO();
    public static final LegacyWavefrontModel LILMAC = model("lilmac").asVBO();
    public static final LegacyWavefrontModel CARBINE = model("carbine").asVBO();
    public static final LegacyWavefrontModel UZI = model("uzi").asVBO();
    public static final LegacyWavefrontModel SPAS_12 = model("spas-12").asVBO();
    public static final LegacyWavefrontModel PANZERSCHRECK = model("panzerschreck").asVBO();
    public static final LegacyWavefrontModel STAR_F = model("star_f").asVBO();
    public static final LegacyWavefrontModel G3 = model("g3").asVBO();
    public static final LegacyWavefrontModel STINGER = model("stinger").asVBO();
    public static final LegacyWavefrontModel MK108 = model("mk108").asVBO();
    public static final LegacyWavefrontModel CHEMTHROWER = model("chemthrower").asVBO();
    public static final LegacyWavefrontModel AMAT = model("amat").asVBO();
    public static final LegacyWavefrontModel M2 = model("m2_browning").asVBO();
    public static final LegacyWavefrontModel SHREDDER = model("shredder").asVBO();
    public static final LegacyWavefrontModel SEXY = model("sexy").asVBO();
    public static final LegacyWavefrontModel WHISKEY = model("whiskey").asVBO();
    public static final LegacyWavefrontModel QUADRO = model("quadro").asVBO();
    public static final LegacyWavefrontModel MIKE_HAWK = model("mike_hawk").asVBO();
    public static final LegacyWavefrontModel MINIGUN = model("minigun").asVBO();
    public static final LegacyWavefrontModel MISSILE_LAUNCHER = model("missile_launcher").asVBO();
    public static final LegacyWavefrontModel TESLA_CANNON = model("tesla_cannon").asVBO();
    public static final LegacyWavefrontModel LASER_PISTOL = model("laser_pistol").asVBO();
    public static final LegacyWavefrontModel STG77 = model("stg77").asVBO();
    public static final LegacyWavefrontModel TAU = model("tau").asVBO();
    public static final LegacyWavefrontModel FATMAN = model("fatman").asVBO();
    public static final LegacyWavefrontModel LASRIFLE = model("lasrifle").asVBO();
    public static final LegacyWavefrontModel LASRIFLE_MODS = model("lasrifle_mods").asVBO();
    public static final LegacyWavefrontModel HANGMAN = model("hangman").asVBO();
    public static final LegacyWavefrontModel FOLLY = model("folly").asVBO();
    public static final LegacyWavefrontModel DOUBLE_BARREL = model("sacred_dragon").asVBO();
    public static final LegacyWavefrontModel ABERRATOR = model("aberrator").asVBO();
    public static final LegacyWavefrontModel MAS36 = model("mas36").asVBO();
    public static final LegacyWavefrontModel CHARGE_THROWER = model("charge_thrower").asVBO();
    public static final LegacyWavefrontModel DRILL = model("drill").asVBO();
    public static final LegacyWavefrontModel N_I_4_N_I = model("n_i_4_n_i").asVBO();
    public static final LegacyWavefrontModel LANCE = model("lance");
    public static final LegacyWavefrontModel GRENADES = model("grenades");
    public static final LegacyWavefrontModel BUILDING = model("building");
    public static final LegacyWavefrontModel TORPEDO = model("torpedo");
    public static final LegacyWavefrontModel TOM_MAIN = model("tom_main").asVBO();
    public static final LegacyWavefrontModel TOM_FLAME = hmfModel("tom_flame").asVBO();
    private static final LegacyWavefrontModel.SelectionHandle SPAS_12_MAIN_BODY =
            SPAS_12.prepareRenderOnlyInCallOrder("MainBody");
    private static final LegacyWavefrontModel.SelectionHandle SPAS_12_PUMP_GRIP =
            SPAS_12.prepareRenderOnlyInCallOrder("PumpGrip");
    private static final LegacyWavefrontModel.SelectionHandle TAU_BODY =
            TAU.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle TAU_ROTOR =
            TAU.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_GUN =
            MARESLEG.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_LEVER =
            MARESLEG.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_STOCK =
            MARESLEG.prepareRenderOnlyInCallOrder("Stock");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_BARREL =
            MARESLEG.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_SHELL =
            MARESLEG.prepareRenderOnlyInCallOrder("Shell");
    private static final LegacyWavefrontModel.SelectionHandle MARESLEG_GUN_LEVER =
            MARESLEG.prepareRenderOnly("Gun", "Lever");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_GUN =
            CARBINE.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_SLIDE =
            CARBINE.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_MAGAZINE =
            CARBINE.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_BULLET =
            CARBINE.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_IRON_SIGHT =
            CARBINE.prepareRenderOnlyInCallOrder("IronSight");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_SCOPE =
            CARBINE.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle CARBINE_BAYONET =
            CARBINE.prepareRenderOnlyInCallOrder("Bayonet");
    private static final LegacyWavefrontModel.SelectionHandle UZI_GUN =
            UZI.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle UZI_GUN_MIRROR =
            UZI.prepareRenderOnlyInCallOrder("GunMirror");
    private static final LegacyWavefrontModel.SelectionHandle UZI_SLIDE =
            UZI.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle UZI_BULLET =
            UZI.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle UZI_MAGAZINE =
            UZI.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle UZI_STOCK_FRONT =
            UZI.prepareRenderOnlyInCallOrder("StockFront");
    private static final LegacyWavefrontModel.SelectionHandle UZI_STOCK_BACK =
            UZI.prepareRenderOnlyInCallOrder("StockBack");
    private static final LegacyWavefrontModel.SelectionHandle UZI_SILENCER =
            UZI.prepareRenderOnlyInCallOrder("Silencer");
    private static final LegacyWavefrontModel.SelectionHandle STAR_F_GUN =
            STAR_F.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle STAR_F_SLIDE =
            STAR_F.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle STAR_F_MAG =
            STAR_F.prepareRenderOnlyInCallOrder("Mag");
    private static final LegacyWavefrontModel.SelectionHandle STAR_F_HAMMER =
            STAR_F.prepareRenderOnlyInCallOrder("Hammer");
    private static final LegacyWavefrontModel.SelectionHandle STAR_F_BULLET =
            STAR_F.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle G3_RIFLE =
            G3.prepareRenderOnlyInCallOrder("Rifle");
    private static final LegacyWavefrontModel.SelectionHandle G3_BULLET =
            G3.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle G3_GUIDE_AND_BOLT =
            G3.prepareRenderOnlyInCallOrder("Guide_And_Bolt");
    private static final LegacyWavefrontModel.SelectionHandle G3_HANDLE =
            G3.prepareRenderOnlyInCallOrder("Handle");
    private static final LegacyWavefrontModel.SelectionHandle G3_PLUG =
            G3.prepareRenderOnlyInCallOrder("Plug");
    private static final LegacyWavefrontModel.SelectionHandle G3_MAG_PADDLE =
            G3.prepareRenderOnlyInCallOrder("Mag_Paddle");
    private static final LegacyWavefrontModel.SelectionHandle G3_MAGAZINE =
            G3.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle G3_STOCK =
            G3.prepareRenderOnlyInCallOrder("Stock");
    private static final LegacyWavefrontModel.SelectionHandle G3_FLASH_HIDER =
            G3.prepareRenderOnlyInCallOrder("Flash_Hider");
    private static final LegacyWavefrontModel.SelectionHandle G3_SCOPE =
            G3.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle G3_SILENCER =
            G3.prepareRenderOnlyInCallOrder("Silencer");
    private static final LegacyWavefrontModel.SelectionHandle G3_SELECTOR =
            G3.prepareRenderOnlyInCallOrder("Selector");
    private static final LegacyWavefrontModel.SelectionHandle G3_TRIGGER =
            G3.prepareRenderOnlyInCallOrder("Trigger");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BOLT =
            AMAT.prepareRenderOnlyInCallOrder("Bolt");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_GUN =
            AMAT.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_SCOPE =
            AMAT.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_MAGAZINE =
            AMAT.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BULLET =
            AMAT.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BIPOD_RIGHT =
            AMAT.prepareRenderOnlyInCallOrder("BipodRight");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BIPOD_HINGE_RIGHT =
            AMAT.prepareRenderOnlyInCallOrder("BipodHingeRight");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_MUZZLE_BRAKE =
            AMAT.prepareRenderOnlyInCallOrder("MuzzleBrake");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BIPOD_HINGE_LEFT =
            AMAT.prepareRenderOnlyInCallOrder("BipodHingeLeft");
    private static final LegacyWavefrontModel.SelectionHandle AMAT_BIPOD_LEFT =
            AMAT.prepareRenderOnlyInCallOrder("BipodLeft");
    private static final LegacyWavefrontModel.SelectionHandle MK108_BARREL =
            MK108.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle MK108_LID =
            MK108.prepareRenderOnlyInCallOrder("Lid");
    private static final LegacyWavefrontModel.SelectionHandle MK108_BELT =
            MK108.prepareRenderOnlyInCallOrder("Belt");
    private static final LegacyWavefrontModel.SelectionHandle MK108_GRENADE =
            MK108.prepareRenderOnlyInCallOrder("Grenade");
    private static final LegacyWavefrontModel.SelectionHandle MK108_DRUM =
            MK108.prepareRenderOnlyInCallOrder("Drum");
    private static final LegacyWavefrontModel.SelectionHandle MK108_GUN =
            MK108.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_GUN =
            MAS36.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_BOLT =
            MAS36.prepareRenderOnlyInCallOrder("Bolt");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_STOCK =
            MAS36.prepareRenderOnlyInCallOrder("Stock");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_BAYONET =
            MAS36.prepareRenderOnlyInCallOrder("Bayonet");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_SCOPE =
            MAS36.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_CLIP =
            MAS36.prepareRenderOnlyInCallOrder("Clip");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_BULLET =
            MAS36.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle MAS36_BULLETS =
            MAS36.prepareRenderOnlyInCallOrder("Bullets");
    private static final LegacyWavefrontModel.SelectionHandle AM180_MAG =
            AM180.prepareRenderOnlyInCallOrder("Mag");
    private static final LegacyWavefrontModel.SelectionHandle AM180_MAG_PLATE =
            AM180.prepareRenderOnlyInCallOrder("MagPlate");
    private static final LegacyWavefrontModel.SelectionHandle AM180_SILENCER =
            AM180.prepareRenderOnlyInCallOrder("Silencer");
    private static final LegacyWavefrontModel.SelectionHandle AM180_BOLT =
            AM180.prepareRenderOnlyInCallOrder("Bolt");
    private static final LegacyWavefrontModel.SelectionHandle AM180_TRIGGER =
            AM180.prepareRenderOnlyInCallOrder("Trigger");
    private static final LegacyWavefrontModel.SelectionHandle AM180_GUN =
            AM180.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_BARREL =
            SEXY.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_SHELL =
            SEXY.prepareRenderOnlyInCallOrder("Shell");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_BELT =
            SEXY.prepareRenderOnlyInCallOrder("Belt");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_MAGAZINE =
            SEXY.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_HOOD =
            SEXY.prepareRenderOnlyInCallOrder("Hood");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_RECOIL_SPRING =
            SEXY.prepareRenderOnlyInCallOrder("RecoilSpring");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_LEVER =
            SEXY.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_LOCK_SPRING =
            SEXY.prepareRenderOnlyInCallOrder("LockSpring");
    private static final LegacyWavefrontModel.SelectionHandle SEXY_GUN =
            SEXY.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle BOLTER_MAG =
            BOLTER.prepareRenderOnlyInCallOrder("Mag");
    private static final LegacyWavefrontModel.SelectionHandle BOLTER_BULLET =
            BOLTER.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle BOLTER_CASING =
            BOLTER.prepareRenderOnlyInCallOrder("Casing");
    private static final LegacyWavefrontModel.SelectionHandle BOLTER_BODY =
            BOLTER.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle STG77_BREECH =
            STG77.prepareRenderOnlyInCallOrder("Breech");
    private static final LegacyWavefrontModel.SelectionHandle STG77_BARREL =
            STG77.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle STG77_LEVER =
            STG77.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle STG77_SAFETY =
            STG77.prepareRenderOnlyInCallOrder("Safety");
    private static final LegacyWavefrontModel.SelectionHandle STG77_HANDLE =
            STG77.prepareRenderOnlyInCallOrder("Handle");
    private static final LegacyWavefrontModel.SelectionHandle STG77_BULLETS =
            STG77.prepareRenderOnlyInCallOrder("Bullets");
    private static final LegacyWavefrontModel.SelectionHandle STG77_MAGAZINE =
            STG77.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle STG77_GUN =
            STG77.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle LASER_PISTOL_TAPE =
            LASER_PISTOL.prepareRenderOnlyInCallOrder("Tape");
    private static final LegacyWavefrontModel.SelectionHandle LASER_PISTOL_CAPACITORS =
            LASER_PISTOL.prepareRenderOnlyInCallOrder("Capacitors");
    private static final LegacyWavefrontModel.SelectionHandle LASER_PISTOL_BATTERY =
            LASER_PISTOL.prepareRenderOnlyInCallOrder("Battery");
    private static final LegacyWavefrontModel.SelectionHandle LASER_PISTOL_LATCH =
            LASER_PISTOL.prepareRenderOnlyInCallOrder("Latch");
    private static final LegacyWavefrontModel.SelectionHandle LASER_PISTOL_GUN =
            LASER_PISTOL.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle QUADRO_ROCKETS =
            QUADRO.prepareRenderOnlyInCallOrder("Rockets");
    private static final LegacyWavefrontModel.SelectionHandle QUADRO_LAUNCHER =
            QUADRO.prepareRenderOnlyInCallOrder("Launcher");
    private static final LegacyWavefrontModel.SelectionHandle MISSILE_LAUNCHER_FRONT =
            MISSILE_LAUNCHER.prepareRenderOnlyInCallOrder("Front");
    private static final LegacyWavefrontModel.SelectionHandle MISSILE_LAUNCHER_BARREL =
            MISSILE_LAUNCHER.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle MISSILE_LAUNCHER_MISSILE =
            MISSILE_LAUNCHER.prepareRenderOnlyInCallOrder("Missile");
    private static final LegacyWavefrontModel.SelectionHandle MISSILE_LAUNCHER_LAUNCHER =
            MISSILE_LAUNCHER.prepareRenderOnlyInCallOrder("Launcher");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_SCOPE =
            LASRIFLE.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_STOCK =
            LASRIFLE.prepareRenderOnlyInCallOrder("Stock");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_BARREL =
            LASRIFLE.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_BATTERY =
            LASRIFLE.prepareRenderOnlyInCallOrder("Battery");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_LEVER =
            LASRIFLE.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_GUN =
            LASRIFLE.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_MODS_UNDER_BARREL =
            LASRIFLE_MODS.prepareRenderOnlyInCallOrder("UnderBarrel");
    private static final LegacyWavefrontModel.SelectionHandle LASRIFLE_MODS_BARREL_SHOTGUN =
            LASRIFLE_MODS.prepareRenderOnlyInCallOrder("BarrelShotgun");
    private static final LegacyWavefrontModel.SelectionHandle PANZERSCHRECK_SHIELD =
            PANZERSCHRECK.prepareRenderOnlyInCallOrder("Shield");
    private static final LegacyWavefrontModel.SelectionHandle PANZERSCHRECK_ROCKET =
            PANZERSCHRECK.prepareRenderOnlyInCallOrder("Rocket");
    private static final LegacyWavefrontModel.SelectionHandle PANZERSCHRECK_TUBE =
            PANZERSCHRECK.prepareRenderOnlyInCallOrder("Tube");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_PISTON =
            FATMAN.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_HANDLE =
            FATMAN.prepareRenderOnlyInCallOrder("Handle");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_GAUGE =
            FATMAN.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_LID =
            FATMAN.prepareRenderOnlyInCallOrder("Lid");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_MININUKE =
            FATMAN.prepareRenderOnlyInCallOrder("MiniNuke");
    private static final LegacyWavefrontModel.SelectionHandle FATMAN_LAUNCHER =
            FATMAN.prepareRenderOnlyInCallOrder("Launcher");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_OOMPH =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Oomph");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_MORTAR =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Mortar");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_GUN =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_SCOPE =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_HOOK =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Hook");
    private static final LegacyWavefrontModel.SelectionHandle CHARGE_THROWER_ROCKET =
            CHARGE_THROWER.prepareRenderOnlyInCallOrder("Rocket");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_GUN =
            ABERRATOR.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_HAMMER =
            ABERRATOR.prepareRenderOnlyInCallOrder("Hammer");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_MAGAZINE =
            ABERRATOR.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_BULLET =
            ABERRATOR.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_SLIDE =
            ABERRATOR.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_SIGHT =
            ABERRATOR.prepareRenderOnlyInCallOrder("Sight");
    private static final LegacyWavefrontModel.SelectionHandle MIKE_HAWK_GRIP =
            MIKE_HAWK.prepareRenderOnlyInCallOrder("Grip");
    private static final LegacyWavefrontModel.SelectionHandle MIKE_HAWK_SLIDE =
            MIKE_HAWK.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle MIKE_HAWK_HAMMER =
            MIKE_HAWK.prepareRenderOnlyInCallOrder("Hammer");
    private static final LegacyWavefrontModel.SelectionHandle MIKE_HAWK_BULLET =
            MIKE_HAWK.prepareRenderOnlyInCallOrder("Bullet");
    private static final LegacyWavefrontModel.SelectionHandle MIKE_HAWK_MAGAZINE =
            MIKE_HAWK.prepareRenderOnlyInCallOrder("Magazine");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_STOCK =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("Stock");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_BARREL_SHORT =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("BarrelShort");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_BARREL =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_BUCKLE =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("Buckle");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_LEVER =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle DOUBLE_BARREL_SHELLS =
            DOUBLE_BARREL.prepareRenderOnlyInCallOrder("Shells");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_GUN =
            LILMAC.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_CYLINDER =
            LILMAC.prepareRenderOnlyInCallOrder("Cylinder");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_BULLETS =
            LILMAC.prepareRenderOnlyInCallOrder("Bullets");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_CASINGS =
            LILMAC.prepareRenderOnlyInCallOrder("Casings");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_PIVOT =
            LILMAC.prepareRenderOnlyInCallOrder("Pivot");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_HAMMER =
            LILMAC.prepareRenderOnlyInCallOrder("Hammer");
    private static final LegacyWavefrontModel.SelectionHandle LILMAC_SCOPE =
            LILMAC.prepareRenderOnlyInCallOrder("Scope");
    private static final LegacyWavefrontModel.SelectionHandle FLAMETHROWER_GUN =
            FLAMETHROWER.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle FLAMETHROWER_TANK =
            FLAMETHROWER.prepareRenderOnlyInCallOrder("Tank");
    private static final LegacyWavefrontModel.SelectionHandle FLAMETHROWER_GAUGE =
            FLAMETHROWER.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle FLAMETHROWER_HEAT_SHIELD =
            FLAMETHROWER.prepareRenderOnlyInCallOrder("HeatShield");
    private static final LegacyWavefrontModel.SelectionHandle CHEMTHROWER_GUN =
            CHEMTHROWER.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle CHEMTHROWER_HOSE =
            CHEMTHROWER.prepareRenderOnlyInCallOrder("Hose");
    private static final LegacyWavefrontModel.SelectionHandle CHEMTHROWER_NOZZLE =
            CHEMTHROWER.prepareRenderOnlyInCallOrder("Nozzle");
    private static final LegacyWavefrontModel.SelectionHandle CHEMTHROWER_GAUGE =
            CHEMTHROWER.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_BASE =
            DRILL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_GAUGE =
            DRILL.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_PISTON_1 =
            DRILL.prepareRenderOnlyInCallOrder("Piston1");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_PISTON_2 =
            DRILL.prepareRenderOnlyInCallOrder("Piston2");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_PISTON_3 =
            DRILL.prepareRenderOnlyInCallOrder("Piston3");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_BACK =
            DRILL.prepareRenderOnlyInCallOrder("DrillBack");
    private static final LegacyWavefrontModel.SelectionHandle DRILL_FRONT =
            DRILL.prepareRenderOnlyInCallOrder("DrillFront");
    private static final LegacyWavefrontModel.SelectionHandle TESLA_CANNON_GUN =
            TESLA_CANNON.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle TESLA_CANNON_EXTENSION =
            TESLA_CANNON.prepareRenderOnlyInCallOrder("Extension");
    private static final LegacyWavefrontModel.SelectionHandle TESLA_CANNON_COG =
            TESLA_CANNON.prepareRenderOnlyInCallOrder("Cog");
    private static final LegacyWavefrontModel.SelectionHandle TESLA_CANNON_CAPACITOR =
            TESLA_CANNON.prepareRenderOnlyInCallOrder("Capacitor");
    private static final LegacyWavefrontModel.SelectionHandle FOLLY_CANNON =
            FOLLY.prepareRenderOnlyInCallOrder("Cannon");
    private static final LegacyWavefrontModel.SelectionHandle FOLLY_BARREL =
            FOLLY.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle FOLLY_SHELL =
            FOLLY.prepareRenderOnlyInCallOrder("Shell");
    private static final LegacyWavefrontModel.SelectionHandle FOLLY_BREECH =
            FOLLY.prepareRenderOnlyInCallOrder("Breech");
    private static final LegacyWavefrontModel.SelectionHandle FOLLY_COG =
            FOLLY.prepareRenderOnlyInCallOrder("Cog");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_BARREL =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_COIN_4 =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Coin4");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_COIN_3 =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Coin3");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_COIN_2 =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Coin2");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_COIN_1 =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Coin1");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_GRIP =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Grip");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_FRAME_LIGHT =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("FrameLight");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_CYLINDER =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("Cylinder");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_CYLINDER_HIGHLIGHTS =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("CylinderHighlights");
    private static final LegacyWavefrontModel.SelectionHandle N_I_4_N_I_FRAME_DARK =
            N_I_4_N_I.prepareRenderOnlyInCallOrder("FrameDark");
    private static final LegacyWavefrontModel.SelectionHandle LANCE_SPEAR =
            LANCE.prepareRenderOnlyInCallOrder("Spear");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_AKIMBO =
            ABERRATOR.prepareRenderOnly("Gun", "Hammer", "Magazine", "Slide", "Sight");
    private static final LegacyWavefrontModel.SelectionHandle ABERRATOR_AKIMBO_FIRST_PERSON =
            ABERRATOR.prepareRenderOnly("Gun", "Hammer", "Magazine", "Bullet", "Slide", "Sight");
    private static final LegacyWavefrontModel.SelectionHandle MINIGUN_GUN_BARRELS =
            MINIGUN.prepareRenderOnly("Gun", "Barrels");
    private static final LegacyWavefrontModel.SelectionHandle MINIGUN_DUAL_BARRELS =
            MINIGUN.prepareRenderOnly("GunDual", "Barrels");

    public static final ResourceLocation SHIMMER_SLEDGE_TEXTURE = texture("shimmer_sledge");
    public static final ResourceLocation SHIMMER_AXE_TEXTURE = texture("shimmer_axe");
    public static final ResourceLocation STOPSIGN_TEXTURE = texture("stopsign");
    public static final ResourceLocation SOPSIGN_TEXTURE = texture("sopsign");
    public static final ResourceLocation CHERNOBYLSIGN_TEXTURE = texture("chernobylsign");
    public static final ResourceLocation GAVEL_WOOD_TEXTURE = texture("gavel_wood");
    public static final ResourceLocation GAVEL_LEAD_TEXTURE = texture("gavel_lead");
    public static final ResourceLocation GAVEL_DIAMOND_TEXTURE = texture("gavel_diamond");
    public static final ResourceLocation GAVEL_MESE_TEXTURE = texture("gavel_mese");
    public static final ResourceLocation CRUCIBLE_HILT_TEXTURE = texture("crucible_hilt");
    public static final ResourceLocation CRUCIBLE_GUARD_TEXTURE = texture("crucible_guard");
    public static final ResourceLocation CRUCIBLE_BLADE_TEXTURE = texture("crucible_blade");
    public static final ResourceLocation CHAINSAW_TEXTURE = texture("chainsaw");
    public static final ResourceLocation BOLTGUN_TEXTURE = texture("boltgun");
    public static final ResourceLocation BOLTER_TEXTURE = texture("bolter");
    public static final ResourceLocation FIREEXT_TEXTURE = texture("fireext_normal");
    public static final ResourceLocation FIREEXT_FOAM_TEXTURE = texture("fireext_foam");
    public static final ResourceLocation FIREEXT_SAND_TEXTURE = texture("fireext_sand");
    public static final ResourceLocation STINGER_TEXTURE = texture("stinger");
    public static final ResourceLocation DETONATOR_LASER_TEXTURE = texture("detonator_laser");
    public static final ResourceLocation SPAS_12_TEXTURE = texture("spas-12");
    public static final ResourceLocation CHEMTHROWER_TEXTURE = texture("chemthrower");
    public static final ResourceLocation M2_TEXTURE = texture("m2_browning");
    public static final ResourceLocation COILGUN_TEXTURE = texture("coilgun");
    public static final ResourceLocation CONGOLAKE_TEXTURE = texture("congolake");
    public static final ResourceLocation DEBUG_GUN_TEXTURE = texture("debug_gun");
    public static final ResourceLocation PEPPERBOX_TEXTURE = texture("pepperbox");
    public static final ResourceLocation BIO_REVOLVER_TEXTURE = texture("bio_revolver");
    public static final ResourceLocation BIO_REVOLVER_ATLAS_TEXTURE = texture("bio_revolver_atlas");
    public static final ResourceLocation DANI_CELESTIAL_TEXTURE = texture("dani_celestial");
    public static final ResourceLocation DANI_LUNAR_TEXTURE = texture("dani_lunar");
    public static final ResourceLocation HENRY_TEXTURE = texture("henry");
    public static final ResourceLocation HENRY_LINCOLN_TEXTURE = texture("henry_lincoln");
    public static final ResourceLocation GREASEGUN_TEXTURE = texture("greasegun");
    public static final ResourceLocation GREASEGUN_CLEAN_TEXTURE = texture("greasegun_clean");
    public static final ResourceLocation MARESLEG_TEXTURE = texture("maresleg");
    public static final ResourceLocation MARESLEG_BROKEN_TEXTURE = texture("maresleg_broken");
    public static final ResourceLocation FLAREGUN_TEXTURE = texture("flaregun");
    public static final ResourceLocation HEAVY_REVOLVER_TEXTURE = texture("heavy_revolver");
    public static final ResourceLocation PROTEGE_TEXTURE = texture("protege");
    public static final ResourceLocation LILMAC_TEXTURE = texture("lilmac");
    public static final ResourceLocation LILMAC_SCOPE_TEXTURE = texture("lilmac_scope");
    public static final ResourceLocation CARBINE_TEXTURE = texture("huntsman");
    public static final ResourceLocation CARBINE_BAYONET_TEXTURE = texture("carbine_bayonet");
    public static final ResourceLocation CARBINE_SCOPE_TEXTURE = texture("carbine_scope");
    public static final ResourceLocation AM180_TEXTURE = texture("am180");
    public static final ResourceLocation LIBERATOR_TEXTURE = texture("liberator");
    public static final ResourceLocation FLAMETHROWER_TEXTURE = texture("flamethrower");
    public static final ResourceLocation FLAMETHROWER_TOPAZ_TEXTURE = texture("flamethrower_topaz");
    public static final ResourceLocation FLAMETHROWER_DAYBREAKER_TEXTURE = texture("flamethrower_daybreaker");
    public static final ResourceLocation MIKE_HAWK_TEXTURE = texture("lag");
    public static final ResourceLocation UZI_TEXTURE = texture("uzi");
    public static final ResourceLocation UZI_SATURNITE_TEXTURE = texture("uzi_saturnite");
    public static final ResourceLocation PANZERSCHRECK_TEXTURE = texture("panzerschreck");
    public static final ResourceLocation STAR_F_TEXTURE = texture("star_f");
    public static final ResourceLocation STAR_F_ELITE_TEXTURE = texture("star_f_elite");
    public static final ResourceLocation G3_TEXTURE = texture("g3");
    public static final ResourceLocation G3_ZEBRA_TEXTURE = texture("g3_zebra");
    public static final ResourceLocation G3_GREEN_TEXTURE = texture("g3_polymer_green");
    public static final ResourceLocation G3_BLACK_TEXTURE = texture("g3_polymer_black");
    public static final ResourceLocation G3_ATTACHMENTS_TEXTURE = texture("g3_attachments");
    public static final ResourceLocation MK108_TEXTURE = texture("mk108");
    public static final ResourceLocation AMAT_TEXTURE = texture("amat");
    public static final ResourceLocation AMAT_SUBTLETY_TEXTURE = texture("amat_subtlety");
    public static final ResourceLocation AMAT_PENANCE_TEXTURE = texture("amat_penance");
    public static final ResourceLocation SHREDDER_TEXTURE = texture("shredder");
    public static final ResourceLocation SHREDDER_ORIG_TEXTURE = texture("shredder_orig");
    public static final ResourceLocation SEXY_TEXTURE = texture("sexy_real_no_fake");
    public static final ResourceLocation HERETIC_TEXTURE = texture("sexy_heretic");
    public static final ResourceLocation WHISKEY_TEXTURE = texture("whiskey");
    public static final ResourceLocation QUADRO_TEXTURE = texture("quadro");
    public static final ResourceLocation QUADRO_ROCKET_TEXTURE = texture("quadro_rocket");
    public static final ResourceLocation MINIGUN_TEXTURE = texture("minigun");
    public static final ResourceLocation MINIGUN_LACUNAE_TEXTURE = texture("minigun_lacunae");
    public static final ResourceLocation MINIGUN_DUAL_TEXTURE = texture("minigun_dual");
    public static final ResourceLocation MISSILE_LAUNCHER_TEXTURE = texture("missile_launcher");
    public static final ResourceLocation TESLA_CANNON_TEXTURE = texture("tesla_cannon");
    public static final ResourceLocation LASER_PISTOL_TEXTURE = texture("laser_pistol");
    public static final ResourceLocation LASER_PISTOL_PEW_PEW_TEXTURE = texture("laser_pistol_pew_pew");
    public static final ResourceLocation LASER_PISTOL_MORNING_GLORY_TEXTURE = texture("laser_pistol_morning_glory");
    public static final ResourceLocation STG77_TEXTURE = texture("stg77");
    public static final ResourceLocation TAU_TEXTURE = texture("tau");
    public static final ResourceLocation FATMAN_TEXTURE = texture("fatman");
    public static final ResourceLocation FATMAN_MININUKE_TEXTURE = texture("fatman_mininuke");
    public static final ResourceLocation FATMAN_BALEFIRE_TEXTURE = texture("fatman_balefire");
    public static final ResourceLocation CLUSTER_SUBMUNITION_TEXTURE = texture("fatman_submunition");
    public static final ResourceLocation LASRIFLE_TEXTURE = texture("lasrifle");
    public static final ResourceLocation LASRIFLE_MODS_TEXTURE = texture("lasrifle_mods");
    public static final ResourceLocation HANGMAN_TEXTURE = texture("hangman");
    public static final ResourceLocation FOLLY_TEXTURE = texture("moonlight");
    public static final ResourceLocation DOUBLE_BARREL_TEXTURE = texture("double_barrel");
    public static final ResourceLocation DOUBLE_BARREL_SACRED_DRAGON_TEXTURE = texture("double_barrel_sacred_dragon");
    public static final ResourceLocation ABERRATOR_TEXTURE = texture("aberrator");
    public static final ResourceLocation EOTT_TEXTURE = texture("eott");
    public static final ResourceLocation MAS36_TEXTURE = texture("mas36");
    public static final ResourceLocation CHARGE_THROWER_TEXTURE = texture("charge_thrower");
    public static final ResourceLocation CHARGE_THROWER_HOOK_TEXTURE = texture("charge_thrower_hook");
    public static final ResourceLocation CHARGE_THROWER_MORTAR_TEXTURE = texture("charge_thrower_mortar");
    public static final ResourceLocation CHARGE_THROWER_ROCKET_TEXTURE = texture("charge_thrower_rocket");
    public static final ResourceLocation DRILL_TEXTURE = texture("drill");
    public static final ResourceLocation N_I_4_N_I_TEXTURE = texture("n_i_4_n_i");
    public static final ResourceLocation N_I_4_N_I_GREYSCALE_TEXTURE = texture("n_i_4_n_i_greyscale");
    public static final ResourceLocation LANCE_TEXTURE = texture("lance");
    public static final ResourceLocation BUILDING_TEXTURE = texture("building");
    public static final ResourceLocation TORPEDO_TEXTURE = texture("torpedo");
    public static final ResourceLocation TOM_MAIN_TEXTURE = texture("tom_main");
    public static final ResourceLocation TOM_FLAME_TEXTURE = texture("tom_flame");
    public static final ResourceLocation GRENADE_FRAG_TEXTURE = grenadeTexture("frag");
    public static final ResourceLocation GRENADE_FRAG_BODY_TEXTURE = grenadeTexture("frag_body");
    public static final ResourceLocation GRENADE_FRAG_LABEL_TEXTURE = grenadeTexture("frag_label");
    public static final ResourceLocation GRENADE_FRAG_FUZE_TEXTURE = grenadeTexture("frag_fuze");
    public static final ResourceLocation GRENADE_STICK_TEXTURE = grenadeTexture("stick");
    public static final ResourceLocation GRENADE_STICK_BODY_TEXTURE = grenadeTexture("stick_body");
    public static final ResourceLocation GRENADE_STICK_LABEL_TEXTURE = grenadeTexture("stick_label");
    public static final ResourceLocation GRENADE_STICK_FUZE_TEXTURE = grenadeTexture("stick_fuze");
    public static final ResourceLocation GRENADE_TECH_TEXTURE = grenadeTexture("tech");
    public static final ResourceLocation GRENADE_TECH_BODY_TEXTURE = grenadeTexture("tech_body");
    public static final ResourceLocation GRENADE_TECH_LIGHTS_TEXTURE = grenadeTexture("tech_lights");
    public static final ResourceLocation GRENADE_TECH_FUZE_TEXTURE = grenadeTexture("tech_fuze");
    public static final ResourceLocation GRENADE_NUKA_TEXTURE = grenadeTexture("nuka");
    public static final ResourceLocation GRENADE_NUKA_BODY_TEXTURE = grenadeTexture("nuka_body");
    public static final ResourceLocation GRENADE_NUKA_LABEL_TEXTURE = grenadeTexture("nuka_label");
    public static final ResourceLocation GRENADE_NUKA_FUZE_TEXTURE = grenadeTexture("nuka_fuze");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                modelLocation(name),
                texture(name)).asVBO();
    }

    private static ResourceLocation modelLocation(String name) {
        String path = switch (name) {
            case "shimmer_axe", "shimmer_sledge" -> "models/" + name + ".obj";
            default -> "models/weapons/" + name + ".obj";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    public static LegacyWavefrontModel hmfModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/weapons/" + name + ".hmf"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return switch (name) {
            case "shimmer_axe", "shimmer_sledge" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
            default -> modelTexture("weapons/" + name);
        };
    }

    public static ResourceLocation grenadeTexture(String name) {
        return modelTexture("grenades/" + name);
    }

    private static ResourceLocation modelTexture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + path + ".png");
    }

    public static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PreparedPart prepared = preparedPart(model, partName);
        if (prepared != null) {
            prepared.model().renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    prepared.selection());
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderOnly(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            String... partNames) {
        PreparedPart prepared = preparedOnly(model, partNames);
        if (prepared != null) {
            prepared.model().renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    prepared.selection());
            return;
        }
        model.renderOnly(texture, poseStack, buffer, packedLight, packedOverlay, partNames);
    }

    public static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha) {
        PreparedPart prepared = preparedPart(model, partName);
        if (prepared != null) {
            prepared.model().renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, false, prepared.selection());
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }

    public static void renderPartGlintWithLegacyTextureMatrix(LegacyWavefrontModel model, String partName,
            ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, int red, int green, int blue, int alpha, float uScale, float vScale,
            float rotationDegrees, float uTranslate, float vTranslate) {
        PreparedPart prepared = preparedPart(model, partName);
        if (prepared != null) {
            prepared.model().renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, false, LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH,
                    legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate),
                    prepared.selection());
            return;
        }
        model.renderPartGlintWithLegacyTextureMatrix(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, uScale, vScale, rotationDegrees, uTranslate, vTranslate);
    }

    public static void renderLanceSpear(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LANCE.renderOnlyInCallOrder(LANCE_TEXTURE, poseStack, buffer, packedLight, packedOverlay, LANCE_SPEAR);
    }

    public static void renderLanceSpearAdditive(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, int red, int green, int blue, int alpha) {
        LANCE.renderOnlyInCallOrder(LANCE_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, false, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                LegacyWavefrontModel.UvTransform.DEFAULT, LANCE_SPEAR);
    }

    private static LegacyWavefrontModel.UvTransform legacyTextureMatrix(float uScale, float vScale,
            float rotationDegrees, float uTranslate, float vTranslate) {
        float radians = (float) Math.toRadians(rotationDegrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return LegacyWavefrontModel.UvTransform.dynamic(
                uScale * cos,
                -uScale * sin,
                vScale * sin,
                vScale * cos,
                uScale * (cos * uTranslate - sin * vTranslate),
                vScale * (sin * uTranslate + cos * vTranslate),
                0.0F);
    }

    private static PreparedPart preparedPart(LegacyWavefrontModel model, String partName) {
        if (model == null || partName == null) {
            return null;
        }
        if (sameModel(model, SPAS_12)) {
            return prepared(SPAS_12, switch (partName) {
                case "MainBody" -> SPAS_12_MAIN_BODY;
                case "PumpGrip" -> SPAS_12_PUMP_GRIP;
                default -> null;
            });
        }
        if (sameModel(model, TAU)) {
            return prepared(TAU, switch (partName) {
                case "Body" -> TAU_BODY;
                case "Rotor" -> TAU_ROTOR;
                default -> null;
            });
        }
        if (sameModel(model, MARESLEG)) {
            return prepared(MARESLEG, switch (partName) {
                case "Gun" -> MARESLEG_GUN;
                case "Lever" -> MARESLEG_LEVER;
                case "Stock" -> MARESLEG_STOCK;
                case "Barrel" -> MARESLEG_BARREL;
                case "Shell" -> MARESLEG_SHELL;
                default -> null;
            });
        }
        if (sameModel(model, CARBINE)) {
            return prepared(CARBINE, switch (partName) {
                case "Gun" -> CARBINE_GUN;
                case "Slide" -> CARBINE_SLIDE;
                case "Magazine" -> CARBINE_MAGAZINE;
                case "Bullet" -> CARBINE_BULLET;
                case "IronSight" -> CARBINE_IRON_SIGHT;
                case "Scope" -> CARBINE_SCOPE;
                case "Bayonet" -> CARBINE_BAYONET;
                default -> null;
            });
        }
        if (sameModel(model, UZI)) {
            return prepared(UZI, switch (partName) {
                case "Gun" -> UZI_GUN;
                case "GunMirror" -> UZI_GUN_MIRROR;
                case "Slide" -> UZI_SLIDE;
                case "Bullet" -> UZI_BULLET;
                case "Magazine" -> UZI_MAGAZINE;
                case "StockFront" -> UZI_STOCK_FRONT;
                case "StockBack" -> UZI_STOCK_BACK;
                case "Silencer" -> UZI_SILENCER;
                default -> null;
            });
        }
        if (sameModel(model, STAR_F)) {
            return prepared(STAR_F, switch (partName) {
                case "Gun" -> STAR_F_GUN;
                case "Slide" -> STAR_F_SLIDE;
                case "Mag" -> STAR_F_MAG;
                case "Hammer" -> STAR_F_HAMMER;
                case "Bullet" -> STAR_F_BULLET;
                default -> null;
            });
        }
        if (sameModel(model, G3)) {
            return prepared(G3, switch (partName) {
                case "Rifle" -> G3_RIFLE;
                case "Bullet" -> G3_BULLET;
                case "Guide_And_Bolt" -> G3_GUIDE_AND_BOLT;
                case "Handle" -> G3_HANDLE;
                case "Plug" -> G3_PLUG;
                case "Mag_Paddle" -> G3_MAG_PADDLE;
                case "Magazine" -> G3_MAGAZINE;
                case "Stock" -> G3_STOCK;
                case "Flash_Hider" -> G3_FLASH_HIDER;
                case "Scope" -> G3_SCOPE;
                case "Silencer" -> G3_SILENCER;
                case "Selector" -> G3_SELECTOR;
                case "Trigger" -> G3_TRIGGER;
                default -> null;
            });
        }
        if (sameModel(model, AMAT)) {
            return prepared(AMAT, switch (partName) {
                case "Bolt" -> AMAT_BOLT;
                case "Gun" -> AMAT_GUN;
                case "Scope" -> AMAT_SCOPE;
                case "Magazine" -> AMAT_MAGAZINE;
                case "Bullet" -> AMAT_BULLET;
                case "BipodRight" -> AMAT_BIPOD_RIGHT;
                case "BipodHingeRight" -> AMAT_BIPOD_HINGE_RIGHT;
                case "MuzzleBrake" -> AMAT_MUZZLE_BRAKE;
                case "BipodHingeLeft" -> AMAT_BIPOD_HINGE_LEFT;
                case "BipodLeft" -> AMAT_BIPOD_LEFT;
                default -> null;
            });
        }
        if (sameModel(model, MK108)) {
            return prepared(MK108, switch (partName) {
                case "Barrel" -> MK108_BARREL;
                case "Lid" -> MK108_LID;
                case "Belt" -> MK108_BELT;
                case "Grenade" -> MK108_GRENADE;
                case "Drum" -> MK108_DRUM;
                case "Gun" -> MK108_GUN;
                default -> null;
            });
        }
        if (sameModel(model, MAS36)) {
            return prepared(MAS36, switch (partName) {
                case "Gun" -> MAS36_GUN;
                case "Bolt" -> MAS36_BOLT;
                case "Stock" -> MAS36_STOCK;
                case "Bayonet" -> MAS36_BAYONET;
                case "Scope" -> MAS36_SCOPE;
                case "Clip" -> MAS36_CLIP;
                case "Bullet" -> MAS36_BULLET;
                case "Bullets" -> MAS36_BULLETS;
                default -> null;
            });
        }
        if (sameModel(model, AM180)) {
            return prepared(AM180, switch (partName) {
                case "Mag" -> AM180_MAG;
                case "MagPlate" -> AM180_MAG_PLATE;
                case "Silencer" -> AM180_SILENCER;
                case "Bolt" -> AM180_BOLT;
                case "Trigger" -> AM180_TRIGGER;
                case "Gun" -> AM180_GUN;
                default -> null;
            });
        }
        if (sameModel(model, SEXY)) {
            return prepared(SEXY, switch (partName) {
                case "Barrel" -> SEXY_BARREL;
                case "Shell" -> SEXY_SHELL;
                case "Belt" -> SEXY_BELT;
                case "Magazine" -> SEXY_MAGAZINE;
                case "Hood" -> SEXY_HOOD;
                case "RecoilSpring" -> SEXY_RECOIL_SPRING;
                case "Lever" -> SEXY_LEVER;
                case "LockSpring" -> SEXY_LOCK_SPRING;
                case "Gun" -> SEXY_GUN;
                default -> null;
            });
        }
        if (sameModel(model, BOLTER)) {
            return prepared(BOLTER, switch (partName) {
                case "Mag" -> BOLTER_MAG;
                case "Bullet" -> BOLTER_BULLET;
                case "Casing" -> BOLTER_CASING;
                case "Body" -> BOLTER_BODY;
                default -> null;
            });
        }
        if (sameModel(model, STG77)) {
            return prepared(STG77, switch (partName) {
                case "Breech" -> STG77_BREECH;
                case "Barrel" -> STG77_BARREL;
                case "Lever" -> STG77_LEVER;
                case "Safety" -> STG77_SAFETY;
                case "Handle" -> STG77_HANDLE;
                case "Bullets" -> STG77_BULLETS;
                case "Magazine" -> STG77_MAGAZINE;
                case "Gun" -> STG77_GUN;
                default -> null;
            });
        }
        if (sameModel(model, LASER_PISTOL)) {
            return prepared(LASER_PISTOL, switch (partName) {
                case "Tape" -> LASER_PISTOL_TAPE;
                case "Capacitors" -> LASER_PISTOL_CAPACITORS;
                case "Battery" -> LASER_PISTOL_BATTERY;
                case "Latch" -> LASER_PISTOL_LATCH;
                case "Gun" -> LASER_PISTOL_GUN;
                default -> null;
            });
        }
        if (sameModel(model, QUADRO)) {
            return prepared(QUADRO, switch (partName) {
                case "Rockets" -> QUADRO_ROCKETS;
                case "Launcher" -> QUADRO_LAUNCHER;
                default -> null;
            });
        }
        if (sameModel(model, MISSILE_LAUNCHER)) {
            return prepared(MISSILE_LAUNCHER, switch (partName) {
                case "Front" -> MISSILE_LAUNCHER_FRONT;
                case "Barrel" -> MISSILE_LAUNCHER_BARREL;
                case "Missile" -> MISSILE_LAUNCHER_MISSILE;
                case "Launcher" -> MISSILE_LAUNCHER_LAUNCHER;
                default -> null;
            });
        }
        if (sameModel(model, LASRIFLE)) {
            return prepared(LASRIFLE, switch (partName) {
                case "Scope" -> LASRIFLE_SCOPE;
                case "Stock" -> LASRIFLE_STOCK;
                case "Barrel" -> LASRIFLE_BARREL;
                case "Battery" -> LASRIFLE_BATTERY;
                case "Lever" -> LASRIFLE_LEVER;
                case "Gun" -> LASRIFLE_GUN;
                default -> null;
            });
        }
        if (sameModel(model, LASRIFLE_MODS)) {
            return prepared(LASRIFLE_MODS, switch (partName) {
                case "UnderBarrel" -> LASRIFLE_MODS_UNDER_BARREL;
                case "BarrelShotgun" -> LASRIFLE_MODS_BARREL_SHOTGUN;
                default -> null;
            });
        }
        if (sameModel(model, PANZERSCHRECK)) {
            return prepared(PANZERSCHRECK, switch (partName) {
                case "Shield" -> PANZERSCHRECK_SHIELD;
                case "Rocket" -> PANZERSCHRECK_ROCKET;
                case "Tube" -> PANZERSCHRECK_TUBE;
                default -> null;
            });
        }
        if (sameModel(model, FATMAN)) {
            return prepared(FATMAN, switch (partName) {
                case "Piston" -> FATMAN_PISTON;
                case "Handle" -> FATMAN_HANDLE;
                case "Gauge" -> FATMAN_GAUGE;
                case "Lid" -> FATMAN_LID;
                case "MiniNuke" -> FATMAN_MININUKE;
                case "Launcher" -> FATMAN_LAUNCHER;
                default -> null;
            });
        }
        if (sameModel(model, CHARGE_THROWER)) {
            return prepared(CHARGE_THROWER, switch (partName) {
                case "Oomph" -> CHARGE_THROWER_OOMPH;
                case "Mortar" -> CHARGE_THROWER_MORTAR;
                case "Gun" -> CHARGE_THROWER_GUN;
                case "Scope" -> CHARGE_THROWER_SCOPE;
                case "Hook" -> CHARGE_THROWER_HOOK;
                case "Rocket" -> CHARGE_THROWER_ROCKET;
                default -> null;
            });
        }
        if (sameModel(model, ABERRATOR)) {
            return prepared(ABERRATOR, switch (partName) {
                case "Gun" -> ABERRATOR_GUN;
                case "Hammer" -> ABERRATOR_HAMMER;
                case "Magazine" -> ABERRATOR_MAGAZINE;
                case "Bullet" -> ABERRATOR_BULLET;
                case "Slide" -> ABERRATOR_SLIDE;
                case "Sight" -> ABERRATOR_SIGHT;
                default -> null;
            });
        }
        if (sameModel(model, MIKE_HAWK)) {
            return prepared(MIKE_HAWK, switch (partName) {
                case "Grip" -> MIKE_HAWK_GRIP;
                case "Slide" -> MIKE_HAWK_SLIDE;
                case "Hammer" -> MIKE_HAWK_HAMMER;
                case "Bullet" -> MIKE_HAWK_BULLET;
                case "Magazine" -> MIKE_HAWK_MAGAZINE;
                default -> null;
            });
        }
        if (sameModel(model, DOUBLE_BARREL)) {
            return prepared(DOUBLE_BARREL, switch (partName) {
                case "Stock" -> DOUBLE_BARREL_STOCK;
                case "BarrelShort" -> DOUBLE_BARREL_BARREL_SHORT;
                case "Barrel" -> DOUBLE_BARREL_BARREL;
                case "Buckle" -> DOUBLE_BARREL_BUCKLE;
                case "Lever" -> DOUBLE_BARREL_LEVER;
                case "Shells" -> DOUBLE_BARREL_SHELLS;
                default -> null;
            });
        }
        if (sameModel(model, LILMAC)) {
            return prepared(LILMAC, switch (partName) {
                case "Gun" -> LILMAC_GUN;
                case "Cylinder" -> LILMAC_CYLINDER;
                case "Bullets" -> LILMAC_BULLETS;
                case "Casings" -> LILMAC_CASINGS;
                case "Pivot" -> LILMAC_PIVOT;
                case "Hammer" -> LILMAC_HAMMER;
                case "Scope" -> LILMAC_SCOPE;
                default -> null;
            });
        }
        if (sameModel(model, FLAMETHROWER)) {
            return prepared(FLAMETHROWER, switch (partName) {
                case "Gun" -> FLAMETHROWER_GUN;
                case "Tank" -> FLAMETHROWER_TANK;
                case "Gauge" -> FLAMETHROWER_GAUGE;
                case "HeatShield" -> FLAMETHROWER_HEAT_SHIELD;
                default -> null;
            });
        }
        if (sameModel(model, CHEMTHROWER)) {
            return prepared(CHEMTHROWER, switch (partName) {
                case "Gun" -> CHEMTHROWER_GUN;
                case "Hose" -> CHEMTHROWER_HOSE;
                case "Nozzle" -> CHEMTHROWER_NOZZLE;
                case "Gauge" -> CHEMTHROWER_GAUGE;
                default -> null;
            });
        }
        if (sameModel(model, DRILL)) {
            return prepared(DRILL, switch (partName) {
                case "Base" -> DRILL_BASE;
                case "Gauge" -> DRILL_GAUGE;
                case "Piston1" -> DRILL_PISTON_1;
                case "Piston2" -> DRILL_PISTON_2;
                case "Piston3" -> DRILL_PISTON_3;
                case "DrillBack" -> DRILL_BACK;
                case "DrillFront" -> DRILL_FRONT;
                default -> null;
            });
        }
        if (sameModel(model, TESLA_CANNON)) {
            return prepared(TESLA_CANNON, switch (partName) {
                case "Gun" -> TESLA_CANNON_GUN;
                case "Extension" -> TESLA_CANNON_EXTENSION;
                case "Cog" -> TESLA_CANNON_COG;
                case "Capacitor" -> TESLA_CANNON_CAPACITOR;
                default -> null;
            });
        }
        if (sameModel(model, FOLLY)) {
            return prepared(FOLLY, switch (partName) {
                case "Cannon" -> FOLLY_CANNON;
                case "Barrel" -> FOLLY_BARREL;
                case "Shell" -> FOLLY_SHELL;
                case "Breech" -> FOLLY_BREECH;
                case "Cog" -> FOLLY_COG;
                default -> null;
            });
        }
        if (sameModel(model, N_I_4_N_I)) {
            return prepared(N_I_4_N_I, switch (partName) {
                case "Barrel" -> N_I_4_N_I_BARREL;
                case "Coin4" -> N_I_4_N_I_COIN_4;
                case "Coin3" -> N_I_4_N_I_COIN_3;
                case "Coin2" -> N_I_4_N_I_COIN_2;
                case "Coin1" -> N_I_4_N_I_COIN_1;
                case "Grip" -> N_I_4_N_I_GRIP;
                case "FrameLight" -> N_I_4_N_I_FRAME_LIGHT;
                case "Cylinder" -> N_I_4_N_I_CYLINDER;
                case "CylinderHighlights" -> N_I_4_N_I_CYLINDER_HIGHLIGHTS;
                case "FrameDark" -> N_I_4_N_I_FRAME_DARK;
                default -> null;
            });
        }
        return null;
    }

    private static PreparedPart preparedOnly(LegacyWavefrontModel model, String... partNames) {
        if (model == null || partNames == null) {
            return null;
        }
        if (sameModel(model, MARESLEG) && sameParts(partNames, "Gun", "Lever")) {
            return prepared(MARESLEG, MARESLEG_GUN_LEVER);
        }
        if (sameModel(model, ABERRATOR)) {
            if (sameParts(partNames, "Gun", "Hammer", "Magazine", "Slide", "Sight")) {
                return prepared(ABERRATOR, ABERRATOR_AKIMBO);
            }
            if (sameParts(partNames, "Gun", "Hammer", "Magazine", "Bullet", "Slide", "Sight")) {
                return prepared(ABERRATOR, ABERRATOR_AKIMBO_FIRST_PERSON);
            }
        }
        if (sameModel(model, MINIGUN)) {
            if (sameParts(partNames, "Gun", "Barrels")) {
                return prepared(MINIGUN, MINIGUN_GUN_BARRELS);
            }
            if (sameParts(partNames, "GunDual", "Barrels")) {
                return prepared(MINIGUN, MINIGUN_DUAL_BARRELS);
            }
        }
        return null;
    }

    private static PreparedPart prepared(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection) {
        return selection == null ? null : new PreparedPart(model, selection);
    }

    private static boolean sameModel(LegacyWavefrontModel model, LegacyWavefrontModel expected) {
        return model == expected || model.modelLocation().equals(expected.modelLocation());
    }

    private static boolean sameParts(String[] actual, String... expected) {
        return Arrays.equals(actual, expected);
    }

    private record PreparedPart(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection) {
    }

    private ObjWeaponModels() {
    }
}
