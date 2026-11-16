package rip.ysm.compat.touhoulittlemaid;

import net.minecraft.world.entity.Entity;

import java.util.Optional;

public final class MaidCapabilityBridge {

    private MaidCapabilityBridge() {
    }

    public static Optional<Object> get(Entity entity) {
        return java.util.Optional.empty();
    }
}
