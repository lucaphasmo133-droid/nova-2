package dev.nova.module;

import dev.nova.setting.Setting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.*;

import java.util.List;

public class StorageFinder extends EspModule {
    private final Setting.Bool chests = add(new Setting.Bool("Kisten", true));
    private final Setting.Bool trapped = add(new Setting.Bool("Trapped Chests", true));
    private final Setting.Bool ender = add(new Setting.Bool("Endertruhen", false));
    private final Setting.Bool barrels = add(new Setting.Bool("Fässer", true));
    private final Setting.Bool shulkers = add(new Setting.Bool("Shulker-Kisten", true));
    private final Setting.Bool droppers = add(new Setting.Bool("Dropper", true));
    private final Setting.Bool dispensers = add(new Setting.Bool("Dispenser", false));
    private final Setting.Bool hoppers = add(new Setting.Bool("Trichter", false));

    public StorageFinder() {
        super("Storage Finder", "Hebt Kisten, Shulker, Dropper & mehr hervor");
    }

    @Override
    protected void scan(ClientLevel level, LocalPlayer p, List<Target> out) {
        forEachBlockEntity(level, p, be -> {
            int col;
            if (be instanceof TrappedChestBlockEntity) { if (!trapped.value) return; col = 0xE05555; }
            else if (be instanceof ChestBlockEntity) { if (!chests.value) return; col = 0xF0A04B; }
            else if (be instanceof EnderChestBlockEntity) { if (!ender.value) return; col = 0xB06CFF; }
            else if (be instanceof BarrelBlockEntity) { if (!barrels.value) return; col = 0xB58355; }
            else if (be instanceof ShulkerBoxBlockEntity) { if (!shulkers.value) return; col = 0xF07AE0; }
            else if (be instanceof DropperBlockEntity) { if (!droppers.value) return; col = 0x62C8FF; }
            else if (be instanceof DispenserBlockEntity) { if (!dispensers.value) return; col = 0x4F8FD9; }
            else if (be instanceof HopperBlockEntity) { if (!hoppers.value) return; col = 0xA8A8B8; }
            else return;
            out.add(new Target(boxOf(be), col));
        });
    }
}
