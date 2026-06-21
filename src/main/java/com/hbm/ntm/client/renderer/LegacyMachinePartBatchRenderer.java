package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import java.util.List;

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
        model.renderOnlyInCallOrder(first.texture(), resolved, run.selectionHandle(model));
    }
}
