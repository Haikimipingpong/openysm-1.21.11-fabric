package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/**
 * PORT: 1.21.11 删除了 StateSwitchingButton（连带 initTextureValues / isStateTriggered），
 * 这里改为直接继承 AbstractButton，自己维护勾选状态并用 blit 画贴图。
 */
@Environment(EnvType.CLIENT)
public class ConfigCheckBox extends AbstractButton implements ISpecialWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");
    private static final int TEXTURE_SIZE = 128;

    private final Consumer<Boolean> onToggle;
    private final Component label;

    private boolean stateTriggered;

    public ConfigCheckBox(int x, int y, int width, Component label, Consumer<Boolean> onToggle) {
        super(x, y, width, 12, label);
        this.label = label;
        this.onToggle = onToggle;
    }

    public ConfigCheckBox(int x, int y, Component label, Consumer<Boolean> onToggle) {
        this(x, y, 115, label, onToggle);
    }

    public boolean isStateTriggered() {
        return this.stateTriggered;
    }

    public void setStateTriggered(boolean value) {
        this.stateTriggered = value;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 原 StateSwitchingButton 用 (0,0,128,12) 作为「未选中」，(0,12) 作为「选中」
        float v = this.stateTriggered ? 12.0f / TEXTURE_SIZE : 0.0f;
        rip.ysm.gui.PortBlit.blit(guiGraphics, TEXTURE, getX(), getY(), getWidth(), getHeight(),
                0.0f, v, (float) getWidth() / TEXTURE_SIZE, (float) getHeight() / TEXTURE_SIZE);
        guiGraphics.drawString(Minecraft.getInstance().font, this.label, getX() + 14, getY() + 2, -1, false);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.stateTriggered = !this.stateTriggered;
        this.onToggle.accept(this.stateTriggered);
    }
}
