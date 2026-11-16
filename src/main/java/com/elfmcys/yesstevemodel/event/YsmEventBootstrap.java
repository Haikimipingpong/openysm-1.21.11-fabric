package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.client.event.*;
import com.elfmcys.yesstevemodel.client.input.*;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import rip.ysm.api.PlatformAPI;
import rip.ysm.compat.gun.tacz.fabric.TacCompatImpl;

public final class YsmEventBootstrap {

    private YsmEventBootstrap() {
    }

    public static void register() {
        ServerStartupEvent.register();
        EnterServerEvent.register();
        PlayerLogoutEvent.register();
        CommonEvent.register();
        CommandRegistry.register();
        CapabilityEvent.register();
        if (!PlatformAPI.isServer()) {
            EntityJoinCallbackEvent.register();
            ClientSetupEvent.register();
            ClientTickEvent.register();
            ClientPlayerJoinNotification.register();
            ClientPlayerCloneEvent.register();
            AnimationLockEvent.register();
            PlayerSkinTextureManager.register();
            RendererManager.register();
            PlayerModelToggleKey.register();
            AnimationRouletteKey.register();
            DebugAnimationKey.register();
            ExtraPlayerRenderKey.register();
            ExtraAnimationKey.register();
            InputStateKey.register();
            // TaCZ 为可选依赖：没装时 init() 直接 return
            TacCompatImpl.init();
        }
    }
}
