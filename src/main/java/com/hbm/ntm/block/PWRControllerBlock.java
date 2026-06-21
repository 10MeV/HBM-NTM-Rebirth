package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PWRControllerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class PWRControllerBlock extends HorizontalMachineBlock implements EntityBlock {
    public PWRControllerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PWRControllerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof PWRControllerBlockEntity controller) {
            if (controller.isAssembled() && isPwrPrinter(player.getItemInHand(hand))) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (!controller.isAssembled()) {
                controller.assemble(player);
            } else if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, controller, controller.getBlockPos());
            }
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.PWR_CONTROLLER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        PWRControllerBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (PWRControllerBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        PWRControllerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (PWRControllerBlockEntity) blockEntity);
    }

    private static boolean isPwrPrinter(ItemStack stack) {
        RegistryObject<Item> printer = ModItems.legacyItem("pwr_printer");
        return printer != null && stack.is(printer.get());
    }
}
