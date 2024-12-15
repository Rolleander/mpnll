package com.broll.mpnll.client;

import java.util.function.Supplier;

public final class NativeClientRegistry {

    public static Supplier<NativeClient> NATIVE_CLIENT_FACTORY = null;

    public static NativeClient createClient() {
        return NATIVE_CLIENT_FACTORY.get();
    }
}
