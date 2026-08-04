package com.likeazusa2.strongermekasuit.advanced;

import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

public final class AdvancedMekaSuitModuleInterop {

    private AdvancedMekaSuitModuleInterop() {
    }

    public static void enqueueIMC(InterModEnqueueEvent event) {
        // Mekanism 1.20.1 picks up advanced suit compatibility through the ModuleHelper mixin below.
    }
}
