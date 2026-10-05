// engine-editor: the Unity-like editor application shell (STR-23).
//
// Phase 1 owns the docking shell, the panel model, the command/undo stack,
// Play-mode snapshot/restore, and the Console. It builds on the canonical
// engine-core scene model and engine-serialize so the editor and the runtime
// share one submission path (ADR-0008).
//
// The ImGui panel rendering (engine-imgui + engine-render RenderTarget) is
// added by the panel/scene-view follow-ups; the headless editor model here is
// deliberately free of GL/ImGui types so it stays unit-testable without a
// display.

plugins {
    `java-library`
}

dependencies {
    api(project(":engine-core"))
    api(project(":engine-serialize"))
}

// The headless editor model is measured; the ImGui panel layer added later is
// excluded the same way engine-imgui excludes its GL glue (ADR-0008 section 7).
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) {
                exclude("io/stratixops/engine/editor/ui/**")
            }
        })
    )
}
