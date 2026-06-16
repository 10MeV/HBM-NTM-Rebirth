package com.hbm.handler.neutron;

import com.hbm.ntm.neutron.NeutronNode;
import com.hbm.ntm.neutron.NeutronType;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 package bridge for neutron streams.
 */
@Deprecated(forRemoval = false)
public abstract class NeutronStream extends com.hbm.ntm.neutron.NeutronStream {
    protected NeutronStream(NeutronNode origin, Vec3 vector) {
        super(origin, vector);
    }

    protected NeutronStream(NeutronNode origin, Vec3 vector, double fluxQuantity, double fluxRatio,
            NeutronType type) {
        super(origin, vector, fluxQuantity, fluxRatio, type);
    }
}
