package com.hbm.ntm.item;

import com.hbm.ntm.blockentity.BedrockOreDepositBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class SurveyScannerItem extends Item {
    private static final int LEGACY_SAMPLE_RADIUS = 5;
    private static final int ORE_SAMPLE_SPACING = 5;
    private static final int BEDROCK_SAMPLE_SPACING = 2;
    private static final int BEDROCK_SCAN_HEIGHT = 6;
    private static final String[] SCHIST_BLOCKS = {
            "stone_gneiss",
            "ore_gneiss_iron",
            "ore_gneiss_gold",
            "ore_gneiss_uranium",
            "ore_gneiss_uranium_scorched",
            "ore_gneiss_copper",
            "ore_gneiss_asbestos",
            "ore_gneiss_lithium",
            "ore_gneiss_schrabidium",
            "ore_gneiss_rare",
            "ore_gneiss_gas"
    };

    public SurveyScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (!scan(level, player)) {
                send(player, "No survey targets found.", ChatFormatting.GRAY);
            }
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (!level.isClientSide) {
            if (!scan(level, player)) {
                send(player, "No survey targets found.", ChatFormatting.GRAY);
            }
        }
        player.swing(context.getHand(), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean scan(Level level, Player player) {
        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();
        int minY = level.getMinBuildHeight();
        int startY = Math.min(y + 15, level.getMaxBuildHeight() - 1);

        boolean hasOil = false;
        boolean hasColtan = false;
        boolean hasBedrockOil = false;
        boolean hasDepth = false;
        boolean hasSchist = false;
        boolean hasAussie = false;
        boolean found = false;
        BedrockOreDepositBlockEntity bedrockOre = null;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int a = -LEGACY_SAMPLE_RADIUS; a <= LEGACY_SAMPLE_RADIUS; a++) {
            for (int b = -LEGACY_SAMPLE_RADIUS; b <= LEGACY_SAMPLE_RADIUS; b++) {
                for (int i = startY; i > minY + 1; i -= 2) {
                    cursor.set(x + a * ORE_SAMPLE_SPACING, i, z + b * ORE_SAMPLE_SPACING);
                    Block block = level.getBlockState(cursor).getBlock();
                    if (isLegacyBlock(block, "ore_oil")) {
                        hasOil = true;
                    } else if (isLegacyBlock(block, "ore_coltan")) {
                        hasColtan = true;
                    } else if (isLegacyBlock(block, "ore_bedrock_oil")) {
                        hasBedrockOil = true;
                    } else if (isLegacyBlock(block, "stone_depth") || isLegacyBlock(block, "stone_depth_nether")) {
                        hasDepth = true;
                    } else if (isAnyLegacyBlock(block, SCHIST_BLOCKS)) {
                        hasSchist = true;
                    } else if (isLegacyBlock(block, "ore_australium")) {
                        hasAussie = true;
                    }
                }

                BedrockOreDepositBlockEntity deposit = findBedrockOre(level, cursor,
                        x + a * BEDROCK_SAMPLE_SPACING, minY, z + b * BEDROCK_SAMPLE_SPACING);
                if (deposit != null) {
                    bedrockOre = deposit;
                }
            }
        }

        if (hasOil) {
            send(player, "Found OIL!", ChatFormatting.BLACK);
            found = true;
        }
        if (hasBedrockOil) {
            send(player, "Found BEDROCK OIL!", ChatFormatting.BLACK);
            found = true;
        }
        if (hasColtan) {
            send(player, "Found COLTAN!", ChatFormatting.GOLD);
            found = true;
        }
        if (hasDepth) {
            send(player, "Found DEPTH ROCK!", ChatFormatting.GRAY);
            found = true;
        }
        if (hasSchist) {
            send(player, "Found SCHIST!", ChatFormatting.DARK_AQUA);
            found = true;
        }
        if (hasAussie) {
            send(player, "Found AUSTRALIUM!", ChatFormatting.YELLOW);
            found = true;
        }
        if (bedrockOre != null) {
            ItemStack resource = bedrockOre.getResource();
            if (!resource.isEmpty()) {
                send(player, "Found BEDROCK ORE for " + resource.getHoverName().getString() + "!",
                        ChatFormatting.RED);
                found = true;
            }
        }
        return found;
    }

    private static void send(Player player, String message, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(message).withStyle(color));
    }

    private static boolean isLegacyBlock(Block block, String legacyName) {
        Block target = legacyBlock(legacyName);
        return target != null && block == target;
    }

    private static boolean isAnyLegacyBlock(Block block, String... legacyNames) {
        for (String legacyName : legacyNames) {
            if (isLegacyBlock(block, legacyName)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static BedrockOreDepositBlockEntity findBedrockOre(Level level, BlockPos.MutableBlockPos cursor,
            int x, int minY, int z) {
        for (int yOffset = 0; yOffset <= BEDROCK_SCAN_HEIGHT; yOffset++) {
            cursor.set(x, minY + yOffset, z);
            if (level.isOutsideBuildHeight(cursor)) {
                continue;
            }
            if (isLegacyBlock(level.getBlockState(cursor).getBlock(), "ore_bedrock")
                    && level.getBlockEntity(cursor) instanceof BedrockOreDepositBlockEntity deposit) {
                return deposit;
            }
        }
        return null;
    }

    @Nullable
    private static Block legacyBlock(String legacyName) {
        RegistryObject<? extends Block> object = ModBlocks.legacyBlock(legacyName);
        return object == null ? null : object.get();
    }
}
