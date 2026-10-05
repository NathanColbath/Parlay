// Desktop bootstrap module (STR-27): implements the EngineRuntime SPI and
// assembles the runtime from engine-core + engine-render. It is the single
// artifact users depend on and contains no gameplay or graphics logic.
//
// The build/packaging wiring here is owned by DX & Build (STR-7); the runtime
// source and the `META-INF/services` registration are owned by the Core Engine
// Coder (STR-27).

dependencies {
    api(project(":engine-core"))
    api(project(":engine-render"))
}
