package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.ElectricHeaterBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ElectricHeaterBlock extends LegacyVisibleMultiblockMachineBlock implements Toolable {
    public ElectricHeaterBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricHeaterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ELECTRIC_HEATER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        ElectricHeaterBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (ElectricHeaterBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        ElectricHeaterBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (ElectricHeaterBlockEntity) blockEntity);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, net.minecraft.core.Direction side,
            Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof ElectricHeaterBlockEntity heater) {
            heater.toggleSetting();
        }
        return true;
    }
}
