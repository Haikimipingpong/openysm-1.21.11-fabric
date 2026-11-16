package com.elfmcys.yesstevemodel.capability.fabric;

import com.elfmcys.yesstevemodel.capability.ProjectileModelCapability;
import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;

public final class ProjectileModelComponent implements Component {

    private final ProjectileModelCapability capability = new ProjectileModelCapability();

    public ProjectileModelCapability capability() {
        return capability;
    }

    @Override
    public void readData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("ProjectileModel", CompoundTag.CODEC).ifPresent(capability::deserializeNBT);
    }

    @Override
    public void writeData(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("ProjectileModel", CompoundTag.CODEC, capability.serializeNBT());
    }
}
