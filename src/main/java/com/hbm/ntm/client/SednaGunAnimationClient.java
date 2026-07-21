package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.client.anim.LegacyBusAnimation;
import com.hbm.ntm.client.anim.LegacyBusAnimationKeyframe.IType;
import com.hbm.ntm.client.anim.LegacyBusAnimationSequence;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.SednaGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Source-shaped animation packet receiver for Sedna guns whose renderers use the shared hotbar bus. */
public final class SednaGunAnimationClient {
    private static final int RELOAD = 0;
    private static final int RELOAD_CYCLE = 1;
    private static final int RELOAD_END = 2;
    private static final int CYCLE = 3;
    private static final int CYCLE_DRY = 5;
    private static final int ALT_CYCLE = 6;
    private static final int SPINUP = 7;
    private static final int EQUIP = 9;
    private static final int INSPECT = 10;
    private static final int JAMMED = 11;
    private static final ResourceLocation STG77_ANIMATION_FILE = new ResourceLocation(HbmNtm.MOD_ID,
            "models/weapons/animations/stg77.json");
    private static final ResourceLocation LAG_ANIMATION_FILE = new ResourceLocation(HbmNtm.MOD_ID,
            "models/weapons/animations/lag.json");
    private static final ResourceLocation FLAMETHROWER_ANIMATION_FILE = new ResourceLocation(HbmNtm.MOD_ID,
            "models/weapons/animations/flamethrower.json");
    private static final ResourceLocation SPAS12_ANIMATION_FILE = new ResourceLocation(HbmNtm.MOD_ID,
            "models/weapons/animations/spas12.json");
    private static final ResourceLocation CONGOLAKE_ANIMATION_FILE = new ResourceLocation(HbmNtm.MOD_ID,
            "models/weapons/animations/congolake.json");

    public static void handle(ItemStack stack, int selectedSlot, short animationType, int receiverIndex, int itemIndex) {
        if (!(stack.getItem() instanceof SednaGunItem gun)) return;
        if (animationType == CYCLE) {
            // Match HbmAnimationPacket: receiverIndex selects the receiver; itemIndex selects the GunConfig/rail.
            ClientSednaGunEffects.markCycle(gun, itemIndex);
            LegacySednaVisualRecoil.onCycle(gun, receiverIndex, itemIndex);
        }
        LegacyBusAnimation animation = switch (gun.gunConfig().legacyName()) {
            case "gun_debug" -> debug(animationType);
            case "gun_pepperbox" -> pepperbox(animationType);
            case "gun_light_revolver", "gun_light_revolver_atlas" -> atlas(animationType);
            case "gun_light_revolver_dani" -> dani(animationType);
            case "gun_hangman" -> hangman(animationType);
            case "gun_henry", "gun_henry_lincoln" -> henry(stack, animationType);
            case "gun_am180" -> am180(stack, animationType);
            case "gun_tesla_cannon" -> tesla(stack, animationType);
            case "gun_laser_pistol", "gun_laser_pistol_pew_pew", "gun_laser_pistol_morning_glory" -> laserPistol(animationType);
            case "gun_lasrifle" -> lasrifle(animationType);
            case "gun_aberrator", "gun_aberrator_eott" -> aberrator(stack, animationType);
            case "gun_bolter" -> bolter(animationType);
            case "gun_stg77" -> stg77(stack, selectedSlot, animationType);
            case "gun_carbine" -> carbine(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_CARBINE_BAYONET));
            case "gun_minigun", "gun_minigun_lacunae" -> minigun(stack, animationType);
            case "gun_minigun_dual" -> minigun(stack, animationType);
            case "gun_mas36" -> mas36(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_MAS_BAYONET));
            case "gun_lag" -> lag(stack, selectedSlot, animationType);
            case "gun_double_barrel", "gun_double_barrel_sacred_dragon" -> doubleBarrel(animationType);
            case "gun_heavy_revolver" -> heavyRevolver(animationType, false);
            case "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege" -> heavyRevolver(animationType, true);
            case "gun_charge_thrower" -> chargeThrower(animationType);
            case "gun_panzerschreck" -> panzerschreck(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_NO_SHIELD));
            case "gun_stinger" -> panzerschreck(stack, animationType, false);
            case "gun_quadro" -> quadro(animationType);
            case "gun_missile_launcher" -> missileLauncher(animationType);
            case "gun_chemthrower" -> chemthrower(animationType);
            case "gun_flamer", "gun_flamer_topaz", "gun_flamer_daybreaker" -> flamer(stack, selectedSlot, animationType);
            case "gun_fatman" -> fatman(animationType);
            case "gun_folly" -> folly(animationType);
            case "gun_maresleg" -> maresleg(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_SAWED_OFF));
            case "gun_maresleg_broken", "gun_maresleg_akimbo" -> maresleg(stack, animationType, true);
            case "gun_liberator" -> liberator(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_LIBERATOR_SPEEDLOADER));
            case "gun_spas12" -> spas12(stack, selectedSlot, animationType);
            case "gun_autoshotgun", "gun_autoshotgun_shredder" -> shredder(animationType);
            case "gun_autoshotgun_sexy", "gun_autoshotgun_heretic" -> sexy(stack, animationType);
            case "gun_uzi", "gun_uzi_akimbo" -> uzi(stack, animationType);
            case "gun_star_f", "gun_star_f_akimbo" -> starF(stack, animationType);
            case "gun_g3", "gun_g3_zebra" -> g3(stack, animationType,
                    SednaWeaponModEvaluator.hasUpgrade(stack, itemIndex, SednaWeaponModEvaluator.ID_NO_STOCK));
            case "gun_amat", "gun_amat_subtlety", "gun_amat_penance" -> amat(animationType);
            case "gun_greasegun" -> greasegun(stack, animationType);
            case "gun_flaregun" -> flaregun(animationType);
            case "gun_congolake" -> congolake(stack, selectedSlot, animationType);
            case "gun_mk108" -> mk108(stack, animationType);
            case "gun_m2" -> m2(animationType);
            case "gun_coilgun" -> coilgun(stack, animationType);
            case "gun_n_i_4_n_i" -> ni4ni(stack, animationType);
            case "gun_tau" -> tau(animationType);
            case "gun_drill" -> drill(animationType);
            default -> null;
        };
        if (animation != null) {
            LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(), animation, false);
        }
    }

    /** Exact XFactory12ga Maresleg lambda, including its source short variant override. */
    private static LegacyBusAnimation maresleg(ItemStack stack, int type, boolean shortened) {
        if (shortened) {
            if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                    seq().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 250, IType.SIN_DOWN));
            if (type == CYCLE) return new LegacyBusAnimation()
                    .addBus("RECOIL", seq().hold(50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                    .addBus("SIGHT", seq().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("LEVER", seq().hold(600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200))
                    .addBus("FLIP", seq().hold(600).addPos(360, 0, 0, 400))
                    .addBus("SHELL", seq().setPos(-20, 0, 0));
            if (type == CYCLE_DRY) return new LegacyBusAnimation()
                    .addBus("LEVER", seq().hold(600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200))
                    .addBus("FLIP", seq().hold(600).addPos(360, 0, 0, 400))
                    .addBus("SHELL", seq().setPos(-20, 0, 0));
            if (type == JAMMED) return new LegacyBusAnimation()
                    .addBus("LIFT", mareslegReloadEndLift())
                    .addBus("LEVER", mareslegJammedLever())
                    .addBus("FLAG", seq().setPos(1, 1, 1));
        }
        return mareslegNormal(stack, type);
    }

    /** Exact XFactoryBlackPowder.LAMBDA_PEPPERBOX_ANIMS rails. */
    private static LegacyBusAnimation pepperbox(int type) {
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("ROTATE", seq().hold(1025).addPos(60, 0, 0, 250))
                .addBus("RECOIL", seq().hold(50).addPos(45, 0, 0, 150, IType.SIN_DOWN).hold(50).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("HAMMER", seq().addPos(80, 0, 0, 25).hold(1000).addPos(0, 0, 0, 250))
                .addBus("TRIGGER", seq().addPos(1, 0, 0, 25).hold(250).addPos(0, 0, 0, 100));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("ROTATE", seq().hold(525).addPos(60, 0, 0, 250))
                .addBus("HAMMER", seq().addPos(80, 0, 0, 25).hold(500).addPos(0, 0, 0, 250))
                .addBus("TRIGGER", seq().addPos(1, 0, 0, 25).hold(250).addPos(0, 0, 0, 100));
        if (type == EQUIP) return new LegacyBusAnimation().addBus("RECOIL",
                seq().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(90, 0, 0, 500, IType.SIN_FULL).hold(1600).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN))
                .addBus("TRANSLATE", seq().addPos(0, -12, 5, 500, IType.SIN_FULL).hold(700).addPos(0, -13, 5, 200).addPos(0, -12, 5, 200).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("LOADER", seq().hold(500).addPos(0, 5, -5, 0).addPos(0, 0, -.1, 500, IType.SIN_FULL).addPos(0, 0, -1, 200).hold(200).addPos(0, 0, -.1, 200).addPos(0, 5, -5, 500, IType.SIN_FULL).addPos(0, 0, 0, 0))
                .addBus("ROTATE", seq().hold(2600).addPos(-360, 0, 0, 750, IType.SIN_FULL))
                .addBus("SHOT", seq().addPos(1, 0, 0, 1400).addPos(0, 0, 0, 0));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("ROTATE", seq().addPos(-360, 0, 0, 750, IType.SIN_FULL))
                .addBus("RECOIL", seq().addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("ROTATE", seq().hold(1300).addPos(60, 0, 0, 500, IType.SIN_FULL).hold(400).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TRANSLATE", seq().hold(500).addPos(0, -6, 0, 400, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 400, IType.SIN_FULL))
                .addBus("RECOIL", seq().hold(500).addPos(45, 0, 0, 400, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 400, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactory44.LAMBDA_HENRY_ANIMS rails shared by Henry and Henry Lincoln. */
    private static LegacyBusAnimation henry(ItemStack stack, int type) {
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(-90, 0, 0, 0).addPos(0, 0, -3, 350, IType.SIN_DOWN))
                .addBus("SIGHT", seq().addPos(80, 0, 0, 0).hold(500).addPos(0, 0, -3, 250, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().hold(50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("SIGHT", seq().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("LEVER", seq().hold(600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("TURN", seq().hold(600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("LEVER", seq().hold(600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("TURN", seq().hold(600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LIFT", seq().addPos(-60, 0, 0, 400, IType.SIN_FULL))
                .addBus("TWIST", seq().hold(500).addPos(0, 0, -90, 200, IType.SIN_FULL))
                .addBus("BULLET", seq().hold(700).addPos(3, 0, -6, 0).addPos(0, 0, 1, 300, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == RELOAD_CYCLE) return new LegacyBusAnimation()
                .addBus("LIFT", seq().setPos(-60, 0, 0))
                .addBus("TWIST", seq().setPos(0, 0, -90))
                .addBus("BULLET", seq().setPos(3, 0, -6).addPos(0, 0, 1, 300, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == RELOAD_END) {
            boolean empty = primaryMagazineAmountBeforeReload(stack) <= 0;
            return new LegacyBusAnimation()
                    .addBus("LIFT", seq().setPos(-60, 0, 0).hold(300).addPos(0, 0, 0, 400, IType.SIN_FULL))
                    .addBus("TWIST", seq().setPos(0, 0, -90).addPos(0, 0, 0, 200, IType.SIN_FULL))
                    .addBus("LEVER", seq().hold(700).addPos(empty ? -90 : 0, 0, 0, 200).addPos(0, 0, 0, 200))
                    .addBus("TURN", seq().hold(700).addPos(0, 0, empty ? 45 : 0, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP));
        }
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().setPos(-60, 0, 0).hold(300).addPos(0, 0, 0, 400, IType.SIN_FULL))
                .addBus("TWIST", seq().setPos(0, 0, -90).addPos(0, 0, 0, 200, IType.SIN_FULL))
                .addBus("LEVER", seq().hold(700).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200).hold(500).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200).hold(200).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("TURN", seq().hold(700).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP).hold(500).addPos(0, 0, 45, 200, IType.SIN_FULL).hold(600).addPos(0, 0, 0, 200, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("YEET", seq().addPos(0, 2, 0, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("ROLL", seq().addPos(0, 0, 360, 400));
        return null;
    }

    /** Exact XFactory357.LAMBDA_ATLAS_ANIMS rails shared by Light Revolver and Atlas. */
    private static LegacyBusAnimation atlas(int type) {
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(300).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(250).addPos(0, 0, 1, 200));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(200).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(250).addPos(0, 0, 1, 200));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LATCH", seq().addPos(0, 0, 90, 300).hold(2000).addPos(0, 0, 0, 150))
                .addBus("FRONT", seq().hold(200).addPos(0, 0, 45, 150).hold(2000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", seq().hold(300).addPos(60, 0, 0, 500).hold(500)
                        .addPos(0, -90, -90, 0).hold(600).addPos(0, 0, 0, 300).hold(100)
                        .addPos(-45, 0, 0, 50).hold(100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", seq().hold(300).addPos(0, -15, 0, 1000).addPos(0, 0, 0, 450))
                .addBus("DRUM_PUSH", seq().hold(1600).addPos(0, 0, -5, 0).addPos(0, 0, 0, 300));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LATCH", seq().addPos(0, 0, 90, 300).hold(1000).addPos(0, 0, 0, 150))
                .addBus("FRONT", seq().hold(200).addPos(0, 0, 45, 150).hold(1000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", seq().hold(300).addPos(45, 0, 0, 500, IType.SIN_FULL).hold(500)
                        .addPos(-45, 0, 0, 50).hold(100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", seq().hold(300).addPos(0, -2.5, 0, 500, IType.SIN_FULL).hold(500)
                        .addPos(0, 0, 0, 350));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LATCH", seq().hold(500).addPos(0, 0, 90, 300).hold(1000).addPos(0, 0, 0, 150))
                .addBus("FRONT", seq().hold(500).hold(200).addPos(0, 0, 45, 150).hold(1000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", seq().hold(500).hold(300).addPos(45, 0, 0, 500, IType.SIN_FULL)
                        .hold(500).addPos(-45, 0, 0, 50).hold(100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", seq().hold(500).hold(300).addPos(0, -2.5, 0, 500, IType.SIN_FULL)
                        .hold(500).addPos(0, 0, 0, 350));
        return null;
    }

    /** Exact XFactory357.LAMBDA_DANI_ANIMS override plus its Atlas delegation. */
    private static LegacyBusAnimation dani(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(1080, 0, 0, 1000, IType.SIN_DOWN));
        return atlas(type);
    }

    /** Exact XFactory44.LAMBDA_HANGMAN_ANIMS rails. */
    private static LegacyBusAnimation hangman(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL",
                seq().hold(50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LID", seq().addPos(0, 0, -90, 250).hold(1500).addPos(0, 0, 0, 250))
                .addBus("MAG", seq().hold(250).addPos(0, -10, 0, 250, IType.SIN_UP).hold(500)
                        .addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("BULLETS", seq().setPos(1, 1, 1).addPos(0, 0, 0, 500))
                .addBus("EQUIP", seq().addPos(-15, 0, 0, 500, IType.SIN_FULL).hold(850)
                        .addPos(-25, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("ROLL", seq().hold(500).addPos(0, 0, 25, 250, IType.SIN_FULL).hold(1000)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("TURN", seq().addPos(0, 170, 0, 500, IType.SIN_UP).hold(550)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("ROLL", seq().addPos(0, 0, 110, 500, IType.SIN_FULL).hold(550)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SMACK", seq().hold(500).addPos(0, 0, 1, 150, IType.SIN_DOWN)
                        .addPos(0, 0, -3, 150, IType.SIN_UP).addPos(0, 0, 0, 350, IType.SIN_FULL));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LID", seq().hold(500).addPos(0, 0, -90, 250).hold(300).addPos(0, 0, 0, 250))
                .addBus("MAG", seq().hold(500).hold(250).addPos(0, -3, 0, 150, IType.SIN_UP)
                        .addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("EQUIP", seq().hold(1000).addPos(-10, 0, 0, 100, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("ROLL", seq().hold(500).addPos(0, 0, 25, 250, IType.SIN_FULL).hold(300)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        return null;
    }

    private static LegacyBusAnimation mareslegNormal(ItemStack stack, int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().hold(50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("SIGHT", seq().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("LEVER", seq().hold(600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("TURN", seq().hold(600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("LEVER", seq().hold(600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("TURN", seq().hold(600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(30, 0, 0, 50).hold(550).addPos(0, 0, 0, 200));
        if (type == RELOAD) {
            boolean empty = primaryMagazineCount(stack) <= 0;
            return new LegacyBusAnimation()
                    .addBus("LIFT", seq().addPos(30, 0, 0, 400, IType.SIN_FULL))
                    .addBus("LEVER", seq().hold(400).addPos(-85, 0, 0, 200))
                    .addBus("SHELL", seq().hold(600).setPos(0, .25, -3)
                            .addPos(0, empty ? .25 : .125, -1.5, 150, IType.SIN_UP)
                            .addPos(0, empty ? .25 : -.25, 0, 150, IType.SIN_DOWN))
                    .addBus("FLAG", seq().hold(empty ? 900 : 0).setPos(1, 1, 1));
        }
        if (type == RELOAD_CYCLE) return new LegacyBusAnimation()
                .addBus("LIFT", seq().setPos(30, 0, 0))
                .addBus("LEVER", seq().setPos(-85, 0, 0))
                .addBus("SHELL", seq().setPos(0, .25, -3).addPos(0, .125, -1.5, 150, IType.SIN_UP)
                        .addPos(0, -.125, 0, 150, IType.SIN_DOWN))
                .addBus("FLAG", seq().setPos(1, 1, 1));
        if (type == RELOAD_END) return new LegacyBusAnimation()
                .addBus("LIFT", mareslegReloadEndLift())
                .addBus("LEVER", seq().setPos(-85, 0, 0).addPos(0, 0, 0, 200))
                .addBus("FLAG", seq().setPos(1, 1, 1));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", mareslegReloadEndLift())
                .addBus("LEVER", mareslegJammedLever())
                .addBus("TURN", seq().hold(850).addPos(0, 0, 45, 200, IType.SIN_DOWN).hold(800)
                        .addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("FLAG", seq().setPos(1, 1, 1));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LIFT", seq().addPos(-35, 0, 0, 300, IType.SIN_FULL).hold(1150).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TURN", seq().hold(450).addPos(0, 0, -90, 500, IType.SIN_FULL).hold(500)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL));
        return null;
    }

    private static LegacyBusAnimationSequence mareslegReloadEndLift() {
        return seq().setPos(30, 0, 0).hold(250).addPos(0, 0, 0, 400, IType.SIN_FULL);
    }

    private static LegacyBusAnimationSequence mareslegJammedLever() {
        return seq().setPos(-85, 0, 0).addPos(-15, 0, 0, 200).hold(650).addPos(-85, 0, 0, 200)
                .addPos(-15, 0, 0, 200).hold(200).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200);
    }

    /** Exact count-dependent four-chamber bus layout of XFactory12ga.LAMBDA_LIBERATOR_ANIMS. */
    private static LegacyBusAnimation liberator(ItemStack stack, int type, boolean speedloader) {
        int ammo = primaryMagazineCount(stack);
        if (speedloader) {
            if (type == RELOAD) return new LegacyBusAnimation()
                    .addBus("LATCH", seq().addPos(15, 0, 0, 100))
                    .addBus("BREAK", seq().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                    .addBus("SHELL1", speedloaderShell())
                    .addBus("SHELL2", speedloaderShell())
                    .addBus("SHELL3", speedloaderShell())
                    .addBus("SHELL4", speedloaderShell());
            if (type == RELOAD_END) return new LegacyBusAnimation()
                    .addBus("LATCH", seq().addPos(15, 0, 0, 0).hold(250).addPos(0, 0, 0, 50))
                    .addBus("BREAK", seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP));
            if (type == JAMMED) return new LegacyBusAnimation()
                    .addBus("LATCH", seq().addPos(15, 0, 0, 0).hold(250).addPos(0, 0, 0, 50).hold(550)
                            .addPos(15, 0, 0, 100).hold(600).addPos(0, 0, 0, 50))
                    .addBus("BREAK", seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP).hold(600)
                            .addPos(45, 0, 0, 250, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 150, IType.SIN_UP));
        }
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL",
                seq().addPos(0, 0, -2.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation();
        if (type == RELOAD && ammo >= 0 && ammo < 4) return liberatorReloadStart(ammo);
        if (type == RELOAD_CYCLE && ammo >= 0 && ammo < 3) return liberatorReloadCycle(ammo);
        if (type == RELOAD_END) return liberatorReloadEnd(ammo);
        if (type == JAMMED) return liberatorJammed(ammo);
        if (type == INSPECT) return liberatorInspect(ammo);
        return null;
    }

    /** Exact shared SHELL1..4 rail from WeaponModLiberatorSpeedloader. */
    private static LegacyBusAnimationSequence speedloaderShell() {
        return seq().setPos(2, -4, -2).hold(400).addPos(0, 0, -2, 450, IType.SIN_FULL)
                .addPos(0, 0, 0, 50, IType.SIN_UP);
    }

    private static LegacyBusAnimation liberatorReloadStart(int ammo) {
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("LATCH", seq().addPos(15, 0, 0, 100))
                .addBus("BREAK", seq().hold(100).addPos(60, 0, 0, 350, IType.SIN_DOWN));
        return liberatorShells(animation, ammo + 1, ammo + 1, false);
    }

    private static LegacyBusAnimation liberatorReloadCycle(int ammo) {
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("LATCH", seq().setPos(15, 0, 0))
                .addBus("BREAK", seq().setPos(60, 0, 0));
        return liberatorShells(animation, ammo + 1, ammo + 2, false);
    }

    private static LegacyBusAnimation liberatorReloadEnd(int ammo) {
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("LATCH", seq().setPos(15, 0, 0).hold(250).addPos(0, 0, 0, 50))
                .addBus("BREAK", seq().setPos(60, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP));
        return liberatorShells(animation, ammo + 1, -1, true);
    }

    private static LegacyBusAnimation liberatorJammed(int ammo) {
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("LATCH", seq().setPos(15, 0, 0).hold(250).addPos(0, 0, 0, 50).hold(550)
                        .addPos(15, 0, 0, 100).hold(600).addPos(0, 0, 0, 50))
                .addBus("BREAK", seq().setPos(60, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP).hold(600)
                        .addPos(45, 0, 0, 250, IType.SIN_DOWN).hold(300).addPos(0, 0, 0, 150, IType.SIN_UP));
        return liberatorShells(animation, ammo + 1, -1, true);
    }

    private static LegacyBusAnimation liberatorInspect(int ammo) {
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("LATCH", seq().addPos(15, 0, 0, 100).hold(1100).addPos(0, 0, 0, 50))
                .addBus("BREAK", seq().hold(100).addPos(60, 0, 0, 350, IType.SIN_DOWN).hold(500)
                        .addPos(0, 0, 0, 250, IType.SIN_UP));
        return liberatorShells(animation, ammo, -1, true);
    }

    private static LegacyBusAnimation liberatorShells(LegacyBusAnimation animation, int loaded, int inserting,
            boolean closeHidden) {
        for (int shell = 1; shell <= 4; shell++) {
            String bus = "SHELL" + shell;
            if (shell == inserting) {
                LegacyBusAnimationSequence sequence = closeHidden
                        ? seq().setPos(2, -8, -2)
                        : seq().setPos(2, -4, -2);
                if (!closeHidden) {
                    if (inserting == loaded) {
                        sequence.hold(400);
                    }
                    sequence.addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP);
                }
                animation.addBus(bus, sequence);
            } else if (shell > loaded) {
                animation.addBus(bus, seq().setPos(2, closeHidden ? -8 : -4, -2));
            }
        }
        return animation;
    }

    /** XFactory12ga.LAMBDA_SPAS_ANIMS: authored source JSON for every event except EQUIP. */
    private static LegacyBusAnimation spas12(ItemStack stack, int selectedSlot, int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
        String animationName = switch (type) {
            case CYCLE -> "Fire";
            case CYCLE_DRY -> "FireDry";
            case ALT_CYCLE -> "FireAlt";
            case RELOAD -> primaryMagazineCount(stack) <= 0 ? "ReloadEmptyStart" : "ReloadStart";
            case RELOAD_CYCLE -> "Reload";
            case RELOAD_END -> "ReloadEnd";
            case JAMMED -> "Jammed";
            case INSPECT -> "Inspect";
            default -> null;
        };
        if (animationName != null) {
            ClientItemAnimationHandler.handle(selectedSlot, 0, stack.getItem().getDescriptionId(),
                    SPAS12_ANIMATION_FILE, animationName, false);
        }
        return null;
    }

    /** Exact XFactory12ga.LAMBDA_SHREDDER_ANIMS rails shared by both Shredder IDs. */
    private static LegacyBusAnimation shredder(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("CYCLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("CYCLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -8, 0, 250, IType.SIN_UP).hold(1000).addPos(0, 0, 0, 300))
                .addBus("LIFT", seq().hold(750).addPos(-25, 0, 0, 300, IType.SIN_FULL).hold(500)
                        .addPos(-27, 0, 0, 100, IType.SIN_DOWN).addPos(-25, 0, 0, 100, IType.SIN_FULL)
                        .hold(150).addPos(0, 0, 0, 300, IType.SIN_FULL));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("MAG", seq().hold(500).addPos(0, -2, 0, 150, IType.SIN_UP).addPos(0, 0, 0, 100))
                .addBus("LIFT", seq().hold(750).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -1, 0, 150).addPos(6, -1, 0, 150).addPos(6, 12, 0, 350, IType.SIN_DOWN)
                        .addPos(6, -2, 0, 350, IType.SIN_UP).addPos(6, -1, 0, 50).hold(100)
                        .addPos(0, -1, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("SPEEN", seq().hold(300).addPos(360, 0, 0, 700))
                .addBus("LIFT", seq().hold(1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactory9mm.LAMBDA_UZI_ANIMS rails shared by Uzi and Uzi Akimbo. */
    private static LegacyBusAnimation uzi(ItemStack stack, int type) {
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(80, 0, 0, 0).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("STOCKBACK", seq().addPos(-200, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("STOCKFRONT", seq().addPos(180, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL",
                seq().addPos(0, 0, gunAiming(stack) ? -.5 : -.75, 25, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 75, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(250).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(500)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(500).addPos(0, 0, -2, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 50, IType.SIN_UP));
        if (type == RELOAD) {
            boolean empty = primaryMagazineCount(stack) <= 0;
            return new LegacyBusAnimation()
                    .addBus("MAG", seq().hold(250).addPos(0, -10, 0, 250, IType.SIN_UP).hold(750)
                            .addPos(0, 0, 0, 500, IType.SIN_DOWN))
                    .addBus("LIFT", seq().addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(2000)
                            .addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SLIDE", seq().hold(2000).addPos(0, 0, -2, 150, IType.SIN_FULL)
                            .addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("BULLET", seq().setPos(empty ? 0 : 1, 0, 0).hold(500).setPos(1, 0, 0));
        }
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1250)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(1000).addPos(0, 0, -2, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 50, IType.SIN_UP).hold(500).addPos(0, 0, -2, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 50, IType.SIN_UP));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("YEET", seq().addPos(0, -1, 0, 100).addPos(0, 0, 0, 100, IType.SIN_UP)
                        .addPos(0, 12, 0, 350, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_UP)
                        .addPos(0, -1, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("SPEEN", seq().hold(250).addPos(-360, 0, 0, 600));
        return null;
    }

    /** Exact XFactory22lr.LAMBDA_STAR_F_ANIMS rails shared by Star F and Star F Akimbo. */
    private static LegacyBusAnimation starF(ItemStack stack, int type) {
        int ammo = primaryMagazineCount(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().hold(50).addPos(0, 0, gunAiming(stack) ? -.125 : -.5, 15, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 35, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(50).addPos(0, 0, gunAiming(stack) ? -.5 : -1, 25, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 75, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 50, IType.SIN_DOWN))
                .addBus("BULLET", ammo <= 1 ? seq().setPos(100, 0, 0)
                        : seq().hold(90).addPos(0, .5, 2.25, 50));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("HAMMER", seq().addPos(1, 0, 0, 50, IType.SIN_UP).hold(450)
                        .addPos(0, 0, 0, 50, IType.SIN_DOWN))
                .addBus("SLIDE", seq().hold(500).addPos(0, 0, gunAiming(stack) ? -.5 : -1, 100, IType.SIN_FULL)
                        .hold(100).addPos(0, 0, 0, 75, IType.SIN_UP))
                .addBus("EQUIP", seq().hold(600).addPos(-3, 0, 0, 175, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(100, 0, 0));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("TILT", seq().addPos(-30, 0, 0, 250, IType.SIN_FULL).hold(1500)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(250).addPos(0, 0, -1, 100, IType.SIN_FULL).hold(1125)
                        .addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("MAG", seq().hold(250).addPos(0, -7, -1.5, 300, IType.SIN_UP).hold(400)
                        .addPos(0, 0, 0, 300, IType.SIN_UP))
                .addBus("EQUIP", seq().hold(500).addPos(3, 0, 0, 750, IType.SIN_FULL)
                        .addPos(-3, 0, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("TURN", seq().hold(200).addPos(0, 0, 15, 300, IType.SIN_FULL).hold(900)
                        .addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(ammo <= 1 ? 100 : 0, 0, 0).hold(750).setPos(0, 0, 0)
                        .hold(750).addPos(0, .5, 2.25, 50));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("TILT", seq().hold(500).addPos(-30, 0, 0, 150, IType.SIN_FULL).hold(800)
                        .addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("TURN", seq().hold(500).addPos(0, 0, 25, 150, IType.SIN_FULL).hold(800)
                        .addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(750).addPos(0, 0, -.5, 100, IType.SIN_FULL).hold(100)
                        .addPos(0, 0, 0, 100, IType.SIN_UP).hold(100).addPos(0, 0, -.5, 100, IType.SIN_FULL)
                        .hold(100).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("BULLET", seq().setPos(0, .5, 2.25).hold(750).addPos(0, .5, 1.25, 100, IType.SIN_FULL)
                        .hold(100).addPos(0, .5, 2.25, 100, IType.SIN_UP).hold(100)
                        .addPos(0, .5, 1.25, 100, IType.SIN_FULL).hold(100).addPos(0, .5, 2.25, 100, IType.SIN_UP));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("TILT", seq().addPos(-30, 0, 0, 250, IType.SIN_FULL).hold(1500)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("TURN", seq().addPos(0, 0, 25, 250, IType.SIN_FULL).hold(1500)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().hold(350).addPos(0, 0, -.5, 100, IType.SIN_FULL).hold(1125)
                        .addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("BULLET", ammo <= 1 ? seq().setPos(100, 0, 0)
                        : seq().setPos(0, .5, 2.25).hold(350).addPos(0, .5, 1.25, 100, IType.SIN_FULL)
                                .hold(1125).addPos(0, .5, 2.25, 100, IType.SIN_UP));
        return null;
    }

    /** Exact XFactory50.LAMBDA_AMAT_ANIMS rails shared by all AMAT variants. */
    private static LegacyBusAnimation amat(int type) {
        double turn = -60.0D;
        double pullAmount = -2.5D;
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("BIPOD", seq().hold(500).addPos(80, 0, 0, 350).addPos(80, 25, 0, 150));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, -.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("LIFT", seq().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("LIFT", seq().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -10, 0, 350, IType.SIN_UP).addPos(0, 0, 0, 650, IType.SIN_UP))
                .addBus("LIFT", seq().hold(1000).addPos(-2, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL).hold(450).addPos(-3, 0, 0, 150, IType.SIN_DOWN)
                        .hold(300).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().hold(1500).addPos(0, 0, turn, 150).hold(700)
                        .addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(1600).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250)
                        .addPos(0, 0, 0, 200, IType.LINEAR));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(250).addPos(-15, 0, 0, 500, IType.SIN_FULL).holdUntil(1650)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).holdUntil(1250)
                        .addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP)
                        .addPos(0, 0, 0, 200, IType.LINEAR).addPos(0, 0, pullAmount, 250, IType.SIN_UP)
                        .addPos(0, 0, 0, 200, IType.LINEAR));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("SCOPE_THROW", seq().addPos(0, .5, 0, 100, IType.SIN_FULL).addPos(4, -2, 0, 500, IType.SIN_FULL)
                        .addPos(4, -2.5, 0, 100).addPos(4, 7, 0, 350, IType.SIN_FULL)
                        .addPos(4, -2.5, 0, 350, IType.SIN_DOWN).addPos(4, -2, 0, 100).hold(250)
                        .addPos(0, .5, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SCOPE_SPIN", seq().hold(700).addPos(-360, 0, 0, 700));
        return null;
    }

    /** Exact XFactoryDrill.LAMBDA_DRILL_ANIMS rails, including continuous CYCLE spin. */
    private static LegacyBusAnimation drill(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(-1, 0, 0).addPos(0, 0, 0, 750, IType.SIN_DOWN));
        if (type == CYCLE) {
            double deploy = LegacyHbmAnimations.getRelevantTransformation("DEPLOY")[0];
            double speed = LegacyHbmAnimations.getRelevantTransformation("SPEED")[0];
            double spin = LegacyHbmAnimations.getRelevantTransformation("SPIN")[0] % 360.0D;
            return new LegacyBusAnimation()
                    .addBus("DEPLOY", seq().setPos(deploy, 0, 0)
                            .addPos(1, 0, 0, (int) (500 * (1 - deploy)), IType.SIN_FULL).hold(1000)
                            .addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("SPIN", seq().setPos(spin, 0, 0).addPos(spin + 360 * 1.5, 0, 0, 1500)
                            .addPos(360 * 3, 0, 0, 750 + (int) (1000 * (1.0D - spin / 360.0D)), IType.SIN_DOWN))
                    .addBus("SPEED", seq().setPos(speed, 0, 0).addPos(1, 0, 0, 500).hold(1000)
                            .addPos(0, 0, 0, 750 + (int) (1000 * (1.0D - spin / 360.0D)), IType.SIN_DOWN));
        }
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("DEPLOY", seq().addPos(.25, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SPIN", seq().addPos(360, 0, 0, 1500, IType.SIN_DOWN))
                .addBus("SPEED", seq().addPos(.75, 0, 0, 250).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == INSPECT) return new LegacyBusAnimation().addBus("LIFT",
                seq().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        return null;
    }

    /** Exact XFactory556mm.LAMBDA_G3_ANIMS rails plus WeapnModG3SawedOff overrides. */
    private static LegacyBusAnimation g3(ItemStack stack, int type, boolean noStock) {
        boolean empty = primaryMagazineCount(stack) <= 0;
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, noStock ? 250 : 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("BOLT", seq().hold(20).addPos(0, 0, -4.5, 40).addPos(0, 0, 0, 40))
                .addBus("RECOIL", seq().addPos(0, 0,
                        (gunAiming(stack) || !noStock) ? -.25 : -.75,
                        25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("BOLT", seq().hold(250).addPos(0, 0, -.3125, 100).hold(25).addPos(0, 0, -2.75, 130)
                        .hold(50).addPos(0, 0, -2.4375, 50).addPos(0, 0, 0, 85))
                .addBus("PLUG", seq().hold(250).hold(125).addPos(0, 0, -2.4375, 130).hold(100).addPos(0, 0, 0, 85))
                .addBus("HANDLE", seq().hold(250).addPos(0, 90, 0, 100).hold(205).addPos(0, 0, 0, 50))
                .addBus("LIFT", seq().hold(400).addPos(-1, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -8, 0, 250, IType.SIN_UP).hold(1050).addPos(0, 0, 0, 250))
                .addBus("BOLT", seq().hold(200).addPos(0, 0, -.3125, 100).hold(10).addPos(0, 0, -3.25, 200)
                        .holdUntil(1875).addPos(0, 0, -2.9375, 50).addPos(0, 0, 0, 100))
                .addBus("PLUG", seq().hold(310).addPos(0, 0, -2.9375, 200).holdUntil(1925).addPos(0, 0, 0, 100))
                .addBus("HANDLE", seq().hold(200).addPos(0, 90, 0, 100).hold(210).addPos(0, 90, 45, 75)
                        .holdUntil(1775).addPos(0, 90, 0, 100).addPos(0, 0, 0, 50))
                .addBus("LIFT", seq().hold(750).addPos(-25, 0, 0, 500, IType.SIN_FULL).holdUntil(1550)
                        .addPos(-26, 0, 0, 100, IType.SIN_DOWN).addPos(-25, 0, 0, 100, IType.SIN_FULL)
                        .holdUntil(2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(empty ? 1 : 0, 0, 0).hold(1000));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -1, 0, 150).addPos(2, -1, 0, 150).addPos(2, 8, 0, 350, IType.SIN_DOWN)
                        .addPos(2, -2, 0, 350, IType.SIN_UP).addPos(2, -1, 0, 50).hold(100)
                        .addPos(0, -1, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("SPEEN", seq().hold(300).addPos(0, 360, 360, 700))
                .addBus("LIFT", seq().hold(1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(empty ? 1 : 0, 0, 0));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1250).addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("BOLT", seq().hold(1000).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100).hold(250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100))
                .addBus("PLUG", seq().hold(1000).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100).hold(250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100));
        return null;
    }

    /** Exact XFactory9mm.LAMBDA_GREASEGUN_ANIMS rails. */
    private static LegacyBusAnimation greasegun(ItemStack stack, int type) {
        boolean empty = primaryMagazineCount(stack) <= 0;
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(80, 0, 0, 0).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("STOCK", seq().setPos(0, 0, -4).hold(200).addPos(0, 0, 0, 300, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, gunAiming(stack) ? -.25 : -.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("FLAP", seq().addPos(0, 0, 15, 100, IType.SIN_DOWN).addPos(0, 0, -5, 100, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TURN", seq().hold(500).addPos(0, 0, -45, 250, IType.SIN_FULL).hold(750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("HANDLE", seq().hold(750).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -8, 0, 250, IType.SIN_UP).hold(750).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                .addBus("LIFT", seq().hold(500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TURN", seq().hold(1750).addPos(0, 0, -45, 250, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("HANDLE", seq().hold(2000).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(empty ? 1 : 0, 0, 0).hold(1000));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TURN", seq().hold(500).addPos(0, 0, -45, 250, IType.SIN_FULL).hold(1500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("HANDLE", seq().hold(750).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).hold(250).addPos(-90, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("TURN", seq().addPos(0, 0, -45, 150).addPos(0, 0, 45, 150).hold(50).addPos(0, 0, 0, 250).hold(500).addPos(0, 0, 45, 150).addPos(0, 0, -45, 150).addPos(0, 0, 0, 150))
                .addBus("FLAP", seq().hold(300).addPos(0, 0, 180, 150).hold(850).addPos(0, 0, 0, 150));
        return null;
    }

    /** Exact XFactory40mm.LAMBDA_FLAREGUN_ANIMS rails. */
    private static LegacyBusAnimation flaregun(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", seq().addPos(15, 0, 0, 50).hold(550).addPos(0, 0, 0, 100));
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("HAMMER",
                seq().addPos(15, 0, 0, 50).hold(550).addPos(0, 0, 0, 100));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("OPEN", seq().addPos(45, 0, 0, 200, IType.SIN_FULL).hold(750).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("SHELL", seq().addPos(4, -8, -4, 0).hold(200).addPos(0, 0, -5, 500, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("FLIP", seq().hold(200).addPos(25, 0, 0, 200, IType.SIN_DOWN).hold(800).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("OPEN", seq().hold(500).addPos(45, 0, 0, 200, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 200, IType.SIN_UP))
                .addBus("FLIP", seq().hold(700).addPos(25, 0, 0, 200, IType.SIN_DOWN).hold(550).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        if (type == INSPECT) return new LegacyBusAnimation().addBus("FLIP",
                seq().addPos(-1080, 0, 0, 1500, IType.SIN_FULL));
        return null;
    }

    /** XFactory40mm.LAMBDA_CONGOLAKE_ANIMS: all non-inline rails are authored JSON. */
    private static LegacyBusAnimation congolake(ItemStack stack, int selectedSlot, int type) {
        int ammo = primaryMagazineCount(stack);
        String animationName = switch (type) {
            case EQUIP -> "Equip";
            case CYCLE -> ammo <= 1 ? "FireEmpty" : "Fire";
            case RELOAD -> ammo == 0 ? "ReloadEmpty" : "ReloadStart";
            case RELOAD_CYCLE -> "Reload";
            case RELOAD_END -> "ReloadEnd";
            case JAMMED -> "Jammed";
            case INSPECT -> "Inspect";
            default -> null;
        };
        if (animationName != null) {
            ClientItemAnimationHandler.handle(selectedSlot, 0, stack.getItem().getDescriptionId(),
                    CONGOLAKE_ANIMATION_FILE, animationName, false);
        }
        return null;
    }

    /** Exact XFactory40mm.LAMBDA_MK108_ANIMS rails, including inspect grenade tosses. */
    private static LegacyBusAnimation mk108(ItemStack stack, int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(45, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == CYCLE) {
            int amount = primaryMagazineCount(stack);
            return new LegacyBusAnimation()
                    .addBus("RECOIL", seq().hold(50).addPos(0, 0, -.25, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("BARREL", seq().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("CYCLE", seq().hold(100).addPos(1, 0, 0, 150))
                    .addBus("SHELLS", seq().setPos(amount - 1, 0, 0));
        }
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("HAMMER",
                seq().addPos(15, 0, 0, 50).hold(550).addPos(0, 0, 0, 100));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LIFT", seq().addPos(10, 0, 0, 500, IType.SIN_FULL).holdUntil(1250).addPos(-50, 0, 0, 750, IType.SIN_FULL).holdUntil(5500).addPos(0, 0, 0, 500, IType.SIN_FULL).hold(500).addPos(1, 0, 0, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("LID", seq().addPos(60, 0, 0, 500, IType.SIN_FULL).holdUntil(6000).addPos(0, 0, 0, 500, IType.SIN_UP))
                .addBus("BELT", seq().setPos(1, 0, 0).hold(500).addPos(0, 0, 0, 750, IType.SIN_UP).holdUntil(4500).addPos(1, 0, 0, 750, IType.SIN_UP))
                .addBus("DRUM", seq().hold(2000).addPos(2.5, 0, 0, 500, IType.SIN_DOWN).addPos(2.5, -2, -8, 500, IType.SIN_UP).setPos(4, -3, -8).addPos(2.5, 0, 0, 1000, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LID", seq().hold(250).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_UP))
                .addBus("LIFT", seq().hold(1000).addPos(1, 0, 0, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL));
        if (type == INSPECT) {
            int yeetHorizontal = 750, untilImpact = yeetHorizontal * 9 / 15, delay = 250, height = 6;
            int arcUp = untilImpact * 5 / 8, arcDown = untilImpact * 3 / 8;
            return new LegacyBusAnimation()
                    .addBus("LIFT", seq().hold(untilImpact).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL).hold(delay - 150).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL).hold(delay - 150).addPos(1, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("GRENH1", seq().setPos(9, 0, 0).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV1", seq().setPos(0, -2, 0).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS1", seq().addPos(720, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact))
                    .addBus("GRENH2", seq().setPos(9, 0, 0).hold(delay).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV2", seq().setPos(0, -2, 0).hold(delay).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS2", seq().hold(delay).addPos(720, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact))
                    .addBus("GRENH3", seq().setPos(9, 0, 0).hold(delay * 2).addPos(-6, 0, 0, yeetHorizontal))
                    .addBus("GRENV3", seq().setPos(0, -2, 0).hold(delay * 2).addPos(0, height, 0, arcUp, IType.SIN_DOWN).addPos(0, 2, 0, arcDown, IType.SIN_UP).addPos(0, 3, 0, yeetHorizontal - untilImpact, IType.SIN_DOWN))
                    .addBus("GRENS3", seq().hold(delay * 2).addPos(720, 0, 0, untilImpact).setPos(0, 0, 0).addPos(360, 0, 0, yeetHorizontal - untilImpact));
        }
        return null;
    }

    private static LegacyBusAnimation am180(ItemStack stack, int type) {
        boolean aiming = gunAiming(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(0, 0, aiming ? -.125 : -.25, 15, IType.SIN_DOWN).addPos(0, 0, 0, 35, IType.SIN_FULL));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("BOLT", seq().addPos(0, 0, 0, 550).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("TURN", turn(300, 15));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAGTURN", seq().addPos(15, 0, 0, 250, IType.SIN_FULL).addPos(15, 0, 0, 250).addPos(15, 0, 70, 300, IType.SIN_FULL).addPos(15, 0, 0, 0).addPos(15, 0, 0, 750).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("MAG", seq().addPos(0, 0, 0, 250).addPos(2, 0, -4, 250, IType.SIN_FULL).addPos(-10, 2, -4, 300, IType.SIN_UP).addPos(3, -6, -4, 0).addPos(2, 0, -4, 500, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BOLT", seq().addPos(0, 0, 0, 2250).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("TURN", turn(2000, 15));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("BOLT", seq().addPos(0, 0, 0, 750).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("TURN", turn(500, 45));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("MAGTURN", seq().addPos(15, 0, 0, 250, IType.SIN_FULL).addPos(15, 0, 0, 1400).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("MAG", seq().addPos(0, 0, 0, 200).addPos(4, -1, -4, 200, IType.SIN_FULL).addPos(4, -1.5, -4, 50).addPos(4, 0, -4, 100).addPos(4, 6, -4, 250, IType.SIN_DOWN).addPos(4, 0, -4, 150, IType.SIN_UP).addPos(4, -1, -4, 100, IType.SIN_DOWN).addPos(4, -1, -4, 250).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("MAGSPIN", seq().addPos(0, 0, 0, 600).addPos(-400, 0, 0, 500, IType.SIN_FULL).addPos(-400, 0, 0, 250).addPos(-360, 0, 0, 250));
        return null;
    }

    private static LegacyBusAnimation tesla(ItemStack stack, int type) {
        boolean aiming = gunAiming(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, aiming ? -.5 : -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("CYCLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350));
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("CYCLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("YOMI", seq().addPos(8, -4, 0, 0).addPos(4, -1, 0, 500, IType.SIN_DOWN).addPos(4, -1, 0, 1000).addPos(6, -6, 0, 500, IType.SIN_UP))
                .addBus("SQUEEZE", seq().addPos(1, 1, 1, 0).addPos(1, 1, 1, 750).addPos(1, 1, .5, 125).addPos(1, 1, 1, 125));
        return null;
    }

    private static LegacyBusAnimation laserPistol(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(0, 0, -.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LATCH", seq().addPos(0, -20, 0, 100).hold(1900).addPos(0, 0, 0, 100))
                .addBus("LIFT", seq().hold(100).addPos(-45, 0, 0, 250, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("JOLT", seq().hold(350).addPos(0, 0, .5, 100, IType.SIN_FULL).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL).holdUntil(2100).addPos(-.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BATTERY", seq().hold(550).addPos(0, 0, 5, 250).hold(550).setPos(0, -2, -2).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_UP));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LATCH", seq().hold(500).addPos(0, -20, 0, 100).hold(250).addPos(0, 0, 0, 100))
                .addBus("JOLT", seq().hold(950).addPos(-.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("EQUIP", seq().hold(1500).addPos(7.5, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation().addBus("SWIRL", seq().addPos(-720, 0, 0, 750, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 750, IType.SIN_FULL));
        return null;
    }

    /** Exact bus layout of XFactoryEnergy.LAMBDA_LASRIFLE. */
    private static LegacyBusAnimation lasrifle(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(0, 0, -.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LEVER", seq().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 1500).addPos(0, 0, 0, 350, IType.SIN_UP))
                .addBus("MAG", seq().addPos(0, 0, 0, 350).addPos(0, -5, 0, 350, IType.SIN_UP).addPos(0, -5, 0, 500).addPos(0, -.25, 0, 500, IType.SIN_FULL).addPos(0, -.25, 0, 150).addPos(0, 0, 0, 350))
                .addBus("EQUIP", seq().addPos(0, 0, 0, 1700).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LEVER", seq().addPos(0, 0, 0, 500).addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                .addBus("MAG", seq().addPos(0, 0, 0, 500).addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -.25, 0, 250, IType.SIN_FULL).addPos(0, -.25, 0, 150).addPos(0, 0, 0, 350))
                .addBus("EQUIP", seq().addPos(0, 0, 0, 500).addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LEVER", seq().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                .addBus("MAG", seq().addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -.25, 0, 250, IType.SIN_FULL).addPos(0, -.25, 0, 150).addPos(0, 0, 0, 350))
                .addBus("EQUIP", seq().addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        return null;
    }

    /** Exact bus layout of XFactory75Bolt.LAMBDA_BOLTER_ANIMS. */
    private static LegacyBusAnimation bolter(int type) {
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(1, 0, 0, 25).addPos(0, 0, 0, 75));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("TILT", seq().addPos(1, 0, 0, 250).addPos(1, 0, 0, 1500).addPos(0, 0, 0, 250))
                .addBus("MAG", seq().addPos(0, 0, 1, 500).addPos(1, 0, 1, 500).addPos(0, 0, 0, 500));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("TILT", seq().addPos(0, 0, 0, 500).addPos(1, 0, 0, 250).addPos(1, 0, 0, 700).addPos(0, 0, 0, 250))
                .addBus("MAG", seq().addPos(0, 0, 0, 750).addPos(.6, 0, 0, 250).addPos(0, 0, 0, 250));
        return null;
    }

    /** Exact default JSON and optional legacy rails of XFactory556mm.LAMBDA_STG77_ANIMS. */
    private static LegacyBusAnimation stg77(ItemStack stack, int selectedSlot, int type) {
        if (!HbmClientConfig.legacyGunAnimations()) {
            String animationName = switch (type) {
                case CYCLE -> "Fire";
                case CYCLE_DRY -> "FireDry";
                case RELOAD -> "Reload";
                case INSPECT -> "Inspect";
                default -> null;
            };
            if (animationName != null) {
                ClientItemAnimationHandler.handle(selectedSlot, 0, stack.getItem().getDescriptionId(),
                        STG77_ANIMATION_FILE, animationName, false);
            }
            if (type != EQUIP) return null;
        }

        boolean aiming = gunAiming(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, aiming ? -.125 : -.375, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL))
                .addBus("SAFETY", seq().addPos(.25, 0, 0, 0).addPos(.25, 0, 0, 2000).addPos(0, 0, 0, 50));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("BOLT", seq().addPos(0, 0, 0, 250).addPos(0, 0, -2, 150).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("SAFETY", seq().addPos(.25, 0, 0, 0).addPos(.25, 0, 0, 2000).addPos(0, 0, 0, 50));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("BOLT", seq().addPos(0, 0, -2, 150).addPos(0, 0, -2, 1600).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("HANDLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 20, 50).addPos(0, 0, 20, 1500).addPos(0, 0, 0, 50))
                .addBus("LIFT", seq().addPos(0, 0, 0, 200).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("BOLT", seq().addPos(0, 0, -2, 150).addPos(0, 0, -2, 6100).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("HANDLE", seq().addPos(0, 0, 0, 150).addPos(0, 0, 20, 50).addPos(0, 0, 20, 6000).addPos(0, 0, 0, 50))
                .addBus("INSPECT_LEVER", seq().addPos(0, 0, 0, 500).addPos(0, 0, -10, 100).addPos(0, 0, -10, 100).addPos(0, 0, 0, 100))
                .addBus("INSPECT_BARREL", seq().addPos(0, 0, 0, 600).addPos(0, 0, 20, 150).addPos(0, 0, 0, 400).addPos(0, 0, 0, 500).addPos(15, 0, 0, 500).addPos(15, 0, 0, 2000).addPos(0, 0, 0, 500).addPos(0, 0, 0, 500).addPos(0, 0, 20, 200).addPos(0, 0, 20, 400).addPos(0, 0, 0, 150))
                .addBus("INSPECT_MOVE", seq().addPos(0, 0, 0, 750).addPos(0, 0, 6, 1000).addPos(2, 0, 3, 500, IType.SIN_FULL).addPos(2, .75, 0, 500, IType.SIN_FULL).addPos(2, .75, 0, 1000).addPos(2, 0, 3, 500, IType.SIN_FULL).addPos(0, 0, 6, 500).addPos(0, 0, 0, 1000))
                .addBus("INSPECT_GUN", seq().addPos(0, 0, 0, 1750).addPos(15, 0, -70, 500, IType.SIN_FULL).addPos(15, 0, -70, 1500).addPos(0, 0, 0, 500, IType.SIN_FULL));
        return null;
    }

    /** XFactory9mm.LAMBDA_LAG_ANIMS: authored JSON for every animated event except the equip rail. */
    private static LegacyBusAnimation lag(ItemStack stack, int selectedSlot, int type) {
        if (type == EQUIP) {
            return new LegacyBusAnimation().addBus("EQUIP",
                    seq().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
        }
        String animationName = switch (type) {
            case CYCLE -> "Firing";
            case CYCLE_DRY -> "Dryfire";
            case RELOAD -> "Reload";
            case JAMMED -> "Jam";
            case INSPECT -> "Inspect";
            default -> null;
        };
        if (animationName != null) {
            ClientItemAnimationHandler.handle(selectedSlot, 0, stack.getItem().getDescriptionId(),
                    LAG_ANIMATION_FILE, animationName, false);
        }
        return null;
    }

    /** Exact XFactory10ga.LAMBDA_DOUBLE_BARREL_ANIMS rails shared by both variants. */
    private static LegacyBusAnimation doubleBarrel(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("BUCKLE", seq().addPos(0, -60, 0, 50).addPos(0, 0, 0, 250));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("TURN", seq().addPos(0, 30, 0, 350, IType.SIN_FULL).hold(1150).addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("LEVER", seq().hold(250).addPos(0, 0, -90, 100, IType.SIN_FULL).hold(1300).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", seq().hold(300).addPos(60, 0, 0, 150, IType.SIN_UP).hold(1150).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", seq().hold(350).addPos(-5, 0, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .hold(700).addPos(-5, 0, 0, 100, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_UP)
                        .addPos(45, 0, 0, 150).hold(150).addPos(-5, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("SHELLS", seq().hold(450).addPos(0, 0, -2.5, 100).addPos(0, -5, -5, 350, IType.SIN_DOWN)
                        .setPos(0, -3, -2).addPos(0, 0, -2, 250).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("SHELL_FLIP", seq().hold(450).addPos(-360, 0, 0, 450).setPos(0, 0, 0));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LEVER", seq().hold(250).addPos(0, 0, -90, 100, IType.SIN_FULL).hold(800).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", seq().hold(300).addPos(60, 0, 0, 150, IType.SIN_UP).hold(650).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", seq().hold(350).addPos(-5, 0, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .hold(200).addPos(-5, 0, 0, 100, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_UP)
                        .addPos(45, 0, 0, 150).hold(150).addPos(-5, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        return null;
    }

    /** XFactory44.LAMBDA_NOPIP_ANIMS and the LILMAC equip override. */
    private static LegacyBusAnimation debug(int type) {
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().hold(50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(400).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(450).addPos(0, 0, 1, 200));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(400).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(450).addPos(0, 0, 1, 200));
        if (type == EQUIP) return new LegacyBusAnimation().addBus("ROTATE", seq().addPos(-360, 0, 0, 350));
        if (type == RELOAD) return heavyRevolverReload(1450, 1700, 1050, 200, 950, 700);
        if (type == INSPECT || type == JAMMED) return new LegacyBusAnimation()
                .addBus("RELAOD_TILT", seq().addPos(-15, 0, 0, 100).addPos(65, 0, 0, 100).addPos(45, 0, 0, 50)
                        .addPos(0, 0, 0, 200).hold(200).addPos(-80, 0, 0, 100).hold(100).addPos(0, 0, 0, 200))
                .addBus("RELOAD_CYLINDER", seq().hold(200).addPos(90, 0, 0, 100).hold(450).addPos(0, 0, 0, 70));
        return null;
    }

    /** XFactory44.LAMBDA_NOPIP_ANIMS and the LILMAC equip override. */
    private static LegacyBusAnimation heavyRevolver(int type, boolean lilmac) {
        if (type == EQUIP) {
            return lilmac
                    ? new LegacyBusAnimation().addBus("SPIN", seq().addPos(-360, 0, 0, 350))
                    : new LegacyBusAnimation().addBus("ROTATE", seq().addPos(90, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        }
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().hold(50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(400).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(450).addPos(0, 0, 1, 200));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("HAMMER", seq().addPos(0, 0, 1, 50).hold(400).addPos(0, 0, 0, 200))
                .addBus("DRUM", seq().hold(450).addPos(0, 0, 1, 200));
        if (type == RELOAD) return heavyRevolverReload(1450, 1700, 1050, 200, 950, 700);
        if (type == INSPECT || type == JAMMED) return new LegacyBusAnimation()
                .addBus("RELAOD_TILT", seq().addPos(-15, 0, 0, 100).addPos(65, 0, 0, 100).addPos(45, 0, 0, 50)
                        .addPos(0, 0, 0, 200).hold(200).addPos(-80, 0, 0, 100).hold(100).addPos(0, 0, 0, 200))
                .addBus("RELOAD_CYLINDER", seq().hold(200).addPos(90, 0, 0, 100).hold(450).addPos(0, 0, 0, 70));
        return null;
    }

    private static LegacyBusAnimation heavyRevolverReload(int tiltHold, int cylinderHold, int liftHold,
            int bulletHold, int bulletsConHold, int bulletsReturn) {
        return new LegacyBusAnimation()
                .addBus("RELAOD_TILT", seq().addPos(-15, 0, 0, 100).addPos(65, 0, 0, 100).addPos(45, 0, 0, 50)
                        .addPos(0, 0, 0, 200).hold(tiltHold).addPos(-80, 0, 0, 100).hold(100).addPos(0, 0, 0, 200))
                .addBus("RELOAD_CYLINDER", seq().hold(200).addPos(90, 0, 0, 100).hold(cylinderHold).addPos(0, 0, 0, 70))
                .addBus("RELOAD_LIFT", seq().hold(350).addPos(-45, 0, 0, 250).hold(350).addPos(-15, 0, 0, 200)
                        .hold(liftHold).addPos(0, 0, 0, 100))
                .addBus("RELOAD_JOLT", seq().hold(600).addPos(2, 0, 0, 50).addPos(0, 0, 0, 100))
                .addBus("RELOAD_BULLETS", seq().hold(650).addPos(10, 0, 0, 300).hold(bulletHold).addPos(0, 0, 0, bulletsReturn))
                .addBus("RELOAD_BULLETS_CON", seq().setPos(1, 0, 0).hold(bulletsConHold).setPos(0, 0, 0));
    }

    /** Exact XFactory762mm.LAMBDA_CARBINE_ANIMS rails. */
    private static LegacyBusAnimation carbine(ItemStack stack, int type, boolean bayonet) {
        int ammo = primaryMagazineCount(stack);
        if (bayonet && type == INSPECT) return new LegacyBusAnimation().addBus("STAB",
                seq().addPos(0, 1, -2, 250, IType.SIN_DOWN).hold(250).addPos(0, 1, 5, 250, IType.SIN_UP)
                        .hold(250).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(45, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, gunAiming(stack) ? -.25 : -.5, 50, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus(ammo <= 1 ? "NULL" : "REL", seq().addPos(0, 0, .25, 50)
                        .addPos(0, .125, 1.25, 100, IType.SIN_UP));
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("SLIDE",
                seq().addPos(0, 0, 0, 500).addPos(0, 0, -1, 100, IType.SIN_DOWN).hold(50)
                        .addPos(0, 0, 0, 100, IType.SIN_UP));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("MAG", seq().addPos(0, -4, 0, 250, IType.SIN_UP).hold(750)
                        .addPos(0, 0, 0, 500, IType.SIN_DOWN))
                .addBus("LIFT", seq().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1000))
                .addBus("BULLET", seq().setPos(ammo == 0 ? 1 : 0, 0, 0).addPos(0, 0, 0, 1000));
        if (type == RELOAD_END) return new LegacyBusAnimation()
                .addBus("LIFT", seq().setPos(-25, 0, 0).hold(750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 250).addPos(0, 0, -1, 100, IType.SIN_DOWN).hold(50)
                        .addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("REL", seq().addPos(0, 0, 0, 250).addPos(0, 0, .25, 150)
                        .addPos(0, .125, 1.25, 100, IType.SIN_UP));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().setPos(-25, 0, 0).hold(750).addPos(0, 0, 0, 500, IType.SIN_FULL)
                        .hold(250).addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(750)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 250).addPos(0, 0, -1, 100, IType.SIN_DOWN).hold(50)
                        .addPos(0, 0, -.25, 100, IType.SIN_UP).hold(1250).addPos(0, 0, -1, 100, IType.SIN_DOWN)
                        .hold(50).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus("REL", seq().addPos(0, 0, 0, 250).addPos(0, 0, .25, 150)
                        .addPos(0, .125, 1, 100, IType.SIN_UP).hold(1250).addPos(0, .125, .25, 100, IType.SIN_DOWN)
                        .addPos(0, .125, 1, 100, IType.SIN_UP));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LIFT", seq().addPos(-25, 0, 0, 250, IType.SIN_FULL).hold(1500)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 500).addPos(0, 0, -.75, 150, IType.SIN_DOWN)
                        .hold(1000).addPos(0, 0, 0, 100, IType.SIN_UP))
                .addBus(ammo == 0 ? "NULL" : "REL", seq().setPos(0, .125, 1.25).hold(500)
                        .addPos(0, .125, .5, 150, IType.SIN_DOWN).hold(1000)
                        .addPos(0, .125, 1.25, 100, IType.SIN_UP));
        return null;
    }

    /** Exact XFactory762mm.LAMBDA_MINIGUN_ANIMS rails for the single-gun models. */
    private static LegacyBusAnimation minigun(ItemStack stack, int type) {
        double recoil = gunAiming(stack) ? -.25 : -.5;
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(45, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().setPos(0, 0, recoil).hold(100).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("ROTATE", seq().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("ROTATE",
                seq().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(-15, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("ROTATE", seq().addPos(0, 0, 60, 50).addPos(0, 0, 720, 1000, IType.SIN_DOWN));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(3, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("ROTATE", seq().addPos(0, 0, -720, 1000, IType.SIN_DOWN));
        return null;
    }

    /** Exact XFactory762mm.LAMBDA_MAS36_ANIMS rails. */
    private static LegacyBusAnimation mas36(ItemStack stack, int type, boolean bayonet) {
        int magazine = primaryMagazineCount(stack);
        double turn = -90.0D;
        double pullAmount = gunAiming(stack) ? -1.0D : -1.5D;
        if (bayonet && type == INSPECT) return new LegacyBusAnimation().addBus("STAB",
                seq().addPos(0, 1, -2, 250, IType.SIN_DOWN).hold(250).addPos(0, 1, 5, 250, IType.SIN_UP)
                        .hold(250).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("STOCK", seq().setPos(-158, 0, 0).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("EQUIP", seq().setPos(45, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL).hold(500)
                        .addPos(1, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, -.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("LIFT", seq().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BULLET", magazine <= 1 ? seq().setPos(-100, 0, 0)
                        : seq().hold(850).addPos(0, .1875, 1.5, 200, IType.LINEAR));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).hold(700).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP).hold(250)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("LIFT", seq().hold(600).addPos(-3, 0, 0, 150, IType.SIN_DOWN).hold(300)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BULLET", seq().setPos(-100, 0, 0));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("BOLT_TURN", seq().addPos(0, 0, turn, 150).holdUntil(2000).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(100).addPos(0, 0, -1.5D, 250, IType.SIN_UP).holdUntil(1800)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("BULLET", seq().setPos(-100, 0, 0).holdUntil(1200).setPos(0, 0, 0).hold(600)
                        .addPos(0, .1875, 1.5, 200, IType.LINEAR))
                .addBus("LIFT", seq().hold(200).addPos(30, 0, 0, 500, IType.SIN_FULL).holdUntil(1200)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SHOW_CLIP", seq().setPos(1, 1, 1))
                .addBus("CLIP", seq().setPos(2, -3, 0).hold(250).addPos(.5, 1, 0, 500, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL).hold(400).addPos(-.5, .5, 0, 150)
                        .addPos(-3, -3, 0, 250, IType.SIN_UP))
                .addBus("BULLETS", seq().setPos(2, -3, 0).hold(250).addPos(.5, 1, 0, 500, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL).hold(150).addPos(0, -1.5, 0, 250, IType.SIN_DOWN));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(250).addPos(-15, 0, 0, 500, IType.SIN_FULL).holdUntil(1650)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().hold(250).addPos(0, 0, turn, 150).holdUntil(1250)
                        .addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(350).addPos(0, 0, pullAmount, 250, IType.SIN_UP)
                        .addPos(0, 0, 0, 200, IType.LINEAR).addPos(0, 0, pullAmount, 250, IType.SIN_UP)
                        .addPos(0, 0, 0, 200, IType.LINEAR));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("LIFT", seq().hold(350).addPos(-3, 0, 0, 150, IType.SIN_DOWN).holdUntil(1050)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("BOLT_TURN", seq().addPos(0, 0, turn, 150).holdUntil(1050).addPos(0, 0, 0, 150))
                .addBus("BOLT_PULL", seq().hold(100).addPos(0, 0, -1, 250, IType.SIN_UP).hold(500)
                        .addPos(0, 0, 0, 200, IType.LINEAR))
                .addBus("BULLET", magazine == 0 ? seq().setPos(-100, 0, 0)
                        : seq().setPos(0, .1875, 1.5).hold(100).addPos(0, .125, .5, 250, IType.SIN_UP).hold(500)
                                .addPos(0, .1875, 1.5, 200, IType.LINEAR));
        return null;
    }

    /** Exact XFactoryTool.LAMBDA_CT_ANIMS rails. */
    private static LegacyBusAnimation chargeThrower(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL",
                seq().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("RAISE", seq().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("AMMO", seq().setPos(0, -10, -5).hold(500).addPos(0, 0, 5, 750, IType.SIN_FULL)
                        .addPos(0, 0, 0, 500, IType.SIN_UP).hold(4000))
                .addBus("TWIST", seq().setPos(0, 0, 25).hold(2000).addPos(0, 0, 0, 150));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("TURN", seq().addPos(0, 60, 0, 500, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("ROLL", seq().hold(750).addPos(0, 0, -90, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactory35800.LAMBDA_ABERRATOR rails shared by Aberrator and EOTT. */
    private static LegacyBusAnimation aberrator(ItemStack stack, int type) {
        boolean aiming = gunAiming(stack);
        int ammo = primaryMagazineCount(stack);
        if (type == EQUIP) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().setPos(360, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("RISE", seq().setPos(0, -3, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, 0, 50).addPos(aiming ? -15 : -25, 0, 0, 100, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("SIGHT", seq().addPos(0, 0, 0, 50).addPos(aiming ? 5 : 15, 0, 0, 100, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 50).addPos(0, 0, -1.125, 50, IType.SIN_DOWN)
                        .addPos(0, 0, -1.125, 50).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus(ammo <= 1 ? "NULL" : "BULLET", seq().addPos(0, 0, 0, 150)
                        .addPos(0, .375, 1.125, 150, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(45, 0, 0, 50).addPos(-45, 0, -1.125, 50, IType.SIN_DOWN)
                        .addPos(-20, 0, -1.125, 50).addPos(0, 0, 0, 150, IType.SIN_UP));
        if (type == CYCLE_DRY) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, 0, 700).addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 550).addPos(0, 0, -1.125, 150, IType.SIN_FULL)
                        .addPos(0, 0, -1.125, 50).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(45, 0, 0, 50).addPos(45, 0, 0, 500)
                        .addPos(-45, 0, -1.125, 150, IType.SIN_FULL).addPos(-20, 0, -1.125, 50)
                        .addPos(0, 0, 0, 150, IType.SIN_UP));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("ROLL", seq().addPos(0, 0, 20, 150, IType.SIN_FULL).addPos(0, 0, 20, 50)
                        .addPos(0, 0, -45, 150, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("MAG", seq().addPos(0, 0, 0, 350).setPos(0, -2, 0)
                        .addPos(-15, -5, 0, 350).setPos(-15, 0, 0).hold(700).setPos(3, 3, 0)
                        .addPos(0, -2, 0, 250, IType.SIN_DOWN).addPos(0, -2, 0, 50)
                        .addPos(0, 0, 0, 150, IType.SIN_DOWN))
                .addBus("MAGROLL", seq().addPos(0, 0, 0, 350).addPos(0, 0, -180, 250).setPos(0, 0, 0))
                .addBus("EQUIP", seq().addPos(0, 0, 0, 750).addPos(5, 0, 0, 150, IType.SIN_FULL)
                        .addPos(-190, 0, 0, 500, IType.SIN_FULL).hold(450)
                        .addPos(-360, 0, 0, 350, IType.SIN_DOWN).setPos(0, 0, 0))
                .addBus("RECOIL", seq().addPos(0, 0, 0, 2350).addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("SLIDE", seq().addPos(0, 0, 0, 2200).addPos(0, 0, -1.125, 150, IType.SIN_FULL)
                        .addPos(0, 0, -1.125, 50).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("HAMMER", seq().addPos(0, 0, 0, 2250).addPos(-45, 0, -1.125, 100, IType.SIN_FULL)
                        .addPos(-20, 0, -1.125, 50).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("BULLET", seq().setPos(ammo > 0 ? 0 : -100, 0, 0).hold(2400).setPos(0, 0, 0)
                        .addPos(0, .375, 1.125, 150, IType.SIN_UP));
        if (type == INSPECT) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(0, 0, 0, 0).addPos(-720, 0, 0, 1000, IType.SIN_FULL).hold(250)
                        .addPos(0, 0, 0, 1000, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactoryRocket.LAMBDA_PANZERSCHRECK_ANIMS rails shared by Panzerschreck and Stinger. */
    private static LegacyBusAnimation panzerschreck(ItemStack stack, int type, boolean noShield) {
        boolean empty = primaryMagazineCount(stack) <= 0;
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, noShield ? 250 : 500, IType.SIN_DOWN));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("RELOAD", seq().addPos(90, 0, 0, 750, IType.SIN_FULL).hold(1000)
                        .addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("ROCKET", seq().setPos(0, -3, -6).hold(750).addPos(0, 0, -6.5, 500, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 350, IType.SIN_UP));
        if (type == JAMMED) empty = false;
        if (type == JAMMED || type == INSPECT) return new LegacyBusAnimation()
                .addBus("RELOAD", seq().addPos(90, 0, 0, 750, IType.SIN_FULL).hold(500)
                        .addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("ROCKET", seq().setPos(0, empty ? -3 : 0, 0));
        return null;
    }

    /** Exact XFactoryRocket.LAMBDA_QUADRO_ANIMS rails. */
    private static LegacyBusAnimation quadro(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL",
                seq().addPos(0, 0, -0.5, 50).addPos(0, 0, 0, 50));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("RELOAD_ROTATE", seq().addPos(0, 0, 60, 500, IType.SIN_FULL).hold(1500)
                        .addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("RELOAD_PUSH", seq().setPos(-1, -1, 0).hold(500).addPos(-1, 0, 0, 350)
                        .addPos(0, 0, 0, 1000));
        if (type == JAMMED || type == INSPECT) return new LegacyBusAnimation().addBus("RELOAD_ROTATE",
                seq().addPos(0, 0, 60, 750, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 750, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactoryRocket.LAMBDA_MISSILE_LAUNCHER_ANIMS rails. */
    private static LegacyBusAnimation missileLauncher(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("BARREL", seq().addPos(0, 0, 1.5, 150).hold(2100).addPos(0, 0, 0, 150))
                .addBus("OPEN", seq().hold(250).addPos(90, 0, 0, 500, IType.SIN_FULL).hold(1000)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("EQUIP", seq().hold(2250).addPos(-1, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("MISSILE", seq().setPos(-10, 0, 0).hold(750).setPos(3, 0, 2)
                        .addPos(0, 0, -6, 350, IType.SIN_FULL).addPos(0, 0, 0, 350, IType.SIN_UP));
        if (type == JAMMED || type == INSPECT) return new LegacyBusAnimation()
                .addBus("BARREL", seq().addPos(0, 0, 1.5, 150).hold(1350).addPos(0, 0, 0, 150))
                .addBus("OPEN", seq().hold(250).addPos(90, 0, 0, 500, IType.SIN_FULL).hold(250)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("EQUIP", seq().hold(1500).addPos(-1, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 150, IType.SIN_UP));
        return null;
    }

    /** Exact XFactory12ga.LAMBDA_SEXY_ANIMS rails, also used by XFactory10ga Heretic. */
    private static LegacyBusAnimation sexy(ItemStack stack, int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(45, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == CYCLE) {
            int amount = primaryMagazineCount(stack);
            return new LegacyBusAnimation()
                    .addBus("RECOIL", seq().hold(50).addPos(0, 0, -.25, 50, IType.SIN_DOWN)
                            .addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BARREL", seq().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150))
                    .addBus("CYCLE", seq().addPos(1, 0, 0, 150))
                    .addBus("HOOD", seq().hold(50).addPos(3, 0, 0, 50, IType.SIN_DOWN)
                            .addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("SHELLS", seq().setPos(amount - 1, 0, 0));
        }
        if (type == CYCLE_DRY) return new LegacyBusAnimation().addBus("CYCLE", seq().addPos(0, 0, 18, 50));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LOWER", seq().addPos(15, 0, 0, 500, IType.SIN_FULL).hold(2750)
                        .addPos(12, 0, 0, 100, IType.SIN_DOWN).addPos(15, 0, 0, 100, IType.SIN_FULL)
                        .hold(1050).addPos(18, 0, 0, 100, IType.SIN_DOWN)
                        .addPos(15, 0, 0, 100, IType.SIN_FULL).hold(300)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("LEVER", seq().addPos(0, 0, 1, 150).hold(4700).addPos(0, 0, 0, 150))
                .addBus("HOOD", seq().hold(250).addPos(60, 0, 0, 500, IType.SIN_FULL).hold(3250)
                        .addPos(0, 0, 0, 500, IType.SIN_UP))
                .addBus("BELT", seq().setPos(1, 0, 0).hold(750).addPos(0, 0, 0, 500, IType.SIN_UP)
                        .hold(2000).addPos(1, 0, 0, 500, IType.SIN_UP))
                .addBus("MAG", seq().hold(1500).addPos(0, -1, 0, 250, IType.SIN_UP)
                        .addPos(2, -1, 0, 500, IType.SIN_UP).addPos(7, 1, 0, 250, IType.SIN_UP)
                        .addPos(15, 2, 0, 250).setPos(0, -2, 0).addPos(0, 0, 0, 500, IType.SIN_UP))
                .addBus("MAGROT", seq().hold(2250).addPos(0, 0, -180, 500, IType.SIN_FULL)
                        .setPos(0, 0, 0));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("BOTTLE", seq().setPos(8, -8, -2).addPos(6, -4, -2, 500, IType.SIN_DOWN)
                        .addPos(3, -3, -5, 500, IType.SIN_FULL).addPos(3, -2, -5, 1000)
                        .addPos(4, -6, -2, 750, IType.SIN_FULL).addPos(6, -8, -2, 500, IType.SIN_UP))
                .addBus("SIP", seq().setPos(25, 0, 0).hold(500).addPos(-90, 0, 0, 500, IType.SIN_FULL)
                        .addPos(-110, 0, 0, 1000).addPos(25, 0, 0, 750, IType.SIN_FULL));
        return null;
    }

    /** XFactoryFlamer.LAMBDA_CHEMTHROWER_ANIMS has only this source equip rail. */
    private static LegacyBusAnimation chemthrower(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        return null;
    }

    /** XFactoryFlamer rails: Reload is the authored source JSON; the remaining rails are inline. */
    private static LegacyBusAnimation flamer(ItemStack stack, int selectedSlot, int type) {
        if (type == RELOAD) {
            ClientItemAnimationHandler.handle(selectedSlot, 0, stack.getItem().getDescriptionId(),
                    FLAMETHROWER_ANIMATION_FILE, "Reload", false);
            return null;
        }
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        if (type == INSPECT || type == JAMMED) return new LegacyBusAnimation().addBus("ROTATE",
                seq().addPos(0, 0, 45, 250, IType.SIN_FULL).hold(350)
                        .addPos(0, 0, -15, 150, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactoryCatapult.LAMBDA_FATMAN_ANIMS rails. */
    private static LegacyBusAnimation fatman(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        if (type == CYCLE) {
            int gaugeAngle = 135 + (Minecraft.getInstance().player == null ? 0
                    : Minecraft.getInstance().player.getRandom().nextInt(136));
            return new LegacyBusAnimation()
                    .addBus("GAUGE", seq().addPos(0, 0, gaugeAngle, 100, IType.SIN_DOWN)
                            .addPos(0, 0, 0, 500, IType.SIN_DOWN))
                    .addBus("PISTON", seq().addPos(0, 0, 3, 100, IType.SIN_UP))
                    .addBus("NUKE", seq().addPos(0, 0, 3, 100, IType.SIN_UP).setPos(0, 0, 0));
        }
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LID", seq().addPos(0, 0, 0, 250).addPos(0, 0, -45, 250, IType.SIN_UP)
                        .hold(1200).addPos(0, 0, 0, 250, IType.SIN_UP))
                .addBus("HANDLE", seq().addPos(0, 0, -2, 500, IType.SIN_FULL).hold(1700)
                        .addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("NUKE", seq().setPos(5, -4, 3).hold(750).addPos(2, .5, 3, 500, IType.SIN_UP)
                        .addPos(1, .5, 3, 100).addPos(0, 0, 3, 100).hold(750)
                        .addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("PISTON", seq().setPos(0, 0, 3).hold(2200).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("EQUIP", seq().addPos(5, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_FULL)
                        .hold(450).addPos(3, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .hold(500).addPos(-10, 0, 0, 375, IType.SIN_DOWN).addPos(0, 0, 0, 375, IType.SIN_UP));
        if (type == JAMMED) return new LegacyBusAnimation()
                .addBus("HANDLE", seq().hold(750).addPos(0, 0, -2, 250, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("EQUIP", seq().hold(500).addPos(-15, 0, 0, 250, IType.SIN_FULL).hold(1000)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("HANDLE", seq().hold(250).addPos(0, 0, -2, 250, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("EQUIP", seq().addPos(-15, 0, 0, 250, IType.SIN_FULL).hold(1000)
                        .addPos(0, 0, 0, 250, IType.SIN_FULL));
        return null;
    }

    /** Exact XFactoryFolly.LAMBDA_FOLLY_ANIMS rails. */
    private static LegacyBusAnimation folly(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP",
                seq().setPos(-60, 0, 0).addPos(5, 0, 0, 1500, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(0, 0, -4.5, 50).hold(500).addPos(0, 0, 0, 500, IType.SIN_UP))
                .addBus("LOAD", seq().addPos(0, 0, 0, 50).addPos(-25, 0, 0, 250, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 1000, IType.SIN_FULL));
        if (type == RELOAD) return new LegacyBusAnimation()
                .addBus("LOAD", seq().addPos(60, 0, 0, 1000, IType.SIN_FULL).hold(6000)
                        .addPos(0, 0, 0, 1000, IType.SIN_FULL))
                .addBus("SCREW", seq().hold(1000).addPos(0, 0, -135, 1000, IType.SIN_FULL).hold(4000)
                        .addPos(0, 0, 0, 1000, IType.SIN_FULL))
                .addBus("BREECH", seq().hold(1000).addPos(0, 0, -.5, 1000, IType.SIN_FULL)
                        .addPos(0, -4, -.5, 1000, IType.SIN_FULL).hold(2000)
                        .addPos(0, 0, -.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 1000, IType.SIN_FULL))
                .addBus("SHELL", seq().setPos(0, -4, -4.5).hold(3000)
                        .addPos(0, 0, -4.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP));
        return null;
    }

    private static LegacyBusAnimationSequence turn(int wait, double angle) {
        return seq().addPos(0, 0, 0, wait).addPos(0, 0, angle, 250, IType.SIN_FULL).addPos(0, 0, angle, 400).addPos(0, 0, 0, 250, IType.SIN_FULL);
    }

    private static LegacyBusAnimation m2(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(80, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(0, 0, -.25, 25).addPos(0, 0, 0, 75));
        return null;
    }

    private static LegacyBusAnimation coilgun(ItemStack stack, int type) {
        boolean aiming = gunAiming(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("RELOAD", seq().addPos(1, 0, 0, 0).addPos(0, 0, 0, 250));
        if (type == CYCLE) return new LegacyBusAnimation().addBus("RECOIL", seq().addPos(aiming ? .5 : 1, 0, 0, 100).addPos(0, 0, 0, 200));
        if (type == RELOAD) return new LegacyBusAnimation().addBus("RELOAD", seq().addPos(1, 0, 0, 250).addPos(1, 0, 0, 500).addPos(0, 0, 0, 250));
        return null;
    }

    private static LegacyBusAnimation ni4ni(ItemStack stack, int type) {
        boolean aiming = gunAiming(stack);
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(-720, 0, 0, 500));
        if (type == CYCLE) return new LegacyBusAnimation()
                .addBus("RECOIL", seq().addPos(aiming ? -5 : -30, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("DRUM", seq().hold(50).addPos(0, 0, 120, 300, IType.SIN_FULL));
        if (type == INSPECT) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(-1080, 0, 0, 750).hold(100).addPos(0, 0, 0, 750));
        return null;
    }

    private static LegacyBusAnimation tau(int type) {
        if (type == EQUIP) return new LegacyBusAnimation().addBus("EQUIP", seq().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        if (type == CYCLE || type == ALT_CYCLE) {
            double recoil = type == CYCLE ? -.5 : -3;
            int in = type == CYCLE ? 50 : 100, out = type == CYCLE ? 150 : 250;
            return new LegacyBusAnimation()
                    .addBus("RECOIL", seq().addPos(0, 0, recoil, in, type == CYCLE ? IType.LINEAR : IType.SIN_DOWN).addPos(0, 0, 0, out, IType.SIN_FULL))
                    .addBus("ROTATE", tauKickRotate());
        }
        if (type == INSPECT) return new LegacyBusAnimation()
                .addBus("EQUIP", seq().addPos(2, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("ROTATE", seq().addPos(0, 0, -1080, 1500, IType.SIN_DOWN));
        if (type == SPINUP) return new LegacyBusAnimation().addBus("ROTATE", seq().addPos(0, 0, 2160, 3000, IType.SIN_UP).addPos(0, 0, 0, 0).addPos(0, 0, 14400, 10000));
        return null;
    }

    private static LegacyBusAnimationSequence tauKickRotate() {
        return seq().addPos(0, 0, -5, 50, IType.SIN_DOWN).addPos(0, 0, 5, 100, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP);
    }

    private static LegacyBusAnimationSequence seq() { return new LegacyBusAnimationSequence(); }
    private static boolean gunAiming(ItemStack stack) { return stack.getItem() instanceof SednaGunItem gun && gun.legacyIsAiming(stack); }
    private static int primaryMagazineCount(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gun) || stack.getTag() == null) return 0;
        for (var mode : gun.gunConfig().configs()) {
            for (var receiver : mode.receivers()) {
                var magazine = receiver.magazineOrNull();
                if (magazine != null && !magazine.nbtCountKey().isBlank()) {
                    return stack.getTag().getInt(magazine.nbtCountKey());
                }
            }
        }
        return 0;
    }
    private static int primaryMagazineAmountBeforeReload(ItemStack stack) {
        if (!(stack.getItem() instanceof SednaGunItem gun) || stack.getTag() == null) return 0;
        for (var mode : gun.gunConfig().configs()) {
            for (var receiver : mode.receivers()) {
                var magazine = receiver.magazineOrNull();
                if (magazine != null && !magazine.nbtBeforeReloadKey().isBlank()) {
                    return stack.getTag().getInt(magazine.nbtBeforeReloadKey());
                }
            }
        }
        return 0;
    }
    private SednaGunAnimationClient() { }
}
