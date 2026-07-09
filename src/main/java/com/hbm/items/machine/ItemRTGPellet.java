package com.hbm.items.machine;

import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.ntm.config.RtgConfig;
import com.hbm.ntm.item.RtgPelletItem;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy 1.7.10 package bridge for RTG pellets.
 */
@Deprecated(forRemoval = false)
public class ItemRTGPellet extends RtgPelletItem {
    public static final List<ItemRTGPellet> pelletList = new ArrayList<>();

    private final short fallbackHeat;
    private boolean fallbackDoesDecay;
    @Nullable
    private ItemStack fallbackDecayItem;
    private long fallbackLifespan;

    public ItemRTGPellet(Item.Properties properties) {
        this(properties, 0);
    }

    private ItemRTGPellet(Item.Properties properties, int heat) {
        super(properties);
        this.fallbackHeat = (short) heat;
        pelletList.add(this);
    }

    public ItemRTGPellet(int heat) {
        this(new Item.Properties(), heat);
    }

    public ItemRTGPellet setDecays(DepletedRTGMaterial material, long lifespan) {
        this.fallbackDoesDecay = true;
        this.fallbackDecayItem = ItemRTGPelletDepleted.stack(material, 1);
        this.fallbackLifespan = lifespan;
        return this;
    }

    public long getMaxLifespan() {
        long runtimeLifespan = RtgPelletRuntime.maxLifespan(this);
        return runtimeLifespan > 0L ? runtimeLifespan : fallbackLifespan;
    }

    public short getHeat() {
        if (RtgPelletRuntime.isPellet(this)) {
            return (short) RtgPelletRuntime.baseHeat(this);
        }
        return fallbackHeat;
    }

    @Override
    public short getHeat(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getHeat(stack);
        }
        return getHeat();
    }

    @Nullable
    public ItemStack getDecayItem() {
        ItemStack runtimeDecayItem = RtgPelletRuntime.decayItem(this);
        if (!runtimeDecayItem.isEmpty()) {
            return runtimeDecayItem.copy();
        }
        return fallbackDecayItem == null ? null : fallbackDecayItem.copy();
    }

    public boolean getDoesDecay() {
        return RtgPelletRuntime.doesDecay(this) || fallbackDoesDecay;
    }

    @Override
    public boolean getDoesDecay(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getDoesDecay(stack);
        }
        return getDoesDecay();
    }

    public static ItemStack handleDecay(ItemStack stack, ItemRTGPellet instance) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return RtgPelletItem.handleDecay(stack, instance);
        }
        if (instance != null && instance.getDoesDecay() && RtgConfig.doRtgsDecay()) {
            if (instance.getLifespan(stack) <= 0L) {
                ItemStack decayItem = instance.getDecayItem();
                return decayItem == null ? ItemStack.EMPTY : decayItem;
            }
            instance.decay(stack);
        }
        return stack;
    }

    @Override
    public void decay(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            RtgPelletRuntime.decay(stack);
            return;
        }
        if (stack.isEmpty() || stack.getItem() != this || !fallbackDoesDecay) {
            return;
        }
        if (stack.hasTag()) {
            stack.getOrCreateTag().putLong(RtgPelletRuntime.TAG_PELLET_DEPLETION, getLifespan(stack) - 1L);
        } else {
            stack.getOrCreateTag().putLong(RtgPelletRuntime.TAG_PELLET_DEPLETION, getMaxLifespan());
        }
    }

    @Override
    public long getLifespan(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return RtgPelletRuntime.lifespan(stack);
        }
        if (stack.isEmpty() || stack.getItem() != this) {
            return 0L;
        }
        if (!stack.hasTag()) {
            stack.getOrCreateTag().putLong(RtgPelletRuntime.TAG_PELLET_DEPLETION, getMaxLifespan());
            return getMaxLifespan();
        }
        return stack.getOrCreateTag().getLong(RtgPelletRuntime.TAG_PELLET_DEPLETION);
    }

    @Override
    public long getMaxLifespan(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getMaxLifespan(stack);
        }
        return getMaxLifespan();
    }

    @Override
    public ItemStack getDecayItem(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getDecayItem(stack);
        }
        ItemStack decayItem = getDecayItem();
        return decayItem == null ? ItemStack.EMPTY : decayItem;
    }

    public static short getScaledPower(ItemRTGPellet fuel, ItemStack stack) {
        if (fuel == null) {
            return 0;
        }
        return (short) Math.ceil(fuel.getHeat() * ((double) fuel.getLifespan(stack) / (double) fuel.getMaxLifespan()));
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getDurabilityForDisplay(stack);
        }
        long maxLifespan = getMaxLifespan();
        if (maxLifespan <= 0L) {
            return 0.0D;
        }
        return 1.0D - (double) getLifespan(stack) / (double) maxLifespan;
    }

    public String getData() {
        return String.format(Locale.US, "%s (%s HE/t) %s",
                getDescriptionId(), getHeat(), getDoesDecay() ? " (decays)" : "");
    }

    @Override
    public String getData(ItemStack stack) {
        if (RtgPelletRuntime.isPellet(stack)) {
            return super.getData(stack);
        }
        return getData();
    }

    public static HashMap<ItemStack, ItemStack> getRecipeMap() {
        HashMap<ItemStack, ItemStack> map = new HashMap<>();
        for (ItemRTGPellet pellet : pelletList) {
            ItemStack decayItem = pellet.getDecayItem();
            if (decayItem != null && !decayItem.isEmpty()) {
                map.put(new ItemStack(pellet), decayItem.copy());
            }
        }
        return map;
    }
}
