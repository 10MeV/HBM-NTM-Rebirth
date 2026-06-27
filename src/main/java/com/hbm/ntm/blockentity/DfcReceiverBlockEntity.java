package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.Laserable;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.DfcReceiverMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class DfcReceiverBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, Laserable {
    public static final int CRYOGEL_CAPACITY = 64_000;
    private final HbmFluidTank cryogel;
    private long joules;

    public DfcReceiverBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.CRYOGEL, CRYOGEL_CAPACITY));
    }

    private DfcReceiverBlockEntity(BlockPos pos, BlockState state, HbmFluidTank cryogel) {
        super(ModBlockEntities.DFC_RECEIVER.get(), pos, state, new HbmEnergyStorage(0L, 0L, Long.MAX_VALUE),
                List.of(cryogel));
        this.cryogel = cryogel;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DfcReceiverBlockEntity receiver) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, receiver);
        receiver.tickServer(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DfcReceiverBlockEntity receiver) {
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        long output = joules * 5000L;
        energy.setMaxPower(Math.max(0L, output));
        energy.setTransferRates(0L, Math.max(0L, output));
        energy.setPower(output);
        tryProvideEnergyToPorts();
        pushForgeEnergyToPorts(output);
        if (joules > 0L) {
            if (cryogel.getFill() >= 20) {
                cryogel.setFill(cryogel.getFill() - 20);
            } else {
                level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
        networkPackNT(50);
        joules = 0L;
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    public void addEnergy(Level level, BlockPos pos, long energy, @Nullable Direction side) {
        if (side != null && side.getOpposite() == facing()) {
            joules += energy;
        } else {
            level.destroyBlock(worldPosition, false);
            level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, 2.5F, Level.ExplosionInteraction.BLOCK);
        }
        setChanged();
    }

    private Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    public HbmFluidTank getCryogelTank() { return cryogel; }
    public long getJoules() { return joules; }
    public long getOutputPower() { return joules * 5000L; }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.dfcReceiver", "DFC Receiver");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DfcReceiverMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(cryogel);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == cryogel.getTankType();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-64, -64, -64), worldPosition.offset(65, 65, 65));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", energy.getPower());
        tag.putLong("joules", joules);
        cryogel.writeToNbt(tag, "tank");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        joules = tag.getLong("joules");
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        cryogel.readFromNbt(tag, "tank");
    }
}
