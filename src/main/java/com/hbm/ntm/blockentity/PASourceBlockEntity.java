package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

public class PASourceBlockEntity extends PABlockEntity {
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_CONTAINER_1 = 3;
    public static final int SLOT_CONTAINER_2 = 4;
    public static final long USAGE = 100_000L;
    private static final String TAG_PARTICLE = "particle";
    private static final String TAG_DEBUG_SPEED = "debugSpeed";
    private static final String TAG_LAST_SPEED = "lastSpeed";
    private static final String TAG_STATE = "state";

    private Particle particle;
    private PAState state = PAState.IDLE;
    private int debugSpeed;
    private int lastSpeed;

    public PASourceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_SOURCE.get(), pos, state, ParticleAcceleratorBlock.Variant.SOURCE, 5, 10_000_000L);
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (particle != null) {
            int steps = 1 + Mth.clamp(particle.momentum / 1_000, 0, 9);
            for (int i = 0; i < steps && particle != null; i++) {
                state = PAState.RUNNING;
                stepParticle();
                debugSpeed = particle == null ? 0 : particle.momentum;
                if (particle != null && particle.invalid) {
                    particle = null;
                }
            }
        } else if (getPower() >= USAGE
                && !items.getStackInSlot(SLOT_INPUT_1).isEmpty()
                && !items.getStackInSlot(SLOT_INPUT_2).isEmpty()) {
            tryRun();
        }
        super.serverTick();
    }

    @Override
    public long getUsage() {
        return USAGE;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) || slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2;
    }

    @Override
    public List<EnergyPort> energyPorts() {
        return fluidPorts().stream().map(port -> energyPort(port.offset(), port.direction())).toList();
    }

    @Override
    public List<FluidPort> fluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                fluidPort(rel(facing, 2), facing),
                fluidPort(rel(facing, 2).offset(rel(side, 2)), facing),
                fluidPort(rel(facing, 2).offset(rel(side, -2)), facing),
                fluidPort(rel(facing, -2), facing.getOpposite()),
                fluidPort(rel(facing, -2).offset(rel(side, 2)), facing.getOpposite()),
                fluidPort(rel(facing, -2).offset(rel(side, -2)), facing.getOpposite()),
                fluidPort(rel(side, 5), side));
    }

    public void stepParticle() {
        if (particle == null || level == null) {
            return;
        }
        if (!level.hasChunkAt(particle.pos())) {
            state = PAState.PAUSE_UNLOADED;
            return;
        }
        BlockEntity core = resolveCore(particle.pos());
        if (!(core instanceof PAParticleUser user)) {
            particle.crash(PAState.CRASH_DERAIL);
            return;
        }
        if (user.canParticleEnter(particle, particle.dir(), particle.pos())) {
            user.onParticleEnter(particle, particle.dir());
            BlockPos exit = user.getParticleExitPos(particle);
            if (exit != null) {
                particle.move(exit);
            }
        } else {
            particle.crash(PAState.CRASH_CANNOT_ENTER);
        }
    }

    public void tryRun() {
        if (!isCool() || !canMoveContainerRemainder(SLOT_INPUT_1, SLOT_CONTAINER_1)
                || !canMoveContainerRemainder(SLOT_INPUT_2, SLOT_CONTAINER_2)) {
            return;
        }
        moveContainerRemainder(SLOT_INPUT_1, SLOT_CONTAINER_1);
        moveContainerRemainder(SLOT_INPUT_2, SLOT_CONTAINER_2);
        setPower(getPower() - USAGE);
        Direction side = beamSide(facing());
        particle = new Particle(this, worldPosition.relative(side, 5), side,
                items.getStackInSlot(SLOT_INPUT_1).copy(), items.getStackInSlot(SLOT_INPUT_2).copy());
        items.setStackInSlot(SLOT_INPUT_1, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_INPUT_2, ItemStack.EMPTY);
        setChanged();
    }

    private boolean canMoveContainerRemainder(int inputSlot, int outputSlot) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty() || !input.hasCraftingRemainingItem()) {
            return true;
        }
        ItemStack remainder = input.getCraftingRemainingItem();
        ItemStack current = items.getStackInSlot(outputSlot);
        return current.isEmpty() || ItemStack.isSameItemSameTags(current, remainder)
                && current.getCount() + remainder.getCount() <= current.getMaxStackSize();
    }

    private void moveContainerRemainder(int inputSlot, int outputSlot) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty() || !input.hasCraftingRemainingItem()) {
            return;
        }
        ItemStack remainder = input.getCraftingRemainingItem();
        ItemStack current = items.getStackInSlot(outputSlot);
        if (current.isEmpty()) {
            items.setStackInSlot(outputSlot, remainder.copy());
        } else if (ItemStack.isSameItemSameTags(current, remainder)) {
            current.grow(remainder.getCount());
        }
    }

    public void updateState(PAState state) {
        this.state = state;
    }

    public PAState getState() {
        return state;
    }

    public int getLastSpeed() {
        return lastSpeed;
    }

    public int getDebugSpeed() {
        return debugSpeed;
    }

    @Override
    protected void loadPa(CompoundTag tag) {
        debugSpeed = tag.getInt(TAG_DEBUG_SPEED);
        lastSpeed = tag.getInt(TAG_LAST_SPEED);
        state = PAState.byOrdinal(tag.getInt(TAG_STATE));
        particle = tag.contains(TAG_PARTICLE) ? Particle.read(this, tag.getCompound(TAG_PARTICLE)) : null;
    }

    @Override
    protected void savePa(CompoundTag tag) {
        tag.putInt(TAG_DEBUG_SPEED, debugSpeed);
        tag.putInt(TAG_LAST_SPEED, lastSpeed);
        tag.putInt(TAG_STATE, state.ordinal());
        if (particle != null) {
            tag.put(TAG_PARTICLE, particle.write());
        }
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (tag.contains("cancel")) {
            particle = null;
            state = PAState.IDLE;
        }
    }

    public enum PAState {
        IDLE(0x8080ff),
        RUNNING(0xffff00),
        SUCCESS(0x00ff00),
        PAUSE_UNLOADED(0x808080),
        CRASH_DEFOCUS(0xff0000),
        CRASH_DERAIL(0xff0000),
        CRASH_CANNOT_ENTER(0xff0000),
        CRASH_NOCOOL(0xff0000),
        CRASH_NOPOWER(0xff0000),
        CRASH_NOCOIL(0xff0000),
        CRASH_OVERSPEED(0xff0000),
        CRASH_UNDERSPEED(0xff0000),
        CRASH_NORECIPE(0xff0000);

        private final int color;

        PAState(int color) {
            this.color = color;
        }

        public int color() {
            return color;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static PAState byOrdinal(int ordinal) {
            PAState[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : IDLE;
        }
    }

    public static final class Particle {
        public static final int MAX_DEFOCUS = 1000;
        private final PASourceBlockEntity source;
        private BlockPos pos;
        private Direction dir;
        private int momentum;
        private int defocus;
        private int distanceTraveled;
        private boolean invalid;
        private final ItemStack input1;
        private final ItemStack input2;

        public Particle(PASourceBlockEntity source, BlockPos pos, Direction dir, ItemStack input1, ItemStack input2) {
            this.source = source;
            this.pos = pos;
            this.dir = dir;
            this.input1 = input1;
            this.input2 = input2;
        }

        public void crash(PAState state) {
            invalid = true;
            source.updateState(state);
        }

        public void move(BlockPos pos) {
            this.pos = pos;
            source.lastSpeed = momentum;
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putInt("dir", dir.get3DDataValue());
            tag.putInt("momentum", momentum);
            tag.putInt("defocus", defocus);
            tag.putInt("dist", distanceTraveled);
            tag.put("input1", input1.save(new CompoundTag()));
            tag.put("input2", input2.save(new CompoundTag()));
            return tag;
        }

        public static Particle read(PASourceBlockEntity source, CompoundTag tag) {
            Particle particle = new Particle(source, new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
                    Direction.from3DDataValue(tag.getInt("dir")),
                    ItemStack.of(tag.getCompound("input1")),
                    ItemStack.of(tag.getCompound("input2")));
            particle.momentum = tag.getInt("momentum");
            particle.defocus = tag.getInt("defocus");
            particle.distanceTraveled = tag.getInt("dist");
            return particle;
        }

        public void addDistance(int dist) {
            distanceTraveled += dist;
        }

        public void resetDistance() {
            distanceTraveled = 0;
        }

        public void addMomentum(int amount) {
            momentum += amount;
        }

        public void defocus(int amount) {
            defocus += amount;
            if (defocus > MAX_DEFOCUS) {
                crash(PAState.CRASH_DEFOCUS);
            }
        }

        public void focus(int amount) {
            defocus = Math.max(0, defocus - amount);
        }

        public BlockPos pos() {
            return pos;
        }

        public Direction dir() {
            return dir;
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }

        public int momentum() {
            return momentum;
        }

        public int defocus() {
            return defocus;
        }

        public int distanceTraveled() {
            return distanceTraveled;
        }

        public boolean invalid() {
            return invalid;
        }

        public ItemStack input1() {
            return input1;
        }

        public ItemStack input2() {
            return input2;
        }
    }
}
