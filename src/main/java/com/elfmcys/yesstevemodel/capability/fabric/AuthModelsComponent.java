package com.elfmcys.yesstevemodel.capability.fabric;

import com.elfmcys.yesstevemodel.capability.AuthModelsCapability;
import org.ladysnake.cca.api.v3.component.Component;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class AuthModelsComponent implements Component {

    private final AuthModelsCapability capability = new AuthModelsCapability();

    public AuthModelsCapability capability() {
        return capability;
    }

    @Override
    public void readData(net.minecraft.world.level.storage.ValueInput input) {
        ListTag list = new ListTag();
        for (String value : input.listOrEmpty("AuthModels", Codec.STRING)) {
            list.add(StringTag.valueOf(value));
        }
        capability.deserializeNBT(list);
    }

    @Override
    public void writeData(net.minecraft.world.level.storage.ValueOutput output) {
        net.minecraft.world.level.storage.ValueOutput.TypedOutputList<String> list = output.list("AuthModels", Codec.STRING);
        for (Tag tag : capability.serializeNBT()) {
            list.add(tag.asString().orElse(""));
        }
    }
}
