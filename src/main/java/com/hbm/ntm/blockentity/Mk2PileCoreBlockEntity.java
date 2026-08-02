package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.Mk2PileStructureBlock;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacyBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.item.Mk2PileRodItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Containers;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Saved geometric contract of one source-built 1.7.10 MK2 Pile cube. */
public final class Mk2PileCoreBlockEntity extends BlockEntity implements LegacyLookOverlayProvider {
    private int height;
    private int width;
    private int depth;
    private int left;
    private int up;
    private Direction facing = Direction.NORTH;
    private boolean restoring;
    private boolean meltingDown;
    private final List<Channel> fuelChannels = new ArrayList<>();
    private final List<Channel> ventilationChannels = new ArrayList<>();
    private final List<Channel> controlChannels = new ArrayList<>();
    private double highestHeat;

    public Mk2PileCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MK2_PILE_CORE.get(), pos, state);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(Mk2PileConnectedTextureData.CONNECTION_MASK,
                Mk2PileConnectedTextureData.connectionMask(level, worldPosition)).build();
    }

    public void configure(int height, int width, int depth, int left, int up, Direction facing) {
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.left = left;
        this.up = up;
        this.facing = facing == null || !facing.getAxis().isHorizontal() ? Direction.NORTH : facing;
        setChanged();
    }

    public boolean isRestoring() {
        return restoring || meltingDown;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, Mk2PileCoreBlockEntity core) {
        core.runSimulation();
        core.handleVentilation();
        core.highestHeat = core.fuelChannels.stream().mapToDouble(channel -> channel.heat).max().orElse(0.0D);
        if (core.handleMeltdown()) return;
        core.setChanged();
        // TileEntityPileCore networkPackNT(25) sends highestHeat to nearby clients every server tick.
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    /** Source: TileEntityPileCore#handleMeltdown (strictly greater than 800). */
    private boolean handleMeltdown() {
        if (level == null || level.isClientSide || meltingDown || highestHeat <= 800.0D || fuelChannels.isEmpty()) {
            return false;
        }

        double averageX = 0.0D;
        double averageZ = 0.0D;
        for (Channel channel : fuelChannels) {
            averageX += channel.entry.getX() + 0.5D + channel.direction.getStepX() * (channel.length - 1) / 2.0D;
            averageZ += channel.entry.getZ() + 0.5D + channel.direction.getStepZ() * (channel.length - 1) / 2.0D;
        }
        averageX /= fuelChannels.size();
        averageZ /= fuelChannels.size();

        meltingDown = true;
        try {
            // Legacy destroy() replaces only the core.  The following explosion removes the remaining structure.
            level.setBlock(worldPosition, ModBlocks.PILE_BRICK.get().defaultBlockState(), Block.UPDATE_ALL);
            WeaponExplosionUtil.explodeStandard(level, averageX, worldPosition.getY() + up, averageZ, 15.0F,
                    null, true, true);

            for (int index = 0; index < 15; index++) {
                double verticalHeading = level.random.nextDouble() * 0.5D + 1.0D;
                BulletLaunchUtil.LaunchPlan launch = BulletLaunchUtil.directedMk4LaunchPlan(LegacyBulletConfigs.PILE_DEBRIS,
                        new Vec3(averageX, worldPosition.getY() + up + 1.0D, averageZ),
                        new Vec3(0.0D, verticalHeading, 0.0D), 1.0F, 0.35F, level.random);
                if (launch.valid()) {
                    BulletProjectileEntity fragment = BulletProjectileEntity.fromLaunchPlan(level, launch, null);
                    fragment.overrideDamage = 100.0F;
                    level.addFreshEntity(fragment);
                }
            }
        } finally {
            meltingDown = false;
        }
        return true;
    }

    public boolean drillChannel(BlockPos entry, Direction direction) {
        if (level == null || !direction.getAxis().isHorizontal() && direction.getAxis() != Direction.Axis.Y) return false;
        Channel.Type type = Channel.Type.of(direction, facing);
        int length = type == Channel.Type.CONTROL ? height : type == Channel.Type.FUEL ? depth : width;
        List<Channel> channels = channels(type);
        Channel existing = channels.stream().filter(channel -> channel.entry.equals(entry) && channel.direction == direction)
                .findFirst().orElse(null);
        if (existing != null) {
            channels.remove(existing);
            if (existing.type == Channel.Type.FUEL) existing.ejectAll();
            for (int step = 0; step < length; step++) setRole(entry.relative(direction, step), Mk2PileStructureBlock.Role.DUMMY);
            setChanged();
            return true;
        }
        for (int step = 0; step < length; step++) {
            BlockState state = level.getBlockState(entry.relative(direction, step));
            if (!state.is(ModBlocks.PILE_BLOCK.get()) || state.getValue(Mk2PileStructureBlock.ROLE) != Mk2PileStructureBlock.Role.DUMMY) return false;
        }
        for (int step = 0; step < length; step++) {
            Mk2PileStructureBlock.Role role = step == 0 ? type.inRole : step == length - 1 ? type.outRole : Mk2PileStructureBlock.Role.CHANNEL;
            setRole(entry.relative(direction, step), role);
        }
        channels.add(new Channel(entry, direction, type, length));
        setChanged();
        return true;
    }

    /** Device-facing equivalent of the old getFuelChannel(x,y,z). */
    public boolean loadFuelRod(BlockPos entry, ItemStack stack) {
        Channel channel = findChannel(fuelChannels, entry);
        if (channel == null || stack.isEmpty() || !(stack.getItem() instanceof Mk2PileRodItem)) return false;
        channel.load(stack.copyWithCount(1));
        setChanged();
        return true;
    }

    public ItemStack lastFuelRod(BlockPos entry) {
        Channel channel = findChannel(fuelChannels, entry);
        return channel == null || channel.rods.length == 0 ? ItemStack.EMPTY : channel.rods[channel.rods.length - 1].copy();
    }

    public double fuelHeat(BlockPos entry) {
        Channel channel = findChannel(fuelChannels, entry);
        return channel == null ? 0.0D : channel.heat;
    }

    public double highestHeat() {
        return highestHeat;
    }

    /** Old BlockPile#printHook: only the core itself shows the pile-wide maximum temperature. */
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        if (!worldPosition.equals(viewedPos)) {
            return null;
        }
        return LegacyLookOverlay.forBlock(this, List.of(Component.literal("Max Temp: "
                + Math.round(highestHeat) + " / 800°C")));
    }

    public int fillVentilation(BlockPos entry, int amount) {
        Channel channel = findChannel(ventilationChannels, entry);
        if (channel == null || amount <= 0) return 0;
        int accepted = Math.min(amount, Channel.MAX_AIR - channel.air);
        channel.air += accepted;
        if (accepted > 0) setChanged();
        return accepted;
    }

    public boolean setControlLevel(BlockPos entry, double level) {
        Channel channel = findChannel(controlChannels, entry);
        if (channel == null) return false;
        channel.control = Math.max(0.0D, Math.min(1.0D, level));
        setChanged();
        return true;
    }

    private Channel findChannel(List<Channel> channels, BlockPos entry) {
        return channels.stream().filter(channel -> channel.entry.equals(entry)).findFirst().orElse(null);
    }

    private void setRole(BlockPos pos, Mk2PileStructureBlock.Role role) {
        BlockState current = level.getBlockState(pos);
        level.setBlock(pos, current.setValue(Mk2PileStructureBlock.ROLE, role), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof Mk2PileMemberBlockEntity member) member.setCorePos(worldPosition);
    }

    private List<Channel> channels(Channel.Type type) {
        return switch (type) { case FUEL -> fuelChannels; case VENTILATION -> ventilationChannels; case CONTROL -> controlChannels; };
    }

    private void runSimulation() {
        for (Channel channel : fuelChannels) {
            double output = 0.0D;
            for (int slot = 0; slot < channel.rods.length; slot++) {
                ItemStack stack = channel.rods[slot];
                if (!stack.isEmpty() && stack.getItem() instanceof Mk2PileRodItem) {
                    double neutrons = Mk2PileRodItem.reactivity(stack, channel.incoming / channel.length);
                    output += neutrons;
                    channel.heat += neutrons * Mk2PileRodItem.heatPerNeutron(stack);
                    channel.rods[slot] = Mk2PileRodItem.react(stack, neutrons);
                }
            }
            channel.outgoing = output;
            channel.incoming = 0.0D;
        }
        List<Segment> segments = segments();
        for (Segment segment : segments) {
            if (!segment.fuel.isEmpty()) {
                double outgoing = segment.fuel.stream().mapToDouble(channel -> channel.outgoing).sum();
                for (Channel channel : segment.fuel) channel.incoming += outgoing;
            }
        }
        for (int source = 1; source < width - 1; source++) {
            Segment origin = segments.get(source);
            if (origin.fuel.isEmpty()) continue;
            double outgoing = origin.fuel.stream().mapToDouble(channel -> channel.outgoing).sum();
            double multiplier = 1.0D;
            for (int target = source - 1; target >= 1; target--) {
                Segment segment = segments.get(target);
                multiplier *= segment.neutronMultiplier(depth);
                for (Channel channel : segment.fuel) channel.incoming += outgoing * multiplier;
            }
            multiplier = 1.0D;
            for (int target = source + 1; target < width - 1; target++) {
                Segment segment = segments.get(target);
                multiplier *= segment.neutronMultiplier(depth);
                for (Channel channel : segment.fuel) channel.incoming += outgoing * multiplier;
            }
        }
    }

    private List<Segment> segments() {
        List<Segment> segments = new ArrayList<>();
        for (int index = 0; index < width; index++) segments.add(new Segment());
        for (Channel channel : fuelChannels) {
            int index = segmentIndex(channel);
            if (index >= 0 && index < segments.size()) segments.get(index).fuel.add(channel);
        }
        for (Channel channel : controlChannels) {
            int index = segmentIndex(channel);
            if (index >= 0 && index < segments.size()) segments.get(index).control.add(channel);
        }
        return segments;
    }

    private int segmentIndex(Channel channel) {
        Direction side = facing.getCounterClockWise();
        return Math.abs((channel.entry.getX() - worldPosition.getX()) * side.getStepX()
                + (channel.entry.getZ() - worldPosition.getZ()) * side.getStepZ()) + left;
    }

    private void handleVentilation() {
        for (Channel vent : ventilationChannels) {
            if (vent.air <= 0) continue;
            double air = (double) vent.air / Channel.MAX_AIR;
            for (Channel fuel : fuelChannels) if (Math.abs(fuel.entry.getY() - vent.entry.getY()) <= 1) fuel.heat *= 1.0D - air * 0.05D;
            vent.air -= (int) Math.ceil(air * 5.0D);
        }
        for (Channel fuel : fuelChannels) fuel.heat = Math.max(20.0D, fuel.heat * 0.999D);
    }

    /** Exact old break contract: every still-present pile member becomes graphite brick. */
    public void restoreBricks() {
        if (level == null || level.isClientSide || restoring || width < 1 || height < 1 || depth < 1) {
            return;
        }
        restoring = true;
        Direction leftDirection = facing.getCounterClockWise();
        try {
            int down = height - up - 1;
            for (int vertical = -down; vertical <= up; vertical++) {
                for (int lateral = -left; lateral < width - left; lateral++) {
                    for (int forward = 0; forward < depth; forward++) {
                        BlockPos target = worldPosition.relative(leftDirection, lateral).relative(facing, forward)
                                .above(vertical);
                        BlockState state = level.getBlockState(target);
                        if (state.is(ModBlocks.PILE_BLOCK.get())) {
                            level.setBlock(target, ModBlocks.PILE_BRICK.get().defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        } finally {
            restoring = false;
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && !restoring) {
            for (Channel channel : fuelChannels) channel.ejectAll();
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("height", height);
        tag.putInt("width", width);
        tag.putInt("depth", depth);
        tag.putInt("left", left);
        tag.putInt("up", up);
        tag.putInt("orientation", facing.get3DDataValue());
        tag.put("fuel", writeChannels(fuelChannels));
        tag.put("vent", writeChannels(ventilationChannels));
        tag.put("control", writeChannels(controlChannels));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        height = tag.getInt("height");
        width = tag.getInt("width");
        depth = tag.getInt("depth");
        left = tag.getInt("left");
        up = tag.getInt("up");
        Direction loaded = Direction.from3DDataValue(tag.getInt("orientation"));
        facing = loaded.getAxis().isHorizontal() ? loaded : Direction.NORTH;
        readChannels(tag.getList("fuel", CompoundTag.TAG_COMPOUND), fuelChannels, Channel.Type.FUEL);
        readChannels(tag.getList("vent", CompoundTag.TAG_COMPOUND), ventilationChannels, Channel.Type.VENTILATION);
        readChannels(tag.getList("control", CompoundTag.TAG_COMPOUND), controlChannels, Channel.Type.CONTROL);
    }

    public static void restoreFrom(Level level, BlockPos corePos) {
        if (level.getBlockEntity(corePos) instanceof Mk2PileCoreBlockEntity core) {
            core.restoreBricks();
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putDouble("highestHeat", highestHeat);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        highestHeat = tag.getDouble("highestHeat");
    }

    private static ListTag writeChannels(List<Channel> channels) {
        ListTag result = new ListTag();
        for (Channel channel : channels) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("entry", channel.entry.asLong()); tag.putByte("direction", (byte) channel.direction.get3DDataValue());
            tag.putDouble("heat", channel.heat); tag.putDouble("incoming", channel.incoming); tag.putDouble("control", channel.control); tag.putInt("air", channel.air);
            ListTag rods = new ListTag();
            for (int slot = 0; slot < channel.rods.length; slot++) if (!channel.rods[slot].isEmpty()) {
                CompoundTag rod = new CompoundTag(); rod.putByte("slot", (byte) slot); channel.rods[slot].save(rod); rods.add(rod);
            }
            tag.put("items", rods); result.add(tag);
        }
        return result;
    }

    private void readChannels(ListTag tags, List<Channel> destination, Channel.Type type) {
        destination.clear();
        for (int index = 0; index < tags.size(); index++) {
            CompoundTag tag = tags.getCompound(index); Direction direction = Direction.from3DDataValue(tag.getByte("direction"));
            Channel channel = new Channel(BlockPos.of(tag.getLong("entry")), direction, type,
                    type == Channel.Type.CONTROL ? height : type == Channel.Type.FUEL ? depth : width);
            channel.heat = tag.getDouble("heat"); channel.incoming = tag.getDouble("incoming"); channel.control = tag.getDouble("control"); channel.air = tag.getInt("air");
            ListTag rods = tag.getList("items", CompoundTag.TAG_COMPOUND);
            for (int rodIndex = 0; rodIndex < rods.size(); rodIndex++) { CompoundTag rod = rods.getCompound(rodIndex); int slot = rod.getByte("slot"); if (slot >= 0 && slot < channel.rods.length) channel.rods[slot] = ItemStack.of(rod); }
            destination.add(channel);
        }
    }

    private final class Channel {
        static final int MAX_AIR = 1_000;
        final BlockPos entry; final Direction direction; final Type type; final int length; final ItemStack[] rods;
        double heat, outgoing, incoming, control = 1.0D; int air;
        Channel(BlockPos entry, Direction direction, Type type, int length) {
            this.entry = entry.immutable(); this.direction = direction; this.type = type; this.length = length; this.rods = new ItemStack[length];
            java.util.Arrays.fill(rods, ItemStack.EMPTY);
        }
        void load(ItemStack stack) {
            if (rods.length == 0) { drop(stack, -1); return; }
            for (int index = 0; index < rods.length; index++) {
                if (rods[index].isEmpty()) { rods[index] = stack; return; }
                ItemStack previous = rods[index]; rods[index] = stack; stack = previous;
            }
            drop(stack, length);
        }
        void ejectAll() {
            for (int index = 0; index < rods.length; index++) {
                drop(rods[index], length);
                rods[index] = ItemStack.EMPTY;
            }
        }
        void drop(ItemStack stack, int distance) {
            if (stack == null || stack.isEmpty() || Mk2PileCoreBlockEntity.this.level == null) return;
            ItemStack output = stack.copy();
            if (output.hasTag()) {
                output.getTag().remove(Mk2PileRodItem.DEPLETION_KEY);
                if (output.getTag().isEmpty()) output.setTag(null);
            }
            BlockPos outputPos = entry.relative(direction, distance);
            Containers.dropItemStack(Mk2PileCoreBlockEntity.this.level, outputPos.getX() + 0.5D, outputPos.getY() + 0.5D,
                    outputPos.getZ() + 0.5D, output);
        }
        enum Type {
            FUEL(Mk2PileStructureBlock.Role.FUEL_IN, Mk2PileStructureBlock.Role.FUEL_OUT),
            VENTILATION(Mk2PileStructureBlock.Role.AIR_IN, Mk2PileStructureBlock.Role.AIR_OUT),
            CONTROL(Mk2PileStructureBlock.Role.CONTROL, Mk2PileStructureBlock.Role.CONTROL);
            final Mk2PileStructureBlock.Role inRole, outRole;
            Type(Mk2PileStructureBlock.Role inRole, Mk2PileStructureBlock.Role outRole) { this.inRole=inRole; this.outRole=outRole; }
            static Type of(Direction direction, Direction facing) {
                if (direction.getAxis().isVertical()) return CONTROL;
                return direction.getAxis() == facing.getAxis() ? FUEL : VENTILATION;
            }
        }
    }

    private static final class Segment {
        final List<Channel> fuel = new ArrayList<>();
        final List<Channel> control = new ArrayList<>();
        double neutronMultiplier(int depth) {
            if (control.isEmpty() || depth < 4) return control.isEmpty() ? 1.0D : 0.0D;
            double total = control.stream().mapToDouble(channel -> channel.control).sum();
            return Math.max(0.0D, Math.min(0.5D, total / (depth - 1.0D)));
        }
    }
}
