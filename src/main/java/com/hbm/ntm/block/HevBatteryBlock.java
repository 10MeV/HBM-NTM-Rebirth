package com.hbm.ntm.block;

import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class HevBatteryBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public HevBatteryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!level.isClientSide && FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player)
                && chest.getItem() instanceof FsbPoweredArmor) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack armor = player.getItemBySlot(slot);
                if (armor.getItem() instanceof IBatteryItem battery) {
                    long maxCharge = battery.getMaxCharge(armor);
                    long currentCharge = battery.getCharge(armor);
                    battery.setCharge(armor, Math.min(currentCharge + 150_000L, maxCharge));
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_BATTERY.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            level.removeBlock(pos, false);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
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
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
