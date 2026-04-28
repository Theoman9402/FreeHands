package com.theoman02.freehands.common.debug;

import com.theoman02.freehands.FreeHands;
import com.theoman02.freehands.config.FreeHandsClientConfig;
import com.theoman02.freehands.config.FreeHandsServerConfig;

public final class FreeHandsDebug {
    private FreeHandsDebug() {
    }

    public static void logClient(String message, Object... args) {
        if (FreeHandsClientConfig.DEBUG.get()) {
            FreeHands.LOGGER.info("[FREEHANDS][CLIENT] " + message, args);
        }
    }

    public static void logServer(String message, Object... args) {
        if (FreeHandsServerConfig.DEBUG.get()) {
            FreeHands.LOGGER.info("[FREEHANDS][SERVER] " + message, args);
        }
    }

    //This is to catch specifically unsimulated cases (final item move for example)
    public static void logServer(boolean isSimulated, String message, Object... args) {
        if (!isSimulated) {
            logServer(message, args);
        }
    }
}