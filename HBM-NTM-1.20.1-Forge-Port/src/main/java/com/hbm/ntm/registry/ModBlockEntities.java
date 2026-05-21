package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.DeconBlockEntity;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.TrinketBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<BlockEntityType<BasicMachineBlockEntity>> BASIC_MACHINE =
            BLOCK_ENTITIES.register("basic_machine", () ->
                    BlockEntityType.Builder.of(BasicMachineBlockEntity::new, ModBlocks.MACHINE_PRESS.get()).build(null));

    public static final RegistryObject<BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITIES.register("boiler", () ->
                    BlockEntityType.Builder.of(BoilerBlockEntity::new, ModBlocks.MACHINE_BOILER_OFF.get()).build(null));

    public static final RegistryObject<BlockEntityType<DeconBlockEntity>> DECON =
            BLOCK_ENTITIES.register("decon", () ->
                    BlockEntityType.Builder.of(DeconBlockEntity::new, ModBlocks.DECON.get()).build(null));

    public static final RegistryObject<BlockEntityType<RedCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITIES.register("red_cable", () ->
                    BlockEntityType.Builder.of(RedCableBlockEntity::new, ModBlocks.RED_CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY =
            BLOCK_ENTITIES.register("machine_battery", () ->
                    BlockEntityType.Builder.of(MachineBatteryBlockEntity::new, ModBlocks.MACHINE_BATTERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TrinketBlockEntity>> TRINKET =
            BLOCK_ENTITIES.register("trinket", () ->
                    BlockEntityType.Builder.of(
                            TrinketBlockEntity::new,
                            ModBlocks.legacyBlock("bobblehead").get(),
                            ModBlocks.legacyBlock("snowglobe").get(),
                            ModBlocks.legacyBlock("plushie").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyLightBlockEntity>> LEGACY_LIGHT =
            BLOCK_ENTITIES.register("legacy_light", () ->
                    BlockEntityType.Builder.of(
                            LegacyLightBlockEntity::new,
                            ModBlocks.legacyBlock("spotlight_incandescent").get(),
                            ModBlocks.legacyBlock("spotlight_fluoro").get(),
                            ModBlocks.legacyBlock("spotlight_halogen").get(),
                            ModBlocks.legacyBlock("floodlight").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyDemonLampBlockEntity>> LEGACY_DEMON_LAMP =
            BLOCK_ENTITIES.register("legacy_demon_lamp", () ->
                    BlockEntityType.Builder.of(
                            LegacyDemonLampBlockEntity::new,
                            ModBlocks.legacyBlock("lamp_demon").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyLanternBlockEntity>> LEGACY_LANTERN =
            BLOCK_ENTITIES.register("legacy_lantern", () ->
                    BlockEntityType.Builder.of(
                            LegacyLanternBlockEntity::new,
                            ModBlocks.legacyBlock("lantern").get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private ModBlockEntities() {
    }
}
