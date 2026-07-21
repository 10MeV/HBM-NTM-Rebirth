package com.hbm.ntm.item;

import com.hbm.ntm.armor.PowerArmorWeaponRuntime;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmKeybindReceiver;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Old XFactoryPA held controller; its actual capability remains armor-provided. */
public final class PowerArmorWeaponItem extends Item implements HbmKeybindReceiver {
    private final Kind kind;

    public PowerArmorWeaponItem(Properties properties, Kind kind) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        acceptClientExtensions("com.hbm.ntm.client.renderer.PowerArmorWeaponItemRendererBridge", consumer);
    }

    private static void acceptClientExtensions(String className, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName(className);
            bridge.getMethod("accept", Consumer.class).invoke(null, consumer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Missing client renderer bridge " + className, exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Client renderer bridge failed " + className, exception.getCause());
        }
    }

    @Override
    public boolean canHandleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind) {
        return WeaponConfig.gunsEnabled() && (keybind == HbmKeybind.GUN_PRIMARY || keybind == HbmKeybind.GUN_SECONDARY);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (pressed && (keybind == HbmKeybind.GUN_PRIMARY || keybind == HbmKeybind.GUN_SECONDARY)) {
            PowerArmorWeaponRuntime.activate(player, stack, kind, keybind == HbmKeybind.GUN_PRIMARY);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && selected && entity instanceof ServerPlayer player) {
            PowerArmorWeaponRuntime.tick(player, stack, kind);
        }
    }

    public enum Kind {
        MELEE,
        RANGED
    }
}
