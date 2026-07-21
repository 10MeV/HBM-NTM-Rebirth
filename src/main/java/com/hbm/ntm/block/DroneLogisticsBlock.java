package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DroneLogisticsBlockEntity;
import com.hbm.ntm.item.DroneLinkerItem;
import net.minecraft.core.BlockPos;
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


/** Shared legacy dock/provider/requester shell. */
public class DroneLogisticsBlock extends Block implements EntityBlock {
    private final DroneLogisticsBlockEntity.Kind kind;
    public DroneLogisticsBlock(Properties properties, DroneLogisticsBlockEntity.Kind kind) { super(properties); this.kind = kind; }
    public DroneLogisticsBlockEntity.Kind kind() { return kind; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DroneLogisticsBlockEntity(pos, state, kind); }
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (tickLevel, tickPos, tickState, entity) -> { if (entity instanceof DroneLogisticsBlockEntity logistics) DroneLogisticsBlockEntity.tick(tickLevel, logistics); };
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Legacy DroneDock (also used for provider/requester) returned true immediately on
        // the client, then returned false for a sneaking server player.
        if (player.isShiftKeyDown()) return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof DroneLogisticsBlockEntity logistics) {
            NetworkHooks.openScreen(server, logistics, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof DroneLogisticsBlockEntity logistics) {
            logistics.dropContents();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, level, pos, next, moved);
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,
            @Nullable net.minecraft.world.level.BlockGetter level, java.util.List<net.minecraft.network.chat.Component> tooltip,
            net.minecraft.world.item.TooltipFlag flag) {
        LegacyStandardInfoTooltip.append(tooltip, switch (kind) {
            case DOCK -> "drone_dock";
            case PROVIDER -> "drone_crate_provider";
            case REQUESTER -> "drone_crate_requester";
        });
    }
}
