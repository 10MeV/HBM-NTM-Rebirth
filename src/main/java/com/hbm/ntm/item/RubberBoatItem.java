package com.hbm.ntm.item;

import com.hbm.ntm.entity.item.RubberBoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RubberBoatItem extends Item {
    public RubberBoatItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 target = eye.add(look.scale(5.0D));
        BlockHitResult hit = level.clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY, player));

        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        List<Entity> candidates = level.getEntities(player,
                player.getBoundingBox().expandTowards(look.scale(5.0D)).inflate(1.0D),
                Entity::canBeCollidedWith);
        for (Entity entity : candidates) {
            AABB bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
            if (bounds.contains(eye)) {
                return InteractionResultHolder.pass(stack);
            }
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos targetPos = hit.getBlockPos();
            BlockState state = level.getBlockState(targetPos);
            if (state.is(Blocks.SNOW)) {
                targetPos = targetPos.relative(Direction.DOWN);
            }

            RubberBoatEntity boat = new RubberBoatEntity(level,
                    targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D);
            boat.setYRot((((int) Math.floor(player.getYRot() * 4.0F / 360.0F + 0.5D) & 3) - 1) * 90.0F);
            boat.yRotO = boat.getYRot();

            if (!level.noCollision(boat, boat.getBoundingBox().deflate(0.1D))) {
                return InteractionResultHolder.pass(stack);
            }

            if (!level.isClientSide) {
                level.addFreshEntity(boat);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return InteractionResultHolder.pass(stack);
    }
}
