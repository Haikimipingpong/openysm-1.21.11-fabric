package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public class DisclaimerScreen extends Screen {

    private Checkbox checkbox;

    private int textY;

    private int textHeight;

    public DisclaimerScreen() {
        super(Component.literal("Disclaimer GUI"));
    }

    public void init() {
        clearWidgets();
        int size = this.font.split(Component.translatable("gui.yes_steve_model.disclaimer.text"), 400).size();
        Objects.requireNonNull(this.font);
        int i = (size * 9) + 20 + 20 + 10 + 20;
        this.textY = (this.width - 400) / 2;
        this.textHeight = (this.height - i) / 2;
        MutableComponent mutableComponentTranslatable = Component.translatable("gui.yes_steve_model.disclaimer.read");
        int iWidth = this.font.width(mutableComponentTranslatable);
        // PORT: Checkbox 构造器非 public，改 builder
        this.checkbox = Checkbox.builder(mutableComponentTranslatable, this.font)
                .pos((this.width - iWidth) / 2, (this.textHeight + i) - 50)
                .selected(!GeneralConfig.DISCLAIMER_SHOW.get().booleanValue())
                .build();
        addRenderableWidget(this.checkbox);
        addRenderableWidget(new Button.Builder(Component.translatable("gui.yes_steve_model.disclaimer.close"), button -> {
            if (this.checkbox.selected()) {
                GeneralConfig.DISCLAIMER_SHOW.set(false);
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        }).size(300, 20).pos((this.width - 300) / 2, (this.textHeight + i) - 20).build());
    }

    /** PORT: 1.21.11 的 Screen#render 会内部调用 renderBackground（画暗色遮罩），
     *  会把本界面自己画的内容盖住；这里改为空实现，由界面自行绘制背景。 */
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // PORT: 1.21.11 的 Screen#render 内部会调用 renderBackground（每帧只允许模糊一次），这里不再重复调用

        // PORT: 1.21.11 的 drawWordWrap 签名变化，先退化为单行绘制
        guiGraphics.drawString(this.font, Component.translatable("gui.yes_steve_model.disclaimer.text"), this.textY, this.textHeight, -1, false);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}