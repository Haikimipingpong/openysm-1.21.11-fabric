package com.elfmcys.yesstevemodel.util.accessors;

/** PORT: 方法名加 ysm$ 前缀，避免与 1.21.11 里 AbstractArrow 自带的 isInGround() 冲突。 */
public interface ProjectileStateAccessor {
    boolean ysm$isInGround();

    int ysm$getInGroundTime();

    String ysm$getOwnerItemId();
}
