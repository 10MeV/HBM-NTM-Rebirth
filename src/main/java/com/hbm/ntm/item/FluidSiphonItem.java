package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFluidStandardReceiverMK2;
import com.hbm.ntm.compat.CompatExternal;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidSiphonItem extends Item {
    public FluidSiphonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity core = CompatExternal.getCoreFromPos(level, context.getClickedPos());
        if (!(core instanceof IFluidStandardReceiverMK2 receiver)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean drained = siphonFirstMatchingTank(player, receiver.getReceivingTanks());
        if (!drained) {
            return InteractionResult.PASS;
        }
        markFluidTargetChanged(level, core);
        return InteractionResult.CONSUME;
    }

    private static boolean siphonFirstMatchingTank(Player player, List<HbmFluidTank> tanks) {
        if (tanks == null || tanks.isEmpty()) {
            return false;
        }
        for (HbmFluidTank tank : tanks) {
            if (tank == null || tank.getFill() <= 0) {
                continue;
            }
            FluidType tankType = tank.getTankType();
            if (tankType.hasTrait(SimpleFluidTraits.Unsiphonable.class)) {
                continue;
            }
            if (siphonTankIntoInventory(player, tank, tankType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean siphonTankIntoInventory(Player player, HbmFluidTank tank, FluidType tankType) {
        boolean drained = false;
        ItemStack availablePipette = ItemStack.EMPTY;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            ItemStack emptyStack = inventory.items.get(slot);
            if (emptyStack.isEmpty()) {
                continue;
            }
            if (availablePipette.isEmpty() && emptyStack.getItem() instanceof FluidPipetteItem pipette
                    && pipette.getKind() != FluidPipetteItem.Kind.LABORATORY
                    && !pipette.willFizzle(tankType)) {
                availablePipette = emptyStack;
            }
            HbmFluidContainerRegistry.ContainerEntry container =
                    HbmFluidContainerRegistry.getContainer(tankType, emptyStack);
            if (container == null || container.content() <= 0) {
                continue;
            }
            ItemStack fullContainer = container.copyFullContainer();
            if (fullContainer.isEmpty()) {
                continue;
            }
            while (tank.getFill() >= container.content() && !emptyStack.isEmpty()) {
                emptyStack.shrink(1);
                tank.setFill(tank.getFill() - container.content());
                giveOrDrop(player, fullContainer.copy());
                drained = true;
            }
            if (emptyStack.isEmpty()) {
                inventory.items.set(slot, ItemStack.EMPTY);
            }
        }
        if (!availablePipette.isEmpty() && tank.getFill() < 1_000
                && availablePipette.getItem() instanceof FluidPipetteItem pipette
                && pipette.acceptsFluid(tankType, availablePipette)) {
            tank.setFill(pipette.tryFill(tankType, tank.getFill(), availablePipette));
            drained = true;
        }
        return drained;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static void markFluidTargetChanged(Level level, BlockEntity core) {
        core.setChanged();
        BlockPos pos = core.getBlockPos();
        if (level.hasChunkAt(pos)) {
            level.sendBlockUpdated(pos, core.getBlockState(), core.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
