package com.hbm.ntm.client.obj;

import java.util.List;

public interface LegacyObjModel {
    void renderAll(ObjRenderContext context);

    void renderPart(String name, ObjRenderContext context);

    void renderOnly(ObjRenderContext context, String... names);

    void renderAllExcept(ObjRenderContext context, String... excludedNames);

    List<String> getPartNames();
}
