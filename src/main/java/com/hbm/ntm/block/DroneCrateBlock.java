package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DroneCrateBlockEntity;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.item.DroneLinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/** Legacy drone_crate: patrol-drone load/unload station, separate from request logistics. */
public class DroneCrateBlock extends Block implements EntityBlock, LegacyLookOverlayBlockProvider {
    public DroneCrateBlock(Properties properties) { super(properties); }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DroneCrateBlockEntity(pos, state);
    }

    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (tickLevel, tickPos, tickState, entity) -> {
            if (entity instanceof DroneCrateBlockEntity crate) DroneCrateBlockEntity.tick(tickLevel, crate);
        };
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof DroneLinkerItem) return InteractionResult.PASS;
        // DroneCrate checked world.isRemote before sneaking: a sneaking client reported
        // success, while the matching server interaction returned false and left the
        // action available to the rest of the use chain.
        if (player.isShiftKeyDown()) return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer server
                && level.getBlockEntity(pos) instanceof DroneCrateBlockEntity crate) {
            NetworkHooks.openScreen(server, crate, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement,
            boolean moved) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof DroneCrateBlockEntity crate) {
            crate.dropContents();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override
    public @Nullable LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        if (!(level.getBlockEntity(viewedPos) instanceof DroneCrateBlockEntity crate)
                || crate.nextTarget() == null) {
            return null;
        }
        BlockPos next = crate.nextTarget();
        return LegacyLookOverlay.forBlockState(viewedState, List.of(Component.literal(
                "Next waypoint: " + next.getX() + " / " + next.getY() + " / " + next.getZ())));
    }

    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,
            @Nullable net.minecraft.world.level.BlockGetter level, java.util.List<net.minecraft.network.chat.Component> tooltip,
            net.minecraft.world.item.TooltipFlag flag) { LegacyStandardInfoTooltip.append(tooltip, "drone_crate"); }
}
