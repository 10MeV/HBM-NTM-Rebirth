package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CargoElevatorBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CargoElevatorBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private static final String TAG_EXTENSION = "extension";
    private static final String TAG_IS_EXTENDING = "isExtending";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_RENDER_PLATFORM = "renderPlatform";
    private static final double SPEED = 2.0D / 20.0D;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private int height;
    private double extension;
    private double prevExtension;
    private double syncExtension;
    private int syncTicks;
    private boolean extending;
    private boolean renderPlatform;

    public CargoElevatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CARGO_ELEVATOR.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CargoElevatorBlockEntity elevator) {
        elevator.prevExtension = elevator.extension;

        MultiblockHelper.CoreLookup lowerCore = MultiblockHelper.findCore(level, pos.below());
        if (lowerCore != null && lowerCore.pos().getX() == pos.getX() && lowerCore.pos().getZ() == pos.getZ()
                && level.getBlockEntity(lowerCore.pos()) instanceof CargoElevatorBlockEntity lower) {
            lower.setHeight(lower.height + elevator.height + 1);
            lower.syncChanged();
            CargoElevatorBlock.convertTowerToDummies((ServerLevel) level, pos, lowerCore.pos(), elevator.height);
            return;
        }

        if (elevator.extending && elevator.extension < elevator.height) {
            elevator.extension += SPEED;
        }
        if (!elevator.extending && elevator.extension > 0.0D) {
            elevator.extension -= SPEED;
        }
        elevator.extension = clamp(elevator.extension, 0.0D, elevator.height);
        elevator.renderPlatform = true;

        if (elevator.extension != elevator.prevExtension) {
            elevator.moveEntities(level, false);
            elevator.syncChanged();
        } else if (level.getGameTime() % 20L == 0L) {
            elevator.syncChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CargoElevatorBlockEntity elevator) {
        elevator.prevExtension = elevator.extension;
        if (elevator.syncTicks > 0) {
            elevator.extension = elevator.extension + ((elevator.syncExtension - elevator.extension) / elevator.syncTicks);
            elevator.syncTicks--;
        } else {
            elevator.extension = elevator.syncExtension;
        }
        if (elevator.extension != elevator.prevExtension) {
            elevator.moveEntities(level, true);
        }
    }

    public void toggleElevator() {
        if (extension >= height) {
            extending = false;
        }
        if (extension <= 0.0D) {
            extending = true;
        }
        syncChanged();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(0, height);
        this.extension = clamp(extension, 0.0D, this.height);
        this.syncExtension = clamp(syncExtension, 0.0D, this.height);
        setChanged();
    }

    public double getExtension() {
        return extension;
    }

    public double getPrevExtension() {
        return prevExtension;
    }

    public boolean shouldRenderPlatform() {
        return renderPlatform;
    }

    public void syncChanged() {
        setChanged();
        syncToTracking();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void moveEntities(Level level, boolean clientSide) {
        double liftUpper = worldPosition.getY() + 1.0D + Math.max(extension, prevExtension);
        double liftLower = worldPosition.getY() + 1.0D + Math.min(extension, prevExtension);
        AABB liftBounds = new AABB(
                worldPosition.getX() - 0.99D,
                liftLower,
                worldPosition.getZ() - 0.99D,
                worldPosition.getX() + 1.99D,
                liftUpper,
                worldPosition.getZ() + 1.99D);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, liftBounds);
        for (Entity entity : entities) {
            if (entity instanceof Player && !clientSide) {
                continue;
            }
            if (entity.getBoundingBox().minY >= liftLower && entity.getBoundingBox().minY <= liftUpper) {
                double delta = entity.getBoundingBox().minY - (worldPosition.getY() + 1.0D + extension);
                entity.move(MoverType.SELF, new Vec3(0.0D, -delta, 0.0D));
                entity.setOnGround(true);
                entity.move(MoverType.SELF, new Vec3(0.0D, -0.125D, 0.0D));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putDouble(TAG_EXTENSION, extension);
        tag.putBoolean(TAG_IS_EXTENDING, extending);
        tag.putInt(TAG_HEIGHT, height);
        tag.putBoolean(TAG_RENDER_PLATFORM, renderPlatform);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        double loadedExtension = tag.getDouble(TAG_EXTENSION);
        extending = tag.getBoolean(TAG_IS_EXTENDING);
        height = tag.getInt(TAG_HEIGHT);
        renderPlatform = tag.getBoolean(TAG_RENDER_PLATFORM);
        if (level != null && level.isClientSide) {
            syncExtension = loadedExtension;
            if (syncExtension > 0.0D && syncExtension < height) {
                syncTicks = 3;
            } else {
                extension = syncExtension;
            }
        } else {
            extension = loadedExtension;
            syncExtension = loadedExtension;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeBoolean(renderPlatform);
        data.writeShort((short) height);
        data.writeDouble(extension);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        renderPlatform = data.readBoolean();
        height = data.readShort();
        syncExtension = data.readDouble();
        if (syncExtension > 0.0D && syncExtension < height) {
            syncTicks = 3;
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        int h = 1 + height;
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                pos.getX() + 2, pos.getY() + h, pos.getZ() + 2);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
