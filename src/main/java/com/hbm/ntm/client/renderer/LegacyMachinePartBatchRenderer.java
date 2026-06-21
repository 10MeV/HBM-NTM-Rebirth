package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

final class LegacyMachinePartBatchRenderer {
    private LegacyMachinePartBatchRenderer() {
    }

    static void renderRuns(List<LegacyMachinePartRenderSelection.Run> runs, LegacyWavefrontModel model,
            ObjRenderContext context) {
        for (LegacyMachinePartRenderSelection.Run run : runs) {
            renderRun(run, model, context);
        }
    }

    private static void renderRun(LegacyMachinePartRenderSelection.Run run, LegacyWavefrontModel model,
            ObjRenderContext context) {
        List<LegacyMachinePartRenderSelection.Entry> parts = run.entries();
        LegacyMachinePartRenderSelection.Entry first = parts.get(0);
        ObjRenderContext resolved = LegacyMachinePartRenderContexts.apply(context, first.properties());
        ResourceLocation texture = first.texture();
        if (parts.size() == 1) {
            model.renderPart(first.partName(), texture, resolved);
            return;
        }
        model.renderOnlyInCallOrder(texture, resolved, run.selectionHandle(model));
    }
}
