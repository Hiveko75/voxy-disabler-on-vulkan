package net.voxydisabler.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.opengl.GlBackend;
import net.minecraft.client.Minecraft;
import net.voxydisabler.VoxyDisabler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Voxy wraps {@code GpuBackend.createDevice} and throws for Vulkan, which makes Minecraft fall back to OpenGL.
 * WrapOperations chain, and the one applied last is the outermost; mixins with a higher priority are applied
 * later. With priority 2000 (Voxy uses the default 1000) this wrapper sits outside Voxy's, so it can catch
 * Voxy's refusal and create the Vulkan device itself. Any other failure is passed on unchanged, so Minecraft's
 * normal fallback still works when Vulkan really is unavailable.
 */
@Mixin(value = Minecraft.class, priority = 2000)
public class MinecraftDeviceMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuBackend;createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;"))
    private static GpuDevice voxydisabler$keepSelectedBackend(GpuBackend backend, GpuDebugOptions options, Operation<GpuDevice> original) throws BackendCreationException {
        GpuDevice device;
        try {
            device = original.call(backend, options);
        } catch (Exception e) {
            if (!(backend instanceof GlBackend) && e instanceof BackendCreationException && VoxyDisabler.isVoxyRefusal(e)) {
                VoxyDisabler.LOGGER.info("Voxy tried to force OpenGL; keeping the selected {} graphics API instead", backend.getName());
                device = backend.createDevice(options);
            } else {
                throw e;
            }
        }
        VoxyDisabler.nonOpenGlBackendActive = !(backend instanceof GlBackend);
        VoxyDisabler.activeBackendName = backend.getName();
        return device;
    }
}
