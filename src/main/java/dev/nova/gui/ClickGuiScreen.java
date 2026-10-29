package dev.nova.gui;

import dev.nova.module.Module;
import dev.nova.module.ModuleManager;
import dev.nova.setting.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int W = 470, H = 310;
    private static final int ACCENT = 0xFF8B6CFF;
    private static final int TEXT = 0xFFE6E6F0, MUTED = 0xFF8B8B9E, TRACK = 0xFF26262F;

    private static Module selected;
    private float scroll, contentH;
    private boolean down, clickPending;
    private Object dragTarget;
    private int dragSub;

    public ClickGuiScreen() {
        super(Component.literal("Nova"));
        if (selected == null) selected = ModuleManager.all().get(0);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void extractBackground(GuiGraphicsExtractor c, int mx, int my, float delta) { c.fill(0, 0, width, height, 0x88000000); }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        down = true;
        clickPending = true;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        down = false;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        float view = H - 78 - 10;
        scroll = Math.max(0, Math.min(Math.max(0, contentH - view), scroll - (float) v * 16));
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        extractBackground(c, mx, my, delta);
        boolean click = clickPending;
        clickPending = false;
        if (!down) dragTarget = null;

        int px = (width - W) / 2, py = (height - H) / 2;
        rrect(c, px - 1, py - 1, W + 2, H + 2, 10, 0x558B6CFF);
        rrect(c, px, py, W, H, 9, 0xF2121218);
        rrect(c, px, py, W, 36, 9, 0xFF181822);
        c.fill(px, py + 28, px + W, py + 36, 0xFF181822);
        c.text(font, "NOVA", px + 16, py + 14, ACCENT, true);
        c.text(font, "RENDER", px + 52, py + 14, MUTED, false);
        c.fill(px, py + 36, px + W, py + 37, 0xFF2A2A36);

        // --- Modul-Liste ---
        List<Module> mods = ModuleManager.all();
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int rx = px + 10, ry = py + 48 + i * 32, rw = 150, rh = 26;
            boolean hover = hit(mx, my, rx, ry, rw, rh);
            if (m == selected) rrect(c, rx, ry, rw, rh, 6, 0xFF22222E);
            else if (hover) rrect(c, rx, ry, rw, rh, 6, 0xFF1B1B25);
            if (m == selected) c.fill(rx, ry + 6, rx + 2, ry + rh - 6, ACCENT);
            c.text(font, m.name, rx + 12, ry + 9, m.enabled ? TEXT : MUTED, false);
            m.anim += ((m.enabled ? 1 : 0) - m.anim) * 0.3f;
            int sx = rx + rw - 34, sy = ry + 7;
            toggle(c, sx, sy, m.anim);
            if (click && hover) {
                if (hit(mx, my, sx - 2, sy - 2, 30, 16)) m.toggle(); else selected = m;
                scroll = 0;
            }
        }
        c.fill(px + 168, py + 48, px + 169, py + H - 12, 0xFF2A2A36);

        // --- Einstellungen ---
        int rx = px + 180, rw = W - 180 - 16;
        c.text(font, selected.name, rx, py + 48, TEXT, true);
        c.text(font, selected.description, rx, py + 61, MUTED, false);
        selected.anim += ((selected.enabled ? 1 : 0) - selected.anim) * 0.3f;
        int tx = rx + rw - 26, ty = py + 50;
        toggle(c, tx, ty, selected.anim);
        if (click && hit(mx, my, tx - 2, ty - 2, 30, 16)) selected.toggle();

        int viewTop = py + 82, viewBottom = py + H - 10;
        boolean inView = my >= viewTop && my <= viewBottom;
        c.enableScissor(rx - 4, viewTop, rx + rw + 8, viewBottom);
        int sy = viewTop - (int) scroll;
        for (Setting s : selected.settings) {
            if (!s.visible.get()) continue;
            if (s instanceof Setting.Bool b) {
                c.text(font, b.name, rx, sy + 2, TEXT, false);
                b.anim += ((b.value ? 1 : 0) - b.anim) * 0.3f;
                toggle(c, rx + rw - 26, sy, b.anim);
                if (click && inView && hit(mx, my, rx, sy - 3, rw, 18)) b.value = !b.value;
                sy += 26;
            } else if (s instanceof Setting.Num n) {
                c.text(font, n.name, rx, sy, TEXT, false);
                String v = n.display();
                c.text(font, v, rx + rw - font.width(v), sy, MUTED, false);
                int by = sy + 15;
                if (click && inView && hit(mx, my, rx, by - 5, rw, 16)) { dragTarget = n; dragSub = 0; }
                if (dragTarget == n) n.setFrac(frac(mx, rx, rw));
                rrect(c, rx, by, rw, 6, 3, TRACK);
                int fw = Math.max(6, (int) (rw * n.frac()));
                rrect(c, rx, by, fw, 6, 3, ACCENT);
                rrect(c, rx + fw - 5, by - 2, 10, 10, 5, 0xFFFFFFFF);
                sy += 36;
            } else if (s instanceof Setting.Mode m) {
                c.text(font, m.name, rx, sy + 3, TEXT, false);
                int pw = 124, pxx = rx + rw - pw;
                rrect(c, pxx, sy - 3, pw, 18, 9, TRACK);
                c.text(font, "<", pxx + 8, sy + 2, MUTED, false);
                c.text(font, ">", pxx + pw - 14, sy + 2, MUTED, false);
                String v = m.get();
                c.text(font, v, pxx + (pw - font.width(v)) / 2, sy + 2, ACCENT, false);
                if (click && inView && hit(mx, my, pxx, sy - 3, pw, 18)) m.cycle(mx < pxx + pw / 2 ? -1 : 1);
                sy += 28;
            } else if (s instanceof Setting.ColorPick cp) {
                c.text(font, cp.name, rx, sy, TEXT, false);
                rrect(c, rx + rw - 26, sy - 1, 26, 10, 5, 0xFF000000 | cp.rgb());
                for (int k = 0; k < 3; k++) {
                    int by = sy + 14 + k * 13;
                    if (click && inView && hit(mx, my, rx, by - 4, rw, 14)) { dragTarget = cp; dragSub = k; }
                    if (dragTarget == cp && dragSub == k) {
                        float f = (float) frac(mx, rx, rw);
                        if (k == 0) cp.h = f; else if (k == 1) cp.s = f; else cp.b = Math.max(0.15f, f);
                    }
                    int n = rw / 3;
                    for (int i = 0; i < n; i++) {
                        float f = i / (float) (n - 1);
                        int col = k == 0 ? hsb(f, 0.85f, 1f) : k == 1 ? hsb(cp.h, f, 1f) : hsb(cp.h, cp.s, Math.max(0.15f, f));
                        c.fill(rx + i * 3, by, rx + i * 3 + 3, by + 6, col);
                    }
                    float pos = k == 0 ? cp.h : k == 1 ? cp.s : cp.b;
                    rrect(c, rx + (int) (pos * (rw - 8)), by - 2, 8, 10, 4, 0xFFFFFFFF);
                }
                sy += 58;
            }
        }
        contentH = sy + scroll - viewTop;
        c.disableScissor();

        c.text(font, "Esc: schließen  •  Mausrad: scrollen", px + 12, py + H - 14, 0xFF55556A, false);
    }

    // ---------- Helfer ----------
    private static boolean hit(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private static double frac(int mx, int x, int w) { return Math.max(0, Math.min(1, (mx - x) / (double) w)); }
    private static int hsb(float h, float s, float b) { return 0xFF000000 | (java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF); }

    private static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static void toggle(GuiGraphicsExtractor c, int x, int y, float anim) {
        rrect(c, x, y, 26, 12, 6, mix(0xFF2E2E3A, ACCENT, anim));
        rrect(c, x + 2 + (int) (anim * 14), y + 2, 8, 8, 4, 0xFFFFFFFF);
    }

    /** Abgerundetes Rechteck (Zeile für Zeile an den Ecken eingerückt). */
    private static void rrect(GuiGraphicsExtractor c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { c.fill(x, y, x + w, y + h, col); return; }
        for (int i = 0; i < r; i++) {
            double d = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - d * d)));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
        c.fill(x, y + r, x + w, y + h - r, col);
    }
}
