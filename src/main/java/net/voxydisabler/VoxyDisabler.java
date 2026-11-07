package net.voxydisabler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VoxyDisabler {
    public static final Logger LOGGER = LoggerFactory.getLogger("Voxy Disabler on Vulkan");

    /** Exact message Voxy uses when it refuses to create a Vulkan device. */
    public static final String VOXY_REFUSAL_MESSAGE = "Voxy does not presently support vulkan";

    /** True once Minecraft has created its GPU device with a non-OpenGL backend (Vulkan). */
    public static volatile boolean nonOpenGlBackendActive = false;
    public static volatile String activeBackendName = "unknown";

    private VoxyDisabler() {}

    /** Was this exception thrown by Voxy's "force OpenGL" wrapper (and not by a real Vulkan failure)? */
    public static boolean isVoxyRefusal(Throwable t) {
        if (VOXY_REFUSAL_MESSAGE.equals(t.getMessage())) return true;
        for (StackTraceElement e : t.getStackTrace()) {
            if (e.getMethodName().contains("voxy$forceOpenglDevice")) return true;
        }
        return false;
    }
}
