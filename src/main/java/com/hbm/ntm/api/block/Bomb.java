package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface Bomb {
    BombReturnCode explode(Level level, BlockPos pos);

    enum BombReturnCode {
        UNDEFINED(false, ""),
        DETONATED(true, "bomb.detonated"),
        TRIGGERED(true, "bomb.triggered"),
        LAUNCHED(true, "bomb.launched"),
        ERROR_MISSING_COMPONENT(false, "bomb.missingComponent"),
        ERROR_INCOMPATIBLE(false, "bomb.incompatible"),
        ERROR_NO_BOMB(false, "bomb.nobomb");

        private final boolean successful;
        private final String translationKey;

        BombReturnCode(boolean successful, String translationKey) {
            this.successful = successful;
            this.translationKey = translationKey;
        }

        public boolean wasSuccessful() {
            return successful;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public String getUnlocalizedMessage() {
            return translationKey;
        }
    }
}
