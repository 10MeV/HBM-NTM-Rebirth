package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class ConveyorPressBlockEntity extends HbmEnergyBlockEntity implements LegacyLookOverlayProvider {
    public static final long MAX_POWER = 50_000L;
    public static final long USAGE = 100L;
    public static final double SPEED = 0.125D;

    private static final String TAG_POWER = "power";
    private static final String TAG_PRESS = "press";
    private static final String TAG_STAMP = "Stamp";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SYNC_PRESS = "SyncPress";
    private static final String TAG_SYNC_STAMP = "SyncStamp";
    private static final int SLOT_STAMP = 0;
    private static final List<EnergyPort> ENERGY_PORTS = List.of(
            EnergyPort.of(1, 0, 0, Direction.EAST),
            EnergyPort.of(-1, 0, 0, Direction.WEST),
            EnergyPort.of(0, 0, 1, Direction.SOUTH),
            EnergyPort.of(0, 0, -1, Direction.NORTH));

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemPressStamp;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private double press;
    private double renderPress;
    private double lastPress;
    private double syncPress;
    private int turnProgress;
    private boolean retracting;
    private int delay;

    public ConveyorPressBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CONVEYOR_PRESS.get(), pos, state);
    }

    protected ConveyorPressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ConveyorPressBlockEntity press) {
        press.subscribeEnergyReceiverToPorts();
        if (press.delay <= 0) {
            if (press.retracting) {
                if (press.canRetract()) {
                    press.press -= SPEED;
                    press.energy.setPower(press.energy.getPower() - USAGE);
                    if (press.press <= 0.0D) {
                        press.press = 0.0D;
                        press.retracting = false;
                        press.delay = 0;
                    }
                    press.setChangedAndSync();
                }
            } else if (press.canExtend()) {
                press.press += SPEED;
                press.energy.setPower(press.energy.getPower() - USAGE);
                if (press.press >= 1.0D) {
                    press.press = 1.0D;
                    press.retracting = true;
                    press.delay = 5;
                    press.process();
                }
                press.setChangedAndSync();
            }
        } else {
            press.delay--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ConveyorPressBlockEntity press) {
        press.lastPress = press.renderPress;
        if (press.turnProgress > 0) {
            press.renderPress += (press.syncPress - press.renderPress) / press.turnProgress;
            press.turnProgress--;
        } else {
            press.renderPress = press.syncPress;
        }
    }

    public ItemStack getStamp() {
        return items.getStackInSlot(SLOT_STAMP);
    }

    public boolean installStamp(ItemStack held, boolean creative) {
        if (!(held.getItem() instanceof ItemPressStamp) || !getStamp().isEmpty()) {
            return false;
        }
        ItemStack stamp = held.copy();
        stamp.setCount(1);
        items.setStackInSlot(SLOT_STAMP, stamp);
        if (!creative) {
            held.shrink(1);
        }
        setChangedAndSync();
        return true;
    }

    public boolean removeStamp(Player player, BlockPos clickedPos) {
        ItemStack stamp = getStamp().copy();
        if (stamp.isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
        if (!player.getInventory().add(stamp)) {
            Block.popResource(level, clickedPos, stamp);
        } else {
            player.containerMenu.broadcastChanges();
        }
        setChangedAndSync();
        return true;
    }

    public double getInterpolatedPress(float partialTick) {
        return lastPress + (renderPress - lastPress) * partialTick;
    }

    public double getLastPress() {
        return lastPress;
    }

    public double getRenderPress() {
        return renderPress;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmEnergySideMode.NONE : HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return ENERGY_PORTS;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        ItemStack stamp = getStamp();
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal(LegacyLookOverlayLines.shortNumber(getPower()) + "HE / "
                        + LegacyLookOverlayLines.shortNumber(getMaxPower()) + "HE"),
                Component.literal("Installed stamp: ")
                        .append(stamp.isEmpty()
                                ? Component.literal("NONE").withStyle(net.minecraft.ChatFormatting.RED)
                                : stamp.getHoverName())));
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 1.0D, pos.getY(), pos.getZ() - 1.0D,
                pos.getX() + 2.0D, pos.getY() + 3.0D, pos.getZ() + 2.0D);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putDouble(TAG_SYNC_PRESS, press);
        ItemStack stamp = getStamp();
        if (!stamp.isEmpty()) {
            tag.put(TAG_SYNC_STAMP, stamp.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        syncPress = tag.getDouble(TAG_SYNC_PRESS);
        if (tag.contains(TAG_SYNC_STAMP)) {
            items.setStackInSlot(SLOT_STAMP, ItemStack.of(tag.getCompound(TAG_SYNC_STAMP)));
        } else {
            items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
        }
        turnProgress = 2;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        handleClientSyncTag(packet.getTag());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putDouble(TAG_PRESS, press);
        tag.put(TAG_ITEMS, items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        press = tag.getDouble(TAG_PRESS);
        if (tag.contains(TAG_ITEMS)) {
            items.deserializeNBT(tag.getCompound(TAG_ITEMS));
        } else if (tag.contains(TAG_STAMP)) {
            items.setStackInSlot(SLOT_STAMP, ItemStack.of(tag.getCompound(TAG_STAMP)));
        }
        syncPress = press;
        renderPress = press;
        lastPress = press;
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

    private boolean canExtend() {
        if (energy.getPower() < USAGE || getStamp().isEmpty()) {
            return false;
        }
        for (MovingItemEntity item : pressableItems()) {
            ItemStack stack = item.getItemStack();
            if (stack.getCount() == 1 && findRecipe(stack, getStamp()) != null) {
                if (item.getX() > worldPosition.getX() + 0.35D && item.getX() < worldPosition.getX() + 0.65D
                        && item.getZ() > worldPosition.getZ() + 0.35D && item.getZ() < worldPosition.getZ() + 0.65D) {
                    item.setPos(worldPosition.getX() + 0.5D, item.getY(), worldPosition.getZ() + 0.5D);
                }
                return true;
            }
        }
        return false;
    }

    private boolean canRetract() {
        return energy.getPower() >= USAGE;
    }

    private void process() {
        for (MovingItemEntity item : pressableItems()) {
            ItemStack stack = item.getItemStack();
            PressRecipe recipe = findRecipe(stack, getStamp());
            if (recipe != null && stack.getCount() == 1) {
                ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
                item.discard();
                MovingItemEntity out = new MovingItemEntity(level, output);
                out.moveTo(item.getX(), item.getY(), item.getZ(), 0.0F, 0.0F);
                level.addFreshEntity(out);
            }
        }
        LegacySoundPlayer.playLegacyPressOperate(level, worldPosition, 1.5F, 1.0F);
        damageStamp();
    }

    private List<MovingItemEntity> pressableItems() {
        if (level == null) {
            return List.of();
        }
        AABB box = new AABB(worldPosition.getX(), worldPosition.getY() + 1.0D, worldPosition.getZ(),
                worldPosition.getX() + 1.0D, worldPosition.getY() + 1.5D, worldPosition.getZ() + 1.0D);
        return level.getEntitiesOfClass(MovingItemEntity.class, box, entity -> !entity.isRemoved());
    }

    @Nullable
    private PressRecipe findRecipe(ItemStack input, ItemStack stamp) {
        if (level == null || input.isEmpty() || stamp.isEmpty()) {
            return null;
        }
        SimpleContainer container = new SimpleContainer(input.copy(), stamp.copy());
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.PRESS.type().get()).stream()
                .sorted(Comparator.comparingInt(PressRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .filter(recipe -> recipe.matches(container, level))
                .findFirst()
                .orElse(null);
    }

    private void damageStamp() {
        ItemStack stamp = getStamp();
        if (!stamp.isEmpty() && stamp.isDamageableItem()) {
            stamp.hurt(1, level.random, null);
            if (stamp.getDamageValue() >= stamp.getMaxDamage()) {
                items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
            } else {
                setChangedAndSync();
            }
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
