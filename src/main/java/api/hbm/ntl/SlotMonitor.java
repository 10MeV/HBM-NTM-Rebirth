package api.hbm.ntl;

import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticSlotMonitor;

/**
 * Legacy-package bridge for a pneumatic storage slot monitor.
 *
 * <p>The live implementation remains {@link PneumaticSlotMonitor}; this class
 * deliberately adds no parallel public mutable state. Its accessors expose the
 * corresponding modern state to migrated call sites.</p>
 */
@Deprecated(forRemoval = false)
public class SlotMonitor extends PneumaticSlotMonitor {
    public SlotMonitor(int index, ISlotMonitorProvider parent) {
        super(index, parent);
    }

    public long getLegacyStackSize() {
        return getStackSize();
    }

    public int getLegacyMeta() {
        return getDamage();
    }
}
