package api.hbm.energymk2;

/**
 * Legacy 1.7.10 package bridge for the Energy MK2 network facade.
 */
@Deprecated(forRemoval = false)
public class PowerNetMK2 extends com.hbm.ntm.energy.PowerNetMK2 {
    public PowerNetMK2() {
        super();
    }

    public PowerNetMK2(long timeoutMs) {
        super(timeoutMs);
    }
}
