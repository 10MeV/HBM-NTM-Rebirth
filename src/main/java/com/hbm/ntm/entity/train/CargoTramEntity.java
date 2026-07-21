package com.hbm.ntm.entity.train;

import com.hbm.ntm.menu.CargoTramMenu;
import com.hbm.ntm.rail.HbmRail;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Exact concrete contract of the old electric flat-bed cargo tram. */
public final class CargoTramEntity extends LegacyRailCarElectricEntity implements MenuProvider, RailCarMenuAccess {
    public static final int SLOT_COUNT = 29;
    public static final int CHARGE_SLOT = 28;
    private static final Vec3 DRIVER_SEAT = new Vec3(0.375D, 2.375D, 0.5D);
    private static final Vec3[] PASSENGER_SEATS = {new Vec3(0.5D, 1.75D, -1.5D), new Vec3(-0.5D, 1.75D, -1.5D)};
    private static final DummyConfig[] DUMMIES = {
            new DummyConfig(2.0F, 1.0F, new Vec3(0.0D, 0.0D, 1.5D)),
            new DummyConfig(2.0F, 1.0F, Vec3.ZERO),
            new DummyConfig(2.0F, 1.0F, new Vec3(0.0D, 0.0D, -1.5D))};

    public CargoTramEntity(EntityType<? extends CargoTramEntity> type, Level level) {
        super(type, level);
    }

    public CargoTramEntity(Level level) {
        this(ModEntityTypes.CARGO_TRAM.get(), level);
    }

    @Override public double getPoweredAcceleration() { return 0.01D; }
    @Override public double getPassiveBrake() { return 0.95D; }
    @Override public boolean shouldUseEngineBrake(Player player) { return Math.abs(getEngineSpeed()) < 0.1D; }
    @Override public double getMaxPoweredSpeed() { return 0.5D; }
    @Override public double getMaxRailSpeed() { return 1.0D; }
    @Override public HbmRail.TrackGauge getGauge() { return HbmRail.TrackGauge.STANDARD; }
    @Override public double getLengthSpan() { return 1.5D; }
    @Override public double getCollisionSpan() { return 2.5D; }
    @Override public Vec3 getRiderSeatPosition() { return DRIVER_SEAT; }
    @Override public Vec3[] getPassengerSeats() { return PASSENGER_SEATS; }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public double getCouplingDist(TrainCoupling coupling) { return coupling == null ? 0.0D : 2.75D; }
    @Override public int getMaxPower() { return getPowerConsumption() * 100; }
    @Override public int getPowerConsumption() { return 10; }
    @Override public boolean hasChargeSlot() { return true; }
    @Override public int getChargeSlot() { return CHARGE_SLOT; }
    @Override public DummyConfig[] getDummies() { return DUMMIES; }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved()) {
            discard();
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : Component.translatable("container.trainTram");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CargoTramMenu(containerId, inventory, this);
    }

    public void openInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(this::createMenu, getDisplayName()),
                buffer -> buffer.writeInt(getId()));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
