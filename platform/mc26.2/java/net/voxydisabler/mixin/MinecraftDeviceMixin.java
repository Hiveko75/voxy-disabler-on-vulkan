package net.voxydisabler.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuBackend;
import com.mojang.blaze3d.systems.GpuDevice;
import net.minecraft.client.Minecraft;
import net.voxydisabler.VoxyDisabler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Minecraft 26.2 version of the device hook (26.2 has the GPU backends in com.mojang.blaze3d; 26.3 moved
 * them to com.mojang.renderpearl). Same logic: if Voxy refuses to create a Vulkan device, create it anyway
 * and remember that a non-OpenGL backend is active so Voxy stays switched off for the session.
 */
@Mixin(value = Minecraft.class, priority = 2000)
public class MinecraftDeviceMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuBackend;createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;"))
    private static GpuDevice voxydisabler$keepSelectedBackend(GpuBackend backend, long window, ShaderSource shaderSource, GpuDebugOptions options, Runnable onCreate, Operation<GpuDevice> original) throws BackendCreationException {
        GpuDevice device;
        try {
            device = original.call(backend, window, shaderSource, options, onCreate);
        } catch (Exception e) {
            if (!(backend instanceof GlBackend) && e instanceof BackendCreationException && VoxyDisabler.isVoxyRefusal(e)) {
                VoxyDisabler.LOGGER.info("Voxy tried to force OpenGL; keeping the selected {} graphics API instead", backend.getName());
                device = backend.createDevice(window, shaderSource, options, onCreate);
            } else {
                throw e;
            }
        }
        VoxyDisabler.nonOpenGlBackendActive = !(backend instanceof GlBackend);
        VoxyDisabler.activeBackendName = backend.getName();
        return device;
    }
}
