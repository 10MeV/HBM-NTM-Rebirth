package com.hbm.ntm.blockentity;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ArcFurnaceMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.util.CrucibleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcFurnaceBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider, HbmLegacyButtonReceiver, LegacyLookOverlayProvider {
    public static final int SLOT_ELECTRODE_0 = 0;
    public static final int SLOT_ELECTRODE_1 = 1;
    public static final int SLOT_ELECTRODE_2 = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_UPGRADE = 4;
    public static final int SLOT_GRID_START = 5;
    public static final int SLOT_GRID_END = 24;
    public static final int SLOT_QUEUE_START = 25;
    public static final int SLOT_QUEUE_END = 29;
    public static final int SLOT_COUNT = 30;
    public static final int CONTROL_LIQUID_MODE = 0;

    public static final long MAX_POWER = 2_500_000L;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_LID = "lid";
    private static final String TAG_DELAY = "delay";
    private static final String TAG_LIQUID_MODE = "liquidMode";
    private static final String TAG_IS_PROGRESSING = "isProgressing";
    private static final String TAG_HAS_MATERIAL = "hasMaterial";
    private static final String TAG_LIQUIDS = "liquids";
    private static final int MAX_LIQUID = MaterialShapes.BLOCK.q(128);
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(UpgradeType.SPEED, 3);

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= SLOT_ELECTRODE_0 && slot <= SLOT_ELECTRODE_2) {
                return isFreshElectrode(stack);
            }
            if (slot == SLOT_BATTERY) {
                return HbmInventoryMenuHelper.isBatteryLike(stack);
            }
            if (slot == SLOT_UPGRADE) {
                return stack.getItem() instanceof ItemMachineUpgrade upgrade
                        && upgrade.getUpgradeType() == UpgradeType.SPEED;
            }
            if (slot >= SLOT_GRID_START && slot <= SLOT_QUEUE_END) {
                return level == null || findRecipe(level, stack) != null;
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= SLOT_ELECTRODE_0 && slot <= SLOT_ELECTRODE_2) {
                return 1;
            }
            if (slot >= SLOT_GRID_START && slot <= SLOT_GRID_END) {
                ItemStack existing = getStackInSlot(slot);
                return existing.isEmpty() ? 1 : getMaxInputSize();
            }
            return super.getSlotLimit(slot);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(AccessibleItemHandler::new);

    private int progress;
    private int delay;
    private int upgrade;
    private float lid = 1.0F;
    private float previousLid = 1.0F;
    private boolean progressing;
    private boolean hasMaterial;
    private boolean liquidMode;
    private final List<MaterialStack> liquids = new ArrayList<>();

    public ArcFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARC_FURNACE.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcFurnaceBlockEntity furnace) {
        long oldPower = furnace.energy.getPower();
        int oldProgress = furnace.progress;
        int oldDelay = furnace.delay;
        int oldUpgrade = furnace.upgrade;
        float oldLid = furnace.lid;
        boolean oldProgressing = furnace.progressing;
        boolean oldHasMaterial = furnace.hasMaterial;
        int oldLiquidAmount = furnace.getLiquidAmount();

        HbmEnergyUtil.chargeStorageFromItem(furnace.items.getStackInSlot(SLOT_BATTERY),
                furnace.energy, furnace.energy.getReceiverSpeed());
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, pos, furnace.energyPorts(state), furnace.energy);
        }

        furnace.upgrade = LegacyMachineUpgradeManager.checkSlots(furnace.items, SLOT_UPGRADE, SLOT_UPGRADE,
                VALID_UPGRADES).getLevel(UpgradeType.SPEED);
        furnace.progressing = false;

        if (furnace.lid > 0.0F) {
            furnace.loadIngredients(level);
        }

        boolean ingredients = furnace.hasIngredients(level);
        boolean electrodes = furnace.hasElectrodes();
        int consumption = furnace.consumption();
        if (furnace.energy.getPower() > 0L && ingredients && electrodes && furnace.delay <= 0
                && furnace.liquids.isEmpty()) {
            if (furnace.lid > 0.0F) {
                furnace.lid = Math.max(0.0F, furnace.lid - furnace.lidStep());
                furnace.progress = 0;
            } else if (furnace.energy.getPower() >= consumption) {
                furnace.progress++;
                furnace.progressing = true;
                furnace.energy.setPower(furnace.energy.getPower() - consumption);
                if (furnace.progress >= furnace.processTime()) {
                    furnace.process(level);
                    furnace.progress = 0;
                    furnace.delay = (int) (120.0D / (furnace.upgrade * 0.5D + 1.0D));
                    PollutionManager.incrementPollution(level, pos, PollutionType.SOOT, 10.0F);
                }
            }
        } else {
            if (furnace.delay > 0) {
                furnace.delay--;
            }
            furnace.progress = 0;
            if (furnace.lid < 1.0F) {
                furnace.lid = Math.min(1.0F, furnace.lid + furnace.lidStep());
            }
        }
        furnace.hasMaterial = ingredients || furnace.hasIngredients(level) || !furnace.liquids.isEmpty();
        if (!furnace.liquids.isEmpty() && furnace.lid > 0.0F) {
            furnace.pourLiquids(level, pos, state);
        }
        furnace.cleanupLiquids();

        boolean changed = oldPower != furnace.energy.getPower()
                || oldProgress != furnace.progress
                || oldDelay != furnace.delay
                || oldUpgrade != furnace.upgrade
                || oldLid != furnace.lid
                || oldProgressing != furnace.progressing
                || oldHasMaterial != furnace.hasMaterial
                || oldLiquidAmount != furnace.getLiquidAmount();
        furnace.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ArcFurnaceBlockEntity furnace) {
        float oldLid = furnace.previousLid;
        float currentLid = furnace.lid;
        if (currentLid != oldLid && level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 4.0D,
                pos.getZ() + 0.5D, 50.0D, false) != null) {
            if (currentLid > oldLid && !(oldLid == 0.0F && currentLid == 1.0F)) {
                double x = pos.getX() + 0.5D + level.random.nextGaussian() * 0.5D;
                double z = pos.getZ() + 0.5D + level.random.nextGaussian() * 0.5D;
                int lifetime = 70 + level.random.nextInt(30);
                float alpha = oldLid / Math.max(currentLid, 0.0001F);
                for (int i = 0; i < 3; i++) {
                    ParticleUtil.spawnCoolingTower(level, x, pos.getY() + 4.0D, z,
                            0.01F, 0.5F, 2.0F, lifetime, true, 0.05F, alpha, 0x000000);
                }
            } else if (currentLid < oldLid && currentLid > 0.5F && furnace.hasMaterial
                    && level.random.nextInt(5) == 0) {
                double x = pos.getX() + 0.5D + level.random.nextGaussian() * 0.5D;
                double z = pos.getZ() + 0.5D + level.random.nextGaussian() * 0.5D;
                for (int i = 0; i < 2; i++) {
                    ParticleUtil.spawnRbmkFlame(level, x, pos.getY() + 2.75D, z, 50);
                }
            }
        }
        furnace.previousLid = currentLid;
    }

    private void loadIngredients(Level level) {
        boolean changed = false;
        for (int queue = SLOT_QUEUE_START; queue <= SLOT_QUEUE_END; queue++) {
            ItemStack queued = items.getStackInSlot(queue);
            if (queued.isEmpty()) {
                continue;
            }
            GenericMachineRecipe recipe = findSolidRecipe(level, queued);
            if (recipe == null) {
                continue;
            }
            int max = maxInputSizeFor(recipe, queued);
            for (int grid = SLOT_GRID_START; grid <= SLOT_GRID_END && !queued.isEmpty(); grid++) {
                ItemStack existing = items.getStackInSlot(grid);
                if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, queued)) {
                    continue;
                }
                int toMove = Math.min(Math.min(existing.getMaxStackSize() - existing.getCount(), queued.getCount()),
                        max - existing.getCount());
                if (toMove > 0) {
                    ItemStack moved = queued.split(toMove);
                    existing.grow(moved.getCount());
                    items.setStackInSlot(grid, existing);
                    items.setStackInSlot(queue, queued);
                    changed = true;
                }
            }
            for (int grid = SLOT_GRID_START; grid <= SLOT_GRID_END && !queued.isEmpty(); grid++) {
                if (!items.getStackInSlot(grid).isEmpty()) {
                    continue;
                }
                int toMove = Math.min(max, queued.getCount());
                ItemStack moved = queued.split(toMove);
                items.setStackInSlot(grid, moved);
                items.setStackInSlot(queue, queued);
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private void process(Level level) {
        for (int slot = SLOT_GRID_START; slot <= SLOT_GRID_END; slot++) {
            ItemStack input = items.getStackInSlot(slot);
            if (input.isEmpty()) {
                continue;
            }
            GenericMachineRecipe recipe = findRecipe(level, input);
            if (recipe == null) {
                continue;
            }
            if (liquidMode) {
                MaterialStack liquid = liquidOutput(recipe);
                if (liquid == null) {
                    continue;
                }
                addLiquid(new MaterialStack(liquid.material, liquid.amount * input.getCount()));
                items.setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                if (recipe.getItemOutputs().isEmpty()) {
                    continue;
                }
                ItemStack output = recipe.getItemOutputs().get(0).copy();
                int count = input.getCount() * output.getCount();
                output.setCount(Math.min(count, output.getMaxStackSize()));
                items.setStackInSlot(slot, output);
            }
        }
        for (int slot = SLOT_ELECTRODE_0; slot <= SLOT_ELECTRODE_2; slot++) {
            ItemStack electrode = items.getStackInSlot(slot);
            if (!electrode.isEmpty() && ArcElectrodeItem.damage(electrode)) {
                items.setStackInSlot(slot, burntElectrode(electrode));
            } else {
                items.setStackInSlot(slot, electrode);
            }
        }
    }

    @Nullable
    private GenericMachineRecipe findSolidRecipe(Level level, ItemStack stack) {
        GenericMachineRecipe recipe = findRecipe(level, stack);
        return recipe != null && !recipe.getItemOutputs().isEmpty() ? recipe : null;
    }

    @Nullable
    private GenericMachineRecipe findRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (GenericMachineRecipe recipe : GenericMachineRecipeRuntime.recipes(level,
                GenericMachineRecipe.Machine.ARC_FURNACE)) {
            if (recipe.getItemInputs().size() != 1 || recipe.getItemOutputs().isEmpty()) {
                continue;
            }
            HbmIngredient input = recipe.getItemInputs().get(0);
            if (input.count() <= stack.getCount() && input.test(stack, true)) {
                if (liquidMode) {
                    return liquidOutput(recipe) == null ? null : recipe;
                }
                return !recipe.getItemOutputs().isEmpty() ? recipe : null;
            }
        }
        return null;
    }

    @Nullable
    private static MaterialStack liquidOutput(GenericMachineRecipe recipe) {
        return switch (recipe.getInternalName()) {
            case "arc.sand" -> new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1));
            case "arc.flint" -> new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2));
            case "arc.quartz", "arc.quartz_dust" ->
                    new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3));
            case "arc.quartz_block" -> new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12));
            default -> null;
        };
    }

    private boolean hasIngredients(Level level) {
        for (int slot = SLOT_GRID_START; slot <= SLOT_GRID_END; slot++) {
            if (findRecipe(level, items.getStackInSlot(slot)) != null) {
                return true;
            }
        }
        return false;
    }

    private void addLiquid(MaterialStack incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        for (MaterialStack stack : liquids) {
            if (stack.material == incoming.material) {
                stack.amount = Math.min(MAX_LIQUID, stack.amount + incoming.amount);
                return;
            }
        }
        liquids.add(new MaterialStack(incoming.material, Math.min(MAX_LIQUID, incoming.amount)));
    }

    private void pourLiquids(Level level, BlockPos pos, BlockState state) {
        Direction dir = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        CrucibleUtil.ImpactHolder impact = new CrucibleUtil.ImpactHolder();
        CrucibleUtil.PourResult result = CrucibleUtil.pourFullStackDetailed(level,
                pos.getX() + 0.5D + dir.getStepX() * 2.875D,
                pos.getY() + 1.25D,
                pos.getZ() + 0.5D + dir.getStepZ() * 2.875D,
                6.0D, true, liquids, MaterialShapes.INGOT.q(1), impact);
        if (result.moved() != null && !result.moved().isEmpty()) {
            Vec3 hit = impact.value();
            float length = Math.max(1.0F, pos.getY() + 1.0F - (float) (Math.ceil(hit.y) - 0.875D));
            ParticleUtil.spawnFoundryPour(level,
                    new Vec3(pos.getX() + 0.5D + dir.getStepX() * 2.875D,
                            pos.getY() + 1.0D,
                            pos.getZ() + 0.5D + dir.getStepZ() * 2.875D),
                    result.moved().material.moltenColor, dir, length);
        }
    }

    private void cleanupLiquids() {
        liquids.removeIf(stack -> stack == null || stack.isEmpty());
    }

    private boolean hasElectrodes() {
        for (int slot = SLOT_ELECTRODE_0; slot <= SLOT_ELECTRODE_2; slot++) {
            if (!isFreshElectrode(items.getStackInSlot(slot))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFreshElectrode(ItemStack stack) {
        return stack.getItem() instanceof ArcElectrodeItem electrode && !electrode.burnt();
    }

    private static ItemStack burntElectrode(ItemStack electrode) {
        if (!(electrode.getItem() instanceof ArcElectrodeItem arcElectrode)) {
            return ItemStack.EMPTY;
        }
        Item item = switch (arcElectrode.type()) {
            case GRAPHITE -> ModItems.ARC_ELECTRODE_BURNT_GRAPHITE.get();
            case LANTHANIUM -> ModItems.ARC_ELECTRODE_BURNT_LANTHANIUM.get();
            case DESH -> ModItems.ARC_ELECTRODE_BURNT_DESH.get();
            case SATURNITE -> ModItems.ARC_ELECTRODE_BURNT_SATURNITE.get();
        };
        return new ItemStack(item);
    }

    private int maxInputSizeFor(GenericMachineRecipe recipe, ItemStack input) {
        ItemStack output = recipe.getItemOutputs().isEmpty() ? ItemStack.EMPTY : recipe.getItemOutputs().get(0);
        if (output.isEmpty()) {
            return getMaxInputSize();
        }
        int recipeMax = Math.max(1, output.getMaxStackSize() / Math.max(1, output.getCount()));
        return Math.min(getMaxInputSize(), Math.min(input.getMaxStackSize(), recipeMax));
    }

    private float lidStep() {
        return (float) (1.0D / (60.0D / (upgrade * 0.5D + 1.0D)));
    }

    private List<EnergyPort> energyPorts(BlockState state) {
        Direction dir = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        Direction rot = dir.getClockWise();
        return List.of(
                port(dir, 3, rot, 1, dir),
                port(dir, 3, rot, -1, dir),
                port(rot, 3, dir, 1, rot),
                port(rot, 3, dir, -1, rot),
                port(rot.getOpposite(), 3, dir, 1, rot.getOpposite()),
                port(rot.getOpposite(), 3, dir, -1, rot.getOpposite()));
    }

    private static EnergyPort port(Direction primary, int primaryOffset, Direction secondary, int secondaryOffset,
            Direction face) {
        return EnergyPort.of(primary.getStepX() * primaryOffset + secondary.getStepX() * secondaryOffset,
                0,
                primary.getStepZ() * primaryOffset + secondary.getStepZ() * secondaryOffset,
                face);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime();
    }

    public int getMaxInputSize() {
        return switch (upgrade) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 16;
            default -> 1;
        };
    }

    public int getDelay() {
        return delay;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public int consumption() {
        return (int) (1_000 * Math.pow(5, upgrade));
    }

    public int processTime() {
        return 400 / (upgrade * 2 + 1);
    }

    public float getLid() {
        return lid;
    }

    public float getPreviousLid() {
        return previousLid;
    }

    public boolean isProgressing() {
        return progressing;
    }

    public boolean hasMaterial() {
        return hasMaterial;
    }

    public boolean isLiquidMode() {
        return liquidMode;
    }

    public int getLiquidAmount() {
        int amount = 0;
        for (MaterialStack stack : liquids) {
            if (stack != null && !stack.isEmpty()) {
                amount += stack.amount;
            }
        }
        return amount;
    }

    public int getLiquidColor() {
        for (MaterialStack stack : liquids) {
            if (stack != null && !stack.isEmpty() && stack.material != null) {
                return stack.material.moltenColor;
            }
        }
        return 0xFFFFFF;
    }

    public int getLiquidMaterialId() {
        for (MaterialStack stack : liquids) {
            if (stack != null && !stack.isEmpty() && stack.material != null) {
                return stack.material.id;
            }
        }
        return -1;
    }

    public int getMaxLiquid() {
        return MAX_LIQUID;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public List<ItemStack> drainLiquidsAsScraps() {
        List<ItemStack> scraps = new ArrayList<>();
        for (MaterialStack stack : liquids) {
            ItemStack scrap = FoundryScrapsItem.create(stack == null ? null : stack.copy());
            if (!scrap.isEmpty()) {
                scraps.add(scrap);
            }
        }
        liquids.clear();
        hasMaterial = hasIngredients(level);
        setChanged();
        return scraps;
    }

    public List<Byte> electrodeStates() {
        List<Byte> states = new ArrayList<>(3);
        for (int slot = SLOT_ELECTRODE_0; slot <= SLOT_ELECTRODE_2; slot++) {
            states.add(electrodeState(items.getStackInSlot(slot)));
        }
        return states;
    }

    public byte electrodeState(ItemStack stack) {
        if (!(stack.getItem() instanceof ArcElectrodeItem electrode)) {
            return 0;
        }
        if (electrode.burnt()) {
            return 3;
        }
        return progressing || ArcElectrodeItem.getDurability(stack) > 0 ? (byte) 2 : (byte) 1;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPorts(getBlockState());
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                Component.literal("Consumption: " + consumption() + " HE/t"),
                Component.literal(liquidMode ? "Liquid mode" : "Solid mode")));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineArcFurnaceLarge", "Electric Arc Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcFurnaceMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_LIQUID_MODE
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_LIQUID_MODE) {
            liquidMode = !liquidMode;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_DELAY, delay);
        tag.putFloat(TAG_LID, lid);
        tag.putBoolean(TAG_LIQUID_MODE, liquidMode);
        tag.putBoolean(TAG_IS_PROGRESSING, progressing);
        tag.putBoolean(TAG_HAS_MATERIAL, hasMaterial);
        tag.put(TAG_LIQUIDS, Mats.writeList(liquids));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        progress = tag.getInt(TAG_PROGRESS);
        delay = tag.getInt(TAG_DELAY);
        float oldLid = lid;
        lid = tag.contains(TAG_LID) ? tag.getFloat(TAG_LID) : 1.0F;
        previousLid = level != null && level.isClientSide ? oldLid : lid;
        liquidMode = tag.getBoolean(TAG_LIQUID_MODE);
        progressing = tag.getBoolean(TAG_IS_PROGRESSING);
        hasMaterial = tag.getBoolean(TAG_HAS_MATERIAL);
        liquids.clear();
        liquids.addAll(Mats.readList(tag.getList(TAG_LIQUIDS, net.minecraft.nbt.Tag.TAG_COMPOUND)));
        upgrade = LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE, SLOT_UPGRADE, VALID_UPGRADES)
                .getLevel(UpgradeType.SPEED);
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

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 28;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            if (mapped < SLOT_ELECTRODE_0 || (mapped > SLOT_ELECTRODE_2 && mapped < SLOT_QUEUE_START)) {
                return stack;
            }
            if (mapped >= SLOT_GRID_START && mapped <= SLOT_GRID_END) {
                return stack;
            }
            return items.insertItem(mapped, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            if (mapped < 0) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items.getStackInSlot(mapped);
            if (mapped <= SLOT_ELECTRODE_2) {
                return lid >= 1.0F && !isFreshElectrode(stack) ? items.extractItem(mapped, amount, simulate)
                        : ItemStack.EMPTY;
            }
            if (mapped >= SLOT_GRID_START && mapped <= SLOT_GRID_END) {
                return lid > 0.0F && (level == null || findRecipe(level, stack) == null)
                        ? items.extractItem(mapped, amount, simulate)
                        : ItemStack.EMPTY;
            }
            if (mapped >= SLOT_QUEUE_START) {
                return level == null || findRecipe(level, stack) == null
                        ? items.extractItem(mapped, amount, simulate)
                        : ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int map(int slot) {
            if (slot >= 0 && slot <= 2) {
                return slot;
            }
            if (slot >= 3 && slot <= 22) {
                return SLOT_GRID_START + slot - 3;
            }
            if (slot >= 23 && slot <= 27) {
                return SLOT_QUEUE_START + slot - 23;
            }
            return -1;
        }
    }
}
