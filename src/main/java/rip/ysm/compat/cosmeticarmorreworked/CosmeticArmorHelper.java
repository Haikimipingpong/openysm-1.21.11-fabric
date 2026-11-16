package rip.ysm.compat.cosmeticarmorreworked;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 时装盔甲（Cosmetic Armor Reworked）兼容层。
 * <p>
 * 该 mod 尚未提供 1.21.11 Fabric 版本，这里退化为原版装备槽：
 * 渲染时读到的就是玩家真正穿着的盔甲/鞘翅，不会出现「时装不显示、原版也不显示」的空窗。
 */
public final class CosmeticArmorHelper {

    private CosmeticArmorHelper() {
    }

    public static ItemStack getArmorItem(LivingEntity entity, EquipmentSlot slot) {
        return entity.getItemBySlot(slot);
    }

    public static ItemStack getElytraItem(LivingEntity livingEntity) {
        // 1.21.11 的鞘翅判定走数据组件 GLIDER（ElytraItem 类已删除）
        ItemStack chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        return chest.has(DataComponents.GLIDER) ? chest : ItemStack.EMPTY;
    }
}
