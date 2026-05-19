package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.TrinketBlock;
import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TrinketBlockEntity extends BlockEntity {
    private static final String TAG_YAW_STEPS = "Yaw";
    private static final String TAG_SQUISH_TIMER = "SquishTimer";

    private int variant;
    private int yawSteps;
    private int squishTimer;

    public TrinketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRINKET.get(), pos, state);
    }

    public TrinketVariant.Kind kind() {
        return getBlockState().getBlock() instanceof TrinketBlock trinket ? trinket.kind() : TrinketVariant.Kind.BOBBLEHEAD;
    }

    public int variant() {
        return TrinketVariant.clamp(kind(), variant);
    }

    public void setVariant(int variant) {
        this.variant = TrinketVariant.clamp(kind(), variant);
        setChangedAndSync();
    }

    public int yawSteps() {
        return yawSteps & 15;
    }

    public void setYawSteps(int yawSteps) {
        this.yawSteps = yawSteps & 15;
        setChangedAndSync();
    }

    public float yawDegrees() {
        return yawSteps() * 22.5F;
    }

    public int squishTimer() {
        return squishTimer;
    }

    public void startSquish() {
        this.squishTimer = 11;
        setChangedAndSync();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrinketBlockEntity blockEntity) {
        if (blockEntity.squishTimer > 0) {
            blockEntity.squishTimer--;
            if (!level.isClientSide || blockEntity.squishTimer == 0) {
                blockEntity.setChangedAndSync();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TrinketVariant.TAG_VARIANT, variant());
        tag.putInt(TAG_YAW_STEPS, yawSteps());
        tag.putInt(TAG_SQUISH_TIMER, squishTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        variant = TrinketVariant.clamp(kind(), tag.getInt(TrinketVariant.TAG_VARIANT));
        yawSteps = tag.getInt(TAG_YAW_STEPS) & 15;
        squishTimer = tag.getInt(TAG_SQUISH_TIMER);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
