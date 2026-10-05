// Core runtime module (STR-5, STR-21).
//
// Owns the loop/lifecycle, the Unity-shaped scene/component model, the
// inspection descriptors, and the single scene submission path
// (`SceneRenderer`). Depends only on the frozen `engine-api` surface so it
// stays headless-testable. It must not import Gson, LWJGL, OpenGL, or ImGui.

plugins {
    `java-library`
}

dependencies {
    api(project(":engine-api"))
}
