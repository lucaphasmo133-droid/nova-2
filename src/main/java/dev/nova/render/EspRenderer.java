package dev.nova.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.nova.module.EspModule;
import dev.nova.module.Module;
import dev.nova.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Zeichnet alle EspModule-Ziele. Sämtliche versionsabhängige Render-API (Pipelines, Layer,
 * Event-Kontext) steht nur in dieser Datei.
 */
public final class EspRenderer {
    private static RenderPipeline quads(String name, DepthTestFunction depth) {
        return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("nova", "pipeline/" + name))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .withDepthTestFunction(depth)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .build());
    }

    private static final RenderPipeline LINES_ND_PIPE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("nova", "pipeline/lines_nd"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build());

    private static final RenderType LINES_ND = RenderType.create("nova_lines_nd", LINES_ND_PIPE);
    private static final RenderType QUADS_ND = RenderType.create("nova_quads_nd", quads("quads_nd", DepthTestFunction.NO_DEPTH_TEST));
    private static final RenderType QUADS_D = RenderType.create("nova_quads_d", quads("quads_d", DepthTestFunction.LEQUAL_DEPTH_TEST));

    public static void render(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        PoseStack ps = ctx.poseStack();
        if (ps == null || !(ctx.bufferSource() instanceof MultiBufferSource.BufferSource buf)) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        for (Module m : ModuleManager.all()) {
            if (m.enabled && m instanceof EspModule e && !e.targets().isEmpty()) draw(e, buf, ps, cam);
        }
    }

    private static void draw(EspModule e, MultiBufferSource.BufferSource buf, PoseStack ps, Vec3 cam) {
        boolean wall = e.throughWalls.value;
        boolean lines = !e.renderMode.is("Gefüllt");
        boolean fill = !e.renderMode.is("Outline");
        float pulse = e.pulse();
        float oa = (float) (e.outlineOpacity.value / 100.0) * pulse;
        float fa = (float) (e.fillOpacity.value / 100.0) * pulse;
        RenderType lineType = wall ? LINES_ND : RenderType.lines();
        RenderType fillType = wall ? QUADS_ND : QUADS_D;

        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = ps.last().pose();

        if (fill) {
            VertexConsumer vc = buf.getBuffer(fillType);
            for (EspModule.Target t : e.targets()) filledBox(vc, mat, t.box(), e.baseColor(t.rgb()), fa);
            buf.endBatch(fillType);
        }
        if (lines) {
            VertexConsumer vc = buf.getBuffer(lineType);
            for (EspModule.Target t : e.targets()) {
                int c = e.baseColor(t.rgb());
                AABB b = t.box();
                ShapeRenderer.renderLineBox(ps, vc, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ,
                        ((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, oa);
            }
            buf.endBatch(lineType);
        }
        ps.popPose();
    }

    private static void filledBox(VertexConsumer vc, Matrix4f m, AABB b, int rgb, float alpha) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        int argb = ((int) (Math.max(0, Math.min(1, alpha)) * 255) << 24) | rgb;
        quad(vc, m, argb, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
        quad(vc, m, argb, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        quad(vc, m, argb, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        quad(vc, m, argb, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2);
        quad(vc, m, argb, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1);
        quad(vc, m, argb, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, int argb,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        vc.addVertex(m, ax, ay, az).setColor(argb);
        vc.addVertex(m, bx, by, bz).setColor(argb);
        vc.addVertex(m, cx, cy, cz).setColor(argb);
        vc.addVertex(m, dx, dy, dz).setColor(argb);
    }
}
