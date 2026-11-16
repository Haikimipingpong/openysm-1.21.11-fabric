package rip.ysm.compat.gun.tacz.fabric;

import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunMeleeEvent;
import com.tacz.guns.api.event.common.GunReloadEvent;

/**
 * TaCZ 在 Fabric 上把 Forge 事件改成了 Fabric {@code Event} API（{@code CALLBACK}）。
 * 开火 / 近战 / 换弹时给玩家 capability 打一个标记，让下一次动画判定立刻切到动作动画。
 */
final class TacEvents {

    private TacEvents() {
    }

    static void register() {
        GunFireEvent.CALLBACK.register(event -> TacBindings.markGunAction(event.getShooter()));
        GunMeleeEvent.CALLBACK.register(event -> TacBindings.markGunAction(event.getShooter()));
        GunReloadEvent.CALLBACK.register(event -> TacBindings.markGunAction(event.getEntity()));
    }
}
