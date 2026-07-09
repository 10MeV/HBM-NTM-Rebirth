package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HeatBoilerBlock extends LegacyVisibleMultiblockMachineBlock {
    public static final EnumProperty<BoilerVisualState> VISUAL =
            EnumProperty.create("visual", BoilerVisualState.class);
    private static final double OVERPRESSURE_THRESHOLD = 0.9D;

    public HeatBoilerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
        registerDefaultState(defaultBlockState().setValue(VISUAL, BoilerVisualState.NORMAL));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VISUAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && !player.isShiftKeyDown()
                && held.getItem() instanceof IFluidIdentifierItem identifier
                && resolveCoreBlockEntity(level, pos) instanceof BoilerBlockEntity boiler) {
            FluidType type = identifier.getIdentifiedFluid(level, boiler.getBlockPos(), held);
            if (isBoilerHeatable(type)) {
                boiler.setFeedTankType(type);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.BOILER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        BoilerBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (BoilerBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        BoilerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (BoilerBlockEntity) blockEntity);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BoilerBlockEntity boiler
                && boiler.hasExploded()) {
            return List.of(
                    new ItemStack(ModItems.STEEL_INGOT.get(), 4),
                    new ItemStack(ModItems.COPPER_PLATE.get(), 8));
        }
        return super.getDrops(state, builder);
    }

    private static boolean isBoilerHeatable(FluidType type) {
        if (type == null) {
            return false;
        }
        HeatableFluidTrait trait = type.getTrait(HeatableFluidTrait.class);
        return trait != null && trait.getEfficiency(HeatingType.BOILER) > 0.0D;
    }

    public static BoilerVisualState visualState(boolean hasExploded, int steamFill, int steamMaxFill) {
        if (hasExploded) {
            return BoilerVisualState.EXPLODED;
        }
        return steamMaxFill > 0 && steamFill > steamMaxFill * OVERPRESSURE_THRESHOLD
                ? BoilerVisualState.OVERPRESSURE
                : BoilerVisualState.NORMAL;
    }

    public enum BoilerVisualState implements StringRepresentable {
        NORMAL("normal"),
        OVERPRESSURE("overpressure"),
        EXPLODED("exploded");

        private final String serializedName;

        BoilerVisualState(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
