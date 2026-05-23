package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class HbmBlockLootProvider extends BlockLootSubProvider {
    public HbmBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        add(ModBlocks.CONVEYOR.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_EXPRESS.get(), conveyorWandDrop("EXPRESS"));
        add(ModBlocks.CONVEYOR_DOUBLE.get(), conveyorWandDrop("DOUBLE"));
        add(ModBlocks.CONVEYOR_TRIPLE.get(), conveyorWandDrop("TRIPLE"));
        add(ModBlocks.CONVEYOR_LIFT.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_CHUTE.get(), conveyorWandDrop("REGULAR"));
        ModBlocks.BLOCK_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        add(ModBlocks.WASTE_LEAVES.get(), noDrop());
        add(ModBlocks.LEAVES_LAYER.get(), noDrop());
        add(ModBlocks.WASTE_LOG.get(), wasteLogDrop());
        add(ModBlocks.FIRE_DIGAMMA.get(), noDrop());
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        add(ModBlocks.GAS_RADON.get(), noDrop());
        add(ModBlocks.GAS_RADON_DENSE.get(), noDrop());
        add(ModBlocks.GAS_RADON_TOMB.get(), noDrop());
        add(ModBlocks.GAS_MELTDOWN.get(), noDrop());
        add(ModBlocks.GAS_MONOXIDE.get(), noDrop());
        add(ModBlocks.GAS_ASBESTOS.get(), noDrop());
        add(ModBlocks.GAS_COAL.get(), noDrop());
        add(ModBlocks.CHLORINE_GAS.get(), noDrop());
        add(ModBlocks.DUMMY_BLOCK.get(), noDrop());
        add(ModBlocks.GLASS_BORON.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_BORON.get()));
        add(ModBlocks.FALLOUT.get(), block -> createSingleItemTable(ModItems.legacyItem("fallout").get()));
    }

    @SuppressWarnings("deprecation")
    private LootTable.Builder conveyorWandDrop(String type) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type);
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.CONVEYOR_WAND.get())
                                .apply(SetNbtFunction.setTag(tag)))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder wasteLogDrop() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.CHARCOAL)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
