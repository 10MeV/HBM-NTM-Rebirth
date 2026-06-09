package com.hbm.ntm.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public final class HbmRegistryUtil {
    private HbmRegistryUtil() {
    }

    public static ResourceLocation itemKey(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static Optional<Item> item(ResourceLocation id) {
        return ForgeRegistries.ITEMS.getHolder(id).map(holder -> holder.get());
    }

    public static ResourceLocation blockKey(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public static Optional<Block> block(ResourceLocation id) {
        return ForgeRegistries.BLOCKS.getHolder(id).map(holder -> holder.get());
    }

    public static ResourceLocation mobEffectKey(MobEffect effect) {
        return ForgeRegistries.MOB_EFFECTS.getKey(effect);
    }

    public static Optional<MobEffect> mobEffect(ResourceLocation id) {
        return ForgeRegistries.MOB_EFFECTS.getHolder(id).map(holder -> holder.get());
    }

    public static Optional<RecipeSerializer<?>> recipeSerializer(ResourceLocation id) {
        return ForgeRegistries.RECIPE_SERIALIZERS.getHolder(id).map(holder -> holder.get());
    }

    public static boolean hasChunkAt(LevelReader level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}
