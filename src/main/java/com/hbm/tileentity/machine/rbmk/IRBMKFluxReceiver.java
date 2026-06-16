package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.neutron.NeutronStream;
import com.hbm.ntm.neutron.RBMKNeutronHandler;

/**
 * Legacy 1.7.10 package bridge for RBMK neutron flux receivers.
 */
@Deprecated(forRemoval = false)
public interface IRBMKFluxReceiver extends com.hbm.ntm.neutron.RBMKFluxReceiver {
    enum NType {
        FAST("trait.rbmk.neutron.fast"),
        SLOW("trait.rbmk.neutron.slow"),
        ANY("trait.rbmk.neutron.any");

        public final String unlocalized;

        NType(String translationKey) {
            this.unlocalized = translationKey;
        }

        public String translationKey() {
            return unlocalized;
        }
    }

    default void receiveFlux(NeutronStream stream) {
        com.hbm.ntm.neutron.NeutronStream modernStream = stream;
        if (modernStream instanceof RBMKNeutronHandler.RBMKNeutronStream rbmkStream) {
            receiveFlux(rbmkStream);
        }
    }
}
