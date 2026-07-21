package com.hbm.ntm.entity.train;

import com.hbm.ntm.menu.CargoTramTrailerMenu;
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

/** Exact concrete contract of the unpowered 45-slot cargo-tram trailer. */
public final class CargoTramTrailerEntity extends LegacyRailCarCargoEntity implements MenuProvider, RailCarMenuAccess {
    public static final int SLOT_COUNT = 45;
    private static final DummyConfig[] DUMMIES = {
            new DummyConfig(2.0F, 1.0F, new Vec3(0.0D, 0.0D, 1.5D)),
            new DummyConfig(2.0F, 1.0F, Vec3.ZERO),
            new DummyConfig(2.0F, 1.0F, new Vec3(0.0D, 0.0D, -1.5D))};

    public CargoTramTrailerEntity(EntityType<? extends CargoTramTrailerEntity> type, Level level) {
        super(type, level);
    }

    public CargoTramTrailerEntity(Level level) {
        this(ModEntityTypes.CARGO_TRAM_TRAILER.get(), level);
    }

    @Override public double getCurrentSpeed() { return 0.0D; }
    @Override public double getMaxRailSpeed() { return 1.0D; }
    @Override public HbmRail.TrackGauge getGauge() { return HbmRail.TrackGauge.STANDARD; }
    @Override public double getLengthSpan() { return 1.5D; }
    @Override public double getCollisionSpan() { return 2.5D; }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public double getCouplingDist(TrainCoupling coupling) { return coupling == null ? 0.0D : 2.75D; }
    @Override public DummyConfig[] getDummies() { return DUMMIES; }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved()) {
            discard();
        }
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult base = super.interact(player, hand);
        if (base.consumesAction()) {
            return base;
        }
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            openInventory(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : Component.translatable("container.trainTramTrailer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CargoTramTrailerMenu(containerId, inventory, this);
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
