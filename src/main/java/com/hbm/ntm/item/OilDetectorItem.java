package com.hbm.ntm.item;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class OilDetectorItem extends Item {
    private static final int INFORM_ID_DETONATOR = 8;

    public OilDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.oil_detector.desc1"));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.oil_detector.desc2"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ScanResult result = scan(level, player);
            Component message = switch (result) {
                case DIRECT -> Component.translatable("item.hbm_ntm_rebirth.oil_detector.bullseye")
                        .withStyle(ChatFormatting.DARK_GREEN);
                case NEARBY -> Component.translatable("item.hbm_ntm_rebirth.oil_detector.detected")
                        .withStyle(ChatFormatting.GOLD);
                case NONE -> Component.translatable("item.hbm_ntm_rebirth.oil_detector.noOil")
                        .withStyle(ChatFormatting.RED);
            };
            ModMessages.sendPlayerInform(serverPlayer, message, INFORM_ID_DETONATOR);
            level.playSound(null, player.blockPosition(), ModSounds.ITEM_TECH_BLEEP.get(), SoundSource.PLAYERS,
                    1.0F, 1.0F);
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static ScanResult scan(Level level, Player player) {
        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();
        int startY = Math.min(y + 15, level.getMaxBuildHeight() - 1);
        int minY = level.getMinBuildHeight();

        boolean direct = hasOil(level, x, z, startY, minY + 5);
        boolean oil = direct
                || hasOil(level, x + 5, z, startY, minY + 5)
                || hasOil(level, x - 5, z, startY, minY + 5)
                || hasOil(level, x, z + 5, startY, minY + 5)
                || hasOil(level, x, z - 5, startY, minY + 5)
                || hasOil(level, x + 10, z, startY, minY + 10)
                || hasOil(level, x - 10, z, startY, minY + 10)
                || hasOil(level, x, z + 10, startY, minY + 10)
                || hasOil(level, x, z - 10, startY, minY + 10)
                || hasOil(level, x + 5, z + 5, startY, minY + 5)
                || hasOil(level, x - 5, z + 5, startY, minY + 5)
                || hasOil(level, x + 5, z - 5, startY, minY + 5)
                || hasOil(level, x - 5, z - 5, startY, minY + 5);

        if (direct) {
            return ScanResult.DIRECT;
        }
        return oil ? ScanResult.NEARBY : ScanResult.NONE;
    }

    private static boolean hasOil(Level level, int x, int z, int startY, int lowerExclusiveY) {
        Block oil = legacyBlock("ore_oil");
        if (oil == null) {
            return false;
        }
        for (int i = startY; i > lowerExclusiveY; i--) {
            if (level.getBlockState(new BlockPos(x, i, z)).getBlock() == oil) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Block legacyBlock(String legacyName) {
        RegistryObject<? extends Block> object = ModBlocks.legacyBlock(legacyName);
        return object == null ? null : object.get();
    }

    private enum ScanResult {
        DIRECT,
        NEARBY,
        NONE
    }
}
