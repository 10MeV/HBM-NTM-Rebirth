package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.menu.FelMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.BufferUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmRegistryUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class FelBlockEntity extends HbmEnergyBlockEntity implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_CRYSTAL = 1;
    public static final int SLOT_COUNT = 2;
    public static final int CONTROL_POWER = 2;
    public static final long MAX_POWER = 20_000_000L;
    public static final int POWER_REQ = 1_250;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_NAME = "name";
    private static final String TAG_POWER = "power";
    private static final String TAG_MODE = "mode";
    private static final String TAG_ON = "isOn";
    private static final String TAG_VALID = "valid";
    private static final String TAG_DISTANCE = "distance";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                case SLOT_CRYSTAL -> wavelengthFor(stack) != LaserWavelength.NULL;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private LaserWavelength mode = LaserWavelength.NULL;
    private boolean on;
    private boolean missingValidSilex = true;
    private int distance;
    private int audioDuration;
    private Object audioLoop;
    @Nullable
    private String customName;

    public FelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FEL.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FelBlockEntity fel) {
        if (level.isClientSide) {
            return;
        }
        fel.subscribeEnergyReceiverToPorts();
        long oldPower = fel.energy.getPower();
        LaserWavelength oldMode = fel.mode;
        boolean oldValid = fel.missingValidSilex;
        int oldDistance = fel.distance;
        HbmEnergyUtil.chargeStorageFromItem(fel.items.getStackInSlot(SLOT_BATTERY), fel.energy, fel.energy.getReceiverSpeed());
        fel.mode = fel.on ? wavelengthFor(fel.items.getStackInSlot(SLOT_CRYSTAL)) : LaserWavelength.NULL;
        int request = fel.powerRequest();
        if (fel.on && fel.mode != LaserWavelength.NULL && fel.energy.getPower() < request) {
            // TileEntityFEL deliberately discards a partial charge when an enabled
            // laser cannot pay its complete legacy per-tick request.
            fel.energy.setPower(0L);
        }
        if (fel.on && fel.mode != LaserWavelength.NULL && fel.energy.getPower() >= request) {
            Direction facing = state.getValue(HorizontalMachineBlock.FACING);
            // The old tile applies hazards using the last scan result, before the
            // current tick consumes energy and scans the next obstruction.
            fel.hurtEntities(level, facing, fel.distance - 1);
            fel.energy.setPower(fel.energy.getPower() - request);
            fel.scanBeam(level, facing);
        }
        // TileEntityFEL broadcasts its renderer/audio state through networkPackNT(250) every tick.
        // The packet layer suppresses unchanged snapshots except for the legacy keepalive cadence.
        fel.networkPackNT(250);
        if (oldPower != fel.energy.getPower() || oldMode != fel.mode || oldValid != fel.missingValidSilex
                || oldDistance != fel.distance) {
            fel.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FelBlockEntity fel) {
        boolean active = fel.isBeamActive();
        fel.audioDuration += active ? 2 : -3;
        fel.audioDuration = Mth.clamp(fel.audioDuration, 0, 60);
        float volume = fel.audioDuration > 10 ? 2.0F : 0.0F;
        float pitch = (fel.audioDuration - 10) / 100.0F + 0.5F;
        fel.audioLoop = LegacyMachineAudioBridge.updateLoop(fel.audioLoop, fel,
                "hbm:block.fel", fel.audioDuration > 10, 25.0D, 10.0F, volume, pitch);
    }

    private void scanBeam(Level level, Direction facing) {
        int range = 24;
        boolean silexSpacing = false;
        for (int i = 3; i < range; i++) {
            BlockPos beamPos = worldPosition.relative(facing, i).above();
            BlockState beamState = level.getBlockState(beamPos);
            // TileEntityFEL uses Material#isOpaque, not collision.  In
            // particular, glass must not stop the beam merely because it has a
            // collision shape.  isSolidRender is the modern state-level
            // equivalent for this legacy opaque-material gate.
            // SILEX used Material.iron in 1.7.10 and therefore passed the
            // legacy opaque-material gate.  Its 1.20.1 visual block is
            // intentionally no-occlusion so the imported model can render,
            // which must not make the beam skip it before the SILEX branch.
            boolean isSilex = beamState.is(ModBlocks.MACHINE_SILEX.get());
            if (!isSilex && !beamState.isSolidRender(level, beamPos) && !beamState.is(Blocks.TNT)) {
                distance = range;
                silexSpacing = false;
                continue;
            }
            if (isSilex) {
                // The illuminated SILEX dummy is one block above the core;
                // TileEntityFEL looks up the core one block farther along the
                // beam at the FEL core's Y level.  Looking up only at beam Y
                // makes every valid modern SILEX invisible to the laser.
                BlockEntity behind = level.getBlockEntity(beamPos.relative(facing).below());
                if (behind instanceof SilexBlockEntity silex) {
                    Direction silexFacing = silex.getBlockState().getValue(HorizontalMachineBlock.FACING);
                    boolean validRotation = silexFacing == facing || silexFacing == facing.getOpposite();
                    if (validRotation && i >= 5 && !silexSpacing) {
                        if (silex.getMode() != mode) {
                            silex.acceptLaser(facing, mode);
                            missingValidSilex = false;
                            silexSpacing = true;
                        }
                    } else {
                        // The legacy machine self-destructs a malformed inline
                        // SILEX assembly and emits precisely one replacement item.
                        level.destroyBlock(silex.getBlockPos(), false);
                        level.addFreshEntity(new ItemEntity(level, beamPos.getX() + 0.5D, beamPos.getY() + 0.5D,
                                beamPos.getZ() + 0.5D, new ItemStack(ModBlocks.MACHINE_SILEX.get())));
                    }
                }
                continue;
            }
            distance = i;
            if (beamState.getExplosionResistance(level, beamPos, null) < 75.0F && level.random.nextInt(5) == 0) {
                LegacySoundPlayer.playSoundEffect(level, beamPos, "random.fizz", 1.0F, 1.0F);
                BlockState fire = mode == LaserWavelength.DRX
                        ? ModBlocks.FIRE_DIGAMMA.get().defaultBlockState()
                        : Blocks.FIRE.defaultBlockState();
                level.setBlock(beamPos, fire, Block.UPDATE_ALL);
                if (mode == LaserWavelength.DRX) {
                    level.setBlock(beamPos.below(), ModBlocks.ASH_DIGAMMA.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            break;
        }
    }

    private void hurtEntities(Level level, Direction facing, int beamDistance) {
        if (mode == LaserWavelength.NULL) {
            return;
        }
        double endX = worldPosition.getX() + facing.getStepX() * beamDistance;
        double endZ = worldPosition.getZ() + facing.getStepZ() * beamDistance;
        AABB box = new AABB(Math.min(worldPosition.getX(), endX) + 0.2D, worldPosition.getY() + 0.2D,
                Math.min(worldPosition.getZ(), endZ) + 0.2D, Math.max(worldPosition.getX(), endX) + 0.8D,
                worldPosition.getY() + 1.8D, Math.max(worldPosition.getZ(), endZ) + 0.8D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            switch (mode) {
                // TileEntityFEL intentionally falls through from VISIBLE into IR in 1.7.10.
                case VISIBLE -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60 * 20, 0));
                    entity.setSecondsOnFire(10);
                }
                case IR, UV -> entity.setSecondsOnFire(10);
                case GAMMA -> RadiationUtil.contaminate(entity, HazardType.RADIATION,
                        ContaminationType.CREATIVE, 25.0F);
                case DRX -> RadiationUtil.applyDigammaData(entity, 0.1F);
                default -> {
                }
            }
        }
    }

    public int powerRequest() {
        return mode == LaserWavelength.NULL ? 0 : (int) (POWER_REQ * Math.pow(3, mode.ordinal()));
    }

    public static long visualBeamPowerRequest(int modeOrdinal) {
        return (long) (POWER_REQ * Math.pow(2, modeOrdinal));
    }

    public static long visualBeamPowerRequest(LaserWavelength wavelength) {
        return visualBeamPowerRequest(wavelength.ordinal());
    }

    public static LaserWavelength wavelengthFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return LaserWavelength.NULL;
        }
        return LaserWavelength.byLegacyItemName(HbmRegistryUtil.itemKey(stack.getItem()).getPath());
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public LaserWavelength getMode() {
        return mode;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isMissingValidSilex() {
        return missingValidSilex;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isBeamActive() {
        return on && mode != LaserWavelength.NULL && energy.getPower() > visualBeamPowerRequest(mode) && distance > 3;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    protected Iterable<HbmEnergyUtil.EnergyPort> getEnergyPorts() {
        Direction facing = getBlockState().getValue(HorizontalMachineBlock.FACING);
        return List.of(new HbmEnergyUtil.EnergyPort(new BlockPos(-facing.getStepX() * 5, 1,
                -facing.getStepZ() * 5), facing.getOpposite()));
    }

    @Override
    protected boolean isEnergyPortKeepalive() {
        // TileEntityFEL retries its single remote Energy Mk2 receiver every
        // legacy server tick.
        return true;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineFEL", "FEL");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FelMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_POWER
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 64.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_POWER) {
            on = !on;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_NAME, customName);
        }
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putString(TAG_MODE, mode.name());
        tag.putBoolean(TAG_ON, on);
        tag.putBoolean(TAG_VALID, missingValidSilex);
        tag.putInt(TAG_DISTANCE, distance);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_NAME, Tag.TAG_STRING) ? tag.getString(TAG_NAME) : null;
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        mode = tag.contains(TAG_MODE, Tag.TAG_STRING) ? safeWavelength(tag.getString(TAG_MODE)) : LaserWavelength.NULL;
        on = tag.getBoolean(TAG_ON);
        missingValidSilex = tag.getBoolean(TAG_VALID);
        distance = tag.getInt(TAG_DISTANCE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return new CompoundTag();
}

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        // TileEntityFEL#serialize: TileEntityMachineBase inherits the two LoadedBase flags, then FEL state.
        // Its inventory is NBT-only and deliberately does not belong in this runtime visual/audio packet.
        writeLegacyLoadedTileBinary(data);
        data.writeLong(energy.getPower());
        BufferUtil.writeString(data, mode.name());
        data.writeBoolean(on);
        data.writeBoolean(missingValidSilex);
        data.writeInt(distance);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        energy.setPower(data.readLong());
        String modeName = BufferUtil.readString(data);
        mode = safeWavelength(modeName == null ? LaserWavelength.NULL.name() : modeName);
        on = data.readBoolean();
        missingValidSilex = data.readBoolean();
        distance = Math.max(0, data.readInt());
    }

    private static LaserWavelength safeWavelength(String name) {
        try {
            return LaserWavelength.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return LaserWavelength.NULL;
        }
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST) || tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
            return;
        }
        if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
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
}
