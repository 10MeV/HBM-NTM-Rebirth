package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HadronCoilBlockEntity;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Source-port of the ordinary, cross-tier connected-texture BlockHadronCoil. */
public final class HadronCoilBlock extends Block implements EntityBlock {
    private final int factor;

    public HadronCoilBlock(Properties properties, int factor) {
        super(properties);
        this.factor = factor;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("info.hbm_ntm_rebirth.coil")
                .append(": ").append(Component.literal(String.format(Locale.US, "%,d", factor))));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HadronCoilBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide && !state.is(oldState.getBlock())) refreshConnectedTextureNeighborhood(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (level.isClientSide && !state.is(newState.getBlock())) refreshConnectedTextureNeighborhood(level, pos);
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (level.isClientSide) refreshConnectedTextureNeighborhood(level, pos);
    }

    /** Legacy CTContext observes all orthogonal and diagonal cells around every face. */
    public static void refreshConnectedTextureNeighborhood(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getBlockEntity(pos) instanceof HadronCoilBlockEntity coil) coil.requestModelDataUpdate();
            ClientGeometryInvalidationBridge.schedule(pos);
        }
    }
}
