package rip.ysm.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.network.message.FeedbackData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public final class TouhouMaidCompat {

    private TouhouMaidCompat() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static void init() {
        // PORT: compat 暂为空实现
    }

    public static boolean isMaidEntity(Entity entity) {
        return false;
    }

    public static void handleProjectileOwner(Projectile projectile, Entity entity) {
        // PORT: compat 暂为空实现
    }

    public static void registerAnimationRoulette(Entity entity, String str, int i) {
        // PORT: compat 暂为空实现
    }

    public static void applyFeedback(Entity entity, FeedbackData message) {
        // PORT: compat 暂为空实现
    }

    @Environment(EnvType.CLIENT)
    public static void playMaidAnimation(Entity entity, String str) {
        // PORT: compat 暂为空实现
    }
}
