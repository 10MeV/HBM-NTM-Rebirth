package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.menu.FileCabinetMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
            customName = tag.contains("name") ? tag.getString("name") : null;
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
