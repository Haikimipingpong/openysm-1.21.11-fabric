package rip.ysm.api.client.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

public final class KeyMappingFactoryImpl {

    /**
     * PORT: 1.21.11 的 KeyMapping 构造器要 KeyMapping.Category 对象，不再收分类的翻译键字符串。
     * 分类需全局注册一次。
     */
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yes_steve_model", "main"));

    private KeyMappingFactoryImpl() {
    }

    public static KeyMapping createInGameAlt(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, type, keyCode, CATEGORY);
    }

    public static KeyMapping createInGameNone(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, type, keyCode, CATEGORY);
    }

    public static boolean isActiveAndMatches(KeyMapping keyMapping, int keyCode, int scanCode) {
        return keyMapping.matches(new KeyEvent(keyCode, scanCode, 0));
    }
}
