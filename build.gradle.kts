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
    apply(plugin = "java")
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
