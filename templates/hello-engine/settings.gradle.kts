// Hello Engine - onboarding template (STR-14).
//
// This is a *standalone* Gradle build. You can copy this directory anywhere and
// build it with the bundled wrapper:
//
//   Windows (cmd/PowerShell):  gradlew.bat run
//   macOS/Linux:               ./gradlew run
//
// When the template still sits inside the engine monorepo, it uses a composite
// build so it compiles against the local `engine-api` module without publishing.
// Once you copy it out, it resolves the published engine artifact instead
// (mavenLocal() then Maven Central).

rootProject.name = "hello-engine"

val monorepoRoot = file("../..")
val localEngine = monorepoRoot.resolve("engine-api")

if (monorepoRoot.resolve("settings.gradle.kts").exists() && localEngine.isDirectory) {
    includeBuild(monorepoRoot) {
        dependencySubstitution {
            substitute(module("io.stratixops:engine-api"))
                .using(project(":engine-api"))
        }
    }
}
