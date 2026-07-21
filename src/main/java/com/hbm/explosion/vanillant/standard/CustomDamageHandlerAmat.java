package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.ICustomDamageHandler;

/** Legacy facade over the modern AMAT damage handler. */
@Deprecated(forRemoval = false)
public class CustomDamageHandlerAmat extends com.hbm.ntm.explosion.vnt.standard.CustomDamageHandlerAmat implements ICustomDamageHandler {
    public CustomDamageHandlerAmat(float radiation) { super(radiation); }
}
