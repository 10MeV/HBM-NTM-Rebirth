package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.Nullable;

public class ThresherBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, HbmFluidCopiable, LegacyLookOverlayProvider {
    private static final int TANK_CAPACITY = 100;
    private static final float MAX_ANGLE = 82.5F;
    private static final int STATE_WAITING = 0;
    private static final int STATE_EXTENDING = 1;
    private static final int STATE_RETRACTING = 2;
    private static final List<FluidType> FUELS = List.of(
            HbmFluids.WOODOIL,
            HbmFluids.ETHANOL,
            HbmFluids.FISHOIL,
            HbmFluids.HEAVYOIL,
            HbmFluids.COALCREOSOTE);
    private final HbmFluidTank tank;
    private boolean on;
    private boolean suspended;
    private int delay;
    private int state;
    private float angle;
    private float previousAngle;
    private float targetAngle;
    private int turnProgress;
    private float spin;
    private float lastSpin;
    private Object audioLoop;

    public ThresherBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WOODOIL, TANK_CAPACITY));
    }

    private ThresherBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.THRESHER.get(), pos, state, List.of(tank));
        this.tank = tank;
        this.tank.conform(new HbmFluidStack(HbmFluids.WOODOIL, 0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, ThresherBlockEntity thresher) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, blockState, thresher);
        boolean oldOn = thresher.on;
        int oldFill = thresher.tank.getFill();
        float oldAngle = thresher.angle;
        if (!thresher.suspended && level.getGameTime() % 20L == 0L) {
            thresher.on = thresher.acceptsFuel(thresher.tank.getTankType()) && thresher.tank.drain(1, false) > 0;
        } else if (thresher.suspended) {
            thresher.on = false;
        }
        if (thresher.on && !thresher.suspended) {
            thresher.runThresher(level, pos, blockState);
        }
        thresher.networkPackNT(20);
        if (oldOn != thresher.on || oldFill != thresher.tank.getFill() || oldAngle != thresher.angle) {
            thresher.setChanged();
            level.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ThresherBlockEntity thresher) {
        thresher.previousAngle = thresher.angle;
        thresher.lastSpin = thresher.spin;
        if (thresher.turnProgress > 0) {
            thresher.angle += Mth.wrapDegrees(thresher.targetAngle - thresher.angle) / thresher.turnProgress;
            thresher.turnProgress--;
        } else {
            thresher.angle = thresher.targetAngle;
        }
        if (thresher.on && !thresher.suspended) {
            if (thresher.angle > 0.0F) {
                thresher.spin += 15.0F;
            }
            Direction dir = facing(state);
            Direction rot = dir.getClockWise();
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.5D + dir.getStepX() * 0.8125D + rot.getStepX() * 0.375D,
                    pos.getY() + 1.5625D,
                    pos.getZ() + 0.5D + dir.getStepZ() * 0.8125D + rot.getStepZ() * 0.375D,
                    0.0D, 0.0D, 0.0D);
        }
        if (thresher.spin >= 360.0F) {
            thresher.spin -= 360.0F;
            thresher.lastSpin -= 360.0F;
        }
        thresher.audioLoop = LegacyMachineAudioBridge.updateLoop(thresher.audioLoop, thresher,
                "hbm:block.turbofanOperate", thresher.on && !thresher.suspended, 15.0D, 10.0F, 1.0F, 1.0F);
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public float getAngle() {
        return angle;
    }

    public float getPreviousAngle() {
        return previousAngle;
    }

    public float getSpin() {
        return spin;
    }

    public float getLastSpin() {
        return lastSpin;
    }

    public void toggleSuspended() {
        suspended = !suspended;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void markFluidSettingsChanged() {
        if (!acceptsFuel(tank.getTankType())) {
            tank.setTankType(HbmFluids.WOODOIL);
        }
        onFluidContentsChanged();
    }

    private void runThresher(Level level, BlockPos pos, BlockState blockState) {
        if (state == STATE_WAITING) {
            delay--;
            if (delay <= 0) {
                state = STATE_EXTENDING;
            }
        }
        if (state == STATE_EXTENDING) {
            angle += MAX_ANGLE / 60.0F;
            if (angle >= MAX_ANGLE) {
                angle = MAX_ANGLE;
                state = STATE_RETRACTING;
            }
        } else if (state == STATE_RETRACTING) {
            angle -= MAX_ANGLE / 60.0F;
            if (angle <= 0.0F) {
                angle = 0.0F;
                state = STATE_WAITING;
                delay = 200 + level.random.nextInt(100);
            }
        }
        if (angle != 0.0F) {
            hitSweep(level, pos, blockState);
        }
    }

    private void hitSweep(Level level, BlockPos pos, BlockState blockState) {
        Direction dir = facing(blockState);
        Direction rot = dir.getClockWise();
        Vec3 pivot = new Vec3(pos.getX() + 0.5D - dir.getStepX(), pos.getY() + 0.5D,
                pos.getZ() + 0.5D - dir.getStepZ());
        Vec3 upperArm = new Vec3(-dir.getStepX() * 4.0D, 0.0D, -dir.getStepZ() * 4.0D);
        Vec3 lowerArm = new Vec3(-dir.getStepX() * 4.0D, 0.0D, -dir.getStepZ() * 4.0D);
        float armAngle = (float) Math.toRadians(MAX_ANGLE - angle);
        if (dir.getStepZ() != 0) {
            upperArm = upperArm.xRot(armAngle);
            lowerArm = lowerArm.xRot(-armAngle);
        }
        if (dir.getStepX() != 0) {
            upperArm = upperArm.zRot(armAngle);
            lowerArm = lowerArm.zRot(-armAngle);
        }
        Vec3 armTip = new Vec3(-dir.getStepX() * 2.0D, 0.0D, -dir.getStepZ() * 2.0D);
        double endX = pivot.x + upperArm.x + lowerArm.x + armTip.x;
        double endZ = pivot.z + upperArm.z + lowerArm.z + armTip.z;

        for (int i = -3; i <= 3; i++) {
            BlockPos hit = new BlockPos((int) Math.floor(endX + rot.getStepX() * i), pos.getY(),
                    (int) Math.floor(endZ + rot.getStepZ() * i));
            BlockState hitState = level.getBlockState(hit);
            if (hitState.isSolidRender(level, hit)) {
                state = STATE_RETRACTING;
                break;
            }
            if (hitState.getBlock() instanceof DoublePlantBlock) {
                handleDoublePlant(level, hit, hitState);
                continue;
            }
            if (hitState.getBlock() instanceof SugarCaneBlock || hitState.getBlock() instanceof CactusBlock) {
                cutCane(level, hit, hitState.getBlock());
                continue;
            }
            if (isMatureGrowable(level, hit, hitState)) {
                cutCrop(level, hit, hitState);
            }
        }

        AABB hitBox = new AABB(endX, pos.getY() + 0.5D, endZ, endX, pos.getY() + 0.5D, endZ)
                .inflate(Math.abs(dir.getStepX() * 0.5D) + Math.abs(rot.getStepX() * 4.5D), 0.5D,
                        Math.abs(dir.getStepZ() * 0.5D) + Math.abs(rot.getStepZ() * 4.5D));
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, hitBox)) {
            if (EntityDamageUtil.attackEntityFromNt(living, level.damageSources().generic(), 100.0F)) {
                if (living instanceof Monster && !living.isAlive()) {
                    dropFromBack(new ItemStack(ModItems.legacyItem("nitra_small").get()));
                }
                level.playSound(null, living.blockPosition(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                        SoundSource.HOSTILE, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);
                int count = Math.min((int) Math.ceil(living.getMaxHealth() / 4.0F), 250);
                ParticleUtil.spawnVanillaRedstoneBlockDustBurst(level, living.getX(),
                        living.getY() + living.getBbHeight() * 0.5D, living.getZ(), count * 4, 0.1D);
            }
        }
    }

    private void handleDoublePlant(Level level, BlockPos pos, BlockState state) {
        BlockState lowerState = state;
        BlockPos lowerPos = pos;
        if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            lowerPos = pos.below();
            lowerState = level.getBlockState(lowerPos);
        }
        if (lowerState.is(Blocks.SUNFLOWER) && level.random.nextInt(250) == 0) {
            level.levelEvent(2001, lowerPos, Block.getId(lowerState));
            dropFromBack(new ItemStack(Blocks.SUNFLOWER));
        } else if (lowerState.is(Blocks.TALL_GRASS) && level.random.nextInt(100) == 0) {
            level.levelEvent(2001, lowerPos, Block.getId(lowerState));
            dropFromBack(new ItemStack(Items.WHEAT_SEEDS));
        }
    }

    private void cutCane(Level level, BlockPos pos, Block target) {
        int offset = level.getBlockState(pos.below()).is(target) ? -1 : 0;
        for (int i = 2 + offset; i > offset; i--) {
            BlockPos cut = pos.above(i);
            BlockState state = level.getBlockState(cut);
            if (!state.is(target)) {
                continue;
            }
            level.levelEvent(2001, cut, Block.getId(state));
            dropResourcesFromBack(state, cut);
            level.setBlock(cut, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private boolean isMatureGrowable(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        return state.getBlock() instanceof BonemealableBlock growable
                && !growable.isValidBonemealTarget(level, pos, state, level.isClientSide);
    }

    private void cutCrop(Level level, BlockPos pos, BlockState state) {
        level.levelEvent(2001, pos, Block.getId(state));
        BlockState replacement = Blocks.AIR.defaultBlockState();
        List<ItemStack> drops = level instanceof ServerLevel serverLevel
                ? new ArrayList<>(Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos)))
                : List.of();
        boolean replanted = false;
        for (ItemStack drop : drops) {
            if (!replanted && drop.getItem() instanceof IPlantable plantable && drop.getCount() > 0) {
                BlockState plant = plantable.getPlant(level, pos);
                if (plant != null && canReplant(level, pos, plant)) {
                    replacement = plant;
                    drop.shrink(1);
                    replanted = true;
                }
            } else if (!replanted && drop.getItem() instanceof BlockItem blockItem) {
                BlockState plant = blockItem.getBlock().defaultBlockState();
                if (canReplant(level, pos, plant)) {
                    replacement = plant;
                    drop.shrink(1);
                    replanted = true;
                }
            }
            if (!drop.isEmpty()) {
                dropFromBack(drop);
            }
        }
        if (state.is(Blocks.WHEAT) && !replanted) {
            replacement = Blocks.WHEAT.defaultBlockState();
        }
        level.setBlock(pos, replacement, Block.UPDATE_ALL);
    }

    private boolean canReplant(Level level, BlockPos pos, BlockState plant) {
        return plant.canSurvive(level, pos);
    }

    private void dropResourcesFromBack(BlockState state, BlockPos dropPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ItemStack drop : Block.getDrops(state, serverLevel, dropPos, level.getBlockEntity(dropPos))) {
            dropFromBack(drop);
        }
    }

    private void dropFromBack(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return;
        }
        Direction metaDirection = facing(getBlockState()).getOpposite();
        double spawnX = worldPosition.getX() + 0.5D - metaDirection.getStepX() * 0.75D;
        double spawnZ = worldPosition.getZ() + 0.5D - metaDirection.getStepZ() * 0.75D;
        ItemEntity itemEntity = new ItemEntity(level, spawnX, worldPosition.getY(), spawnZ, stack.copy());
        itemEntity.setPickUpDelay(10);
        itemEntity.setDeltaMovement(metaDirection.getStepX() * -0.2D + 0.2D, itemEntity.getDeltaMovement().y,
                metaDirection.getStepZ() * -0.2D);
        level.addFreshEntity(itemEntity);
    }

    private boolean acceptsFuel(FluidType type) {
        return FUELS.contains(type);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!acceptsFuel(type)) {
            return amount;
        }
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return acceptsFuel(type) ? tank.getSpace() : 0L;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return acceptsFuel(type) && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction rot = facing(getBlockState()).getClockWise();
        return List.of(
                FluidPort.of(rot.getStepX(), 0, rot.getStepZ(), rot),
                FluidPort.of(-rot.getStepX(), 0, -rot.getStepZ(), rot.getOpposite()));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.UP ? HbmFluidSideMode.NONE : HbmFluidSideMode.INPUT;
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, new int[] { tank.getTankType().getId() });
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable net.minecraft.world.entity.player.Player player,
            boolean recursive) {
        if (tag == null || !tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            return false;
        }
        java.util.OptionalInt id = HbmFluidCopiable.copiedFluidIdAt(tag, index);
        if (id.isEmpty()) {
            return false;
        }
        FluidType type = HbmFluids.fromId(id.getAsInt());
        if (!acceptsFuel(type)) {
            return false;
        }
        tank.setTankType(type);
        onFluidContentsChanged();
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        lines.add(tank.getTankType().getDisplayName().copy()
                .append(Component.literal(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB")
                        .withStyle(ChatFormatting.RESET)));
        if (suspended) {
            lines.add(Component.literal("! ")
                    .append(Component.translatable("block.hbm_ntm_rebirth.machine_thresher.suspended"))
                    .append(Component.literal(" !"))
                    .withStyle(ChatFormatting.RED));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isOn", on);
        tag.putBoolean("isSuspended", suspended);
        tag.putFloat("angle", angle);
        tag.putInt("state", state);
        tank.writeToNbt(tag, "t");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        } else if (!tag.contains("hbm_fluids") && (tag.contains("tank") || tag.contains("tank_type"))) {
            tank.readFromNbt(tag, "tank");
        }
        on = tag.getBoolean("isOn");
        suspended = tag.getBoolean("isSuspended");
        angle = tag.getFloat("angle");
        previousAngle = angle;
        targetAngle = angle;
        state = tag.getInt("state");
        if (!acceptsFuel(tank.getTankType())) {
            tank.setTankType(HbmFluids.WOODOIL);
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isOn", on);
        tag.putBoolean("isSuspended", suspended);
        tag.putFloat("angle", angle);
        tank.writeToNbt(tag, "t");
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        on = tag.getBoolean("isOn");
        suspended = tag.getBoolean("isSuspended");
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        }
        targetAngle = tag.getFloat("angle");
        turnProgress = 3;
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
    public AABB getRenderBoundingBox() {
        Direction facing = facing(getBlockState());
        double centerX = worldPosition.getX() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;
        double cross = 4.75D;
        double front = 11.75D;
        double back = 1.25D;

        double minX;
        double maxX;
        double minZ;
        double maxZ;
        if (facing.getAxis() == Direction.Axis.Z) {
            minX = centerX - cross;
            maxX = centerX + cross;
            minZ = facing == Direction.NORTH ? centerZ - front : centerZ - back;
            maxZ = facing == Direction.NORTH ? centerZ + back : centerZ + front;
        } else {
            minX = facing == Direction.WEST ? centerX - front : centerX - back;
            maxX = facing == Direction.WEST ? centerX + back : centerX + front;
            minZ = centerZ - cross;
            maxZ = centerZ + cross;
        }
        return new AABB(minX, worldPosition.getY(), minZ, maxX, worldPosition.getY() + 7.0D, maxZ);
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(HorizontalMachineBlock.FACING) ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }
}
