package com.hbm.ntm.blockentity;

import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.network.HbmClientMissileMultipartReceiver;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.network.MissileMultipartSnapshot;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MissileAssemblyBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyButtonReceiver,
        HbmClientMissileMultipartReceiver {
    private static final String TAG_INVENTORY = "Inventory";
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
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (!(stack.getItem() instanceof MissilePartItem part)) {
                return false;
            }
            return switch (slot) {
                case SLOT_CHIP -> part.type() == MissilePartItem.PartType.CHIP;
                case SLOT_WARHEAD -> part.type() == MissilePartItem.PartType.WARHEAD;
                case SLOT_FUSELAGE -> part.type() == MissilePartItem.PartType.FUSELAGE;
                case SLOT_STABILITY -> part.type() == MissilePartItem.PartType.FINS;
                case SLOT_THRUSTER -> part.type() == MissilePartItem.PartType.THRUSTER;
                default -> false;
            };
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

    public MissileAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISSILE_ASSEMBLY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MissileAssemblyBlockEntity assembly) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            ModMessages.sendToAllAround(ModMessages.missileMultipartPacket(pos, assembly.multipartSnapshot()),
                    serverLevel, pos, MULTIPART_SYNC_RANGE);
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
        CompoundTag tag = saveWithoutMetadata();
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        if (customName != null && !customName.isEmpty()) {
            tag.putString("name", customName);
        }
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    private void syncToClient() {
        Level currentLevel = level;
        if (currentLevel != null && !currentLevel.isClientSide) {
            BlockState state = getBlockState();
            currentLevel.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (currentLevel instanceof ServerLevel serverLevel) {
                ModMessages.sendToAllAround(ModMessages.missileMultipartPacket(worldPosition, multipartSnapshot()),
                        serverLevel, worldPosition, MULTIPART_SYNC_RANGE);
            }
        }
    }
}
