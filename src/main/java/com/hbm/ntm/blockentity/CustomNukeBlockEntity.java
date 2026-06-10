package com.hbm.ntm.blockentity;

import com.hbm.ntm.explosion.CustomNukeExplosion;
import com.hbm.ntm.menu.CustomNukeMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomNukeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 27;
    private static final String TAG_INVENTORY = "Inventory";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            statsDirty = true;
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private boolean statsDirty = true;
    private CustomNukeStats cachedStats = CustomNukeStats.EMPTY;

    public CustomNukeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOM_NUKE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ItemStack[] getDrops() {
        return HbmItemStackUtil.carefulCopyArray(items);
    }

    public void clearSlots() {
        for (int i = 0; i < items.getSlots(); i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        statsDirty = true;
        setChanged();
    }

    public CustomNukeStats getStats() {
        if (statsDirty) {
            cachedStats = calculateStats(items);
            statsDirty = false;
        }
        return cachedStats;
    }

    private static CustomNukeStats calculateStats(ItemStackHandler items) {
        float tnt = 0.0F;
        float nuke = 0.0F;
        float hydro = 0.0F;
        float amat = 0.0F;
        float dirty = 0.0F;
        float schrab = 0.0F;
        float euph = 0.0F;
        float tntMod = 1.0F;
        float nukeMod = 1.0F;
        float hydroMod = 1.0F;
        float amatMod = 1.0F;
        float dirtyMod = 1.0F;
        float schrabMod = 1.0F;
        boolean falling = false;

        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (isItem(stack, "custom_fall")) {
                falling = true;
            }
            for (CustomNukeEntry entry : CustomNukeEntries.entries()) {
                if (!entry.matches(stack)) {
                    continue;
                }
                float value = entry.value() * stack.getCount();
                if (entry.multiplier()) {
                    switch (entry.type()) {
                        case TNT -> tntMod *= value;
                        case NUKE -> nukeMod *= value;
                        case HYDRO -> hydroMod *= value;
                        case AMAT -> amatMod *= value;
                        case DIRTY -> dirtyMod *= value;
                        case SCHRAB -> schrabMod *= value;
                        case EUPH -> {
                        }
                    }
                } else {
                    switch (entry.type()) {
                        case TNT -> tnt += value;
                        case NUKE -> nuke += value;
                        case HYDRO -> hydro += value;
                        case AMAT -> amat += value;
                        case DIRTY -> dirty += value;
                        case SCHRAB -> schrab += value;
                        case EUPH -> euph += value;
                    }
                }
            }
        }

        tnt *= tntMod;
        nuke *= nukeMod;
        hydro *= hydroMod;
        amat *= amatMod;
        dirty *= dirtyMod;
        schrab *= schrabMod;

        if (tnt < 16.0F) {
            nuke = 0.0F;
        }
        if (nuke < 100.0F) {
            hydro = 0.0F;
        }
        if (nuke < 50.0F) {
            amat = 0.0F;
            schrab = 0.0F;
        }
        if (schrab == 0.0F) {
            euph = 0.0F;
        }

        return new CustomNukeStats(tnt, nuke, hydro, amat, dirty, schrab, euph, falling);
    }

    @Nullable
    public static CustomNukeTooltipEntry getTooltipEntry(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (CustomNukeEntry entry : CustomNukeEntries.entries()) {
            if (entry.matches(stack)) {
                return new CustomNukeTooltipEntry(entry.type().displayName(), entry.value(), entry.multiplier());
            }
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomNukeBlockEntity blockEntity) {
        blockEntity.getStats();
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
        statsDirty = true;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm_ntm_rebirth.nuke_custom");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CustomNukeMenu(containerId, inventory, this);
    }

    private static boolean isItem(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    private static boolean isBlock(ItemStack stack, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block != null && !stack.isEmpty() && stack.is(block.get().asItem());
    }

    private static final class CustomNukeEntries {
        private static final List<CustomNukeEntry> ENTRIES = buildEntries();

        static List<CustomNukeEntry> entries() {
            return ENTRIES;
        }

        private static List<CustomNukeEntry> buildEntries() {
            List<CustomNukeEntry> entries = new ArrayList<>();
            addItem(entries, "custom_tnt", BombType.TNT, 10.0F);
            addItem(entries, "custom_nuke", BombType.NUKE, 30.0F);
            addItem(entries, "custom_hydro", BombType.HYDRO, 30.0F);
            addItem(entries, "custom_amat", BombType.AMAT, 15.0F);
            addItem(entries, "custom_dirty", BombType.DIRTY, 10.0F);
            addItem(entries, "custom_schrab", BombType.SCHRAB, 15.0F);

            addItem(entries, "ingot_semtex", BombType.TNT, 8.0F);
            addBlock(entries, "det_cord", BombType.TNT, 1.5F);
            addBlock(entries, "det_charge", BombType.TNT, 15.0F);
            addBlock(entries, "red_barrel", BombType.TNT, 2.5F);
            addBlock(entries, "pink_barrel", BombType.TNT, 4.0F);

            addItem(entries, "ingot_u233", BombType.NUKE, 15.0F);
            addItem(entries, "ingot_u235", BombType.NUKE, 15.0F);
            addItem(entries, "ingot_pu239", BombType.NUKE, 25.0F);
            addItem(entries, "ingot_pu241", BombType.NUKE, 25.0F);
            addItem(entries, "ingot_neptunium", BombType.NUKE, 30.0F);
            addItem(entries, "nugget_u233", BombType.NUKE, 1.5F);
            addItem(entries, "nugget_u235", BombType.NUKE, 1.5F);
            addItem(entries, "nugget_pu239", BombType.NUKE, 2.5F);
            addItem(entries, "nugget_pu241", BombType.NUKE, 2.5F);
            addItem(entries, "nugget_neptunium", BombType.NUKE, 3.0F);
            addItem(entries, "powder_neptunium", BombType.NUKE, 30.0F);

            addItem(entries, "cell_deuterium", BombType.HYDRO, 20.0F);
            addItem(entries, "cell_tritium", BombType.HYDRO, 30.0F);
            addItem(entries, "lithium", BombType.HYDRO, 20.0F);

            addItem(entries, "cell_antimatter", BombType.AMAT, 5.0F);
            addItem(entries, "egg_balefire_shard", BombType.AMAT, 15.0F);
            addItem(entries, "egg_balefire", BombType.AMAT, 150.0F);

            addItem(entries, "ingot_tungsten", BombType.DIRTY, 1.0F);
            addItem(entries, "ingot_schrabidium", BombType.SCHRAB, 5.0F);
            addBlock(entries, "block_schrabidium", BombType.SCHRAB, 50.0F);
            addItem(entries, "nugget_schrabidium", BombType.SCHRAB, 0.5F);
            addItem(entries, "powder_schrabidium", BombType.SCHRAB, 5.0F);
            addItem(entries, "cell_sas3", BombType.SCHRAB, 7.5F);
            addItem(entries, "cell_anti_schrabidium", BombType.SCHRAB, 15.0F);

            addItem(entries, "nugget_euphemium", BombType.EUPH, 1.0F);
            addItem(entries, "ingot_euphemium", BombType.EUPH, 1.0F);

            addItem(entries, "ingot_uranium", BombType.NUKE, 1.05F, true);
            addItem(entries, "ingot_plutonium", BombType.NUKE, 1.15F, true);
            addItem(entries, "ingot_u238", BombType.NUKE, 1.1F, true);
            addItem(entries, "ingot_pu238", BombType.NUKE, 1.15F, true);
            addItem(entries, "nugget_uranium", BombType.NUKE, 1.005F, true);
            addItem(entries, "nugget_plutonium", BombType.NUKE, 1.15F, true);
            addItem(entries, "nugget_u238", BombType.NUKE, 1.01F, true);
            addItem(entries, "nugget_pu238", BombType.NUKE, 1.015F, true);
            addItem(entries, "powder_uranium", BombType.NUKE, 1.05F, true);
            addItem(entries, "powder_plutonium", BombType.NUKE, 1.15F, true);

            addItem(entries, "ingot_pu240", BombType.DIRTY, 1.05F, true);
            addItem(entries, "nuclear_waste", BombType.DIRTY, 1.025F, true);
            addBlock(entries, "block_waste", BombType.DIRTY, 1.25F, true);
            addBlock(entries, "yellow_barrel", BombType.DIRTY, 1.2F, true);
            return entries;
        }

        private static void addItem(List<CustomNukeEntry> entries, String name, BombType type, float value) {
            addItem(entries, name, type, value, false);
        }

        private static void addItem(List<CustomNukeEntry> entries, String name, BombType type, float value, boolean multiplier) {
            if (ModItems.legacyItem(name) != null) {
                entries.add(new CustomNukeEntry(name, true, type, value, multiplier));
            }
        }

        private static void addBlock(List<CustomNukeEntry> entries, String name, BombType type, float value) {
            addBlock(entries, name, type, value, false);
        }

        private static void addBlock(List<CustomNukeEntry> entries, String name, BombType type, float value, boolean multiplier) {
            if (ModBlocks.legacyBlock(name) != null) {
                entries.add(new CustomNukeEntry(name, false, type, value, multiplier));
            }
        }
    }

    private record CustomNukeEntry(String name, boolean item, BombType type, float value, boolean multiplier) {
        boolean matches(ItemStack stack) {
            return item ? isItem(stack, name) : isBlock(stack, name);
        }
    }

    private enum BombType {
        TNT("TNT"),
        NUKE("Nuclear"),
        HYDRO("Hydrogen"),
        AMAT("Antimatter"),
        DIRTY("Salted"),
        SCHRAB("Schrabidium"),
        EUPH("Anti Mass");

        private final String displayName;

        BombType(String displayName) {
            this.displayName = displayName;
        }

        String displayName() {
            return displayName;
        }
    }

    public record CustomNukeTooltipEntry(String stage, float value, boolean multiplier) {
    }

    public record CustomNukeStats(float tnt, float nuke, float hydro, float amat, float dirty, float schrab,
            float euph, boolean falling) {
        public static final CustomNukeStats EMPTY = new CustomNukeStats(0, 0, 0, 0, 0, 0, 0, false);

        public boolean isEmpty() {
            return tnt <= 0.0F && nuke <= 0.0F && hydro <= 0.0F && amat <= 0.0F
                    && dirty <= 0.0F && schrab <= 0.0F && euph <= 0.0F;
        }

        public float adjustedNuke() {
            return CustomNukeExplosion.adjustedNuclear(tnt, nuke);
        }

        public float adjustedHydro() {
            return CustomNukeExplosion.adjustedHydrogen(tnt, nuke, hydro);
        }

        public float adjustedAmat() {
            return CustomNukeExplosion.adjustedAntimatter(tnt, nuke, hydro, amat);
        }

        public float adjustedSchrab() {
            return CustomNukeExplosion.adjustedSchrab(tnt, nuke, hydro, amat, schrab);
        }

        public void explode(Level level, double x, double y, double z) {
            CustomNukeExplosion.explode(level, x, y, z, tnt, nuke, hydro, amat, dirty, schrab, euph);
        }
    }
}
