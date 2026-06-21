package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class TeleporterBlockEntity extends HbmEnergyBlockEntity implements LegacyLookOverlayProvider {
    public static final long MAX_POWER = 1_500_000L;
    public static final long CONSUMPTION = 1_000_000L;

    private static final String TAG_POWER = "power";
    private static final String TAG_TARGET_X = "x1";
    private static final String TAG_TARGET_Y = "y1";
    private static final String TAG_TARGET_Z = "z1";
    private static final String TAG_TARGET_DIM = "dim";
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final DustParticleOptions ACTIVE_PARTICLE =
            new DustParticleOptions(new Vector3f(0.4F, 0.8F, 1.0F), 1.0F);

    private int targetX = -1;
    private int targetY = -1;
    private int targetZ = -1;
    private int targetDim = 0;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TELEPORTER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity teleporter) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = teleporter.energy.getPower();
        teleporter.subscribeEnergyReceiverToAllSides();

        if (teleporter.hasTarget()) {
            AABB box = new AABB(
                    pos.getX() + 0.25D, pos.getY(), pos.getZ() + 0.25D,
                    pos.getX() + 0.75D, pos.getY() + 2.0D, pos.getZ() + 0.75D);
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, box, entity -> entity.isAlive());
            for (Entity entity : entities) {
                teleporter.teleport(entity);
            }
        }

        teleporter.networkPackNT(15);
        if (oldPower != teleporter.energy.getPower() || level.getGameTime() % 10L == 0L) {
            teleporter.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity teleporter) {
        if (!teleporter.hasTarget() || teleporter.energy.getPower() < CONSUMPTION) {
            return;
        }
        double x = pos.getX() + 0.5D + level.random.nextGaussian() * 0.25D;
        double y = pos.getY() + 1.0D + level.random.nextDouble() * 2.0D;
        double z = pos.getZ() + 0.5D + level.random.nextGaussian() * 0.25D;
        level.addParticle(ACTIVE_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public boolean hasTarget() {
        return targetY != -1;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getTargetZ() {
        return targetZ;
    }

    public int getTargetDim() {
        return targetDim;
    }

    public void setTarget(int x, int y, int z, int dim) {
        targetX = x;
        targetY = y;
        targetZ = z;
        targetDim = dim;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean teleport(Entity entity) {
        if (level == null || level.isClientSide || energy.getPower() < CONSUMPTION || !hasTarget()) {
            return false;
        }
        Optional<ServerLevel> targetLevel = targetLevel();
        if (targetLevel.isEmpty()) {
            return false;
        }

        double x = targetX + 0.5D;
        double y = targetY + 1.5D;
        double z = targetZ + 0.5D;
        LegacySoundPlayer.playSoundEffect(level,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D, worldPosition.getZ() + 0.5D,
                "mob.endermen.portal", 1.0F, 1.0F);

        boolean teleported = false;
        ServerLevel destination = targetLevel.get();
        if (entity instanceof ServerPlayer player) {
            if (player.serverLevel() == destination) {
                player.teleportTo(x, y, z);
            } else {
                player.teleportTo(destination, x, y, z, player.getYRot(), player.getXRot());
            }
            teleported = true;
        } else if (entity.level() == destination) {
            entity.teleportTo(x, y, z);
            teleported = true;
        } else {
            Entity changed = entity.changeDimension(destination);
            if (changed != null) {
                changed.teleportTo(x, y, z);
                entity = changed;
                teleported = true;
            }
        }

        if (!teleported) {
            return false;
        }

        LegacySoundPlayer.playSoundEffect(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                "mob.endermen.portal", 1.0F, 1.0F);
        energy.setPower(Math.max(0L, energy.getPower() - CONSUMPTION));
        setChanged();
        return true;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        if (!hasTarget()) {
            return LegacyLookOverlay.forBlock(this, List.of(
                    Component.literal("No destination set!").withStyle(ChatFormatting.RED)));
        }
        ChatFormatting powerColor = energy.getPower() >= CONSUMPTION ? ChatFormatting.GREEN : ChatFormatting.RED;
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal(NUMBER_FORMAT.format(energy.getPower()) + " / "
                        + NUMBER_FORMAT.format(MAX_POWER)).withStyle(powerColor),
                Component.literal("Destination: " + targetX + " / " + targetY + " / " + targetZ
                        + " (D: " + targetDim + ")")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_TARGET_X, targetX);
        tag.putInt(TAG_TARGET_Y, targetY);
        tag.putInt(TAG_TARGET_Z, targetZ);
        tag.putInt(TAG_TARGET_DIM, targetDim);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        targetX = tag.getInt(TAG_TARGET_X);
        targetY = tag.contains(TAG_TARGET_Y) ? tag.getInt(TAG_TARGET_Y) : -1;
        targetZ = tag.getInt(TAG_TARGET_Z);
        targetDim = tag.getInt(TAG_TARGET_DIM);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    private Optional<ServerLevel> targetLevel() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        MinecraftServer server = serverLevel.getServer();
        ResourceKey<Level> key = keyForLegacyDimension(targetDim);
        return key == null ? Optional.empty() : Optional.ofNullable(server.getLevel(key));
    }

    @Nullable
    private static ResourceKey<Level> keyForLegacyDimension(int dimension) {
        return switch (dimension) {
            case 0 -> Level.OVERWORLD;
            case -1 -> Level.NETHER;
            case 1 -> Level.END;
            default -> null;
        };
    }

    public static int legacyDimensionId(Level level) {
        ResourceKey<Level> dimension = level.dimension();
        if (dimension == Level.NETHER) {
            return -1;
        }
        if (dimension == Level.END) {
            return 1;
        }
        return 0;
    }
}
