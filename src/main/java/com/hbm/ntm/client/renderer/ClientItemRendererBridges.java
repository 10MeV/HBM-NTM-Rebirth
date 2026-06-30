package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ClientItemRendererBridges {
    private ClientItemRendererBridges() {
    }

    public static void acceptBalefireBomb(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> BalefireBombItemRenderer.INSTANCE);
    }

    public static void acceptBombMulti(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> BombMultiItemRenderer.INSTANCE);
    }

    public static void acceptCableDiode(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> CableDiodeItemRenderer.INSTANCE);
    }

    public static void acceptFluidDuct(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> FluidDuctItemRenderer.INSTANCE);
    }

    public static void acceptGeiger(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> GeigerItemRenderer.INSTANCE);
    }

    public static void acceptHexafluorideTank(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> HexafluorideTankItemRenderer.INSTANCE);
    }

    public static void acceptLegacyDemonLamp(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyDemonLampItemRenderer.INSTANCE);
    }

    public static void acceptLegacyFan(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyFanItemRenderer.INSTANCE);
    }

    public static void acceptLegacyFileCabinet(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyFileCabinetItemRenderer.INSTANCE);
    }

    public static void acceptLegacyFloodlight(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyFloodlightItemRenderer.INSTANCE);
    }

    public static void acceptLegacyLantern(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyLanternItemRenderer.INSTANCE);
    }

    public static void acceptNuclearDevice(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> NuclearDeviceItemRenderer.INSTANCE);
    }

    public static void acceptRedCable(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> RedCableItemRenderer.INSTANCE);
    }

    public static void acceptTrinket(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> TrinketItemRenderer.INSTANCE);
    }

    public static void acceptTurret(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> TurretItemRenderer.INSTANCE);
    }

    public static void acceptVisibleMachine(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyVisibleMachineItemRenderer.INSTANCE);
    }
}
