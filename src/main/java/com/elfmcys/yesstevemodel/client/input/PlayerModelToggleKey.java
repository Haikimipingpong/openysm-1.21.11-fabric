package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.DisclaimerScreen;
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.util.InputUtil;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import rip.ysm.api.PlatformAPI;
import rip.ysm.api.client.KeyMappingFactory;

public final class PlayerModelToggleKey {

    public static final KeyMapping KEY_MAPPING = KeyMappingFactory.createInGameAlt("key.yes_steve_model.player_model.desc", InputConstants.Type.KEYSYM, 89, "key.category.yes_steve_model");

    private PlayerModelToggleKey() {
    }

    private static boolean wasDown;

    public static void register() {
        if (PlatformAPI.isServer()) {
            return;
        }
        // PORT: 1.21.11 下 Architectury 的 ClientRawInputEvent 收不到按键，改为每 tick 轮询按键状态
        dev.architectury.event.events.client.ClientTickEvent.CLIENT_POST.register(client -> {
            boolean down = KEY_MAPPING.isDown();
            if (down && !wasDown) {
                onKeyPressed();
            }
            wasDown = down;
            // 临时测试开关：-Dysm.testOpenScreen=1 时进世界自动打开模型界面
            if (System.getProperty("ysm.testOpenScreen") != null && client.player != null && client.screen == null) {
                onKeyPressed();
            }
            // 临时测试开关：-Dysm.testSwing=1 强制第三人称并周期性挥手（验证交互动画）
            if (System.getProperty("ysm.testSwing") != null && client.player != null && client.screen == null) {
                client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                if (client.player.tickCount % 40 == 20) {
                    client.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
        });
    }

    private static void onKeyPressed() {
        if (!InputUtil.isPlayerReady()) {
            return;
        }
        // 临时测试开关：-Dysm.testTextureScreen=1 直接打开贴图选择页
        if (System.getProperty("ysm.testTextureScreen") != null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                PlayerCapability.get(minecraft.player).ifPresent(cap -> {
                    net.minecraft.client.gui.screens.Screen screen =
                            GeneralConfig.TEXTURE_SCREEN_MODE.get() == GeneralConfig.TextureScreenMode.MODERN
                                    ? new rip.ysm.gui.ModernPlayerTextureScreen(null, cap.getModelId(), cap.getModelAssembly())
                                    : new com.elfmcys.yesstevemodel.client.gui.PlayerTextureScreen(null, cap.getModelId(), cap.getModelAssembly());
                    minecraft.setScreen(screen);
                });
            }
            return;
        }
        if (!YesSteveModel.isAvailable()) {
            YesSteveModel.sendUnavailableMessage();
            return;
        }
        if (NetworkHandler.isClientConnected() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
            Minecraft.getInstance().setScreen(new ExtraPlayerConfigScreen(null));
        } else if (GeneralConfig.DISCLAIMER_SHOW.get()) {
            Minecraft.getInstance().setScreen(new DisclaimerScreen());
        } else {
            Minecraft.getInstance().setScreen(new PlayerModelScreen());
        }
    }

    private static void onKeyInput(int action, int keyCode, int scanCode) {
        if (InputUtil.isPlayerReady() &&  InputUtil.isKeyPressed(keyCode, scanCode, KEY_MAPPING)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage();
                return;
            }
            if (NetworkHandler.isClientConnected() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
                Minecraft.getInstance().setScreen(new ExtraPlayerConfigScreen(null));
            } else if (GeneralConfig.DISCLAIMER_SHOW.get()) {
                Minecraft.getInstance().setScreen(new DisclaimerScreen());
            } else {
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            }
        }
    }
}