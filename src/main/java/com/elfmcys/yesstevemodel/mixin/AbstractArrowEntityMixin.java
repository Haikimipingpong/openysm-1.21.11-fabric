package com.elfmcys.yesstevemodel.mixin;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractArrow.class})
public abstract class AbstractArrowEntityMixin implements ProjectileStateAccessor {

    @Unique
    private String ownerMainHandItem = StringPool.EMPTY;

    // PORT: 1.21.11 的 inGround 字段改成了 isInGround()/setInGround()（数据跟踪器）
    @Shadow
    public abstract boolean isInGround();

    @Shadow
    public int inGroundTime;

    @Override
    @Unique
    public boolean ysm$isInGround() {
        return this.isInGround();
    }

    @Override
    @Unique
    public int ysm$getInGroundTime() {
        return this.inGroundTime;
    }

    @Override
    @Unique
    public String ysm$getOwnerItemId() {
        return this.ownerMainHandItem;
    }

    @Inject(at = {@At("RETURN")}, method = {"setOwner(Lnet/minecraft/world/entity/Entity;)V"})
    private void onSetOwner(Entity entity, CallbackInfo callbackInfo) {
        Identifier key;
        if (YesSteveModel.isAvailable() && (entity instanceof LivingEntity) && (key = BuiltInRegistries.ITEM.getKey(((LivingEntity) entity).getMainHandItem().getItem())) != null) {
            this.ownerMainHandItem = key.toString();
        }
    }
}
