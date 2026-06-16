package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.BobMathUtil;
import java.math.BigInteger;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
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
public class BatteryReddBlock extends LegacyVisibleMultiblockMachineBlock {
    public BatteryReddBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryReddBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof BatteryReddBlockEntity battery) {
            NetworkHooks.openScreen(serverPlayer, battery, battery.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.BATTERY_REDD.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> BatteryReddBlockEntity.clientTick(
                        tickLevel, tickPos, tickState, (BatteryReddBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> BatteryReddBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (BatteryReddBlockEntity) blockEntity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BatteryReddBlockEntity battery) {
            ItemStack stack = new ItemStack(this);
            battery.writePersistentStateToStack(stack);
            return List.of(stack);
        }
        return super.getDrops(state, builder);
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
        if (persistent.contains("power", Tag.TAG_BYTE_ARRAY)) {
            tooltip.add(Component.literal(BobMathUtil.format(new BigInteger(persistent.getByteArray("power")))
                    + " HE").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BatteryReddBlockEntity battery) {
            for (ItemStack stack : battery.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
