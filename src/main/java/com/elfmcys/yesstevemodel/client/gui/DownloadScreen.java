package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class DownloadScreen extends Screen {

    private final PlayerModelScreen parentScreen;

    private int guiLeft;

    private int guiTop;

    public DownloadScreen(PlayerModelScreen modelScreen) {
        super(Component.literal("YSM Config GUI"));
        this.parentScreen = modelScreen;
    }

    public void init() {
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        addRenderableWidget(new FlatColorButton(this.guiLeft + 5, this.guiTop, 80, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> {
            Minecraft.getInstance().setScreen(this.parentScreen);
        }));
    }

    /** PORT: 1.21.11 的 Screen#render 会内部调用 renderBackground（画暗色遮罩），
     *  会把本界面自己画的内容盖住；这里改为空实现，由界面自行绘制背景。 */
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // PORT: 1.21.11 的 Screen#render 内部会调用 renderBackground（每帧只允许模糊一次），这里不再重复调用

        guiGraphics.drawCenteredString(this.font, "Coming Soooooooooooooooooooooooooon™", this.width / 2, (this.height / 2) - 5, ChatFormatting.DARK_RED.getColor().intValue());
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
