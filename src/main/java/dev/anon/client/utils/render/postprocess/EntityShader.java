package dev.anon.client.utils.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.anon.client.mixininterface.ILevelRenderer;
import dev.anon.client.utils.render.CustomOutlineVertexConsumerProvider;
import net.minecraft.world.entity.Entity;

import static dev.anon.client.AnonClient.mc;

public abstract class EntityShader extends PostProcessShader {
    public final CustomOutlineVertexConsumerProvider vertexConsumerProvider;

    protected EntityShader(RenderPipeline pipeline) {
        super(pipeline);
        this.vertexConsumerProvider = new CustomOutlineVertexConsumerProvider();
    }

    public abstract boolean shouldDraw(Entity entity);

    @Override
    protected void preDraw() {
        ((ILevelRenderer) mc.levelRenderer).anon$pushEntityOutlineFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        ((ILevelRenderer) mc.levelRenderer).anon$popEntityOutlineFramebuffer();
    }

    public void submitVertices() {
        submitVertices(vertexConsumerProvider::draw);
    }
}
