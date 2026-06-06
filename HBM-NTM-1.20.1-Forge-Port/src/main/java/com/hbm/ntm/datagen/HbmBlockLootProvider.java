package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
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
        ModBlocks.MACHINE_TAB_BLOCKS.stream()
                .filter(block -> block != ModBlocks.MACHINE_FLUIDTANK
                        && block != ModBlocks.MACHINE_BAT9000
                        && block != ModBlocks.MACHINE_BIGASSTANK)
                .filter(block -> block != ModBlocks.BARREL_PLASTIC
                        && block != ModBlocks.BARREL_STEEL
                        && block != ModBlocks.BARREL_TCALLOY
                        && block != ModBlocks.BARREL_ANTIMATTER)
                .filter(block -> block != ModBlocks.MACHINE_REFINERY
                        && block != ModBlocks.MACHINE_WELL
                        && block != ModBlocks.MACHINE_PUMPJACK
                        && block != ModBlocks.MACHINE_FRACKING_TOWER)
                .filter(block -> block != ModBlocks.MACHINE_CATALYTIC_REFORMER
                        && block != ModBlocks.MACHINE_VACUUM_DISTILL
                        && block != ModBlocks.MACHINE_HYDROTREATER)
                .forEach(block -> dropSelf(block.get()));
        add(ModBlocks.MACHINE_FLUIDTANK.get(), noDrop());
        add(ModBlocks.MACHINE_BAT9000.get(), noDrop());
        add(ModBlocks.MACHINE_BIGASSTANK.get(), noDrop());
        add(ModBlocks.MACHINE_REFINERY.get(), noDrop());
        add(ModBlocks.BARREL_PLASTIC.get(), noDrop());
        dropSelf(ModBlocks.BARREL_CORRODED.get());
        add(ModBlocks.BARREL_STEEL.get(), noDrop());
        add(ModBlocks.BARREL_TCALLOY.get(), noDrop());
        add(ModBlocks.BARREL_ANTIMATTER.get(), noDrop());
        add(ModBlocks.MACHINE_WELL.get(), noDrop());
        add(ModBlocks.MACHINE_PUMPJACK.get(), noDrop());
        add(ModBlocks.MACHINE_FRACKING_TOWER.get(), noDrop());
        add(ModBlocks.OIL_PIPE.get(), noDrop());
        add(ModBlocks.MACHINE_CATALYTIC_REFORMER.get(), noDrop());
        add(ModBlocks.MACHINE_VACUUM_DISTILL.get(), noDrop());
        add(ModBlocks.MACHINE_HYDROTREATER.get(), noDrop());
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
        add(ModBlocks.FROZEN_GRASS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_DIRT.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_LOG.get(), block -> snowballStackDrop(2.0F, 4.0F));
        add(ModBlocks.FROZEN_PLANKS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FIRE_DIGAMMA.get(), noDrop());
        add(ModBlocks.BALEFIRE.get(), noDrop());
        add(ModBlocks.MUD_BLOCK.get(), noDrop());
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
        add(ModBlocks.GLASS_QUARTZ.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_QUARTZ.get()));
        add(ModBlocks.GLASS_TRINITITE.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_TRINITITE.get()));
        add(ModBlocks.FALLOUT.get(), block -> createSingleItemTable(ModItems.legacyItem("fallout").get()));
        add(ModBlocks.WASTE_TRINITITE.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.WASTE_TRINITITE_RED.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.TEKTITE.get(), block -> createSingleItemTable(ModBlocks.TEKTITE.get()));
        add(ModBlocks.ORE_TEKTITE_OSMIRIDIUM.get(), block -> singleItemDrop(ModItems.legacyItem("powder_tektite").get()));
        add(ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), block -> singleItemDrop(Items.DIAMOND));
        add(ModBlocks.ORE_SELLAFIELD_EMERALD.get(), block -> singleItemDrop(Items.EMERALD));
        add(ModBlocks.ORE_SELLAFIELD_RADGEM.get(), block -> singleItemDrop(ModItems.legacyItem("gem_rad").get()));
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

    private LootTable.Builder singleItemDrop(Item item) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(item))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder snowballStackDrop(float min, float max) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.SNOWBALL)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
