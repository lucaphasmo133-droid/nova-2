package dev.nova.setting;

import java.util.function.Supplier;

public abstract class Setting {
    public final String name;
    public Supplier<Boolean> visible = () -> true;

    protected Setting(String name) { this.name = name; }

    public static class Bool extends Setting {
        public boolean value;
        public float anim;
        public Bool(String name, boolean value) { super(name); this.value = value; this.anim = value ? 1 : 0; }
    }

    public static class Num extends Setting {
        public double value, min, max, step;
        public String suffix;
        public Num(String name, double value, double min, double max, double step, String suffix) {
            super(name); this.value = value; this.min = min; this.max = max; this.step = step; this.suffix = suffix;
        }
        public double frac() { return (value - min) / (max - min); }
        public void setFrac(double f) {
            double raw = min + f * (max - min);
            value = Math.max(min, Math.min(max, Math.round(raw / step) * step));
        }
        public String display() {
            return (step >= 1 ? String.valueOf((int) value) : String.format("%.2f", value)) + suffix;
        }
    }

    public static class Mode extends Setting {
        public final String[] modes;
        public int index;
        public Mode(String name, String... modes) { super(name); this.modes = modes; }
        public String get() { return modes[index]; }
        public boolean is(String m) { return modes[index].equals(m); }
        public void cycle(int dir) { index = (index + dir + modes.length) % modes.length; }
    }

    public static class ColorPick extends Setting {
        public float h, s, b;
        public ColorPick(String name, float h, float s, float b) { super(name); this.h = h; this.s = s; this.b = b; }
        public int rgb() { return java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF; }
    }
}
