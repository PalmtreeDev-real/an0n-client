package dev.anon.client.utils.render.postprocess;

import dev.anon.client.renderer.MeshRenderer;
import dev.anon.client.renderer.AnonRenderPipelines;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.StorageESP;

public class StorageOutlineShader extends PostProcessShader {
    private static StorageESP storageESP;

    public StorageOutlineShader() {
        super(AnonRenderPipelines.POST_OUTLINE);
    }

    @Override
    protected boolean shouldDraw() {
        if (storageESP == null) storageESP = Modules.get().get(StorageESP.class);
        return storageESP.isShader();
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        renderer.uniform("OutlineData", OutlineUniforms.write(
            storageESP.outlineWidth.get(),
            storageESP.fillOpacity.get() / 255.0f,
            storageESP.shapeMode.get().ordinal(),
            storageESP.glowMultiplier.get().floatValue()
        ));
    }
}
