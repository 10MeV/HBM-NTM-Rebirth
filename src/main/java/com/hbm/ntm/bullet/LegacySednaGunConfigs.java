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
    public static final String HUD_COMPONENT_DURABILITY = "HUD_COMPONENT_DURABILITY";
    public static final String HUD_COMPONENT_DURABILITY_MIRROR = "HUD_COMPONENT_DURABILITY_MIRROR";
    public static final String HUD_COMPONENT_AMMO = "HUD_COMPONENT_AMMO";
    public static final String HUD_COMPONENT_AMMO_MIRROR = "HUD_COMPONENT_AMMO_MIRROR";
    public static final String HUD_COMPONENT_AMMO_NOCOUNTER = "HUD_COMPONENT_AMMO_NOCOUNTER";
    public static final String HUD_COMPONENT_AMMO_SECOND = "HUD_COMPONENT_AMMO_SECOND";

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

    public static final SednaGunConfig GUN_MARESLEG = register(exact("gun_maresleg", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory12ga.init(); name mutator switches to _short when ID_SAWED_OFF is installed.",
            standardMode(0, 600.0F, 10, 39, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_MARESLEG", "XFactory12ga.LAMBDA_MARESLEG_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MARESLEG)
                            .damage(16.0F).delay(20).reload(22, 10, 13, 0).jam(24)
                            .sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_MARESLEG").build())
                    .build()));
    public static final SednaGunConfig GUN_MARESLEG_AKIMBO = register(exact("gun_maresleg_akimbo",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory12ga.init(); legacy source creates two GunConfig instances.",
            new ModeBuilder(0)
                    .durability(600.0F).draw(5).inspect(39).reloadSequential(true)
                    .crosshair(Crosshair.L_CIRCLE).smoke("Lego.LAMBDA_STANDARD_SMOKE")
                    .orchestra("Orchestras.ORCHESTRA_MARESLEG_AKIMBO")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory12ga.LAMBDA_MARESLEG_SHORT_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_MARESLEG_AKIMBO_PRIMARY, 0)
                            .damage(16.0F).spreadHipfire(0.0F).spreadAmmo(1.35F).delay(20)
                            .reload(22, 10, 13, 0).jam(24)
                            .sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, 0.1875D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_MARESLEG").build())
                    .notes("No tertiary aim handler is installed in the first akimbo Maresleg GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(600.0F).draw(5).inspect(39).reloadSequential(true)
                    .crosshair(Crosshair.L_CIRCLE).smoke("Lego.LAMBDA_STANDARD_SMOKE")
                    .orchestra("Orchestras.ORCHESTRA_MARESLEG_AKIMBO")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory12ga.LAMBDA_MARESLEG_SHORT_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_MARESLEG_AKIMBO_SECONDARY, 0)
                            .damage(16.0F).spreadHipfire(0.0F).spreadAmmo(1.35F).delay(20)
                            .reload(22, 10, 13, 0).jam(24)
                            .sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_MARESLEG").build())
                    .notes("No tertiary aim handler is installed in the second akimbo Maresleg GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));
    public static final SednaGunConfig GUN_MARESLEG_BROKEN = register(exact("gun_maresleg_broken",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory12ga.init(); receiver uses Lego.LAMBDA_NOWEAR_FIRE.",
            standardMode(0, 0.0F, 5, 39, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_MARESLEG_SHORT", "XFactory12ga.LAMBDA_MARESLEG_SHORT_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MARESLEG_BROKEN)
                            .damage(48.0F).spreadAmmo(1.15F).delay(20)
                            .reload(22, 10, 13, 0).jam(24)
                            .sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .recoil("XFactory12ga.LAMBDA_RECOIL_MARESLEG").build())
                    .build()));
    public static final SednaGunConfig GUN_LIBERATOR = register(exact("gun_liberator", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory12ga.init().",
            standardMode(0, 200.0F, 20, 21, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_LIBERATOR", "XFactory12ga.LAMBDA_LIBERATOR_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LIBERATOR)
                            .damage(16.0F).delay(20).rounds(4).reload(25, 15, 7, 0).jam(45)
                            .sound("NTMSounds.GUN_LIBERATOR_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_LIBERATOR").build())
                    .build()));
    public static final SednaGunConfig GUN_SPAS12 = register(exact("gun_spas12", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory12ga.init(); secondary press is XFactory12ga.LAMBDA_SPAS_SECONDARY and tertiary aim is disabled.",
            standardMode(0, 600.0F, 20, 39, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_SPAS", "XFactory12ga.LAMBDA_SPAS_ANIMS")
                    .reloadSequential(true).reloadChangeType(true)
                    .pressSecondary("XFactory12ga.LAMBDA_SPAS_SECONDARY")
                    .pressTertiary("")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_SPAS12)
                            .damage(32.0F).spreadHipfire(0.0F).delay(20)
                            .reload(5, 10, 10, 10, 0).jam(36)
                            .sound("NTMSounds.GUN_SPAS_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_MARESLEG").build())
                    .build()));
    public static final SednaGunConfig GUN_AUTOSHOTGUN = register(exact("gun_autoshotgun", "XFactory12ga",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory12ga.init().",
            standardMode(0, 2000.0F, 10, 33, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_SHREDDER", "XFactory12ga.LAMBDA_SHREDDER_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AUTOSHOTGUN)
                            .damage(48.0F).delay(10).auto(true).autoAfterDry(true)
                            .dryfireAfterAuto(true).reload(44).jam(19)
                            .sound("NTMSounds.GUN_SHREDDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.125D, -0.25D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_AUTOSHOTGUN").build())
                    .build()));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_SHREDDER = register(exact(
            "gun_autoshotgun_shredder", "XFactory12ga", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory12ga.init(); belt magazine carries Shredder beam configs.",
            standardMode(0, 2000.0F, 10, 33, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_SHREDDER", "XFactory12ga.LAMBDA_SHREDDER_ANIMS")
                    .reloadSequential(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AUTOSHOTGUN_SHREDDER)
                            .damage(50.0F).delay(10).auto(true).autoAfterDry(true)
                            .dryfireAfterAuto(true).reload(44).jam(19)
                            .sound("NTMSounds.GUN_SHREDDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.125D, -0.25D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_AUTOSHOTGUN").build())
                    .build()));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_SEXY = register(exact("gun_autoshotgun_sexy",
            "XFactory12ga", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory12ga.init(); hideCrosshair=false and inspectCancel=false.",
            standardMode(0, 5000.0F, 20, 65, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_SHREDDER_SEXY", "XFactory12ga.LAMBDA_SEXY_ANIMS")
                    .reloadSequential(true).inspectCancel(false).hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AUTOSHOTGUN_SEXY)
                            .damage(64.0F).delay(4).auto(true).dryfireAfterAuto(true)
                            .reload(110).jam(19).sound("NTMSounds.GUN_SHREDDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.125D, -0.25D).standardFire()
                            .recoil("XFactory12ga.LAMBDA_RECOIL_SEXY").build())
                    .build()));

    public static final SednaGunConfig GUN_DOUBLE_BARREL = register(exact("gun_double_barrel",
            "XFactory10ga", "ItemGunBaseNT", WeaponQuality.SPECIAL,
            "Exact fields copied from XFactory10ga.init(); secondary press manually fires the same receiver.",
            standardMode(0, 1000.0F, 10, 39, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_DOUBLE_BARREL", "XFactory10ga.LAMBDA_DOUBLE_BARREL_ANIMS")
                    .pressSecondary("XFactory10ga.LAMBDA_DOUBLE_SECONDARY")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_DOUBLE_BARREL)
                            .damage(30.0F).rounds(2).delay(10).reload(41).reloadOnEmpty(true)
                            .sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 0.9F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory10ga.LAMBDA_RECOIL_DOUBLE_BARREL").build())
                    .build()));
    public static final SednaGunConfig GUN_DOUBLE_BARREL_SACRED_DRAGON = register(exact(
            "gun_double_barrel_sacred_dragon", "XFactory10ga", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactory10ga.init(); secondary press manually fires the same receiver.",
            standardMode(0, 6000.0F, 10, 39, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_DOUBLE_BARREL", "XFactory10ga.LAMBDA_DOUBLE_BARREL_ANIMS")
                    .pressSecondary("XFactory10ga.LAMBDA_DOUBLE_SECONDARY")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_DOUBLE_BARREL_SACRED_DRAGON)
                            .damage(45.0F).spreadAmmo(1.35F).rounds(2).delay(10).reload(41)
                            .reloadOnEmpty(true).sound("NTMSounds.GUN_SHOTGUN_FIRE", 1.0F, 0.9F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory10ga.LAMBDA_RECOIL_DOUBLE_BARREL").build())
                    .build()));
    public static final SednaGunConfig GUN_AUTOSHOTGUN_HERETIC = register(exact(
            "gun_autoshotgun_heretic", "XFactory10ga", "ItemGunBaseNT", WeaponQuality.DEBUG,
            "Exact fields copied from XFactory10ga.init(); receiver uses Lego.LAMBDA_NOWEAR_FIRE.",
            standardMode(0, 0.0F, 20, 65, Crosshair.L_CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_SHREDDER_SEXY", "XFactory12ga.LAMBDA_SEXY_ANIMS")
                    .reloadSequential(true).inspectCancel(false).hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AUTOSHOTGUN_HERETIC)
                            .damage(100.0F).delay(3).auto(true).dryfireAfterAuto(true)
                            .reload(110).jam(19).sound("NTMSounds.GUN_SHREDDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.125D, -0.25D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .recoil("XFactory12ga.LAMBDA_RECOIL_SEXY").build())
                    .build()));

    public static final SednaGunConfig GUN_FLAREGUN = register(exact("gun_flaregun", "XFactory40mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory40mm.init().",
            standardMode(0, 100.0F, 7, 39, Crosshair.L_CIRCUMFLEX, "XFactory40mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_FLAREGUN", "XFactory40mm.LAMBDA_FLAREGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FLAREGUN)
                            .damage(15.0F).delay(20).reload(28).jam(33)
                            .sound("NTMSounds.GUN_UNDERBARREL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory40mm.LAMBDA_RECOIL_GL").build())
                    .build()));
    public static final SednaGunConfig GUN_CONGOLAKE = register(exact("gun_congolake", "XFactory40mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory40mm.init().",
            standardMode(0, 400.0F, 7, 39, Crosshair.L_CIRCUMFLEX, "XFactory40mm.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_CONGOLAKE", "XFactory40mm.LAMBDA_CONGOLAKE_ANIMS")
                    .reloadSequential(true).reloadChangeType(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_CONGOLAKE)
                            .damage(20.0F).delay(24).reload(16, 16, 16, 0).jam(0)
                            .sound("NTMSounds.GUN_CONGO_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactory40mm.LAMBDA_RECOIL_GL").build())
                    .build()));
    public static final SednaGunConfig GUN_MK108 = register(exact("gun_mk108", "XFactory40mm",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactory40mm.init(); hideCrosshair=false and no smoke handler is installed.",
            standardMode(0, 5000.0F, 20, 65, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_MK108", "XFactory40mm.LAMBDA_MK108_ANIMS")
                    .hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MK108)
                            .damage(25.0F).delay(10).auto(true).dryfireAfterAuto(true)
                            .reload(135).jam(25).sound("NTMSounds.GUN_MK108_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.125D, -0.125D).standardFire()
                            .recoil("XFactory40mm.LAMBDA_RECOIL_MK108").build())
                    .build()));

    public static final SednaGunConfig GUN_AMAT = register(exact("gun_amat", "XFactory50",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory50.init().",
            standardMode(0, 350.0F, 20, 50, Crosshair.CIRCLE, "XFactory50.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_AMAT", "XFactory50.LAMBDA_AMAT_ANIMS")
                    .scopeTexture("hbm:textures/misc/scope_amat.png")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AMAT)
                            .damage(30.0F).delay(25).dryDelay(25).spreadHipfire(0.05F)
                            .reload(51).jam(43).sound("NTMSounds.GUN_AMAT_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.25D).standardFire()
                            .recoil("XFactory50.LAMBDA_RECOIL_AMAT").build())
                    .build()));
    public static final SednaGunConfig GUN_AMAT_SUBTLETY = register(exact("gun_amat_subtlety",
            "XFactory50", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory50.init(); magazine prepends secret bmg50_equestrian.",
            standardMode(0, 1000.0F, 20, 50, Crosshair.CIRCLE, "XFactory50.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_AMAT", "XFactory50.LAMBDA_AMAT_ANIMS")
                    .scopeTexture("hbm:textures/misc/scope_amat.png")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AMAT_SUBTLETY)
                            .damage(50.0F).delay(25).dryDelay(25).spreadHipfire(0.05F)
                            .reload(51).jam(43).sound("NTMSounds.GUN_AMAT_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.25D).standardFire()
                            .recoil("XFactory50.LAMBDA_RECOIL_AMAT").build())
                    .build()));
    public static final SednaGunConfig GUN_AMAT_PENANCE = register(exact("gun_amat_penance",
            "XFactory50", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactory50.init(); thermal sights use hbm:textures/misc/scope_penance.png.",
            standardMode(0, 5000.0F, 20, 50, Crosshair.CIRCLE, "XFactory50.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_AMAT", "XFactory50.LAMBDA_AMAT_ANIMS")
                    .scopeTexture("hbm:textures/misc/scope_penance.png").thermalSights(true)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_AMAT_PENANCE)
                            .damage(45.0F).delay(25).dryDelay(25).spreadHipfire(0.0F)
                            .reload(51).jam(43).sound("NTMSounds.GUN_AMAT_SILENCER", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.25D).standardFire()
                            .recoil("XFactory50.LAMBDA_RECOIL_AMAT").build())
                    .build()));
    public static final SednaGunConfig GUN_M2 = register(exact("gun_m2", "XFactory50",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactory50.init().",
            standardMode(0, 3000.0F, 10, 31, Crosshair.L_CIRCLE, "XFactory50.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_M2", "XFactory50.LAMBDA_M2_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_M2)
                            .damage(7.5F).delay(2).dryDelay(10).auto(true).spread(0.005F)
                            .sound("NTMSounds.TURRET_50BMG", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory50.LAMBDA_RECOIL_M2").build())
                    .build()));

    public static final SednaGunConfig GUN_BOLTER = register(exact("gun_bolter", "XFactory75Bolt",
            "ItemGunBaseNT", WeaponQuality.SPECIAL, "Exact fields copied from XFactory75Bolt.init().",
            standardMode(0, 3000.0F, 20, 31, Crosshair.L_CIRCLE, "XFactory75Bolt.LAMBDA_SMOKE",
                    "Orchestras.ORCHESTRA_BOLTER", "XFactory75Bolt.LAMBDA_BOLTER_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_BOLTER)
                            .damage(15.0F).delay(2).auto(true).spread(0.005F)
                            .reload(40).jam(55).sound("NTMSounds.GUN_POWDER_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactory75Bolt.LAMBDA_RECOIL_BOLT").build())
                    .build()));
    public static final SednaGunConfig GUN_ABERRATOR = register(exact("gun_aberrator", "XFactory35800",
            "ItemGunBaseNT", WeaponQuality.SECRET, "Exact fields copied from XFactory35800.init().",
            standardMode(0, 2000.0F, 10, 26, Crosshair.CIRCLE, "Lego.LAMBDA_STANDARD_SMOKE",
                    "Orchestras.ORCHESTRA_ABERRATOR", "XFactory35800.LAMBDA_ABERRATOR")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_ABERRATOR)
                            .damage(100.0F).delay(13).dryDelay(21).reload(51)
                            .sound("NTMSounds.GUN_ABERRATOR_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.09375D, -0.1875D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .recoil("XFactory35800.LAMBDA_RECOIL_ABERRATOR").build())
                    .build()));
    public static final SednaGunConfig GUN_ABERRATOR_EOTT = register(exact("gun_aberrator_eott",
            "XFactory35800", "ItemGunBaseNT", WeaponQuality.SECRET,
            "Exact fields copied from XFactory35800.init(); legacy source creates two GunConfig instances.",
            new ModeBuilder(0)
                    .durability(2000.0F).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.ORCHESTRA_ABERRATOR")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory35800.LAMBDA_ABERRATOR")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_ABERRATOR_EOTT_PRIMARY, 0)
                            .damage(100.0F).spreadHipfire(0.0F).delay(13).dryDelay(21).reload(51)
                            .sound("NTMSounds.GUN_ABERRATOR_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.09375D, 0.1875D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .recoil("XFactory35800.LAMBDA_RECOIL_ABERRATOR").build())
                    .notes("No tertiary aim handler is installed in the first EOTT GunConfig.")
                    .build(),
            new ModeBuilder(1)
                    .durability(2000.0F).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.ORCHESTRA_ABERRATOR")
                    .pressSecondary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactory35800.LAMBDA_ABERRATOR")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_ABERRATOR_EOTT_SECONDARY, 0)
                            .damage(100.0F).spreadHipfire(0.0F).delay(13).dryDelay(21).reload(51)
                            .sound("NTMSounds.GUN_ABERRATOR_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.09375D, -0.1875D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .recoil("XFactory35800.LAMBDA_RECOIL_ABERRATOR").build())
                    .notes("No tertiary aim handler is installed in the second EOTT GunConfig; receiver index remains 0 while magazine NBT index is 1.")
                    .build()));

    public static final SednaGunConfig GUN_PANZERSCHRECK = register(exact("gun_panzerschreck",
            "XFactoryRocket", "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryRocket.init(); legacy orchestra constant is spelled PANERSCHRECK.",
            standardMode(0, 300.0F, 7, 40, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_PANERSCHRECK", "XFactoryRocket.LAMBDA_PANZERSCHRECK_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_PANZERSCHRECK)
                            .damage(25.0F).delay(5).reload(50).jam(40)
                            .sound("NTMSounds.GUN_ROCKET_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryRocket.LAMBDA_RECOIL_ROCKET").build())
                    .build()));
    public static final SednaGunConfig GUN_STINGER = register(exact("gun_stinger", "XFactoryRocket",
            "ItemGunStinger", WeaponQuality.SPECIAL,
            "Exact fields copied from XFactoryRocket.init(); secondary press/release manage old Stinger lock-on flow.",
            standardMode(0, 300.0F, 7, 40, Crosshair.L_BOX_OUTLINE, "",
                    "Orchestras.ORCHESTRA_STINGER", "XFactoryRocket.LAMBDA_PANZERSCHRECK_ANIMS")
                    .pressSecondary("XFactoryRocket.LAMBDA_STINGER_SECONDARY_PRESS")
                    .releaseSecondary("XFactoryRocket.LAMBDA_STINGER_SECONDARY_RELEASE")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_STINGER)
                            .damage(35.0F).delay(5).reload(50).jam(40)
                            .sound("NTMSounds.GUN_ROCKET_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).lockonFire()
                            .recoil("XFactoryRocket.LAMBDA_RECOIL_ROCKET").build())
                    .build()));
    public static final SednaGunConfig GUN_QUADRO = register(exact("gun_quadro", "XFactoryRocket",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactoryRocket.init().",
            standardMode(0, 400.0F, 7, 40, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_QUADRO", "XFactoryRocket.LAMBDA_QUADRO_ANIMS")
                    .hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_QUADRO)
                            .damage(40.0F).spreadHipfire(0.0F).delay(10).reload(55).jam(40)
                            .sound("NTMSounds.GUN_ROCKET_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryRocket.LAMBDA_RECOIL_ROCKET").build())
                    .build()));
    public static final SednaGunConfig GUN_MISSILE_LAUNCHER = register(exact("gun_missile_launcher",
            "XFactoryRocket", "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryRocket.init(); primary press uses missile launcher targeting handler.",
            standardMode(0, 500.0F, 20, 40, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_MISSILE_LAUNCHER", "XFactoryRocket.LAMBDA_MISSILE_LAUNCHER_ANIMS")
                    .hideCrosshair(false)
                    .pressPrimary("XFactoryRocket.LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_MISSILE_LAUNCHER)
                            .damage(50.0F).spreadHipfire(0.0F).delay(5).reload(48).jam(33)
                            .sound("NTMSounds.GUN_ROCKET_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryRocket.LAMBDA_RECOIL_ROCKET").build())
                    .build()));

    public static final SednaGunConfig GUN_FLAMER = register(exact("gun_flamer", "XFactoryFlamer",
            "ItemGunBaseNT", WeaponQuality.A_SIDE, "Exact fields copied from XFactoryFlamer.init().",
            standardMode(0, 20000.0F, 10, 17, Crosshair.L_CIRCLE, "",
                    "Orchestras.ORCHESTRA_FLAMER", "XFactoryFlamer.LAMBDA_FLAMER_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FLAMER)
                            .damage(1.0F).spreadHipfire(0.0F).delay(1).auto(true)
                            .reload(90).jam(17).offset(0.75D, -0.0625D, -0.25D)
                            .standardFire().build())
                    .build()));
    public static final SednaGunConfig GUN_FLAMER_TOPAZ = register(exact("gun_flamer_topaz",
            "XFactoryFlamer", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactoryFlamer.init(); magazine capacity is supplied by magazine config.",
            standardMode(0, 20000.0F, 10, 17, Crosshair.L_CIRCLE, "",
                    "Orchestras.ORCHESTRA_FLAMER", "XFactoryFlamer.LAMBDA_FLAMER_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FLAMER_TOPAZ)
                            .damage(1.5F).spreadHipfire(0.0F).delay(1).auto(true)
                            .reload(90).jam(17).offset(0.75D, -0.0625D, -0.25D)
                            .standardFire().build())
                    .build()));
    public static final SednaGunConfig GUN_FLAMER_DAYBREAKER = register(exact("gun_flamer_daybreaker",
            "XFactoryFlamer", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactoryFlamer.init().",
            standardMode(0, 20000.0F, 10, 17, Crosshair.L_CIRCLE, "",
                    "Orchestras.ORCHESTRA_FLAMER_DAYBREAKER", "XFactoryFlamer.LAMBDA_FLAMER_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FLAMER_DAYBREAKER)
                            .damage(25.0F).spreadHipfire(0.0F).delay(10).auto(true)
                            .reload(90).jam(17).sound("NTMSounds.GUN_POWDER_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.25D).standardFire().build())
                    .build()));
    public static final SednaGunConfig GUN_CHEMTHROWER = register(exact("gun_chemthrower",
            "XFactoryFlamer", "ItemGunChemthrower", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryFlamer.init(); ItemGunChemthrower fills MagazineFluid from fluid containers.",
            new ModeBuilder(0)
                    .durability(90000.0F).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                    .smoke("Lego.LAMBDA_STANDARD_SMOKE").orchestra("Orchestras.ORCHESTRA_CHEMTHROWER")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactoryFlamer.LAMBDA_CHEMTHROWER_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_CHEMTHROWER)
                            .damage(0.0F).spreadHipfire(0.0F).delay(1).auto(true)
                            .offset(0.75D, -0.0625D, -0.25D)
                            .fireHandlers("ItemGunChemthrower.LAMBDA_CAN_FIRE", "ItemGunChemthrower.LAMBDA_FIRE")
                            .build())
                    .notes("No reload or tertiary handler is installed in the legacy chemthrower GunConfig.")
                    .build()));

    public static final SednaGunConfig GUN_TESLA_CANNON = register(exact("gun_tesla_cannon",
            "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryEnergy.init().",
            standardMode(0, 1000.0F, 10, 33, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_TESLA", "XFactoryEnergy.LAMBDA_TESLA_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_TESLA_CANNON)
                            .damage(35.0F).delay(20).spreadHipfire(1.5F).reload(44).jam(19)
                            .sound("NTMSounds.GUN_TESLA_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, 0.0D, -0.375D).offsetScoped(0.75D, 0.0D, -0.25D)
                            .standardFire().recoil("XFactoryEnergy.LAMBDA_RECOIL_ENERGY").build())
                    .build()));
    public static final SednaGunConfig GUN_LASER_PISTOL = register(exact("gun_laser_pistol",
            "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryEnergy.init().",
            standardMode(0, 500.0F, 10, 26, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_LASER_PISTOL", "XFactoryEnergy.LAMBDA_LASER_PISTOL")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LASER_PISTOL)
                            .damage(25.0F).delay(5).spread(1.0F).spreadHipfire(1.0F)
                            .reload(45).jam(37).sound("NTMSounds.GUN_LASER_PISTOL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryEnergy.LAMBDA_RECOIL_ENERGY").build())
                    .build()));
    public static final SednaGunConfig GUN_LASER_PISTOL_PEW_PEW = register(exact(
            "gun_laser_pistol_pew_pew", "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.B_SIDE,
            "Exact fields copied from XFactoryEnergy.init().",
            standardMode(0, 500.0F, 10, 26, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_LASER_PISTOL", "XFactoryEnergy.LAMBDA_LASER_PISTOL")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_LASER_PISTOL_PEW_PEW)
                            .damage(30.0F).rounds(5).delay(10).spread(0.25F).spreadHipfire(1.0F)
                            .reload(45).jam(37).sound("NTMSounds.GUN_LASER_PISTOL_FIRE", 1.0F, 0.8F)
                            .offset(0.75D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryEnergy.LAMBDA_RECOIL_ENERGY").build())
                    .build()));
    public static final SednaGunConfig GUN_LASER_PISTOL_MORNING_GLORY = register(exact(
            "gun_laser_pistol_morning_glory", "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.LEGENDARY,
            "Exact fields copied from XFactoryEnergy.init().",
            standardMode(0, 1500.0F, 10, 26, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_LASER_PISTOL", "XFactoryEnergy.LAMBDA_LASER_PISTOL")
                    .receiver(SednaReceiverConfig.fromMagazine(
                            LegacySednaMagazineConfigs.GUN_LASER_PISTOL_MORNING_GLORY)
                            .damage(20.0F).delay(7).spread(0.0F).spreadHipfire(0.5F)
                            .reload(45).jam(37).sound("NTMSounds.GUN_LASER_PISTOL_FIRE", 1.0F, 1.1F)
                            .offset(0.75D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryEnergy.LAMBDA_RECOIL_ENERGY").build())
                    .build()));
    public static final SednaGunConfig GUN_LASRIFLE = register(exact("gun_lasrifle",
            "XFactoryEnergy", "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryEnergy.init(); scope_luna resolves to hbm:textures/misc/scope_amat.png.",
            standardMode(0, 2000.0F, 10, 26, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_LASRIFLE", "XFactoryEnergy.LAMBDA_LASRIFLE")
                    .scopeTexture("hbm:textures/misc/scope_amat.png")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_LASRIFLE)
                            .damage(50.0F).delay(8).spreadHipfire(1.0F).reload(44).jam(36)
                            .sound("NTMSounds.GUN_LASER_RIFLE_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.09375D, -0.1875D).standardFire()
                            .recoil("XFactoryEnergy.LAMBDA_RECOIL_ENERGY").build())
                    .build()));

    public static final SednaGunConfig GUN_TAU = register(exact("gun_tau", "XFactoryAccelerator",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryAccelerator.init(); secondary charge magazine has key gun_tau.charge.",
            new ModeBuilder(0)
                    .durability(6400.0F).draw(10).inspect(10).crosshair(Crosshair.CIRCLE)
                    .orchestra("Orchestras.ORCHESTRA_TAU")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .releasePrimary("XFactoryAccelerator.LAMBDA_TAU_PRIMARY_RELEASE")
                    .pressSecondary("XFactoryAccelerator.LAMBDA_TAU_SECONDARY_PRESS")
                    .releaseSecondary("XFactoryAccelerator.LAMBDA_TAU_SECONDARY_RELEASE")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactoryAccelerator.LAMBDA_TAU_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_TAU)
                            .damage(25.0F).spreadHipfire(0.0F).delay(4).auto(true).spread(0.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactoryAccelerator.LAMBDA_RECOIL_TAU").build())
                    .notes("No tertiary aim handler is installed in the legacy Tau GunConfig.")
                    .build()));
    public static final SednaGunConfig GUN_COILGUN = register(exact("gun_coilgun",
            "XFactoryAccelerator", "ItemGunBaseNT", WeaponQuality.SPECIAL,
            "Exact fields copied from XFactoryAccelerator.init().",
            standardMode(0, 400.0F, 5, 39, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_COILGUN", "XFactoryAccelerator.LAMBDA_COILGUN_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_COILGUN)
                            .damage(35.0F).delay(5).reload(20).jam(33)
                            .sound("NTMSounds.GUN_COIL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D).standardFire()
                            .recoil("XFactoryAccelerator.LAMBDA_RECOIL_COILGUN").build())
                    .build()));
    public static final SednaGunConfig GUN_NI4NI = register(exact("gun_n_i_4_n_i",
            "XFactoryAccelerator", "ItemGunNI4NI", WeaponQuality.SPECIAL,
            "Exact fields copied from XFactoryAccelerator.init(); secondary press uses NI4NI special behavior and MagazineInfinite.",
            standardMode(0, 0.0F, 5, 39, Crosshair.CIRCLE, "",
                    "Orchestras.ORCHESTRA_COILGUN", "XFactoryAccelerator.LAMBDA_NI4NI_ANIMS")
                    .pressSecondary("XFactoryAccelerator.LAMBDA_NI4NI_SECONDARY_PRESS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_NI4NI)
                            .damage(35.0F).delay(10).sound("NTMSounds.GUN_COIL_FIRE", 1.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "Lego.LAMBDA_NOWEAR_FIRE")
                            .build())
                    .build()));

    public static final SednaGunConfig GUN_FATMAN = register(exact("gun_fatman", "XFactoryCatapult",
            "ItemGunBaseNT", WeaponQuality.A_SIDE,
            "Exact fields copied from XFactoryCatapult.init(); legacy setDefaultAmmoExpensive is recorded as data only.",
            standardMode(0, 300.0F, 20, 30, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_FATMAN", "XFactoryCatapult.LAMBDA_FATMAN_ANIMS")
                    .reloadChangeType(true).hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FATMAN)
                            .damage(100.0F).spreadHipfire(0.0F).delay(10).reload(57).jam(40)
                            .sound("NTMSounds.GUN_FATMAN_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.09375D, -0.1875D)
                            .offsetScoped(1.0D, -0.09375D, -0.125D).standardFire()
                            .recoil("XFactoryCatapult.LAMBDA_RECOIL_FATMAN").build())
                    .notes("528/expensive mode gameplay branch is intentionally not migrated.")
                    .build()));
    public static final SednaGunConfig GUN_FOLLY = register(exact("gun_folly", "XFactoryFolly",
            "ItemGunBaseNT", WeaponQuality.SECRET, "Exact fields copied from XFactoryFolly.init().",
            new ModeBuilder(0)
                    .durability(0.0F).draw(40).crosshair(Crosshair.NONE)
                    .orchestra("Orchestras.ORCHESTRA_FOLLY")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .pressTertiary("XFactoryFolly.LAMBDA_TOGGLE_AIM")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactoryFolly.LAMBDA_FOLLY_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FOLLY)
                            .damage(1000.0F).delay(26).dryfire(false).reload(160).jam(0)
                            .sound("NTMSounds.GUN_PLEASE_REMOVE_MY_EARDRUMS_THANKS", 100.0F, 1.0F)
                            .offset(0.75D, -0.0625D, -0.1875D)
                            .offsetScoped(0.75D, -0.0625D, -0.125D)
                            .fireHandlers("XFactoryFolly.LAMBDA_CAN_FIRE", "XFactoryFolly.LAMBDA_FIRE")
                            .recoil("XFactoryFolly.LAMBDA_RECOIL_FOLLY").build())
                    .build()));
    public static final SednaGunConfig GUN_FIREEXT = register(exact("gun_fireext", "XFactoryTool",
            "ItemGunBaseNT", WeaponQuality.UTILITY, "Exact fields copied from XFactoryTool.init().",
            standardMode(0, 5000.0F, 10, 55, Crosshair.L_CIRCLE, "",
                    "Orchestras.ORCHESTRA_FIREEXT", "")
                    .reloadChangeType(true).hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_FIREEXT)
                            .damage(0.0F).delay(1).dryDelay(0).auto(true).spread(0.0F).spreadHipfire(0.0F)
                            .reload(20).jam(0).sound("NTMSounds.GUN_EXTINGUISHER_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire().build())
                    .build()));
    public static final SednaGunConfig GUN_CHARGE_THROWER = register(exact("gun_charge_thrower",
            "XFactoryTool", "ItemGunChargeThrower", WeaponQuality.UTILITY,
            "Exact fields copied from XFactoryTool.init(); hook stores last hook entity id in ItemGunChargeThrower runtime NBT.",
            standardMode(0, 3000.0F, 10, 55, Crosshair.L_CIRCUMFLEX, "",
                    "Orchestras.ORCHESTRA_CHARGE_THROWER", "XFactoryTool.LAMBDA_CT_ANIMS")
                    .reloadChangeType(true).hideCrosshair(false)
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_CHARGE_THROWER)
                            .damage(10.0F).delay(4).dryDelay(10).auto(true).spread(0.0F).spreadHipfire(0.0F)
                            .reload(60).jam(0).sound("NTMSounds.GUN_CHARGE_FIRE", 1.0F, 1.0F)
                            .offset(1.0D, -0.15625D, -0.25D).standardFire()
                            .recoil("XFactoryTool.LAMBDA_RECOIL_CT").build())
                    .build()));
    public static final SednaGunConfig GUN_DRILL = register(exact("gun_drill", "XFactoryDrill",
            "ItemGunDrill", WeaponQuality.UTILITY,
            "Exact data fields copied from XFactoryDrill.init(); block breaking, entity melee, engine fuel/electric handling, AoE highlight and weapon-mod evaluation remain deferred to tool runtime migration.",
            new ModeBuilder(0)
                    .durability(3000.0F).draw(10).inspect(55).hideCrosshair(false)
                    .crosshair(Crosshair.L_CIRCUMFLEX)
                    .orchestra("Orchestras.ORCHESTRA_DRILL")
                    .pressPrimary("Lego.LAMBDA_STANDARD_CLICK_PRIMARY")
                    .pressReload("Lego.LAMBDA_STANDARD_RELOAD")
                    .decider("GunStateDecider.LAMBDA_STANDARD_DECIDER")
                    .animation("XFactoryDrill.LAMBDA_DRILL_ANIMS")
                    .receiver(SednaReceiverConfig.fromMagazine(LegacySednaMagazineConfigs.GUN_DRILL_BASE)
                            .damage(10.0F).delay(20).dryDelay(30).auto(true).jam(0)
                            .offset(1.0D, -0.15625D, -0.25D)
                            .fireHandlers("Lego.LAMBDA_STANDARD_CAN_FIRE", "XFactoryDrill.LAMBDA_DRILL_FIRE")
                            .notes("Liquid engine accepts GASOLINE/GASOLINE_LEADED/COALGAS/COALGAS_LEADED; drill fire consumes 10 liquid fuel or 1000 electric operations when the electric engine weapon mod is installed.")
                            .build())
                    .notes("No tertiary aim handler is installed in the legacy drill GunConfig; ItemGunDrill implements IFillableItem and IBatteryItem.")
                    .build()));

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

    private static SednaGunConfig register(SednaGunConfig config) {
        config = withLegacyHud(config);
        BY_NAME.put(config.legacyName(), config);
        return config;
    }

    private static SednaGunConfig withLegacyHud(SednaGunConfig config) {
        List<SednaGunConfig.GunModeConfig> modes = new ArrayList<>(config.configs().size());
        for (SednaGunConfig.GunModeConfig mode : config.configs()) {
            List<String> hud = legacyHudComponents(config.legacyName(), mode.configIndex());
            modes.add(hud.isEmpty() ? mode : copyModeWithHud(mode, hud));
        }
        return new SednaGunConfig(config.legacyName(), config.sourceClassName(), config.itemClassName(),
                config.quality(), modes, config.notes());
    }

    private static SednaGunConfig.GunModeConfig copyModeWithHud(SednaGunConfig.GunModeConfig mode,
            List<String> hudComponents) {
        return new SednaGunConfig.GunModeConfig(mode.configIndex(), mode.durability(), mode.drawDuration(),
                mode.inspectDuration(), mode.inspectCancel(), mode.crosshair(), mode.hideCrosshair(),
                mode.thermalSights(), mode.reloadRequiresTypeChange(), mode.reloadAnimationsSequential(),
                mode.scopeTexture(), mode.smokeHandlerName(), mode.orchestraName(),
                mode.pressPrimaryHandlerName(), mode.pressSecondaryHandlerName(), mode.pressTertiaryHandlerName(),
                mode.pressReloadHandlerName(), mode.releasePrimaryHandlerName(), mode.releaseSecondaryHandlerName(),
                mode.releaseTertiaryHandlerName(), mode.releaseReloadHandlerName(), mode.deciderName(),
                mode.animationProfileName(), hudComponents, mode.receivers(), mode.notes());
    }

    private static List<String> legacyHudComponents(String legacyName, int configIndex) {
        return switch (legacyName) {
            case "gun_debug" -> List.of(HUD_COMPONENT_DURABILITY, HUD_COMPONENT_AMMO, HUD_COMPONENT_AMMO_SECOND);
            case "gun_light_revolver_dani", "gun_maresleg_akimbo", "gun_uzi_akimbo", "gun_star_f_akimbo",
                    "gun_minigun_dual" -> configIndex == 0
                    ? List.of(HUD_COMPONENT_DURABILITY_MIRROR, HUD_COMPONENT_AMMO_MIRROR)
                    : List.of(HUD_COMPONENT_DURABILITY, HUD_COMPONENT_AMMO);
            case "gun_aberrator_eott" -> configIndex == 0
                    ? List.of(HUD_COMPONENT_AMMO_MIRROR)
                    : List.of(HUD_COMPONENT_AMMO);
            case "gun_maresleg_broken", "gun_autoshotgun_heretic", "gun_folly", "gun_aberrator" ->
                    List.of(HUD_COMPONENT_AMMO);
            case "gun_flamer", "gun_flamer_topaz", "gun_flamer_daybreaker" ->
                    List.of(HUD_COMPONENT_DURABILITY, HUD_COMPONENT_AMMO_NOCOUNTER);
            case "gun_pepperbox", "gun_light_revolver", "gun_light_revolver_atlas", "gun_henry",
                    "gun_henry_lincoln", "gun_greasegun", "gun_maresleg", "gun_flaregun",
                    "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege",
                    "gun_carbine", "gun_am180", "gun_liberator", "gun_congolake", "gun_uzi", "gun_spas12",
                    "gun_panzerschreck", "gun_star_f", "gun_g3", "gun_g3_zebra", "gun_stinger", "gun_mk108",
                    "gun_chemthrower", "gun_amat", "gun_amat_subtlety", "gun_amat_penance", "gun_m2",
                    "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_autoshotgun_sexy", "gun_quadro",
                    "gun_lag", "gun_minigun", "gun_minigun_lacunae", "gun_missile_launcher",
                    "gun_tesla_cannon", "gun_laser_pistol", "gun_laser_pistol_pew_pew",
                    "gun_laser_pistol_morning_glory", "gun_stg77", "gun_tau", "gun_fatman", "gun_lasrifle",
                    "gun_coilgun", "gun_hangman", "gun_mas36", "gun_bolter", "gun_double_barrel",
                    "gun_double_barrel_sacred_dragon", "gun_fireext", "gun_charge_thrower" ->
                    List.of(HUD_COMPONENT_DURABILITY, HUD_COMPONENT_AMMO);
            default -> List.of();
        };
    }

    private LegacySednaGunConfigs() {
    }
}
