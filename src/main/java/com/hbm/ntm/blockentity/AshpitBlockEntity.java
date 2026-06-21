package com.hbm.ntm.blockentity;

import com.hbm.ntm.config.AshpitConfig;
import com.hbm.ntm.menu.AshpitMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AshpitBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile {
    public static final int SLOT_COUNT = 5;

    private static final String TAG_ASH_WOOD = "ashLevelWood";
    private static final String TAG_ASH_COAL = "ashLevelCoal";
    private static final String TAG_ASH_MISC = "ashLevelMisc";
    private static final String TAG_ASH_FLY = "ashLevelFly";
    private static final String TAG_ASH_SOOT = "ashLevelSoot";
    private static final String TAG_PLAYERS_USING = "playersUsing";
    private static final String TAG_DOOR_ANGLE = "doorAngle";
    private static final String TAG_PREV_DOOR_ANGLE = "prevDoorAngle";
    private static final String TAG_IS_FULL = "isFull";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();

    private int ashLevelWood;
    private int ashLevelCoal;
    private int ashLevelMisc;
    private int ashLevelFly;
    private int ashLevelSoot;
    private int playersUsing;
    private float doorAngle;
    private float prevDoorAngle;
    private boolean isFull;

    public AshpitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASHPIT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AshpitBlockEntity ashpit) {
        if (level.isClientSide) {
            return;
        }
        boolean oldFull = ashpit.isFull;
        boolean changed = false;
        changed |= ashpit.processAsh(AshType.WOOD);
        changed |= ashpit.processAsh(AshType.COAL);
        changed |= ashpit.processAsh(AshType.MISC);
        changed |= ashpit.processAsh(AshType.FLY);
        changed |= ashpit.processAsh(AshType.SOOT);
        ashpit.isFull = ashpit.hasOutputAsh();
        changed |= oldFull != ashpit.isFull;
        if (changed) {
            ashpit.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        ashpit.networkPackNT(50);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AshpitBlockEntity ashpit) {
        if (!level.isClientSide) {
            return;
        }
        ashpit.prevDoorAngle = ashpit.doorAngle;
        float swingSpeed = ashpit.doorAngle / 10.0F + 3.0F;
        ashpit.doorAngle += ashpit.playersUsing > 0 ? swingSpeed : -swingSpeed;
        ashpit.doorAngle = Mth.clamp(ashpit.doorAngle, 0.0F, 135.0F);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public float getDoorAngle(float partialTick) {
        return prevDoorAngle + (doorAngle - prevDoorAngle) * partialTick;
    }

    public boolean isFull() {
        return isFull;
    }

    public void closeMenu(Player player) {
        playersUsing = Math.max(0, playersUsing - 1);
        syncToClient();
    }

    public void addWoodAsh(long amount) {
        ashLevelWood = addClamped(ashLevelWood, amount);
    }

    public void addCoalAsh(long amount) {
        ashLevelCoal = addClamped(ashLevelCoal, amount);
    }

    public void addMiscAsh(long amount) {
        ashLevelMisc = addClamped(ashLevelMisc, amount);
    }

    public void addFlyAsh(long amount) {
        ashLevelFly = addClamped(ashLevelFly, amount);
    }

    public void addSoot(long amount) {
        ashLevelSoot = addClamped(ashLevelSoot, amount);
    }

    private boolean processAsh(AshType type) {
        int threshold = type.threshold();
        if (ashLevel(type) < threshold) {
            return false;
        }
        if (!insertAsh(type)) {
            return false;
        }
        setAshLevel(type, ashLevel(type) - threshold);
        return true;
    }

    private boolean insertAsh(AshType type) {
        return HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, 0, SLOT_COUNT - 1,
                new ItemStack(type.item())).isEmpty();
    }

    private boolean hasOutputAsh() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!items.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int ashLevel(AshType type) {
        return switch (type) {
            case WOOD -> ashLevelWood;
            case COAL -> ashLevelCoal;
            case MISC -> ashLevelMisc;
            case FLY -> ashLevelFly;
            case SOOT -> ashLevelSoot;
        };
    }

    private void setAshLevel(AshType type, int value) {
        switch (type) {
            case WOOD -> ashLevelWood = value;
            case COAL -> ashLevelCoal = value;
            case MISC -> ashLevelMisc = value;
            case FLY -> ashLevelFly = value;
            case SOOT -> ashLevelSoot = value;
        }
    }

    private int addClamped(int current, long amount) {
        if (amount <= 0L) {
            return current;
        }
        setChanged();
        long result = current + amount;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_ntm_rebirth.ashpit");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        playersUsing++;
        syncToClient();
        return new AshpitMenu(containerId, inventory, this);
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            networkPackNT(50);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        tag.putInt(TAG_ASH_WOOD, ashLevelWood);
        tag.putInt(TAG_ASH_COAL, ashLevelCoal);
        tag.putInt(TAG_ASH_MISC, ashLevelMisc);
        tag.putInt(TAG_ASH_FLY, ashLevelFly);
        tag.putInt(TAG_ASH_SOOT, ashLevelSoot);
        tag.putInt(TAG_PLAYERS_USING, playersUsing);
        tag.putFloat(TAG_DOOR_ANGLE, doorAngle);
        tag.putFloat(TAG_PREV_DOOR_ANGLE, prevDoorAngle);
        tag.putBoolean(TAG_IS_FULL, isFull);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        ashLevelWood = tag.getInt(TAG_ASH_WOOD);
        ashLevelCoal = tag.getInt(TAG_ASH_COAL);
        ashLevelMisc = tag.getInt(TAG_ASH_MISC);
        ashLevelFly = tag.getInt(TAG_ASH_FLY);
        ashLevelSoot = tag.getInt(TAG_ASH_SOOT);
        playersUsing = Math.max(0, tag.getInt(TAG_PLAYERS_USING));
        doorAngle = tag.getFloat(TAG_DOOR_ANGLE);
        prevDoorAngle = tag.getFloat(TAG_PREV_DOOR_ANGLE);
        isFull = tag.getBoolean(TAG_IS_FULL) || hasOutputAsh();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
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

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    private enum AshType {
        WOOD("powder_ash_wood"),
        COAL("powder_ash_coal"),
        MISC("powder_ash_misc"),
        FLY("powder_ash_fly"),
        SOOT("powder_ash_soot");

        private final String itemName;

        AshType(String itemName) {
            this.itemName = itemName;
        }

        private int threshold() {
            return switch (this) {
                case WOOD -> AshpitConfig.thresholdWood();
                case COAL -> AshpitConfig.thresholdCoal();
                case MISC -> AshpitConfig.thresholdMisc();
                case FLY -> AshpitConfig.thresholdFly();
                case SOOT -> AshpitConfig.thresholdSoot();
            };
        }

        private net.minecraft.world.item.Item item() {
            return ModItems.legacyItem(itemName).get();
        }
    }
}
