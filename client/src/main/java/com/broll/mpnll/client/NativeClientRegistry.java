package com.broll.mpnll.client;

import java.util.function.Supplier;

public final class NativeClientRegistry {

    public static Supplier<NativeClient> NATIVE_CLIENT_FACTORY = null;

    public static NativeClient createClient() {
        if (NATIVE_CLIENT_FACTORY == null) {
            throw new IllegalStateException(
                "No native client factory is installed; pass a NativeClient to the MpnllClient constructor"
            );
        }
        return NATIVE_CLIENT_FACTORY.get();
    }
}
