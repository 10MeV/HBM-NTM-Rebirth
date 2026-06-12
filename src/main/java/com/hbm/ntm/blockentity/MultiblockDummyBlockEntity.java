package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmForgeFluidInterop;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiblockDummyBlockEntity extends BlockEntity implements HbmEnergyConnector, HbmFluidConnector,
        HbmFluidReceiver, RORValueProvider, RORInteractive {
    private static final String TAG_CORE_POS = "CorePos";
    private static final String TAG_LEGACY_TARGET_X = "tx";
    private static final String TAG_LEGACY_TARGET_Y = "ty";
    private static final String TAG_LEGACY_TARGET_Z = "tz";
    private static final String TAG_PROXY = "Proxy";
    private static final String TAG_PROXY_INVENTORY = "ProxyInventory";
    private static final String TAG_PROXY_POWER = "ProxyPower";
    private static final String TAG_PROXY_CONDUCTOR = "ProxyConductor";
    private static final String TAG_PROXY_FLUID = "ProxyFluid";
    private static final String TAG_PROXY_HEAT = "ProxyHeat";
    private static final String TAG_PROXY_MOLTEN_METAL = "ProxyMoltenMetal";
    private static final String TAG_PROXY_ALL = "ProxyAll";
    private static final String TAG_LEGACY_EXTRA = "LegacyExtra";

    @Nullable
    private BlockPos corePos;
    private LegacyProxyMode proxyMode = LegacyProxyMode.none();
    private boolean legacyExtra;
    private boolean dropCoreOnRemoval;

    public MultiblockDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_DUMMY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MultiblockDummyBlockEntity blockEntity) {
        if (blockEntity.corePos == null) {
            level.removeBlock(pos, false);
            return;
        }
        if (!HbmRegistryUtil.hasChunkAt(level, blockEntity.corePos)) {
            return;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, blockEntity.corePos);
        if (!MultiblockHelper.ownsDummy(level, core, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Nullable
    public BlockPos getCorePos() {
        return corePos;
    }

    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos.immutable();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void configure(BlockPos corePos, LegacyProxyMode proxyMode, boolean legacyExtra) {
        this.corePos = corePos.immutable();
        this.proxyMode = proxyMode == null ? LegacyProxyMode.none() : proxyMode;
        this.legacyExtra = legacyExtra;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean isProxy() {
        return proxyMode.isProxy();
    }

    public void setProxy(boolean proxy) {
        setProxyMode(proxy ? LegacyProxyMode.fullCombo() : LegacyProxyMode.none());
    }

    public LegacyProxyMode getProxyMode() {
        return proxyMode;
    }

    public boolean isLegacyExtra() {
        return legacyExtra;
    }

    public void setLegacyExtra(boolean legacyExtra) {
        this.legacyExtra = legacyExtra;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void setProxyMode(LegacyProxyMode proxyMode) {
        this.proxyMode = proxyMode == null ? LegacyProxyMode.none() : proxyMode;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        if (side == null || !proxyMode.isProxy() || (!proxyMode.power() && !proxyMode.conductor()
                && !proxyMode.allCapabilities())) {
            return false;
        }
        MultiblockHelper.CoreLookup core = validCore();
        if (core == null || level == null) {
            return false;
        }
        BlockEntity coreEntity = level.getBlockEntity(core.pos());
        ICapabilityProvider target = legacyProxyTarget(coreEntity);
        if (target instanceof HbmEnergyConnector connector) {
            return connector.canConnectEnergy(side);
        }
        return proxyMode.allows(ForgeCapabilities.ENERGY)
                && target != null
                && target.getCapability(ForgeCapabilities.ENERGY, side).isPresent();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        if (side == null || type == null || type == HbmFluids.NONE || !proxyMode.isProxy()
                || (!proxyMode.fluid() && !proxyMode.moltenMetal() && !proxyMode.allCapabilities())) {
            return false;
        }
        MultiblockHelper.CoreLookup core = validCore();
        if (core == null || level == null) {
            return false;
        }
        BlockEntity coreEntity = level.getBlockEntity(core.pos());
        ICapabilityProvider target = legacyProxyTarget(coreEntity);
        if (target instanceof HbmFluidConnector connector) {
            return connector.canConnectFluid(type, side);
        }
        return proxyMode.allows(ForgeCapabilities.FLUID_HANDLER)
                && target != null
                && target.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        MultiblockHelper.CoreLookup core = validCore();
        if (level == null || core == null) {
            return List.of();
        }
        BlockEntity coreEntity = level.getBlockEntity(core.pos());
        ICapabilityProvider target = legacyProxyTarget(coreEntity);
        if (target instanceof HbmFluidReceiver receiver) {
            return receiver.getAllTanks();
        }
        if (target instanceof HbmFluidBlockEntity fluidBlockEntity) {
            return fluidBlockEntity.getAllTanks();
        }
        return List.of();
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!canProxyFluid() || type == null || type == HbmFluids.NONE || amount <= 0L) {
            return amount;
        }
        MultiblockHelper.CoreLookup core = validCore();
        if (level == null || core == null) {
            return amount;
        }
        ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
        if (target instanceof HbmFluidReceiver receiver && target != this) {
            return receiver.transferFluid(type, pressure, amount);
        }
        if (!HbmForgeFluidInterop.isStandardPressure(pressure) || !HbmFluidForgeMappings.canExport(type)
                || target == null) {
            return amount;
        }
        int forgeAmount = (int) Math.min(Integer.MAX_VALUE, amount);
        FluidStack stack = HbmFluidForgeMappings.toForge(type, forgeAmount);
        if (stack.isEmpty()) {
            return amount;
        }
        int accepted = target.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                .map(handler -> handler.fill(stack, IFluidHandler.FluidAction.EXECUTE))
                .orElse(0);
        return amount - accepted;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if (!canProxyFluid() || type == null || type == HbmFluids.NONE) {
            return 0L;
        }
        MultiblockHelper.CoreLookup core = validCore();
        if (level == null || core == null) {
            return 0L;
        }
        ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
        if (target instanceof HbmFluidReceiver receiver && target != this) {
            return receiver.getDemand(type, pressure);
        }
        if (!HbmForgeFluidInterop.isStandardPressure(pressure) || !HbmFluidForgeMappings.canExport(type)
                || target == null) {
            return 0L;
        }
        FluidStack sample = HbmFluidForgeMappings.toForge(type, Integer.MAX_VALUE);
        if (sample.isEmpty()) {
            return 0L;
        }
        return target.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                .map(handler -> (long) handler.fill(sample, IFluidHandler.FluidAction.SIMULATE))
                .orElse(0L);
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        MultiblockHelper.CoreLookup core = validCore();
        if (level != null && core != null) {
            ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
            if (target instanceof HbmFluidReceiver receiver && target != this) {
                return receiver.getReceiverSpeed(type, pressure);
            }
        }
        return HbmFluidReceiver.super.getReceiverSpeed(type, pressure);
    }

    @Override
    public int[] getReceivingPressureRange(FluidType type) {
        MultiblockHelper.CoreLookup core = validCore();
        if (level != null && core != null) {
            ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
            if (target instanceof HbmFluidReceiver receiver && target != this) {
                return receiver.getReceivingPressureRange(type);
            }
        }
        return HbmFluidReceiver.super.getReceivingPressureRange(type);
    }

    private boolean canProxyFluid() {
        return proxyMode.isProxy() && (proxyMode.fluid() || proxyMode.moltenMetal() || proxyMode.allCapabilities());
    }

    public InteractionResult forwardUse(ServerPlayer player, InteractionHand hand, BlockHitResult hit) {
        MultiblockHelper.CoreLookup core = validCore();
        if (level == null || core == null) {
            return InteractionResult.PASS;
        }
        return core.state().use(level, player, hand, hit.withPosition(core.pos()));
    }

    public void setDropCoreOnRemoval(boolean dropCoreOnRemoval) {
        this.dropCoreOnRemoval = dropCoreOnRemoval;
    }

    @Override
    public String[] getFunctionInfo() {
        Object target = rorTarget();
        return target instanceof RORInfo info ? info.getFunctionInfo() : new String[0];
    }

    @Override
    public String provideRORValue(String name) {
        Object target = rorTarget();
        return target instanceof RORValueProvider provider ? provider.provideRORValue(name) : null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        Object target = rorTarget();
        return target instanceof RORInteractive interactive ? interactive.runRORFunction(name, params) : null;
    }

    public void destroyCore() {
        destroyCore(dropCoreOnRemoval);
    }

    public void destroyCore(boolean drop) {
        dropCoreOnRemoval = false;
        MultiblockHelper.CoreLookup core = validCore();
        if (level != null && core != null && !core.pos().equals(worldPosition)) {
            level.destroyBlock(core.pos(), drop);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        MultiblockHelper.CoreLookup core = validCore();
        if (proxyMode.allows(capability) && level != null && core != null && !core.pos().equals(worldPosition)) {
            ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
            if (target != null) {
                return target.getCapability(capability, side);
            }
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    private ICapabilityProvider legacyProxyTarget(@Nullable BlockEntity coreEntity) {
        if (coreEntity == null || coreEntity.isRemoved()) {
            return null;
        }
        if (coreEntity instanceof LegacyProxyDelegateProvider delegateProvider) {
            ICapabilityProvider delegate = delegateProvider.getLegacyProxyDelegate(worldPosition);
            if (delegate != null) {
                return delegate;
            }
        }
        return coreEntity;
    }

    @Nullable
    private Object rorTarget() {
        if (!proxyMode.isProxy()) {
            return null;
        }
        MultiblockHelper.CoreLookup core = validCore();
        if (level == null || core == null) {
            return null;
        }
        ICapabilityProvider target = legacyProxyTarget(level.getBlockEntity(core.pos()));
        return target == this ? null : target;
    }

    @Nullable
    private MultiblockHelper.CoreLookup validCore() {
        if (level == null || corePos == null || corePos.equals(worldPosition) || !HbmRegistryUtil.hasChunkAt(level, corePos)) {
            return null;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, corePos);
        return MultiblockHelper.ownsDummy(level, core, worldPosition) ? core : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_PROXY, proxyMode.isProxy());
        tag.putBoolean(TAG_PROXY_INVENTORY, proxyMode.inventory());
        tag.putBoolean(TAG_PROXY_POWER, proxyMode.power());
        tag.putBoolean(TAG_PROXY_CONDUCTOR, proxyMode.conductor());
        tag.putBoolean(TAG_PROXY_FLUID, proxyMode.fluid());
        tag.putBoolean(TAG_PROXY_HEAT, proxyMode.heat());
        tag.putBoolean(TAG_PROXY_MOLTEN_METAL, proxyMode.moltenMetal());
        tag.putBoolean(TAG_PROXY_ALL, proxyMode.allCapabilities());
        tag.putBoolean(TAG_LEGACY_EXTRA, legacyExtra);
        if (corePos != null) {
            tag.put(TAG_CORE_POS, NbtUtils.writeBlockPos(corePos));
            tag.putInt(TAG_LEGACY_TARGET_X, corePos.getX());
            tag.putInt(TAG_LEGACY_TARGET_Y, corePos.getY());
            tag.putInt(TAG_LEGACY_TARGET_Z, corePos.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        proxyMode = readProxyMode(tag);
        legacyExtra = tag.getBoolean(TAG_LEGACY_EXTRA);
        if (tag.contains(TAG_CORE_POS)) {
            corePos = NbtUtils.readBlockPos(tag.getCompound(TAG_CORE_POS));
        } else if (tag.contains(TAG_LEGACY_TARGET_X) && tag.contains(TAG_LEGACY_TARGET_Y) && tag.contains(TAG_LEGACY_TARGET_Z)) {
            corePos = new BlockPos(tag.getInt(TAG_LEGACY_TARGET_X), tag.getInt(TAG_LEGACY_TARGET_Y), tag.getInt(TAG_LEGACY_TARGET_Z));
        } else {
            corePos = null;
        }
    }

    private static LegacyProxyMode readProxyMode(CompoundTag tag) {
        if (!tag.getBoolean(TAG_PROXY)) {
            return LegacyProxyMode.none();
        }
        if (tag.getBoolean(TAG_PROXY_ALL) || !hasProxyModeTags(tag)) {
            return LegacyProxyMode.all();
        }
        return LegacyProxyMode.passive()
                .withInventory(tag.getBoolean(TAG_PROXY_INVENTORY))
                .withPower(tag.getBoolean(TAG_PROXY_POWER))
                .withConductor(tag.getBoolean(TAG_PROXY_CONDUCTOR))
                .withFluid(tag.getBoolean(TAG_PROXY_FLUID))
                .withHeat(tag.getBoolean(TAG_PROXY_HEAT))
                .withMoltenMetal(tag.getBoolean(TAG_PROXY_MOLTEN_METAL));
    }

    private static boolean hasProxyModeTags(CompoundTag tag) {
        return tag.contains(TAG_PROXY_INVENTORY)
                || tag.contains(TAG_PROXY_POWER)
                || tag.contains(TAG_PROXY_CONDUCTOR)
                || tag.contains(TAG_PROXY_FLUID)
                || tag.contains(TAG_PROXY_HEAT)
                || tag.contains(TAG_PROXY_MOLTEN_METAL)
                || tag.contains(TAG_PROXY_ALL);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
