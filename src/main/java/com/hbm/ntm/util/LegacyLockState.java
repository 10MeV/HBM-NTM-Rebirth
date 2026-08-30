package com.hbm.ntm.util;

import com.hbm.ntm.item.KeyPinItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Source-backed state and interaction contract of 1.7.10
 * {@code TileEntityLockableBase}.
 *
 * <p>The state deliberately has no BlockEntity or world dependency.  A caller
 * owns persistence/synchronisation and supplies both of those effects through
 * {@link Hooks}; this keeps ordinary containers, multiblock doors and future
 * lockable carriers from silently acquiring a guessed world-update policy.</p>
 *
 * <p>Legacy {@code key_red} was a red-room/dungeon item. It is globally
 * excluded in this port and is intentionally not accepted here.</p>
 */
public final class LegacyLockState {
    public static final String TAG_PINS = "lock";
    public static final String TAG_CHEESABLE = "cheesable";
    public static final String TAG_LOCKED = "isLocked";
    public static final String TAG_LOCK_MOD = "lockMod";

    public static final String SOUND_LOCK_HANG = "hbm:block.lockHang";
    public static final String SOUND_LOCK_OPEN = "hbm:block.lockOpen";
    public static final String SOUND_PIN_UNLOCK = "hbm:item.pinUnlock";
    public static final String SOUND_PIN_BREAK = "hbm:item.pinBreak";

    private int pins;
    private boolean locked;
    private double lockModifier = 0.1D;
    private boolean cheesable = true;

    public boolean isLocked() {
        return locked;
    }

    public int pins() {
        return pins;
    }

    public double lockModifier() {
        return lockModifier;
    }

    public boolean isCheesable() {
        return cheesable;
    }

    public void setPins(int pins) {
        this.pins = pins;
    }

    public void setLockModifier(double lockModifier) {
        this.lockModifier = lockModifier;
    }

    public void setCheesable(boolean cheesable) {
        this.cheesable = cheesable;
    }

    /**
     * Legacy-compatible lock transition. A zero-pin lock is not created: all
     * source-backed padlock callers reject it before calling the base state.
     */
    public boolean lock(Hooks hooks) {
        if (pins == 0 || locked) {
            return false;
        }
        locked = true;
        hooks.stateChanged();
        return true;
    }

    public boolean unlock(Hooks hooks) {
        if (!locked) {
            return false;
        }
        locked = false;
        hooks.stateChanged();
        return true;
    }

    /**
     * Applies the current modern padlock interaction.  The caller owns the
     * target lookup; this method owns only the old shared lock-state change,
     * sound request and held-stack consumption.
     */
    public boolean tryApplyPadlock(@Nullable Player player, ItemStack held, Hooks hooks) {
        if (!(held.getItem() instanceof PadlockItem padlock) || locked) {
            return false;
        }
        int padlockPins = KeyPinItem.getPins(held);
        if (padlockPins == 0) {
            return false;
        }

        pins = padlockPins;
        lockModifier = padlock.lockMod();
        locked = true;
        hooks.stateChanged();
        if (player != null) {
            hooks.playSound(player, SOUND_LOCK_HANG, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return true;
    }

    /**
     * Returns whether the holder may use the locked carrier. The current port
     * intentionally accepts only the registered ordinary/counterfeit keys;
     * the old red-key bypass is excluded with the red-room/dungeon chain.
     */
    public boolean canAccess(@Nullable Player player, ItemStack held, RandomSource random, Hooks hooks) {
        if (!locked) {
            return true;
        }
        if (player == null) {
            return false;
        }
        if (isMatchingKey(held)) {
            hooks.playSound(player, SOUND_LOCK_OPEN, 1.0F, 1.0F);
            return true;
        }
        return tryPick(player, held, random, hooks);
    }

    public void save(CompoundTag tag) {
        tag.putInt(TAG_PINS, pins);
        tag.putBoolean(TAG_CHEESABLE, cheesable);
        tag.putBoolean(TAG_LOCKED, locked);
        tag.putDouble(TAG_LOCK_MOD, lockModifier);
    }

    /**
     * Uses the modern same-family default for a missing {@code cheesable}
     * field. New 1.20.1 worlds always write the field; this avoids turning a
     * fresh/default carrier non-counterfeitable merely because a narrow item
     * or visual snapshot lacks lock state.
     */
    public void load(CompoundTag tag) {
        pins = tag.getInt(TAG_PINS);
        locked = tag.getBoolean(TAG_LOCKED);
        lockModifier = tag.contains(TAG_LOCK_MOD) ? tag.getDouble(TAG_LOCK_MOD) : 0.1D;
        cheesable = !tag.contains(TAG_CHEESABLE) || tag.getBoolean(TAG_CHEESABLE);
    }

    private boolean isMatchingKey(ItemStack held) {
        return !held.isEmpty()
                && (held.is(ModItems.KEY.get()) || held.is(ModItems.KEY_FAKE.get()))
                && KeyPinItem.getPins(held) == pins;
    }

    private boolean tryPick(Player player, ItemStack held, RandomSource random, Hooks hooks) {
        boolean canPick = false;
        if (!held.isEmpty() && held.is(ModItems.PIN.get()) && hasScrewdriver(player)) {
            held.shrink(1);
            canPick = true;
        } else if (isScrewdriver(held) && consumeOnePin(player)) {
            canPick = true;
        }
        if (!canPick) {
            return false;
        }

        double chanceOfSuccess = lockModifier * 100.0D;
        if (isWearingLockpickJacket(player)) {
            chanceOfSuccess *= 100.0D;
        }
        if (chanceOfSuccess > random.nextDouble() * 100.0D) {
            hooks.playSound(player, SOUND_PIN_UNLOCK, 1.0F, 1.0F);
            return true;
        }
        hooks.playSound(player, SOUND_PIN_BREAK, 1.0F, 0.8F + random.nextFloat() * 0.2F);
        return false;
    }

    private static boolean hasScrewdriver(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (isScrewdriver(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isScrewdriver(ItemStack stack) {
        return stack.is(ModItems.SCREWDRIVER.get()) || stack.is(ModItems.SCREWDRIVER_DESH.get());
    }

    private static boolean consumeOnePin(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.PIN.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean isWearingLockpickJacket(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(ModItems.JACKET.get()) || chest.is(ModItems.JACKET2.get());
    }

    /**
     * Explicit adapter for the two side effects owned by a concrete carrier.
     * Implementations normally mark/save/send their own state and forward the
     * legacy ID to {@code LegacySoundPlayer}; no world behaviour is assumed by
     * this reusable state class.
     */
    public interface Hooks {
        void stateChanged();

        void playSound(Player player, String legacySoundId, float volume, float pitch);
    }
}
