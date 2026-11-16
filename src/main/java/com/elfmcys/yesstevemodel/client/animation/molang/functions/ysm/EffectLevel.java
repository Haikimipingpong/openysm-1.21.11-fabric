package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;

public class EffectLevel extends com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction {
    @Override
    public Object eval(com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext<com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext<net.minecraft.world.entity.Entity>> context,
                       ArgumentCollection arguments) {
        // PORT: 药水效果查询涉及 Holder<MobEffect> 重构，暂时恒 false
        return false;
    }
}
