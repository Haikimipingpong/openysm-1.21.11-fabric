package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios;

import rip.ysm.compat.curios.CuriosCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.util.ThreadLocalItemTagSets;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;

public class HasAnyCurios extends LivingEntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        // PORT: 降级
        return false;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size > 0;
    }
}
