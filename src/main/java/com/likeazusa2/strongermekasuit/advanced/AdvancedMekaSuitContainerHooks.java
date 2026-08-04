package com.likeazusa2.strongermekasuit.advanced;

import net.minecraftforge.eventbus.api.IEventBus;

public final class AdvancedMekaSuitContainerHooks {

    private AdvancedMekaSuitContainerHooks() {
    }

    public static void register(IEventBus modBus) {
        // Mekanism 1.20.1 still exposes most MekaSuit storage behavior through the base item implementation.
        // Keep the hook class so the project structure remains aligned; capacity overrides can be
        // reintroduced here later if they need a deeper 1.20.1-specific implementation.
    }
}

