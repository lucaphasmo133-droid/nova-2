package dev.nova.render;

import dev.nova.module.EspModule;
import dev.nova.module.Module;
import dev.nova.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

/**
 * Zeichnet die Treffer der Highlight-Module über Minecrafts eigenes Gizmo-System
 * (dasselbe, das auch die Debug-Overlays nutzt). Die Koordinaten sind Weltkoordinaten.
 */
public final class EspRenderer {
    public static void render(LevelRenderContext ctx) {
        for (Module m : ModuleManager.all()) {
            if (m.enabled && m instanceof EspModule e && !e.targets().isEmpty()) draw(e);
        }
    }

    private static void draw(EspModule e) {
        boolean lines = !e.renderMode.is("Gefüllt");
        boolean fill = !e.renderMode.is("Outline");
        boolean top = e.throughWalls.value;
        float pulse = e.pulse();
        int outlineAlpha = alpha(e.outlineOpacity.value, pulse);
        int fillAlpha = alpha(e.fillOpacity.value, pulse);
        float width = (float) e.lineWidth.value;

        for (EspModule.Target t : e.targets()) {
            int rgb = e.baseColor(t.rgb());
            int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
            AABB box = t.box();
            if (fill && fillAlpha > 0) {
                GizmoStyle style = GizmoStyle.fill(ARGB.color(fillAlpha, r, g, b));
                if (top) Gizmos.cuboid(box, style).setAlwaysOnTop(); else Gizmos.cuboid(box, style);
            }
            if (lines && outlineAlpha > 0) {
                GizmoStyle style = GizmoStyle.stroke(ARGB.color(outlineAlpha, r, g, b), width);
                if (top) Gizmos.cuboid(box, style).setAlwaysOnTop(); else Gizmos.cuboid(box, style);
            }
        }
    }

    private static int alpha(double percent, float pulse) {
        return Math.max(0, Math.min(255, (int) Math.round(percent * 2.55 * pulse)));
    }
}
