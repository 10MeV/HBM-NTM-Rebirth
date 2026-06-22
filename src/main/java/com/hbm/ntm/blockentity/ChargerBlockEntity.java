package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ChargerBlock;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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

public class ChargerBlockEntity extends HbmEnergyBlockEntity {
    private static final String TAG_USING = "usingTicks";
    private static final String TAG_LAST_USING = "lastUsingTicks";
    private static final String TAG_DELAY = "delay";
    private static final int MAX_USING_TICKS = 20;
    private static final int PARTICLE_TICKS = 4;

    private int usingTicks;
    private int lastUsingTicks;
    private int delay;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGER.get(), pos, state, new HbmEnergyStorage(0L, 0L, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity charger) {
        if (level.isClientSide) {
            return;
        }
        int previousUsing = charger.usingTicks;
        charger.lastUsingTicks = charger.usingTicks;
        long demand = charger.collectChargeDemand(level, pos);
        charger.energy.setMaxPower(demand);
        charger.energy.setTransferRates(demand, 0L);
        charger.subscribeEnergyReceiverToSide(charger.inputSide());

        boolean charged = charger.usingTicks >= MAX_USING_TICKS
                && charger.energy.getPower() > 0L
                && charger.chargeNearbyPlayers(level, pos);
        if (charged) {
            charger.delay = PARTICLE_TICKS;
            charger.usingTicks = Math.min(MAX_USING_TICKS, charger.usingTicks + 1);
        } else if (demand > 0L || charger.delay > 0) {
            charger.usingTicks = Math.min(MAX_USING_TICKS, charger.usingTicks + 1);
        } else if (charger.delay > 0) {
            charger.delay--;
        } else {
            charger.usingTicks = Math.max(0, charger.usingTicks - 1);
        }

        if (!charged && charger.delay > 0) {
            charger.delay--;
        }

        if (previousUsing < 2 && charger.usingTicks >= 2) {
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 1.0F);
        } else if (previousUsing > 4 && charger.usingTicks <= 4) {
            level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        charger.networkPackNT(20);
        if (previousUsing != charger.usingTicks || charged || level.getGameTime() % 20L == 0L) {
            charger.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity charger) {
        charger.lastUsingTicks = charger.usingTicks;
        if (charger.delay <= 0 || level.getRandom().nextInt(4) != 0) {
            return;
        }
        Direction dir = charger.inputSide();
        level.addParticle(ParticleTypes.ENCHANTED_HIT,
                pos.getX() + 0.5D + level.getRandom().nextDouble() * 0.0625D + dir.getStepX() * 0.75D,
                pos.getY() + 0.1D,
                pos.getZ() + 0.5D + level.getRandom().nextDouble() * 0.0625D + dir.getStepZ() * 0.75D,
                -dir.getStepX() + level.getRandom().nextGaussian() * 0.1D,
                0.0D,
                -dir.getStepZ() + level.getRandom().nextGaussian() * 0.1D);
    }

    public float getSlide(float partialTick) {
        return (lastUsingTicks + (usingTicks - lastUsingTicks) * partialTick) / (float) MAX_USING_TICKS;
    }

    public int getUsingTicks() {
        return usingTicks;
    }

    public long getStoredPower() {
        return energy.getPower();
    }

    private boolean chargeNearbyPlayers(Level level, BlockPos pos) {
        boolean charged = false;
        for (Player player : level.getEntitiesOfClass(Player.class, chargeBox(pos))) {
            charged |= chargeStack(player.getItemBySlot(EquipmentSlot.HEAD));
            charged |= chargeStack(player.getItemBySlot(EquipmentSlot.CHEST));
            charged |= chargeStack(player.getItemBySlot(EquipmentSlot.LEGS));
            charged |= chargeStack(player.getItemBySlot(EquipmentSlot.FEET));
            charged |= chargeStack(player.getMainHandItem());
            charged |= chargeStack(player.getOffhandItem());
        }
        return charged;
    }

    private long collectChargeDemand(Level level, BlockPos pos) {
        long demand = 0L;
        for (Player player : level.getEntitiesOfClass(Player.class, chargeBox(pos))) {
            demand += chargeDemand(player.getItemBySlot(EquipmentSlot.HEAD));
            demand += chargeDemand(player.getItemBySlot(EquipmentSlot.CHEST));
            demand += chargeDemand(player.getItemBySlot(EquipmentSlot.LEGS));
            demand += chargeDemand(player.getItemBySlot(EquipmentSlot.FEET));
            demand += chargeDemand(player.getMainHandItem());
            demand += chargeDemand(player.getOffhandItem());
        }
        return Math.max(0L, demand);
    }

    private long chargeDemand(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof HbmChargeableItem battery)) {
            return 0L;
        }
        return Math.max(0L, Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack)));
    }

    private boolean chargeStack(ItemStack stack) {
        if (stack.isEmpty() || energy.getPower() <= 0L) {
            return false;
        }
        long before = energy.getPower();
        long after = HbmBatteryTransfer.chargeItemsFromPower(stack, before, energy.getMaxPower());
        energy.setPower(after);
        return after != before;
    }

    private Direction inputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(ChargerBlock.FACING) ? state.getValue(ChargerBlock.FACING).getOpposite() : Direction.SOUTH;
    }

    private static AABB chargeBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 0.5D, pos.getZ() + 1.0D);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null || side == inputSide() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.remove("Energy");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        usingTicks = tag.getInt(TAG_USING);
        lastUsingTicks = tag.getInt(TAG_LAST_USING);
        delay = tag.getInt(TAG_DELAY);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt(TAG_USING, usingTicks);
        tag.putInt(TAG_LAST_USING, lastUsingTicks);
        tag.putInt(TAG_DELAY, delay);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        usingTicks = tag.getInt(TAG_USING);
        lastUsingTicks = tag.getInt(TAG_LAST_USING);
        delay = tag.getInt(TAG_DELAY);
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
        handleClientSyncTag(packet.getTag());
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
}
