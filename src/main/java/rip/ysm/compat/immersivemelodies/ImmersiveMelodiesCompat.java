package rip.ysm.compat.immersivemelodies;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

public final class ImmersiveMelodiesCompat {

    public static final class ImmersiveMelodiesData {
        public float pitch = 0f;
        public float volume = 0f;
        public float current = 0f;
        public long delta = 0L;
        public long time = 0L;
    }

    private ImmersiveMelodiesCompat() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static void updateMelodyProgress(LivingEntity livingEntity, ImmersiveMelodiesData imData) {
        // PORT: compat 暂为空实现
    }

    public static void registerBindings(CtrlBinding binding) {
        // PORT: compat 暂为空实现
    }
}
