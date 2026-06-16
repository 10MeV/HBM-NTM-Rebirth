package com.hbm.ntm.particle;

import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.registry.ModBlocks;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LegacyBlockStateMappings {
    public static final String KEY_STATE = "state";
    public static final String KEY_BLOCK = "block";
    public static final String KEY_META = "meta";
    public static final String KEY_BLOCK_NAME = "blockName";
    public static final String KEY_LEGACY_BLOCK = "legacyBlock";
    public static final String KEY_LEGACY_BLOCK_NAME = "legacyBlockName";
    public static final String KEY_BLOCK_NAME_SNAKE = "block_name";
    public static final String KEY_LEGACY_BLOCK_SNAKE = "legacy_block";

    public static BlockState fromParticleData(CompoundTag data) {
        if (data == null) {
            return fallback();
        }
        if (data.contains(KEY_STATE, Tag.TAG_INT)) {
            return fromModernStateId(data.getInt(KEY_STATE));
        }

        BlockState named = fromParticleNameFields(data);
        if (named != null) {
            return named;
        }

        if (data.contains(KEY_BLOCK, Tag.TAG_STRING)) {
            BlockState state = resolveName(data.getString(KEY_BLOCK), data.getInt(KEY_META));
            if (state != null) {
                return state;
            }
        }
        if (data.contains(KEY_BLOCK)) {
            return fromLegacyVanillaId(data.getInt(KEY_BLOCK), data.getInt(KEY_META));
        }
        return fallback();
    }

    public static BlockState fromModernStateId(int stateId) {
        return safeState(Block.stateById(stateId));
    }

    public static BlockState fromLegacyVanillaId(int legacyBlockId, int legacyMeta) {
        int meta = legacyMeta & 15;
        return switch (legacyBlockId) {
            case 1 -> legacyStone(meta);
            case 2 -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 3 -> switch (meta) {
                case 1 -> Blocks.COARSE_DIRT.defaultBlockState();
                case 2 -> Blocks.PODZOL.defaultBlockState();
                default -> Blocks.DIRT.defaultBlockState();
            };
            case 4 -> Blocks.COBBLESTONE.defaultBlockState();
            case 5 -> legacyPlanks(meta);
            case 6 -> legacySapling(meta);
            case 7 -> Blocks.BEDROCK.defaultBlockState();
            case 8, 9 -> Blocks.WATER.defaultBlockState();
            case 10, 11 -> Blocks.LAVA.defaultBlockState();
            case 12 -> legacySand(meta);
            case 13 -> Blocks.GRAVEL.defaultBlockState();
            case 14 -> Blocks.GOLD_ORE.defaultBlockState();
            case 15 -> Blocks.IRON_ORE.defaultBlockState();
            case 16 -> Blocks.COAL_ORE.defaultBlockState();
            case 17 -> legacyLog(meta);
            case 18 -> legacyLeaves(meta);
            case 19 -> Blocks.WET_SPONGE.defaultBlockState();
            case 20 -> Blocks.GLASS.defaultBlockState();
            case 21 -> Blocks.LAPIS_ORE.defaultBlockState();
            case 22 -> Blocks.LAPIS_BLOCK.defaultBlockState();
            case 23 -> Blocks.DISPENSER.defaultBlockState();
            case 24 -> legacySandstone(meta);
            case 25 -> Blocks.NOTE_BLOCK.defaultBlockState();
            case 26 -> Blocks.RED_BED.defaultBlockState();
            case 27 -> Blocks.POWERED_RAIL.defaultBlockState();
            case 28 -> Blocks.DETECTOR_RAIL.defaultBlockState();
            case 29 -> Blocks.STICKY_PISTON.defaultBlockState();
            case 30 -> Blocks.COBWEB.defaultBlockState();
            case 31 -> legacyTallGrass(meta);
            case 32 -> Blocks.DEAD_BUSH.defaultBlockState();
            case 33 -> Blocks.PISTON.defaultBlockState();
            case 34 -> Blocks.PISTON_HEAD.defaultBlockState();
            case 35 -> legacyWool(meta);
            case 36 -> Blocks.MOVING_PISTON.defaultBlockState();
            case 37 -> Blocks.DANDELION.defaultBlockState();
            case 38 -> legacyRedFlower(meta);
            case 39 -> Blocks.BROWN_MUSHROOM.defaultBlockState();
            case 40 -> Blocks.RED_MUSHROOM.defaultBlockState();
            case 41 -> Blocks.GOLD_BLOCK.defaultBlockState();
            case 42 -> Blocks.IRON_BLOCK.defaultBlockState();
            case 43 -> legacyDoubleStoneSlab(meta);
            case 44 -> legacyStoneSlab(meta);
            case 45 -> Blocks.BRICKS.defaultBlockState();
            case 46 -> Blocks.TNT.defaultBlockState();
            case 47 -> Blocks.BOOKSHELF.defaultBlockState();
            case 48 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 49 -> Blocks.OBSIDIAN.defaultBlockState();
            case 50 -> Blocks.TORCH.defaultBlockState();
            case 51 -> Blocks.FIRE.defaultBlockState();
            case 52 -> Blocks.SPAWNER.defaultBlockState();
            case 53 -> Blocks.OAK_STAIRS.defaultBlockState();
            case 54 -> Blocks.CHEST.defaultBlockState();
            case 55 -> Blocks.REDSTONE_WIRE.defaultBlockState();
            case 56 -> Blocks.DIAMOND_ORE.defaultBlockState();
            case 57 -> Blocks.DIAMOND_BLOCK.defaultBlockState();
            case 58 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 59 -> Blocks.WHEAT.defaultBlockState();
            case 60 -> Blocks.FARMLAND.defaultBlockState();
            case 61, 62 -> Blocks.FURNACE.defaultBlockState();
            case 63 -> Blocks.OAK_SIGN.defaultBlockState();
            case 64 -> Blocks.OAK_DOOR.defaultBlockState();
            case 65 -> Blocks.LADDER.defaultBlockState();
            case 66 -> Blocks.RAIL.defaultBlockState();
            case 67 -> Blocks.COBBLESTONE_STAIRS.defaultBlockState();
            case 68 -> Blocks.OAK_WALL_SIGN.defaultBlockState();
            case 69 -> Blocks.LEVER.defaultBlockState();
            case 70 -> Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
            case 71 -> Blocks.IRON_DOOR.defaultBlockState();
            case 72 -> Blocks.OAK_PRESSURE_PLATE.defaultBlockState();
            case 73, 74 -> Blocks.REDSTONE_ORE.defaultBlockState();
            case 75, 76 -> Blocks.REDSTONE_TORCH.defaultBlockState();
            case 77 -> Blocks.STONE_BUTTON.defaultBlockState();
            case 78 -> snowLayer(meta);
            case 79 -> Blocks.ICE.defaultBlockState();
            case 80 -> Blocks.SNOW_BLOCK.defaultBlockState();
            case 81 -> Blocks.CACTUS.defaultBlockState();
            case 82 -> Blocks.CLAY.defaultBlockState();
            case 83 -> Blocks.SUGAR_CANE.defaultBlockState();
            case 84 -> Blocks.JUKEBOX.defaultBlockState();
            case 85 -> Blocks.OAK_FENCE.defaultBlockState();
            case 86 -> Blocks.PUMPKIN.defaultBlockState();
            case 87 -> Blocks.NETHERRACK.defaultBlockState();
            case 88 -> Blocks.SOUL_SAND.defaultBlockState();
            case 89 -> Blocks.GLOWSTONE.defaultBlockState();
            case 90 -> Blocks.NETHER_PORTAL.defaultBlockState();
            case 91 -> Blocks.JACK_O_LANTERN.defaultBlockState();
            case 92 -> Blocks.CAKE.defaultBlockState();
            case 93, 94 -> Blocks.REPEATER.defaultBlockState();
            case 95 -> legacyStainedGlass(meta);
            case 96 -> Blocks.OAK_TRAPDOOR.defaultBlockState();
            case 97 -> legacyMonsterEgg(meta);
            case 98 -> legacyStoneBricks(meta);
            case 99 -> Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
            case 100 -> Blocks.RED_MUSHROOM_BLOCK.defaultBlockState();
            case 101 -> Blocks.IRON_BARS.defaultBlockState();
            case 102 -> Blocks.GLASS_PANE.defaultBlockState();
            case 103 -> Blocks.MELON.defaultBlockState();
            case 104 -> Blocks.PUMPKIN_STEM.defaultBlockState();
            case 105 -> Blocks.MELON_STEM.defaultBlockState();
            case 106 -> Blocks.VINE.defaultBlockState();
            case 107 -> Blocks.OAK_FENCE_GATE.defaultBlockState();
            case 108 -> Blocks.BRICK_STAIRS.defaultBlockState();
            case 109 -> Blocks.STONE_BRICK_STAIRS.defaultBlockState();
            case 110 -> Blocks.MYCELIUM.defaultBlockState();
            case 111 -> Blocks.LILY_PAD.defaultBlockState();
            case 112 -> Blocks.NETHER_BRICKS.defaultBlockState();
            case 113 -> Blocks.NETHER_BRICK_FENCE.defaultBlockState();
            case 114 -> Blocks.NETHER_BRICK_STAIRS.defaultBlockState();
            case 115 -> Blocks.NETHER_WART.defaultBlockState();
            case 116 -> Blocks.ENCHANTING_TABLE.defaultBlockState();
            case 117 -> Blocks.BREWING_STAND.defaultBlockState();
            case 118 -> Blocks.CAULDRON.defaultBlockState();
            case 119 -> Blocks.END_PORTAL.defaultBlockState();
            case 120 -> Blocks.END_PORTAL_FRAME.defaultBlockState();
            case 121 -> Blocks.END_STONE.defaultBlockState();
            case 122 -> Blocks.DRAGON_EGG.defaultBlockState();
            case 123, 124 -> Blocks.REDSTONE_LAMP.defaultBlockState();
            case 125 -> legacyDoubleWoodSlab(meta);
            case 126 -> legacyWoodSlab(meta);
            case 127 -> Blocks.COCOA.defaultBlockState();
            case 128 -> Blocks.SANDSTONE_STAIRS.defaultBlockState();
            case 129 -> Blocks.EMERALD_ORE.defaultBlockState();
            case 130 -> Blocks.ENDER_CHEST.defaultBlockState();
            case 131 -> Blocks.TRIPWIRE_HOOK.defaultBlockState();
            case 132 -> Blocks.TRIPWIRE.defaultBlockState();
            case 133 -> Blocks.EMERALD_BLOCK.defaultBlockState();
            case 134 -> Blocks.SPRUCE_STAIRS.defaultBlockState();
            case 135 -> Blocks.BIRCH_STAIRS.defaultBlockState();
            case 136 -> Blocks.JUNGLE_STAIRS.defaultBlockState();
            case 137 -> Blocks.COMMAND_BLOCK.defaultBlockState();
            case 138 -> Blocks.BEACON.defaultBlockState();
            case 139 -> legacyCobblestoneWall(meta);
            case 140 -> Blocks.FLOWER_POT.defaultBlockState();
            case 141 -> Blocks.CARROTS.defaultBlockState();
            case 142 -> Blocks.POTATOES.defaultBlockState();
            case 143 -> Blocks.OAK_BUTTON.defaultBlockState();
            case 144 -> legacySkull(meta);
            case 145 -> Blocks.ANVIL.defaultBlockState();
            case 146 -> Blocks.TRAPPED_CHEST.defaultBlockState();
            case 147 -> Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState();
            case 148 -> Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState();
            case 149, 150 -> Blocks.COMPARATOR.defaultBlockState();
            case 151 -> Blocks.DAYLIGHT_DETECTOR.defaultBlockState();
            case 152 -> Blocks.REDSTONE_BLOCK.defaultBlockState();
            case 153 -> Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
            case 154 -> Blocks.HOPPER.defaultBlockState();
            case 155 -> legacyQuartz(meta);
            case 156 -> Blocks.QUARTZ_STAIRS.defaultBlockState();
            case 157 -> Blocks.ACTIVATOR_RAIL.defaultBlockState();
            case 158 -> Blocks.DROPPER.defaultBlockState();
            case 159 -> legacyTerracotta(meta);
            case 160 -> legacyStainedGlassPane(meta);
            case 161 -> legacyLeaves2(meta);
            case 162 -> legacyLog2(meta);
            case 163 -> Blocks.ACACIA_STAIRS.defaultBlockState();
            case 164 -> Blocks.DARK_OAK_STAIRS.defaultBlockState();
            case 165 -> Blocks.SLIME_BLOCK.defaultBlockState();
            case 166 -> Blocks.BARRIER.defaultBlockState();
            case 167 -> Blocks.IRON_TRAPDOOR.defaultBlockState();
            case 168 -> legacyPrismarine(meta);
            case 169 -> Blocks.SEA_LANTERN.defaultBlockState();
            case 170 -> Blocks.HAY_BLOCK.defaultBlockState();
            case 171 -> legacyCarpet(meta);
            case 172 -> Blocks.TERRACOTTA.defaultBlockState();
            case 173 -> Blocks.COAL_BLOCK.defaultBlockState();
            case 174 -> Blocks.PACKED_ICE.defaultBlockState();
            case 175 -> legacyDoublePlant(meta);
            default -> fallback();
        };
    }

    public static BlockState fromLegacyName(String legacyName, int legacyMeta) {
        return safeState(resolveName(legacyName, legacyMeta));
    }

    public static BlockState withLegacyMeta(BlockState state, int legacyMeta) {
        BlockState safe = safeState(state);
        if (safe.hasProperty(LegacySellafieldBlock.LEVEL)) {
            return safe.setValue(LegacySellafieldBlock.LEVEL, Mth.clamp(legacyMeta, 0, 5));
        }
        if (safe.hasProperty(LegacySellafieldSlakedBlock.LEVEL)) {
            return safe.setValue(LegacySellafieldSlakedBlock.LEVEL, Mth.clamp(legacyMeta, 0, 15));
        }
        if (safe.hasProperty(FalloutLayerBlock.LAYERS)) {
            return safe.setValue(FalloutLayerBlock.LAYERS, Mth.clamp((legacyMeta & 7) + 1, 1, 8));
        }
        return safe;
    }

    public static BlockState safeState(BlockState state) {
        return state == null || state.isAir() ? fallback() : state;
    }

    public static int stateId(BlockState state) {
        return Block.getId(safeState(state));
    }

    public static void putState(CompoundTag data, BlockState state) {
        data.putInt(KEY_STATE, stateId(state));
    }

    public static void putLegacyName(CompoundTag data, String legacyName, int legacyMeta) {
        if (legacyName == null || legacyName.isBlank()) {
            putState(data, fallback());
            return;
        }
        data.putString(KEY_BLOCK_NAME, legacyName);
        data.putInt(KEY_META, legacyMeta);
    }

    public static void putLegacyId(CompoundTag data, int legacyBlockId, int legacyMeta) {
        data.putInt(KEY_BLOCK, legacyBlockId);
        data.putInt(KEY_META, legacyMeta);
    }

    private static BlockState fromParticleNameFields(CompoundTag data) {
        String[] keys = {
                KEY_BLOCK_NAME,
                KEY_LEGACY_BLOCK,
                KEY_LEGACY_BLOCK_NAME,
                KEY_BLOCK_NAME_SNAKE,
                KEY_LEGACY_BLOCK_SNAKE
        };
        for (String key : keys) {
            if (data.contains(key, Tag.TAG_STRING)) {
                BlockState state = resolveName(data.getString(key), data.getInt(KEY_META));
                if (state != null) {
                    return state;
                }
            }
        }
        return null;
    }

    private static BlockState resolveName(String name, int legacyMeta) {
        String normalized = normalizeName(name);
        if (normalized.isBlank()) {
            return null;
        }

        BlockState aliased = vanillaAlias(normalized, legacyMeta);
        if (aliased != null) {
            return aliased;
        }

        ResourceLocation explicitId = ResourceLocation.tryParse(normalized);
        if (explicitId != null && normalized.contains(":")) {
            if ("hbm".equals(explicitId.getNamespace())) {
                BlockState legacy = hbmLegacyState(explicitId.getPath(), legacyMeta);
                if (legacy != null) {
                    return legacy;
                }
            }
            Block block = ForgeRegistries.BLOCKS.getValue(explicitId);
            return block == null ? null : withLegacyMeta(block.defaultBlockState(), legacyMeta);
        }

        BlockState hbm = hbmLegacyState(normalized, legacyMeta);
        if (hbm != null) {
            return hbm;
        }

        ResourceLocation vanillaId = ResourceLocation.tryParse("minecraft:" + normalized);
        if (vanillaId != null) {
            Block block = ForgeRegistries.BLOCKS.getValue(vanillaId);
            if (block != null) {
                return withLegacyMeta(block.defaultBlockState(), legacyMeta);
            }
        }
        return null;
    }

    private static BlockState hbmLegacyState(String path, int legacyMeta) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(path);
        if (block != null && block.isPresent()) {
            return withLegacyMeta(block.get().defaultBlockState(), legacyMeta);
        }
        return switch (path) {
            case "sand_mix", "sand_boron_layer" -> Blocks.SAND.defaultBlockState();
            default -> null;
        };
    }

    private static BlockState vanillaAlias(String normalized, int legacyMeta) {
        return switch (normalized) {
            case "minecraft:grass", "grass" -> Blocks.GRASS_BLOCK.defaultBlockState();
            case "minecraft:hardened_clay", "hardened_clay" -> Blocks.TERRACOTTA.defaultBlockState();
            case "minecraft:stained_hardened_clay", "stained_hardened_clay" -> legacyTerracotta(legacyMeta);
            case "minecraft:snow_layer", "snow_layer" -> snowLayer(legacyMeta);
            case "minecraft:stone", "stone" -> legacyStone(legacyMeta);
            case "minecraft:dirt", "dirt" -> fromLegacyVanillaId(3, legacyMeta);
            case "minecraft:planks", "planks" -> legacyPlanks(legacyMeta);
            case "minecraft:log", "log" -> legacyLog(legacyMeta);
            case "minecraft:log2", "log2" -> legacyLog2(legacyMeta);
            case "minecraft:leaves", "leaves" -> legacyLeaves(legacyMeta);
            case "minecraft:leaves2", "leaves2" -> legacyLeaves2(legacyMeta);
            case "minecraft:sapling", "sapling" -> legacySapling(legacyMeta);
            case "minecraft:tallgrass", "tallgrass" -> legacyTallGrass(legacyMeta);
            case "minecraft:yellow_flower", "yellow_flower" -> Blocks.DANDELION.defaultBlockState();
            case "minecraft:red_flower", "red_flower" -> legacyRedFlower(legacyMeta);
            case "minecraft:sand", "sand" -> legacySand(legacyMeta);
            case "minecraft:sandstone", "sandstone" -> legacySandstone(legacyMeta);
            case "minecraft:double_stone_slab", "double_stone_slab" -> legacyDoubleStoneSlab(legacyMeta);
            case "minecraft:stone_slab", "stone_slab" -> legacyStoneSlab(legacyMeta);
            case "minecraft:stonebrick", "stonebrick" -> legacyStoneBricks(legacyMeta);
            case "minecraft:monster_egg", "monster_egg" -> legacyMonsterEgg(legacyMeta);
            case "minecraft:cobblestone_wall", "cobblestone_wall" -> legacyCobblestoneWall(legacyMeta);
            case "minecraft:skull", "skull" -> legacySkull(legacyMeta);
            case "minecraft:quartz_block", "quartz_block" -> legacyQuartz(legacyMeta);
            case "minecraft:prismarine", "prismarine" -> legacyPrismarine(legacyMeta);
            case "minecraft:wool", "wool" -> legacyWool(legacyMeta);
            case "minecraft:stained_glass", "stained_glass" -> legacyStainedGlass(legacyMeta);
            case "minecraft:stained_glass_pane", "stained_glass_pane" -> legacyStainedGlassPane(legacyMeta);
            case "minecraft:carpet", "carpet" -> legacyCarpet(legacyMeta);
            case "minecraft:double_plant", "double_plant" -> legacyDoublePlant(legacyMeta);
            case "minecraft:wooden_slab", "wooden_slab" -> legacyWoodSlab(legacyMeta);
            case "minecraft:double_wooden_slab", "double_wooden_slab" -> legacyDoubleWoodSlab(legacyMeta);
            case "minecraft:waterlily", "waterlily" -> Blocks.LILY_PAD.defaultBlockState();
            case "minecraft:reeds", "reeds" -> Blocks.SUGAR_CANE.defaultBlockState();
            case "minecraft:lit_pumpkin", "lit_pumpkin" -> Blocks.JACK_O_LANTERN.defaultBlockState();
            case "minecraft:lit_redstone_lamp", "lit_redstone_lamp" -> Blocks.REDSTONE_LAMP.defaultBlockState();
            case "minecraft:lit_furnace", "lit_furnace" -> Blocks.FURNACE.defaultBlockState();
            case "minecraft:lit_redstone_ore", "lit_redstone_ore" -> Blocks.REDSTONE_ORE.defaultBlockState();
            case "minecraft:unlit_redstone_torch", "unlit_redstone_torch" -> Blocks.REDSTONE_TORCH.defaultBlockState();
            case "minecraft:unpowered_repeater", "minecraft:powered_repeater",
                    "unpowered_repeater", "powered_repeater" -> Blocks.REPEATER.defaultBlockState();
            case "minecraft:unpowered_comparator", "minecraft:powered_comparator",
                    "unpowered_comparator", "powered_comparator" -> Blocks.COMPARATOR.defaultBlockState();
            default -> null;
        };
    }

    private static String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("tile.")) {
            normalized = normalized.substring("tile.".length());
        }
        if (normalized.endsWith(".name")) {
            normalized = normalized.substring(0, normalized.length() - ".name".length());
        }
        return normalized;
    }

    private static BlockState legacyStone(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.GRANITE.defaultBlockState();
            case 2 -> Blocks.POLISHED_GRANITE.defaultBlockState();
            case 3 -> Blocks.DIORITE.defaultBlockState();
            case 4 -> Blocks.POLISHED_DIORITE.defaultBlockState();
            case 5 -> Blocks.ANDESITE.defaultBlockState();
            case 6 -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState legacySand(int meta) {
        return (meta & 1) == 1 ? Blocks.RED_SAND.defaultBlockState() : Blocks.SAND.defaultBlockState();
    }

    private static BlockState snowLayer(int meta) {
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Mth.clamp((meta & 7) + 1, 1, 8));
    }

    private static BlockState legacySapling(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SPRUCE_SAPLING.defaultBlockState();
            case 2 -> Blocks.BIRCH_SAPLING.defaultBlockState();
            case 3 -> Blocks.JUNGLE_SAPLING.defaultBlockState();
            case 4 -> Blocks.ACACIA_SAPLING.defaultBlockState();
            case 5 -> Blocks.DARK_OAK_SAPLING.defaultBlockState();
            default -> Blocks.OAK_SAPLING.defaultBlockState();
        };
    }

    private static BlockState legacyTallGrass(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.GRASS.defaultBlockState();
            case 2 -> Blocks.FERN.defaultBlockState();
            default -> Blocks.DEAD_BUSH.defaultBlockState();
        };
    }

    private static BlockState legacyRedFlower(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.BLUE_ORCHID.defaultBlockState();
            case 2 -> Blocks.ALLIUM.defaultBlockState();
            case 3 -> Blocks.AZURE_BLUET.defaultBlockState();
            case 4 -> Blocks.RED_TULIP.defaultBlockState();
            case 5 -> Blocks.ORANGE_TULIP.defaultBlockState();
            case 6 -> Blocks.WHITE_TULIP.defaultBlockState();
            case 7 -> Blocks.PINK_TULIP.defaultBlockState();
            case 8 -> Blocks.OXEYE_DAISY.defaultBlockState();
            default -> Blocks.POPPY.defaultBlockState();
        };
    }

    private static BlockState legacyPlanks(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case 2 -> Blocks.BIRCH_PLANKS.defaultBlockState();
            case 3 -> Blocks.JUNGLE_PLANKS.defaultBlockState();
            case 4 -> Blocks.ACACIA_PLANKS.defaultBlockState();
            case 5 -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            default -> Blocks.OAK_PLANKS.defaultBlockState();
        };
    }

    private static BlockState legacyLog(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.SPRUCE_LOG.defaultBlockState();
            case 2 -> Blocks.BIRCH_LOG.defaultBlockState();
            case 3 -> Blocks.JUNGLE_LOG.defaultBlockState();
            default -> Blocks.OAK_LOG.defaultBlockState();
        };
    }

    private static BlockState legacyLog2(int meta) {
        return (meta & 3) == 1 ? Blocks.DARK_OAK_LOG.defaultBlockState() : Blocks.ACACIA_LOG.defaultBlockState();
    }

    private static BlockState legacyLeaves(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.SPRUCE_LEAVES.defaultBlockState();
            case 2 -> Blocks.BIRCH_LEAVES.defaultBlockState();
            case 3 -> Blocks.JUNGLE_LEAVES.defaultBlockState();
            default -> Blocks.OAK_LEAVES.defaultBlockState();
        };
    }

    private static BlockState legacyLeaves2(int meta) {
        return (meta & 3) == 1 ? Blocks.DARK_OAK_LEAVES.defaultBlockState() : Blocks.ACACIA_LEAVES.defaultBlockState();
    }

    private static BlockState legacyDoubleStoneSlab(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SANDSTONE.defaultBlockState();
            case 2 -> Blocks.OAK_PLANKS.defaultBlockState();
            case 3 -> Blocks.COBBLESTONE.defaultBlockState();
            case 4 -> Blocks.BRICKS.defaultBlockState();
            case 5 -> Blocks.STONE_BRICKS.defaultBlockState();
            case 6 -> Blocks.NETHER_BRICKS.defaultBlockState();
            case 7 -> Blocks.QUARTZ_BLOCK.defaultBlockState();
            default -> Blocks.SMOOTH_STONE.defaultBlockState();
        };
    }

    private static BlockState legacyStoneSlab(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SANDSTONE_SLAB.defaultBlockState();
            case 2 -> Blocks.PETRIFIED_OAK_SLAB.defaultBlockState();
            case 3 -> Blocks.COBBLESTONE_SLAB.defaultBlockState();
            case 4 -> Blocks.BRICK_SLAB.defaultBlockState();
            case 5 -> Blocks.STONE_BRICK_SLAB.defaultBlockState();
            case 6 -> Blocks.NETHER_BRICK_SLAB.defaultBlockState();
            case 7 -> Blocks.QUARTZ_SLAB.defaultBlockState();
            default -> Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        };
    }

    private static BlockState legacyDoubleWoodSlab(int meta) {
        return legacyPlanks(meta);
    }

    private static BlockState legacyWoodSlab(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.SPRUCE_SLAB.defaultBlockState();
            case 2 -> Blocks.BIRCH_SLAB.defaultBlockState();
            case 3 -> Blocks.JUNGLE_SLAB.defaultBlockState();
            case 4 -> Blocks.ACACIA_SLAB.defaultBlockState();
            case 5 -> Blocks.DARK_OAK_SLAB.defaultBlockState();
            default -> Blocks.OAK_SLAB.defaultBlockState();
        };
    }

    private static BlockState legacySandstone(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.CHISELED_SANDSTONE.defaultBlockState();
            case 2 -> Blocks.CUT_SANDSTONE.defaultBlockState();
            default -> Blocks.SANDSTONE.defaultBlockState();
        };
    }

    private static BlockState legacyStoneBricks(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            case 2 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case 3 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    private static BlockState legacyMonsterEgg(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.INFESTED_COBBLESTONE.defaultBlockState();
            case 2 -> Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
            case 3 -> Blocks.INFESTED_MOSSY_STONE_BRICKS.defaultBlockState();
            case 4 -> Blocks.INFESTED_CRACKED_STONE_BRICKS.defaultBlockState();
            case 5 -> Blocks.INFESTED_CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.INFESTED_STONE.defaultBlockState();
        };
    }

    private static BlockState legacyCobblestoneWall(int meta) {
        return (meta & 1) == 1 ? Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState() : Blocks.COBBLESTONE_WALL.defaultBlockState();
    }

    private static BlockState legacySkull(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.WITHER_SKELETON_SKULL.defaultBlockState();
            case 2 -> Blocks.ZOMBIE_HEAD.defaultBlockState();
            case 3 -> Blocks.PLAYER_HEAD.defaultBlockState();
            case 4 -> Blocks.CREEPER_HEAD.defaultBlockState();
            case 5 -> Blocks.DRAGON_HEAD.defaultBlockState();
            default -> Blocks.SKELETON_SKULL.defaultBlockState();
        };
    }

    private static BlockState legacyQuartz(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
            case 2 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
            default -> Blocks.QUARTZ_BLOCK.defaultBlockState();
        };
    }

    private static BlockState legacyPrismarine(int meta) {
        return switch (meta & 3) {
            case 1 -> Blocks.PRISMARINE_BRICKS.defaultBlockState();
            case 2 -> Blocks.DARK_PRISMARINE.defaultBlockState();
            default -> Blocks.PRISMARINE.defaultBlockState();
        };
    }

    private static BlockState legacyWool(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_WOOL.defaultBlockState();
            case 2 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
            case 4 -> Blocks.YELLOW_WOOL.defaultBlockState();
            case 5 -> Blocks.LIME_WOOL.defaultBlockState();
            case 6 -> Blocks.PINK_WOOL.defaultBlockState();
            case 7 -> Blocks.GRAY_WOOL.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
            case 9 -> Blocks.CYAN_WOOL.defaultBlockState();
            case 10 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 11 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 12 -> Blocks.BROWN_WOOL.defaultBlockState();
            case 13 -> Blocks.GREEN_WOOL.defaultBlockState();
            case 14 -> Blocks.RED_WOOL.defaultBlockState();
            case 15 -> Blocks.BLACK_WOOL.defaultBlockState();
            default -> Blocks.WHITE_WOOL.defaultBlockState();
        };
    }

    private static BlockState legacyTerracotta(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            case 2 -> Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
            case 4 -> Blocks.YELLOW_TERRACOTTA.defaultBlockState();
            case 5 -> Blocks.LIME_TERRACOTTA.defaultBlockState();
            case 6 -> Blocks.PINK_TERRACOTTA.defaultBlockState();
            case 7 -> Blocks.GRAY_TERRACOTTA.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
            case 9 -> Blocks.CYAN_TERRACOTTA.defaultBlockState();
            case 10 -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
            case 11 -> Blocks.BLUE_TERRACOTTA.defaultBlockState();
            case 12 -> Blocks.BROWN_TERRACOTTA.defaultBlockState();
            case 13 -> Blocks.GREEN_TERRACOTTA.defaultBlockState();
            case 14 -> Blocks.RED_TERRACOTTA.defaultBlockState();
            case 15 -> Blocks.BLACK_TERRACOTTA.defaultBlockState();
            default -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
        };
    }

    private static BlockState legacyStainedGlass(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_STAINED_GLASS.defaultBlockState();
            case 2 -> Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
            case 4 -> Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
            case 5 -> Blocks.LIME_STAINED_GLASS.defaultBlockState();
            case 6 -> Blocks.PINK_STAINED_GLASS.defaultBlockState();
            case 7 -> Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
            case 9 -> Blocks.CYAN_STAINED_GLASS.defaultBlockState();
            case 10 -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            case 11 -> Blocks.BLUE_STAINED_GLASS.defaultBlockState();
            case 12 -> Blocks.BROWN_STAINED_GLASS.defaultBlockState();
            case 13 -> Blocks.GREEN_STAINED_GLASS.defaultBlockState();
            case 14 -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            case 15 -> Blocks.BLACK_STAINED_GLASS.defaultBlockState();
            default -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
        };
    }

    private static BlockState legacyStainedGlassPane(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState();
            case 2 -> Blocks.MAGENTA_STAINED_GLASS_PANE.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();
            case 4 -> Blocks.YELLOW_STAINED_GLASS_PANE.defaultBlockState();
            case 5 -> Blocks.LIME_STAINED_GLASS_PANE.defaultBlockState();
            case 6 -> Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState();
            case 7 -> Blocks.GRAY_STAINED_GLASS_PANE.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.defaultBlockState();
            case 9 -> Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState();
            case 10 -> Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState();
            case 11 -> Blocks.BLUE_STAINED_GLASS_PANE.defaultBlockState();
            case 12 -> Blocks.BROWN_STAINED_GLASS_PANE.defaultBlockState();
            case 13 -> Blocks.GREEN_STAINED_GLASS_PANE.defaultBlockState();
            case 14 -> Blocks.RED_STAINED_GLASS_PANE.defaultBlockState();
            case 15 -> Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState();
            default -> Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState();
        };
    }

    private static BlockState legacyCarpet(int meta) {
        return switch (meta & 15) {
            case 1 -> Blocks.ORANGE_CARPET.defaultBlockState();
            case 2 -> Blocks.MAGENTA_CARPET.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
            case 4 -> Blocks.YELLOW_CARPET.defaultBlockState();
            case 5 -> Blocks.LIME_CARPET.defaultBlockState();
            case 6 -> Blocks.PINK_CARPET.defaultBlockState();
            case 7 -> Blocks.GRAY_CARPET.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
            case 9 -> Blocks.CYAN_CARPET.defaultBlockState();
            case 10 -> Blocks.PURPLE_CARPET.defaultBlockState();
            case 11 -> Blocks.BLUE_CARPET.defaultBlockState();
            case 12 -> Blocks.BROWN_CARPET.defaultBlockState();
            case 13 -> Blocks.GREEN_CARPET.defaultBlockState();
            case 14 -> Blocks.RED_CARPET.defaultBlockState();
            case 15 -> Blocks.BLACK_CARPET.defaultBlockState();
            default -> Blocks.WHITE_CARPET.defaultBlockState();
        };
    }

    private static BlockState legacyDoublePlant(int meta) {
        return switch (meta & 7) {
            case 1 -> Blocks.LILAC.defaultBlockState();
            case 2 -> Blocks.TALL_GRASS.defaultBlockState();
            case 3 -> Blocks.LARGE_FERN.defaultBlockState();
            case 4 -> Blocks.ROSE_BUSH.defaultBlockState();
            case 5 -> Blocks.PEONY.defaultBlockState();
            default -> Blocks.SUNFLOWER.defaultBlockState();
        };
    }

    private static BlockState fallback() {
        return Blocks.STONE.defaultBlockState();
    }

    private LegacyBlockStateMappings() {
    }
}
