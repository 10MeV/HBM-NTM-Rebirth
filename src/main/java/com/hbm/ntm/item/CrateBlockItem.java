package com.hbm.ntm.item;

import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.menu.HeldCrateMenu;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateBlockItem extends BlockItem {
    public CrateBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // ItemBlockStorageCrate#onItemUse returns false in this exact branch:
        // non-sneaking players must release a held crate first instead of
        // accidentally placing it.  Mass storage never uses this item class.
        if (ServerConfig.crateOpenHeldEnabled()
                && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ServerConfig.crateOpenHeldEnabled() || stack.getCount() != 1) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!canOpenHeld(player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            triggerHeldSpiders(serverPlayer, stack);
            Component title = stack.hasCustomHoverName() ? stack.getHoverName()
                    : Component.translatable(kind().titleKey());
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((containerId, inventory, owner) ->
                            new HeldCrateMenu(containerId, inventory, hand), title),
                    data -> data.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        StorageCrateBlockEntity.appendLegacyTooltip(stack, tooltip);
    }

    private StorageCrateBlockEntity.Kind kind() {
        return ((CrateBlock) getBlock()).kind();
    }

    /** Mirrors ItemBlockStorageCrate's held-item lock gate. */
    private static boolean canOpenHeld(Player player, ItemStack crate) {
        if (!crate.hasTag() || !crate.getTag().contains(StorageCrateBlockEntity.LEGACY_LOCK_TAG)) {
            return true;
        }
        int pins = crate.getTag().getInt(StorageCrateBlockEntity.LEGACY_LOCK_TAG);
        for (ItemStack candidate : player.getInventory().items) {
            if ((candidate.is(ModItems.KEY.get()) || candidate.is(ModItems.KEY_FAKE.get()))
                    && KeyPinItem.getPins(candidate) == pins) {
                return true;
            }
        }
        return false;
    }

    /** TileEntityCrateBase.spawnSpiders(player, world, ItemStack), ported for the held menu path. */
    private static void triggerHeldSpiders(ServerPlayer player, ItemStack crate) {
        if (!crate.hasTag() || !crate.getTag().getBoolean(StorageCrateBlockEntity.LEGACY_SPIDERS_TAG)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            CaveSpider spider = new CaveSpider(net.minecraft.world.entity.EntityType.CAVE_SPIDER, level);
            spider.moveTo(player.getX() + level.random.nextGaussian() * 2.0D, player.getY() + 1.0D,
                    player.getZ() + level.random.nextGaussian() * 2.0D, level.random.nextFloat(), 0.0F);
            spider.setTarget(player);
            level.addFreshEntity(spider);
        }
        crate.getTag().remove(StorageCrateBlockEntity.LEGACY_SPIDERS_TAG);
    }
}
