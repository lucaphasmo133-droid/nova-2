package dev.nova.module;

import dev.nova.setting.Setting;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name, description;
    public final Category category;
    public boolean enabled;
    public float anim;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }
    public void toggle() { enabled = !enabled; }
    public void onTick(Minecraft mc) {}
}
