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
import com.hbm.ntm.neutron.RBMKBaseRuntimePlanner;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
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

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            fillColumnLayout(level, pos, state);
        }
    }

    @Nullable
    @Override
    public LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return layoutForCurrentHeight(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return localColumnSegmentShape(state, BlockPos.ZERO);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return localColumnCollisionShape(state, BlockPos.ZERO);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        int heightAbove = columnHeightAbove();
        for (int y = 0; y <= heightAbove; y++) {
            double segmentHeight = y == heightAbove && state.getValue(LID).hasLid() ? 1.25D : 1.0D;
            shape = Shapes.or(shape, Shapes.box(0.0D, y, 0.0D, 1.0D, y + segmentHeight, 1.0D));
        }
        return shape.optimize();
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean requiresCompleteOperationalLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localColumnSegmentShape(state, dummyPos.subtract(corePos));
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localColumnSegmentShape(state, dummyPos.subtract(corePos));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        CoreColumnLookup core = resolveOperationalColumn(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        Kind coreKind = core.block().kind();
        if (coreKind.rod() && held.getItem() instanceof RBMKFuelRodItem) {
            if (core.entity().hasFuelRod()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && core.entity().loadFuelRod(held)) {
                LegacySoundPlayer.playLegacyUpgradePlug(level, core.pos(), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        LidType lid = lidForStack(held);
        if (coreKind.storage() && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKStorageMenu(containerId, inventory, core.entity()),
                        Component.translatable("container.rbmkStorage")), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (coreKind.rod() && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                String key = coreKind.reasim() ? "container.rbmkReaSim" : "container.rbmkRod";
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKRodMenu(containerId, inventory, core.entity()),
                        Component.translatable(key)), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (coreKind.control() && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                String key = coreKind.automatic() ? "container.rbmkControlAuto" : "container.rbmkControl";
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) -> coreKind.automatic()
                                ? new com.hbm.ntm.menu.RBMKControlAutoMenu(containerId, inventory, core.entity())
                                : new com.hbm.ntm.menu.RBMKControlMenu(containerId, inventory, core.entity()),
                        Component.translatable(key)), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (coreKind == Kind.HEATER && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKHeaterMenu(containerId, inventory, core.entity()),
                        Component.translatable("container.rbmkHeater")), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (coreKind == Kind.BOILER && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKBoilerMenu(containerId, inventory, core.entity()),
                        Component.translatable("container.rbmkBoiler")), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (coreKind == Kind.OUTGASSER && lid == LidType.NONE) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) ->
                                new com.hbm.ntm.menu.RBMKOutgasserMenu(containerId, inventory, core.entity()),
                        Component.translatable("container.rbmkOutgasser")), core.pos());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (lid == LidType.NONE) {
            return InteractionResult.PASS;
        }
        if (core.state().getValue(LID).hasLid()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            setLid(level, core.pos(), core.state(), lid);
            level.playSound(null, core.pos(), lid == LidType.GLASS ? SoundType.GLASS.getPlaceSound()
                    : SoundType.STONE.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool == ToolType.HAND_DRILL) {
            CoreColumnLookup core = resolveOperationalColumn(level, pos);
            if (core == null) {
                return false;
            }
            if (!level.isClientSide) {
                sendDiagnostics(player, core);
            }
            return true;
        }
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null) {
            return false;
        }
        BlockPos corePos = core.pos();
        boolean operational = level.isClientSide
                ? MultiblockHelper.isOperationalCoreLayoutComplete(level, corePos)
                : MultiblockHelper.ensureOperationalCoreLayoutComplete(level, corePos);
        if (!operational) {
            return false;
        }
        BlockState state = core.state();
        if (!(state.getBlock() instanceof RBMKColumnBlock) || !state.getValue(LID).hasLid()) {
            return false;
        }
        if (!level.isClientSide) {
            LidType lid = state.getValue(LID);
            setLid(level, corePos, state, LidType.NONE);
            ItemStack drop = new ItemStack(lid == LidType.GLASS ? ModItems.RBMK_LID_GLASS.get() : ModItems.RBMK_LID.get());
            BlockPos dropPos = RBMKStructureDimensions.columnTop(corePos);
            HbmItemStackUtil.dropStack(level, dropPos, drop);
        }
        return true;
    }

    private static void sendDiagnostics(Player player, CoreColumnLookup core) {
        player.sendSystemMessage(Component.literal("RBMK COLUMN "
                + core.pos().getX() + " " + core.pos().getY() + " " + core.pos().getZ())
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("TYPE: " + core.block().kind().name())
                .withStyle(ChatFormatting.YELLOW));
        for (RBMKBaseRuntimePlanner.DiagnosticEntry entry :
                RBMKBaseRuntimePlanner.diagnosticEntries(core.entity().diagnosticData())) {
            player.sendSystemMessage(Component.literal(entry.key() + ": " + entry.legacyValue())
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            CoreColumnLookup core = resolveOwnedColumn(level, pos);
            if (core != null) {
                dropCoreContents(level, core.pos(), core.state(), core.block(), core.entity());
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void beforeMultiblockDummyDestroysCore(Level level, BlockPos corePos, BlockState coreState,
            BlockPos dummyPos, boolean drop) {
        if (drop && !level.isClientSide
                && coreState.getBlock() instanceof RBMKColumnBlock column
                && level.getBlockEntity(corePos) instanceof RBMKColumnBlockEntity blockEntity) {
            dropCoreContents(level, corePos, coreState, column, blockEntity);
        }
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
                .withProxyModes(offset -> proxyMode(offset, heightAbove))
                .withLegacyExtraOffsets(legacyExtraOffsets);
    }

    private void fillColumnLayout(Level level, BlockPos corePos, BlockState state) {
        if (!level.isClientSide) {
            LegacyMultiblockLayout layout = layoutForCurrentHeight(state);
            boolean filled = MultiblockHelper.fillLayout(level, corePos, layout);
            if (!filled || !MultiblockHelper.isLayoutComplete(level, corePos, layout)) {
                level.destroyBlock(corePos, false);
            }
        }
    }

    private LegacyProxyMode proxyMode(BlockPos offset, int heightAbove) {
        if (offset.equals(BlockPos.ZERO)) {
            return LegacyProxyMode.none();
        }
        if (kind.storage()) {
            return LegacyProxyMode.passive().withInventory(true);
        }
        boolean topExtra = heightAbove > 0 && offset.getX() == 0 && offset.getY() == heightAbove
                && offset.getZ() == 0;
        if (!topExtra) {
            return LegacyProxyMode.none();
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
        return RBMKStructureDimensions.columnHeightAboveCore();
    }

    private static VoxelShape localColumnCollisionShape(BlockState state, BlockPos offset) {
        return localColumnSegmentShape(state, offset);
    }

    private static VoxelShape localColumnSegmentShape(BlockState state, BlockPos offset) {
        boolean topSegment = offset.getX() == 0 && offset.getZ() == 0 && offset.getY() == columnHeightAbove();
        double height = topSegment && state.hasProperty(LID) && state.getValue(LID).hasLid() ? 1.25D : 1.0D;
        return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
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
    private static CoreColumnLookup resolveOperationalColumn(Level level, BlockPos pos) {
        CoreColumnLookup core = resolveOwnedColumn(level, pos);
        if (core == null) {
            return null;
        }
        if (level.isClientSide) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, core.pos()) ? core : null;
        }
        return MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos()) ? core : null;
    }

    @Nullable
    private static CoreColumnLookup resolveOwnedColumn(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null || !(core.state().getBlock() instanceof RBMKColumnBlock column)
                || !(level.getBlockEntity(core.pos()) instanceof RBMKColumnBlockEntity blockEntity)) {
            return null;
        }
        return new CoreColumnLookup(core.pos(), core.state(), column, blockEntity);
    }

    private static void dropCoreContents(Level level, BlockPos pos, BlockState state, RBMKColumnBlock column,
            RBMKColumnBlockEntity blockEntity) {
        if (state.getValue(LID).hasLid()) {
            LidType lid = state.getValue(LID);
            popResource(level, RBMKStructureDimensions.columnTop(pos),
                    new ItemStack(lid == LidType.GLASS ? ModItems.RBMK_LID_GLASS.get() : ModItems.RBMK_LID.get()));
        }
        if (column.kind().rod()) {
            ItemStack rod = blockEntity.removeFuelRodForDrop();
            if (!rod.isEmpty()) {
                popResource(level, pos, rod);
            }
        }
        if (column.kind().storage()) {
            for (ItemStack stack : blockEntity.removeStorageForDrop()) {
                if (!stack.isEmpty()) {
                    popResource(level, pos, stack);
                }
            }
        }
        if (column.kind() == Kind.OUTGASSER) {
            for (ItemStack stack : blockEntity.removeOutgasserItemsForDrop()) {
                if (!stack.isEmpty()) {
                    popResource(level, pos, stack);
                }
            }
        }
        if (column.kind() == Kind.HEATER) {
            for (ItemStack stack : blockEntity.removeHeaterItemsForDrop()) {
                if (!stack.isEmpty()) {
                    popResource(level, pos, stack);
                }
            }
        }
    }

    private record CoreColumnLookup(BlockPos pos, BlockState state, RBMKColumnBlock block,
                                    RBMKColumnBlockEntity entity) {
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
