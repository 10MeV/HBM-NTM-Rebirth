package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyVolcanoCoreBlockEntity;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyVolcanoCoreBlock extends BaseEntityBlock {
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 4);

    public static final int META_STATIC_ACTIVE = 0;
    public static final int META_STATIC_EXTINGUISHING = 1;
    public static final int META_GROWING_ACTIVE = 2;
    public static final int META_GROWING_EXTINGUISHING = 3;
    public static final int META_SMOLDERING = 4;

    public LegacyVolcanoCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, META_STATIC_ACTIVE));
    }

    public static boolean isGrowing(int mode) {
        return mode == META_GROWING_ACTIVE || mode == META_GROWING_EXTINGUISHING;
    }

    public static boolean isExtinguishing(int mode) {
        return mode == META_STATIC_EXTINGUISHING || mode == META_GROWING_EXTINGUISHING;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyVolcanoCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.LEGACY_VOLCANO_CORE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyVolcanoCoreBlockEntity.serverTick(tickLevel, tickPos, tickState, (LegacyVolcanoCoreBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        int mode = legacyVariant(stack);
        if (mode == META_SMOLDERING) {
            tooltip.add(Component.literal("SHIELD VOLCANO").withStyle(ChatFormatting.GOLD));
            return;
        }
        tooltip.add(Component.literal(isGrowing(mode) ? "DOES GROW" : "DOES NOT GROW")
                .withStyle(isGrowing(mode) ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(isExtinguishing(mode) ? "DOES EXTINGUISH" : "DOES NOT EXTINGUISH")
                .withStyle(isExtinguishing(mode) ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
    }

    private static int legacyVariant(ItemStack stack) {
        return stack.getTag() == null ? 0 : Math.max(0, Math.min(4, stack.getTag().getInt(LegacyStateBlockItem.TAG_VARIANT)));
    }
}
