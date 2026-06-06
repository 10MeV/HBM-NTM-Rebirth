package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidBarrelBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final Variant variant;

    public FluidBarrelBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (variant == Variant.CORRODED) {
            return null;
        }
        return new FluidBarrelBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (variant == Variant.CORRODED) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && held.getItem() instanceof IFluidIdentifierItem identifier) {
                FluidType type = identifier.getIdentifiedFluid(level, pos, held);
                if (barrel.setIdentifiedType(type)) {
                    player.displayClientMessage(Component.literal("Changed type to ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(type.getDisplayName())
                            .append(Component.literal("!").withStyle(ChatFormatting.YELLOW)), true);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            NetworkHooks.openScreen(serverPlayer, barrel, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.FLUID_BARREL.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        FluidBarrelBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (FluidBarrelBlockEntity) blockEntity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmPersistentBlockState persistent) {
            persistent.readPersistentStateFromStack(stack);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (variant == Variant.CORRODED) {
            return 0;
        }
        return level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel ? barrel.getComparatorPower() : 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (variant == Variant.CORRODED) {
            return super.getDrops(state, builder);
        }
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FluidBarrelBlockEntity barrel) {
            List<ItemStack> drops = new java.util.ArrayList<>();
            drops.add(barrel.createPersistentBlockDrop(asItem()));
            drops.addAll(barrel.getDrops());
            return drops;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        variant.appendTooltip(tooltip);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
        HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, variant.capacity());
        tank.readFromNbt(persistent, "tank");
        tooltip.add(HbmFluidGuiHelper.tankInfo(tank, tank.getFill(), tank.getMaxFill())
                .copy()
                .withStyle(ChatFormatting.YELLOW));
    }

    public enum Variant {
        PLASTIC(12_000, "barrel.tooltip.capacity.12000", new String[] {
                "barrel.tooltip.no_hot",
                "barrel.tooltip.no_corrosive",
                "barrel.tooltip.no_antimatter"
        }),
        CORRODED(6_000, "barrel.tooltip.capacity.6000", new String[] {
                "barrel.tooltip.can_hot",
                "barrel.tooltip.can_high_corrosive",
                "barrel.tooltip.no_antimatter",
                "barrel.tooltip.leaky"
        }),
        STEEL(16_000, "barrel.tooltip.capacity.16000", new String[] {
                "barrel.tooltip.can_hot",
                "barrel.tooltip.can_corrosive",
                "barrel.tooltip.no_high_corrosive_properly",
                "barrel.tooltip.no_antimatter"
        }),
        TCALLOY(24_000, "barrel.tooltip.capacity.24000", new String[] {
                "barrel.tooltip.can_hot",
                "barrel.tooltip.can_high_corrosive",
                "barrel.tooltip.no_antimatter"
        }),
        ANTIMATTER(16_000, "barrel.tooltip.capacity.16000", new String[] {
                "barrel.tooltip.can_hot",
                "barrel.tooltip.can_high_corrosive",
                "barrel.tooltip.can_antimatter"
        });

        private final int capacity;
        private final String capacityKey;
        private final String[] tooltipKeys;

        Variant(int capacity, String capacityKey, String[] tooltipKeys) {
            this.capacity = capacity;
            this.capacityKey = capacityKey;
            this.tooltipKeys = tooltipKeys;
        }

        public int capacity() {
            return capacity;
        }

        private void appendTooltip(List<Component> tooltip) {
            tooltip.add(Component.translatable(capacityKey).withStyle(ChatFormatting.AQUA));
            for (String key : tooltipKeys) {
                ChatFormatting color = key.contains("can_") ? ChatFormatting.GREEN
                        : key.contains("leaky") ? ChatFormatting.RED
                        : ChatFormatting.YELLOW;
                tooltip.add(Component.translatable(key).withStyle(color));
            }
        }
    }
}
