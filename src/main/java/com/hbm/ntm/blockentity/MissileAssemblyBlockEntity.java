package com.hbm.ntm.blockentity;

import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.network.HbmClientMissileMultipartReceiver;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.network.MissileMultipartSnapshot;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class MissileAssemblyBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyButtonReceiver,
        HbmClientMissileMultipartReceiver, HbmTileSyncable {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_CLIENT_WARHEAD = "clientWarhead";
    private static final String TAG_CLIENT_FUSELAGE = "clientFuselage";
    private static final String TAG_CLIENT_FINS = "clientFins";
    private static final String TAG_CLIENT_THRUSTER = "clientThruster";
    private static final double MULTIPART_SYNC_RANGE = 250.0D;

    public static final int SLOT_CHIP = 0;
    public static final int SLOT_WARHEAD = 1;
    public static final int SLOT_FUSELAGE = 2;
    public static final int SLOT_STABILITY = 3;
    public static final int SLOT_THRUSTER = 4;
    public static final int SLOT_OUTPUT = 5;
    public static final int SLOT_COUNT = 6;

    private MissileMultipartSnapshot clientMultipart = MissileMultipartSnapshot.EMPTY;
    private String customName;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // TileEntityMachineMissileAssembly#isItemValidForSlot always returned false.
            // Player menu slots were ordinary unfiltered Slots, whereas automation could
            // not insert anything. Keep those two legacy boundaries separate.
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new MissileAssemblyExternalItemHandler(items));

    public MissileAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISSILE_ASSEMBLY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MissileAssemblyBlockEntity assembly) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            ModMessages.sendToAllAround(ModMessages.missileMultipartPacket(pos, assembly.multipartSnapshot()),
                    serverLevel, pos.getX(), pos.getY(), pos.getZ(), MULTIPART_SYNC_RANGE);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        return drops;
    }

    public int chipState() {
        return partState(SLOT_CHIP, MissilePartItem.PartType.CHIP);
    }

    public int fuselageState() {
        return partState(SLOT_FUSELAGE, MissilePartItem.PartType.FUSELAGE);
    }

    public int warheadState() {
        CustomMissilePartProfile.Assembly assembly = assemblyFromInputs();
        if (assembly == null || assembly.warhead() == null || assembly.fuselage() == null
                || assembly.thruster() == null) {
            return 0;
        }
        return assembly.warhead().profile().bottom() == assembly.fuselage().profile().top()
                && assembly.warhead().profile().weight() <= assembly.thruster().profile().lift() ? 1 : 0;
    }

    public int stabilityState() {
        ItemStack stability = items.getStackInSlot(SLOT_STABILITY);
        if (stability.isEmpty()) {
            return -1;
        }
        CustomMissilePartProfile.Assembly assembly = assemblyFromInputs();
        if (assembly == null || assembly.fins() == null || assembly.fuselage() == null) {
            return 0;
        }
        return assembly.fins().profile().top() == assembly.fuselage().profile().bottom() ? 1 : 0;
    }

    public int thrusterState() {
        CustomMissilePartProfile.Assembly assembly = assemblyFromInputs();
        if (assembly == null || assembly.thruster() == null || assembly.fuselage() == null) {
            return 0;
        }
        return assembly.thruster().profile().top() == assembly.fuselage().profile().bottom()
                && assembly.thruster().profile().fuelType() == assembly.fuselage().profile().fuelType() ? 1 : 0;
    }

    public boolean canBuild() {
        return items.getStackInSlot(SLOT_OUTPUT).isEmpty()
                && chipState() == 1
                && warheadState() == 1
                && fuselageState() == 1
                && thrusterState() == 1
                && stabilityState() != 0;
    }

    public void construct() {
        if (!canBuild()) {
            return;
        }
        boolean consumeStability = stabilityState() == 1;
        items.setStackInSlot(SLOT_OUTPUT, CustomMissileItem.buildMissile(
                items.getStackInSlot(SLOT_CHIP),
                items.getStackInSlot(SLOT_WARHEAD),
                items.getStackInSlot(SLOT_FUSELAGE),
                items.getStackInSlot(SLOT_STABILITY),
                items.getStackInSlot(SLOT_THRUSTER)));
        items.setStackInSlot(SLOT_CHIP, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_WARHEAD, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_FUSELAGE, ItemStack.EMPTY);
        if (consumeStability) {
            items.setStackInSlot(SLOT_STABILITY, ItemStack.EMPTY);
        }
        items.setStackInSlot(SLOT_THRUSTER, ItemStack.EMPTY);
        if (level != null) {
            LegacySoundPlayer.playSoundEffect(level, worldPosition, "hbm:block.missileAssembly2", 1.0F, 1.0F);
        }
        setChanged();
        syncToClient();
    }

    public ItemStack previewMissileStack() {
        if (!items.getStackInSlot(SLOT_WARHEAD).isEmpty()
                || !items.getStackInSlot(SLOT_FUSELAGE).isEmpty()
                || !items.getStackInSlot(SLOT_STABILITY).isEmpty()
                || !items.getStackInSlot(SLOT_THRUSTER).isEmpty()) {
            return CustomMissileItem.buildMissile(
                    items.getStackInSlot(SLOT_CHIP),
                    items.getStackInSlot(SLOT_WARHEAD),
                    items.getStackInSlot(SLOT_FUSELAGE),
                    items.getStackInSlot(SLOT_STABILITY),
                    items.getStackInSlot(SLOT_THRUSTER));
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public CustomMissilePartProfile.Assembly assemblyFromInputs() {
        return CustomMissilePartProfile.assemblyFromStack(CustomMissileItem.buildMissile(
                items.getStackInSlot(SLOT_CHIP),
                items.getStackInSlot(SLOT_WARHEAD),
                items.getStackInSlot(SLOT_FUSELAGE),
                items.getStackInSlot(SLOT_STABILITY),
                items.getStackInSlot(SLOT_THRUSTER)));
    }

    @Nullable
    public CustomMissilePartProfile.Assembly assemblyForPreview() {
        if (level != null && level.isClientSide && !clientMultipart.isEmpty()) {
            return assemblyFromSnapshot(clientMultipart);
        }
        return assemblyFromInputs();
    }

    public boolean hasPreviewParts() {
        if (level != null && level.isClientSide && !clientMultipart.isEmpty()) {
            return true;
        }
        return !items.getStackInSlot(SLOT_WARHEAD).isEmpty()
                || !items.getStackInSlot(SLOT_FUSELAGE).isEmpty()
                || !items.getStackInSlot(SLOT_STABILITY).isEmpty()
                || !items.getStackInSlot(SLOT_THRUSTER).isEmpty();
    }

    @Override
    public void handleClientMissileMultipart(MissileMultipartSnapshot multipart) {
        clientMultipart = multipart == null ? MissileMultipartSnapshot.EMPTY : multipart;
        setChanged();
    }

    private int partState(int slot, MissilePartItem.PartType expectedType) {
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.getItem() instanceof MissilePartItem part && part.type() == expectedType
                && CustomMissilePartProfile.fromPartItem(part) != null) {
            return 1;
        }
        return 0;
    }

    private MissileMultipartSnapshot multipartSnapshot() {
        return MissileMultipartSnapshot.of(
                items.getStackInSlot(SLOT_WARHEAD),
                items.getStackInSlot(SLOT_FUSELAGE),
                items.getStackInSlot(SLOT_STABILITY),
                items.getStackInSlot(SLOT_THRUSTER));
    }

    @Nullable
    private static CustomMissilePartProfile.Assembly assemblyFromSnapshot(MissileMultipartSnapshot snapshot) {
        return new CustomMissilePartProfile.Assembly(
                null,
                resolve(snapshot.warhead(), MissilePartItem.PartType.WARHEAD),
                resolve(snapshot.fuselage(), MissilePartItem.PartType.FUSELAGE),
                resolve(snapshot.fins(), MissilePartItem.PartType.FINS),
                resolve(snapshot.thruster(), MissilePartItem.PartType.THRUSTER));
    }

    @Nullable
    private static CustomMissilePartProfile.ResolvedPart resolve(ResourceLocation id,
            MissilePartItem.PartType expectedType) {
        return CustomMissilePartProfile.resolve(id, expectedType);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == 0 && value == 0 && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        construct();
    }

    @Override
    public Component getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return Component.literal(customName);
        }
        return Component.translatable("container.missileAssembly");
    }

    /**
     * {@code MachineMissileAssembly#onBlockPlacedBy} copied a named block item's
     * display name into the tile entity. Keep the legacy string-NBT carrier used
     * by this machine rather than introducing a separate modern name component.
     */
    public void setCustomName(String name) {
        customName = name;
        setChanged();
        syncToClient();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MissileAssemblyMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        if (customName != null && !customName.isEmpty()) {
            tag.putString("name", customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        if (tag.contains(TAG_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        }
        customName = tag.getString("name");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeMultipartSnapshot(tag, multipartSnapshot());
        if (customName != null && !customName.isEmpty()) {
            tag.putString("name", customName);
        }
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        clientMultipart = readMultipartSnapshot(tag);
        if (tag.contains("name")) {
            customName = tag.getString("name");
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection,
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            handleClientSyncTag(packet.getTag());
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        externalItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // TileEntityMachineMissileAssembly used TileEntity.INFINITE_EXTENT_AABB.
        // Its multipart preview and struts have no source-bounded maximum extent,
        // so a guessed finite box can cull them before the renderer's existing
        // global 512-block distance guard runs.
        return LegacyMachineRenderBounds.INFINITE_EXTENT_AABB;
    }

    private void syncToClient() {
        Level currentLevel = level;
        if (currentLevel != null && !currentLevel.isClientSide) {
            BlockState state = getBlockState();
            currentLevel.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (currentLevel instanceof ServerLevel serverLevel) {
                ModMessages.sendToAllAround(ModMessages.missileMultipartPacket(worldPosition, multipartSnapshot()),
                        serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        MULTIPART_SYNC_RANGE);
            }
        }
    }

    private static void writeMultipartSnapshot(CompoundTag tag, MissileMultipartSnapshot multipart) {
        writeMultipartPart(tag, TAG_CLIENT_WARHEAD, multipart.warhead());
        writeMultipartPart(tag, TAG_CLIENT_FUSELAGE, multipart.fuselage());
        writeMultipartPart(tag, TAG_CLIENT_FINS, multipart.fins());
        writeMultipartPart(tag, TAG_CLIENT_THRUSTER, multipart.thruster());
    }

    private static void writeMultipartPart(CompoundTag tag, String key, @Nullable ResourceLocation id) {
        if (id != null) {
            tag.putString(key, id.toString());
        }
    }

    private static MissileMultipartSnapshot readMultipartSnapshot(CompoundTag tag) {
        return new MissileMultipartSnapshot(
                readMultipartPart(tag, TAG_CLIENT_WARHEAD),
                readMultipartPart(tag, TAG_CLIENT_FUSELAGE),
                readMultipartPart(tag, TAG_CLIENT_FINS),
                readMultipartPart(tag, TAG_CLIENT_THRUSTER));
    }

    @Nullable
    private static ResourceLocation readMultipartPart(CompoundTag tag, String key) {
        return tag.contains(key) ? ResourceLocation.tryParse(tag.getString(key)) : null;
    }

    /**
     * TileEntityMachineMissileAssembly exposed its legacy {@code access = {0}}
     * array on every side, while rejecting both insert and extract.  Keep that
     * observable one-slot surface without granting automation access to the
     * five other GUI-only slots.
     */
    private static final class MissileAssemblyExternalItemHandler implements IItemHandler {
        private final ItemStackHandler inventory;

        private MissileAssemblyExternalItemHandler(ItemStackHandler inventory) {
            this.inventory = inventory;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? inventory.getStackInSlot(SLOT_CHIP) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? inventory.getSlotLimit(SLOT_CHIP) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
