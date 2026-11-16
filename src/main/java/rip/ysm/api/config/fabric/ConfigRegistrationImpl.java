package rip.ysm.api.config.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ConfigRegistrationImpl {

    private ConfigRegistrationImpl() {
    }

    public static void register(String modId, ModConfig.Type type, ModConfigSpec spec) {
        // PORT: Forge Config API Port 5.x（NeoForge 风味）用 ConfigRegistry.INSTANCE
        ConfigRegistry.INSTANCE.register(modId, type, spec);
    }
}
