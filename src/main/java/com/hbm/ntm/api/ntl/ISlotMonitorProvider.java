package com.hbm.ntm.api.ntl;

import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticSlotMonitorProvider;

/**
 * Legacy-name bridge for pneumatic storage providers that expose slot monitors.
 */
@Deprecated(forRemoval = false)
public interface ISlotMonitorProvider extends PneumaticSlotMonitorProvider {
}
