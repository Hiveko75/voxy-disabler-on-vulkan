# Voxy Disabler on Vulkan

A small client-side Fabric add-on for [Voxy](https://modrinth.com/mod/voxy) on Minecraft 26.3+.

Voxy only renders with OpenGL, so it forces Minecraft onto the OpenGL graphics API even when you
selected Vulkan. This add-on stops that:

- **Graphics API set to Vulkan:** the game stays on Vulkan and Voxy is disabled for that session.
- **Graphics API set to OpenGL:** nothing changes; Voxy works as usual (if your GPU supports it).

**This add-on does not make Voxy work on Vulkan.** It only lets you choose Vulkan while Voxy is
installed, for example to share one mod profile between a Vulkan setup and an OpenGL setup, or on
macOS, where Voxy can't run anyway.

## How it works

- Voxy wraps Minecraft's GPU device creation and refuses Vulkan. This add-on wraps the same call
  one layer further out (mixin priority 2000), recognises Voxy's refusal and creates the Vulkan
  device directly. Any real Vulkan failure is passed on, so Minecraft's normal fallback to OpenGL
  still works.
- When the active backend is not OpenGL, it skips `VoxyClient.initVoxyClient()`. Voxy then stays
  inactive, exactly as it does on GPUs it doesn't support.

The jar contains no Voxy code or assets; Voxy's classes are only targeted by name.

You'll see this in the log when it's active:

```
[Voxy Disabler on Vulkan] Voxy tried to force OpenGL; keeping the selected Vulkan graphics API instead
[Voxy Disabler on Vulkan] Graphics API is Vulkan; Voxy only supports OpenGL and has been disabled for this session. Switch to OpenGL to use Voxy.
```

## Requirements

- Minecraft 26.3 or newer, Fabric Loader, Java 25
- Voxy (tested with 0.2.20-beta)

## Building

Requires JDK 25.

```
./gradlew build
```

The jar is written to `build/libs/`.

## License

MIT © Hiveko, see [LICENSE](LICENSE). Voxy itself is a separate, all-rights-reserved project by MCRcortex
and is not affiliated with this add-on.
