package dev.nova;

import dev.nova.gui.ClickGuiScreen;
import dev.nova.module.ModuleManager;
import dev.nova.render.EspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import org.lwjgl.glfw.GLFW;

public class NovaClient implements ClientModInitializer {
    private boolean keyWasDown;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ModuleManager.tick(mc);
            long handle = GLFW.glfwGetCurrentContext();
            boolean down = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !keyWasDown && mc.screen == null && mc.level != null) {
                mc.setScreenAndShow(new ClickGuiScreen());
            }
            keyWasDown = down;
        });

        LevelRenderEvents.END_MAIN.register(EspRenderer::render);
    }
}
