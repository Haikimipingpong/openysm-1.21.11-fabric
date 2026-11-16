package rip.ysm.compat.sbackpack;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public final class SBackpackCompat {

    private SBackpackCompat() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static void setupRenderLayers() {
        // PORT: compat 暂为空实现
    }

    public static Optional<Pair<String, String>> getInCompatibleInfo() {
        return java.util.Optional.empty();
    }

    public static void registerControllerFunctions(CtrlBinding binding) {
        // PORT: compat 暂为空实现
    }

    public static ItemStack getBackpackItem(LivingEntity livingEntity) {
        return null;
    }
}
