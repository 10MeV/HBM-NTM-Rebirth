package com.hbm.ntm.bullet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hbm.ntm.bullet.SednaGunConfig.Crosshair;
import com.hbm.ntm.bullet.SednaGunConfig.ModeBuilder;
import com.hbm.ntm.bullet.SednaGunConfig.WeaponQuality;

public final class LegacySednaGunConfigs {
    private static final Map<String, SednaGunConfig> BY_NAME = new LinkedHashMap<>();

    public static final SednaGunConfig GUN_DEBUG = register(SednaGunConfig.builder("gun_debug", "GunFactory",
            "ItemGunBaseNT", WeaponQuality.DEBUG)
            .config(new ModeBuilder(0)
                    .durability(600.0F).draw(15).inspect(23).crosshair(Crosshair.L_CLASSIC)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.DEBUG_ORCHESTRA")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressSecondary("lambda:Lego.clickReceiver(receiver=1)")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD").pressTertiary("Lego.LAMBDA_TOGGLE_AIM")
                    .decider("GunFactory.LAMBDA_DEBUG_DECIDER").animation("Lego.LAMBDA_DEBUG_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_DEBUG_PRIMARY)
                            .damage(10.0F).delay(14).reload(46).jam(23)
                            .sound("hbm:weapon.44Shoot", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire().build())
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_DEBUG_SECONDARY)
                            .damage(5.0F).delay(14).reload(46).jam(23)
                            .sound("hbm:weapon.44Shoot", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).secondFire().build())
                    .build())
            .notes("Legacy debug gun has one GunConfig with two receivers and a custom decider for primary/secondary auto-refire.")
            .build());

    public static final SednaGunConfig GUN_PEPPERBOX = register(exact("gun_pepperbox", "XFactoryBlackPowder",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactoryBlackPowder.init().",
            standardMode(0, 300.0F, 4, 23, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_PEPPERBOX", "XFactoryBlackPowder.LAMBDA_PEPPERBOX_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_PEPPERBOX)
                            .damage(5.0F).delay(27).reload(67).jam(58)
                            .sound("NTMSounds.GUN_POWDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactoryBlackPowder.LAMBDA_RECOIL_PEPPERBOX").build())
                    .build()));

    public static final SednaGunConfig GUN_LIGHT_REVOLVER = register(exact("gun_light_revolver", "XFactory357",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory357.init().",
            standardMode(0, 300.0F, 4, 23, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_ATLAS", "XFactory357.LAMBDA_ATLAS_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LIGHT_REVOLVER)
                            .damage(7.5F).delay(11).reload(55).jam(45)
                            .sound("NTMSounds.GUN_PISTOL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory357.LAMBDA_RECOIL_ATLAS").build())
                    .build()));
    public static final SednaGunConfig GUN_LIGHT_REVOLVER_ATLAS = register(exact("gun_light_revolver_atlas",
            "XFactory357", "ItemGunBaseNT", WeaponQuality.B_SIDE, "Exact fields copied from XFactory357.init().",
            standardMode(0, 300.0F, 4, 23, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_ATLAS", "XFactory357.LAMBDA_ATLAS_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LIGHT_REVOLVER_ATLAS)
                            .damage(12.5F).delay(11).reload(55).jam(45)
                            .sound("NTMSounds.GUN_PISTOL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory357.LAMBDA_RECOIL_ATLAS").build())
                    .build()));
    public static final SednaGunConfig GUN_LIGHT_REVOLVER_DANI = register(exact("gun_light_revolver_dani",
            "XFactory357", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory357.init(); legacy source creates two GunConfig instances.",
            new ModeBuilder(0)
                    .durability(30000.0F).draw(20).inspect(23).crosshair(Crosshair.CIRCLE)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.ORCHESTRA_DANI")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory357.LAMBDA_DANI_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_LIGHT_REVOLVER_DANI_PRIMARY, 0)
                            .damage(15.0F).spreadHipfire(0.0F).delay(11).reload(55).jam(45)
                            .sound("NTMSounds.GUN_PISTOL_FIRE", 1.0F, 1.1F)
                            .offset(0.75D, -0.0625D, 0.3125D).standardFire()
                            .recoil("XFactory357.LAMBDA_RECOIL_DANI").build())
                    .notes("No tertiary aim handler is installed in the first Dani GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(30000.0F).draw(20).inspect(23).crosshair(Crosshair.CIRCLE)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.ORCHESTRA_DANI")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory357.LAMBDA_DANI_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_LIGHT_REVOLVER_DANI_SECONDARY, 0)
                            .damage(15.0F).spreadHipfire(0.0F).delay(11).reload(55).jam(45)
                            .sound("NTMSounds.GUN_PISTOL_FIRE", 1.0F, 0.9F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory357.LAMBDA_RECOIL_DANI").build())
                    .notes("No tertiary aim handler is installed in the second Dani GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));

    public static final SednaGunConfig GUN_HENRY = register(exact("gun_henry", "XFactory44", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, "Exact fields copied from XFactory44.init().",
            standardMode(0, 300.0F, 15, 23, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_HENRY", "XFactory44.LAMBDA_HENRY_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HENRY)
                            .damage(10.0F).delay(20).reload(25, 11, 14, 8).jam(45)
                            .sound("NTMSounds.GUN_RIFLE_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_HENRY").build())
                    .build()));
    public static final SednaGunConfig GUN_HENRY_LINCOLN = register(exact("gun_henry_lincoln", "XFactory44",
            "ItemGunBaseNT", WeaponQuality.B_SIDE, "Exact fields copied from XFactory44.init().",
            standardMode(0, 300.0F, 15, 23, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_HENRY", "XFactory44.LAMBDA_HENRY_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HENRY_LINCOLN)
                            .damage(20.0F).spreadHipfire(0.0F).delay(20).reload(25, 11, 14, 8).jam(45)
                            .sound("NTMSounds.GUN_RIFLE_FIRE", 1.0F, 1.25F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_HENRY").build())
                    .build()));
    public static final SednaGunConfig GUN_HEAVY_REVOLVER = register(exact("gun_heavy_revolver", "XFactory44",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory44.init().",
            standardMode(0, 600.0F, 10, 23, Crosshair.L_CLASSIC, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_NOPIP", "XFactory44.LAMBDA_NOPIP_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HEAVY_REVOLVER)
                            .damage(15.0F).delay(14).reload(46).jam(23)
                            .sound("NTMSounds.GUN_HEAVY_REVOLVER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_NOPIP").build())
                    .notes("Legacy name mutator switches to _scoped when ID_SCOPE is installed.")
                    .build()));
    public static final SednaGunConfig GUN_HEAVY_REVOLVER_LILMAC = register(exact("gun_heavy_revolver_lilmac",
            "XFactory44", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory44.init(); scope_lilmac is hbm:textures/misc/scope_44.png.",
            standardMode(0, 31000.0F, 10, 23, Crosshair.L_CLASSIC, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_NOPIP", "XFactory44.LAMBDA_LILMAC_ANIMS")
                    .scopeTexture("hbm:textures/misc/scope_44.png")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HEAVY_REVOLVER_LILMAC)
                            .damage(30.0F).delay(14).reload(46).jam(23)
                            .sound("NTMSounds.GUN_HEAVY_REVOLVER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_NOPIP").build())
                    .build()));
    public static final SednaGunConfig GUN_HEAVY_REVOLVER_PROTEGE = register(exact(
            "gun_heavy_revolver_protege", "XFactory44", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory44.init().",
            standardMode(0, 31000.0F, 10, 23, Crosshair.L_CLASSIC, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_NOPIP", "XFactory44.LAMBDA_LILMAC_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HEAVY_REVOLVER_PROTEGE)
                            .damage(30.0F).delay(14).reload(46).jam(23)
                            .sound("NTMSounds.GUN_HEAVY_REVOLVER_FIRE", 1.0F, 0.8F)
                            .offset(0.75D, -0.0625D, -0.3125D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_NOPIP").build())
                    .build()));
    public static final SednaGunConfig GUN_HANGMAN = register(exact("gun_hangman", "XFactory44", "ItemGunBaseNT",
            WeaponQuality.LEGENDARY, "Exact fields copied from XFactory44.init().",
            standardMode(0, 600.0F, 10, 31, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_HANGMAN", "XFactory44.LAMBDA_HANGMAN_ANIMS")
                    .inspectCancel(false)
                    .pressSecondary("XFactory44.SMACK_A_FUCKER")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_HANGMAN)
                            .damage(25.0F).delay(10).reload(46).jam(23)
                            .sound("NTMSounds.GUN_HEAVY_REVOLVER_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory44.LAMBDA_RECOIL_HANGMAN").build())
                    .build()));

    public static final SednaGunConfig GUN_GREASEGUN = register(exact("gun_greasegun", "XFactory9mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory9mm.init(); name mutator switches to _m3 when ID_GREASEGUN_CLEAN is installed.",
            standardMode(0, 3000.0F, 20, 31, Crosshair.L_CIRCLE, "XFactory9mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_GREASEGUN", "XFactory9mm.LAMBDA_GREASEGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_GREASEGUN)
                            .damage(3.0F).delay(4).dryDelay(40).auto(true).spread(0.015F)
                            .reload(60).jam(55).sound("NTMSounds.GUN_GREASEGUN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory9mm.LAMBDA_RECOIL_GREASEGUN").build())
                    .build()));
    public static final SednaGunConfig GUN_LAG = register(exact("gun_lag", "XFactory9mm", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, "Exact fields copied from XFactory9mm.init(); custom fire can self-damage during inspect.",
            standardMode(0, 1700.0F, 7, 31, Crosshair.CIRCLE, "XFactory9mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_LAG", "XFactory9mm.LAMBDA_LAG_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LAG)
                            .damage(25.0F).delay(4).dryDelay(10).spread(0.005F)
                            .reload(53).jam(44).sound("NTMSounds.GUN_PISTOL_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "XFactory9mm.LAMBDA_FIRE_LAG")
                            .recoil("XFactory9mm.LAMBDA_RECOIL_LAG").build())
                    .build()));
    public static final SednaGunConfig GUN_UZI = register(exact("gun_uzi", "XFactory9mm", "ItemGunBaseNT",
            WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory9mm.init(); name mutator switches to _richter when ID_SILENCER is installed.",
            standardMode(0, 3000.0F, 15, 31, Crosshair.CIRCLE, "XFactory9mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_UZI", "XFactory9mm.LAMBDA_UZI_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_UZI)
                            .damage(3.0F).delay(2).dryDelay(25).auto(true).spread(0.005F)
                            .reload(55).jam(50).sound("NTMSounds.GUN_UZI_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory9mm.LAMBDA_RECOIL_UZI").build())
                    .build()));
    public static final SednaGunConfig GUN_UZI_AKIMBO = register(exact("gun_uzi_akimbo", "XFactory9mm",
            "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory9mm.init(); legacy source creates two GunConfig instances.",
            new ModeBuilder(0)
                    .durability(3000.0F).draw(15).inspect(31).crosshair(Crosshair.CIRCLE)
                    .smoke("XFactory9mm.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_UZI_AKIMBO")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory9mm.LAMBDA_UZI_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_UZI_AKIMBO_PRIMARY, 0)
                            .damage(3.0F).spreadHipfire(0.0F).delay(2).dryDelay(25).auto(true)
                            .spread(0.005F).reload(55).jam(50)
                            .sound("NTMSounds.GUN_UZI_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, 0.375D).standardFire()
                            .recoil("XFactory9mm.LAMBDA_RECOIL_UZI").build())
                    .notes("No tertiary aim handler is installed in the first akimbo Uzi GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(3000.0F).draw(15).inspect(31).crosshair(Crosshair.CIRCLE)
                    .smoke("XFactory9mm.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_UZI_AKIMBO")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("XFactory9mm.LAMBDA_SECOND_UZI")
                    .animation("XFactory9mm.LAMBDA_UZI_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_UZI_AKIMBO_SECONDARY, 0)
                            .damage(3.0F).spreadHipfire(0.0F).delay(2).dryDelay(25).auto(true)
                            .spread(0.005F).reload(55).jam(50)
                            .sound("NTMSounds.GUN_UZI_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.375D).standardFire()
                            .recoil("XFactory9mm.LAMBDA_RECOIL_UZI").build())
                    .notes("No tertiary aim handler is installed in the second akimbo Uzi GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));

    public static final SednaGunConfig GUN_AM180 = register(exact("gun_am180", "XFactory22lr", "ItemGunBaseNT",
            WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory22lr.init(); name mutator switches to _silenced when ID_SILENCER is installed.",
            standardMode(0, 4425.0F, 15, 38, Crosshair.L_CIRCLE, "XFactory22lr.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_AM180", "XFactory22lr.LAMBDA_AM180_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AM180)
                            .damage(2.0F).delay(1).dryDelay(10).auto(true).spread(0.01F)
                            .reload(66).jam(30).sound("NTMSounds.GUN_GREASEGUN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactory22lr.LAMBDA_RECOIL_AM180").build())
                    .build()));
    public static final SednaGunConfig GUN_STAR_F = register(exact("gun_star_f", "XFactory22lr",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory22lr.init(); name mutator switches to _silenced when ID_SILENCER is installed.",
            standardMode(0, 375.0F, 15, 38, Crosshair.CIRCLE, "XFactory22lr.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_STAR_F", "XFactory22lr.LAMBDA_STAR_F_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_STAR_F)
                            .damage(12.5F).delay(5).dryDelay(17).spread(0.01F)
                            .reload(40).jam(32).sound("NTMSounds.GUN_STARF_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactory22lr.LAMBDA_RECOIL_STAR_F").build())
                    .build()));
    public static final SednaGunConfig GUN_STAR_F_AKIMBO = register(exact("gun_star_f_akimbo", "XFactory22lr",
            "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory22lr.init(); legacy source creates two GunConfig instances.",
            new ModeBuilder(0)
                    .durability(375.0F).draw(15).inspect(38).crosshair(Crosshair.CIRCLE)
                    .smoke("XFactory22lr.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_STAR_F_AKIMBO")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory22lr.LAMBDA_STAR_F_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_STAR_F_AKIMBO_PRIMARY, 0)
                            .damage(12.5F).delay(5).dryDelay(17).spread(0.01F)
                            .reload(40).jam(32).sound("NTMSounds.GUN_STARF_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, 0.25D).standardFire()
                            .recoil("XFactory22lr.LAMBDA_RECOIL_STAR_F").build())
                    .notes("No tertiary aim handler is installed in the first akimbo Star-F GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(375.0F).draw(15).inspect(38).crosshair(Crosshair.CIRCLE)
                    .smoke("XFactory22lr.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_STAR_F_AKIMBO")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory22lr.LAMBDA_STAR_F_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_STAR_F_AKIMBO_SECONDARY, 0)
                            .damage(12.5F).delay(5).dryDelay(17).spread(0.01F)
                            .reload(40).jam(32).sound("NTMSounds.GUN_STARF_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.25D).standardFire()
                            .recoil("XFactory22lr.LAMBDA_RECOIL_STAR_F").build())
                    .notes("No tertiary aim handler is installed in the second akimbo Star-F GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));

    public static final SednaGunConfig GUN_G3 = register(exact("gun_g3", "XFactory556mm", "ItemGunBaseNT",
            WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory556mm.init(); secondary press is also standard click and the name mutator can switch to _infiltrator or _a3.",
            standardMode(0, 3000.0F, 10, 33, Crosshair.CIRCLE, "XFactory556mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_G3", "XFactory556mm.LAMBDA_G3_ANIMS")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_SECONDARY")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_G3)
                            .damage(5.0F).delay(2).dryDelay(15).auto(true)
                            .reload(50).jam(47).sound("NTMSounds.GUN_ASSAULT_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory556mm.LAMBDA_RECOIL_G3").build())
                    .build()));
    public static final SednaGunConfig GUN_G3_ZEBRA = register(exact("gun_g3_zebra", "XFactory556mm",
            "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory556mm.init(); incendiary ammo set is linked through the magazine config.",
            standardMode(0, 6000.0F, 10, 33, Crosshair.CIRCLE, "XFactory556mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_G3", "XFactory556mm.LAMBDA_G3_ANIMS")
                    .scopeTexture("hbm:textures/misc/scope_bolt.png")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_SECONDARY")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_G3_ZEBRA)
                            .damage(7.5F).delay(2).dryDelay(15).auto(true).spreadHipfire(0.01F)
                            .reload(50).jam(47).sound("NTMSounds.GUN_RIFLE_SILENCER", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory556mm.LAMBDA_RECOIL_ZEBRA").build())
                    .build()));
    public static final SednaGunConfig GUN_STG77 = register(exact("gun_stg77", "XFactory556mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory556mm.init(); custom decider allows secondary-held auto refire.",
            new ModeBuilder(0)
                    .durability(3000.0F).draw(10).inspect(125).crosshair(Crosshair.CIRCLE)
                    .scopeTexture("hbm:textures/misc/scope_bolt.png")
                    .smoke("XFactory556mm.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_STG77")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressTertiary("Lego.LAMBDA_TOGGLE_AIM")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("XFactory556mm.LAMBDA_STG77_DECIDER")
                    .animation("XFactory556mm.LAMBDA_STG77_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_STG77)
                            .damage(10.0F).delay(2).dryDelay(15).auto(true)
                            .reload(46).jam(0).sound("NTMSounds.GUN_ASSAULT_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory556mm.LAMBDA_RECOIL_STG").build())
                    .build()));

    public static final SednaGunConfig GUN_CARBINE = register(exact("gun_carbine", "XFactory762mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory762mm.init().",
            standardMode(0, 3000.0F, 10, 31, Crosshair.CIRCLE, "XFactory762mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_CARBINE", "XFactory762mm.LAMBDA_CARBINE_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_CARBINE)
                            .damage(15.0F).delay(5).dryDelay(15).spread(0.0F)
                            .reload(30, 0, 15, 0).jam(60).sound("NTMSounds.GUN_POWDER_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_CARBINE").build())
                    .build()));
    public static final SednaGunConfig GUN_MINIGUN = register(exact("gun_minigun", "XFactory762mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory762mm.init().",
            standardMode(0, 50000.0F, 20, 20, Crosshair.L_CIRCLE, "XFactory762mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_MINIGUN", "XFactory762mm.LAMBDA_MINIGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MINIGUN)
                            .damage(6.0F).delay(1).dryDelay(15).auto(true).spread(0.01F)
                            .sound("NTMSounds.GUN_MINIGUN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_MINIGUN").build())
                    .build()));
    public static final SednaGunConfig GUN_MINIGUN_LACUNAE = register(exact("gun_minigun_lacunae",
            "XFactory762mm", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory762mm.init(); energy beam ammo set is linked through the magazine config.",
            standardMode(0, 50000.0F, 20, 20, Crosshair.L_CIRCLE, "",
                    "Orchestras.ORCHESTRA_MINIGUN", "XFactory762mm.LAMBDA_MINIGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MINIGUN_LACUNAE)
                            .damage(12.0F).delay(1).dryDelay(15).auto(true).reload(15).spread(0.01F)
                            .sound("NTMSounds.GUN_LASER_GATLING_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_LACUNAE").build())
                    .build()));
    public static final SednaGunConfig GUN_MINIGUN_DUAL = register(exact("gun_minigun_dual", "XFactory762mm",
            "ItemGunBaseNT", WeaponQuality.DEBUG,
            "Exact fields copied from XFactory762mm.init(); legacy source creates two GunConfig instances for a dual belt debug gun.",
            new ModeBuilder(0)
                    .durability(50000.0F).draw(20).inspect(20).crosshair(Crosshair.L_CIRCLE)
                    .smoke("XFactory762mm.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_MINIGUN_DUAL")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory762mm.LAMBDA_MINIGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MINIGUN_DUAL_PRIMARY, 0)
                            .damage(6.0F).delay(1).dryDelay(15).auto(true).spread(0.01F)
                            .sound("NTMSounds.GUN_MINIGUN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, 0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_MINIGUN").build())
                    .notes("No tertiary aim handler is installed in the first dual-minigun GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(50000.0F).draw(20).inspect(20).crosshair(Crosshair.L_CIRCLE)
                    .smoke("XFactory762mm.LAMBDA_SMOKE").orchestra("Orchestras.ORCHESTRA_MINIGUN_DUAL")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("XFactory762mm.LAMBDA_SECOND_MINIGUN")
                    .animation("XFactory762mm.LAMBDA_MINIGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MINIGUN_DUAL_SECONDARY, 0)
                            .damage(6.0F).delay(1).dryDelay(15).auto(true).spread(0.01F)
                            .sound("NTMSounds.GUN_MINIGUN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_MINIGUN").build())
                    .notes("No tertiary aim handler is installed in the second dual-minigun GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));
    public static final SednaGunConfig GUN_MAS36 = register(exact("gun_mas36", "XFactory762mm",
            "ItemGunBaseNT", WeaponQuality.LEGENDARY, "Exact fields copied from XFactory762mm.init().",
            standardMode(0, 5000.0F, 20, 31, Crosshair.CIRCLE, "XFactory762mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_MAS36", "XFactory762mm.LAMBDA_MAS36_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MAS36)
                            .damage(30.0F).delay(25).dryDelay(25).spread(0.0F)
                            .reload(43).jam(43).sound("NTMSounds.GUN_HEAVY_RIFLE_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.25D).standardFire()
                            .recoil("XFactory762mm.LAMBDA_RECOIL_CARBINE").build())
                    .build()));

    public static final SednaGunConfig GUN_MARESLEG = register(simple("gun_maresleg", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23, true,
            "LAMBDA_MARESLEG_ANIMS", "Orchestras.ORCHESTRA_MARESLEG"));
    public static final SednaGunConfig GUN_MARESLEG_AKIMBO = register(simple("gun_maresleg_akimbo",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.B_SIDE, Crosshair.DUAL, 300.0F, 10, 23, true,
            "LAMBDA_MARESLEG_AKIMBO_ANIMS", "Orchestras.ORCHESTRA_MARESLEG",
            "Legacy source creates two akimbo GunConfig instances."));
    public static final SednaGunConfig GUN_MARESLEG_BROKEN = register(simple("gun_maresleg_broken",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.LEGENDARY, Crosshair.CIRCLE, 300.0F, 10, 23, true,
            "LAMBDA_MARESLEG_ANIMS", "Orchestras.ORCHESTRA_MARESLEG"));
    public static final SednaGunConfig GUN_LIBERATOR = register(simple("gun_liberator", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23, true,
            "LAMBDA_LIBERATOR_ANIMS", "Orchestras.ORCHESTRA_LIBERATOR"));
    public static final SednaGunConfig GUN_SPAS12 = register(simple("gun_spas12", "XFactory12ga", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23, true,
            "LAMBDA_SPAS12_ANIMS", "Orchestras.ORCHESTRA_SPAS12"));
    public static final SednaGunConfig GUN_AUTOSHOTGUN = register(simple("gun_autoshotgun", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AUTOSHOTGUN_ANIMS", "Orchestras.ORCHESTRA_AUTOSHOTGUN"));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_SHREDDER = register(simple("gun_autoshotgun_shredder",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.B_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AUTOSHOTGUN_ANIMS", "Orchestras.ORCHESTRA_AUTOSHOTGUN"));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_SEXY = register(simple("gun_autoshotgun_sexy",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.LEGENDARY, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AUTOSHOTGUN_ANIMS", "Orchestras.ORCHESTRA_AUTOSHOTGUN"));

    public static final SednaGunConfig GUN_DOUBLE_BARREL = register(simple("gun_double_barrel", "XFactory10ga",
            "ItemGunBaseNT", WeaponQuality.SPECIAL, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_DOUBLE_BARREL_ANIMS", "Orchestras.ORCHESTRA_DOUBLE_BARREL",
            "Secondary press uses XFactory10ga.LAMBDA_DOUBLE_SECONDARY."));
    public static final SednaGunConfig GUN_DOUBLE_BARREL_SACRED_DRAGON = register(simple(
            "gun_double_barrel_sacred_dragon", "XFactory10ga", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            Crosshair.CIRCLE, 300.0F, 10, 23, "LAMBDA_DOUBLE_BARREL_ANIMS",
            "Orchestras.ORCHESTRA_DOUBLE_BARREL", "Secondary press uses XFactory10ga.LAMBDA_DOUBLE_SECONDARY."));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_HERETIC = register(simple("gun_autoshotgun_heretic",
            "XFactory10ga", "ItemGunBaseNT", WeaponQuality.DEBUG, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AUTOSHOTGUN_HERETIC_ANIMS", "Orchestras.ORCHESTRA_AUTOSHOTGUN"));

    public static final SednaGunConfig GUN_FLAREGUN = register(simple("gun_flaregun", "XFactory40mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_FLAREGUN_ANIMS", "Orchestras.ORCHESTRA_FLAREGUN"));
    public static final SednaGunConfig GUN_CONGOLAKE = register(simple("gun_congolake", "XFactory40mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_CONGOLAKE_ANIMS", "Orchestras.ORCHESTRA_CONGOLAKE"));
    public static final SednaGunConfig GUN_MK108 = register(simple("gun_mk108", "XFactory40mm", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_MK108_ANIMS", "Orchestras.ORCHESTRA_MK108"));

    public static final SednaGunConfig GUN_AMAT = register(simple("gun_amat", "XFactory50", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AMAT_ANIMS", "Orchestras.ORCHESTRA_AMAT"));
    public static final SednaGunConfig GUN_AMAT_SUBTLETY = register(simple("gun_amat_subtlety", "XFactory50",
            "ItemGunBaseNT", WeaponQuality.LEGENDARY, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AMAT_ANIMS", "Orchestras.ORCHESTRA_AMAT"));
    public static final SednaGunConfig GUN_AMAT_PENANCE = register(simple("gun_amat_penance", "XFactory50",
            "ItemGunBaseNT", WeaponQuality.LEGENDARY, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_AMAT_ANIMS", "Orchestras.ORCHESTRA_AMAT"));
    public static final SednaGunConfig GUN_M2 = register(simple("gun_m2", "XFactory50", "ItemGunBaseNT",
            WeaponQuality.A_SIDE, Crosshair.CIRCLE, 5000.0F, 20, 30,
            "LAMBDA_M2_ANIMS", "Orchestras.ORCHESTRA_M2"));

    public static final SednaGunConfig GUN_BOLTER = register(simple("gun_bolter", "XFactory75Bolt",
            "ItemGunBaseNT", WeaponQuality.SPECIAL, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_BOLTER_ANIMS", "Orchestras.ORCHESTRA_BOLTER"));
    public static final SednaGunConfig GUN_ABERRATOR = register(simple("gun_aberrator", "XFactory35800",
            "ItemGunBaseNT", WeaponQuality.SECRET, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_ABERRATOR_ANIMS", "Orchestras.ORCHESTRA_ABERRATOR"));
    public static final SednaGunConfig GUN_ABERRATOR_EOTT = register(simple("gun_aberrator_eott",
            "XFactory35800", "ItemGunBaseNT", WeaponQuality.SECRET, Crosshair.CIRCLE, 300.0F, 10, 23,
            "LAMBDA_ABERRATOR_EOTT_ANIMS", "Orchestras.ORCHESTRA_ABERRATOR",
            "Legacy source creates two GunConfig instances."));

    public static final SednaGunConfig GUN_PANZERSCHRECK = register(simple("gun_panzerschreck", "XFactoryRocket",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCUMFLEX, 300.0F, 20, 30,
            "LAMBDA_PANZERSCHRECK_ANIMS", "Orchestras.ORCHESTRA_RPG"));
    public static final SednaGunConfig GUN_STINGER = register(simple("gun_stinger", "XFactoryRocket",
            "ItemGunStinger", WeaponQuality.SPECIAL, Crosshair.L_CIRCUMFLEX, 300.0F, 20, 30,
            "LAMBDA_STINGER_ANIMS", "Orchestras.ORCHESTRA_RPG",
            "Secondary press/release handlers manage the old Stinger lock-on flow."));
    public static final SednaGunConfig GUN_QUADRO = register(simple("gun_quadro", "XFactoryRocket",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCUMFLEX, 300.0F, 20, 30,
            "LAMBDA_QUADRO_ANIMS", "Orchestras.ORCHESTRA_RPG"));
    public static final SednaGunConfig GUN_MISSILE_LAUNCHER = register(simple("gun_missile_launcher",
            "XFactoryRocket", "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCUMFLEX, 300.0F, 20, 30,
            "LAMBDA_MISSILE_LAUNCHER_ANIMS", "Orchestras.ORCHESTRA_RPG",
            "Primary press is XFactoryRocket.LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS."));

    public static final SednaGunConfig GUN_FLAMER = register(simple("gun_flamer", "XFactoryFlamer",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCLE, 5000.0F, 10, 55,
            "LAMBDA_FLAMER_ANIMS", "Orchestras.ORCHESTRA_FLAMER"));
    public static final SednaGunConfig GUN_FLAMER_TOPAZ = register(simple("gun_flamer_topaz", "XFactoryFlamer",
            "ItemGunBaseNT", WeaponQuality.B_SIDE, Crosshair.L_CIRCLE, 5000.0F, 10, 55,
            "LAMBDA_FLAMER_ANIMS", "Orchestras.ORCHESTRA_FLAMER"));
    public static final SednaGunConfig GUN_FLAMER_DAYBREAKER = register(simple("gun_flamer_daybreaker",
            "XFactoryFlamer", "ItemGunBaseNT", WeaponQuality.LEGENDARY, Crosshair.L_CIRCLE, 5000.0F, 10, 55,
            "LAMBDA_FLAMER_ANIMS", "Orchestras.ORCHESTRA_FLAMER"));
    public static final SednaGunConfig GUN_CHEMTHROWER = register(simple("gun_chemthrower", "XFactoryFlamer",
            "ItemGunChemthrower", WeaponQuality.A_SIDE, Crosshair.L_CIRCLE, 5000.0F, 10, 55,
            "LAMBDA_CHEMTHROWER_ANIMS", "Orchestras.ORCHESTRA_FLAMER",
            "Legacy ItemGunChemthrower fills MagazineFluid from fluid containers."));

    public static final SednaGunConfig GUN_TESLA_CANNON = register(simple("gun_tesla_cannon", "XFactoryEnergy",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCLE, 3000.0F, 20, 30,
            "LAMBDA_TESLA_ANIMS", "Orchestras.ORCHESTRA_TESLA"));
    public static final SednaGunConfig GUN_LASER_PISTOL = register(simple("gun_laser_pistol", "XFactoryEnergy",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CROSS, 3000.0F, 10, 23,
            "LAMBDA_LASER_PISTOL_ANIMS", "Orchestras.ORCHESTRA_LASER"));
    public static final SednaGunConfig GUN_LASER_PISTOL_PEW_PEW = register(simple("gun_laser_pistol_pew_pew",
            "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.B_SIDE, Crosshair.L_CROSS, 3000.0F, 10, 23,
            "LAMBDA_LASER_PISTOL_ANIMS", "Orchestras.ORCHESTRA_LASER"));
    public static final SednaGunConfig GUN_LASER_PISTOL_MORNING_GLORY = register(simple(
            "gun_laser_pistol_morning_glory", "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            Crosshair.L_CROSS, 3000.0F, 10, 23, "LAMBDA_LASER_PISTOL_ANIMS", "Orchestras.ORCHESTRA_LASER"));
    public static final SednaGunConfig GUN_LASRIFLE = register(simple("gun_lasrifle", "XFactoryEnergy",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CROSS, 3000.0F, 10, 23,
            "LAMBDA_LASRIFLE_ANIMS", "Orchestras.ORCHESTRA_LASER"));

    public static final SednaGunConfig GUN_TAU = register(simple("gun_tau", "XFactoryAccelerator",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCLE, 5000.0F, 20, 30,
            "LAMBDA_TAU_ANIMS", "Orchestras.ORCHESTRA_TAU",
            "Primary release and secondary press use Tau charge-specific handlers; secondary charge magazine has key gun_tau.charge."));
    public static final SednaGunConfig GUN_COILGUN = register(simple("gun_coilgun", "XFactoryAccelerator",
            "ItemGunBaseNT", WeaponQuality.SPECIAL, Crosshair.L_CIRCLE, 5000.0F, 20, 30,
            "LAMBDA_COILGUN_ANIMS", "Orchestras.ORCHESTRA_COILGUN"));
    public static final SednaGunConfig GUN_NI4NI = register(simple("gun_n_i_4_n_i", "XFactoryAccelerator",
            "ItemGunNI4NI", WeaponQuality.SPECIAL, Crosshair.L_CIRCLE, 0.0F, 20, 30,
            "LAMBDA_NI4NI_ANIMS", "Orchestras.ORCHESTRA_COILGUN",
            "Secondary press uses ItemGunNI4NI special behavior and MagazineInfinite."));

    public static final SednaGunConfig GUN_FATMAN = register(simple("gun_fatman", "XFactoryCatapult",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, Crosshair.L_CIRCUMFLEX, 300.0F, 20, 30,
            true, false, "LAMBDA_FATMAN_ANIMS", "Orchestras.ORCHESTRA_FATMAN",
            "Legacy setDefaultAmmoExpensive is recorded as data only; 528/expensive mode is not migrated."));
    public static final SednaGunConfig GUN_FOLLY = register(simple("gun_folly", "XFactoryFolly", "ItemGunBaseNT",
            WeaponQuality.SECRET, Crosshair.NONE, 0.0F, 40, 0,
            false, false, "LAMBDA_FOLLY_ANIMS", "Orchestras.ORCHESTRA_FOLLY",
            "Tertiary aim toggle is XFactoryFolly.LAMBDA_TOGGLE_AIM."));
    public static final SednaGunConfig GUN_FIREEXT = register(simple("gun_fireext", "XFactoryTool",
            "ItemGunBaseNT", WeaponQuality.UTILITY, Crosshair.L_CIRCLE, 5000.0F, 10, 55,
            true, false, "", "Orchestras.ORCHESTRA_FIREEXT", "Fire extinguisher tool gun."));
    public static final SednaGunConfig GUN_CHARGE_THROWER = register(simple("gun_charge_thrower", "XFactoryTool",
            "ItemGunChargeThrower", WeaponQuality.UTILITY, Crosshair.L_CIRCUMFLEX, 3000.0F, 10, 55,
            true, false, "LAMBDA_CT_ANIMS", "Orchestras.ORCHESTRA_CHARGE_THROWER",
            "Charge thrower stores last hook entity id in ItemGunChargeThrower runtime NBT."));
    public static final SednaGunConfig GUN_DRILL = register(simple("gun_drill", "XFactoryDrill", "ItemGunDrill",
            WeaponQuality.UTILITY, Crosshair.NONE, 0.0F, 0, 0,
            false, false, "LAMBDA_DRILL_ANIMS", "",
            "Tool/engine gun. Drill runtime, block breaking and engine fuel handling are deferred to tool migration."));

    public static Optional<SednaGunConfig> byName(String legacyName) {
        return Optional.ofNullable(BY_NAME.get(legacyName));
    }

    public static Collection<SednaGunConfig> all() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    public static List<String> missingMagazineKeys() {
        List<String> missing = new ArrayList<>();
        for (SednaGunConfig config : BY_NAME.values()) {
            missing.addAll(config.missingMagazineKeys());
        }
        return List.copyOf(missing);
    }

    private static SednaGunConfig exact(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, String notes, SednaGunConfig.GunModeConfig... modes) {
        SednaGunConfig.Builder builder = SednaGunConfig.builder(legacyName, sourceClassName, itemClassName, quality)
                .notes(notes);
        for (SednaGunConfig.GunModeConfig mode : modes) {
            builder.config(mode);
        }
        return builder.build();
    }

    private static ModeBuilder standardMode(int index, float durability, int draw, int inspect, Crosshair crosshair,
            String smoke, String orchestra, String animation) {
        return new ModeBuilder(index)
                .durability(durability)
                .draw(draw)
                .inspect(inspect)
                .crosshair(crosshair)
                .smoke(smoke)
                .orchestra(orchestra)
                .standardConfiguration()
                .animation(animation);
    }

    private static SednaGunConfig simple(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, Crosshair crosshair, float durability, int draw, int inspect,
            String animationProfile, String orchestraName) {
        return simple(legacyName, sourceClassName, itemClassName, quality, crosshair, durability, draw, inspect,
                false, animationProfile, orchestraName, "");
    }

    private static SednaGunConfig simple(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, Crosshair crosshair, float durability, int draw, int inspect,
            String animationProfile, String orchestraName, String notes) {
        return simple(legacyName, sourceClassName, itemClassName, quality, crosshair, durability, draw, inspect,
                false, animationProfile, orchestraName, notes);
    }

    private static SednaGunConfig simple(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, Crosshair crosshair, float durability, int draw, int inspect,
            boolean reloadSequential, String animationProfile, String orchestraName) {
        return simple(legacyName, sourceClassName, itemClassName, quality, crosshair, durability, draw, inspect,
                reloadSequential, animationProfile, orchestraName, "");
    }

    private static SednaGunConfig simple(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, Crosshair crosshair, float durability, int draw, int inspect,
            boolean reloadSequential, String animationProfile, String orchestraName, String notes) {
        return simple(legacyName, sourceClassName, itemClassName, quality, crosshair, durability, draw, inspect,
                false, reloadSequential, animationProfile, orchestraName, notes);
    }

    private static SednaGunConfig simple(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality, Crosshair crosshair, float durability, int draw, int inspect,
            boolean reloadChangeType, boolean reloadSequential, String animationProfile, String orchestraName,
            String notes) {
        String pendingFieldNotes = "Broad registration preserves source owner, item class, quality, magazine keys "
                + "and default ammo links. Per-gun draw/inspect/crosshair/animation/orchestra and receiver numeric "
                + "values are intentionally left blank until each factory block is filled from exact source lines.";
        ModeBuilder mode = new ModeBuilder(0)
                .durability(0.0F)
                .draw(0)
                .inspect(0)
                .crosshair(Crosshair.NONE)
                .reloadChangeType(false)
                .reloadSequential(false)
                .standardConfiguration()
                .notes(pendingFieldNotes);

        for (SednaMagazineConfig magazine : LegacySednaMagazineConfigs.byOwner(legacyName)) {
            mode.receiver(SednaReceiverConfig.fromMagazine(magazine)
                    .standardFire()
                    .notes("Receiver numeric fire/reload values are preserved when filled in later source passes; magazine key is authoritative for ammo/NBT.")
                    .build());
        }

        return SednaGunConfig.builder(legacyName, sourceClassName, itemClassName, quality)
                .config(mode.build())
                .notes(mergeNotes(notes, pendingFieldNotes))
                .build();
    }

    private static String mergeNotes(String notes, String fallback) {
        if (notes == null || notes.isEmpty()) {
            return fallback;
        }
        return notes + " " + fallback;
    }

    private static SednaGunConfig register(SednaGunConfig config) {
        BY_NAME.put(config.legacyName(), config);
        return config;
    }

    private LegacySednaGunConfigs() {
    }
}
