package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.NeutronNode;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RBMKColumnBlock extends BaseEntityBlock implements Toolable, MultiblockCoreBlock, LegacyMultiblockPlaceable {
    public static final EnumProperty<LidType> LID = EnumProperty.create("lid", LidType.class);

    private static final VoxelShape NO_LID_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final VoxelShape LID_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.25D, 1.0D);

    private final Kind kind;

    public RBMKColumnBlock(BlockBehaviour.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind == null ? Kind.BLANK : kind;
        registerDefaultState(stateDefinition.any().setValue(LID, LidType.NONE));
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = getDirectPlacementState(context);
        if (state == null) {
            return null;
        }
        BlockPos corePos = getDirectPlacementCore(context, state);
        return canPlaceDirectMultiblock(context.getLevel(), corePos, context.getClickedPos(), state) ? state : null;
    }

    @Nullable
    @Override
    public BlockState getDirectPlacementState(BlockPlaceContext context) {
        return defaultBlockState().setValue(LID, LidType.NONE);
    }

    @Override
    public BlockPos getDirectPlacementCore(BlockPlaceContext context, BlockState state) {
        return context.getClickedPos();
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos, defaultBlockState());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return MultiblockHelper.checkLayout(level, corePos, layoutForCurrentHeight(state), temporaryPos);
    }

    @Override
    public void afterDirectCorePlaced(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, corePos, state, placer, stack);
    }

    @Override
    public void completeDirectMultiblockPlacement(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        fillColumnLayout(level, corePos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        fillColumnLayout(level, pos, state);
    }

    @Nullable
    @Override
    public LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return layoutForCurrentHeight(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LID).hasLid() ? LID_SHAPE : NO_LID_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (kind.rod() && held.getItem() instanceof RBMKFuelRodItem) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof RBMKColumnBlockEntity column) || column.hasFuelRod()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && column.loadFuelRod(held)) {
                LegacySoundPlayer.playLegacyUpgradePlug(level, pos, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        LidType lid = lidForStack(held);
        if (kind.storage() && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                    && level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity column) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKStorageMenu(containerId, inventory, column),
                        Component.translatable("container.rbmkStorage")), pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (kind == Kind.OUTGASSER && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                    && level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity column) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKOutgasserMenu(containerId, inventory, column),
                        Component.translatable("container.rbmkOutgasser")), pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (lid == LidType.NONE) {
            return InteractionResult.PASS;
        }
        if (state.getValue(LID).hasLid()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            setLid(level, pos, state, lid);
            level.playSound(null, pos, lid == LidType.GLASS ? SoundType.GLASS.getPlaceSound()
                    : SoundType.STONE.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RBMKColumnBlock) || !state.getValue(LID).hasLid()) {
            return false;
        }
        if (!level.isClientSide) {
            LidType lid = state.getValue(LID);
            setLid(level, pos, state, LidType.NONE);
            ItemStack drop = new ItemStack(lid == LidType.GLASS ? ModItems.RBMK_LID_GLASS.get() : ModItems.RBMK_LID.get());
            BlockPos dropPos = pos.above(columnHeightAbove());
            HbmItemStackUtil.dropStack(level, dropPos, drop);
        }
        return true;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.getValue(LID).hasLid() && !player.getAbilities().instabuild) {
            LidType lid = state.getValue(LID);
            popResource(level, pos, new ItemStack(lid == LidType.GLASS ? ModItems.RBMK_LID_GLASS.get() : ModItems.RBMK_LID.get()));
        }
        if (!level.isClientSide && state.getBlock() instanceof RBMKColumnBlock column && column.kind().rod()
                && level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity blockEntity) {
            ItemStack rod = blockEntity.removeFuelRodForDrop();
            if (!rod.isEmpty()) {
                popResource(level, pos, rod);
            }
        }
        if (!level.isClientSide && state.getBlock() instanceof RBMKColumnBlock column && column.kind().storage()
                && level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity blockEntity
                && !player.getAbilities().instabuild) {
            for (ItemStack stack : blockEntity.removeStorageForDrop()) {
                if (!stack.isEmpty()) {
                    popResource(level, pos, stack);
                }
            }
        }
        if (!level.isClientSide && state.getBlock() instanceof RBMKColumnBlock column
                && column.kind() == Kind.OUTGASSER
                && level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity blockEntity
                && !player.getAbilities().instabuild) {
            for (ItemStack stack : blockEntity.removeOutgasserItemsForDrop()) {
                if (!stack.isEmpty()) {
                    popResource(level, pos, stack);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeLayout(level, pos, layoutForCurrentHeight(state));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private LegacyMultiblockLayout layoutForCurrentHeight(BlockState state) {
        int heightAbove = columnHeightAbove();
        List<BlockPos> offsets = new ArrayList<>(heightAbove + 1);
        offsets.add(BlockPos.ZERO);
        for (int y = 1; y <= heightAbove; y++) {
            offsets.add(new BlockPos(0, y, 0));
        }
        List<BlockPos> legacyExtraOffsets = heightAbove <= 0 ? List.of() : List.of(new BlockPos(0, heightAbove, 0));
        return LegacyMultiblockLayout.ofOffsets(offsets)
                .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyMode())
                .withLegacyExtraOffsets(legacyExtraOffsets);
    }

    private void fillColumnLayout(Level level, BlockPos corePos, BlockState state) {
        if (!level.isClientSide) {
            MultiblockHelper.fillLayout(level, corePos, layoutForCurrentHeight(state));
        }
    }

    private LegacyProxyMode proxyMode() {
        if (kind.storage()) {
            return LegacyProxyMode.passive().withInventory(true);
        }
        if (kind == Kind.BOILER || kind == Kind.HEATER || kind == Kind.COOLER) {
            return LegacyProxyMode.passive().withFluid(true);
        }
        if (kind == Kind.OUTGASSER) {
            return LegacyProxyMode.passive().withInventory(true).withFluid(true);
        }
        if (kind.rod()) {
            return LegacyProxyMode.passive().withInventory(true);
        }
        if (kind == Kind.CONTROL || kind == Kind.CONTROL_MOD || kind == Kind.CONTROL_REASIM) {
            return LegacyProxyMode.passive();
        }
        return LegacyProxyMode.none();
    }

    private static int columnHeightAbove() {
        return Math.max(0, RBMKNeutronHandler.settings().columnHeight() - 1);
    }

    private static LidType lidForStack(ItemStack stack) {
        if (stack.is(ModItems.RBMK_LID.get())) {
            return LidType.STANDARD;
        }
        if (stack.is(ModItems.RBMK_LID_GLASS.get())) {
            return LidType.GLASS;
        }
        return LidType.NONE;
    }

    private static void setLid(Level level, BlockPos pos, BlockState state, LidType lid) {
        level.setBlock(pos, state.setValue(LID, lid), Block.UPDATE_ALL);
        NeutronNode node = NeutronNodeWorld.getNode(level, pos);
        if (node instanceof RBMKNeutronHandler.RBMKNeutronNode rbmkNode) {
            if (lid.hasLid()) {
                rbmkNode.addLid();
            } else {
                rbmkNode.removeLid();
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RBMKColumnBlockEntity.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.RBMK_COLUMN.get(), RBMKColumnBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LID);
    }

    public enum LidType implements StringRepresentable {
        NONE("none"),
        STANDARD("standard"),
        GLASS("glass");

        private final String serializedName;

        LidType(String serializedName) {
            this.serializedName = serializedName;
        }

        public boolean hasLid() {
            return this != NONE;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    public enum Kind {
        BLANK(RBMKNeutronHandler.RBMKType.OTHER, false, false, false, false, false),
        MODERATOR(RBMKNeutronHandler.RBMKType.MODERATOR, false, false, false, false, false),
        REFLECTOR(RBMKNeutronHandler.RBMKType.REFLECTOR, false, false, false, false, false),
        ABSORBER(RBMKNeutronHandler.RBMKType.ABSORBER, false, false, false, false, false),
        ROD(RBMKNeutronHandler.RBMKType.ROD, false, false, false, true, false),
        ROD_MOD(RBMKNeutronHandler.RBMKType.ROD, true, false, false, true, false),
        ROD_REASIM(RBMKNeutronHandler.RBMKType.ROD, false, false, false, true, true),
        ROD_REASIM_MOD(RBMKNeutronHandler.RBMKType.ROD, true, false, false, true, true),
        BOILER(RBMKNeutronHandler.RBMKType.OTHER, false, false, false, false, false),
        HEATER(RBMKNeutronHandler.RBMKType.OTHER, false, false, false, false, false),
        COOLER(RBMKNeutronHandler.RBMKType.OTHER, false, false, false, false, false),
        OUTGASSER(RBMKNeutronHandler.RBMKType.OUTGASSER, false, false, false, false, false),
        STORAGE(RBMKNeutronHandler.RBMKType.OTHER, false, false, false, false, false, true),
        CONTROL(RBMKNeutronHandler.RBMKType.CONTROL_ROD, false, false, false, false, false),
        CONTROL_MOD(RBMKNeutronHandler.RBMKType.CONTROL_ROD, true, false, false, false, false),
        CONTROL_AUTO(RBMKNeutronHandler.RBMKType.CONTROL_ROD, false, true, false, false, false),
        CONTROL_REASIM(RBMKNeutronHandler.RBMKType.CONTROL_ROD, false, false, true, false, false),
        CONTROL_REASIM_AUTO(RBMKNeutronHandler.RBMKType.CONTROL_ROD, false, true, true, false, false);

        private final RBMKNeutronHandler.RBMKType rbmkType;
        private final boolean moderated;
        private final boolean automatic;
        private final boolean powered;
        private final boolean rod;
        private final boolean reasim;
        private final boolean storage;

        Kind(RBMKNeutronHandler.RBMKType rbmkType, boolean moderated, boolean automatic, boolean powered,
                boolean rod, boolean reasim) {
            this(rbmkType, moderated, automatic, powered, rod, reasim, false);
        }

        Kind(RBMKNeutronHandler.RBMKType rbmkType, boolean moderated, boolean automatic, boolean powered,
                boolean rod, boolean reasim, boolean storage) {
            this.rbmkType = rbmkType;
            this.moderated = moderated;
            this.automatic = automatic;
            this.powered = powered;
            this.rod = rod;
            this.reasim = reasim;
            this.storage = storage;
        }

        public RBMKNeutronHandler.RBMKType rbmkType() {
            return rbmkType;
        }

        public boolean moderated() {
            return moderated;
        }

        public boolean automatic() {
            return automatic;
        }

        public boolean powered() {
            return powered;
        }

        public boolean control() {
            return rbmkType == RBMKNeutronHandler.RBMKType.CONTROL_ROD;
        }

        public boolean rod() {
            return rod;
        }

        public boolean reasim() {
            return reasim;
        }

        public boolean storage() {
            return storage;
        }
    }
}
