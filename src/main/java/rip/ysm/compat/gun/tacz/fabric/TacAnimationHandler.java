package rip.ysm.compat.gun.tacz.fabric;

import com.elfmcys.yesstevemodel.client.renderer.RenderContext;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rip.ysm.compat.gun.tacz.ConditionTAC;

import java.util.Locale;
import java.util.Optional;

/**
 * TaCZ 枪械驱动的动画状态机（对应上游 forge 侧的 TacAnimHandler）。
 * 只有装了 TaCZ 时这个类才会被加载。
 */
final class TacAnimationHandler {

    private static final boolean DEBUG = Boolean.getBoolean("ysm.debugTacz");

    private static void dbg(String msg) {
        if (DEBUG) {
            System.out.println("[YSM-TACZ] " + msg);
        }
    }

    private TacAnimationHandler() {
    }

    static boolean isGunItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof IGun;
    }

    /** 把枪模型画在模型自带的 tac_pistol / tac_rifle 骨骼上（模型没这些骨骼就不做事）。 */
    static void applyGunTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack,
                                  int packedLightIn, float partialTicks) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return;
        }
        Optional<CommonGunIndex> indexOptional = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOptional.isEmpty()) {
            return;
        }
        String type = indexOptional.get().getType();
        SubmitNodeCollector collector = RenderContext.collector();
        if (collector == null) {
            return;
        }
        ItemInHandRenderer itemRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        if (isGunType(type, GunTabType.PISTOL) && !model.tacPistolBones().isEmpty()) {
            RenderUtils.prepMatrixForLocator(poseStack, model.tacPistolBones());
            poseStack.translate(0.0d, -0.125d, 0.0d);
            poseStack.scale(0.65f, 0.65f, 0.65f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
            itemRenderer.renderItem(entity, stack, ItemDisplayContext.FIXED, poseStack, collector, packedLightIn);
        }
        if (!isGunType(type, GunTabType.PISTOL) && !model.tacRifleBones().isEmpty()) {
            RenderUtils.prepMatrixForLocator(poseStack, model.tacRifleBones());
            poseStack.scale(0.65f, 0.65f, 0.65f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-180.0f));
            itemRenderer.renderItem(entity, stack, ItemDisplayContext.FIXED, poseStack, collector, packedLightIn);
        }
    }

    /** 「tac:xxx」优先；没有对应动画就退回模型自带的同名动画。 */
    static PlayState handleTaczAnimState(AnimationEvent<?> event, String animation, ILoopType loopType) {
        String taczAnimation = "tac:" + animation;
        if (event.getAnimatable().getAnimation(taczAnimation) != null) {
            return setAnimationAndContinue(event, taczAnimation, loopType);
        }
        return setAnimationAndContinue(event, animation, loopType);
    }

    /** 持枪待机：瞄准 / 跑动 / 游泳爬行 / 普通持枪。 */
    static PlayState handleGunHold(AnimationEvent<? extends com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?>> event,
                                   ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return PlayState.STOP;
        }
        Optional<CommonGunIndex> indexOptional = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOptional.isEmpty()) {
            return PlayState.STOP;
        }
        String type = indexOptional.get().getType();
        LivingEntity entity = ((com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?>) event.getAnimatable()).getEntity();
        IGunOperator operator = IGunOperator.fromLivingEntity(entity);
        dbg("hold type=" + type + " operator=" + (operator == null ? "null" : "ok")
                + " swim=" + entity.isSwimming() + " pose=" + entity.getPose()
                + " sprint=" + entity.isSprinting() + " onGround=" + entity.onGround()
                + " aiming=" + (operator == null ? -1 : operator.getSynAimingProgress()));
        if (operator == null) {
            return PlayState.STOP;
        }
        if (!entity.isSwimming() && entity.getPose() == Pose.SWIMMING) {
            if (Math.abs(event.getLimbSwingAmount()) > 0.05d) {
                return playGunAnimation(event, type, "tac:climb:");
            }
            return playGunAnimation(event, type, "tac:climbing:");
        }
        if (operator.getSynAimingProgress() > 0.0f) {
            return playGunAnimation(event, type, "tac:aim:");
        }
        if (entity.onGround() && entity.isSprinting()) {
            return playGunAnimation(event, type, "tac:run:");
        }
        return playGunAnimation(event, type, "tac:hold:");
    }

    /** 开枪 / 换弹 / 近战：带冷却时间判定，播完由控制器继续。 */
    static PlayState handleGunAction(AnimationEvent<? extends com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?>> event,
                                     ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return PlayState.STOP;
        }
        Optional<CommonGunIndex> indexOptional = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOptional.isEmpty()) {
            return PlayState.STOP;
        }
        String type = indexOptional.get().getType();
        com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?> animatable =
                (com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?>) event.getAnimatable();
        LivingEntity entity = animatable.getEntity();
        IGunOperator operator = IGunOperator.fromLivingEntity(entity);
        if (operator == null) {
            return PlayState.STOP;
        }
        long shootCoolDown = operator.getSynShootCoolDown();
        if (entity instanceof LocalPlayer localPlayer) {
            IClientPlayerGunOperator clientOperator = IClientPlayerGunOperator.fromLocalPlayer(localPlayer);
            if (clientOperator != null) {
                shootCoolDown = Math.max(shootCoolDown, clientOperator.getClientShootCoolDown());
            }
        }
        if (animatable.isExtraRenderFlag()) {
            playLoopAnimation(event, "empty");
        }
        animatable.setExtraRenderFlag(false);
        if (operator.getSynReloadState().getCountDown() > 0) {
            return playGunAnimationWithMode(event, type, "tac:reload:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        if (operator.getSynMeleeCoolDown() > 0) {
            return playGunAnimationWithMode(event, type, "tac:melee:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        if (shootCoolDown > 0) {
            float aimingProgress = operator.getSynAimingProgress();
            if (!entity.isSwimming() && entity.getPose() == Pose.SWIMMING && Math.abs(event.getLimbSwingAmount()) <= 0.05d) {
                return playGunAnimationWithMode(event, type, "tac:climbing:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            if (aimingProgress > 0.0f) {
                return playGunAnimationWithMode(event, type, "tac:aim:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            return playGunAnimationWithMode(event, type, "tac:hold:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        return PlayState.CONTINUE;
    }

    /** 本机玩家开火时，TaCZ 的枪口火焰/抛壳需要标记为「自己」，否则会被当作他人渲染。 */
    static void handleGunSound(LivingEntity entity) {
        if (entity.equals(Minecraft.getInstance().player)) {
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
        }
    }

    static void clearGunSound() {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }

    @Nullable
    static net.minecraft.resources.Identifier getGunId(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun == null ? null : gun.getGunId(stack);
    }

    @NotNull
    private static PlayState playGunAnimation(AnimationEvent<?> event, String type, String prefix) {
        return playGunAnimationWithMode(event, type, prefix, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @NotNull
    private static PlayState playGunAnimationWithMode(AnimationEvent<?> event, String type, String prefix, ILoopType loopType) {
        if (event.getAnimatable() instanceof com.elfmcys.yesstevemodel.client.entity.LivingAnimatable<?> animatable) {
            // 模型还没加载完时 getModelConfig() 可能是 null，这里整体兜底，不能影响正常动画
            ConditionTAC tac = null;
            try {
                tac = animatable.getModelConfig() == null ? null : animatable.getModelConfig().getTAC();
            } catch (RuntimeException ignored) {
                tac = null;
            }
            if (tac != null) {
                String matched = tac.doTest(animatable.getEntity().getMainHandItem(), prefix);
                if (StringUtils.isNoneBlank(matched)) {
                    return setAnimationAndContinue(event, matched, loopType);
                }
            }
        }
        String chosen = isGunType(type, GunTabType.PISTOL) ? prefix + "pistol"
                : isGunType(type, GunTabType.RPG) ? prefix + "rpg" : prefix + "rifle";
        boolean exists = event.getAnimatable().getAnimation(chosen) != null;
        dbg("anim-> " + chosen + " exists=" + exists + " (tac match was null)");
        return setAnimationAndContinue(event, chosen, loopType);
    }

    @NotNull
    private static PlayState playLoopAnimation(AnimationEvent<?> event, String animation) {
        return setAnimationAndContinue(event, animation, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @NotNull
    private static PlayState setAnimationAndContinue(AnimationEvent<?> event, String animation, ILoopType loopType) {
        event.getController().setAnimation(animation, loopType);
        return PlayState.CONTINUE;
    }

    private static boolean isGunType(String type, GunTabType gunTabType) {
        return type != null && type.equals(gunTabType.name().toLowerCase(Locale.ENGLISH));
    }
}
