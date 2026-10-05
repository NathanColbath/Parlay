// Desktop rendering platform (STR-6): GLFW window/OpenGL 3.3 context, the
// OpenGL sprite-batch renderer, texture/atlas/font loading, and the raw
// InputDevice/AudioDevice SPI implementations.
//
// It depends only on `engine-api` (the frozen, backend-neutral surface) so the
// core runtime and rendering can be built and tested in parallel (ADR-0004).
// All LWJGL/OpenGL usage is confined to this module.

val lwjglVersion = "3.3.6"

dependencies {
    api(project(":engine-api"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl-openal")

    // LWJGL auto-extracts the matching natives from the classpath; the full set
    // ships so the published artifact runs on every supported desktop (STR-7).
    runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-openal::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-openal::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-openal::natives-macos")
    runtimeOnly("org.lwjgl:lwjgl::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-macos-arm64")
    runtimeOnly("org.lwjgl:lwjgl-openal::natives-macos-arm64")
}

// Coverage scope for a GPU-backed module (STR-31; docs/qa/quality-gate.md
// "GPU-backed module coverage").
//
// Most of this module is thin LWJGL/OpenGL/GLFW/OpenAL glue whose lines can
// only execute with a live GL context and a display. Those cases are tagged
// `gpu` and are opt-in, so they do not run in the default headless `check`
// (QA gate). The headless coverage rule is therefore scoped to the
// headless-testable classes, which meet the 0.60 floor on their own:
// QuadBatch, RenderTransform, LogicalViewportLayout, RenderTargetLayout,
// Colors, MiniJson, AtlasReader/AtlasRegion, and BuiltInFontData.
//
// When the gpu suite is enabled (`-Pstratix.qa.gpuTests=true`, run by the CI
// `gpu-tests` job under xvfb) the GL classes do execute, so they are measured
// alongside the headless-testable classes (`RenderTargetGpuTest` covers the
// renderer, framebuffers, textures, shaders and batching that run against a
// live context). In both cases the enforced scope matches the suite that
// actually ran.
//
// `platform/**` (GlfwWindow, GlfwInputDevice, OpenAlAudioDevice) is raw device
// I/O: it has no automated coverage today and is excluded from *both*
// measurements. This is an explicit, tracked exclusion (STR-11 will add
// gpu-tagged device smoke tests), not an oversight.
val gpuTestsEnabled =
    (findProperty("stratix.qa.gpuTests") as String?)?.toBoolean() ?: false

val deviceIoExcludes = listOf("io/stratixops/engine/render/platform/**")

val headlessOnlyExcludes = listOf(
    "io/stratixops/engine/render/BitmapFont*.class",
    "io/stratixops/engine/render/DesktopAssets*.class",
    "io/stratixops/engine/render/GlFramebuffer*.class",
    "io/stratixops/engine/render/GlRenderer*.class",
    "io/stratixops/engine/render/GlSprite*.class",
    "io/stratixops/engine/render/GlTexture*.class",
    "io/stratixops/engine/render/GlTextureAtlas*.class",
    "io/stratixops/engine/render/LogicalViewport.class",
    "io/stratixops/engine/render/RenderTarget*.class",
    "io/stratixops/engine/render/ShaderProgram*.class",
    "io/stratixops/engine/render/SpriteBatch*.class",
) + deviceIoExcludes

val coverageExcludes = if (gpuTestsEnabled) deviceIoExcludes else headlessOnlyExcludes

tasks.withType<JacocoReport>().configureEach {
    classDirectories.setFrom(
        classDirectories.files.map { fileTree(it) { exclude(coverageExcludes) } }
    )
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    classDirectories.setFrom(
        classDirectories.files.map { fileTree(it) { exclude(coverageExcludes) } }
    )
}
