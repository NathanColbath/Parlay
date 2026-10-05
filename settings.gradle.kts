// QA-owned build harness.
//
// The engine module layout is defined by the Lead Engineer's spec (STR-2).
// To stay decoupled from that decision, this settings file auto-discovers
// any top-level directory that contains a Gradle build file and includes it
// as a subproject. Engine modules (engine-core, engine-render, sample, ...)
// therefore light up automatically once they land, with no edit to this file.
//
// Directories that are not engine modules are ignored explicitly.

rootProject.name = "stratix-engine"

val nonModuleDirs = setOf(
    ".git", ".github", ".gradle", ".idea",
    "build", "gradle", "docs", "scripts", "config"
)

file(".")
    .listFiles { f: java.io.File -> f.isDirectory && f.name !in nonModuleDirs }
    ?.filter { f: java.io.File ->
        file("${f.name}/build.gradle.kts").exists() || file("${f.name}/build.gradle").exists()
    }
    ?.sortedBy { it.name }
    ?.forEach { include(it.name) }

// The playable Gem Dash sample lives in a *nested* module
// (`samples/gem-dash`) that the top-level auto-discovery above cannot see, so
// include it explicitly (STR-2 spec §7). Its artifact is
// `io.stratixops:gem-dash-sample`; its main class is
// `io.stratixops.gemdash.Main`.
include("samples:gem-dash")
