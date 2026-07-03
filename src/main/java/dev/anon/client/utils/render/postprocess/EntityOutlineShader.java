package dev.anon.client.utils.render.postprocess;

import dev.anon.client.renderer.MeshRenderer;
import dev.anon.client.renderer.AnonRenderPipelines;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.ESP;
import net.minecraft.world.entity.Entity;

public class EntityOutlineShader extends EntityShader {
    private static ESP esp;

    public EntityOutlineShader() {
        super(AnonRenderPipelines.POST_OUTLINE);
    }

    @Override
    protected boolean shouldDraw() {
        if (esp == null) esp = Modules.get().get(ESP.class);
        return esp.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!shouldDraw()) return false;
        return !esp.shouldSkip(entity);
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        renderer.uniform("OutlineData", OutlineUniforms.write(
            esp.outlineWidth.get(),
            esp.fillOpacity.get().floatValue(),
            esp.shapeMode.get().ordinal(),
            esp.glowMultiplier.get().floatValue()
        ));
    }
}
