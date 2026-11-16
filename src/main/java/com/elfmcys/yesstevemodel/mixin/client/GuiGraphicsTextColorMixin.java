package com.elfmcys.yesstevemodel.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 1.20.1 的 {@code GuiGraphics#drawString} 颜色是 RGB（alpha 被忽略，等同不透明），
 * 1.21.11 改成了 ARGB：alpha=0 就是完全透明。
 * <p>
 * YSM 里所有界面颜色都是 {@code 0x00RRGGBB} 形式（例如 {@code 15986656} = {@code 0x00F3EFE0}、
 * {@code 7829367} = {@code 0x00777777}），升级后这些文字“画了但完全看不见”，
 * 表现为「字体正常、坐标正常，但 Screen 里没有文字」。
 * <p>
 * 这里对 {@code drawString} / {@code drawStringWithBackdrop} 的**所有重载**（String / Component /
 * FormattedCharSequence，带不带 shadow 参数）统一把 alpha==0 的颜色补成不透明，
 * 模拟 1.20.1 的行为；本来就是 ARGB 的调用保持原样，不影响原版界面。
 */
@Mixin(GuiGraphics.class)
public class GuiGraphicsTextColorMixin {

    /**
     * 所有重载里 color 都是第 3 个 int 参数：this=0, Font=1, text=2, x=3, y=4, color=5。
     */
    @ModifyVariable(method = {
            "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V",
            "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",
            "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",
            "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V",
            "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            "drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"
    }, at = @At("HEAD"), argsOnly = true, index = 5)
    private int ysm$makeOpaqueIfAlphaZero(int color) {
        if ((color & 0xFF000000) == 0) {
            return color | 0xFF000000;
        }
        return color;
    }
}
