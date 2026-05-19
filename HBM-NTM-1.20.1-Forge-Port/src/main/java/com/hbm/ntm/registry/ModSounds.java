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

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HbmNtm.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
