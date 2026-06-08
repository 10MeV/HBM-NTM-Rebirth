package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class OilDrillBlock extends LegacyVisibleMultiblockMachineBlock {
    public OilDrillBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilDrillBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof OilDrillBlockEntity drill) {
            NetworkHooks.openScreen(serverPlayer, drill, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof OilDrillBlockEntity drill) {
            for (ItemStack stack : drill.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.OIL_DRILL.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof OilDrillBlockEntity drill) {
                        OilDrillBlockEntity.clientTick(tickLevel, tickPos, tickState, drill);
                    }
                }
                : (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof OilDrillBlockEntity drill) {
                        OilDrillBlockEntity.serverTick(tickLevel, tickPos, tickState, drill);
                    }
                };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof OilDrillBlockEntity drill) {
            return List.of(drill.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof OilDrillBlockEntity drill && drill.hasStoredFluid()) {
            drill.clearStoredFluids();
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 15.0F,
                    Level.ExplosionInteraction.TNT);
        }
        super.onBlockExploded(state, level, pos, explosion);
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
        tooltip.add(Component.literal(shortPower(persistent.getLong("power")) + "HE").withStyle(ChatFormatting.GREEN));
        for (HbmFluidTank tank : readTooltipTanks(persistent)) {
            tooltip.add(HbmFluidGuiHelper.tankInfo(tank, tank.getFill(), tank.getMaxFill())
                    .copy()
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static List<HbmFluidTank> readTooltipTanks(CompoundTag persistent) {
        List<HbmFluidTank> tanks = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
            tank.readFromNbt(persistent, "t" + i);
            tanks.add(tank);
        }
        return tanks;
    }

    private static String shortPower(long power) {
        if (power < 1_000L) {
            return Long.toString(power);
        }
        if (power < 1_000_000L) {
            return trim(power / 1_000.0D) + "k";
        }
        if (power < 1_000_000_000L) {
            return trim(power / 1_000_000.0D) + "M";
        }
        return trim(power / 1_000_000_000.0D) + "G";
    }

    private static String trim(double value) {
        String text = String.format(java.util.Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
