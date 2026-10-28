package dev.nova.module;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.add(new StorageFinder());
        MODULES.add(new SpawnerFinder());
    }
    public static List<Module> all() { return MODULES; }
    public static void tick(Minecraft mc) {
        for (Module m : MODULES) if (m.enabled) m.onTick(mc);
    }
}
