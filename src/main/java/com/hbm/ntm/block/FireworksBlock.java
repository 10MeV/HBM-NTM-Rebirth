package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FireworksBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Exact 1.7.10 firework battery interaction and redstone carrier. */
@SuppressWarnings("deprecation")
public final class FireworksBlock extends BaseEntityBlock {
    private static final int[] LEGACY_DYE_COLORS = {
            0x1E1B1B, 0xB3312C, 0x3B511A, 0x51301A,
            0x253192, 0x7B2FBE, 0x287697, 0xABABAB,
            0x434343, 0xD88198, 0x41CD34, 0xDECF2A,
            0x6689D3, 0xC354CD, 0xEB8844, 0xF0F0F0
    };

    public FireworksBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FireworksBlockEntity fireworks)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GUNPOWDER)) {
            fireworks.addCharges(stack.getCount() * 3);
            stack.setCount(0);
            return InteractionResult.CONSUME;
        }
        if (stack.is(ModItems.legacyItem("sulfur").get())) {
            fireworks.addCharges(stack.getCount());
            stack.setCount(0);
            return InteractionResult.CONSUME;
        }
        if (stack.getItem() instanceof DyeItem dye) {
            fireworks.setColor(legacyDyeColor(dye.getDyeColor()));
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }
        if (stack.is(Items.NAME_TAG)) {
            fireworks.setMessage(stack.getHoverName().getString());
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }

        player.sendSystemMessage(Component.translatable(getDescriptionId()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("block.hbm_ntm_rebirth.fireworks.charges", fireworks.charges())
                .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.translatable("block.hbm_ntm_rebirth.fireworks.color",
                Integer.toHexString(fireworks.color())).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.translatable("block.hbm_ntm_rebirth.fireworks.message", fireworks.message())
                .withStyle(ChatFormatting.YELLOW));
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireworksBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FIREWORKS.get(), FireworksBlockEntity::serverTick);
    }

    private static int legacyDyeColor(DyeColor color) {
        return LEGACY_DYE_COLORS[switch (color) {
            case BLACK -> 0;
            case RED -> 1;
            case GREEN -> 2;
            case BROWN -> 3;
            case BLUE -> 4;
            case PURPLE -> 5;
            case CYAN -> 6;
            case LIGHT_GRAY -> 7;
            case GRAY -> 8;
            case PINK -> 9;
            case LIME -> 10;
            case YELLOW -> 11;
            case LIGHT_BLUE -> 12;
            case MAGENTA -> 13;
            case ORANGE -> 14;
            case WHITE -> 15;
        }];
    }
}
