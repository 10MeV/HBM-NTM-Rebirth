package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.sound.LegacySirenTrack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HbmNtm.MOD_ID);

    public static final RegistryObject<SoundEvent> BLOCK_PRESS_OPERATE = register("block.press_operate");
    public static final RegistryObject<SoundEvent> BLOCK_MOTOR = register("block.motor");
    public static final RegistryObject<SoundEvent> BLOCK_ASSEMBLER_OPERATE = register("block.assembler_operate");
    public static final RegistryObject<SoundEvent> BLOCK_ASSEMBLER_STRIKE = register("block.assembler_strike");
    public static final RegistryObject<SoundEvent> BLOCK_ASSEMBLER_START = register("block.assembler_start");
    public static final RegistryObject<SoundEvent> BLOCK_ASSEMBLER_STOP = register("block.assembler_stop");
    public static final RegistryObject<SoundEvent> BLOCK_ASSEMBLER_CUT = register("block.assembler_cut");
    public static final RegistryObject<SoundEvent> BLOCK_CHEMPLANT_OPERATE = register("block.chemplant_operate");
    public static final RegistryObject<SoundEvent> BLOCK_CHEMICAL_PLANT = register("block.chemical_plant");
    public static final RegistryObject<SoundEvent> BLOCK_PIPE_PLACED = register("block.pipe_placed");
    public static final RegistryObject<SoundEvent> BLOCK_DEBRIS = register("block.debris");
    public static final RegistryObject<SoundEvent> BLOCK_REACTOR_START = register("block.reactor_start");
    public static final RegistryObject<SoundEvent> BLOCK_BOILER = register("block.boiler");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_1 = register("tool.geiger1");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_2 = register("tool.geiger2");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_3 = register("tool.geiger3");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_4 = register("tool.geiger4");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_5 = register("tool.geiger5");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_6 = register("tool.geiger6");
    public static final RegistryObject<SoundEvent> TOOL_TECH_BOOP = register("tool.tech_boop");
    public static final RegistryObject<SoundEvent> TOOL_TECH_BLEEP = register("tool.tech_bleep");
    public static final RegistryObject<SoundEvent> TOOL_RADAWAY = register("tool.radaway");
    public static final RegistryObject<SoundEvent> PLAYER_COUGH = register("player.cough");
    public static final RegistryObject<SoundEvent> PLAYER_VOMIT = register("player.vomit");
    public static final RegistryObject<SoundEvent> ENTITY_UFO_BLAST = register("entity.ufo_blast");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_FLYING_LOOP = register("entity.chopper_flying_loop");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_CRASHING_LOOP = register("entity.chopper_crashing_loop");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_MINE_LOOP = register("entity.chopper_mine_loop");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_DROP = register("entity.chopper_drop");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_CHARGE = register("entity.chopper_charge");
    public static final RegistryObject<SoundEvent> ENTITY_CHOPPER_DAMAGE = register("entity.chopper_damage");
    public static final RegistryObject<SoundEvent> WEAPON_MUKE_EXPLOSION = register("weapon.muke_explosion");
    public static final RegistryObject<SoundEvent> WEAPON_NUCLEAR_EXPLOSION = register("weapon.nuclear_explosion");
    public static final RegistryObject<SoundEvent> WEAPON_FSTBMB_START = register("weapon.fstbmb_start");
    public static final RegistryObject<SoundEvent> WEAPON_FSTBMB_PING = register("weapon.fstbmb_ping");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_LARGE_NEAR = register("weapon.explosion_large_near");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_LARGE_FAR = register("weapon.explosion_large_far");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_SMALL_NEAR = register("weapon.explosion_small_near");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_SMALL_FAR = register("weapon.explosion_small_far");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_TINY = register("weapon.explosion_tiny");
    public static final RegistryObject<SoundEvent> WEAPON_EXPLOSION_MEDIUM = register("weapon.explosion_medium");
    public static final RegistryObject<SoundEvent> WEAPON_RELOAD_TUBE_FWOOMP = register("weapon.reload.tube_fwoomp");
    public static final RegistryObject<SoundEvent> WEAPON_CASING_SHELL = register("weapon.casing.shell");
    public static final RegistryObject<SoundEvent> WEAPON_CASING_SMALL = register("weapon.casing.small");
    public static final RegistryObject<SoundEvent> WEAPON_CASING_MEDIUM = register("weapon.casing.medium");
    public static final RegistryObject<SoundEvent> WEAPON_CASING_LARGE = register("weapon.casing.large");
    public static final Map<LegacySirenTrack, RegistryObject<SoundEvent>> ALARM_TRACKS = registerAlarmTracks();

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    public static SoundEvent geiger(int level) {
        return switch (Math.max(1, Math.min(6, level))) {
            case 2 -> TOOL_GEIGER_2.get();
            case 3 -> TOOL_GEIGER_3.get();
            case 4 -> TOOL_GEIGER_4.get();
            case 5 -> TOOL_GEIGER_5.get();
            case 6 -> TOOL_GEIGER_6.get();
            default -> TOOL_GEIGER_1.get();
        };
    }

    public static SoundEvent sirenTrack(LegacySirenTrack track) {
        RegistryObject<SoundEvent> sound = ALARM_TRACKS.get(track);
        return sound == null ? null : sound.get();
    }

    private static Map<LegacySirenTrack, RegistryObject<SoundEvent>> registerAlarmTracks() {
        EnumMap<LegacySirenTrack, RegistryObject<SoundEvent>> sounds = new EnumMap<>(LegacySirenTrack.class);
        for (LegacySirenTrack track : LegacySirenTrack.values()) {
            if (track.hasSound()) {
                sounds.put(track, register(track.eventPath()));
            }
        }
        return Collections.unmodifiableMap(sounds);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HbmNtm.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
