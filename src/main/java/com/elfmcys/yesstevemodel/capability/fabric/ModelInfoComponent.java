package com.elfmcys.yesstevemodel.capability.fabric;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;

public final class ModelInfoComponent implements Component {

    private final ModelInfoCapability capability = new ModelInfoCapability();

    public ModelInfoCapability capability() {
        return capability;
    }

    @Override
    public void readData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("ModelInfo", CompoundTag.CODEC).ifPresent(capability::deserializeNBT);
    }

    @Override
    public void writeData(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("ModelInfo", CompoundTag.CODEC, capability.serializeNBT());
    }
}
