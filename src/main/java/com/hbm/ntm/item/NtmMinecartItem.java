package com.hbm.ntm.item;

import com.hbm.ntm.entity.cart.NtmMinecartBase;
import com.hbm.ntm.entity.cart.NtmCrateMinecartEntity;
import com.hbm.ntm.entity.cart.NtmDestroyerMinecartEntity;
import com.hbm.ntm.entity.cart.NtmMinecartEntity;
import com.hbm.ntm.entity.cart.NtmMinecartType;
import com.hbm.ntm.entity.cart.NtmEmptyMinecartEntity;
import com.hbm.ntm.entity.cart.NtmPowderMinecartEntity;
import com.hbm.ntm.entity.cart.NtmSemtexMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class NtmMinecartItem extends Item {
    private static final DefaultDispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            if (!(stack.getItem() instanceof NtmMinecartItem minecartItem)) {
                return defaultBehavior.dispense(source, stack);
            }

            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            Level level = source.getLevel();
            double x = source.x() + direction.getStepX() * 1.125D;
            double y = source.y() + direction.getStepY() * 1.125D;
            double z = source.z() + direction.getStepZ() * 1.125D;
            BlockPos targetPos = source.getPos().relative(direction);
            BlockState targetState = level.getBlockState(targetPos);
            double yOffset;

            if (targetState.is(BlockTags.RAILS)) {
                yOffset = 0.0D;
            } else {
                if (!targetState.isAir() || !level.getBlockState(targetPos.below()).is(BlockTags.RAILS)) {
                    return defaultBehavior.dispense(source, stack);
                }
                yOffset = -1.0D;
            }

            NtmMinecartEntity cart = minecartItem.createMinecart(level, x, y + yOffset, z, stack);
            level.addFreshEntity(cart);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.getLevel().levelEvent(1000, source.getPos(), 0);
        }
    };

    private final NtmMinecartBase base;
    private final NtmMinecartType type;

    public NtmMinecartItem(NtmMinecartBase base, NtmMinecartType type, Properties properties) {
        super(properties.stacksTo(4));
        this.base = base;
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    public NtmMinecartItem(NtmMinecartBase base, Properties properties) {
        this(base, NtmMinecartType.EMPTY, properties);
    }

    public NtmMinecartBase base() {
        return base;
    }

    public NtmMinecartType cartType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            Vec3 click = context.getClickLocation();
            NtmMinecartEntity cart = createMinecart(level, click.x, click.y, click.z, stack);
            level.addFreshEntity(cart);
            level.gameEvent(GameEvent.ENTITY_PLACE, pos,
                    GameEvent.Context.of(context.getPlayer(), level.getBlockState(pos.below())));
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private NtmMinecartEntity createMinecart(Level level, double x, double y, double z, ItemStack stack) {
        NtmMinecartEntity cart = switch (type) {
            case EMPTY -> new NtmEmptyMinecartEntity(level, x, y, z, base);
            case CRATE -> new NtmCrateMinecartEntity(level, x, y, z, stack);
            case POWDER -> new NtmPowderMinecartEntity(level, x, y, z, base);
            case SEMTEX -> new NtmSemtexMinecartEntity(level, x, y, z, base);
            case DESTROYER -> new NtmDestroyerMinecartEntity(level, x, y, z, base);
        };
        if (stack.hasCustomHoverName()) {
            cart.setCustomName(stack.getHoverName());
        }
        return cart;
    }
}
