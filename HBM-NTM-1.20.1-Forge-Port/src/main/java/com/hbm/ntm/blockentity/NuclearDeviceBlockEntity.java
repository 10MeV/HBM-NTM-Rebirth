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
        this.items = createItemHandler(kind, this::setChanged);
        this.itemHandler = LazyOptional.of(() -> items);
    }

    public static ItemStackHandler createItemHandler(NuclearDeviceBlock.Kind kind) {
        return createItemHandler(kind, null);
    }

    private static ItemStackHandler createItemHandler(NuclearDeviceBlock.Kind kind, @Nullable Runnable changeListener) {
        return new ItemStackHandler(kind.slots()) {
            @Override
            protected void onContentsChanged(int slot) {
                if (changeListener != null) {
                    changeListener.run();
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotLimit(kind);
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                super.setStackInSlot(slot, clampStack(slot, stack));
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isValidComponent(kind, slot, stack);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }

            @Override
            public void deserializeNBT(CompoundTag nbt) {
                super.deserializeNBT(nbt);
                for (int slot = 0; slot < getSlots(); slot++) {
                    ItemStack stack = getStackInSlot(slot);
                    ItemStack clamped = clampStack(slot, stack);
                    if (clamped != stack) {
                        setStackInSlot(slot, clamped);
                    }
                }
            }

            private ItemStack clampStack(int slot, ItemStack stack) {
                if (stack.isEmpty()) {
                    return stack;
                }
                int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
                if (stack.getCount() <= limit) {
                    return stack;
                }
                ItemStack copy = stack.copy();
                copy.setCount(limit);
                return copy;
            }
        };
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
        return isReady(kind, items);
    }

    public static boolean isReady(NuclearDeviceBlock.Kind kind, ItemStackHandler items) {
        return switch (kind) {
            case GADGET -> hasAll(items, "early_explosive_lenses", 1, 2, 3, 4)
                    && has(items, 0, "gadget_wireing")
                    && has(items, 5, "gadget_core");
            case BOY -> has(items, 0, "boy_shielding")
                    && has(items, 1, "boy_target")
                    && has(items, 2, "boy_bullet")
                    && has(items, 3, "boy_propellant")
                    && has(items, 4, "boy_igniter");
            case MAN -> hasAll(items, "early_explosive_lenses", 1, 2, 3, 4)
                    && has(items, 0, "man_igniter")
                    && has(items, 5, "man_core");
            case TSAR, MIKE -> hasManLensesAndCore(items);
            case PROTOTYPE -> hasPrototypeLoad(items);
            case FLEIJA -> hasAll(items, "fleija_igniter", 0, 1)
                    && hasAll(items, "fleija_propellant", 2, 3, 4)
                    && hasAll(items, "fleija_core", 5, 6, 7, 8, 9, 10);
            case SOLINIUM -> hasAll(items, "solinium_igniter", 0, 3, 5, 8)
                    && hasAll(items, "solinium_propellant", 1, 2, 6, 7)
                    && has(items, 4, "solinium_core");
            case N2 -> hasAll(items, "n2_charge", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        };
    }

    public boolean isFilled() {
        return isFilled(kind, items);
    }

    public static boolean isFilled(NuclearDeviceBlock.Kind kind, ItemStackHandler items) {
        return switch (kind) {
            case TSAR -> hasManLensesAndCore(items) && has(items, 5, "tsar_core");
            case MIKE -> hasManLensesAndCore(items)
                    && has(items, 5, "mike_core")
                    && has(items, 6, "mike_deut")
                    && has(items, 7, "mike_cooling_unit");
            default -> isReady(kind, items);
        };
    }

    public NuclearDeviceBlock.Kind detonationKind() {
        return switch (kind) {
            case TSAR -> isFilled() ? NuclearDeviceBlock.Kind.TSAR : NuclearDeviceBlock.Kind.MAN;
            case MIKE -> NuclearDeviceBlock.Kind.MIKE;
            default -> kind;
        };
    }

    public static int slotLimit(NuclearDeviceBlock.Kind kind) {
        return 1;
    }

    private static boolean isValidComponent(NuclearDeviceBlock.Kind kind, int slot, ItemStack stack) {
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

    private static boolean hasManLensesAndCore(ItemStackHandler items) {
        return hasAll(items, "explosive_lenses", 0, 1, 2, 3) && has(items, 4, "man_core");
    }

    private static boolean hasPrototypeLoad(ItemStackHandler items) {
        return hasAll(items, "cell_sas3", 0, 1, 12, 13)
                && hasPrototypeRod(items, "URANIUM", 2, 3, 10, 11)
                && hasPrototypeRod(items, "LEAD", 4, 5, 8, 9)
                && hasPrototypeRod(items, "NP237", 6, 7);
    }

    private static boolean isPrototypeComponent(int slot, ItemStack stack) {
        return switch (slot) {
            case 0, 1, 12, 13 -> is(stack, "cell_sas3");
            case 2, 3, 10, 11 -> isRod(stack, "URANIUM");
            case 4, 5, 8, 9 -> isRod(stack, "LEAD");
            case 6, 7 -> isRod(stack, "NP237");
            default -> false;
        };
    }

    private static boolean hasPrototypeRod(ItemStackHandler items, String rodType, int... slots) {
        return Arrays.stream(slots).allMatch(slot -> isRod(items.getStackInSlot(slot), rodType));
    }

    private static boolean isRod(ItemStack stack, String rodType) {
        if (stack.isEmpty() || !is(stack, "rod_quad")) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && rodType.equalsIgnoreCase(tag.getString("BreedingRodType"));
    }

    private static boolean hasAll(ItemStackHandler items, String name, int... slots) {
        return Arrays.stream(slots).allMatch(slot -> has(items, slot, name));
    }

    private static boolean has(ItemStackHandler items, int slot, String name) {
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
