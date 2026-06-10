package com.hbm.ntm.sound;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LegacySoundIds {
    private static final String LEGACY_HBM_NAMESPACE = "hbm";
    private static final String NTMSOUNDS_MARKER = "NTMSounds.";
    private static final Set<String> LEGACY_HBM_SOUND_PREFIXES = Set.of(
            "alarm",
            "block",
            "door",
            "entity",
            "item",
            "misc",
            "music",
            "player",
            "potatos",
            "step",
            "turret",
            "weapon");

    public static final Map<String, String> NTMSOUNDS_ALIASES = buildNtmsoundsAliases();
    public static final Map<String, String> VANILLA_SOUND_ALIASES = buildVanillaSoundAliases();

    @Nullable
    public static ResourceLocation resolveLocation(@Nullable String soundId) {
        String expanded = expandAlias(soundId);
        if (expanded.isEmpty()) {
            return null;
        }

        String vanillaAlias = VANILLA_SOUND_ALIASES.get(expanded);
        if (vanillaAlias != null) {
            return ResourceLocation.tryParse(vanillaAlias);
        }

        int namespaceSeparator = expanded.indexOf(':');
        if (namespaceSeparator >= 0) {
            String namespace = expanded.substring(0, namespaceSeparator);
            String path = expanded.substring(namespaceSeparator + 1);
            if (namespace.equalsIgnoreCase(LEGACY_HBM_NAMESPACE) || namespace.equalsIgnoreCase(HbmNtm.MOD_ID)) {
                return hbmLocation(path);
            }
            return ResourceLocation.tryParse(namespace.toLowerCase(Locale.ROOT) + ":" + path);
        }

        if (isLegacyHbmSoundEvent(expanded)) {
            return hbmLocation(expanded);
        }

        return ResourceLocation.tryParse(expanded);
    }

    @Nullable
    public static SoundEvent resolveEvent(@Nullable String soundId) {
        ResourceLocation location = resolveLocation(soundId);
        return location == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(location);
    }

    public static String normalizeIdString(@Nullable String soundId) {
        String cleaned = clean(soundId);
        ResourceLocation location = resolveLocation(cleaned);
        return location == null ? cleaned : location.toString();
    }

    public static String normalizePath(@Nullable String path) {
        String value = clean(path).replace('\\', '/');
        value = value.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
        value = value.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return value.toLowerCase(Locale.ROOT);
    }

    public static boolean isLegacyHbmId(@Nullable String soundId) {
        String expanded = expandAlias(soundId);
        int namespaceSeparator = expanded.indexOf(':');
        return (namespaceSeparator > 0 && expanded.substring(0, namespaceSeparator).equalsIgnoreCase(LEGACY_HBM_NAMESPACE))
                || (namespaceSeparator < 0 && isLegacyHbmSoundEvent(expanded));
    }

    public static boolean isNullRedirect(@Nullable String soundId) {
        ResourceLocation location = resolveLocation(soundId);
        if (location == null || !HbmNtm.MOD_ID.equals(location.getNamespace())) {
            return false;
        }
        String path = location.getPath();
        return path.equals("misc.null_chopper") || path.equals("misc.null_crashing") || path.equals("misc.null_mine");
    }

    public static ResourceLocation geigerLocation(int level) {
        return hbmLocation("item.geiger" + Math.max(1, Math.min(6, level)));
    }

    public static String expandAlias(@Nullable String soundId) {
        String cleaned = clean(soundId);
        if (cleaned.isEmpty()) {
            return "";
        }
        String key = cleaned;
        int markerIndex = key.lastIndexOf(NTMSOUNDS_MARKER);
        if (markerIndex >= 0) {
            key = key.substring(markerIndex + NTMSOUNDS_MARKER.length());
        }
        String alias = NTMSOUNDS_ALIASES.get(key);
        return alias == null ? cleaned : alias;
    }

    private static ResourceLocation hbmLocation(String path) {
        return ResourceLocation.tryParse(HbmNtm.MOD_ID + ":" + normalizePath(path));
    }

    private static boolean isLegacyHbmSoundEvent(String soundId) {
        int dotIndex = soundId.indexOf('.');
        if (dotIndex <= 0) {
            return false;
        }
        return LEGACY_HBM_SOUND_PREFIXES.contains(soundId.substring(0, dotIndex));
    }

    private static String clean(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private static void put(Map<String, String> map, String name, String legacyId) {
        map.put(name, legacyId);
    }

    private static Map<String, String> buildNtmsoundsAliases() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        put(map, "GUN_REVOLVER_COCK", "hbm:weapon.reload.revolverCock");
        put(map, "GUN_REVOLVER_CLOSE", "hbm:weapon.reload.revolverClose");
        put(map, "GUN_REVOLVER_SPIN", "hbm:weapon.reload.revolverSpin");
        put(map, "GUN_PISTOL_COCK", "hbm:weapon.reload.pistolCock");
        put(map, "GUN_MAG_SMALL_REMOVE", "hbm:weapon.reload.magSmallRemove");
        put(map, "GUN_MAG_SMALL_INSERT", "hbm:weapon.reload.magSmallInsert");
        put(map, "GUN_MAG_REMOVE", "hbm:weapon.reload.magRemove");
        put(map, "GUN_MAG_INSERT", "hbm:weapon.reload.magInsert");
        put(map, "GUN_CANISTER_INSERT", "hbm:weapon.reload.insertCanister");
        put(map, "GUN_ROCKET_INSERT", "hbm:weapon.reload.insertRocket");
        put(map, "GUN_BOLT_OPEN", "hbm:weapon.reload.boltOpen");
        put(map, "GUN_BOLT_CLOSE", "hbm:weapon.reload.boltClose");
        put(map, "GUN_RIFLE_COCK", "hbm:weapon.reload.rifleCock");
        put(map, "GUN_LEVER_COCK", "hbm:weapon.reload.leverCock");
        put(map, "GUN_SHOTGUN_LOAD", "hbm:weapon.reload.shotgunReload");
        put(map, "GUN_SHOTGUN_OPEN", "hbm:weapon.reload.shotgunCockOpen");
        put(map, "GUN_SHOTGUN_CLOSE", "hbm:weapon.reload.shotgunCockClose");
        put(map, "GUN_SHOTGUN_COCK", "hbm:weapon.reload.shotgunCock");
        put(map, "GUN_GRENADE_RELOAD", "hbm:weapon.glReload");
        put(map, "GUN_GRENADE_OPEN", "hbm:weapon.glOpen");
        put(map, "GUN_GRENADE_CLOSE", "hbm:weapon.glClose");
        put(map, "GUN_SCREW", "hbm:weapon.reload.screw");
        put(map, "GUN_COIL_RELOAD", "hbm:weapon.coilgunReload");
        put(map, "GUN_IMPACT", "hbm:weapon.reload.impact");
        put(map, "GUN_LATCH_OPEN", "hbm:weapon.reload.openLatch");
        put(map, "GUN_VALVE", "hbm:weapon.reload.pressureValve");
        put(map, "GUN_FATMAN_RELOAD", "hbm:weapon.reload.fatmanFull");
        put(map, "GRENADE_TECH", "hbm:weapon.reload.grenadeTech");
        put(map, "GRENADE_NUKA", "hbm:weapon.reload.grenadeNuka");
        put(map, "GUN_WHACK", "hbm:weapon.foley.gunWhack");
        put(map, "GUN_LOCKON", "hbm:weapon.fire.lockon");
        put(map, "GUN_SMACK", "hbm:weapon.fire.smack");
        put(map, "GUN_STAB_A_FUCKER", "hbm:weapon.fire.stab");
        put(map, "GUN_SHREDDER_CYCLE", "hbm:weapon.fire.shredderCycle");
        put(map, "GUN_DRY_FIRE", "hbm:weapon.reload.dryFireClick");
        put(map, "GUN_POWDER_FIRE", "hbm:weapon.fire.blackPowder");
        put(map, "GUN_RIFLE_FIRE", "hbm:weapon.fire.rifle");
        put(map, "GUN_RIFLE_SILENCER", "hbm:weapon.fire.silenced");
        put(map, "GUN_HEAVY_RIFLE_FIRE", "hbm:weapon.fire.rifleHeavy");
        put(map, "GUN_ASSAULT_FIRE", "hbm:weapon.fire.assault");
        put(map, "GUN_HEAVY_REVOLVER_FIRE", "hbm:weapon.44Shoot");
        put(map, "GUN_AMAT_FIRE", "hbm:weapon.fire.amat");
        put(map, "GUN_AMAT_SILENCER", "hbm:weapon.silencerShoot");
        put(map, "GUN_SHOTGUN_FIRE", "hbm:weapon.fire.shotgun");
        put(map, "GUN_SPAS_FIRE", "hbm:weapon.shotgunShoot");
        put(map, "GUN_LIBERATOR_FIRE", "hbm:weapon.fire.shotgunAlt");
        put(map, "GUN_SHREDDER_FIRE", "hbm:weapon.fire.shotgunAuto");
        put(map, "GUN_GREASEGUN_FIRE", "hbm:weapon.fire.greaseGun");
        put(map, "GUN_STARF_FIRE", "hbm:weapon.fire.pistolLight");
        put(map, "GUN_PISTOL_FIRE", "hbm:weapon.fire.pistol");
        put(map, "GUN_UZI_FIRE", "hbm:weapon.fire.uzi");
        put(map, "GUN_ABERRATOR_FIRE", "hbm:weapon.fire.aberrator");
        put(map, "GUN_UNDERBARREL_FIRE", "hbm:weapon.hkShoot");
        put(map, "GUN_CONGO_FIRE", "hbm:weapon.glShoot");
        put(map, "GUN_CHARGE_FIRE", "hbm:weapon.fire.grenade");
        put(map, "GUN_FLAMER_LOOP", "hbm:weapon.fire.flameLoop");
        put(map, "GUN_FATMAN_FIRE", "hbm:weapon.fire.fatman");
        put(map, "GUN_MINIGUN_FIRE", "hbm:weapon.calShoot");
        put(map, "GUN_LASER_GATLING_FIRE", "hbm:weapon.fire.laserGatling");
        put(map, "GUN_LASER_PISTOL_FIRE", "hbm:weapon.fire.laserPistol");
        put(map, "GUN_LASER_RIFLE_FIRE", "hbm:weapon.fire.laser");
        put(map, "GUN_COIL_FIRE", "hbm:weapon.coilgunShoot");
        put(map, "GUN_TAU_FIRE", "hbm:weapon.fire.tau");
        put(map, "GUN_TAU_STOPFIRE", "hbm:weapon.fire.tauRelease");
        put(map, "GUN_TAU_LOOP", "hbm:weapon.fire.tauLoop");
        put(map, "GUN_TESLA_FIRE", "hbm:weapon.fire.tesla");
        put(map, "GUN_TESLA_BLAST", "hbm:entity.ufoBlast");
        put(map, "GUN_MK108_FIRE", "hbm:weapon.fire.mk108");
        put(map, "GUN_ROCKET_FIRE", "hbm:weapon.rpgShoot");
        put(map, "GUN_EXTINGUISHER_FIRE", "hbm:weapon.extinguisher");
        put(map, "GUN_PLEASE_REMOVE_MY_EARDRUMS_THANKS", "hbm:weapon.fire.loudestNoiseOnEarth");
        put(map, "GUN_VYLET_PONY_CUTIEMARKS_AND_THE_THINGS_THAT_BIND_US_INTRO_JINGLE", "hbm:weapon.fire.vstar");
        put(map, "GUN_GO_GO_GADGET_FUCK_EVERYTHING_IN_THIS_GENERAL_DIRECTION", "hbm:alarm.trainHorn");
        put(map, "GUN_SOLDIER_TF2_BOAT_EXE_WAV_MP3", "hbm:weapon.boat");
        put(map, "GUN_MINI_NUKE_EXPLOSION", "hbm:weapon.mukeExplosion");
        put(map, "TURRET_50BMG", "hbm:turret.chekhov_fire");
        put(map, "TURRET_CIWS_RELOAD", "hbm:turret.howard_reload");
        put(map, "PLAYER_GULP", "hbm:player.gulp");
        put(map, "PLAYER_GROAN", "hbm:player.groan");
        put(map, "BLOCK_PLUSHY", "hbm:block.squeakyToy");
        put(map, "BLOCK_HUNDUNS_MAGNIFICENT_HOWL", "hbm:block.hunduns_magnificent_howl");
        put(map, "BLOCK_FALLOUT_3_POPUP", "hbm:block.bobble");
        put(map, "LEVER_START", "hbm:block.leverStart");
        put(map, "LEVER_STOP", "hbm:block.leverStop");
        put(map, "SPARK", "hbm:block.spark");
        put(map, "ELECTRIC_MOTOR_LOOP", "hbm:block.motor");
        put(map, "ENGINE_LOOP", "hbm:block.engine");
        put(map, "TURBINE_LARGE_LOOP", "hbm:block.largeTurbineRunning");
        put(map, "TURBINE_LEVI_LOOP", "hbm:block.chungusTurbineRunning");
        put(map, "FEL_LOOP", "hbm:block.fel");
        put(map, "ELECTRIC_HUM_LOOP", "hbm:block.electricHum");
        put(map, "FUSION_REACTOR_LOOP", "hbm:block.fusionReactorRunning");
        put(map, "BOILER_LOOP", "hbm:block.boiler");
        put(map, "BOILER_GROAN", "hbm:block.boilerGroan");
        put(map, "CENTRIFUGE_LOOP", "hbm:block.centrifugeOperate");
        put(map, "TURBOFAN_LOOP", "hbm:block.turbofanOperate");
        put(map, "TURBOFAN_DAMAGE", "hbm:block.damage");
        put(map, "ASSEMBLER_STRIKE", "hbm:block.assemblerStrike");
        put(map, "ASSEMBLER_CUT", "hbm:block.assemblerCut");
        put(map, "ASSEMBLER_START", "hbm:block.assemblerStart");
        put(map, "ASSEMBLER_STOP", "hbm:block.assemblerStop");
        put(map, "CHEMPLANT_LOOP", "hbm:block.chemicalPlant");
        put(map, "HEPHAESTUS_LOOP", "hbm:block.hephaestusRunning");
        put(map, "STEAM_ENGINE_HIT", "hbm:block.steamEngineOperate");
        put(map, "REACTOR_GEIGER_LOOP", "hbm:block.reactorLoop");
        put(map, "TECH_BOOP", "hbm:item.techBoop");
        put(map, "TECH_BLEEP", "hbm:item.techBleep");
        put(map, "UPGRADE_PLUG", "hbm:item.upgradePlug");
        put(map, "UNPACK", "hbm:item.unpack");
        put(map, "RIVET_GUN", "hbm:item.boltgun");
        put(map, "GEIGER_PREFIX", "hbm:item.geiger");
        put(map, "FILTER_SCREW", "hbm:item.gasmaskScrew");
        put(map, "SUIT_BATTERY", "hbm:item.battery");
        put(map, "CRATE_OPEN", "hbm:block.crateOpen");
        put(map, "CRATE_CLOSE", "hbm:block.crateClose");
        put(map, "PADLOCK", "hbm:block.lockHang");
        put(map, "SPRAY_CAN", "hbm:item.spray");
        put(map, "REPAIR", "hbm:item.repair");
        put(map, "GAVEL", "hbm:weapon.whack");
        put(map, "BONK", "hbm:weapon.bonk");
        put(map, "STOP", "hbm:weapon.stop");
        put(map, "BANG", "hbm:weapon.bang");
        put(map, "SLICE", "hbm:weapon.slice");
        put(map, "KAPENG", "hbm:weapon.kapeng");
        put(map, "METAL_IMPACT", "hbm:block.metalImpact");
        put(map, "VANILLA_ORB", "random.orb");
        put(map, "VANILLA_PLINK", "random.break");
        put(map, "VANILLA_FIREWORKS_BANG", "fireworks.blast");
        put(map, "VANILLA_HISS", "random.fizz");
        put(map, "VANILLA_FIRE", "fire.fire");
        put(map, "VANILLA_IGNITE", "fire.ignite");
        put(map, "VANILLA_MINECART", "minecart.base");
        put(map, "VANILLA_GIB", "mob.zombie.woodbreak");
        put(map, "VANILLA_TELEPORT", "mob.endermen.portal");
        put(map, "VANILLA_ANVIL", "random.anvil_land");
        put(map, "VANILLA_PISTON_OUT", "tile.piston.out");
        put(map, "VANILLA_PISTON_IN", "tile.piston.in");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, String> buildVanillaSoundAliases() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        put(map, "gui.button.press", "minecraft:ui.button.click");
        put(map, "random.orb", "minecraft:entity.experience_orb.pickup");
        put(map, "random.break", "minecraft:entity.item.break");
        put(map, "random.explode", "minecraft:entity.generic.explode");
        put(map, "random.click", "minecraft:ui.button.click");
        put(map, "random.bow", "minecraft:entity.arrow.shoot");
        put(map, "random.burp", "minecraft:entity.player.burp");
        put(map, "random.eat", "minecraft:entity.generic.eat");
        put(map, "fireworks.blast", "minecraft:entity.firework_rocket.blast");
        put(map, "random.fizz", "minecraft:block.fire.extinguish");
        put(map, "fire.fire", "minecraft:block.fire.ambient");
        put(map, "fire.ignite", "minecraft:item.flintandsteel.use");
        put(map, "minecart.base", "minecraft:entity.minecart.riding");
        put(map, "game.neutral.hurt", "minecraft:entity.generic.hurt");
        put(map, "game.neutral.die", "minecraft:entity.generic.death");
        put(map, "mob.zombie.woodbreak", "minecraft:entity.zombie.break_wooden_door");
        put(map, "mob.zombie.say", "minecraft:entity.zombie.ambient");
        put(map, "mob.zombie.hurt", "minecraft:entity.zombie.hurt");
        put(map, "mob.zombie.death", "minecraft:entity.zombie.death");
        put(map, "mob.zombie.step", "minecraft:entity.zombie.step");
        put(map, "mob.skeleton.say", "minecraft:entity.skeleton.ambient");
        put(map, "mob.skeleton.hurt", "minecraft:entity.skeleton.hurt");
        put(map, "mob.skeleton.death", "minecraft:entity.skeleton.death");
        put(map, "mob.skeleton.step", "minecraft:entity.skeleton.step");
        put(map, "mob.blaze.hit", "minecraft:entity.blaze.hurt");
        put(map, "mob.chicken.step", "minecraft:entity.chicken.step");
        put(map, "mob.endermen.portal", "minecraft:entity.enderman.teleport");
        put(map, "ambient.weather.thunder", "minecraft:entity.lightning_bolt.thunder");
        put(map, "random.anvil_land", "minecraft:block.anvil.land");
        put(map, "tile.piston.out", "minecraft:block.piston.extend");
        put(map, "tile.piston.in", "minecraft:block.piston.contract");
        put(map, "game.tnt.primed", "minecraft:entity.tnt.primed");
        put(map, "game.neutral.swim.splash", "minecraft:entity.generic.splash");
        put(map, "liquid.lavapop", "minecraft:block.lava.pop");
        put(map, "liquid.lava", "minecraft:block.lava.ambient");
        put(map, "dig.glass", "minecraft:block.glass.break");
        put(map, "note.piano", "minecraft:block.note_block.harp");
        put(map, "note.bassdrum", "minecraft:block.note_block.basedrum");
        put(map, "note.snare", "minecraft:block.note_block.snare");
        put(map, "note.clicks", "minecraft:block.note_block.hat");
        put(map, "note.bassguitar", "minecraft:block.note_block.bass");
        return Collections.unmodifiableMap(map);
    }

    private LegacySoundIds() {
    }
}
