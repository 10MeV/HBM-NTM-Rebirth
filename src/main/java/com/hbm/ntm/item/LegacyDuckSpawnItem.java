package com.hbm.ntm.item;

import com.hbm.ntm.entity.mob.EntityDuck;
import com.hbm.ntm.registry.ModEntityTypes;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class LegacyDuckSpawnItem extends Item {
    public LegacyDuckSpawnItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        double x = context.getClickedPos().getX() + context.getClickedFace().getStepX() + 0.5D;
        double y = context.getClickedPos().getY() + context.getClickedFace().getStepY();
        double z = context.getClickedPos().getZ() + context.getClickedFace().getStepZ() + 0.5D;
        return spawnDuck(serverLevel, stack, context.getPlayer(), x, y, z)
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(level.getBlockState(hit.getBlockPos()).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        boolean spawned = spawnDuck(serverLevel, stack, player,
                hit.getBlockPos().getX(),
                hit.getBlockPos().getY(),
                hit.getBlockPos().getZ());
        return spawned ? InteractionResultHolder.consume(stack) : InteractionResultHolder.pass(stack);
    }

    private static boolean spawnDuck(ServerLevel level, ItemStack stack, @Nullable Player player,
            double x, double y, double z) {
        EntityDuck duck = ModEntityTypes.DUCK.get().create(level);
        if (duck == null) {
            return false;
        }
        float yaw = Mth.wrapDegrees(level.random.nextFloat() * 360.0F);
        duck.moveTo(x, y, z, yaw, 0.0F);
        duck.setYHeadRot(duck.getYRot());
        duck.yBodyRot = duck.getYRot();
        duck.finalizeSpawn(level, level.getCurrentDifficultyAt(duck.blockPosition()), MobSpawnType.SPAWN_EGG,
                null, null);
        if (stack.hasCustomHoverName()) {
            duck.setCustomName(stack.getHoverName());
        }
        if (!level.addFreshEntity(duck)) {
            return false;
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }
}
