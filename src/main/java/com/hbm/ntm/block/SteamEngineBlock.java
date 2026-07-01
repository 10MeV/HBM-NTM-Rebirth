package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SteamEngineBlock extends LegacyVisibleMultiblockMachineBlock {
    public SteamEngineBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        LegacyStandardInfoTooltip.append(tooltip, "machine_steam_engine");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamEngineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.STEAM_ENGINE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        SteamEngineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (SteamEngineBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        SteamEngineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (SteamEngineBlockEntity) blockEntity);
    }
}
