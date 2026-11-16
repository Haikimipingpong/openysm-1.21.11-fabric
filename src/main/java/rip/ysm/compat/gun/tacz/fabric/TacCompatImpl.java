package rip.ysm.compat.gun.tacz.fabric;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * TaCZ（Timeless &amp; Classics Zero）兼容层的 Fabric 实现。
 * <p>
 * TaCZ 是可选依赖：所有引用 TaCZ API 的代码都放在
 * {@link TacAnimationHandler} / {@link TacBindings} / {@link TacEvents} 里，
 * 只有 {@code isLoaded()} 为 true 时才会被加载，避免没装 TaCZ 时抛 NoClassDefFoundError。
 */
public final class TacCompatImpl {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("tacz");

    private TacCompatImpl() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    /** 客户端初始化时调用一次（注册枪械事件）。 */
    public static void init() {
        if (!LOADED) {
            return;
        }
        TacEvents.register();
    }

    public static void registerControllerFunctions(CtrlBinding binding) {
        if (LOADED) {
            TacBindings.registerFunctions(binding);
            return;
        }
        // 没装 TaCZ：变量仍然注册，值恒为默认，模型的 molang 不会因为缺变量报错
        binding.livingEntityVar("tac_hold_gun", ctx -> false);
        binding.livingEntityVar("tac_gun_type", ctx -> StringPool.EMPTY);
        binding.livingEntityVar("tac_gun_id", ctx -> StringPool.EMPTY);
        binding.livingEntityVar("tac_is_fire", ctx -> false);
        binding.livingEntityVar("tac_is_aim", ctx -> false);
        binding.livingEntityVar("tac_is_reload", ctx -> false);
        binding.livingEntityVar("tac_is_melee", ctx -> false);
        binding.livingEntityVar("tac_is_draw", ctx -> false);
        binding.livingEntityVar("tac_fire_mode", ctx -> StringPool.EMPTY);
    }

    public static boolean isTacGunItem(ItemStack stack) {
        return LOADED && TacAnimationHandler.isGunItem(stack);
    }

    public static void applyItemTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity,
                                          PoseStack poseStack, int packedLightIn, float partialTicks) {
        if (LOADED && TacAnimationHandler.isGunItem(stack)) {
            poseStack.pushPose();
            TacAnimationHandler.applyGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks);
            poseStack.popPose();
        }
    }

    public static PlayState handleTaczAnimState(LivingEntity entity, AnimationEvent<? extends LivingAnimatable<?>> event,
                                                String animation, ILoopType loopType) {
        if (LOADED && TacAnimationHandler.isGunItem(entity.getMainHandItem())) {
            return TacAnimationHandler.handleTaczAnimState(event, animation, loopType);
        }
        return null;
    }

    public static PlayState handleGunHoldAnimState(ItemStack stack, AnimationEvent<? extends LivingAnimatable<?>> event) {
        if (LOADED && TacAnimationHandler.isGunItem(stack)) {
            return TacAnimationHandler.handleGunHold(event, stack);
        }
        return null;
    }

    public static PlayState handleGunActionAnimState(ItemStack stack, AnimationEvent<? extends LivingAnimatable<?>> event) {
        if (LOADED && TacAnimationHandler.isGunItem(stack)) {
            return TacAnimationHandler.handleGunAction(event, stack);
        }
        return null;
    }

    public static void handleGunSound(LivingEntity entity, ItemStack stack) {
        if (LOADED && TacAnimationHandler.isGunItem(stack)) {
            TacAnimationHandler.handleGunSound(entity);
        }
    }

    public static void handleItemSound(ItemStack stack) {
        if (LOADED && TacAnimationHandler.isGunItem(stack)) {
            TacAnimationHandler.clearGunSound();
        }
    }

    public static Identifier getGunTexture(ItemStack stack) {
        if (LOADED) {
            return TacAnimationHandler.getGunId(stack);
        }
        return null;
    }
}
