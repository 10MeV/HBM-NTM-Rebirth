package api.hbm.energymk2;

/**
 * Legacy 1.7.10 package bridge for Energy MK2 providers and receivers.
 */
@Deprecated(forRemoval = false)
public interface IEnergyHandlerMK2 extends com.hbm.ntm.energy.IEnergyHandlerMK2, IEnergyConnectorMK2,
        api.hbm.tile.ILoadedTile {
}
