package rip.ysm.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * PORT: 1.21.6+ 的 tooltip 改成「延迟提交」：{@code GuiGraphics#renderComponentTooltip} 被
 * {@code setComponentTooltipForNextFrame} 取代，且要传 FormattedCharSequence 列表。
 * <p>
 * YSM 的作者列表可能很长（一行几十个作者），原版 tooltip 不会自动换行 → 会超出屏幕。
 * 这里按 GUI 像素宽度手工折行。
 */
public final class PortTooltip {

    /** 单行最大宽度（GUI 像素）。1280 宽窗口的 GUI 宽度约 426，留出两侧空间。 */
    private static final int MAX_WIDTH = 220;

    private PortTooltip() {
    }

    public static void tooltip(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            wrapped.addAll(font.split(line, MAX_WIDTH));
        }
        graphics.setTooltipForNextFrame(font, wrapped, x, y);
    }
}
