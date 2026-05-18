package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.MachineBlockEntityBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HbmNtm.MOD_ID);

    // Legacy 1.7.10 machine IDs. Only machine_press has the first BlockEntity scaffold so far.
    public static final RegistryObject<Block> MACHINE_PRESS = basicMachine("machine_press");
    public static final RegistryObject<Block> MACHINE_DIFURNACE_OFF = machine("machine_difurnace_off");
    public static final RegistryObject<Block> MACHINE_ELECTRIC_FURNACE_OFF = machine("machine_electric_furnace_off");
    public static final RegistryObject<Block> MACHINE_BOILER_OFF = machine("machine_boiler_off");
    public static final RegistryObject<Block> MACHINE_SHREDDER = machine("machine_shredder");

    // Legacy 1.7.10 nuclear device IDs. These are model-only placeholders for now.
    public static final RegistryObject<Block> NUKE_GADGET = nonOccludingMachine("nuke_gadget");
    public static final RegistryObject<Block> NUKE_BOY = nonOccludingMachine("nuke_boy");
    public static final RegistryObject<Block> NUKE_MAN = nonOccludingMachine("nuke_man");
    public static final RegistryObject<Block> NUKE_TSAR = nonOccludingMachine("nuke_tsar");
    public static final RegistryObject<Block> NUKE_MIKE = nonOccludingMachine("nuke_mike");
    public static final RegistryObject<Block> NUKE_PROTOTYPE = nonOccludingMachine("nuke_prototype");
    public static final RegistryObject<Block> NUKE_FLEIJA = nonOccludingMachine("nuke_fleija");
    public static final RegistryObject<Block> NUKE_SOLINIUM = nonOccludingMachine("nuke_solinium");
    public static final RegistryObject<Block> NUKE_N2 = nonOccludingMachine("nuke_n2");
    public static final RegistryObject<Block> NUKE_FSTBMB = nonOccludingMachine("nuke_fstbmb");
    public static final RegistryObject<Block> BOMB_MULTI = nonOccludingMachine("bomb_multi");

    public static final List<RegistryObject<Block>> MACHINE_TAB_BLOCKS = List.of(
            MACHINE_PRESS,
            MACHINE_DIFURNACE_OFF,
            MACHINE_ELECTRIC_FURNACE_OFF,
            MACHINE_BOILER_OFF,
            MACHINE_SHREDDER
    );

    public static final List<RegistryObject<Block>> NUKE_TAB_BLOCKS = List.of(
            NUKE_GADGET,
            NUKE_BOY,
            NUKE_MAN,
            NUKE_TSAR,
            NUKE_MIKE,
            NUKE_PROTOTYPE,
            NUKE_FLEIJA,
            NUKE_SOLINIUM,
            NUKE_N2,
            NUKE_FSTBMB,
            BOMB_MULTI
    );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private static RegistryObject<Block> machine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> nonOccludingMachine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static RegistryObject<Block> basicMachine(String name) {
        return registerBlockWithItem(name, () -> new MachineBlockEntityBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private ModBlocks() {
    }
}
