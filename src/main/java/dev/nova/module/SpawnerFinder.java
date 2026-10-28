package dev.nova.module;

import dev.nova.setting.Setting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import java.util.List;

public class SpawnerFinder extends EspModule {
    private final Setting.Num maxY = add(new Setting.Num("Max. Höhe (Y)", 320, -64, 320, 8, ""));

    public SpawnerFinder() {
        super("Spawner Finder", "Hebt Monster-Spawner hervor");
        color.h = 0.0f; color.s = 0.8f; color.b = 1f;
    }

    @Override
    protected void scan(ClientLevel level, LocalPlayer p, List<Target> out) {
        forEachBlockEntity(level, p, be -> {
            if (be instanceof SpawnerBlockEntity && be.getBlockPos().getY() <= maxY.value) {
                out.add(new Target(boxOf(be), 0xFF4D6D));
            }
        });
    }
}
