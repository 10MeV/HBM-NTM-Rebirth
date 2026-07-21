package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SupplyCrateBlockEntity;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/** Source BlockSupplyCrate: retain its contents when broken, crowbar releases them, never opens a GUI. */
public class SupplyCrateBlock extends Block implements EntityBlock {
    public SupplyCrateBlock(Properties properties) { super(properties); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SupplyCrateBlockEntity(pos, state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(ModItems.CROWBAR.get())) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate) {
            crate.items().forEach(stack -> Block.popResource(level, pos, stack));
            crate.clearItems();
            level.destroyBlock(pos, false);
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:block.crateBreak", 0.5F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate) crate.loadFromPlacedStack(stack);
    }
    @Override public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate) {
            Block.popResource(level, pos, crate.createDroppedStack()); crate.clearItems();
        }
        super.playerWillDestroy(level, pos, state, player);
    }
    @Override public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) { return List.of(); }
}
