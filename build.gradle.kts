// QA-owned test & coverage harness for the StratixOps engine.
//
// Responsibilities of this file:
//   * pin the Java toolchain used by every module,
//   * make JUnit 5 (Jupiter) the test platform everywhere,
//   * produce machine-readable (XML) test results and JaCoCo coverage,
//   * enforce the MVP coverage floor as part of `check`.
//
// Production build/packaging (distributions, publishing, templates) is owned
// by the DX & Build Engineer and is intentionally not configured here.

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    java
    jacoco
}

allprojects {
    group = "io.stratixops"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// Coverage floor enforced by the MVP quality gate. Overridable per-machine or
// per-CI via -Pstratix.minLineCoverage=... (see gradle.properties).
val minLineCoverage: String =
    (findProperty("stratix.minLineCoverage") as String?) ?: "0.60"

val gpuTestsEnabled: Boolean =
    (findProperty("stratix.qa.gpuTests") as String?)?.toBoolean() ?: false

subprojects {
    // `java-library` (a superset of `java`) so engine modules can express their
    // dependency direction with `api(...)` (STR-2 §7).
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    dependencies {
        // Testing stack available to every module.
        add("testImplementation", platform("org.junit:junit-bom:5.11.3"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", "org.assertj:assertj-core:3.26.3")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            if (!gpuTestsEnabled) {
                // Display/GPU-bound tests are opt-in so headless CI stays green.
                excludeTags("gpu")
            }
        }
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        // Ensure a machine-readable report always lands for CI even on failures.
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        systemProperty("stratix.qa.gpuTests", gpuTestsEnabled.toString())
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }

    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    val testTask = tasks.named<Test>("test")

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(testTask)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    val hasMainSources = project.file("src/main/java").exists()

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(testTask)
        // Coverage only applies to modules that actually ship main sources.
        onlyIf { hasMainSources }
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = minLineCoverage.toBigDecimal()
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn("jacocoTestCoverageVerification")
    }
}

tasks.register("printQualityGate") {
    group = "verification"
    description = "Prints the active QA quality gate configuration."
    doLast {
        println("StratixOps quality gate")
        println("  java toolchain        : 17")
        println("  test platform         : JUnit 5 (Jupiter)")
        println("  min line coverage     : $minLineCoverage (per module with main sources)")
        println("  gpu/display tests     : ${if (gpuTestsEnabled) "ENABLED" else "excluded ('gpu' tag opt-in)"}")
    }
}

// ---------------------------------------------------------------------------
// DX & Build (STR-7): Maven Local publishing and distributable packaging.
//
// Owned by the DX & Build Engineer; the QA harness above is intentionally left
// untouched. The MVP distribution path is `./gradlew publishToMavenLocal`,
// which publishes the engine modules under `io.stratixops`. `assembleDist`
// produces the release archives that the DevOps release workflow collects from
// `build/distributions`. Both honor `project.version`, so
// `gradle/release-init.gradle.kts` stamps the release tag through unchanged.
// ---------------------------------------------------------------------------

// Modules that ship to users. engine-api/core/render are transitive details;
// engine-desktop is *the* user-facing artifact and gem-dash-sample is the
// playable sample. The map value is the published Maven artifactId.
val dxPublishedProjects = mapOf(
    ":engine-api" to "engine-api",
    ":engine-core" to "engine-core",
    ":engine-render" to "engine-render",
    ":engine-desktop" to "engine-desktop",
    ":samples:gem-dash" to "gem-dash-sample",
)

subprojects {
    val publishedArtifactId = dxPublishedProjects[path] ?: return@subprojects

    apply(plugin = "maven-publish")
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifactId = publishedArtifactId
            }
        }
    }
}

// The onboarding template is a *standalone* build that lives inside the
// monorepo; ship it as `hello-engine-<version>.zip` (GETTING_STARTED §3).
val templateDist by tasks.registering(Zip::class) {
    group = "distribution"
    description = "Packages the hello-engine onboarding template as a zip."
    archiveBaseName.set("hello-engine")
    // Resolve the version lazily so `gradle/release-init.gradle.kts` (which
    // stamps the release version in `projectsEvaluated`) is reflected in the
    // archive name.
    archiveVersion.set(provider { project.version.toString() })
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from("templates/hello-engine") {
        exclude("**/build/**", "**/.gradle/**")
    }
}

// Single entry point used by the release workflow and `scripts/release/*`.
tasks.register("assembleDist") {
    group = "distribution"
    description = "Builds all distributable archives into build/distributions."
    dependsOn(templateDist)
    // The playable sample ships as a runnable distribution (application plugin).
    dependsOn(":samples:gem-dash:distZip")
}
