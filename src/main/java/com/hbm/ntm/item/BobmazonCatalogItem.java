package com.hbm.ntm.item;

import com.hbm.ntm.bobmazon.BobmazonOfferFactory;
import com.hbm.ntm.client.BobmazonScreenBridge;
import com.hbm.ntm.entity.missile.BobmazonDeliveryEntity;
import com.hbm.ntm.network.HbmItemActionReceiver;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

public class BobmazonCatalogItem extends Item implements HbmItemActionReceiver {
    private static final String TAG_OFFER = "offer";
    private static final String FAILSAFE_MISMATCH =
            "[BOBMAZON] There appears to be a mismatch between the offer you have requested and the offers that exist.";
    private static final String FAILSAFE_ENGAGE = "[BOBMAZON] Engaging fail-safe...";
    private static final String NOT_ENOUGH_CAPS = "[BOBMAZON] Not enough caps!";
    private static final String REQUIREMENT_MISSING = "[BOBMAZON] Achievement requirement not met!";

    public BobmazonCatalogItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BobmazonScreenBridge.open(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canReceiveItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
            ResourceLocation actionType, CompoundTag data) {
        return HbmNetworkActions.BOBMAZON_OFFER.equals(actionType);
    }

    @Override
    public void handleItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
            ResourceLocation actionType, CompoundTag data) {
        if (!HbmNetworkActions.BOBMAZON_OFFER.equals(actionType)) {
            return;
        }

        List<BobmazonOfferFactory.Offer> offers = BobmazonOfferFactory.offersFor(stack);
        if (offers == null || data == null || !data.contains(TAG_OFFER, Tag.TAG_INT)) {
            failSafe(player);
            return;
        }

        int index = data.getInt(TAG_OFFER);
        if (index < 0 || index >= offers.size()) {
            failSafe(player);
            return;
        }

        BobmazonOfferFactory.Offer offer = offers.get(index);
        boolean creative = player.getAbilities().instabuild;
        if (!creative && !offer.requirement().fulfilledBy(player)) {
            player.sendSystemMessage(Component.literal(REQUIREMENT_MISSING));
            return;
        }

        if (countCaps(player) >= offer.cost() || creative) {
            payCaps(player, offer.cost());
            player.inventoryMenu.broadcastChanges();
            spawnDelivery(player, offer.stack());
        } else {
            player.sendSystemMessage(Component.literal(NOT_ENOUGH_CAPS));
        }
    }

    private static void failSafe(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(FAILSAFE_MISMATCH));
        player.sendSystemMessage(Component.literal(FAILSAFE_ENGAGE));
        player.hurt(ModDamageSources.source(player.level(), ModDamageSources.NUCLEAR_BLAST), 1_000.0F);
        player.setDeltaMovement(player.getDeltaMovement().x, 2.0D, player.getDeltaMovement().z);
        player.hurtMarked = true;
        player.hasImpulse = true;
    }

    private static void spawnDelivery(ServerPlayer player, ItemStack payload) {
        Level level = player.level();
        RandomSource random = level.getRandom();
        BobmazonDeliveryEntity delivery = new BobmazonDeliveryEntity(ModEntityTypes.BOBMAZON_DELIVERY.get(), level);
        delivery.setPos(player.getX() + random.nextGaussian() * 10.0D, 300.0D,
                player.getZ() + random.nextGaussian() * 10.0D);
        delivery.setPayload(payload);
        level.addFreshEntity(delivery);
    }

    private static int countCaps(Player player) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && isCap(stack.getItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void payCaps(Player player, int price) {
        if (price <= 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        int remaining = price;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !isCap(stack.getItem())) {
                continue;
            }
            while (!stack.isEmpty() && remaining > 0) {
                stack.shrink(1);
                remaining--;
            }
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        inventory.setChanged();
    }

    private static boolean isCap(Item item) {
        return item == ModItems.CAP_FRITZ.get()
                || item == ModItems.CAP_KORL.get()
                || item == ModItems.CAP_NUKA.get()
                || item == ModItems.CAP_QUANTUM.get()
                || item == ModItems.CAP_RAD.get()
                || item == ModItems.CAP_SPARKLE.get();
    }
}
