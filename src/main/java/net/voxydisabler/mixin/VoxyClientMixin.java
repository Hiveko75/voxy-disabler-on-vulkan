package net.voxydisabler.mixin;

import net.voxydisabler.VoxyDisabler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Voxy only renders with OpenGL. {@code VoxyClient.initVoxyClient()} probes the GPU with raw OpenGL calls and,
 * if supported, registers Voxy's instance factory; every other part of Voxy checks for that factory/instance.
 * Skipping it on Vulkan leaves Voxy in the same inactive state it uses for unsupported GPUs.
 */
@Mixin(targets = "me.cortex.voxy.client.VoxyClient", remap = false)
public class VoxyClientMixin {
    @Inject(method = "initVoxyClient", at = @At("HEAD"), cancellable = true, remap = false)
    private static void voxydisabler$disableOnVulkan(CallbackInfo ci) {
        if (VoxyDisabler.nonOpenGlBackendActive) {
            VoxyDisabler.LOGGER.warn("Graphics API is {}; Voxy only supports OpenGL and has been disabled for this session. Switch to OpenGL to use Voxy.", VoxyDisabler.activeBackendName);
            ci.cancel();
        }
    }
}
