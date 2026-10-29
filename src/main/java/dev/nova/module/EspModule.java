package dev.nova.module;

import dev.nova.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Gemeinsame Basis für alle Highlight-Module (Farbe, Deckkraft, Modi ...). */
public abstract class EspModule extends Module {
    public record Target(AABB box, int rgb) {}

    public final Setting.Mode renderMode = add(new Setting.Mode("Darstellung", "Outline", "Gefüllt", "Beides"));
    public final Setting.Mode colorMode = add(new Setting.Mode("Farbmodus", "Statisch", "Regenbogen", "Pulsierend", "Nach Typ"));
    public final Setting.ColorPick color = add(new Setting.ColorPick("Farbe", 0.62f, 0.65f, 1f));
    public final Setting.Num outlineOpacity = add(new Setting.Num("Outline-Deckkraft", 100, 0, 100, 5, "%"));
    public final Setting.Num fillOpacity = add(new Setting.Num("Füllungs-Deckkraft", 25, 0, 100, 5, "%"));
    public final Setting.Num lineWidth = add(new Setting.Num("Linienstärke", 3, 1, 10, 1, ""));
    public final Setting.Num expand = add(new Setting.Num("Box-Größe (+)", 0.0, 0.0, 0.3, 0.01, ""));
    public final Setting.Num range = add(new Setting.Num("Reichweite", 128, 16, 256, 8, " m"));
    public final Setting.Bool throughWalls = add(new Setting.Bool("Durch Wände", true));

    private List<Target> targets = List.of();
    private int tickCounter;

    protected EspModule(String name, String description) {
        super(name, description, Category.RENDER);
        color.visible = () -> colorMode.is("Statisch") || colorMode.is("Pulsierend");
        lineWidth.visible = () -> !renderMode.is("Gefüllt");
        outlineOpacity.visible = () -> !renderMode.is("Gefüllt");
        fillOpacity.visible = () -> !renderMode.is("Outline");
    }

    protected abstract void scan(ClientLevel level, LocalPlayer player, List<Target> out);

    public List<Target> targets() { return targets; }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.level == null || mc.player == null) { targets = List.of(); return; }
        if (tickCounter++ % 10 == 0) {
            List<Target> list = new ArrayList<>();
            scan(mc.level, mc.player, list);
            targets = list;
        }
    }

    protected void forEachBlockEntity(ClientLevel level, LocalPlayer p, Consumer<BlockEntity> action) {
        double r = range.value, r2 = r * r;
        int cr = (int) Math.ceil(r / 16.0);
        ChunkPos cp = p.chunkPosition();
        for (int dx = -cr; dx <= cr; dx++) {
            for (int dz = -cr; dz <= cr; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x() + dx, cp.z() + dz);
                if (chunk == null) continue;
                for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (be.getBlockPos().distToCenterSqr(p.getX(), p.getY(), p.getZ()) <= r2) action.accept(be);
                }
            }
        }
    }

    protected AABB boxOf(BlockEntity be) { return new AABB(be.getBlockPos()).inflate(expand.value); }

    public int baseColor(int typeRgb) {
        return switch (colorMode.get()) {
            case "Regenbogen" -> java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 5000) / 5000f, 0.75f, 1f) & 0xFFFFFF;
            case "Nach Typ" -> typeRgb;
            default -> color.rgb();
        };
    }

    public float pulse() {
        return colorMode.is("Pulsierend") ? (float) (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 350.0))) : 1f;
    }
}
