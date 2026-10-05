// Gem Dash - the playable StratixOps sample game (STR-2 spec §6/§7).
//
// Published as `io.stratixops:gem-dash-sample` and launched by the onboarding
// template's `runSample` task (see docs/GETTING_STARTED.md). The entry point is
// `io.stratixops.gemdash.Main`.
//
// The playable game content is owned by the Designers / sample implementer; this
// module owns its build wiring and the `application` distribution so it can be
// run and packaged.

plugins {
    application
}

// The Gradle project is `gem-dash`; the published artifact is
// `gem-dash-sample` (the root publishing block sets the Maven artifactId too).
base {
    archivesName.set("gem-dash-sample")
}

dependencies {
    // The user-facing engine artifact: pulls engine-api + engine-core +
    // engine-render (and the LWJGL natives) transitively.
    implementation(project(":engine-desktop"))
}

application {
    mainClass.set("io.stratixops.gemdash.Main")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "io.stratixops.gemdash.Main")
    }
}

// The sample is a runnable artifact, not engine runtime logic: exclude it from
// the per-module line-coverage floor (see QA quality gate, STR-4). Gameplay
// behavior is exercised end to end by the sample smoke test (STR-12).
tasks.named("jacocoTestCoverageVerification") {
    enabled = false
}
