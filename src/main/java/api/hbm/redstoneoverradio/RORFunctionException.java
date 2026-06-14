package api.hbm.redstoneoverradio;

/**
 * Legacy 1.7.10 package bridge for RoR command parsing failures.
 */
@Deprecated(forRemoval = false)
public class RORFunctionException extends com.hbm.ntm.api.redstoneoverradio.RORFunctionException {
    public RORFunctionException(String message) {
        super(message);
    }
}
