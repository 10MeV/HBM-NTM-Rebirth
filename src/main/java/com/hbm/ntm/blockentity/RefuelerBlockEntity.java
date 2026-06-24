package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.block.RefuelerBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RefuelerBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver {
    private static final String TAG_TANK = "t";
    private static final String TAG_OPERATING = "isOperating";
    private static final String TAG_OPERATING_TIME = "operatingTime";
    private static final String TAG_FILL_LEVEL = "fillLevel";
    private static final String TAG_PREV_FILL_LEVEL = "prevFillLevel";
    private static final int CAPACITY = 100;

    private final HbmFluidTank tank;
    private boolean isOperating;
    private int operatingTime;
    private double fillLevel;
    private double prevFillLevel;

    public RefuelerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.KEROSENE, CAPACITY));
    }

    private RefuelerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.REFUELER.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefuelerBlockEntity refueler) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, refueler);

        boolean previousOperating = refueler.isOperating;
        int previousFill = refueler.tank.getFill();
        refueler.isOperating = refueler.fillNearbyPlayers(level, pos);
        if (refueler.isOperating) {
            if (refueler.operatingTime % 20 == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 0.5F);
            }
            refueler.operatingTime++;
        } else {
            refueler.operatingTime = 0;
        }

        refueler.networkPackNT(150);
        if (previousOperating != refueler.isOperating || previousFill != refueler.tank.getFill()
                || level.getGameTime() % 20L == 0L) {
            refueler.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RefuelerBlockEntity refueler) {
        if (!level.isClientSide) {
            return;
        }
        Direction dir = refueler.inputSide();
        Direction rot = dir.getClockWise();
        if (refueler.isOperating) {
            int color = refueler.tank.getTankType().getColor();
            ParticleUtil.spawnFluidFill(level,
                    pos.getX() + 0.5D + level.random.nextDouble() * 0.0625D + dir.getStepX() * 0.5D + rot.getStepX() * 0.25D,
                    pos.getY() + 0.375D,
                    pos.getZ() + 0.5D + level.random.nextDouble() * 0.0625D + dir.getStepZ() * 0.5D + rot.getStepZ() * 0.25D,
                    -dir.getStepX() + level.random.nextGaussian() * 0.1D,
                    0.0D,
                    -dir.getStepZ() + level.random.nextGaussian() * 0.1D,
                    color);
        }

        refueler.prevFillLevel = refueler.fillLevel;
        double targetFill = refueler.tank.getMaxFill() <= 0
                ? 0.0D
                : refueler.tank.getFill() / (double) refueler.tank.getMaxFill();
        double factor = targetFill > refueler.fillLevel || !refueler.isOperating ? 0.1D : 0.01D;
        refueler.fillLevel += (targetFill - refueler.fillLevel) * factor;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean isOperating() {
        return isOperating;
    }

    public double getInterpolatedFillLevel(float partialTick) {
        return prevFillLevel + (fillLevel - prevFillLevel) * partialTick;
    }

    public boolean setTankType(@Nullable FluidType type) {
        FluidType next = type == null ? HbmFluids.NONE : type;
        if (tank.getTankType() == next) {
            return false;
        }
        tank.setTankType(next);
        onFluidContentsChanged();
        invalidateFluidHandlers();
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        ArrayList<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        lines.add(LegacyLookOverlayLines.tank(true, tank));
        return LegacyLookOverlay.forBlock(this, lines);
    }

    private boolean fillNearbyPlayers(Level level, BlockPos pos) {
        boolean filled = false;
        for (Player player : level.getEntitiesOfClass(Player.class, fillBox(pos))) {
            filled |= fillStack(player.getItemBySlot(EquipmentSlot.HEAD));
            filled |= fillStack(player.getItemBySlot(EquipmentSlot.CHEST));
            filled |= fillStack(player.getItemBySlot(EquipmentSlot.LEGS));
            filled |= fillStack(player.getItemBySlot(EquipmentSlot.FEET));
            filled |= fillStack(player.getMainHandItem());
            filled |= fillStack(player.getOffhandItem());
        }
        if (filled) {
            onFluidContentsChanged();
        }
        return filled;
    }

    private boolean fillStack(ItemStack stack) {
        if (stack.isEmpty() || tank.isEmpty()) {
            return false;
        }
        boolean moved = fillDirectHbmItem(stack);
        HbmFluidItemTransfer.ArmorModFluidTransferReport armorReport =
                HbmFluidItemTransfer.fillArmorModsFromTankReport(stack, tank, tank.getFill(), false);
        return moved || armorReport.moved();
    }

    private boolean fillDirectHbmItem(ItemStack stack) {
        if (!(stack.getItem() instanceof IFillableItem fillable) || !fillable.acceptsFluid(tank.getTankType(), stack)) {
            return false;
        }
        int before = tank.getFill();
        int remainder = fillable.tryFill(tank.getTankType(), before, stack);
        tank.setFill(Math.max(0, remainder));
        return tank.getFill() < before;
    }

    private Direction inputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(RefuelerBlock.FACING)
                ? state.getValue(RefuelerBlock.FACING).getOpposite()
                : Direction.SOUTH;
    }

    private static AABB fillBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 0.5D, pos.getZ() + 1.0D);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        int before = tank.getFill();
        long remainder = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (tank.getFill() != before) {
            onFluidContentsChanged();
        }
        return remainder;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != null && type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        return List.of(HbmFluidPortLayouts.adjacent(inputSide()));
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null || side == inputSide() ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null
                && type != null
                && type != HbmFluids.NONE
                && type == tank.getTankType()
                && side == inputSide();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNbt(tag, TAG_TANK);
        tag.putBoolean(TAG_OPERATING, isOperating);
        tag.putInt(TAG_OPERATING_TIME, operatingTime);
        tag.putDouble(TAG_FILL_LEVEL, fillLevel);
        tag.putDouble(TAG_PREV_FILL_LEVEL, prevFillLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (hasTankTag(tag, TAG_TANK)) {
            tank.readFromNbt(tag, TAG_TANK);
        }
        isOperating = tag.getBoolean(TAG_OPERATING);
        operatingTime = tag.getInt(TAG_OPERATING_TIME);
        fillLevel = tag.getDouble(TAG_FILL_LEVEL);
        prevFillLevel = tag.getDouble(TAG_PREV_FILL_LEVEL);
        invalidateFluidHandlers();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean(TAG_OPERATING, isOperating);
        tag.putInt(TAG_OPERATING_TIME, operatingTime);
        tag.putDouble(TAG_FILL_LEVEL, fillLevel);
        tag.putDouble(TAG_PREV_FILL_LEVEL, prevFillLevel);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        isOperating = tag.getBoolean(TAG_OPERATING);
        operatingTime = tag.getInt(TAG_OPERATING_TIME);
        fillLevel = tag.getDouble(TAG_FILL_LEVEL);
        prevFillLevel = tag.getDouble(TAG_PREV_FILL_LEVEL);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }
}
