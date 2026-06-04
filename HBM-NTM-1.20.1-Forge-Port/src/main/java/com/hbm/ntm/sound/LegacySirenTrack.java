package com.hbm.ntm.sound;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public enum LegacySirenTrack {
    NULL(" ", null, null, SoundType.SOUND, 0, 0),
    HATCH("Hatch Siren", "hbm:alarm.hatch", "alarm.hatch", SoundType.LOOP, 3358839, 250),
    ATUOPILOT("Autopilot Disconnected", "hbm:alarm.autopilot", "alarm.autopilot", SoundType.LOOP, 11908533, 50),
    AMS_SIREN("AMS Siren", "hbm:alarm.amsSiren", "alarm.ams_siren", SoundType.LOOP, 15055698, 50),
    BLAST_DOOR("Blast Door Alarm", "hbm:alarm.blastDoorAlarm", "alarm.blast_door_alarm", SoundType.LOOP, 11665408, 50),
    APC_LOOP("APC Siren", "hbm:alarm.apcLoop", "alarm.apc_loop", SoundType.LOOP, 3565216, 50),
    KLAXON("Klaxon", "hbm:alarm.klaxon", "alarm.klaxon", SoundType.LOOP, 8421504, 50),
    KLAXON_A("Vault Door Alarm", "hbm:alarm.foKlaxonA", "alarm.fo_klaxon_a", SoundType.LOOP, 0x8c810b, 50),
    KLAXON_B("Security Alert", "hbm:alarm.foKlaxonB", "alarm.fo_klaxon_b", SoundType.LOOP, 0x76818e, 50),
    SIREN("Standard Siren", "hbm:alarm.regularSiren", "alarm.regular_siren", SoundType.LOOP, 6684672, 100),
    CLASSIC("Classic Siren", "hbm:alarm.classic", "alarm.classic", SoundType.LOOP, 0xc0cfe8, 100),
    BANK_ALARM("Bank Alarm", "hbm:alarm.bankAlarm", "alarm.bank_alarm", SoundType.LOOP, 3572962, 100),
    BEEP_SIREN("Beep Siren", "hbm:alarm.beepSiren", "alarm.beep_siren", SoundType.LOOP, 13882323, 100),
    CONTAINER_ALARM("Container Alarm", "hbm:alarm.containerAlarm", "alarm.container_alarm", SoundType.LOOP, 14727839, 100),
    SWEEP_SIREN("Sweep Siren", "hbm:alarm.sweepSiren", "alarm.sweep_siren", SoundType.LOOP, 15592026, 500),
    STRIDER_SIREN("Missile Silo Siren", "hbm:alarm.striderSiren", "alarm.strider_siren", SoundType.LOOP, 11250586, 500),
    AIR_RAID("Air Raid Siren", "hbm:alarm.airRaid", "alarm.air_raid", SoundType.LOOP, 0xDF3795, 500),
    NOSTROMO_SIREN("Nostromo Self Destruct", "hbm:alarm.nostromoSiren", "alarm.nostromo_siren", SoundType.LOOP, 0x5dd800, 100),
    EAS_ALARM("EAS Alarm Screech", "hbm:alarm.easAlarm", "alarm.eas_alarm", SoundType.LOOP, 0xb3a8c1, 50),
    APC_PASS("APC Pass", "hbm:alarm.apcPass", "alarm.apc_pass", SoundType.PASS, 3422163, 50),
    RAZORTRAIN("Razortrain Horn", "hbm:alarm.razortrainHorn", "alarm.razortrain_horn", SoundType.SOUND, 7819501, 250);

    private static final LegacySirenTrack[] VALUES = values();

    private final String title;
    private final String legacySoundId;
    private final String eventPath;
    private final ResourceLocation eventLocation;
    private final SoundType type;
    private final int color;
    private final int volume;

    LegacySirenTrack(String title, String legacySoundId, String eventPath, SoundType type, int color, int volume) {
        this.title = title;
        this.legacySoundId = legacySoundId;
        this.eventPath = eventPath;
        this.eventLocation = eventPath == null ? null : new ResourceLocation(HbmNtm.MOD_ID, eventPath);
        this.type = type;
        this.color = color;
        this.volume = volume;
    }

    public int id() {
        return ordinal();
    }

    public String title() {
        return title;
    }

    public String legacySoundId() {
        return legacySoundId;
    }

    public String eventPath() {
        return eventPath;
    }

    public ResourceLocation eventLocation() {
        return eventLocation;
    }

    public SoundType type() {
        return type;
    }

    public int color() {
        return color;
    }

    public int volume() {
        return volume;
    }

    public boolean hasSound() {
        return eventLocation != null;
    }

    public static LegacySirenTrack byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : NULL;
    }

    public enum SoundType {
        LOOP,
        PASS,
        SOUND
    }
}
