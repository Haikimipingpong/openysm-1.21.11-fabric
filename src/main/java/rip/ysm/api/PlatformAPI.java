package rip.ysm.api;

import rip.ysm.api.fabric.PlatformAPIImpl;

public final class PlatformAPI {
    private PlatformAPI() {
    }

    public static boolean isServer() {
        return PlatformAPIImpl.isServer();
    }

    public static String getPlatformName() {
        return PlatformAPIImpl.getPlatformName();
    }
}
