package com.elfmcys.yesstevemodel.capability.fabric;

import com.elfmcys.yesstevemodel.capability.StarModelsCapability;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.ladysnake.cca.api.v3.component.Component;

public final class StarModelsComponent implements Component {

    private final StarModelsCapability capability = new StarModelsCapability();

    public StarModelsCapability capability() {
        return capability;
    }

    @Override
    public void readData(net.minecraft.world.level.storage.ValueInput input) {
        ListTag list = new ListTag();
        for (String value : input.listOrEmpty("StarModels", Codec.STRING)) {
            list.add(StringTag.valueOf(value));
        }
        capability.deserializeNBT(list);
    }

    @Override
    public void writeData(net.minecraft.world.level.storage.ValueOutput output) {
        net.minecraft.world.level.storage.ValueOutput.TypedOutputList<String> list = output.list("StarModels", Codec.STRING);
        for (Tag tag : capability.serializeNBT()) {
            list.add(tag.asString().orElse(""));
        }
    }
}
