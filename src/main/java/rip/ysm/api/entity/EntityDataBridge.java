package rip.ysm.api.entity;

import rip.ysm.api.entity.fabric.EntityDataBridgeImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class EntityDataBridge {

    private EntityDataBridge() {
    }

    public static CompoundTag getPersistentData(Entity entity) {
        return EntityDataBridgeImpl.getPersistentData(entity);
    }

    public static boolean shouldRiderSit(Entity vehicle) {
        return EntityDataBridgeImpl.shouldRiderSit(vehicle);
    }
}
