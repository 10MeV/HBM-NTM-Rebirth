package com.hbm.ntm.blockentity;

import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.BombMultiMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BombMultiBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 6;
    private static final String TAG_INVENTORY = "Inventory";
    private static final float EXPLOSION_BASE_VALUE = 8.0F;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public BombMultiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOMB_MULTI.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ItemStack[] getDrops() {
        return HbmItemStackUtil.carefulCopyArray(items);
    }

    public boolean isLoaded() {
        return isVanillaTnt(0) && isVanillaTnt(1) && isVanillaTnt(3) && isVanillaTnt(4);
    }

    public int modifierType(int slot) {
        if (slot != 2 && slot != 5) {
            return 0;
        }
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.GUNPOWDER)) {
            return 1;
        }
        if (stack.is(Blocks.TNT.asItem())) {
            return 2;
        }
        if (isLegacyItem(stack, "pellet_cluster")) {
            return 3;
        }
        if (isLegacyItem(stack, "powder_fire")) {
            return 4;
        }
        if (isLegacyItem(stack, "powder_poison")) {
            return 5;
        }
        if (isLegacyItem(stack, "pellet_gas")) {
            return 6;
        }
        return 0;
    }

    public BombMultiStats getStats() {
        float explosionValue = EXPLOSION_BASE_VALUE;
        int clusterCount = 0;
        int fireRadius = 0;
        int poisonRadius = 0;
        int gasCloud = 0;

        int[] types = {modifierType(2), modifierType(5)};
        for (int type : types) {
            switch (type) {
                case 1 -> explosionValue += 1.0F;
                case 2 -> explosionValue += 4.0F;
                case 3 -> clusterCount += 50;
                case 4 -> fireRadius += 10;
                case 5 -> poisonRadius += 15;
                case 6 -> gasCloud += 50;
                default -> {
                }
            }
        }
        return new BombMultiStats(explosionValue, clusterCount, fireRadius, poisonRadius, gasCloud);
    }

    public void clearSlots() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_INVENTORY)) {
            HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm_ntm_rebirth.bomb_multi");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BombMultiMenu(containerId, inventory, this);
    }

    private boolean isVanillaTnt(int slot) {
        return items.getStackInSlot(slot).is(Blocks.TNT.asItem());
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    public record BombMultiStats(float explosionValue, int clusterCount, int fireRadius, int poisonRadius,
            int gasCloud) {
        public void explode(Level level, BlockPos pos) {
            ExplosionLarge.explode(level, pos.getX(), pos.getY(), pos.getZ(), explosionValue, true, true, true);
            if (clusterCount > 0) {
                ExplosionChaos.cluster(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        clusterCount, 0.0F, (float) Math.PI * 0.5F, (float) Math.PI * 2.0F,
                        (float) Math.PI * 0.125F, 0.375F);
            }
            if (fireRadius > 0) {
                ExplosionChaos.igniteAllBlocks(level, pos.getX(), pos.getY(), pos.getZ(), fireRadius);
            }
            if (poisonRadius > 0) {
                ExplosionNukeGeneric.wasteNoSchrab(level, pos.getX(), pos.getY(), pos.getZ(), poisonRadius);
            }
            if (gasCloud > 0) {
                level.addFreshEntity(MistEntity.create(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, HbmFluids.CHLORINE, gasCloud * 15.0F / 50.0F,
                        gasCloud * 7.5F / 50.0F, 150));
            }
        }
    }
}
