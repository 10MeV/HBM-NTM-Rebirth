package com.hbm.ntm.turret;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidPortLayouts.LegacyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TurretFritzBlockEntity extends TurretBlockEntityBase
        implements HbmStandardFluidReceiver, HbmFluidCopiable {
    private static final String TAG_TANK = "diesel";
    private static final int TANK_CAPACITY = 16_000;
    private static final int FLUID_PER_SHOT = 2;
    private static final int DIESEL_CAN_AMOUNT = 1_000;
    private static final LegacyPort[] FLUID_PORT_TEMPLATE = {
            LegacyPort.of(-1, 0, Direction.NORTH),
            LegacyPort.of(-1, -1, Direction.NORTH),
            LegacyPort.of(0, -2, Direction.WEST),
            LegacyPort.of(1, -2, Direction.WEST),
            LegacyPort.of(0, 1, Direction.EAST),
            LegacyPort.of(1, 1, Direction.EAST),
            LegacyPort.of(2, 0, Direction.SOUTH),
            LegacyPort.of(2, -1, Direction.SOUTH)
    };

    private final HbmFluidTank tank = new HbmFluidTank(HbmFluids.DIESEL, TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> fluidHandler =
            LazyOptional.of(() -> new ForgeFluidHandlerAdapter(List.of(tank), 0, true, false, this::onFluidContentsChanged));

    public TurretFritzBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_FRITZ.get(), pos, state, 10_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_fritz";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_fritz.png";
    }

    @Override
    protected double getDetectorRange() {
        return 48.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return 2.0D;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 15.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 45.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 2.25D;
    }

    @Override
    public List<ItemStack> getAmmoTypesForDisplay() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(ModItems.AMMO_STANDARD_FLAME_DIESEL.get()));
        for (FluidType type : HbmFluids.niceOrder()) {
            if (type.hasTrait(CombustibleFluidTrait.class) && type.hasTrait(SimpleFluidTraits.Liquid.class)) {
                stacks.add(FluidIconItem.make(type, 0));
            }
        }
        return stacks;
    }

    @Override
    protected int[] externalAccessibleSlots() {
        return new int[] {SLOT_AMMO_START, 2, 3, 4, 5, 6, 7, 8};
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return tank.getSpace();
    }

    @Override
    public void onFluidSettingsPasted() {
        onFluidContentsChanged();
    }

    @Override
    protected void updateServerTick() {
        subscribeFluidReceiverPorts();
    }

    @Override
    protected void updateServerTickAfterTargeting() {
        boolean changed = updateTankFromInventory();
        if (changed) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected void updateFiringTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        FluidType type = tank.getTankType();
        FlammableFluidTrait flammable = type.getTrait(FlammableFluidTrait.class);
        if (flammable == null || !type.hasTrait(SimpleFluidTraits.Liquid.class) || tank.getFill() < FLUID_PER_SHOT) {
            return;
        }
        tank.drain(FLUID_PER_SHOT, false);

        BulletConfig config = type == HbmFluids.BALEFIRE
                ? LegacySednaRuntimeBulletConfigs.FLAME_NOGRAV_BF
                : LegacySednaRuntimeBulletConfigs.FLAME_NOGRAV;
        spawnBullet(config, legacyFlameDamage(flammable));

        level.playSound(null, worldPosition, ModSounds.WEAPON_FLAMETHROWER_SHOOT.get(),
                SoundSource.BLOCKS, 2.0F, 1.0F + level.random.nextFloat() * 0.5F);
        onFluidContentsChanged();
    }

    private static float legacyFlameDamage(FlammableFluidTrait flammable) {
        return Math.min((float) (flammable.getHeatEnergyPerBucket() / 500_000.0D), 20.0F);
    }

    private boolean updateTankFromInventory() {
        boolean changed = false;
        for (int slot = SLOT_AMMO_START; slot < SLOT_AMMO_END; slot++) {
            changed |= HbmFluidItemTransfer.loadTankFromSlot(getItems(), slot, SLOT_AMMO_END, tank);
        }
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = getItems().getStackInSlot(slot);
            if (!stack.is(ModItems.AMMO_STANDARD_FLAME_DIESEL.get())
                    || tank.getTankType() != HbmFluids.DIESEL
                    || tank.getFill() + DIESEL_CAN_AMOUNT > tank.getMaxFill()) {
                continue;
            }
            stack.shrink(1);
            getItems().setStackInSlot(slot, stack);
            tank.setFill(tank.getFill() + DIESEL_CAN_AMOUNT);
            changed = true;
        }
        return changed;
    }

    private void subscribeFluidReceiverPorts() {
        if (level == null || level.isClientSide || tank.getTankType() == HbmFluids.NONE) {
            return;
        }
        for (FluidPort port : fluidPorts()) {
            HbmFluidUtil.subscribeReceiverToPort(level, worldPosition, port, tank.getTankType(), this);
        }
    }

    private List<FluidPort> fluidPorts() {
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return HbmFluidPortLayouts.legacy(facing, FLUID_PORT_TEMPLATE);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNbt(tag, TAG_TANK);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNbt(tag, TAG_TANK);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tank.writeToNbt(tag, TAG_TANK);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tank.writeToNbt(tag, TAG_TANK);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        tank.readFromNbt(tag, TAG_TANK);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && side != Direction.UP && side != Direction.DOWN) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
