package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

/** Non-GUI EntitySupplyCrate inventory; content opens only by the source crowbar interaction. */
public class SupplyCrateBlockEntity extends BlockEntity {
    private final List<ItemStack> items = new ArrayList<>();
    public SupplyCrateBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SUPPLY_CRATE.get(), pos, state); }
    public List<ItemStack> items() { return List.copyOf(items); }
    public void addItems(List<ItemStack> stacks) { for (ItemStack stack : stacks) if (!stack.isEmpty()) items.add(stack.copy()); setChanged(); }
    public void clearItems() { items.clear(); setChanged(); }
    public void loadFromPlacedStack(ItemStack stack) {
        items.clear();
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        int amount = tag.getInt("amount");
        for (int i = 0; i < amount; i++) {
            ItemStack item = ItemStack.of(tag.getCompound("slot" + i));
            if (!item.isEmpty()) items.add(item);
        }
        setChanged();
    }
    public ItemStack createDroppedStack() {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("amount", items.size());
        for (int i = 0; i < items.size(); i++) tag.put("slot" + i, items.get(i).save(new CompoundTag()));
        return stack;
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); ListTag list = new ListTag();
        for (ItemStack item : items) list.add(item.save(new CompoundTag())); tag.put("items", list);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); items.clear(); ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) { ItemStack item = ItemStack.of(list.getCompound(i)); if (!item.isEmpty()) items.add(item); }
    }
}
