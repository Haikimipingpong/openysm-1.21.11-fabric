package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IconButton extends FlatColorButton {

    private static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    private final int iconU;

    private final int iconV;

    public IconButton(int x, int y, int width, int height, int iconU, int iconV, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);
        this.iconU = iconU;
        this.iconV = iconV;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        rip.ysm.gui.PortBlit.blit(guiGraphics, ICON_TEXTURE, getX() + ((this.width - 16) / 2), getY() + ((this.height - 16) / 2), 16, 16, (float)(this.iconU) / (float)(256), (float)(this.iconV) / (float)(256), (float)(16) / (float)(256), (float)(16) / (float)(256));
    }
}