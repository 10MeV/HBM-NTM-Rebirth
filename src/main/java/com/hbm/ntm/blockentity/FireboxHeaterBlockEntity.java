package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.menu.FireboxHeaterMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireboxHeaterBlockEntity extends BlockEntity
        implements MenuProvider, HeatSource, HbmLegacyLoadedTile {
    public static final int SLOT_COUNT = 2;

    private static final String TAG_MAX_BURN_TIME = "maxBurnTime";
    private static final String TAG_BURN_TIME = "burnTime";
    private static final String TAG_BURN_HEAT = "burnHeat";
    private static final String TAG_HEAT = "heatEnergy";
    private static final String TAG_WAS_ON = "wasOn";
    private static final String TAG_PLAYERS_USING = "playersUsing";
    private static final LegacyBurnTimeModule BURN_MODULE = new LegacyBurnTimeModule()
            .setLigniteTimeMod(1.25D)
            .setCoalTimeMod(1.25D)
            .setCokeTimeMod(1.25D)
            .setSolidTimeMod(1.5D)
            .setRocketTimeMod(1.5D)
            .setBalefireTimeMod(0.5D)
            .setLigniteHeatMod(2.0D)
            .setCoalHeatMod(2.0D)
            .setCokeHeatMod(2.0D)
            .setSolidHeatMod(3.0D)
            .setRocketHeatMod(5.0D)
            .setBalefireHeatMod(15.0D);

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(50);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && BURN_MODULE.getBurnTime(stack) > 0;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int maxBurnTime;
    private int burnTime;
    private int burnHeat;
    private int heatEnergy;
    private boolean wasOn;
    private int playersUsing;
    private float doorAngle;
    private float prevDoorAngle;

    public FireboxHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIREBOX_HEATER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FireboxHeaterBlockEntity firebox) {
        if (level.isClientSide) {
            return;
        }
        if (firebox.kind().oven()) {
            firebox.pullHeatFromBelow(level, pos);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction side = direction.getClockWise();
            for (int j = -1; j <= 1; j++) {
                BlockPos connector = pos.relative(direction, 2).relative(side, j);
                firebox.pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), direction);
            }
        }

        boolean oldWasOn = firebox.wasOn;
        int oldHeat = firebox.heatEnergy;
        int oldBurn = firebox.burnTime;
        firebox.wasOn = false;
        if (firebox.burnTime <= 0) {
            firebox.tryConsumeFuel(level, pos);
        } else {
            if (firebox.heatEnergy < firebox.maxHeat()) {
                firebox.burnTime--;
                if (level.getGameTime() % 20L == 0L) {
                    firebox.pollution.pollute(level, pos, PollutionType.SOOT,
                            PollutionManager.SOOT_PER_SECOND * 3.0F);
                }
            }
            firebox.wasOn = true;
            if (level.random.nextInt(15) == 0 && !firebox.isMuffled()) {
                LegacySoundPlayer.playSoundEffect(level, pos, "VANILLA_FIRE", 1.0F,
                        0.5F + level.random.nextFloat() * 0.5F);
            }
        }

        if (firebox.wasOn) {
            firebox.heatEnergy = Math.min(firebox.heatEnergy + firebox.burnHeat, firebox.maxHeat());
        } else {
            firebox.heatEnergy = Math.max(firebox.heatEnergy - Math.max(firebox.heatEnergy / 1000, 1), 0);
            firebox.burnHeat = 0;
        }

        firebox.networkPackNT(50);
        if (oldWasOn != firebox.wasOn || oldHeat != firebox.heatEnergy || oldBurn != firebox.burnTime) {
            firebox.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FireboxHeaterBlockEntity firebox) {
        if (!level.isClientSide) {
            return;
        }
        firebox.prevDoorAngle = firebox.doorAngle;
        float swingSpeed = firebox.doorAngle / 10.0F + 3.0F;
        firebox.doorAngle += firebox.playersUsing > 0 ? swingSpeed : -swingSpeed;
        firebox.doorAngle = Mth.clamp(firebox.doorAngle, 0.0F, 135.0F);
        if (firebox.wasOn && level.getGameTime() % 5L == 0L) {
            Direction facing = firebox.facing();
            double x = pos.getX() + 0.5D + facing.getStepX();
            double y = pos.getY() + 0.25D;
            double z = pos.getZ() + 0.5D + facing.getStepZ();
            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    x + level.random.nextDouble() * 0.5D - 0.25D,
                    y + level.random.nextDouble() * 0.25D,
                    z + level.random.nextDouble() * 0.5D - 0.25D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnHeat() {
        return burnHeat;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int maxHeat() {
        return kind().maxHeat;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public float getDoorAngle(float partialTick) {
        return prevDoorAngle + (doorAngle - prevDoorAngle) * partialTick;
    }

    public Kind kind() {
        return getBlockState().is(com.hbm.ntm.registry.ModBlocks.HEATER_OVEN.get())
                ? Kind.OVEN
                : Kind.FIREBOX;
    }

    public static LegacyBurnTimeModule burnModule() {
        return BURN_MODULE;
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        heatEnergy = Math.max(0, heatEnergy - Math.max(0, heat));
        setChanged();
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public Component getDisplayName() {
        return kind().displayName;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        playersUsing++;
        syncToClient();
        return new FireboxHeaterMenu(containerId, inventory, this);
    }

    public void closeMenu(Player player) {
        playersUsing = Math.max(0, playersUsing - 1);
        syncToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        pollution.writeLegacyNbt(tag);
        tag.putInt(TAG_MAX_BURN_TIME, maxBurnTime);
        tag.putInt(TAG_BURN_TIME, burnTime);
        tag.putInt(TAG_BURN_HEAT, burnHeat);
        tag.putInt(TAG_HEAT, heatEnergy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        loadItems(tag);
        pollution.readLegacyNbt(tag);
        maxBurnTime = tag.getInt(TAG_MAX_BURN_TIME);
        burnTime = tag.getInt(TAG_BURN_TIME);
        burnHeat = tag.getInt(TAG_BURN_HEAT);
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        pollution.writeLegacyNbt(tag);
        tag.putInt(TAG_MAX_BURN_TIME, maxBurnTime);
        tag.putInt(TAG_BURN_TIME, burnTime);
        tag.putInt(TAG_BURN_HEAT, burnHeat);
        tag.putInt(TAG_HEAT, heatEnergy);
        tag.putBoolean(TAG_WAS_ON, wasOn);
        tag.putInt(TAG_PLAYERS_USING, playersUsing);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        loadItems(tag);
        pollution.readLegacyNbt(tag);
        maxBurnTime = tag.getInt(TAG_MAX_BURN_TIME);
        burnTime = tag.getInt(TAG_BURN_TIME);
        burnHeat = tag.getInt(TAG_BURN_HEAT);
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
        wasOn = tag.getBoolean(TAG_WAS_ON);
        playersUsing = Math.max(0, tag.getInt(TAG_PLAYERS_USING));
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
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
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    private void tryConsumeFuel(Level level, BlockPos pos) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int baseTime = BURN_MODULE.getBurnTime(stack);
            if (baseTime <= 0) {
                continue;
            }
            int fuel = (int) (baseTime * kind().timeMult);
            addAshToBelow(level, pos, stack, baseTime);
            maxBurnTime = burnTime = fuel;
            burnHeat = BURN_MODULE.getBurnHeat(kind().baseHeat, stack);
            ItemStack remaining = stack.copy();
            remaining.shrink(1);
            ItemStack container = stack.getCraftingRemainingItem();
            items.setStackInSlot(slot, remaining.isEmpty() && !container.isEmpty() ? container : remaining);
            wasOn = true;
            break;
        }
    }

    private void addAshToBelow(Level level, BlockPos pos, ItemStack stack, int baseTime) {
        if (!(level.getBlockEntity(pos.below()) instanceof AshpitBlockEntity ashpit)) {
            return;
        }
        switch (BURN_MODULE.getAshFromFuel(stack)) {
            case WOOD -> ashpit.addWoodAsh(baseTime);
            case COAL -> ashpit.addCoalAsh(baseTime);
            case MISC -> ashpit.addMiscAsh(baseTime);
        }
    }

    private void pullHeatFromBelow(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.below()) instanceof HeatSource source)) {
            return;
        }
        int toPull = Math.max(Math.min(source.getHeatStored(), maxHeat() - heatEnergy), 0);
        if (toPull > 0) {
            heatEnergy += (int) (toPull * Kind.OVEN.heatPullEfficiency);
            source.useUpHeat(toPull);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            networkPackNT(50);
        }
    }

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "Inventory", items);
    }

    public enum Kind {
        FIREBOX(Component.translatableWithFallback("container.heaterFirebox", "Firebox"), 100, 1.0D, 100_000, 0.0D),
        OVEN(Component.translatableWithFallback("container.heaterOven", "Heating Oven"), 500, 0.125D, 500_000, 0.5D);

        private final Component displayName;
        private final int baseHeat;
        private final double timeMult;
        private final int maxHeat;
        private final double heatPullEfficiency;

        Kind(Component displayName, int baseHeat, double timeMult, int maxHeat, double heatPullEfficiency) {
            this.displayName = displayName;
            this.baseHeat = baseHeat;
            this.timeMult = timeMult;
            this.maxHeat = maxHeat;
            this.heatPullEfficiency = heatPullEfficiency;
        }

        public boolean oven() {
            return this == OVEN;
        }
    }
}
