package dev.nova;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nova.gui.ClickGuiScreen;
import dev.nova.module.ModuleManager;
import dev.nova.render.EspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;

public class NovaClient implements ClientModInitializer {
    /** 229 = rechte Shift-Taste (SDL-Scancode). In den Minecraft-Einstellungen unter "Steuerung" änderbar. */
    private static final KeyMapping OPEN_GUI = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.nova.open_gui", InputConstants.Type.KEYBOARD, 229, KeyMapping.Category.MISC));

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ModuleManager.tick(mc);
            while (OPEN_GUI.consumeClick()) {
                if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                    mc.gui.setScreen(new ClickGuiScreen());
                }
            }
        });

        LevelRenderEvents.BEFORE_GIZMOS.register(EspRenderer::render);
    }
}
