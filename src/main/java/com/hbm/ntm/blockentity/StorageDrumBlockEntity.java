package com.hbm.ntm.blockentity;

import com.hbm.config.VersatileConfig;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidReleaseEffects;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.menu.StorageDrumMenu;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmBlockStateUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageDrumBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidSender {
    public static final int SLOT_COUNT = 24;
    private static final int TANK_CAPACITY = 16_000;
    private static final double RADIATION_RANGE = 32.0D;
    private static final int[] LONG_LIQUID = { 0, 0, 0, 0, 0 };
    private static final int[] LONG_GAS = { 0, 50, 100, 0, 250 };
    private static final int[] SHORT_LIQUID = { 0, 50, 150, 250, 350, 500, 750, 1_000 };
    private static final int[] SHORT_GAS = { 100, 100, 500, 1_000, 1_000, 1_000, 1_000, 1_000 };

    private final HbmFluidTank liquidTank;
    private final HbmFluidTank gasTank;
    private int age;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return canInsert(stack);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    public StorageDrumBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WASTEFLUID, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.WASTEGAS, TANK_CAPACITY));
    }

    private StorageDrumBlockEntity(BlockPos pos, BlockState state, HbmFluidTank liquidTank, HbmFluidTank gasTank) {
        super(ModBlockEntities.STORAGE_DRUM.get(), pos, state, List.of(liquidTank, gasTank));
        this.liquidTank = liquidTank;
        this.gasTank = gasTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageDrumBlockEntity drum) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, drum);
        boolean changed = drum.decayContents(level);

        drum.age++;
        if (drum.age >= 20) {
            drum.age -= 20;
        }

        drum.tryProvideFluidToPorts(drum.liquidTank.getTankType(), drum.liquidTank.getPressure(), drum);
        drum.tryProvideFluidToPorts(drum.gasTank.getTankType(), drum.gasTank.getPressure(), drum);
        drum.networkPackNT(25);

        if (changed) {
            drum.setChangedAndSync();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getLiquidTank() {
        return liquidTank;
    }

    public HbmFluidTank getGasTank() {
        return gasTank;
    }

    public int age() {
        return age;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.storageDrum", "Nuclear Waste Disposal Drum");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new StorageDrumMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(liquidTank, gasTank);
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        setChangedAndSync();
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return getSendingTanks().stream().anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        tag.putInt("age", age);
        liquidTank.writeToNbt(tag, "liquid");
        gasTank.writeToNbt(tag, "gas");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        age = tag.getInt("age");
        if (tag.contains("liquid")) {
            liquidTank.readFromNbt(tag, "liquid");
        }
        if (tag.contains("gas")) {
            gasTank.readFromNbt(tag, "gas");
        }
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

    private boolean decayContents(Level level) {
        boolean changed = false;
        float radiation = 0.0F;
        int liquid = 0;
        int gas = 0;

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (level.getGameTime() % 20L == 0L) {
                radiation += HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
            }
            DecayResult result = decay(level, stack);
            if (result != null) {
                items.setStackInSlot(slot, result.stack());
                liquid += result.liquid();
                gas += result.gas();
                changed = true;
            }
        }

        if (liquid > 0) {
            int accepted = liquidTank.fill(HbmFluids.WASTEFLUID, liquid, 0, false);
            releaseOverflow(liquidTank, liquid - accepted);
            changed = true;
        }
        if (gas > 0) {
            int accepted = gasTank.fill(HbmFluids.WASTEGAS, gas, 0, false);
            releaseOverflow(gasTank, gas - accepted);
            changed = true;
        }
        if (radiation > 0.0F) {
            radiate(level, radiation);
        }
        return changed;
    }

    private void releaseOverflow(HbmFluidTank tank, int overflow) {
        if (overflow <= 0 || level == null) {
            return;
        }
        HbmFluidReleaseEffects.applyRelease(level, worldPosition, tank.getTankType(), overflow, FluidReleaseType.SPILL);
    }

    @Nullable
    private DecayResult decay(Level level, ItemStack stack) {
        String id = itemId(stack);
        int meta = Math.max(0, stack.getDamageValue());
        int longMeta = rectify(meta, LONG_LIQUID.length);
        int shortMeta = rectify(meta, LONG_LIQUID.length);

        if (matches(id, "nuclear_waste_long") && roll(level, VersatileConfig.getLongDecayChance())) {
            return new DecayResult(itemStack("nuclear_waste_long_depleted", meta), LONG_LIQUID[longMeta], LONG_GAS[longMeta]);
        }
        if (matches(id, "nuclear_waste_long_tiny") && roll(level, VersatileConfig.getLongDecayChance() / 10)) {
            return new DecayResult(itemStack("nuclear_waste_long_depleted_tiny", meta), LONG_LIQUID[longMeta] / 10, LONG_GAS[longMeta] / 10);
        }
        if (matches(id, "nuclear_waste_short") && roll(level, VersatileConfig.getShortDecayChance())) {
            return new DecayResult(itemStack("nuclear_waste_short_depleted", meta), SHORT_LIQUID[shortMeta], SHORT_GAS[shortMeta]);
        }
        if (matches(id, "nuclear_waste_short_tiny") && roll(level, VersatileConfig.getShortDecayChance() / 10)) {
            return new DecayResult(itemStack("nuclear_waste_short_depleted_tiny", meta), SHORT_LIQUID[shortMeta] / 10, SHORT_GAS[shortMeta] / 10);
        }
        if (matches(id, "ingot_au198") && roll(level, VersatileConfig.getShortDecayChance() / 20)) {
            return new DecayResult(itemStack("ingot_mercury", meta), 0, 0);
        }
        if (matches(id, "nugget_au198") && roll(level, VersatileConfig.getShortDecayChance() / 100)) {
            return new DecayResult(itemStack("nugget_mercury", meta), 0, 0);
        }
        if (matches(id, "ingot_pb209") && roll(level, VersatileConfig.getShortDecayChance() / 10)) {
            return new DecayResult(itemStack("ingot_bismuth", meta), 0, 0);
        }
        if (matches(id, "nugget_pb209") && roll(level, VersatileConfig.getShortDecayChance() / 50)) {
            return new DecayResult(itemStack("nugget_bismuth", meta), 0, 0);
        }
        if (matches(id, "powder_sr90") && roll(level, VersatileConfig.getShortDecayChance() / 10)) {
            return new DecayResult(itemStack("powder_zirconium", meta), 0, 0);
        }
        if (matches(id, "nugget_sr90") && roll(level, VersatileConfig.getShortDecayChance() / 50)) {
            return new DecayResult(itemStack("nugget_zirconium", meta), 0, 0);
        }
        return null;
    }

    private void radiate(Level level, float radiation) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        AABB box = new AABB(center, center).inflate(RADIATION_RANGE);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            Vec3 delta = entity.getEyePosition().subtract(center);
            double length = delta.length();
            if (length <= 0.0D) {
                continue;
            }
            Vec3 normal = delta.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < length; i++) {
                BlockPos sample = BlockPos.containing(center.add(normal.scale(i)));
                resistance += HbmBlockStateUtil.explosionResistance(level.getBlockState(sample), level, sample);
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }
            float exposure = radiation / resistance / (float) (length * length);
            RadiationUtil.contaminate(entity, exposure, true);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean roll(Level level, int chance) {
        return chance > 0 && level.random.nextInt(chance) == 0;
    }

    private static int rectify(int meta, int length) {
        return length <= 0 ? 0 : Math.abs(meta) % length;
    }

    private static boolean canInsert(ItemStack stack) {
        String id = itemId(stack);
        return matches(id, "nuclear_waste_long")
                || matches(id, "nuclear_waste_long_tiny")
                || matches(id, "nuclear_waste_short")
                || matches(id, "nuclear_waste_short_tiny")
                || matches(id, "ingot_au198");
    }

    private static boolean canExtract(ItemStack stack) {
        String id = itemId(stack);
        return matches(id, "nuclear_waste_long_depleted")
                || matches(id, "nuclear_waste_long_depleted_tiny")
                || matches(id, "nuclear_waste_short_depleted")
                || matches(id, "nuclear_waste_short_depleted_tiny")
                || matches(id, "ingot_mercury");
    }

    private static boolean matches(String actual, String expected) {
        return expected.equals(actual);
    }

    private static String itemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.getPath();
    }

    private static ItemStack itemStack(String id, int damageValue) {
        RegistryObject<Item> item = ModItems.legacyItem(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item.get());
        if (damageValue > 0) {
            stack.setDamageValue(damageValue);
        }
        return stack;
    }

    private record DecayResult(ItemStack stack, int liquid, int gas) {
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return items.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canExtract(items.getStackInSlot(slot)) ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return items.isItemValid(slot, stack); }
    }
}
