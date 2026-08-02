package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.Mk2PileDeviceBlock;
import com.hbm.ntm.block.Mk2PileStructureBlock;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.Mk2PileRodItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * Shared modern BE for the three old pile_device metadata families.  Its fields deliberately
 * retain the old loader/vent/control timers and NBT contract rather than simulating a generic machine.
 */
public final class Mk2PileDeviceBlockEntity extends HbmFluidBlockEntity
        implements LegacyLookOverlayProvider, RORValueProvider, RORInteractive {
    private static final double LOADER_SPEED = 1.0D / 7.0D;
    private static final double CONTROL_SPEED = 1.0D / 60.0D;
    private final HbmFluidTank compressedAir;
    private final ItemStackHandler loaderItems = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof Mk2PileRodItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            // TileEntityPileLoader#canExtractItem was always false: rods leave only through the actuator.
            return ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            changed();
        }
    };
    private final LazyOptional<IItemHandler> loaderItemHandler = LazyOptional.of(() -> loaderItems);
    private boolean loading;
    private boolean wasRedstone;
    private int delay;
    private double actuatorLevel;
    private double targetLevel;
    private boolean ventActive;
    private double renderLevel;
    private double renderTargetLevel;
    private double previousRenderLevel;
    private int renderInterpolationTicks;
    private float fanAngle;
    private float previousFanAngle;
    private boolean renderVentActive;
    private final RORDispatcher loaderRor = RORDispatcher.builder()
            .value("meta", this::loaderRodMeta)
            .value("depletion", this::loaderRodDepletion)
            .value("deppercent", this::loaderRodDepletionPercent)
            .value("lifetime", this::loaderRodLifetime)
            .value("temp", () -> Integer.toString((int) Math.round(getChannelHeat())))
            .build();
    private final RORDispatcher controlRor = RORDispatcher.builder()
            .function("setrods", this::setRods, "percent")
            .function("extendrods", this::extendRods, "percent")
            .build();

    public Mk2PileDeviceBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.AIR, 4_000).withPressure(1));
    }

    private Mk2PileDeviceBlockEntity(BlockPos pos, BlockState state, HbmFluidTank compressedAir) {
        super(ModBlockEntities.MK2_PILE_DEVICE.get(), pos, state, List.of(compressedAir));
        this.compressedAir = compressedAir;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, Mk2PileDeviceBlockEntity device) {
        if (level.isClientSide) {
            device.tickClient();
            return;
        }
        switch (device.kind()) {
            case LOADER -> device.tickLoader();
            case VENT -> device.tickVent();
            case CONTROL -> device.tickControl();
        }
    }

    public boolean useLoader(Player player, InteractionHand hand) {
        if (kind() != Mk2PileDeviceBlock.Kind.LOADER || level == null || level.isClientSide || actuatorLevel > 0.0D || loading) return true;
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && loaderItems.getStackInSlot(0).isEmpty() && held.getItem() instanceof Mk2PileRodItem) {
            loaderItems.setStackInSlot(0, held.copyWithCount(1));
            if (!player.getAbilities().instabuild) held.shrink(1);
            changed();
            return true;
        }
        loading = true;
        changed();
        return true;
    }

    public ItemStack getLoaderStack() { return loaderItems.getStackInSlot(0).copy(); }
    public ItemStack getChannelStack() { return coreAt(channelPos(), Mk2PileStructureBlock.Role.FUEL_IN).map(core -> core.lastFuelRod(channelPos())).orElse(ItemStack.EMPTY); }
    public double getChannelHeat() { return coreAt(channelPos(), Mk2PileStructureBlock.Role.FUEL_IN).map(core -> core.fuelHeat(channelPos())).orElse(0.0D); }
    public double getActuatorLevel() { return actuatorLevel; }
    public double getTargetLevel() { return targetLevel; }
    public boolean isVentActive() { return ventActive; }
    public HbmFluidTank compressedAirTank() { return compressedAir; }
    public double getRenderActuatorLevel(float partialTick) {
        return previousRenderLevel + (renderLevel - previousRenderLevel) * partialTick;
    }
    public float getRenderFanAngle(float partialTick) {
        return previousFanAngle + (fanAngle - previousFanAngle) * partialTick;
    }

    @Override
    public String[] getFunctionInfo() {
        return switch (kind()) {
            case LOADER -> loaderRor.getFunctionInfo();
            case CONTROL -> controlRor.getFunctionInfo();
            case VENT -> new String[0];
        };
    }

    @Override
    public String provideRORValue(String name) {
        return kind() == Mk2PileDeviceBlock.Kind.LOADER ? loaderRor.provideValue(name) : null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        return kind() == Mk2PileDeviceBlock.Kind.CONTROL ? controlRor.runFunction(name, params) : null;
    }

    /** Exact BlockPileDevice#printHook coverage; vent intentionally has no old constant overlay. */
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        if (!worldPosition.equals(viewedPos)) {
            return null;
        }
        return switch (kind()) {
            case LOADER -> loaderLookOverlay();
            case CONTROL -> LegacyLookOverlay.withTitle(deviceTitle(), List.of(Component.literal("Extraction level: "
                    + (int) (actuatorLevel * 100.0D) + "%")));
            case VENT -> null;
        };
    }

    private LegacyLookOverlay loaderLookOverlay() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Temp: " + Math.round(getChannelHeat()) + " / 800°C"));
        ItemStack loadingStack = getLoaderStack();
        if (!loadingStack.isEmpty()) {
            lines.add(Component.literal("Loading: ").append(loadingStack.getHoverName()));
        }
        ItemStack channelStack = getChannelStack();
        if (!channelStack.isEmpty()) {
            lines.add(Component.literal("Last rod: ").append(channelStack.getHoverName()));
            double depletion = Mk2PileRodItem.depletionPercent(channelStack);
            if (depletion > 0.0D) {
                lines.add(Component.literal("Depletion: " + Math.round(depletion) + "%"));
            }
        }
        return LegacyLookOverlay.withTitle(deviceTitle(), lines);
    }

    private Component deviceTitle() {
        return Component.translatable("block.hbm_ntm_rebirth.pile_device."
                + kind().getSerializedName());
    }

    private void tickClient() {
        previousRenderLevel = renderLevel;
        if (renderInterpolationTicks > 0) {
            renderLevel += (renderTargetLevel - renderLevel) / renderInterpolationTicks;
            renderInterpolationTicks--;
        } else {
            renderLevel = renderTargetLevel;
        }
        previousFanAngle = fanAngle;
        if (renderVentActive) fanAngle += 45.0F;
        if (fanAngle >= 360.0F) {
            previousFanAngle -= 360.0F;
            fanAngle -= 360.0F;
        }
    }

    private void tickLoader() {
        BlockPos channelPos = channelPos();
        boolean redstone = level.hasNeighborSignal(worldPosition.relative(facing()));
        if (redstone && !wasRedstone && delay <= 0 && actuatorLevel <= 0.0D) loading = true;
        wasRedstone = redstone;
        if (delay > 0) { delay--; changed(); return; }
        if (loading) {
            actuatorLevel = Math.min(1.0D, actuatorLevel + LOADER_SPEED);
            if (actuatorLevel >= 1.0D) { loading = false; delay = 5; }
        } else if (actuatorLevel > 0.0D) {
            if (actuatorLevel >= 1.0D && !loaderItems.getStackInSlot(0).isEmpty()) {
                coreAt(channelPos, Mk2PileStructureBlock.Role.FUEL_IN).ifPresent(core -> {
                    if (core.loadFuelRod(channelPos, loaderItems.getStackInSlot(0))) {
                        loaderItems.setStackInSlot(0, ItemStack.EMPTY);
                    }
                });
            }
            actuatorLevel = Math.max(0.0D, actuatorLevel - LOADER_SPEED);
        }
        changed();
    }

    private void tickVent() {
        BlockPos channelPos = channelPos();
        ventActive = coreAt(channelPos, Mk2PileStructureBlock.Role.AIR_IN).map(core -> {
            int moved = core.fillVentilation(channelPos, compressedAir.getFill());
            if (moved > 0) compressedAir.drain(moved, false);
            return moved > 0;
        }).orElse(false);
        if (ventActive) onFluidContentsChanged();
        changed();
    }

    private void tickControl() {
        BlockPos channelPos = worldPosition.below();
        boolean canMove = coreAt(channelPos, Mk2PileStructureBlock.Role.CONTROL).isPresent();
        if (canMove && actuatorLevel != targetLevel) {
            if (Math.abs(actuatorLevel - targetLevel) <= CONTROL_SPEED) actuatorLevel = targetLevel;
            else actuatorLevel += actuatorLevel < targetLevel ? CONTROL_SPEED : -CONTROL_SPEED;
        }
        coreAt(channelPos, Mk2PileStructureBlock.Role.CONTROL).ifPresent(core -> core.setControlLevel(channelPos, actuatorLevel));
        boolean redstone = level.hasNeighborSignal(worldPosition.relative(facing()));
        if (redstone && !wasRedstone) targetLevel = 1.0D;
        if (!redstone && wasRedstone) targetLevel = 0.0D;
        wasRedstone = redstone;
        changed();
    }

    private java.util.Optional<Mk2PileCoreBlockEntity> coreAt(BlockPos entry, Mk2PileStructureBlock.Role role) {
        if (level == null || !level.hasChunkAt(entry)) return java.util.Optional.empty();
        BlockState state = level.getBlockState(entry);
        if (!state.is(ModBlocks.PILE_BLOCK.get()) || state.getValue(Mk2PileStructureBlock.ROLE) != role) return java.util.Optional.empty();
        if (level.getBlockEntity(entry) instanceof Mk2PileCoreBlockEntity core) return java.util.Optional.of(core);
        if (level.getBlockEntity(entry) instanceof Mk2PileMemberBlockEntity member
                && level.getBlockEntity(member.corePos()) instanceof Mk2PileCoreBlockEntity core) return java.util.Optional.of(core);
        return java.util.Optional.empty();
    }

    private BlockPos channelPos() { return worldPosition.relative(facing().getOpposite()); }
    private Direction facing() { return getBlockState().getValue(Mk2PileDeviceBlock.FACING); }
    private Mk2PileDeviceBlock.Kind kind() { return getBlockState().getValue(Mk2PileDeviceBlock.KIND); }

    private String loaderRodMeta() {
        ItemStack rod = getChannelStack();
        return Integer.toString(rod.isEmpty() ? -1 : rod.getDamageValue());
    }

    private String loaderRodDepletion() {
        ItemStack rod = getChannelStack();
        return rod.isEmpty() ? "0" : Integer.toString((int) Math.round(Mk2PileRodItem.depletionPercent(rod)));
    }

    private String loaderRodDepletionPercent() {
        return loaderRodDepletion();
    }

    private String loaderRodLifetime() {
        ItemStack rod = getChannelStack();
        return rod.isEmpty() ? "0" : Integer.toString((int) Math.round(Mk2PileRodItem.lifetime(rod)));
    }

    private String setRods(String[] params) {
        if (params.length > 0) {
            targetLevel = RORInteractive.parseInt(params[0], 0, 100) / 100.0D;
            changed();
        }
        return null;
    }

    private String extendRods(String[] params) {
        if (params.length > 0) {
            int percent = RORInteractive.parseInt(params[0], -100, 100);
            targetLevel = Math.max(0.0D, Math.min(1.0D, targetLevel + percent / 100.0D));
            changed();
        }
        return null;
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return kind() == Mk2PileDeviceBlock.Kind.VENT && (side == null || side == facing()) ? List.of(compressedAir) : List.of();
    }
    @Override protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) { return List.of(); }
    @Override protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return kind() == Mk2PileDeviceBlock.Kind.VENT && (side == null || side == facing()) ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }
    @Override protected boolean showsLegacyFluidLookOverlay() { return kind() == Mk2PileDeviceBlock.Kind.VENT; }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        loaderItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && kind() == Mk2PileDeviceBlock.Kind.LOADER) {
            return loaderItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("loading", loading); tag.putDouble("level", actuatorLevel); tag.putDouble("targetLevel", targetLevel);
        tag.putInt("delay", delay); tag.putBoolean("redstone", wasRedstone); compressedAir.writeToNbt(tag, "t");
        if (!loaderItems.getStackInSlot(0).isEmpty()) tag.put("stack", loaderItems.getStackInSlot(0).save(new CompoundTag()));
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        loading = tag.getBoolean("loading"); actuatorLevel = tag.getDouble("level"); targetLevel = tag.getDouble("targetLevel");
        delay = tag.getInt("delay"); wasRedstone = tag.getBoolean("redstone"); compressedAir.setTankType(HbmFluids.AIR); compressedAir.withPressure(1);
        if (tag.contains("t")) compressedAir.readFromNbt(tag, "t");
        loaderItems.setStackInSlot(0, tag.contains("stack") ? ItemStack.of(tag.getCompound("stack")) : ItemStack.EMPTY);
        if (tag.contains("active")) renderVentActive = tag.getBoolean("active");
    }

    @Override public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean("active", ventActive);
        return tag;
    }

    @Override public void handleClientSyncTag(CompoundTag tag) {
        double previousTarget = renderTargetLevel;
        double previousLevel = renderLevel;
        super.handleClientSyncTag(tag);
        renderTargetLevel = actuatorLevel;
        if (renderTargetLevel != previousTarget) {
            renderLevel = previousLevel;
            renderInterpolationTicks = 2;
        }
    }
}
