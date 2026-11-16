package rip.ysm.api.capability;

import rip.ysm.api.capability.fabric.CapabilityLifecycleImpl;
import net.minecraft.world.entity.Entity;

public final class CapabilityLifecycle {

    private CapabilityLifecycle() {
    }

    public static void revive(Entity entity) {
        CapabilityLifecycleImpl.revive(entity);
    }

    public static void invalidate(Entity entity) {
        CapabilityLifecycleImpl.invalidate(entity);
    }
}
