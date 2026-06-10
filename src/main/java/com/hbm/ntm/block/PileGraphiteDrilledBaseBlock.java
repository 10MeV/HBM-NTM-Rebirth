package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.PileBreedingFuelBlockEntity;
import com.hbm.ntm.blockentity.PileFuelBlockEntity;
import com.hbm.ntm.blockentity.PileNeutronDetectorBlockEntity;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.neutron.PileGraphiteInteractionPlanner;
import com.hbm.ntm.neutron.PileGraphiteMetadata;
import com.hbm.ntm.neutron.PileGraphiteNeutronRules;
import com.hbm.ntm.neutron.PileGraphiteTogglePlanner;
import com.hbm.ntm.neutron.PileNeutronBlockBehavior;
import com.hbm.ntm.neutron.PileNeutronBlockResult;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class PileGraphiteDrilledBaseBlock extends BaseEntityBlock
        implements Toolable, PileNeutronBlockBehavior {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ALUMINUM = BooleanProperty.create("aluminum");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind;

    public PileGraphiteDrilledBaseBlock(
            Properties properties,
            PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind) {
        super(properties);
        this.graphiteKind = graphiteKind;
        registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(ALUMINUM, false)
                .setValue(ACTIVE, false));
    }

    public PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind() {
        return graphiteKind;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withLegacyMeta(defaultBlockState(), PileGraphiteMetadata.orientationForSide(context.getClickedFace()));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        PileGraphiteBlockEntityPlanner.ToolAction action = toolAction(tool);
        if (action == null) {
            return false;
        }
        int meta = legacyMeta(level.getBlockState(pos));
        PileGraphiteBlockEntityPlanner.ToolUseExecutionPlan plan =
                PileGraphiteBlockEntityPlanner.planToolUse(
                        pos,
                        graphiteKind,
                        meta,
                        side,
                        action,
                        player.isShiftKeyDown(),
                        fuelState(level, pos),
                        detectorState(level, pos));
        if (plan.interaction() == null || !plan.interaction().accepted()) {
            return false;
        }
        if (!level.isClientSide) {
            executeInteraction(level, player, plan.interaction());
            if (plan.detectorThresholdPlan() != null
                    && level.getBlockEntity(pos) instanceof PileNeutronDetectorBlockEntity detector) {
                detector.setMaxNeutrons(plan.detectorThresholdPlan().newThreshold());
            }
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool != null && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        PileGraphiteInteractionPlanner.HeldItem held = heldItem(player.getItemInHand(hand));
        if (held == PileGraphiteInteractionPlanner.HeldItem.NONE) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean accepted = held.insertedItem() == PileGraphiteInsertionPlanner.InsertedItem.NONE
                ? executeSimpleActivation(level, player, pos, hit.getDirection(), held)
                : executeInsertion(level, player, pos, hit.getDirection(), held.insertedItem());
        return accepted ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public PileNeutronBlockResult evaluatePileNeutronBlock(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity) {
        if (graphiteKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.ROD) {
            return PileGraphiteNeutronRules.boronRodResult(legacyMeta(state));
        }
        return PileNeutronBlockResult.pass();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return graphiteKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.FUEL;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PileFuelBlockEntity fuel) {
            return PileGraphiteBlockEntityPlanner.comparatorSignalForFuel(fuel.fuelState(), legacyMeta(state));
        }
        return 0;
    }

    public static int legacyMeta(BlockState state) {
        int meta = switch (state.getValue(AXIS)) {
            case Y -> 0;
            case Z -> 1;
            case X -> 2;
        };
        if (state.getValue(ALUMINUM)) {
            meta |= PileGraphiteMetadata.ALUMINUM_MASK;
        }
        if (state.getValue(ACTIVE)) {
            meta |= PileGraphiteMetadata.ACTIVE_MASK;
        }
        return meta;
    }

    public static BlockState withLegacyMeta(BlockState state, int meta) {
        return state
                .setValue(AXIS, PileGraphiteMetadata.axis(meta))
                .setValue(ALUMINUM, PileGraphiteMetadata.hasAluminum(meta))
                .setValue(ACTIVE, PileGraphiteMetadata.isActive(meta));
    }

    protected static void executeInteraction(
            Level level,
            @Nullable Player player,
            PileGraphiteInteractionPlanner.InteractionPlan plan) {
        for (PileGraphiteInteractionPlanner.BlockMutation mutation : plan.blockMutations()) {
            setLegacyBlock(level, mutation.pos(), mutation.legacyBlockId(), mutation.newMeta(), null);
        }
        for (PileGraphiteInteractionPlanner.ItemEjection ejection : plan.ejections()) {
            ejectLegacyItem(level, ejection.sourcePos(), ejection.direction(), ejection.stack());
        }
        for (PileGraphiteInteractionPlanner.SoundCue sound : plan.sounds()) {
            playPlannedSound(level, sound.legacySoundId(), sound.pos(), sound.volume(), sound.pitch());
        }
        if (plan.consumeHeldItem() && player != null && !player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
    }

    public static void setLegacyBlock(
            Level level,
            BlockPos pos,
            String legacyBlockId,
            int meta,
            @Nullable CompoundTag copiedBlockEntityTag) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyBlockId);
        if (block == null || !block.isPresent()) {
            return;
        }
        BlockState newState = block.get().defaultBlockState();
        if (newState.hasProperty(AXIS)) {
            newState = withLegacyMeta(newState, meta);
        }
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (copiedBlockEntityTag != null && blockEntity != null) {
            blockEntity.load(copiedBlockEntityTag);
            blockEntity.setChanged();
        }
    }

    protected static PileGraphiteInsertionPlanner.ColumnState columnState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PileGraphiteDrilledBaseBlock graphite) {
            return PileGraphiteInsertionPlanner.ColumnState.graphite(graphite.graphiteKind(), legacyMeta(state));
        }
        return state.isSolidRender(level, pos)
                ? PileGraphiteInsertionPlanner.ColumnState.solid()
                : PileGraphiteInsertionPlanner.ColumnState.empty();
    }

    public static PileGraphiteTogglePlanner.ChainState chainState(
            Level level,
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind expectedKind) {
        BlockState state = level.getBlockState(pos);
        boolean same = state.getBlock() instanceof PileGraphiteDrilledBaseBlock graphite
                && graphite.graphiteKind() == expectedKind;
        return new PileGraphiteTogglePlanner.ChainState(same, same ? legacyMeta(state) : 0);
    }

    private boolean executeSimpleActivation(
            Level level,
            Player player,
            BlockPos pos,
            Direction side,
            PileGraphiteInteractionPlanner.HeldItem held) {
        PileGraphiteInteractionPlanner.InteractionPlan plan =
                PileGraphiteInteractionPlanner.planDrilledBlockActivation(pos, legacyMeta(level.getBlockState(pos)), side, held);
        if (!plan.accepted()) {
            return false;
        }
        executeInteraction(level, player, plan);
        return plan.hasWorldMutation();
    }

    private boolean executeInsertion(
            Level level,
            Player player,
            BlockPos pos,
            Direction direction,
            PileGraphiteInsertionPlanner.InsertedItem insertedItem) {
        PileGraphiteInsertionPlanner.InsertionPlan plan =
                PileGraphiteInsertionPlanner.planInsertion(pos, direction, insertedItem, probe -> columnState(level, probe));
        if (!plan.accepted()) {
            return false;
        }

        Map<BlockPos, CompoundTag> copiedTags = new HashMap<>();
        for (PileGraphiteInsertionPlanner.BlockMutation mutation : plan.mutations()) {
            if (mutation.tileDataAction().mode() == PileGraphiteInsertionPlanner.TileDataMode.COPY_FROM
                    && mutation.tileDataAction().sourcePos() != null
                    && level.getBlockEntity(mutation.tileDataAction().sourcePos()) != null) {
                BlockEntity source = level.getBlockEntity(mutation.tileDataAction().sourcePos());
                copiedTags.put(mutation.pos(), source.saveWithoutMetadata());
            }
        }
        for (PileGraphiteInsertionPlanner.BlockMutation mutation : plan.mutations()) {
            setLegacyBlock(level, mutation.pos(), mutation.newBlock().legacyBlockId(), mutation.newMeta(),
                    copiedTags.get(mutation.pos()));
        }
        if (plan.hasEjection()) {
            ejectLegacyItem(level, plan.ejection().sourcePos(), plan.ejection().direction(),
                    PileGraphiteInteractionPlanner.LegacyItemStack.insertedItem(plan.ejection().item()));
        }
        if (!player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
        playPlannedSound(level, PileGraphiteInteractionPlanner.LEGACY_SOUND_UPGRADE_PLUG, pos, 1.0F, 1.0F);
        return true;
    }

    private static void ejectLegacyItem(
            Level level,
            BlockPos pos,
            Direction direction,
            PileGraphiteInteractionPlanner.LegacyItemStack stack) {
        RegistryObject<Item> item = ModItems.legacyItem(stack.legacyItemId());
        if (item == null || !item.isPresent()) {
            return;
        }
        double x = pos.getX() + 0.5D + direction.getStepX() * 0.35D;
        double y = pos.getY() + 0.5D + direction.getStepY() * 0.35D;
        double z = pos.getZ() + 0.5D + direction.getStepZ() * 0.35D;
        ItemEntity entity = new ItemEntity(level, x, y, z, new ItemStack(item.get(), stack.count()));
        entity.setDeltaMovement(direction.getStepX() * 0.08D, direction.getStepY() * 0.08D, direction.getStepZ() * 0.08D);
        level.addFreshEntity(entity);
    }

    private static void playPlannedSound(
            Level level,
            String legacySoundId,
            BlockPos pos,
            float volume,
            float pitch) {
        SoundEvent sound = switch (legacySoundId) {
            case PileGraphiteInteractionPlanner.LEGACY_SOUND_UPGRADE_PLUG -> ModSounds.ITEM_UPGRADE_PLUG.get();
            case PileGraphiteInteractionPlanner.LEGACY_SOUND_TECH_BLEEP -> ModSounds.ITEM_TECH_BLEEP.get();
            default -> null;
        };
        if (sound != null) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    private static PileGraphiteBlockEntityPlanner.ToolAction toolAction(ToolType tool) {
        return switch (tool) {
            case HAND_DRILL -> PileGraphiteBlockEntityPlanner.ToolAction.HAND_DRILL;
            case SCREWDRIVER -> PileGraphiteBlockEntityPlanner.ToolAction.SCREWDRIVER;
            case DEFUSER -> PileGraphiteBlockEntityPlanner.ToolAction.DEFUSER;
            default -> null;
        };
    }

    private static PileGraphiteInteractionPlanner.HeldItem heldItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return PileGraphiteInteractionPlanner.HeldItem.NONE;
        }
        for (PileGraphiteInteractionPlanner.HeldItem held : PileGraphiteInteractionPlanner.HeldItem.values()) {
            if (held == PileGraphiteInteractionPlanner.HeldItem.NONE
                    || held == PileGraphiteInteractionPlanner.HeldItem.ALUMINUM_SHELL) {
                continue;
            }
            String legacyItem = held == PileGraphiteInteractionPlanner.HeldItem.GRAPHITE_INGOT
                    ? "ingot_graphite"
                    : held.insertedItem().legacyItemId();
            RegistryObject<Item> item = ModItems.legacyItem(legacyItem);
            if (item != null && item.isPresent() && stack.is(item.get())) {
                return held;
            }
        }
        return PileGraphiteInteractionPlanner.HeldItem.NONE;
    }

    @Nullable
    private static com.hbm.ntm.neutron.PileFuelState fuelState(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PileFuelBlockEntity fuel) {
            return fuel.fuelState();
        }
        if (level.getBlockEntity(pos) instanceof PileBreedingFuelBlockEntity breedingFuel) {
            return breedingFuel.fuelState();
        }
        return null;
    }

    @Nullable
    private static com.hbm.ntm.neutron.PileNeutronDetectorState detectorState(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PileNeutronDetectorBlockEntity detector
                ? detector.detectorState()
                : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, ALUMINUM, ACTIVE);
    }
}
