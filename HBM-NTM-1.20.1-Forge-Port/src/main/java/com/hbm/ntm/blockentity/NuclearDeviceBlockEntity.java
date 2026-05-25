package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.ntm.menu.NuclearDeviceMenu;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NuclearDeviceBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_INVENTORY = "Inventory";

    private final NuclearDeviceBlock.Kind kind;
    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemHandler;

    public NuclearDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUCLEAR_DEVICE.get(), pos, state);
        this.kind = state.getBlock() instanceof NuclearDeviceBlock device ? device.kind() : NuclearDeviceBlock.Kind.GADGET;
        this.items = new ItemStackHandler(kind.slots()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isValidComponent(slot, stack);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
        this.itemHandler = LazyOptional.of(() -> items);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public NuclearDeviceBlock.Kind kind() {
        return kind;
    }

    public ItemStack[] getDrops() {
        ItemStack[] drops = new ItemStack[items.getSlots()];
        for (int i = 0; i < items.getSlots(); i++) {
            drops[i] = items.getStackInSlot(i).copy();
        }
        return drops;
    }

    public void clearSlots() {
        for (int i = 0; i < items.getSlots(); i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public boolean isReady() {
        return switch (kind) {
            case GADGET -> hasAll("early_explosive_lenses", 1, 2, 3, 4)
                    && has(0, "gadget_wireing")
                    && has(5, "gadget_core");
            case BOY -> has(0, "boy_shielding")
                    && has(1, "boy_target")
                    && has(2, "boy_bullet")
                    && has(3, "boy_propellant")
                    && has(4, "boy_igniter");
            case MAN -> hasAll("early_explosive_lenses", 1, 2, 3, 4)
                    && has(0, "man_igniter")
                    && has(5, "man_core");
            case TSAR, MIKE -> hasManLensesAndCore();
            case PROTOTYPE -> hasPrototypeLoad();
            case FLEIJA -> hasAll("fleija_igniter", 0, 1)
                    && hasAll("fleija_propellant", 2, 3, 4)
                    && hasAll("fleija_core", 5, 6, 7, 8, 9, 10);
            case SOLINIUM -> hasAll("solinium_igniter", 0, 3, 5, 8)
                    && hasAll("solinium_propellant", 1, 2, 6, 7)
                    && has(4, "solinium_core");
            case N2 -> hasAll("n2_charge", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        };
    }

    public boolean isFilled() {
        return switch (kind) {
            case TSAR -> hasManLensesAndCore() && has(5, "tsar_core");
            case MIKE -> hasManLensesAndCore()
                    && has(5, "mike_core")
                    && has(6, "mike_deut")
                    && has(7, "mike_cooling_unit");
            default -> isReady();
        };
    }

    public NuclearDeviceBlock.Kind detonationKind() {
        return switch (kind) {
            case TSAR -> isFilled() ? NuclearDeviceBlock.Kind.TSAR : NuclearDeviceBlock.Kind.MAN;
            case MIKE -> NuclearDeviceBlock.Kind.MIKE;
            default -> kind;
        };
    }

    private boolean isValidComponent(int slot, ItemStack stack) {
        return switch (kind) {
            case GADGET -> switch (slot) {
                case 0 -> is(stack, "gadget_wireing");
                case 1, 2, 3, 4 -> is(stack, "early_explosive_lenses");
                case 5 -> is(stack, "gadget_core");
                default -> false;
            };
            case BOY -> switch (slot) {
                case 0 -> is(stack, "boy_shielding");
                case 1 -> is(stack, "boy_target");
                case 2 -> is(stack, "boy_bullet");
                case 3 -> is(stack, "boy_propellant");
                case 4 -> is(stack, "boy_igniter");
                default -> false;
            };
            case MAN -> switch (slot) {
                case 0 -> is(stack, "man_igniter");
                case 1, 2, 3, 4 -> is(stack, "early_explosive_lenses");
                case 5 -> is(stack, "man_core");
                default -> false;
            };
            case TSAR -> switch (slot) {
                case 0, 1, 2, 3 -> is(stack, "explosive_lenses");
                case 4 -> is(stack, "man_core");
                case 5 -> is(stack, "tsar_core");
                default -> false;
            };
            case MIKE -> switch (slot) {
                case 0, 1, 2, 3 -> is(stack, "explosive_lenses");
                case 4 -> is(stack, "man_core");
                case 5 -> is(stack, "mike_core");
                case 6 -> is(stack, "mike_deut");
                case 7 -> is(stack, "mike_cooling_unit");
                default -> false;
            };
            case PROTOTYPE -> isPrototypeComponent(slot, stack);
            case FLEIJA -> switch (slot) {
                case 0, 1 -> is(stack, "fleija_igniter");
                case 2, 3, 4 -> is(stack, "fleija_propellant");
                case 5, 6, 7, 8, 9, 10 -> is(stack, "fleija_core");
                default -> false;
            };
            case SOLINIUM -> switch (slot) {
                case 0, 3, 5, 8 -> is(stack, "solinium_igniter");
                case 1, 2, 6, 7 -> is(stack, "solinium_propellant");
                case 4 -> is(stack, "solinium_core");
                default -> false;
            };
            case N2 -> is(stack, "n2_charge");
        };
    }

    private boolean hasManLensesAndCore() {
        return hasAll("explosive_lenses", 0, 1, 2, 3) && has(4, "man_core");
    }

    private boolean hasPrototypeLoad() {
        return hasAll("cell_sas3", 0, 1, 12, 13)
                && hasPrototypeRod("URANIUM", 2, 3, 10, 11)
                && hasPrototypeRod("LEAD", 4, 5, 8, 9)
                && hasPrototypeRod("NP237", 6, 7);
    }

    private boolean isPrototypeComponent(int slot, ItemStack stack) {
        return switch (slot) {
            case 0, 1, 12, 13 -> is(stack, "cell_sas3");
            case 2, 3, 10, 11 -> isRod(stack, "URANIUM");
            case 4, 5, 8, 9 -> isRod(stack, "LEAD");
            case 6, 7 -> isRod(stack, "NP237");
            default -> false;
        };
    }

    private boolean hasPrototypeRod(String rodType, int... slots) {
        return Arrays.stream(slots).allMatch(slot -> isRod(items.getStackInSlot(slot), rodType));
    }

    private boolean isRod(ItemStack stack, String rodType) {
        if (stack.isEmpty() || !is(stack, "rod_quad")) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && rodType.equalsIgnoreCase(tag.getString("BreedingRodType"));
    }

    private boolean hasAll(String name, int... slots) {
        return Arrays.stream(slots).allMatch(slot -> has(slot, name));
    }

    private boolean has(int slot, String name) {
        return slot >= 0 && slot < items.getSlots() && is(items.getStackInSlot(slot), name);
    }

    private static boolean is(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_INVENTORY)) {
            items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        }
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm." + switch (kind) {
            case GADGET -> "nuke_gadget";
            case BOY -> "nuke_boy";
            case MAN -> "nuke_man";
            case TSAR -> "nuke_tsar";
            case MIKE -> "nuke_mike";
            case PROTOTYPE -> "nuke_prototype";
            case FLEIJA -> "nuke_fleija";
            case SOLINIUM -> "nuke_solinium";
            case N2 -> "nuke_n2";
        });
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new NuclearDeviceMenu(containerId, inventory, this);
    }
}
