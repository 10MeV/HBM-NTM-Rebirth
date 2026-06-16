package com.hbm.ntm.item;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyToolItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private final Toolable.ToolType toolType;

    public LegacyToolItem(Properties properties, Toolable.ToolType toolType) {
        super(properties);
        this.toolType = toolType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (toolType == Toolable.ToolType.WRENCH && !player.isShiftKeyDown()) {
            InteractionResult anchorResult = usePipeAnchorWrench(context);
            if (anchorResult != InteractionResult.PASS) {
                return anchorResult;
            }
        }

        BlockState state = level.getBlockState(pos);
        ToolTarget target = resolveToolTarget(level, pos, state);
        if (target == null) {
            return InteractionResult.PASS;
        }

        Direction side = context.getClickedFace();
        Vec3 hit = context.getClickLocation();
        boolean used = target.toolable().onToolUse(level, player, target.pos(), side, hit, toolType);
        if (!used) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player != null && !player.getAbilities().instabuild) {
            ItemStack stack = context.getItemInHand();
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (toolType == Toolable.ToolType.WRENCH) {
            Vec3 look = attacker.getLookAngle().scale(0.5D);
            target.push(look.x, look.y, look.z);
            target.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS,
                    3.0F, 0.75F);
            return false;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (toolType != Toolable.ToolType.WRENCH || !level.isClientSide || !selected
                || !(entity instanceof Player player) || !hasStoredPipeStart(stack.getTag())) {
            return;
        }
        CompoundTag tag = stack.getTag();
        double dx = entity.getX() - tag.getInt(TAG_X);
        double dy = entity.getY() - tag.getInt(TAG_Y);
        double dz = entity.getZ() - tag.getInt(TAG_Z);
        int distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        player.displayClientMessage(Component.literal(stack.getHoverName().getString() + ": " + distance + "m"), true);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return toolType == Toolable.ToolType.WRENCH || super.doesSneakBypassUse(stack, level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (toolType == Toolable.ToolType.WRENCH) {
            CompoundTag tag = stack.getTag();
            if (hasStoredPipeStart(tag)) {
                tooltip.add(Component.literal("Pipe start x: " + tag.getInt(TAG_X)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Pipe start y: " + tag.getInt(TAG_Y)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Pipe start z: " + tag.getInt(TAG_Z)).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal("Right-click anchor to connect").withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public Toolable.ToolType getToolType() {
        return toolType;
    }

    private static InteractionResult usePipeAnchorWrench(UseOnContext context) {
        Level level = context.getLevel();
        PipeAnchorLookup target = resolvePipeAnchor(level, context.getClickedPos());
        if (target == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        CompoundTag tag = stack.getTag();
        if (!hasStoredPipeStart(tag)) {
            tag = stack.getOrCreateTag();
            tag.putInt(TAG_X, target.pos().getX());
            tag.putInt(TAG_Y, target.pos().getY());
            tag.putInt(TAG_Z, target.pos().getZ());
            player.sendSystemMessage(Component.literal("Pipe start"));
        } else {
            BlockPos startPos = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
            PipeAnchorLookup start = resolvePipeAnchor(level, startPos);
            if (start == null) {
                player.sendSystemMessage(Component.literal("Pipe error"));
            } else {
                FluidPipeAnchorBlockEntity.LinkResult result =
                        FluidPipeAnchorBlockEntity.link(start.anchor(), target.anchor());
                player.sendSystemMessage(Component.literal(messageFor(result)));
            }
            clearStoredPipeStart(stack);
        }

        player.swing(context.getHand(), true);
        return InteractionResult.CONSUME;
    }

    private static ToolTarget resolveToolTarget(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof Toolable toolable) {
            return new ToolTarget(pos, toolable);
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null && core.state().getBlock() instanceof Toolable toolable) {
            return new ToolTarget(core.pos(), toolable);
        }
        return null;
    }

    private static boolean hasStoredPipeStart(@Nullable CompoundTag tag) {
        return tag != null && tag.contains(TAG_X) && tag.contains(TAG_Y) && tag.contains(TAG_Z);
    }

    @Nullable
    private static PipeAnchorLookup resolvePipeAnchor(Level level, BlockPos pos) {
        if (!isLoadedBlock(level, pos)) {
            return null;
        }
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, pos);
        if (!isLoadedBlock(level, corePos)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(corePos);
        if (blockEntity instanceof FluidPipeAnchorBlockEntity anchor) {
            return new PipeAnchorLookup(corePos, anchor);
        }
        return null;
    }

    private static void clearStoredPipeStart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_X);
        tag.remove(TAG_Y);
        tag.remove(TAG_Z);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static boolean isLoadedBlock(Level level, BlockPos pos) {
        return level != null && pos != null && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static String messageFor(FluidPipeAnchorBlockEntity.LinkResult result) {
        return switch (result) {
            case CONNECTED -> "Pipe end";
            case INCOMPATIBLE -> "Pipe error - Pipes are not the same type";
            case SAME_BLOCK -> "Pipe error - Cannot connect to the same pipe anchor";
            case TOO_FAR -> "Pipe error - Pipe anchor is too far away";
            case FLUID_MISMATCH -> "Pipe error - Pipe anchor fluid types do not match";
        };
    }

    private record ToolTarget(BlockPos pos, Toolable toolable) {
    }

    private record PipeAnchorLookup(BlockPos pos, FluidPipeAnchorBlockEntity anchor) {
    }
}
