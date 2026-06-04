package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RefineryBlock extends LegacyVisibleMultiblockMachineBlock {
    public RefineryBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefineryBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RefineryBlockEntity refinery) {
            return refinery.isExploded() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.REFINERY.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof RefineryBlockEntity refinery) {
                RefineryBlockEntity.serverTick(tickLevel, tickPos, tickState, refinery);
            }
        };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof RefineryBlockEntity refinery) {
            return List.of(refinery.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!(level.getBlockEntity(pos) instanceof RefineryBlockEntity refinery)) {
            return;
        }
        if (!refinery.markExplosionHandled(explosion)) {
            return;
        }
        if (!refinery.isExploded()) {
            refinery.explode();
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
        for (HbmFluidTank tank : readTooltipTanks(persistent)) {
            tooltip.add(HbmFluidGuiHelper.tankInfo(tank, tank.getFill(), tank.getMaxFill())
                    .copy()
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (persistent.getBoolean("hasExploded")) {
            tooltip.add(Component.translatable("container.fluidtank.damaged").withStyle(ChatFormatting.RED));
        }
        if (persistent.getBoolean("onFire")) {
            tooltip.add(Component.translatable("container.fluidtank.burning").withStyle(ChatFormatting.RED));
        }
    }

    private static List<HbmFluidTank> readTooltipTanks(CompoundTag persistent) {
        List<HbmFluidTank> tanks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
            tank.readFromNbt(persistent, Integer.toString(i));
            tanks.add(tank);
        }
        return tanks;
    }
}
