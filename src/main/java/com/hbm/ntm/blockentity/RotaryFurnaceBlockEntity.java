package com.hbm.ntm.blockentity;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayPorts;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.menu.RotaryFurnaceMenu;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.RotaryFurnaceRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.util.CrucibleUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RotaryFurnaceBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, LegacyProxyDelegateProvider {
    public static final int SLOT_INPUT_0 = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_FLUID_ID = 3;
    public static final int SLOT_FUEL = 4;
    public static final int SLOT_COUNT = 5;

    public static final int INPUT_CAPACITY = 16_000;
    public static final int STEAM_CAPACITY = 12_000;
    public static final int SPENT_STEAM_CAPACITY = 120;
    public static final int SMOKE_CAPACITY = 50;
    public static final int MAX_OUTPUT = MaterialShapes.BLOCK.q(16);

    private static final String TAG_ITEMS = "items";
    private static final String TAG_MODERN_ITEMS_FALLBACK = "Items";
    private static final String TAG_PROGRESS = "prog";
    private static final String TAG_BURN = "burn";
    private static final String TAG_HEAT = "heat";
    private static final String TAG_MAX_BURN = "maxBurn";
    private static final String TAG_OUT_TYPE = "outType";
    private static final String TAG_OUT_AMOUNT = "outAmount";
    private static final String TAG_STEAM_USED = "steamUsed";
    private static final String TAG_VENTING = "isVenting";
    private static final String TAG_ANIM = "anim";
    private static final String TAG_LAST_ANIM = "lastAnim";
    private static final LegacyBurnTimeModule BURN_MODULE = new LegacyBurnTimeModule()
            .setCokeTimeMod(1.25D)
            .setRocketTimeMod(1.5D)
            .setSolidTimeMod(1.5D)
            .setBalefireTimeMod(1.5D)
            .setSolidHeatMod(1.5D)
            .setRocketHeatMod(3.0D)
            .setBalefireHeatMod(10.0D);

    private final HbmFluidTank inputTank;
    private final HbmFluidTank steamTank;
    private final HbmFluidTank spentSteamTank;
    private final HbmFluidTank smokeTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT_0, SLOT_INPUT_1, SLOT_INPUT_2 -> true;
                case SLOT_FLUID_ID -> true;
                case SLOT_FUEL -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };
    private final LazyOptional<IItemHandler> coreItemHandler = LazyOptional.empty();
    private final IItemHandler input0Handler = new InsertOnlySlotHandler(SLOT_INPUT_0);
    private final IItemHandler input1Handler = new InsertOnlySlotHandler(SLOT_INPUT_1);
    private final IItemHandler input2Handler = new InsertOnlySlotHandler(SLOT_INPUT_2);
    private final IItemHandler fuelHandler = new InsertOnlySlotHandler(SLOT_FUEL);
    private final IFluidHandler steamFluidHandler;
    private final IFluidHandler inputFluidHandler;
    private final IFluidHandler smokeFluidHandler;
    private final ICapabilityProvider steamDelegate;
    private final ICapabilityProvider inputFluidDelegate;
    private final ICapabilityProvider smokeDelegate;
    private final ICapabilityProvider input0Delegate = new ItemDelegate(() -> input0Handler);
    private final ICapabilityProvider input1Delegate = new ItemDelegate(() -> input1Handler);
    private final ICapabilityProvider input2Delegate = new ItemDelegate(() -> input2Handler);
    private final ICapabilityProvider fuelDelegate = new ItemDelegate(() -> fuelHandler);

    private boolean isProgressing;
    private float progress;
    private int burnTime;
    private double burnHeat = 1.0D;
    private int maxBurnTime;
    private int steamUsed;
    private boolean isVenting;
    private MaterialStack output;
    private int anim;
    private int lastAnim;

    public RotaryFurnaceBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.NONE, INPUT_CAPACITY),
                new HbmFluidTank(HbmFluids.STEAM, STEAM_CAPACITY),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, SPENT_STEAM_CAPACITY),
                new HbmFluidTank(HbmFluids.SMOKE, SMOKE_CAPACITY));
    }

    private RotaryFurnaceBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank steamTank, HbmFluidTank spentSteamTank, HbmFluidTank smokeTank) {
        super(ModBlockEntities.ROTARY_FURNACE.get(), pos, state,
                List.of(inputTank, steamTank, spentSteamTank, smokeTank));
        this.inputTank = inputTank;
        this.steamTank = steamTank;
        this.spentSteamTank = spentSteamTank;
        this.smokeTank = smokeTank;
        this.steamTank.setTankType(HbmFluids.STEAM);
        this.spentSteamTank.setTankType(HbmFluids.SPENTSTEAM);
        this.smokeTank.setTankType(HbmFluids.SMOKE);
        steamFluidHandler = new ForgeFluidHandlerAdapter(List.of(steamTank), List.of(spentSteamTank),
                0, true, true, this::onFluidContentsChanged);
        inputFluidHandler = new ForgeFluidHandlerAdapter(List.of(inputTank), List.of(), 0, true, false,
                this::onFluidContentsChanged);
        smokeFluidHandler = new ForgeFluidHandlerAdapter(List.of(), List.of(smokeTank), 0, false, true,
                this::onFluidContentsChanged);
        steamDelegate = new FluidDelegate(() -> steamFluidHandler);
        inputFluidDelegate = new FluidDelegate(() -> inputFluidHandler);
        smokeDelegate = new FluidDelegate(() -> smokeFluidHandler);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RotaryFurnaceBlockEntity furnace) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = furnace.tickServer(level, pos, state);
        furnace.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RotaryFurnaceBlockEntity furnace) {
        if (!level.isClientSide) {
            return;
        }
        furnace.lastAnim = furnace.anim;
        if (furnace.isProgressing) {
            furnace.anim += Math.max((int) furnace.burnHeat, 1);
        }
        Direction facing = facing(state);
        Direction rot = legacyDownSide(facing);
        if (furnace.burnTime > 0) {
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.5D + facing.getStepX() * 0.5D + rot.getStepX()
                            + level.random.nextGaussian() * 0.25D,
                    pos.getY() + 0.375D,
                    pos.getZ() + 0.5D + facing.getStepZ() * 0.5D + rot.getStepZ()
                            + level.random.nextGaussian() * 0.25D,
                    0.0D, 0.0D, 0.0D);
        }
        if (furnace.isVenting && level.getGameTime() % 2L == 0L) {
            ParticleUtil.spawnRotaryFurnaceVentSmoke(level, pos, rot);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public HbmFluidTank getSpentSteamTank() {
        return spentSteamTank;
    }

    public HbmFluidTank getSmokeTank() {
        return smokeTank;
    }

    public int getProgressScaled() {
        return Math.round(Mth.clamp(progress, 0.0F, 1.0F) * 10_000.0F);
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getOutputAmount() {
        return output == null || output.isEmpty() ? 0 : output.amount;
    }

    public int getOutputColor() {
        return output == null || output.material == null ? 0xFFFFFF : output.material.moltenColor;
    }

    public int getOutputMaterialId() {
        return output == null || output.material == null ? -1 : output.material.id;
    }

    public String getOutputMaterialName() {
        return output == null || output.material == null ? "" : output.material.names[0];
    }

    public boolean isProgressing() {
        return isProgressing;
    }

    public boolean isVenting() {
        return isVenting;
    }

    public float getPistonOffset(float partialTick) {
        float lerped = Mth.lerp(partialTick, lastAnim, anim);
        return (float) Math.sin(lerped / 10.0D) * 0.375F;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineRotaryFurnace", "Rotary Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RotaryFurnaceMenu(containerId, inventory, this);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlayPorts.rotaryFurnacePort(this, viewedPos);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank, steamTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(spentSteamTank, smokeTank);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    public long getProviderSpeed(FluidType type, int pressure) {
        return Math.max(1L, type == HbmFluids.SPENTSTEAM ? spentSteamTank.getFill() : smokeTank.getFill());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return coreItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    @Override
    public ICapabilityProvider getLegacyProxyDelegate(BlockPos proxyPos) {
        Direction facing = facing(getBlockState());
        Direction rot = legacyUpSide(facing);
        BlockPos rel = proxyPos.subtract(worldPosition);
        if (rel.equals(relative(facing, rot, -1, -2, 0))) {
            return input0Delegate;
        }
        if (rel.equals(relative(facing, rot, -1, -1, 0))) {
            return input1Delegate;
        }
        if (rel.equals(relative(facing, rot, -1, 0, 0))) {
            return input2Delegate;
        }
        if (rel.equals(relative(facing, rot, 1, 1, 0))) {
            return fuelDelegate;
        }
        Direction downRot = legacyDownSide(facing);
        if (rel.equals(relative(facing, downRot, -1, -1, 0))
                || rel.equals(relative(facing, downRot, -1, -2, 0))) {
            return steamDelegate;
        }
        if (rel.equals(relative(facing, downRot, 1, 2, 0))
                || rel.equals(relative(facing, downRot, -1, 2, 0))) {
            return inputFluidDelegate;
        }
        if (rel.equals(relative(facing, downRot, 0, 1, 4))) {
            return smokeDelegate;
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        inputTank.writeToNbt(tag, "t0");
        steamTank.writeToNbt(tag, "t1");
        spentSteamTank.writeToNbt(tag, "t2");
        smokeTank.writeToNbt(tag, "smoke0");
        tag.putFloat(TAG_PROGRESS, progress);
        tag.putInt(TAG_BURN, burnTime);
        tag.putDouble(TAG_HEAT, burnHeat);
        tag.putInt(TAG_MAX_BURN, maxBurnTime);
        tag.putInt(TAG_STEAM_USED, steamUsed);
        tag.putBoolean(TAG_VENTING, isVenting);
        tag.putInt(TAG_ANIM, anim);
        tag.putInt(TAG_LAST_ANIM, lastAnim);
        if (output != null && !output.isEmpty()) {
            tag.putInt(TAG_OUT_TYPE, output.material.id);
            tag.putInt(TAG_OUT_AMOUNT, output.amount);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST) || tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_ITEMS_FALLBACK, items);
        }
        if (tag.contains("t0") || tag.contains("t0_type")) {
            inputTank.readFromNbt(tag, "t0");
        }
        if (tag.contains("t1") || tag.contains("t1_type")) {
            steamTank.readFromNbt(tag, "t1");
        }
        if (tag.contains("t2") || tag.contains("t2_type")) {
            spentSteamTank.readFromNbt(tag, "t2");
        }
        if (tag.contains("smoke0") || tag.contains("smoke0_type")) {
            smokeTank.readFromNbt(tag, "smoke0");
        }
        progress = tag.getFloat(TAG_PROGRESS);
        burnTime = tag.getInt(TAG_BURN);
        burnHeat = tag.contains(TAG_HEAT) ? tag.getDouble(TAG_HEAT) : 1.0D;
        maxBurnTime = tag.getInt(TAG_MAX_BURN);
        steamUsed = tag.getInt(TAG_STEAM_USED);
        isVenting = tag.getBoolean(TAG_VENTING);
        anim = tag.getInt(TAG_ANIM);
        lastAnim = tag.getInt(TAG_LAST_ANIM);
        if (tag.contains(TAG_OUT_TYPE) && tag.contains(TAG_OUT_AMOUNT)) {
            output = new MaterialStack(Mats.matById.get(tag.getInt(TAG_OUT_TYPE)), tag.getInt(TAG_OUT_AMOUNT));
            if (output.isEmpty()) {
                output = null;
            }
        } else {
            output = null;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(4, 6, 4));
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        int oldInput = inputTank.getFill();
        int oldSteam = steamTank.getFill();
        int oldSpent = spentSteamTank.getFill();
        int oldSmoke = smokeTank.getFill();
        int oldBurn = burnTime;
        int oldMaxBurn = maxBurnTime;
        int oldOutput = getOutputAmount();
        float oldProgress = progress;
        boolean oldProgressing = isProgressing;
        boolean oldVenting = isVenting;

        setFluidTankTypeFromIdentifierSlot(items, SLOT_FLUID_ID, inputTank);
        refreshTrackedTransceiverFluidPortsReport(getReceivingTanks(), getSendingTanks(), this);
        if (spentSteamTank.getFill() > 0) {
            tryProvideFluidToPorts(spentSteamTank.getTankType(), spentSteamTank.getPressure(), this);
        }
        if (smokeTank.getFill() > 0) {
            tryProvideFluidToPorts(smokeTank.getTankType(), smokeTank.getPressure(), this);
        }

        pourOutput(level, pos, state);

        RotaryFurnaceRecipeRuntime.Recipe recipe = RotaryFurnaceRecipeRuntime.find(
                items.getStackInSlot(SLOT_INPUT_0),
                items.getStackInSlot(SLOT_INPUT_1),
                items.getStackInSlot(SLOT_INPUT_2));
        isProgressing = false;
        isVenting = false;
        if (recipe != null) {
            loadFuel();
            float processSpeed = Math.max((float) burnHeat, 1.0F);
            float steamUseMult = (float) (10.0D * Math.log10(processSpeed) + 1.0D);
            if (canProcess(recipe, steamUseMult)) {
                progress += processSpeed / recipe.duration();
                int steamUse = Math.max(0, (int) (recipe.steam() * steamUseMult));
                steamTank.setFill(steamTank.getFill() - steamUse);
                steamUsed += steamUse;
                isProgressing = true;
                while (steamUsed >= 100 && spentSteamTank.getSpace() > 0) {
                    spentSteamTank.fill(HbmFluids.SPENTSTEAM, 1, spentSteamTank.getPressure(), false);
                    steamUsed -= 100;
                }
                if (progress >= 1.0F) {
                    progress = 0.0F;
                    process(recipe);
                }
                pollute(PollutionType.SOOT, PollutionManager.SOOT_PER_SECOND / 10.0F);
                if (burnTime > 0) {
                    burnTime--;
                }
            }
        } else {
            progress = 0.0F;
        }

        return oldInput != inputTank.getFill()
                || oldSteam != steamTank.getFill()
                || oldSpent != spentSteamTank.getFill()
                || oldSmoke != smokeTank.getFill()
                || oldBurn != burnTime
                || oldMaxBurn != maxBurnTime
                || oldOutput != getOutputAmount()
                || oldProgress != progress
                || oldProgressing != isProgressing
                || oldVenting != isVenting;
    }

    private void loadFuel() {
        if (burnTime > 0) {
            return;
        }
        ItemStack stack = items.getStackInSlot(SLOT_FUEL);
        int burn = BURN_MODULE.getBurnTime(stack, 0.0D);
        if (stack.isEmpty() || burn <= 0 || ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) <= 0) {
            return;
        }
        burnHeat = BURN_MODULE.getBurnHeat(1000, stack) / 1000.0D;
        maxBurnTime = burnTime = Math.max(1, burn / 2);
        ItemStack remainder = stack.getCraftingRemainingItem();
        items.extractItem(SLOT_FUEL, 1, false);
        if (items.getStackInSlot(SLOT_FUEL).isEmpty() && !remainder.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, remainder.copy());
        }
    }

    private boolean canProcess(RotaryFurnaceRecipeRuntime.Recipe recipe, float steamUseMult) {
        if (burnTime <= 0) {
            return false;
        }
        if (recipe.fluid() != null) {
            if (inputTank.getTankType() != recipe.fluid().type()) {
                return false;
            }
            if (inputTank.getFill() < recipe.fluid().amount()) {
                return false;
            }
        }
        int steamUse = Math.max(0, (int) (recipe.steam() * steamUseMult));
        if (steamTank.getFill() < steamUse) {
            return false;
        }
        if (spentSteamTank.getSpace() < steamUse / 100) {
            return false;
        }
        if (steamUsed > 100) {
            return false;
        }
        if (output != null && !output.isEmpty()) {
            if (output.material != recipe.output().material) {
                return false;
            }
            return output.amount + recipe.output().amount <= MAX_OUTPUT;
        }
        return recipe.output().amount <= MAX_OUTPUT;
    }

    private void process(RotaryFurnaceRecipeRuntime.Recipe recipe) {
        for (RotaryFurnaceRecipeRuntime.IngredientSpec ingredient : recipe.ingredients()) {
            consumeIngredient(ingredient);
        }
        if (recipe.fluid() != null) {
            inputTank.setFill(inputTank.getFill() - recipe.fluid().amount());
        }
        if (output == null || output.isEmpty()) {
            output = recipe.output().copy();
        } else {
            output.amount = Math.min(MAX_OUTPUT, output.amount + recipe.output().amount);
        }
    }

    private void consumeIngredient(RotaryFurnaceRecipeRuntime.IngredientSpec ingredient) {
        for (int slot = SLOT_INPUT_0; slot <= SLOT_INPUT_2; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (ingredient.matches(stack)) {
                items.extractItem(slot, ingredient.count(), false);
                return;
            }
        }
    }

    private void pourOutput(Level level, BlockPos pos, BlockState state) {
        if (output == null || output.isEmpty()) {
            output = null;
            return;
        }
        Direction rot = legacyDownSide(facing(state));
        int previous = output.amount;
        CrucibleUtil.ImpactHolder impact = new CrucibleUtil.ImpactHolder();
        MaterialStack leftover = CrucibleUtil.pourSingleStack(level,
                pos.getX() + 0.5D + rot.getStepX() * 2.875D,
                pos.getY() + 1.25D,
                pos.getZ() + 0.5D + rot.getStepZ() * 2.875D,
                6.0D, true, output, MaterialShapes.INGOT.q(1), impact);
        output = leftover == null || leftover.isEmpty() ? null : leftover;
        if (output != null && previous != output.amount && impact.value() != null) {
            ParticleUtil.spawnFoundryPour(level, impact.value(), output.material.moltenColor, rot,
                    (float) impact.value().distanceTo(new Vec3(
                            pos.getX() + 0.5D + rot.getStepX() * 2.875D,
                            pos.getY() + 1.25D,
                            pos.getZ() + 0.5D + rot.getStepZ() * 2.875D)));
        }
    }

    private void pollute(PollutionType type, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        int fluidAmount = (int) Math.ceil(amount * 100.0F);
        smokeTank.setFill(smokeTank.getFill() + fluidAmount);
        if (smokeTank.getFill() > smokeTank.getMaxFill() && level != null) {
            int overflow = smokeTank.getFill() - smokeTank.getMaxFill();
            smokeTank.setFill(smokeTank.getMaxFill());
            PollutionManager.incrementPollution(level, worldPosition, type, overflow / 100.0F);
            isVenting = true;
        }
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    private static Direction legacyDownSide(Direction facing) {
        return facing.getCounterClockWise();
    }

    private static Direction legacyUpSide(Direction facing) {
        return facing.getClockWise();
    }

    private static BlockPos relative(Direction facing, Direction rot, int forward, int side, int y) {
        return new BlockPos(
                facing.getStepX() * forward + rot.getStepX() * side,
                y,
                facing.getStepZ() * forward + rot.getStepZ() * side);
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        Direction facing = facing(state);
        Direction rot = legacyDownSide(facing);
        List<FluidPort> ports = new ArrayList<>();
        ports.add(port(relative(facing, rot, -1, -1, 0), facing.getOpposite()));
        ports.add(port(relative(facing, rot, -1, -2, 0), facing.getOpposite()));
        ports.add(port(relative(facing, rot, 1, 2, 0), rot));
        ports.add(port(relative(facing, rot, -1, 2, 0), rot));
        ports.add(port(relative(facing, rot, 0, 1, 4), Direction.UP));
        return ports;
    }

    private static FluidPort port(BlockPos offset, Direction direction) {
        return FluidPort.of(offset.getX(), offset.getY(), offset.getZ(), direction);
    }

    private final class InsertOnlySlotHandler implements IItemHandler {
        private final int slot;

        private InsertOnlySlotHandler(int slot) {
            this.slot = slot;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(this.slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 ? items.insertItem(this.slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(this.slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && items.isItemValid(this.slot, stack);
        }
    }

    private record ItemDelegate(java.util.function.Supplier<IItemHandler> handler) implements ICapabilityProvider {
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
            return capability == ForgeCapabilities.ITEM_HANDLER
                    ? LazyOptional.of(handler::get).cast()
                    : LazyOptional.empty();
        }
    }

    private record FluidDelegate(java.util.function.Supplier<IFluidHandler> handler) implements ICapabilityProvider {
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
            return capability == ForgeCapabilities.FLUID_HANDLER
                    ? LazyOptional.of(handler::get).cast()
                    : LazyOptional.empty();
        }
    }
}
