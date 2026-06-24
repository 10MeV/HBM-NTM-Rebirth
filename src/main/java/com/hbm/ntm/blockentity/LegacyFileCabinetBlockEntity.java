package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.item.KeyPinItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.menu.FileCabinetMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyFileCabinetBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 8;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int timer;
    private int playersUsing;
    private float lowerExtent;
    private float prevLowerExtent;
    private float upperExtent;
    private float prevUpperExtent;
    private String customName;
    private int lockPins;
    private boolean locked;
    private double lockMod = 0.1D;
    private boolean cheesable = true;

    public LegacyFileCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_FILE_CABINET.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyFileCabinetBlockEntity cabinet) {
        cabinet.tick();
    }

    private void tick() {
        if (level == null) {
            return;
        }
        if (!level.isClientSide) {
            if (playersUsing > 0) {
                if (timer < 10) {
                    timer++;
                }
            } else {
                timer = 0;
            }
        } else {
            prevLowerExtent = lowerExtent;
            prevUpperExtent = upperExtent;
        }

        float openSpeed = playersUsing > 0 ? 1.0F / 16.0F : 1.0F / 25.0F;
        float maxExtent = 0.8F;

        if (playersUsing > 0) {
            if (lowerExtent == 0.0F && upperExtent == 0.0F) {
                playOpenSound(0.8F, 1.0F);
            } else {
                if (upperExtent + openSpeed >= maxExtent && lowerExtent < maxExtent) {
                    playOpenSound(0.5F, randomPitch(0.7F));
                }
                if (lowerExtent + openSpeed >= maxExtent && lowerExtent < maxExtent) {
                    playOpenSound(0.5F, randomPitch(0.7F));
                }
            }
            lowerExtent += openSpeed;
            if (timer >= 10) {
                upperExtent += openSpeed;
            }
        } else if (lowerExtent > 0.0F) {
            if (upperExtent - openSpeed < maxExtent / 2.0F && upperExtent >= maxExtent / 2.0F
                    && upperExtent != lowerExtent) {
                playCloseSound(0.8F, 1.0F);
            }
            if (lowerExtent - openSpeed < maxExtent / 2.0F && lowerExtent >= maxExtent / 2.0F) {
                playCloseSound(0.8F, 1.0F);
            }
            upperExtent -= openSpeed;
            lowerExtent -= openSpeed;
        }

        lowerExtent = net.minecraft.util.Mth.clamp(lowerExtent, 0.0F, maxExtent);
        upperExtent = net.minecraft.util.Mth.clamp(upperExtent, 0.0F, maxExtent);
        if (!level.isClientSide) {
            syncVisualState();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public float lowerExtent(float partialTick) {
        return prevLowerExtent + (lowerExtent - prevLowerExtent) * partialTick;
    }

    public float upperExtent(float partialTick) {
        return prevUpperExtent + (upperExtent - prevUpperExtent) * partialTick;
    }

    public int variant() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyFileCabinetBlock.VARIANT) ? state.getValue(LegacyFileCabinetBlock.VARIANT) : 0;
    }

    public void openInventory() {
        if (level != null && !level.isClientSide) {
            playersUsing++;
            syncVisualState();
        }
    }

    public void closeInventory() {
        if (level != null && !level.isClientSide) {
            playersUsing = Math.max(0, playersUsing - 1);
            syncVisualState();
        }
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = HbmItemStackUtil.clearToDrops(items);
        setChanged();
        return drops;
    }

    public boolean tryApplyPadlock(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof PadlockItem padlock) || locked || KeyPinItem.getPins(stack) == 0) {
            return false;
        }
        lockPins = KeyPinItem.getPins(stack);
        locked = true;
        lockMod = padlock.lockMod();
        setChangedAndUpdate();
        if (player != null && level != null) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:block.lockHang", 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    public boolean tryCreateCounterfeitKeys(Player player, InteractionHand hand) {
        if (!locked || player == null) {
            return false;
        }
        if (!cheesable) {
            player.displayClientMessage(Component.literal(
                    "This lock is too elaborate for a counterfeit key to be made"), false);
            player.displayClientMessage(Component.literal(
                    "Perhaps there is another way around here to unlock it"), false);
            return true;
        }
        ItemStack first = new ItemStack(ModItems.KEY_FAKE.get());
        KeyPinItem.setPins(first, lockPins);
        ItemStack second = first.copy();
        player.setItemInHand(hand, first);
        if (!player.getInventory().add(second)) {
            player.drop(second, false);
        }
        player.swing(hand, true);
        return true;
    }

    public boolean canAccess(Player player, ItemStack held) {
        if (!locked) {
            return true;
        }
        if (!held.isEmpty() && (held.is(ModItems.KEY.get()) || held.is(ModItems.KEY_FAKE.get()))
                && KeyPinItem.getPins(held) == lockPins) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:block.lockOpen", 1.0F, 1.0F);
            return true;
        }
        return tryPick(player, held);
    }

    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
            customName = tag.contains("name") ? tag.getString("name") : null;
            lockPins = tag.getInt("lock");
            locked = tag.getBoolean("isLocked");
            lockMod = tag.contains("lockMod") ? tag.getDouble("lockMod") : 0.1D;
            cheesable = !tag.contains("cheesable") || tag.getBoolean("cheesable");
        }
        if (stack.hasCustomHoverName()) {
            customName = stack.getHoverName().getString();
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.fileCabinet");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FileCabinetMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString("name", customName);
        }
        tag.putInt("lock", lockPins);
        tag.putBoolean("cheesable", cheesable);
        tag.putBoolean("isLocked", locked);
        tag.putDouble("lockMod", lockMod);
        tag.putInt("timer", timer);
        tag.putInt("playersUsing", playersUsing);
        tag.putFloat("lowerExtent", lowerExtent);
        tag.putFloat("upperExtent", upperExtent);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
        customName = tag.contains("name") ? tag.getString("name") : null;
        lockPins = tag.getInt("lock");
        locked = tag.getBoolean("isLocked");
        lockMod = tag.contains("lockMod") ? tag.getDouble("lockMod") : 0.1D;
        cheesable = !tag.contains("cheesable") || tag.getBoolean("cheesable");
        timer = tag.getInt("timer");
        playersUsing = tag.getInt("playersUsing");
        lowerExtent = tag.getFloat("lowerExtent");
        upperExtent = tag.getFloat("upperExtent");
        prevLowerExtent = lowerExtent;
        prevUpperExtent = upperExtent;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(1, 1, 1));
    }

    private void syncVisualState() {
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private boolean tryPick(Player player, ItemStack held) {
        if (player == null || level == null) {
            return false;
        }
        boolean canPick = false;
        double chanceOfSuccess = lockMod * 100.0D;

        if (!held.isEmpty() && held.is(ModItems.PIN.get()) && hasScrewdriver(player)) {
            held.shrink(1);
            canPick = true;
        } else if (!held.isEmpty() && held.is(ModItems.SCREWDRIVER.get()) && consumeOnePin(player)) {
            canPick = true;
        }

        if (!canPick) {
            return false;
        }

        if (isWearingLockpickJacket(player)) {
            chanceOfSuccess *= 100.0D;
        }

        if (chanceOfSuccess > level.random.nextDouble() * 100.0D) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:item.pinUnlock", 1.0F, 1.0F);
            return true;
        }

        LegacySoundPlayer.playSoundAtPlayer(player, "hbm:item.pinBreak", 1.0F,
                0.8F + level.random.nextFloat() * 0.2F);
        return false;
    }

    private static boolean hasScrewdriver(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SCREWDRIVER.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOnePin(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.PIN.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean isWearingLockpickJacket(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(ModItems.JACKET.get()) || chest.is(ModItems.JACKET2.get());
    }

    private void setChangedAndUpdate() {
        setChanged();
        syncVisualState();
    }

    private float randomPitch(float base) {
        return base + (level == null ? 0.0F : level.random.nextFloat() * 0.1F);
    }

    private void playOpenSound(float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyCrateOpen(level, worldPosition, volume, pitch);
        }
    }

    private void playCloseSound(float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyCrateClose(level, worldPosition, volume, pitch);
        }
    }
}
