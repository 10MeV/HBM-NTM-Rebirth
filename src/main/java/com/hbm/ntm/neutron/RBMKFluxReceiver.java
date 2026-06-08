package com.hbm.ntm.neutron;

public interface RBMKFluxReceiver extends RBMKNeutronColumn {
    void receiveFlux(RBMKNeutronHandler.RBMKNeutronStream stream);

    enum NType {
        FAST("trait.rbmk.neutron.fast"),
        SLOW("trait.rbmk.neutron.slow"),
        ANY("trait.rbmk.neutron.any");

        private final String translationKey;

        NType(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}
