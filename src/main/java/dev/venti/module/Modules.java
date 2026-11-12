package dev.venti.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class Modules {
    private static final List<Module> ALL = new ArrayList<>();
    private static boolean[] bindDown = new boolean[0];

    private Modules() {
    }

    public static <M extends Module> M register(M module) {
        ALL.add(module);
        module.settings().load();
        bindDown = new boolean[ALL.size()];
        return module;
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(ALL);
    }

    @SuppressWarnings("unchecked")
    public static <M extends Module> M get(Class<M> type) {
        for (Module module : ALL) {
            if (type.isInstance(module)) {
                return (M) module;
            }
        }
        return null;
    }

    public static void startAll() {
        for (Module module : ALL) {
            module.sync();
        }
    }

    public static void stopAll() {
        for (Module module : ALL) {
            module.shutdown();
        }
    }

    public static void tick(MinecraftClient client) {
        boolean acceptBinds = client.currentScreen == null && client.isRunning();
        long window = client.getWindow().getHandle();
        for (int i = 0; i < ALL.size(); i++) {
            Module module = ALL.get(i);
            int key = module.settings().bind;
            boolean down = key > 0 && GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            if (down && !bindDown[i] && acceptBinds) {
                module.toggle();
            }
            bindDown[i] = down;
            module.tick();
        }
    }
}
