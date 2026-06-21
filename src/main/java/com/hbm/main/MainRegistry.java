package com.hbm.main;

import com.hbm.ntm.HbmNtm;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.File;

/**
 * Minimal legacy MainRegistry facade. Registrations remain owned by modern
 * DeferredRegister classes; this class only preserves shared constants/hooks.
 */
@Deprecated(forRemoval = false)
public final class MainRegistry {
    public static final String MODID = HbmNtm.MOD_ID;
    public static final Logger logger = HbmNtm.LOGGER;
    public static final File configHbmDir = FMLPaths.CONFIGDIR.get().resolve("hbmConfig").toFile();
    public static final ServerProxy proxy = new ServerProxy();

    private MainRegistry() {
    }
}
