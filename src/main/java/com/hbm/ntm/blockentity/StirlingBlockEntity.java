package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.StirlingBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.projectile.CogEntity;
import com.hbm.ntm.entity.projectile.MachinePartProjectileEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StirlingBlockEntity extends HbmEnergyBlockEntity implements HbmPersistentBlockState {
    public static final double DIFFUSION = 0.1D;
    public static final double EFFICIENCY = 0.5D;
    public static final int MAX_HEAT_NORMAL = 300;
    public static final int MAX_HEAT_STEEL = 1_500;
    public static final int OVERSPEED_LIMIT = 300;

    private long powerBuffer;
    private int heat;
    private int warnCooldown;
    private int overspeed;
    private boolean hasCog = true;
    private float spin;
    private float lastSpin;

    public StirlingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STIRLING.get(), pos, state, new HbmEnergyStorage(0L, 0L, Long.MAX_VALUE));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StirlingBlockEntity stirling) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = stirling.powerBuffer;
        int oldHeat = stirling.heat;
        boolean oldCog = stirling.hasCog;

        if (stirling.hasCog) {
            stirling.powerBuffer = 0L;
            stirling.tryPullHeat(level, pos);
            stirling.powerBuffer = (long) (stirling.heat * (stirling.kind().creative() ? 1.0D : EFFICIENCY));
            stirling.energy.setMaxPower(stirling.powerBuffer);
            stirling.energy.setTransferRates(0L, stirling.powerBuffer);
            stirling.energy.setPower(stirling.powerBuffer);

            if (stirling.warnCooldown > 0) {
                stirling.warnCooldown--;
            }

            if (stirling.heat > stirling.maxHeat() && !stirling.kind().creative()) {
                stirling.overspeed++;
                if (stirling.overspeed > 60 && stirling.warnCooldown == 0) {
                    stirling.warnCooldown = 100;
                    LegacySoundPlayer.playSoundEffect(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            "hbm:block.warnOverspeed", SoundSource.BLOCKS, 2.0F, 1.0F);
                }
                if (stirling.overspeed > OVERSPEED_LIMIT) {
                    stirling.hasCog = false;
                    stirling.energy.setPower(0L);
                    level.explode(null, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            5.0F, false, Level.ExplosionInteraction.NONE);
                    stirling.spawnCog(level, pos, stirling.facing());
                }
            } else {
                stirling.overspeed = 0;
            }
        } else {
            stirling.overspeed = 0;
            stirling.warnCooldown = 0;
            if (stirling.powerBuffer > 0L) {
                stirling.powerBuffer--;
            }
            stirling.energy.setPower(0L);
        }

        if (stirling.hasCog && stirling.powerBuffer > 0L) {
            stirling.tryProvideEnergyToPorts();
        }

        if (oldPower != stirling.powerBuffer || oldHeat != stirling.heat || oldCog != stirling.hasCog) {
            stirling.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        stirling.networkPackNT(150);
        stirling.heat = 0;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, StirlingBlockEntity stirling) {
        if (!level.isClientSide) {
            return;
        }
        float momentum = stirling.maxHeat() <= 0 ? 0.0F : stirling.powerBuffer * 50.0F / (float) stirling.maxHeat();
        if (stirling.kind().creative()) {
            momentum = Math.min(momentum, 45.0F);
        }
        stirling.lastSpin = stirling.spin;
        stirling.spin += momentum;
        if (stirling.spin >= 360.0F) {
            stirling.spin -= 360.0F;
            stirling.lastSpin -= 360.0F;
        }
    }

    private void tryPullHeat(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos.below());
        if (blockEntity instanceof HeatSource source) {
            int pulled = (int) (source.getHeatStored() * DIFFUSION);
            if (pulled > 0) {
                source.useUpHeat(pulled);
                heat += pulled;
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    public StirlingBlock.Kind kind() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof StirlingBlock block ? block.kind() : StirlingBlock.Kind.NORMAL;
    }

    public int maxHeat() {
        return kind() == StirlingBlock.Kind.NORMAL ? MAX_HEAT_NORMAL : MAX_HEAT_STEEL;
    }

    public int gearMeta() {
        return kind().gearMeta();
    }

    public boolean hasCog() {
        return hasCog;
    }

    private void spawnCog(Level level, BlockPos pos, Direction facing) {
        CogEntity cog = new CogEntity(level,
                pos.getX() + 0.5D + facing.getStepX(),
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D + facing.getStepZ());
        cog.setOrientation(MachinePartProjectileEntity.legacyOrientation(facing));
        cog.setMeta(gearMeta());
        Direction side = MachinePartProjectileEntity.legacyDownRotation(facing);
        cog.setDeltaMovement(side.getStepX(), 1.0D + (heat - maxHeat()) * 0.0001D, side.getStepZ());
        level.addFreshEntity(cog);
    }

    public void installCog() {
        hasCog = true;
        overspeed = 0;
        warnCooldown = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public long powerBuffer() {
        return powerBuffer;
    }

    public int heat() {
        return heat;
    }

    public float getSpin(float partialTick) {
        return lastSpin + (spin - lastSpin) * partialTick;
    }

    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(heat + "TU/t"));
        lines.add(Component.literal((hasCog ? powerBuffer : 0L) + "HE/t"));
        if (!kind().creative()) {
            int maxHeat = maxHeat();
            lines.add(legacyHeatPercent(heat, maxHeat));
            if (heat > maxHeat) {
                lines.add(LegacyLookOverlayLines.blinkingWarning("OVERSPEED"));
            }
            if (!hasCog) {
                lines.add(LegacyLookOverlayLines.error("Gear missing!"));
            }
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    private static Component legacyHeatPercent(int heat, int maxHeat) {
        if (maxHeat <= 0) {
            return Component.literal("0.0%").withStyle(ChatFormatting.RED);
        }
        double percent = (double) heat / (double) maxHeat;
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);
        if (percent > 1.0D) {
            color = 0xFF0000;
        }
        double shown = (heat * 1000 / maxHeat) / 10.0D;
        final int textColor = color;
        return Component.literal(shown + "%").withStyle(style -> style.withColor(textColor));
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }

    @Override
    public long getPower() {
        return powerBuffer;
    }

    @Override
    public void setPower(long power) {
        powerBuffer = Math.max(0L, power);
        energy.setMaxPower(powerBuffer);
        energy.setPower(powerBuffer);
    }

    @Override
    public long getMaxPower() {
        return powerBuffer;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(
                EnergyPort.of(2, 0, 0, Direction.EAST),
                EnergyPort.of(-2, 0, 0, Direction.WEST),
                EnergyPort.of(0, 0, 2, Direction.SOUTH),
                EnergyPort.of(0, 0, -2, Direction.NORTH));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("powerBuffer", powerBuffer);
        tag.putBoolean("hasCog", hasCog);
        tag.putInt("overspeed", overspeed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        powerBuffer = Math.max(0L, tag.getLong("powerBuffer"));
        hasCog = !tag.contains("hasCog") || tag.getBoolean("hasCog");
        overspeed = Math.max(0, tag.getInt("overspeed"));
        readRuntimeSync(tag);
        refreshEnergyState();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong("powerBuffer", powerBuffer);
        tag.putInt("heat", heat);
        tag.putBoolean("hasCog", hasCog);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains("powerBuffer")) {
            powerBuffer = Math.max(0L, tag.getLong("powerBuffer"));
        }
        readRuntimeSync(tag);
        refreshEnergyState();
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

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("heat")) {
            heat = Math.max(0, tag.getInt("heat"));
        }
        if (tag.contains("hasCog")) {
            hasCog = tag.getBoolean("hasCog");
        }
    }

    private void refreshEnergyState() {
        energy.setMaxPower(powerBuffer);
        energy.setTransferRates(0L, powerBuffer);
        energy.setPower(hasCog ? powerBuffer : 0L);
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        if (!hasCog) {
            persistent.putBoolean("missingCog", true);
        }
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        if (persistent.getBoolean("missingCog")) {
            hasCog = false;
        }
    }
}
