// engine-imgui: imgui-java integration, theme, and widget kit (STR-22).
//
// Backend frozen by ADR-0005: thin LWJGL 3 + OpenGL 3.3 core + imgui-java
// (docking branch). The exact imgui-java version is pinned once in
// gradle.properties (`stratix.imguiVersion`) and never anywhere else.
//
// ImGui types are an implementation detail of this module: they are declared
// with `implementation`, so they never leak onto a consumer's compile classpath
// (ADR-0005 section 2, ADR-0008 section 4). engine-imgui depends on engine-render
// for the shared GL context; that dependency is wired once the engine-render
// module API is stable, since it is still landing in the tree.

plugins {
    `java-library`
}

val imguiVersion: String =
    (findProperty("stratix.imguiVersion") as String?) ?: "1.92.7.1"
val lwjglVersion: String =
    (findProperty("stratix.lwjglVersion") as String?) ?: "3.4.1"

// Publish natives for every supported desktop target so CI can build the
// distribution once, mirroring the LWJGL native policy from ADR-0002.
// imgui-java ships one universal macOS native for both x64 and arm64, so the
// arm64 LWJGL classifier maps to the same imgui artifact.
val imguiNativeArtifacts = mapOf(
    "natives-windows" to "imgui-java-natives-windows",
    "natives-linux" to "imgui-java-natives-linux",
    "natives-macos" to "imgui-java-natives-macos",
    "natives-macos-arm64" to "imgui-java-natives-macos"
)

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")

    imguiNativeArtifacts.forEach { (classifier, artifact) ->
        runtimeOnly("io.github.spair:$artifact:$imguiVersion")
        runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$classifier")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Native/GL glue and the visual demo window are not unit-testable without a
// display; ADR-0008 section 7 allows documented coverage exclusions for native
// glue. Everything the gate measures (theme, tokens, widget geometry) stays in
// scope.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) {
                exclude("io/stratixops/engine/imgui/glue/**")
                exclude("io/stratixops/engine/imgui/demo/**")
            }
        })
    )
}
