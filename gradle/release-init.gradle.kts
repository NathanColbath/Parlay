// DevOps-owned Gradle init script used by the release pipeline.
//
// Why an init script and not a build.gradle.kts edit: the root build harness is
// QA-owned. This file lets CI/CD stamp a release version onto every module
// without touching that harness.
//
// Usage (release workflow):
//   ./gradlew build --init-script gradle/release-init.gradle.kts -Pstratix.version=0.1.0
//
// Resolution order:
//   1. -Pstratix.version=<value>
//   2. STRATIX_VERSION environment variable
//   3. no override (development default from build.gradle.kts is kept)
//
// It runs in projectsEvaluated so it wins over the hard-coded development
// version after all build scripts have been configured.

val cliProperties = gradle.startParameter.projectProperties

gradle.projectsEvaluated {
    val cliVersion = cliProperties["stratix.version"]?.takeIf { it.isNotBlank() }
    val envVersion = System.getenv("STRATIX_VERSION")?.takeIf { it.isNotBlank() }
    val releaseVersion = cliVersion ?: envVersion ?: return@projectsEvaluated

    gradle.rootProject.allprojects {
        version = releaseVersion
    }

    println("release-init: stamped version '$releaseVersion' onto all projects")
}
