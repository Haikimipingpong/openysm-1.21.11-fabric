package com.elfmcys.yesstevemodel.fabric.client;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay;
import com.elfmcys.yesstevemodel.client.renderer.ExtraPlayerOverlay;
import com.elfmcys.yesstevemodel.client.renderer.ModelSyncStateOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import rip.ysm.api.client.HudOverlay;

public final class YesSteveModelFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudOverlay debugOverlay = AnimationDebugOverlay.createOverlay();
        HudOverlay loadingOverlay = new ExtraPlayerOverlay();
        HudOverlay syncOverlay = new ModelSyncStateOverlay();
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            // 1.21.11 的 HUD 元素在 Screen 之后仍会显示，界面打开时不要往界面上叠 HUD
            if (mc.screen != null) {
                return;
            }
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            // PORT: Fabric 的 HudRenderCallback 回调参数从 float 变成了 DeltaTracker
            float partialTick = tickDelta.getGameTimeDeltaPartialTick(false);
            debugOverlay.render(guiGraphics, mc.font, partialTick, w, h);
            loadingOverlay.render(guiGraphics, mc.font, partialTick, w, h);
            syncOverlay.render(guiGraphics, mc.font, partialTick, w, h);
        });

        ClientModelManager.loadDefaultModel();
    }
}
