package com.hbm.ntm.blockentity;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.item.AmsCatalystItem;
import com.hbm.ntm.item.AmsCoreItem;
import com.hbm.ntm.menu.DfcCoreMenu;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmWorldUtil;
import com.hbm.saveddata.satellites.SatelliteRayScan;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DfcCoreBlockEntity extends HbmFluidNetworkBlockEntity implements MenuProvider {
    public static final int SLOT_CATALYST_LEFT = 0;
    public static final int SLOT_CORE = 1;
    public static final int SLOT_CATALYST_RIGHT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int TANK_CAPACITY = 128_000;
    private static final float LEGACY_MELTDOWN_CHUNK_RADIATION = 100.0F;
    private static final float LEGACY_AMS_DAMAGE = 1000.0F;
    private static final float LEGACY_AMS_CORE_DAMAGE = 10000.0F;
    private static final int LEGACY_AMS_FIRE_SECONDS = 3;

    private static final String TAG_ITEMS = "items";

    private final HbmFluidTank fuel1;
    private final HbmFluidTank fuel2;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_CATALYST_LEFT, SLOT_CATALYST_RIGHT -> stack.getItem() instanceof AmsCatalystItem;
                case SLOT_CORE -> stack.getItem() instanceof AmsCoreItem;
                default -> false;
            };
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int field;
    private int heat;
    private int color;
    private boolean lastTickValid;
    private boolean meltdownTick;
    private int consumption;
    private int prevConsumption;

    public DfcCoreBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.DEUTERIUM, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.TRITIUM, TANK_CAPACITY));
    }

    private DfcCoreBlockEntity(BlockPos pos, BlockState state, HbmFluidTank fuel1, HbmFluidTank fuel2) {
        super(ModBlockEntities.DFC_CORE.get(), pos, state, List.of(fuel1, fuel2));
        this.fuel1 = fuel1;
        this.fuel2 = fuel2;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DfcCoreBlockEntity core) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, core);
        core.tickServer(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DfcCoreBlockEntity core) {
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        prevConsumption = consumption;
        consumption = 0;
        meltdownTick = false;
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        lastTickValid = level.hasChunk(chunkX, chunkZ)
                && level.hasChunk(chunkX + 1, chunkZ + 1)
                && level.hasChunk(chunkX + 1, chunkZ - 1)
                && level.hasChunk(chunkX - 1, chunkZ + 1)
                && level.hasChunk(chunkX - 1, chunkZ - 1);
        color = calculateCoreColor();
        if (lastTickValid && heat > 0 && heat >= field) {
            handleLegacyMeltdown(level, pos);
        }
        if (heat > 0) {
            applyLegacyAmsRadiation(level, pos);
            if (level.getGameTime() % 100L == 0L) {
                SatelliteRayScan.reportEvent(level, pos.getX(), pos.getY(), pos.getZ(),
                        SatelliteRayScan.RayEvent.INFO_PARTICLE, 200);
            }
        }
        networkPackNT(250);
        heat = 0;
        if (lastTickValid && field > 0) {
            field--;
        }
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private void handleLegacyMeltdown(Level level, BlockPos pos) {
        int size = calculateLegacyMeltdownSize();
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        if (!NukeExplosionMk3Entity.hasAntiTeleportOverlap(level, x, y, z)) {
            level.addFreshEntity(NukeExplosionMk3Entity.createFleija(level, x, y, z, size));
            LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(), "random.explode",
                    100000.0F, 1.0F);
            level.addFreshEntity(CloudFleijaRainbowEntity.create(level, pos.getX(), pos.getY(), pos.getZ(), size));
            return;
        }
        meltdownTick = true;
        ChunkRadiationManager.proxy.incrementRad(level, pos, LEGACY_MELTDOWN_CHUNK_RADIATION);
    }

    private int calculateLegacyMeltdownSize() {
        int fill = fuel1.getFill() + fuel2.getFill();
        int max = fuel1.getMaxFill() + fuel2.getMaxFill();
        if (max <= 0) {
            return 50;
        }
        int mod = heat * 10;
        return Math.max(Math.min(fill * mod / max, 1000), 50);
    }

    private void applyLegacyAmsRadiation(Level level, BlockPos pos) {
        double scale = meltdownTick ? 5.0D : 3.0D;
        double range = meltdownTick ? 50.0D : 10.0D;
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 origin = new Vec3(pos.getX() + 0.5D, pos.getY() + 6.5D, pos.getZ() + 0.5D);

        for (Entity entity : level.getEntities(null, centeredBox(center, range))) {
            if (entity instanceof Player player && ArmorUtil.checkForHazmat(player)) {
                continue;
            }
            Vec3 target = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (!HbmWorldUtil.isObstructed(level, origin, target)) {
                entity.hurt(ModDamageSources.source(level, ModDamageSources.AMS), LEGACY_AMS_DAMAGE);
                entity.setSecondsOnFire(LEGACY_AMS_FIRE_SECONDS);
            }
        }

        for (Entity entity : level.getEntities(null, centeredBox(center, scale))) {
            if (entity instanceof Player player && ArmorUtil.checkForHaz2(player)) {
                continue;
            }
            entity.hurt(ModDamageSources.source(level, ModDamageSources.AMS_CORE), LEGACY_AMS_CORE_DAMAGE);
        }
    }

    private static AABB centeredBox(Vec3 center, double radius) {
        return new AABB(center.x, center.y, center.z, center.x, center.y, center.z).inflate(radius);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getFuel1() {
        return fuel1;
    }

    public HbmFluidTank getFuel2() {
        return fuel2;
    }

    public int getField() {
        return field;
    }

    public int getHeat() {
        return heat;
    }

    public int getColor() {
        return color;
    }

    public boolean isMeltdownTick() {
        return meltdownTick;
    }

    public int getPrevConsumption() {
        return prevConsumption;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getFieldScaled(int height) {
        return field * height / 100;
    }

    public int getHeatScaled(int height) {
        return heat * height / 100;
    }

    public long burn(long joules) {
        if (!isReady()) {
            return joules;
        }
        int demand = (int) Math.ceil(joules / 1000.0D);
        if (fuel1.getFill() < demand || fuel2.getFill() < demand) {
            return joules;
        }
        consumption += demand;
        heat += (int) Math.ceil(joules / 10000.0D);
        fuel1.setFill(fuel1.getFill() - demand);
        fuel2.setFill(fuel2.getFill() - demand);
        setChanged();
        return (long) (joules * getCoreMultiplier()
                * getFuelEfficiency(fuel1.getTankType())
                * getFuelEfficiency(fuel2.getTankType()));
    }

    public boolean isReady() {
        return lastTickValid
                && getCoreMultiplier() > 0
                && color != 0
                && getFuelEfficiency(fuel1.getTankType()) > 0.0F
                && getFuelEfficiency(fuel2.getTankType()) > 0.0F;
    }

    public void applyField(int watts) {
        field = Math.max(field, watts);
        setChanged();
    }

    public static float getFuelEfficiency(FluidType type) {
        if (type == HbmFluids.HYDROGEN) return 1.0F;
        if (type == HbmFluids.DEUTERIUM) return 1.5F;
        if (type == HbmFluids.TRITIUM) return 1.7F;
        if (type == HbmFluids.OXYGEN) return 1.2F;
        if (type == HbmFluids.PEROXIDE) return 1.4F;
        if (type == HbmFluids.XENON) return 1.5F;
        if (type == HbmFluids.SAS3) return 2.0F;
        if (type == HbmFluids.BALEFIRE) return 2.5F;
        if (type == HbmFluids.AMAT) return 2.2F;
        if (type == HbmFluids.ASCHRAB) return 2.7F;
        return 0.0F;
    }

    private int getCoreMultiplier() {
        ItemStack stack = items.getStackInSlot(SLOT_CORE);
        return stack.getItem() instanceof AmsCoreItem core ? core.dfcMultiplier() : 0;
    }

    private int calculateCoreColor() {
        ItemStack left = items.getStackInSlot(SLOT_CATALYST_LEFT);
        ItemStack right = items.getStackInSlot(SLOT_CATALYST_RIGHT);
        if (!(left.getItem() instanceof AmsCatalystItem leftCatalyst)
                || !(right.getItem() instanceof AmsCatalystItem rightCatalyst)) {
            return 0;
        }
        return averageColor(leftCatalyst.color(), rightCatalyst.color());
    }

    private static int averageColor(int first, int second) {
        int r = (((first & 0xFF0000) >> 16) + ((second & 0xFF0000) >> 16)) / 2;
        int g = (((first & 0x00FF00) >> 8) + ((second & 0x00FF00) >> 8)) / 2;
        int b = ((first & 0x0000FF) + (second & 0x0000FF)) / 2;
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.dfcCore", "DFC Core");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DfcCoreMenu(containerId, inventory, this);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return false;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.NONE;
    }

    @Override
    public boolean supportsFluidSettingsCopy() {
        return false;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, -4, -4), worldPosition.offset(5, 5, 5));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt("field", field);
        fuel1.writeToNbt(tag, "fuel1");
        fuel2.writeToNbt(tag, "fuel2");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        field = tag.getInt("field");
        fuel1.readFromNbt(tag, "fuel1");
        fuel2.readFromNbt(tag, "fuel2");
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("heat", heat);
        tag.putInt("color", color);
        tag.putBoolean("meltdownTick", meltdownTick);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        heat = tag.getInt("heat");
        color = tag.getInt("color");
        meltdownTick = tag.getBoolean("meltdownTick");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
