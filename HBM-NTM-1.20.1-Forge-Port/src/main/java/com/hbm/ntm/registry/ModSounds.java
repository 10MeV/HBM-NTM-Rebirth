package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HbmNtm.MOD_ID);

    public static final RegistryObject<SoundEvent> BLOCK_PRESS_OPERATE = register("block.press_operate");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_1 = register("tool.geiger1");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_2 = register("tool.geiger2");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_3 = register("tool.geiger3");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_4 = register("tool.geiger4");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_5 = register("tool.geiger5");
    public static final RegistryObject<SoundEvent> TOOL_GEIGER_6 = register("tool.geiger6");
    public static final RegistryObject<SoundEvent> TOOL_TECH_BOOP = register("tool.tech_boop");
    public static final RegistryObject<SoundEvent> TOOL_RADAWAY = register("tool.radaway");

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

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HbmNtm.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
