package com.hbm.ntm.compat.jei;

import com.hbm.ntm.item.FluidIconItem;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

/**
 * JEI's default transfer implementation, with HBM's display-only fluid icons removed first.
 *
 * <p>HBM recipe pages intentionally render fluids as {@link FluidIconItem} stacks so that old NEI
 * layouts remain readable. Those stacks are not inventory ingredients, and passing them through to
 * JEI's normal transfer algorithm would try to insert a fake icon into a machine input slot.</p>
 */
final class HbmItemRecipeTransferHandler<C extends AbstractContainerMenu, R>
        implements IRecipeTransferHandler<C, R> {
    private final IRecipeTransferHandler<C, R> delegate;
    private final IRecipeTransferHandlerHelper helper;

    private HbmItemRecipeTransferHandler(IRecipeTransferRegistration registration,
            IRecipeTransferInfo<C, R> transferInfo) {
        this.helper = registration.getTransferHelper();
        this.delegate = helper.createUnregisteredRecipeTransferHandler(transferInfo);
    }

    static <C extends AbstractContainerMenu, R> void register(IRecipeTransferRegistration registration,
            Class<C> menuClass, RecipeType<R> recipeType, int[] recipeSlotIndexes,
            int inventorySlotStart, int inventorySlotCount) {
        register(registration, menuClass, recipeType, recipeSlotIndexes, inventorySlotStart,
                inventorySlotCount, menu -> true);
    }

    static <C extends AbstractContainerMenu, R> void register(IRecipeTransferRegistration registration,
            Class<C> menuClass, RecipeType<R> recipeType, int[] recipeSlotIndexes,
            int inventorySlotStart, int inventorySlotCount, Predicate<C> canHandle) {
        SlotMapping<C, R> mapping = new SlotMapping<>(menuClass, recipeType, recipeSlotIndexes,
                inventorySlotStart, inventorySlotCount, canHandle);
        registration.addRecipeTransferHandler(new HbmItemRecipeTransferHandler<>(registration, mapping), recipeType);
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return delegate.getContainerClass();
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return delegate.getMenuType();
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return delegate.getRecipeType();
    }

    @Override
    public IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots,
            Player player, boolean maxTransfer, boolean doTransfer) {
        List<IRecipeSlotView> transferableSlots = recipeSlots.getSlotViews().stream()
                .filter(HbmItemRecipeTransferHandler::isTransferableSlot)
                .toList();
        boolean hasItemInput = transferableSlots.stream()
                .anyMatch(slot -> slot.getRole() == RecipeIngredientRole.INPUT);
        if (!hasItemInput) {
            return helper.createUserErrorWithTooltip(Component.translatableWithFallback(
                    "hbm_ntm_rebirth.jei.transfer.no_item_input",
                    "This recipe has no item input to transfer."));
        }
        return delegate.transferRecipe(container, recipe,
                helper.createRecipeSlotsView(transferableSlots), player, maxTransfer, doTransfer);
    }

    private static boolean isTransferableSlot(IRecipeSlotView slot) {
        if (slot.getRole() != RecipeIngredientRole.INPUT) {
            return true;
        }
        return slot.getItemStacks().anyMatch(stack -> !stack.isEmpty()
                && !(stack.getItem() instanceof FluidIconItem));
    }

    private record SlotMapping<C extends AbstractContainerMenu, R>(Class<C> menuClass,
            RecipeType<R> recipeType, int[] recipeSlotIndexes, int inventorySlotStart,
            int inventorySlotCount, Predicate<C> canHandle) implements IRecipeTransferInfo<C, R> {
        @Override
        public Class<? extends C> getContainerClass() {
            return menuClass;
        }

        @Override
        public Optional<MenuType<C>> getMenuType() {
            return Optional.empty();
        }

        @Override
        public RecipeType<R> getRecipeType() {
            return recipeType;
        }

        @Override
        public boolean canHandle(C container, R recipe) {
            return canHandle.test(container)
                    && recipeSlotIndexes.length > 0
                    && inventorySlotStart >= 0
                    && inventorySlotCount > 0
                    && Arrays.stream(recipeSlotIndexes).allMatch(index -> index >= 0 && index < container.slots.size())
                    && inventorySlotStart + inventorySlotCount <= container.slots.size();
        }

        @Override
        public List<Slot> getRecipeSlots(C container, R recipe) {
            return Arrays.stream(recipeSlotIndexes).mapToObj(container::getSlot).toList();
        }

        @Override
        public List<Slot> getInventorySlots(C container, R recipe) {
            return IntStream.range(inventorySlotStart, inventorySlotStart + inventorySlotCount)
                    .mapToObj(container::getSlot).toList();
        }
    }
}
