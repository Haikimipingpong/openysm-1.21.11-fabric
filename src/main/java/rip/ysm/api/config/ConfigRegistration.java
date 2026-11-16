package rip.ysm.api.config;

import rip.ysm.api.config.fabric.ConfigRegistrationImpl;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;

public final class ConfigRegistration {

    private ConfigRegistration() {
    }

    public static void register(String modId, ModConfig.Type type, ModConfigSpec spec) {
        ConfigRegistrationImpl.register(modId, type, spec);
    }
}
