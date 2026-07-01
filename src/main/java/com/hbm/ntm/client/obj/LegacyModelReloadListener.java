package com.hbm.ntm.client.obj;

import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.shader.HbmIrisExtendedShaderAccess;
import com.hbm.ntm.client.render.HbmBakedObjModelDiagnostics;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class LegacyModelReloadListener extends SimplePreparableReloadListener<Void> {
    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void ignored, ResourceManager resourceManager, ProfilerFiller profiler) {
        LegacyMachineEffectPresenter.clear();
        HbmIrisExtendedShaderAccess.invalidateShaderCache();
        HbmRenderFrameLight.invalidateCaches();
        LegacyTexturedQuadRenderer.clearSpriteCache();
        LegacyWavefrontModel.reloadAll(resourceManager);
        HbmBakedObjModelDiagnostics.reload(resourceManager);
    }
}
