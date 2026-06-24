package com.hbm.ntm.blockentity;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.EntityDamageUtil;
import com.hbm.ntm.util.HbmWorldUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TeslaBlockEntity extends HbmEnergyBlockEntity implements HbmLegacyLoadedTile {
    public static final long MAX_POWER = 100_000L;
    public static final long CONSUMPTION = 5_000L;
    public static final int RANGE = 10;
    public static final double OFFSET = 1.75D;
    private static final double BEAM_RENDER_PADDING = 1.0D;

    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_TARGETS = "Targets";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final String METEOR_BATTERY_PATH = "meteor_battery";
    @Nullable
    private static final EntityDataAccessor<Boolean> CREEPER_POWERED = findCreeperPoweredAccessor();

    private final List<TeslaTarget> targets = new ArrayList<>();

    public TeslaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TESLA.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TeslaBlockEntity tesla) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = tesla.energy.getPower();
        int oldTargets = tesla.targets.size();
        tesla.subscribeEnergyReceiverToAllSides();
        tesla.targets.clear();

        if (tesla.hasMeteorBatteryBelow()) {
            tesla.energy.setPower(MAX_POWER);
        }

        if (tesla.energy.getPower() >= CONSUMPTION) {
            tesla.energy.setPower(tesla.energy.getPower() - CONSUMPTION);
            Vec3 origin = tesla.sourcePosition();
            tesla.targets.addAll(zap(level, origin, RANGE, null));
        }

        tesla.networkPackNT(100);
        if (oldPower != tesla.energy.getPower() || oldTargets != tesla.targets.size()) {
            tesla.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static List<TeslaTarget> zap(Level level, Vec3 origin, double radius, @Nullable Entity source) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(origin, origin).inflate(radius), LivingEntity::isAlive);
        List<TeslaTarget> ret = new ArrayList<>();

        for (LivingEntity target : nearby) {
            if (target instanceof Ocelot || target == source) {
                continue;
            }

            Vec3 targetPoint = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (targetPoint.subtract(origin).length() > RANGE) {
                continue;
            }
            if (HbmWorldUtil.isObstructed(level, origin, targetPoint)) {
                continue;
            }

            if (RadiationUtil.hasLegacyClassName(target, "EntityTaintCrab")) {
                ret.add(new TeslaTarget(target.getX(), target.getY() + 1.25D, target.getZ()));
                target.heal(15.0F);
                continue;
            }
            if (RadiationUtil.hasLegacyClassName(target, "EntityTeslaCrab")) {
                ret.add(new TeslaTarget(target.getX(), target.getY() + 1.0D, target.getZ()));
                target.heal(10.0F);
                continue;
            }
            if (RadiationUtil.hasLegacyClassName(target, "EntityCyberCrab")) {
                ret.add(new TeslaTarget(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ()));
                continue;
            }
            if (target instanceof Creeper creeper) {
                chargeCreeper(creeper);
                ret.add(new TeslaTarget(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ()));
                continue;
            }

            if (!(target instanceof Player player && ArmorUtil.checkForFaraday(player))) {
                float damage = Mth.clamp(target.getMaxHealth() * 0.5F, 3.0F, 20.0F)
                        / Math.max(1.0F, (float) nearby.size());
                if (EntityDamageUtil.attackEntityFromNt(target,
                        ModDamageSources.source(level, ModDamageSources.ELECTRICITY), damage)) {
                    LegacySoundPlayer.playLegacyTesla(target);
                }
            }

            double playerOffset = source != null && target instanceof Player && level.isClientSide
                    ? target.getBbHeight()
                    : 0.0D;
            ret.add(new TeslaTarget(target.getX(), target.getY() + target.getBbHeight() * 0.5D - playerOffset,
                    target.getZ()));
        }

        return ret;
    }

    public Vec3 sourcePosition() {
        return new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + OFFSET, worldPosition.getZ() + 0.5D);
    }

    public List<TeslaTarget> getTargets() {
        return List.copyOf(targets);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        Vec3 source = sourcePosition();
        AABB box = new AABB(worldPosition).minmax(new AABB(source, source)).inflate(BEAM_RENDER_PADDING);
        for (TeslaTarget target : targets) {
            Vec3 targetPoint = new Vec3(target.x(), target.y(), target.z());
            box = box.minmax(new AABB(source, targetPoint).inflate(BEAM_RENDER_PADDING));
        }
        return box;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        writeTargets(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readTargets(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeLong(energy.getPower());
        data.writeVarInt(targets.size());
        for (TeslaTarget target : targets) {
            data.writeDouble(target.x());
            data.writeDouble(target.y());
            data.writeDouble(target.z());
        }
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        energy.setPower(data.readLong());
        targets.clear();
        int count = Math.min(data.readVarInt(), Short.MAX_VALUE);
        for (int i = 0; i < count; i++) {
            targets.add(new TeslaTarget(data.readDouble(), data.readDouble(), data.readDouble()));
        }
    }

    private void writeTargets(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TeslaTarget target : targets) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble(TAG_X, target.x());
            entry.putDouble(TAG_Y, target.y());
            entry.putDouble(TAG_Z, target.z());
            list.add(entry);
        }
        tag.put(TAG_TARGETS, list);
    }

    private void readTargets(CompoundTag tag) {
        targets.clear();
        ListTag list = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            targets.add(new TeslaTarget(entry.getDouble(TAG_X), entry.getDouble(TAG_Y), entry.getDouble(TAG_Z)));
        }
    }

    private boolean hasMeteorBatteryBelow() {
        if (level == null) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(worldPosition.below()).getBlock());
        return METEOR_BATTERY_PATH.equals(key.getPath())
                && (HbmNtm.MOD_ID.equals(key.getNamespace()) || "hbm".equals(key.getNamespace()));
    }

    private static boolean chargeCreeper(Creeper creeper) {
        if (CREEPER_POWERED == null) {
            return false;
        }
        creeper.getEntityData().set(CREEPER_POWERED, true);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static EntityDataAccessor<Boolean> findCreeperPoweredAccessor() {
        for (String name : List.of("DATA_IS_POWERED", "f_32274_")) {
            try {
                Field field = Creeper.class.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof EntityDataAccessor<?> accessor) {
                    return (EntityDataAccessor<Boolean>) accessor;
                }
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    public record TeslaTarget(double x, double y, double z) {
    }
}
