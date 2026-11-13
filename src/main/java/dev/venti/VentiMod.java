package dev.venti;

import dev.venti.module.Modules;
import dev.venti.modules.coords.CopyCoordsModule;
import dev.venti.modules.health.LowHealthModule;
import dev.venti.modules.movement.SprintModule;
import dev.venti.modules.nametags.NameTagsModule;
import dev.venti.modules.weather.WeatherNotifierModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Registers the modules and keeps them ticking, the same way the shipped client does. */
public final class VentiMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Modules.register(new NameTagsModule());
        Modules.register(new CopyCoordsModule());
        Modules.register(new LowHealthModule());
        Modules.register(new WeatherNotifierModule());
        Modules.register(new SprintModule());

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> Modules.startAll());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Modules.stopAll());
        ClientTickEvents.END_CLIENT_TICK.register(Modules::tick);
    }
}
