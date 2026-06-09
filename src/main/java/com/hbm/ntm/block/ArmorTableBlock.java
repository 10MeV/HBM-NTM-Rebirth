package com.hbm.ntm.block;

import com.hbm.ntm.menu.ArmorTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class ArmorTableBlock extends Block {
    private static final Component TITLE = Component.translatable("container.armorTable");

    public ArmorTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, menuProvider(pos), pos);
        }
        return player.isShiftKeyDown() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static MenuProvider menuProvider(BlockPos pos) {
        return new SimpleMenuProvider((containerId, inventory, player) ->
                new ArmorTableMenu(containerId, inventory, pos), TITLE);
    }
}
