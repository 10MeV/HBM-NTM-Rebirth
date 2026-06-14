package com.hbm.ntm.item;

import com.hbm.ntm.bullet.LegacySednaMagazineConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmmoContainerItem extends Item {
    private static final int MAX_GUNS = 3;

    private final boolean makeshift;

    public AmmoContainerItem(Properties properties, boolean makeshift) {
        super(properties.stacksTo(1));
        this.makeshift = makeshift;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        List<SednaGunItem> guns = eligibleGuns(player);
        if (guns.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }

        Collections.shuffle(guns);
        for (int i = 0; i < Math.min(MAX_GUNS, guns.size()); i++) {
            giveDefaultAmmo(player, guns.get(i).gunConfig());
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_UNPACK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getInventory().setChanged();
        stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.ammo_container.desc")
                .withStyle(ChatFormatting.YELLOW));
        if (makeshift) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.ammo_container_alt.desc")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private List<SednaGunItem> eligibleGuns(Player player) {
        List<SednaGunItem> guns = new ArrayList<>();
        for (ItemStack inv : player.getInventory().items) {
            if (inv.getItem() instanceof SednaGunItem gun) {
                Optional<LegacySednaMagazineConfigs.DefaultAmmo> ammo = gun.gunConfig().defaultAmmo();
                if (ammo.isPresent() && (!makeshift || !ammo.get().expensiveFlag())) {
                    guns.add(gun);
                }
            }
        }
        return guns;
    }

    private void giveDefaultAmmo(Player player, SednaGunConfig config) {
        config.defaultAmmo().ifPresent(defaultAmmo -> defaultAmmo.ammoEntry().ifPresent(entry -> {
            String legacyItemName = defaultAmmo.legacyItemName() + "_" + entry.legacyName().toLowerCase();
            Item item = ModItems.legacyItem(legacyItemName) == null ? null : ModItems.legacyItem(legacyItemName).get();
            if (item == null) {
                return;
            }
            int count = makeshift ? (int) Math.ceil(defaultAmmo.amount() / 2.0D) : defaultAmmo.amount();
            ItemStack ammo = new ItemStack(item, count);
            ItemStack remainder = HbmInventoryUtil.tryAddItemToInventory(player.getInventory(), ammo);
            if (!remainder.isEmpty()) {
                player.drop(remainder, false);
            }
        }));
    }
}
