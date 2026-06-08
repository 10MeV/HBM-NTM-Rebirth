package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.explosion.vnt.interfaces.BlockProcessor;
import com.hbm.ntm.explosion.vnt.interfaces.DropChanceMutator;
import com.hbm.ntm.explosion.vnt.interfaces.FortuneMutator;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BlockProcessorStandard implements BlockProcessor {
    private DropChanceMutator chance;
    private FortuneMutator fortune;
    private BlockMutator mutator;

    public BlockProcessorStandard withChance(DropChanceMutator chance) {
        this.chance = chance;
        return this;
    }

    public BlockProcessorStandard withFortune(FortuneMutator fortune) {
        this.fortune = fortune;
        return this;
    }

    public BlockProcessorStandard withBlockEffect(BlockMutator mutator) {
        this.mutator = mutator;
        return this;
    }

    @Override
    public void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Set<BlockPos> affectedBlocks) {
        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        boolean causedByPlayer = explosion.indirectSourceEntity() instanceof Player;
        ObjectArrayList<BlockPos> orderedPositions = new ObjectArrayList<>(affectedBlocks);
        Util.shuffle(orderedPositions, level.random);
        float dropChance = 1.0F / explosion.size();

        for (BlockPos pos : orderedPositions) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                affectedBlocks.remove(pos);
                continue;
            }

            if (chance != null) {
                dropChance = chance.mutateDropChance(explosion, state, pos, dropChance);
            }
            boolean suppressDrops = mutator != null && mutator.suppressDrops(explosion, state, pos);
            if (!suppressDrops && state.canDropFromExplosion(level, pos, explosion.compat())
                    && level.random.nextFloat() < dropChance) {
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                ItemStack tool = ItemStack.EMPTY;
                if (fortune != null) {
                    int dropFortune = fortune.mutateFortune(explosion, state, pos);
                    if (dropFortune > 0) {
                        tool = new ItemStack(Items.DIAMOND_PICKAXE);
                        tool.enchant(Enchantments.BLOCK_FORTUNE, dropFortune);
                    }
                }
                LootParams.Builder lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, tool)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.exploder());

                state.spawnAfterBreak(level, pos, ItemStack.EMPTY, causedByPlayer);
                state.getDrops(lootParams).forEach(stack -> addBlockDrops(drops, stack, pos.immutable()));
            }

            state.onBlockExploded(level, pos, explosion.compat());
            if (mutator != null) {
                mutator.mutatePre(explosion, state, pos);
            }
        }

        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(level, pair.getSecond(), pair.getFirst());
        }

        if (mutator != null) {
            for (BlockPos pos : orderedPositions) {
                if (level.getBlockState(pos).isAir()) {
                    mutator.mutatePost(explosion, pos);
                }
            }
        }
    }

    public BlockProcessorStandard setNoDrop() {
        this.chance = new DropChanceMutatorStandard(0.0F);
        return this;
    }

    public BlockProcessorStandard setAllDrop() {
        this.chance = new DropChanceMutatorStandard(1.0F);
        return this;
    }

    public BlockProcessorStandard setFortune(int fortune) {
        this.fortune = (explosion, state, pos) -> fortune;
        return this;
    }

    public static void placeFire(Level level, Iterable<BlockPos> affectedBlocks) {
        for (BlockPos pos : affectedBlocks) {
            if (level.random.nextInt(3) == 0 && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
            }
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositions, ItemStack stack, BlockPos pos) {
        for (int i = 0; i < dropPositions.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = dropPositions.get(i);
            ItemStack existing = pair.getFirst();
            if (ItemEntity.areMergable(existing, stack)) {
                ItemStack merged = ItemEntity.merge(existing, stack, 16);
                dropPositions.set(i, Pair.of(merged, pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        dropPositions.add(Pair.of(stack, pos));
    }
}
